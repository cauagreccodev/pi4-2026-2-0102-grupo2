package com.helpdesk.socket.model;

/**
 * Dados do usuário carregados da tabela 'users' no Neon DB (CT13)
 * para autenticar a sessão WebSocket (CT19).
 */
public class UserSessionInfo {

    private final int id;
    private final String name;
    private final String email;
    private final String role; // CLIENT, SUPPORT, ADMIN, TECNICO
    private final String department;
    private final boolean active;

    public UserSessionInfo(int id, String name, String email, String role, boolean active) {
        this(id, name, email, role, null, active);
    }

    public UserSessionInfo(int id, String name, String email, String role, String department, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : "CLIENT";
        this.department = department != null ? department.trim() : "";
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

    public String getDepartment() {
        return department;
    }

    public boolean isActive() {
        return active;
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
}
