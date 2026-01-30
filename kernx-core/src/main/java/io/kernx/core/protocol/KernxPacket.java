/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at LICENSE
 */
package io.kernx.core.protocol;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * The Universal Data Envelope.
 * Every request is converted into this immutable packet before entering the Kernel.
 */
public record KernxPacket(
    String id,
    String source,
    Instant timestamp,
    ByteBuffer payload,
    Map<String, String> meta
) {
    // Compact Constructor for "Hot Path" creation
    public static KernxPacket create(String source, byte[] data) {
        return new KernxPacket(
            UUID.randomUUID().toString(),
            source,
            Instant.now(),
            ByteBuffer.wrap(data), // Zero-Copy wrap
            Map.of()
        );
    }
}