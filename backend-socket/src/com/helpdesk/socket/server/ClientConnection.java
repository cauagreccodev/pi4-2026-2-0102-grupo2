package com.helpdesk.socket.server;

import com.helpdesk.socket.util.WebSocketUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representa uma sessão WebSocket autenticada (equivalente à classe Parceiro).
 * Implementa a decisão da ADR 0001: possui uma LinkedBlockingQueue limitada (outbox)
 * exclusiva para escrita assíncrona de frames WebSocket e envio de Ping keep-alive (20s).
 */
public class ClientConnection {

    private static final int OUTBOX_CAPACITY = 100;
    private static final int PING_INTERVAL_SECONDS = 20;

    private final Socket wsSocket;
    private final OutputStream out;
    private final int userId;
    private final String role;
    private final String department;
    private final LinkedBlockingQueue<String> outbox;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Thread writerThread;

    public ClientConnection(Socket wsSocket, OutputStream out, int userId, String role) {
        this(wsSocket, out, userId, role, null);
    }

    public ClientConnection(Socket wsSocket, OutputStream out, int userId, String role, String department) {
        this.wsSocket = wsSocket;
        this.out = out;
        this.userId = userId;
        this.role = role != null ? role.toUpperCase() : "CLIENT";
        this.department = department != null ? department.trim() : "";
        this.outbox = new LinkedBlockingQueue<>(OUTBOX_CAPACITY);
        this.writerThread = new Thread(this::writeLoop, "ws-writer-user-" + userId);
        this.writerThread.setDaemon(true);
    }

    public void startWriter() {
        this.writerThread.start();
    }

    public int getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isClosed() {
        return closed.get();
    }

    public boolean isSupportOrAdmin() {
        return "SUPPORT".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)
                || "TECNICO".equalsIgnoreCase(role) || isTiDepartment();
    }

    public boolean isTiDepartment() {
        if (department == null) return false;
        String dep = department.trim().toUpperCase();
        return "TI".equals(dep) || "TECNOLOGIA".equals(dep) || dep.startsWith("TI ");
    }

    /**
     * Enfileira uma mensagem JSON no outbox desta sessão WebSocket (O(1), não bloqueante).
     * Se a fila estiver cheia (cliente lento), fecha a conexão WebSocket conforme ADR 0001.
     */
    public void enqueueMessage(String jsonMessage) {
        if (closed.get()) return;
        if (!outbox.offer(jsonMessage)) {
            System.err.println("[WS] Outbox cheio para userId=" + userId + ". Desconectando cliente lento.");
            close();
        }
    }

    private void writeLoop() {
        try {
            while (!closed.get() && !Thread.currentThread().isInterrupted()) {
                String message = outbox.poll(PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
                if (message != null) {
                    WebSocketUtil.writeTextFrame(out, message);
                } else if (!closed.get()) {
                    // Sem mensagens no intervalo de 20s: dispara Ping frame (opcode 0x9) para manter vivo no Render
                    WebSocketUtil.writePingFrame(out);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Conexão WebSocket caiu durante a escrita
        } finally {
            close();
        }
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            outbox.clear();
            writerThread.interrupt();
            try {
                WebSocketUtil.writeCloseFrame(out);
            } catch (Exception ignored) {
            }
            try {
                wsSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
