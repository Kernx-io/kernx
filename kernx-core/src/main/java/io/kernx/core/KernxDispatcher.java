/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core;

import io.kernx.core.protocol.KernxPacket;
import io.kernx.core.state.AgentRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KernxDispatcher {

    private final ExecutorService vThreadExecutor;
    private final AgentRegistry registry;

    public KernxDispatcher() {
        this.vThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.registry = new AgentRegistry();
    }

    public void dispatch(KernxPacket packet) {
        vThreadExecutor.submit(() -> process(packet));
    }

    private void process(KernxPacket packet) {
        String payload = new String(packet.payload().array());

        // --- COMMAND: DEPLOY ---
        if (payload.startsWith("DEPLOY")) {
            String agentId = payload.substring(7).trim();
            // FIX: Use 'spawn' instead of 'register'
            registry.spawn(agentId);
            System.out.println("[KERNEL] 🚀 DEPLOYED: " + agentId);
        } 
        
        // --- COMMAND: KILL ---
        else if (payload.startsWith("KILL")) {
            String agentId = payload.substring(5).trim();
            // FIX: Use 'kill' instead of 'unregister'
            registry.kill(agentId);
            System.out.println("[KERNEL] 💀 KILLED: " + agentId);
        }
        
        // --- COMMAND: STATUS ---
        else if (payload.equalsIgnoreCase("STATUS")) {
            System.out.println("[KERNEL] 📊 STATE: " + registry.dumpState());
        }

        // --- COMMAND: MSG (Routing Logic) ---
        else if (payload.startsWith("MSG")) {
            String[] parts = payload.split(" ", 3);
            if (parts.length < 3) return;
            
            String targetId = parts[1];
            String message = parts[2];
            
            var actor = registry.get(targetId);
            if (actor != null) {
                // Forward to the Actor's private mailbox
                actor.send(io.kernx.core.protocol.KernxPacket.create("Router", message.getBytes()));
            } else {
                System.out.println("[KERNEL] ⚠️ 404 Agent Not Found: " + targetId);
            }
        }
        
        // --- DATA INGEST ---
        else {
            System.out.println("[DATA] 💾 Processed " + packet.payload().limit() + " bytes");
        }
    }
}