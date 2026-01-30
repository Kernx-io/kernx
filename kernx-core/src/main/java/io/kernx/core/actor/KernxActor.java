/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core.actor;

import io.kernx.core.protocol.KernxPacket;
import org.jctools.queues.MpscUnboundedArrayQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class KernxActor {

    private final String id;
    private final Queue<KernxPacket> mailbox = new MpscUnboundedArrayQueue<>(1024);
    private final io.kernx.core.ai.AiProvider brain = new io.kernx.core.ai.AiProvider();
    
    // The "Context Window" (Short-Term Memory)
    // In a real startup, this would be a Vector Database (Pinecone).
    private final List<String> memory = new ArrayList<>();
    
    private volatile boolean running = true;

    public KernxActor(String id) {
        this.id = id;
        start();
    }

    private void start() {
        Thread.ofVirtual().name("actor-" + id).start(() -> {
            System.out.println("[ACTOR] " + id + " is online.");
            while (running) {
                KernxPacket packet = mailbox.poll();
                if (packet != null) {
                    process(packet);
                } else {
                    Thread.yield();
                }
            }
        });
    }

    private void process(KernxPacket packet) {
        String msg = new String(packet.payload().array());
        
        // 1. Add to Memory
        memory.add("User: " + msg);
        System.out.println("[ACTOR " + id + "] 📨 Received: " + msg);
        
        // 2. Build the Full Prompt (Context + New Task)
        String fullContext = "History: " + memory.toString() + "\nNew Task: " + msg;

        // 3. Ask AI (Async)
        brain.prompt(fullContext).thenAccept(response -> {
             // 4. Remember the AI's own answer too!
             memory.add("AI: " + response);
             System.out.println("[ACTOR " + id + "] 🧠 AI Thought: " + response);
        });
    }

    public void send(KernxPacket packet) {
        mailbox.offer(packet);
    }

    public void kill() {
        this.running = false;
        System.out.println("[ACTOR] " + id + " shutting down.");
    }
}