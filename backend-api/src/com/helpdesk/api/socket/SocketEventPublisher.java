package com.helpdesk.api.socket;

import com.helpdesk.api.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CT18 — avisa o backend-socket sempre que um chamado muda
 * (criado, status alterado, atribuído ou excluído).
 *
 * A URL do socket vem da variável de ambiente SOCKET_URL
 * (ex.: wss://seu-backend-socket.onrender.com/ws).
 * Se ela estiver vazia ou o socket estiver fora do ar, a API continua
 * funcionando normalmente: o erro só aparece no log.
 */
public final class SocketEventPublisher {

    private static final String SOCKET_URL = System.getenv("SOCKET_URL");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Envia em segundo plano para não atrasar a resposta da API
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "socket-publisher");
        t.setDaemon(true);
        return t;
    });

    private static volatile WebSocket webSocket;

    private SocketEventPublisher() {}

    /**
     * Publica o CT18.
     *
     * @param eventType CREATION, STATUS_CHANGE ou DELETION
     * @param ticketId  id do chamado
     * @param newStatus status atual do chamado (pode ser null na exclusão)
     * @param message   texto exibido para o usuário
     */
    public static void publishTicketChanged(String eventType, int ticketId, String newStatus, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contract", "CT18");
        payload.put("type", eventType);
        payload.put("ticketId", ticketId);
        payload.put("newStatus", newStatus);
        payload.put("message", message);
        payload.put("timestamp", Instant.now().toString());
        send(payload);
    }

    private static void send(Map<String, Object> payload) {
        if (SOCKET_URL == null || SOCKET_URL.isBlank()) {
            return; // socket ainda não configurado
        }
        String json = JsonUtil.toJson(payload);
        EXECUTOR.submit(() -> {
            try {
                connection().sendText(json, true).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("[SocketEventPublisher] Falha ao enviar CT18: " + e.getMessage());
                webSocket = null; // tenta reconectar no próximo evento
            }
        });
    }

    private static WebSocket connection() throws Exception {
        WebSocket ws = webSocket;
        if (ws != null && !ws.isOutputClosed()) {
            return ws;
        }
        ws = HTTP.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create(SOCKET_URL), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onClose(WebSocket w, int statusCode, String reason) {
                        webSocket = null;
                        return null;
                    }

                    @Override
                    public void onError(WebSocket w, Throwable error) {
                        System.err.println("[SocketEventPublisher] Conexão caiu: " + error.getMessage());
                        webSocket = null;
                    }
                })
                .get(10, TimeUnit.SECONDS);
        webSocket = ws;
        return ws;
    }
}
