/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at LICENSE
 */
package io.kernx.core.spi;

import io.kernx.core.KernxDispatcher;

/**
 * The Universal Plug.
 * Any system that wants to talk to the Kernel MUST implement this.
 * * NOTE: We removed 'sealed' so 3rd party developers (AWS, Plugins) 
 * can build their own Adapters without modifying the Core.
 */
public interface KernxAdapter {

    /**
     * Start listening for traffic.
     * @param dispatcher The pipe to the kernel.
     */
    void start(KernxDispatcher dispatcher);

    /**
     * Stop gracefully.
     */
    void stop();
    
    // Default method to identify the adapter
    default String name() {
        return this.getClass().getSimpleName();
    }
}