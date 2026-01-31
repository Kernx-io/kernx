/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 */
package io.kernx.core.state;

import io.kernx.core.actor.KernxActor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentRegistry {

    private final AtomicInteger globalQueueDepth = new AtomicInteger(100);
    private final Map<String, KernxActor> agents = new ConcurrentHashMap<>();

    public void setGlobalQueueDepth(int depth) {
        this.globalQueueDepth.set(depth);
        System.out.println("[REGISTRY] ⚙️ System Policy Updated: Queue Depth = " + depth);
    }

    public void register(String agentId) {
        int currentPolicy = globalQueueDepth.get();
        agents.computeIfAbsent(agentId, id -> new KernxActor(id, currentPolicy));
    }

    public KernxActor get(String agentId) {
        return agents.get(agentId);
    }

    public void remove(String agentId) {
        KernxActor actor = agents.remove(agentId);
        if (actor != null) {
            actor.kill();
        }
    }

    // --- NEW METHOD (Fixes your Red Line) ---
    public int count() {
        return agents.size();
    }
}