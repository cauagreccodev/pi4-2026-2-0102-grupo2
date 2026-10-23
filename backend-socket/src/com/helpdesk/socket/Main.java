package com.helpdesk.socket;

import com.helpdesk.socket.util.WebSocketUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry point do Servidor WebSocket (Help Desk PI IV — Render + Neon Cloud).
 * Etapa 1: Aceitadora de conexões testando as rotas HTTP (/health, /) e o Handshake WebSocket (RFC 6455).
 */
public class Main {

    public static void main(String[] args) {
        int wsPort = Integer.parseInt(System.getenv().getOrDefault(
                "PORT", System.getenv().getOrDefault("WS_PORT", "5000")));

        System.out.println("==========================================================");
        System.out.println("  Help Desk — WebSocket Server (Etapa 1: HTTP + WS Test)  ");
        System.out.println("==========================================================");

        ExecutorService connectionPool = Executors.newCachedThreadPool();

        try (ServerSocket wsServerSocket = new ServerSocket(wsPort)) {
            System.out.println("  [Servidor Ativo] Escutando na porta " + wsPort);
            System.out.println("  -> Teste HTTP:      http://localhost:" + wsPort + "/health");
            System.out.println("  -> Teste WebSocket: ws://localhost:" + wsPort + "/ws");
            System.out.println("==========================================================");

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = wsServerSocket.accept();
                connectionPool.submit(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[Main] Erro fatal no ServerSocket (" + wsPort + "): " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleConnection(Socket socket) {
        try (socket) {
            socket.setSoTimeout(15_000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Faz o Handshake WebSocket (ou responde 200 OK se for GET /health ou GET /)
            boolean isWebSocket = WebSocketUtil.performHandshake(in, out);
            if (!isWebSocket) {
                System.out.println("[HTTP] Health Check / Keep-Alive respondido com 200 OK.");
                return;
            }

            System.out.println("[WebSocket] Handshake 101 concluído! Cliente conectado: " + socket.getRemoteSocketAddress());
            socket.setSoTimeout(0);

            // Lê frames WebSocket enviados pelo navegador para testarmos a comunicação na Etapa 1
            String frame;
            while ((frame = WebSocketUtil.readTextFrame(in, out)) != null) {
                System.out.println("[WebSocket] Mensagem recebida: " + frame);
                WebSocketUtil.writeTextFrame(out, "{\"type\":\"echoTesteEtapa1\",\"recebido\":" + frame + "}");
            }
            System.out.println("[WebSocket] Cliente desconectou.");
        } catch (IOException e) {
            System.err.println("[WebSocket] Conexão encerrada: " + e.getMessage());
        }
    }
}
