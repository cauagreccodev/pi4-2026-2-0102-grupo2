package com.helpdesk.api.handler;

import com.helpdesk.api.util.HttpHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HealthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendOptions(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()) && !"HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendError(exchange, 405, "Method not allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        // Rota pública para /health, /api/health ou raiz /
        if ("/health".equals(path) || "/api/health".equals(path) || "/".equals(path)) {
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("status", "UP");
            status.put("service", "Help Desk API");
            status.put("version", "v0.4.3");
            status.put("frontend", "https://help-desk-pi-iv.vercel.app");
            status.put("timestamp", System.currentTimeMillis());

            HttpHelper.sendJson(exchange, 200, status);
        } else {
            HttpHelper.sendError(exchange, 404, "Rota não encontrada");
        }
    }
}
