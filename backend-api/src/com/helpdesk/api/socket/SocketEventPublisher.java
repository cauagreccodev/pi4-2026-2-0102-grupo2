package com.helpdesk.api.socket;

import com.helpdesk.api.model.Ticket;
import com.helpdesk.api.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CT18 (publicarEvento) — avisa o backend-socket quando um chamado é criado ou atualizado.
 * O backend-socket repassa o evento aos usuários conectados como CT22 (chamadoAtualizado).
 *
 * Protocolo (definido no backend-socket): a API abre uma conexão WebSocket, envia
 * {"type":"publicarEvento","data":{secret, action, ticketId, title, status, priority,
 * clientId, supportId?, supportName?, updatedAt}}, recebe {"received": true|false} e a conexão é fechada.
 *
 * Variáveis de ambiente:
 * - SOCKET_URL: ex.: wss://seu-backend-socket.onrender.com/ws (vazia = não envia nada)
 * - INTERNAL_SECRET: o mesmo valor configurado no backend-socket
 *
 * Falhas no socket nunca quebram a API: o envio é assíncrono e o erro só vai para o log.
 */
public final class SocketEventPublisher {

    private static final String SOCKET_URL = System.getenv("SOCKET_URL");
    private static final String INTERNAL_SECRET = System.getenv().getOrDefault(
            "INTERNAL_SECRET", "segredo-interno-entre-backends");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "socket-publisher");
        t.setDaemon(true);
        return t;
    });

    private SocketEventPublisher() {}

    /** Chamado criado (action = CREATED). */
    public static void ticketCreated(Ticket ticket) {
        publish("CREATED", ticket);
    }

    /** Chamado atualizado: status, responsável ou detalhes (action = UPDATED). */
    public static void ticketUpdated(Ticket ticket) {
        publish("UPDATED", ticket);
    }

    private static void publish(String action, Ticket ticket) {
        if (SOCKET_URL == null || SOCKET_URL.isBlank() || ticket == null) {
            return;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("secret", INTERNAL_SECRET);
        data.put("action", action);
        data.put("ticketId", ticket.getId());
        data.put("title", ticket.getTitle());
        data.put("status", ticket.getStatus());
        data.put("priority", ticket.getPriority());
        data.put("clientId", ticket.getClientId());
        if (ticket.getSupportId() != null) {
            data.put("supportId", ticket.getSupportId());
        }
        if (ticket.getSupportName() != null && !ticket.getSupportName().isBlank()) {
            data.put("supportName", ticket.getSupportName());
        }
        data.put("updatedAt", ticket.getUpdatedAt() != null ? ticket.getUpdatedAt() : Instant.now().toString());

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("type", "publicarEvento");
        envelope.put("data", data);
        String json = JsonUtil.toJson(envelope);

        EXECUTOR.submit(() -> send(json, ticket.getId()));
    }

    private static void send(String json, int ticketId) {
        CompletableFuture<String> reply = new CompletableFuture<>();
        WebSocket ws = null;
        try {
            ws = HTTP.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(SOCKET_URL), new WebSocket.Listener() {
                        private final StringBuilder buffer = new StringBuilder();

                        @Override
                        public CompletionStage<?> onText(WebSocket w, CharSequence text, boolean last) {
                            buffer.append(text);
                            if (last) {
                                reply.complete(buffer.toString());
                            }
                            w.request(1);
                            return null;
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket w, int statusCode, String reason) {
                            reply.complete(null);
                            return null;
                        }

                        @Override
                        public void onError(WebSocket w, Throwable error) {
                            reply.completeExceptionally(error);
                        }
                    })
                    .get(10, TimeUnit.SECONDS);

            ws.sendText(json, true).get(5, TimeUnit.SECONDS);
            String answer = reply.get(10, TimeUnit.SECONDS);
            if (answer == null || !answer.contains("\"received\":true")) {
                System.err.println("[CT18] Socket não confirmou o evento do chamado #" + ticketId
                        + " (resposta: " + answer + "). Confira INTERNAL_SECRET e os campos do chamado.");
            }
        } catch (Exception e) {
            System.err.println("[CT18] Falha ao publicar evento do chamado #" + ticketId + ": " + e.getMessage());
        } finally {
            if (ws != null && !ws.isOutputClosed()) {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "ok");
            }
        }
    }
}
