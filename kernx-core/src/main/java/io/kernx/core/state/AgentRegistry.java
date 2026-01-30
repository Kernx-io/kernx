/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core.state;

import io.kernx.core.actor.KernxActor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentRegistry {

    // Store the Living Actor, not just data
    private final Map<String, KernxActor> activeActors = new ConcurrentHashMap<>();

    public void spawn(String agentId) {
        if (activeActors.containsKey(agentId)) {
            System.out.println("[REGISTRY] ⚠️ Agent " + agentId + " already exists!");
            return;
        }
        // This launches the Virtual Thread immediately
        KernxActor actor = new KernxActor(agentId);
        activeActors.put(agentId, actor);
    }

    public void kill(String agentId) {
        KernxActor actor = activeActors.remove(agentId);
        if (actor != null) {
            actor.kill();
        }
    }

    public KernxActor get(String agentId) {
        return activeActors.get(agentId);
    }
    
    public String dumpState() {
        return "Active Actors: " + activeActors.keySet().toString();
    }
}