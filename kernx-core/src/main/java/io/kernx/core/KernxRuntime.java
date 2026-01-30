/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at LICENSE
 */
package io.kernx.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.kernx.core.protocol.KernxPacket;

/**
 * The deterministic runtime entry point for the Kernx Platform.
 * This manages the lifecycle of the Virtual Thread Scheduler.
 */
public class KernxRuntime {

    private static final System.Logger logger = System.getLogger("KernxRuntime");

    public static void main(String[] args) {
        System.out.println("""
            ==================================================
              _  __  ______   _____    _   __  __   __
             | |/ / |  ____| |  __ \\  | \\ | | \\ \\ / /
             | ' /  | |__    | |__) | |  \\| |  \\ V / 
             |  <   |  __|   |  _  /  | . ` |   > <  
             | . \\  | |____  | | \\ \\  | |\\  |  / . \\ 
             |_|\\_\\ |______| |_|  \\_\\ |_| \\_| /_/ \\_\\
            
             Kernx Core v1.0.0-SNAPSHOT
             Java Version: %s
             Virtual Threads: ENABLED
            ==================================================
            """.formatted(System.getProperty("java.version")));

        new KernxRuntime().boot();
    }

    public void boot() {
        System.out.println("[INFO] Booting Kernel...");
        
        // 1. Start the Engine
        KernxDispatcher dispatcher = new KernxDispatcher();
        
        // 2. Plug in the Adapter
        // In the future, this will load from a config file (Dependency Injection)
        var cliAdapter = new io.kernx.core.adapters.StdInAdapter();
        cliAdapter.start(dispatcher);
        
        // Keep main alive
        try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException e) {}
    }
}