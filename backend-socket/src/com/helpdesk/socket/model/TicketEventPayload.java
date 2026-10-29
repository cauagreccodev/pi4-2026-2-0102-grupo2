package com.helpdesk.socket.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Payload de entrada do CT18 (publicarEvento) e de saída do CT22 (chamadoAtualizado).
 */
public class TicketEventPayload {

    private String secret;      // Presente apenas na entrada do CT18
    private String action;      // CREATED | UPDATED
    private Integer ticketId;   // Obrigatório
    private String title;       // Obrigatório
    private String status;      // NEW | ASSIGNED | CLOSED | UNRESOLVED
    private String priority;    // LOW | MEDIUM | HIGH | URGENT
    private Integer clientId;   // Obrigatório
    private Integer supportId;  // Opcional
    private String supportName; // Opcional
    private String updatedAt;   // Obrigatório (ISO-8601)

    public String getSecret() {
        return secret;
    }

    public String getAction() {
        return action;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public Integer getClientId() {
        return clientId;
    }

    public Integer getSupportId() {
        return supportId;
    }

    public String getSupportName() {
        return supportName;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Valida os campos obrigatórios definidos na ficha CT18.
     */
    public boolean hasRequiredFields() {
        if (isBlank(action) || (!"CREATED".equals(action) && !"UPDATED".equals(action))) return false;
        if (ticketId == null || ticketId <= 0) return false;
        if (isBlank(title)) return false;
        if (isBlank(status) || !isValidStatus(status)) return false;
        if (isBlank(priority) || !isValidPriority(priority)) return false;
        if (clientId == null || clientId <= 0) return false;
        if (isBlank(updatedAt)) return false;
        return true;
    }

    private boolean isValidStatus(String s) {
        return "NEW".equals(s) || "ASSIGNED".equals(s) || "CLOSED".equals(s) || "UNRESOLVED".equals(s);
    }

    private boolean isValidPriority(String p) {
        return "LOW".equals(p) || "MEDIUM".equals(p) || "HIGH".equals(p) || "URGENT".equals(p);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Converte para o objeto "data" do CT22 (chamadoAtualizado), omitindo o campo 'secret'.
     */
    public Map<String, Object> toCt22DataMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("action", action);
        map.put("ticketId", ticketId);
        map.put("title", title);
        map.put("status", status);
        map.put("priority", priority);
        map.put("clientId", clientId);
        if (supportId != null) {
            map.put("supportId", supportId);
        }
        if (supportName != null && !supportName.isBlank()) {
            map.put("supportName", supportName);
        }
        map.put("updatedAt", updatedAt);
        return map;
    }
}
