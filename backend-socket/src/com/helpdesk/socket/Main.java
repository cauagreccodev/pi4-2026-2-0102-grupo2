package com.helpdesk.socket;

import com.helpdesk.socket.config.Database;
import com.helpdesk.socket.dao.UserDao;
import com.helpdesk.socket.server.ServerState;
import com.helpdesk.socket.server.WebSocketClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry point do Servidor WebSocket (Help Desk PI IV - Render + Neon Cloud).
 * Escuta na porta PORT (injetada pelo Render, ou 5000 por padrao) falando o protocolo WebSocket (RFC 6455).
 */
public class Main {

    public static void main(String[] args) {
        int wsPort = Integer.parseInt(System.getenv().getOrDefault(
                "PORT", System.getenv().getOrDefault("WS_PORT", "5000")));

        System.out.println("==========================================================");
        System.out.println("  Help Desk - WebSocket Server (Render + Neon.tech Cloud) ");
        System.out.println("==========================================================");

        // 1. Valida a conexao TLS inicial com o Neon.tech (sem derrubar o boot caso o Neon esteja acordando do idle)
        try {
            Database.init();
        } catch (Exception e) {
            System.err.println("  [Aviso Neon DB] Nao foi possivel validar no boot imediato ("
                    + e.getMessage() + "). Tentara conectar sob demanda no CT19.");
        }

        // 2. Inicializa estado concorrente e workers (ADR 0001)
        ServerState.getInstance();
        UserDao userDao = new UserDao();
        ExecutorService connectionPool = Executors.newCachedThreadPool();

        // 3. Inicia o Servidor WebSocket na porta publica (CT19, CT18 e CT22)
        try (ServerSocket wsServerSocket = new ServerSocket(wsPort)) {
            System.out.println("  [WebSocket Server] Escutando conexoes ws:// e wss:// na porta " + wsPort);
            System.out.println("==========================================================");

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = wsServerSocket.accept();
                connectionPool.submit(new WebSocketClientHandler(clientSocket, userDao));
            }
        } catch (IOException e) {
            System.err.println("[Main] Erro fatal no ServerSocket WebSocket (" + wsPort + "): " + e.getMessage());
            System.exit(1);
        }
    }
}
