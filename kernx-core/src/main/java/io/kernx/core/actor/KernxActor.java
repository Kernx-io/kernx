/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core.actor;

import io.kernx.core.protocol.KernxPacket;
import io.kernx.core.state.ResultStore;
import org.jctools.queues.MpscArrayQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.LockSupport; // IMPORT THIS

public class KernxActor {

    private final String id;
    private final Queue<KernxPacket> mailbox; 
    private final io.kernx.core.ai.AiProvider brain = new io.kernx.core.ai.AiProvider();
    
    private final List<String> memory = new ArrayList<>();
    private static final int MEMORY_LIMIT = 50; 
    
    private volatile boolean running = true;

    public KernxActor(String id, int queueDepth) {
        this.id = id;
        this.mailbox = new MpscArrayQueue<>(queueDepth); 
        start();
    }

    private void start() {
        Thread.ofVirtual().name("actor-" + id).start(() -> {
            while (running) {
                KernxPacket packet = mailbox.poll();
                if (packet != null) {
                    process(packet);
                } else {
                    // FIX: Micro-Sleep (10 microseconds)
                    // Gives the Dispatcher time to fill the queue, but wakes up fast.
                    LockSupport.parkNanos(10_000); 
                }
            }
        });
    }

    public boolean offer(KernxPacket packet) {
        return mailbox.offer(packet);
    }

    private void process(KernxPacket packet) {
        String msg = new String(packet.payload().array());
        
        if (memory.size() > MEMORY_LIMIT) {
            memory.remove(0);
        }
        memory.add("User: " + msg);
        
        // No logs. Pure speed.
        String response = "Processed-" + System.nanoTime(); 
        ResultStore.INSTANCE.put(packet.id(), response);
    }

    public void kill() {
        this.running = false;
    }
}