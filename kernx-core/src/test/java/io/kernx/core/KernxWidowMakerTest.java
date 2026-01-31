package io.kernx.core;

import io.kernx.core.protocol.KernxPacket;
import io.kernx.core.state.ResultStore;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class KernxWidowMakerTest {

    // --- 💀 THE WIDOWMAKER CONFIG ---
    private static final int WIDOW_REQUESTS = 100_000; 
    private static final int WIDOW_AGENTS = 1_000;

    /**
     * 1. THE WIDOWMAKER
     * This is your benchmark. It tries to melt the CPU.
     */
    @Test
    public void runWidowmaker() throws InterruptedException {
        System.out.println("🔥 INITIALIZING WIDOWMAKER STRESS TEST...");
        System.out.println("🎯 Target: " + WIDOW_REQUESTS + " requests across " + WIDOW_AGENTS + " agents.");
        
        KernxDispatcher dispatcher = new KernxDispatcher();
        
        // 1. Deploy the Army
        long setupStart = System.currentTimeMillis();
        for (int i = 0; i < WIDOW_AGENTS; i++) {
            dispatcher.dispatch(createPacket("DEPLOY Agent-" + i));
        }
        System.out.println("✅ DEPLOYMENT COMPLETE: " + (System.currentTimeMillis() - setupStart) + "ms");

        // 2. Open the Floodgates (Virtual Threads)
        ExecutorService attacker = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(WIDOW_REQUESTS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        
        System.out.println("🚀 LAUNCHING ATTACK...");
        long start = System.currentTimeMillis();

        for (int i = 0; i < WIDOW_REQUESTS; i++) {
            final int agentId = i % WIDOW_AGENTS;
            attacker.submit(() -> {
                try {
                    dispatcher.dispatch(createPacket("MSG Agent-" + agentId + " Payload-" + UUID.randomUUID()));
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    rejectedCount.incrementAndGet(); // Backpressure
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long duration = System.currentTimeMillis() - start;

        System.out.println("\n===========================================");
        System.out.println("💀 WIDOWMAKER RESULTS");
        System.out.println("⏱️  Time: " + duration + " ms");
        System.out.println("📨 Throughput: " + (WIDOW_REQUESTS / (duration / 1000.0)) + " req/sec");
        System.out.println("✅ Accepted: " + successCount.get());
        System.out.println("⛔ Rejected: " + rejectedCount.get());
        System.out.println("===========================================\n");
        
        if (successCount.get() == 0 && rejectedCount.get() == 0) {
            fail("SYSTEM FAILED: Zero requests processed.");
        }
    }
    
    // --- HELPER ---
    private KernxPacket createPacket(String text) {
        return new KernxPacket(
                UUID.randomUUID().toString(), "Test", Instant.now(),
                ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8)),
                Collections.emptyMap());
    }
}