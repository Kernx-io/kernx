/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Post Office" for Async Results.
 * Agents drop answers here. Users pick them up via HTTP GET.
 */
public class ResultStore {

    // Singleton Instance (Simple version)
    public static final ResultStore INSTANCE = new ResultStore();

    private final Map<String, String> results = new ConcurrentHashMap<>();

    public void put(String requestId, String answer) {
        results.put(requestId, answer);
    }

    public String get(String requestId) {
        return results.getOrDefault(requestId, "PENDING");
    }
}