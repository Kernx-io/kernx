/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core;

import io.kernx.core.protocol.KernxPacket;
import io.kernx.core.state.AgentRegistry;
import io.kernx.core.state.ResultStore;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class KernxDispatcher {

    private final AgentRegistry registry = new AgentRegistry();
    private final Set<String> identityBlocklist = ConcurrentHashMap.newKeySet();
    private final List<byte[]> binarySignatures = new CopyOnWriteArrayList<>();
    private volatile boolean dpiEnabled = false; 

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong rejectedRequests = new AtomicLong(0);
    private final AtomicLong blockedRequests = new AtomicLong(0);
    private final Instant startTime = Instant.now();

    public void dispatch(KernxPacket packet) {
        totalRequests.incrementAndGet();
        byte[] payload = packet.payload().array();

        // --- LAYER 1: DEEP PACKET INSPECTION ---
        if (dpiEnabled && !binarySignatures.isEmpty()) {
            for (byte[] signature : binarySignatures) {
                if (containsSequence(payload, signature)) {
                    blockedRequests.incrementAndGet();
                    // SILENCED LOG
                    ResultStore.INSTANCE.put(packet.id(), "{\"error\": \"MALWARE_DETECTED\"}");
                    throw new SecurityException("BINARY_SIGNATURE_BLOCK");
                }
            }
        }

        String msg = new String(payload, StandardCharsets.UTF_8);

        // --- LAYER 2: IDENTITY FIREWALL ---
        String[] tokens = msg.split(" ");
        if (tokens.length > 1) {
            String targetAgent = tokens[1];
            if (identityBlocklist.contains(targetAgent)) {
                blockedRequests.incrementAndGet();
                // SILENCED LOG
                ResultStore.INSTANCE.put(packet.id(), "{\"error\": \"BLOCKED_BY_ADMIN\"}");
                throw new SecurityException("BLOCKED_BY_ADMIN");
            }
        }

        // --- COMMANDS ---

        if (msg.startsWith("MSG")) {
            if (tokens.length < 3) return;
            String agentId = tokens[1];
            String message = msg.substring(msg.indexOf(tokens[2])); 

            var actor = registry.get(agentId);
            if (actor != null) {
                var newPacket = new KernxPacket(
                    packet.id(), "Router", Instant.now(), 
                    ByteBuffer.wrap(message.getBytes()), Collections.emptyMap()
                );

                boolean accepted = actor.offer(newPacket);

                if (!accepted) {
                    rejectedRequests.incrementAndGet();
                    // SILENCED LOG
                    throw new IllegalStateException("ACTOR_OVERLOADED");
                }
                // SILENCED: System.out.println("[KERNEL] ➡️ Routed to: " + agentId);
            } else {
                ResultStore.INSTANCE.put(packet.id(), "AGENT_NOT_FOUND");
            }
        }

        // Keep Control Plane logs (These are rare, so they are fine)
        else if (msg.startsWith("STATS")) {
            long uptime = java.time.Duration.between(startTime, Instant.now()).toSeconds();
            String report = """
                { "uptime": %d, "processed": %d, "rejected": %d, "active_agents": %d }
                """.formatted(uptime, totalRequests.get(), rejectedRequests.get(), registry.count());
            ResultStore.INSTANCE.put(packet.id(), report);
        }
        else if (msg.startsWith("DEPLOY")) {
            registry.register(tokens[1]);
            ResultStore.INSTANCE.put(packet.id(), "DEPLOY_SUCCESS");
        }
        else if (msg.startsWith("CONFIG")) {
             // Config logic here (Keep it simple)
        }
        else if (msg.startsWith("BLOCK")) {
            identityBlocklist.add(tokens[1]);
            registry.remove(tokens[1]); 
        }
    }

    private boolean containsSequence(byte[] source, byte[] match) {
        if (match.length == 0 || source.length < match.length) return false;
        for (int i = 0; i <= source.length - match.length; i++) {
            boolean found = true;
            for (int j = 0; j < match.length; j++) {
                if (source[i + j] != match[j]) { found = false; break; }
            }
            if (found) return true;
        }
        return false;
    }
}