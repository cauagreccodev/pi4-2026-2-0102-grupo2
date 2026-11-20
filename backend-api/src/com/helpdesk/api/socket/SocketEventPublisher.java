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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CT18 (publicarEvento) — avisa o backend-socket quando um chamado é criado ou atualizado.
 * O backend-socket repassa o evento aos usuários conectados como CT22 (chamadoAtualizado).
 *
 * Protocolo (definido no backend-socket): a API envia pela WebSocket persistente
 * {"type":"publicarEvento","data":{secret, action, ticketId, title, status, priority,
 * clientId, supportId?, supportName?, updatedAt}}.
 *
 * Variáveis de ambiente:
 * - SOCKET_URL: ex.: wss://seu-backend-socket.onrender.com/ws (vazia = não envia nada)
 * - INTERNAL_SECRET: o mesmo valor configurado no backend-socket
 *
 * Otimização: mantém uma única conexão WebSocket persistente e reutilizável entre a API
 * e o backend-socket, eliminando o handshake TCP/TLS/WebSocket a cada evento publicado.
 * Falhas no socket nunca quebram a API: o envio é assíncrono e o erro só vai para o log.
 */
public final class SocketEventPublisher {

    private static final String SOCKET_URL = System.getenv("SOCKET_URL");
    private static final String INTERNAL_SECRET = System.getenv().getOrDefault(
            "INTERNAL_SECRET", "segredo-interno-entre-backends");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Fila de mensagens a serem enviadas pela WebSocket persistente
    private static final BlockingQueue<String> MESSAGE_QUEUE = new LinkedBlockingQueue<>();
    // Worker single-thread que consome a fila e envia via WebSocket reutilizável
    private static final ExecutorService SENDER_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "socket-sender");
        t.setDaemon(true);
        return t;
    });

    // Referência ao WebSocket ativo (persistente). Nula quando fechada/erro → reconecta no próximo envio.
    private static final AtomicReference<WebSocket> ACTIVE_WS = new AtomicReference<>(null);
    private static final Object WS_LOCK = new Object();

    private SocketEventPublisher() {}

    static {
        // Inicia o worker de envio assíncrono que mantém a WebSocket aberta e reutilizável
        SENDER_EXECUTOR.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String json = MESSAGE_QUEUE.take();
                    WebSocket ws = getOrCreateWebSocket();
                    if (ws != null && !ws.isOutputClosed()) {
                        ws.sendText(json, true);
                    } else {
                        MESSAGE_QUEUE.offer(json);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("[CT18] Erro no worker de envio WebSocket: " + e.getMessage());
                }
            }
        });
    }

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

        // Enfileira para envio assíncrono via WebSocket persistente (não bloqueia a requisição HTTP)
        MESSAGE_QUEUE.offer(json);
    }

    /**
     * Retorna a WebSocket persistente ativa, criando uma nova se necessário.
     * A conexão é reutilizada entre chamadas, evitando handshake TCP/TLS/WebSocket repetido.
     */
    private static WebSocket getOrCreateWebSocket() {
        WebSocket ws = ACTIVE_WS.get();
        if (ws != null && !ws.isOutputClosed()) {
            return ws;
        }

        synchronized (WS_LOCK) {
            ws = ACTIVE_WS.get();
            if (ws != null && !ws.isOutputClosed()) {
                return ws;
            }

            try {
                CompletableFuture<WebSocket> future = new CompletableFuture<>();
                HTTP.newWebSocketBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .buildAsync(URI.create(SOCKET_URL), new WebSocket.Listener() {
                            @Override
                            public void onOpen(WebSocket webSocket) {
                                System.out.println("[CT18] Conexão WebSocket persistente estabelecida com " + SOCKET_URL);
                                future.complete(webSocket);
                            }

                            @Override
                            public CompletionStage<?> onText(WebSocket w, CharSequence text, boolean last) {
                                w.request(1);
                                return null;
                            }

                            @Override
                            public void onClose(WebSocket w, int statusCode, String reason) {
                                System.out.println("[CT18] WebSocket fechado (status=" + statusCode
                                        + ", reason=" + reason + "). Reconectará no próximo envio.");
                                ACTIVE_WS.compareAndSet(w, null);
                                return null;
                            }

                            @Override
                            public void onError(WebSocket w, Throwable error) {
                                System.err.println("[CT18] Erro no WebSocket persistente: " + error.getMessage());
                                ACTIVE_WS.compareAndSet(w, null);
                                return null;
                            }
                        });

                ws = future.get(10, TimeUnit.SECONDS);
                ACTIVE_WS.set(ws);
                return ws;
            } catch (Exception e) {
                System.err.println("[CT18] Falha ao estabelecer WebSocket persistente: " + e.getMessage());
                return null;
            }
        }
    }
}

