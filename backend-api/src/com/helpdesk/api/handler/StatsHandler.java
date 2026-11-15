package com.helpdesk.api.handler;

import com.helpdesk.api.config.Database;
import com.helpdesk.api.util.AuthUtil;
import com.helpdesk.api.util.HttpHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de estatísticas gerais do help desk.
 * Retorna contagens de chamados (total, por status e por prioridade),
 * total de usuários e notificações não lidas do usuário autenticado.
 */
public class StatsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendOptions(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendError(exchange, 405, "Method not allowed");
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpHelper.sendError(exchange, 401, "Não autenticado");
            return;
        }
        Integer userId = AuthUtil.validateToken(authHeader.substring(7));
        if (userId == null) {
            HttpHelper.sendError(exchange, 401, "Token inválido ou expirado");
            return;
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = Database.getConnection();

            stats.put("totalTickets", count(conn, "SELECT count(*) FROM tickets"));

            Map<String, Integer> byStatus = new LinkedHashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT status, count(*) FROM tickets GROUP BY status");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byStatus.put(rs.getString("status"), rs.getInt(2));
                }
            }
            stats.put("ticketsByStatus", byStatus);

            Map<String, Integer> byPriority = new LinkedHashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT priority, count(*) FROM tickets GROUP BY priority");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byPriority.put(rs.getString("priority"), rs.getInt(2));
                }
            }
            stats.put("ticketsByPriority", byPriority);

            stats.put("totalUsers", count(conn, "SELECT count(*) FROM users"));
            stats.put("unreadNotifications", count(conn,
                    "SELECT count(*) FROM notifications WHERE user_id = ? AND is_read = false", userId));

            HttpHelper.sendJson(exchange, 200, stats);
        } catch (SQLException e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Erro ao calcular estatísticas");
        } finally {
            if (conn != null) {
                Database.releaseConnection(conn);
            }
        }
    }

    private int count(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object p = params[i];
                if (p instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) p);
                } else if (p instanceof String) {
                    stmt.setString(i + 1, (String) p);
                } else {
                    stmt.setObject(i + 1, p);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
