/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core.adapters;

import io.kernx.core.KernxDispatcher;
import io.kernx.core.protocol.KernxPacket;
import io.kernx.core.spi.KernxAdapter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * A Developer-Friendly Adapter that reads from the Console.
 * This proves we can inject external data into the Kernel.
 */
public final class StdInAdapter implements KernxAdapter {

    private volatile boolean running = true;

    @Override
    public void start(KernxDispatcher dispatcher) {
        // We run the "Listener" on a Virtual Thread too!
        Thread.ofVirtual().name("stdin-listener").start(() -> {
            System.out.println("[INFO] StdIn Adapter Connected. Type a message (or 'exit'):");
            
            try (Scanner scanner = new Scanner(System.in)) {
                while (running && scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(line)) {
                        System.exit(0);
                    }
                    
                    // 1. Convert "Keyboard Event" -> "Universal Packet"
                    var packet = KernxPacket.create("User-Console", line.getBytes(StandardCharsets.UTF_8));
                    
                    // 2. Inject into Kernel
                    dispatcher.dispatch(packet);
                }
            }
        });
    }

    @Override
    public void stop() {
        this.running = false;
    }
}