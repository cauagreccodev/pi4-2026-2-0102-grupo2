package com.helpdesk.api.dao;

import com.helpdesk.api.config.Database;
import com.helpdesk.api.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketDao {

    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setStatus(rs.getString("status"));
        ticket.setPriority(rs.getString("priority"));
        ticket.setCategoryId(rs.getInt("category_id"));
        ticket.setClientId(rs.getInt("client_id"));
        
        int supportId = rs.getInt("support_id");
        if (!rs.wasNull()) {
            ticket.setSupportId(supportId);
        }
        
        ticket.setCreatedAt(rs.getTimestamp("created_at").toString());
        if (rs.getTimestamp("updated_at") != null) {
            ticket.setUpdatedAt(rs.getTimestamp("updated_at").toString());
        }
        if (rs.getTimestamp("closed_at") != null) {
            ticket.setClosedAt(rs.getTimestamp("closed_at").toString());
        }

        // Extra fields from JOINs (may not be present in all queries, handle gracefully)
        try {
            ticket.setClientName(rs.getString("client_name"));
            ticket.setCategoryName(rs.getString("category_name"));
            String supportName = rs.getString("support_name");
            if (supportName != null) ticket.setSupportName(supportName);
        } catch (SQLException e) {
            // These columns might not exist in the result set if no JOIN was used
        }

        return ticket;
    }

    public Ticket insert(Ticket ticket) {
        String sql = "INSERT INTO tickets (title, description, priority, category_id, client_id, support_id, status) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING *";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, ticket.getTitle());
                stmt.setString(2, ticket.getDescription());
                stmt.setString(3, ticket.getPriority());
                stmt.setInt(4, ticket.getCategoryId());
                stmt.setInt(5, ticket.getClientId());
                if (ticket.getSupportId() == null) stmt.setNull(6, java.sql.Types.INTEGER);
                else stmt.setInt(6, ticket.getSupportId());
                stmt.setString(7, ticket.getSupportId() == null ? "NEW" : "ASSIGNED");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTicket(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir chamado: " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return null;
    }

    public List<Ticket> findAll(String status, Integer clientId, Integer supportId) {
        List<Ticket> tickets = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, c.name as client_name, s.name as support_name, cat.name as category_name " +
            "FROM tickets t " +
            "JOIN users c ON t.client_id = c.id " +
            "LEFT JOIN users s ON t.support_id = s.id " +
            "LEFT JOIN categories cat ON t.category_id = cat.id " +
            "WHERE 1=1 "
        );

        if (status != null && !status.isEmpty()) {
            sql.append("AND t.status = ? ");
        }
        if (clientId != null) {
            sql.append("AND t.client_id = ? ");
        }
        if (supportId != null) {
            sql.append("AND t.support_id = ? ");
        }
        sql.append("ORDER BY t.created_at DESC");

        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int index = 1;
                if (status != null && !status.isEmpty()) {
                    stmt.setString(index++, status);
                }
                if (clientId != null) {
                    stmt.setInt(index++, clientId);
                }
                if (supportId != null) {
                    stmt.setInt(index++, supportId);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tickets.add(mapResultSetToTicket(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar chamados: " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return tickets;
    }

    public Ticket findById(int id) {
        String sql = "SELECT t.*, c.name as client_name, s.name as support_name, cat.name as category_name " +
                     "FROM tickets t " +
                     "JOIN users c ON t.client_id = c.id " +
                     "LEFT JOIN users s ON t.support_id = s.id " +
                     "LEFT JOIN categories cat ON t.category_id = cat.id " +
                     "WHERE t.id = ?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTicket(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar chamado por id: " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return null;
    }

    public boolean deleteById(int id) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            // Remove timeline entries first (FK dependency)
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM ticket_timeline WHERE ticket_id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Remove notifications linked to this ticket
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM notifications WHERE ticket_id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Remove the ticket
            boolean deleted;
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM tickets WHERE id = ?")) {
                stmt.setInt(1, id);
                deleted = stmt.executeUpdate() > 0;
            }

            conn.commit();
            return deleted;
        } catch (SQLException e) {
            System.err.println("Erro ao deletar chamado: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            Database.releaseConnection(conn);
        }
        return false;
    }

    public boolean update(Ticket ticket) {
        String sql = "UPDATE tickets SET title = ?, description = ?, priority = ?, category_id = ?, " +
            "status = ?, support_id = ?, updated_at = CURRENT_TIMESTAMP, " +
            "closed_at = CASE WHEN ? IN ('CLOSED', 'UNRESOLVED') THEN COALESCE(closed_at, CURRENT_TIMESTAMP) ELSE NULL END WHERE id = ?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, ticket.getTitle());
                stmt.setString(2, ticket.getDescription());
                stmt.setString(3, ticket.getPriority());
                stmt.setInt(4, ticket.getCategoryId());
                stmt.setString(5, ticket.getStatus());
                if (ticket.getSupportId() == null) stmt.setNull(6, java.sql.Types.INTEGER);
                else stmt.setInt(6, ticket.getSupportId());
                stmt.setString(7, ticket.getStatus());
                stmt.setInt(8, ticket.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar chamado: " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return false;
    }

    public boolean updateStatus(int id, String newStatus, Integer newSupportId) {
        String sql = "UPDATE tickets SET status = ?, support_id = COALESCE(?, support_id), " +
                     "closed_at = CASE WHEN ? IN ('CLOSED', 'UNRESOLVED') THEN CURRENT_TIMESTAMP ELSE closed_at END " +
                     "WHERE id = ?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                if (newSupportId != null) {
                    stmt.setInt(2, newSupportId);
                } else {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                }
                stmt.setString(3, newStatus);
                stmt.setInt(4, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status do chamado: " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return false;
    }
}
