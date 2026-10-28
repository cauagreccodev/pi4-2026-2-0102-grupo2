package com.helpdesk.api.dao;

import com.helpdesk.api.config.Database;
import com.helpdesk.api.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setIcon(rs.getString("icon"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getBoolean("is_active"));
        category.setCreatedAt(rs.getTimestamp("created_at").toString());
        return category;
    }

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_active = true ORDER BY name";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar categorias: " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return categories;
    }

    public Category insert(Category category) {
        String sql = "INSERT INTO categories (name, icon, description, is_active) VALUES (?, ?, ?, ?) RETURNING *";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                stmt.setString(2, category.getIcon());
                stmt.setString(3, category.getDescription());
                stmt.setBoolean(4, category.isActive());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToCategory(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir categoria: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                Database.releaseConnection(conn);
            }
        }
        return null;
    }

    public Category findById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToCategory(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categoria: " + e.getMessage());
        } finally {
            Database.releaseConnection(conn);
        }
        return null;
    }
}
