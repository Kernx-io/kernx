/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core.actor;

import io.kernx.core.protocol.KernxPacket;
import org.jctools.queues.MpscUnboundedArrayQueue;
import java.util.Queue;

/**
 * The "Living" Unit of the Kernel.
 * Each Actor runs on its own Virtual Thread and processes its own Mailbox.
 * This is the "Share Nothing" architecture that makes us fast.
 */
public class KernxActor {

    private final String id;
    // The "Ferrari" Queue (Multi-Producer, Single-Consumer)
    // No locks. No synchronization. Pure speed.
    private final Queue<KernxPacket> mailbox = new MpscUnboundedArrayQueue<>(1024);
    private volatile boolean running = true;

    public KernxActor(String id) {
        this.id = id;
        start();
    }

    private void start() {
        Thread.ofVirtual().name("actor-" + id).start(() -> {
            System.out.println("[ACTOR] " + id + " is online.");
            while (running) {
                // Non-blocking poll (Drain the inbox)
                KernxPacket packet = mailbox.poll();
                if (packet != null) {
                    process(packet);
                } else {
                    // If empty, yield the CPU (Virtual Threads are cheap!)
                    Thread.yield();
                }
            }
        });
    }

    private void process(KernxPacket packet) {
        // This is where the Agent Logic will eventually live.
        // For now, it just echoes what it hears.
        String msg = new String(packet.payload().array());
        System.out.println("[ACTOR " + id + "] 📨 Received: " + msg);
    }

    public void send(KernxPacket packet) {
        mailbox.offer(packet);
    }

    public void kill() {
        this.running = false;
        System.out.println("[ACTOR] " + id + " shutting down.");
    }
}