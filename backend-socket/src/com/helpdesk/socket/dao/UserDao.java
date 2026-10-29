package com.helpdesk.socket.dao;

import com.helpdesk.socket.config.Database;
import com.helpdesk.socket.model.UserSessionInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO para consulta da tabela 'users' no Neon PostgreSQL (CT13)
 * durante a autenticação WebSocket (CT19).
 */
public class UserDao { //Data Access Object (DAO) para a tabela 'users' no banco de dados Neon PostgreSQL.

    public UserSessionInfo findSessionInfoById(int userId) {
        String sql = "SELECT id, name, email, role, is_active FROM users WHERE id = ?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new UserSessionInfo(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("role"),
                                rs.getBoolean("is_active")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDao - Neon] Erro ao consultar usuario id=" + userId + ": " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return null;
    }
}
