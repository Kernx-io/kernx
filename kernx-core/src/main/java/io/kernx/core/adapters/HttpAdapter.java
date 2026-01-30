/*
 * Copyright (c) 2026 Kernx. All rights reserved.
 * Licensed under the Business Source License 1.1.
 */
package io.kernx.core.adapters;

import com.sun.net.httpserver.HttpServer;
import io.kernx.core.KernxDispatcher;
import io.kernx.core.protocol.KernxPacket;
import io.kernx.core.spi.KernxAdapter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * The Web Interface.
 * Allows the outside world (Postman, Frontend) to talk to the Kernel.
 */
public class HttpAdapter implements KernxAdapter {

    private HttpServer server;

    @Override
    public void start(KernxDispatcher dispatcher) {
        try {
            // Listen on Port 8080
            this.server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // Endpoint: POST /api/kernel
            server.createContext("/api/kernel", exchange -> {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // 1. Read the HTTP Body
                    byte[] body = exchange.getRequestBody().readAllBytes();
                    
                    // 2. Convert to Universal Packet
                    var packet = KernxPacket.create("HTTP-User", body);
                    
                    // 3. Send to Kernel
                    dispatcher.dispatch(packet);
                    
                    // 4. Response 202 Accepted (Async)
                    String response = "{\"status\": \"accepted\", \"id\": \"" + packet.id() + "\"}";
                    exchange.sendResponseHeaders(202, response.length());
                    exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
                exchange.close();
            });
            
            // Run the server on a Virtual Thread!
            server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
            server.start();
            
            System.out.println("[INFO] 🌍 HTTP Adapter listening on http://localhost:8080/api/kernel");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (server != null) server.stop(0);
    }
}