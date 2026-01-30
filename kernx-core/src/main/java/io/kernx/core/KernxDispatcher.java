/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at LICENSE
 */
package io.kernx.core;

import io.kernx.core.protocol.KernxPacket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The High-Throughput Ingestion Engine.
 * It takes Universal Packets and schedules them on Virtual Threads.
 */
public class KernxDispatcher {

    private final ExecutorService vThreadExecutor;

    public KernxDispatcher() {
        // Creates the "Infinite" Thread Pool
        this.vThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void dispatch(KernxPacket packet) {
        // Fire and Forget (Non-blocking)
        vThreadExecutor.submit(() -> {
            process(packet);
        });
    }

    private void process(KernxPacket packet) {
        // 1. Extract the raw data
        String payload = new String(packet.payload().array());
        
        // 2. The "Brain" Logic
        if (payload.startsWith("DEPLOY")) {
            System.out.println("[KERNEL] 🚀 DEPLOYING AGENT: " + payload.substring(7));
            // In the future: Spin up a Docker container here
        } 
        else if (payload.startsWith("KILL")) {
            System.out.println("[KERNEL] 💀 TERMINATING PROCESS: " + payload.substring(5));
        }
        else {
            // Default "Data Ingest" path
            System.out.println("[DATA] 💾 Ingested " + packet.payload().limit() + " bytes from " + packet.source());
        }
    }
}