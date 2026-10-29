package com.helpdesk.socket.server;

import com.helpdesk.socket.model.TicketEventPayload;
import com.helpdesk.socket.util.JsonUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Estado centralizado do servidor WebSocket (ADR 0001).
 * - ConcurrentHashMap<Integer, Set<ClientConnection>>: mapeia userId -> sessões WebSocket ativas (múltiplas abas).
 * - LinkedBlockingQueue<Runnable>: fila assíncrona consumida por workers dedicados para processar
 *   eventos e desacoplar o loop de rede.
 */
public class ServerState {

    private static final int EVENT_QUEUE_CAPACITY = 1000;
    private static final int WORKER_THREADS = 2;

    private static final ServerState INSTANCE = new ServerState();

    // userId -> conjunto concorrente de conexões WebSocket ativas (abas abertas)
    private final ConcurrentHashMap<Integer, Set<ClientConnection>> sessionsByUser = new ConcurrentHashMap<>();

    // Fila assíncrona de eventos (ADR 0001)
    private final LinkedBlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<>(EVENT_QUEUE_CAPACITY);

    private ServerState() {
        for (int i = 1; i <= WORKER_THREADS; i++) {
            Thread worker = new Thread(this::eventWorkerLoop, "ws-event-worker-" + i);
            worker.setDaemon(true);
            worker.start();
        }
    }

    public static ServerState getInstance() {
        return INSTANCE;
    }

    public void registerSession(ClientConnection connection) {
        sessionsByUser
                .computeIfAbsent(connection.getUserId(), k -> ConcurrentHashMap.newKeySet())
                .add(connection);
        System.out.println("[WS State] Sessao registrada: userId=" + connection.getUserId()
                + " (" + connection.getRole() + ") | Total usuarios online: " + sessionsByUser.size());
    }

    public void removeSession(ClientConnection connection) {
        if (connection == null) return;
        int userId = connection.getUserId();
        Set<ClientConnection> userSet = sessionsByUser.get(userId);
        if (userSet != null) {
            userSet.remove(connection);
            if (userSet.isEmpty()) {
                sessionsByUser.remove(userId, userSet);
            }
        }
        connection.close();
        System.out.println("[WS State] Sessao removida: userId=" + userId
                + " | Total usuarios online: " + sessionsByUser.size());
    }

    /**
     * Enfileira um evento CT18 validado para ser distribuído assincronamente via WebSocket como CT22 (chamadoAtualizado).
     */
    public boolean publishTicketEventAsync(TicketEventPayload payload) {
        return eventQueue.offer(() -> broadcastChamadoAtualizado(payload));
    }

    /**
     * Envia CT22 (chamadoAtualizado) via WebSocket para:
     * - Solicitante (clientId)
     * - Técnico responsável (supportId, se houver)
     * - Todos os usuários SUPPORT e ADMIN conectados
     */
    private void broadcastChamadoAtualizado(TicketEventPayload payload) {
        String messageJson = JsonUtil.buildEnvelope("chamadoAtualizado", payload.toCt22DataMap());

        Set<Integer> targetUserIds = new HashSet<>();
        if (payload.getClientId() != null) {
            targetUserIds.add(payload.getClientId());
        }
        if (payload.getSupportId() != null) {
            targetUserIds.add(payload.getSupportId());
        }

        // Adiciona todos os usuários com perfil SUPPORT ou ADMIN atualmente conectados no WebSocket
        for (Map.Entry<Integer, Set<ClientConnection>> entry : sessionsByUser.entrySet()) {
            for (ClientConnection conn : entry.getValue()) {
                if (!conn.isClosed() && conn.isSupportOrAdmin()) {
                    targetUserIds.add(entry.getKey());
                    break;
                }
            }
        }

        int deliveredConnections = 0;
        for (Integer userId : targetUserIds) {
            Set<ClientConnection> connections = sessionsByUser.get(userId);
            if (connections != null) {
                for (ClientConnection conn : connections) {
                    if (!conn.isClosed()) {
                        conn.enqueueMessage(messageJson);
                        deliveredConnections++;
                    }
                }
            }
        }

        System.out.println("[CT22 - WebSocket] chamadoAtualizado (ticketId=" + payload.getTicketId()
                + ", action=" + payload.getAction() + ", status=" + payload.getStatus()
                + ") enviado para " + deliveredConnections + " sessoes WebSocket.");
    }

    /**
     * Suporte à ADR 0002 (NOTIFICATION_PING direcionado a um userId específico via WebSocket).
     */
    public void sendNotificationPing(int targetUserId, Map<String, Object> notificationData) {
        eventQueue.offer(() -> {
            String pingJson = JsonUtil.toJson(Map.of(
                    "event", "NOTIFICATION_PING",
                    "type", "NOTIFICATION_PING",
                    "data", notificationData
            ));
            Set<ClientConnection> connections = sessionsByUser.get(targetUserId);
            if (connections != null) {
                for (ClientConnection conn : connections) {
                    if (!conn.isClosed()) {
                        conn.enqueueMessage(pingJson);
                    }
                }
            }
        });
    }

    private void eventWorkerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Runnable task = eventQueue.take();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[Worker] Erro ao processar evento na fila: " + e.getMessage());
            }
        }
    }
}
