package com.helpdesk.api;

import com.helpdesk.api.config.Database;
import com.helpdesk.api.handler.CategoryHandler;
import com.helpdesk.api.handler.HealthHandler;
import com.helpdesk.api.handler.LoginHandler;
import com.helpdesk.api.handler.NotificationHandler;
import com.helpdesk.api.handler.TicketHandler;
import com.helpdesk.api.handler.UserHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        try {
            // Inicializa a conexão com o banco
            Database.init();

            // Configura servidor na porta dinâmica (PORT env do Render) ou 8000
            int port = 8000;
            String envPort = System.getenv("PORT");
            if (envPort != null && !envPort.isEmpty()) {
                try {
                    port = Integer.parseInt(envPort);
                } catch (NumberFormatException ignored) {}
            }
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Rota pública de Health Check / Status (para Cron Jobs e evitar cold start do Render)
            HealthHandler healthHandler = new HealthHandler();
            server.createContext("/health", healthHandler);
            server.createContext("/api/health", healthHandler);
            server.createContext("/", healthHandler);

            // Rotas da API (suporta com e sem /api)
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/api/register", new UserHandler());
            server.createContext("/register", new UserHandler());
            server.createContext("/api/users", new UserHandler());
            server.createContext("/users", new UserHandler());
            server.createContext("/api/categorias", new CategoryHandler());
            server.createContext("/categorias", new CategoryHandler());
            server.createContext("/api/chamados", new TicketHandler());
            server.createContext("/chamados", new TicketHandler());
            server.createContext("/api/notificacoes", new NotificationHandler());
            server.createContext("/notificacoes", new NotificationHandler());

            // Executa com um thread pool para lidar com múltiplas requisições simultâneas
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            System.out.println("API Principal iniciada na porta " + port);

        } catch (Exception e) {
            System.err.println("Falha ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
