package com.helpdesk.socket.model;

/**
 * Dados do usuário carregados da tabela 'users' no Neon DB (CT13)
 * para autenticar a sessão WebSocket (CT19).
 */
public class UserSessionInfo {

    private final int id;
    private final String name;
    private final String email;
    private final String role; // CLIENT, SUPPORT, ADMIN
    private final boolean active;

    public UserSessionInfo(int id, String name, String email, String role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : "CLIENT";
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSupportOrAdmin() {
        return "SUPPORT".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }
}
