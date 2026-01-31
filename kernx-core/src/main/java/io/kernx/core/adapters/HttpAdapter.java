/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 */
package io.kernx.core.adapters;

import io.kernx.core.KernxDispatcher;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class HttpAdapter {
    private static final AtomicLong requestCounter = new AtomicLong(0);
    private static final long startTime = System.currentTimeMillis();

    public void start(KernxDispatcher dispatcher) {
        try {
            // FIX: Force bind to IPv4 Loopback (127.0.0.1) to fix macOS issues
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
            
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // Endpoint 1: Ingestion
            server.createContext("/api/kernel", exchange -> {
                requestCounter.incrementAndGet();
                send(exchange, 202, "{\"status\": \"ACCEPTED\"}");
            });
            
            // Endpoint 2: Stats
            server.createContext("/stats", exchange -> {
                long uptime = (System.currentTimeMillis() - startTime) / 1000;
                if (uptime == 0) uptime = 1;
                long rps = requestCounter.get() / uptime;
                
                String json = """
                    {
                        "throughput": %d,
                        "uptime": %d,
                        "active_agents": 1
                    }
                    """.formatted(rps, uptime);
                
                send(exchange, 200, json);
            });

            server.start();
            System.out.println("[INFO] 🌍 HTTP Adapter listening on http://127.0.0.1:8080/api/kernel");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}