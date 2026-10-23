package com.helpdesk.api.util;

import com.helpdesk.api.model.User;
import com.helpdesk.api.model.Ticket;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

/** Department names shared with the frontend. Roles do not grant TI access. */
public final class DepartmentPolicy {
    private DepartmentPolicy() {}

    public static String normalize(String value) {
        if (value == null) return null;
        String key = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (key.equals("TECNOLOGIA") || key.equals("TECNOLOGIADAINFORMACAO") || key.equals("TECNOLOGIADEINFORMACAO")) key = "TI";
        return Map.of(
            "FINANCEIRO", "Financeiro", "COMPRAS", "Compras", "TI", "TI",
            "PRODUCAO", "Produção", "RECURSOSHUMANOS", "Recursos Humanos",
            "MARKETING", "Marketing", "VENDAS", "Vendas", "LOGISTICA", "Logística",
            "ADMINISTRATIVO", "Administrativo"
        ).get(key);
    }

    public static Set<Integer> departmentMemberIds(User actor, List<User> users) {
        if (actor == null) return Set.of();
        String department = normalize(actor.getDepartment());
        if (department == null) return Set.of();
        Set<Integer> members = users.stream()
            .filter(user -> department.equals(normalize(user.getDepartment())))
            .map(User::getId).collect(Collectors.toSet());
        members.add(actor.getId());
        return members;
    }

    public static boolean canViewTicket(User actor, Ticket ticket, Set<Integer> members) {
        return actor != null && ticket != null && (canManageTickets(actor) ||
            members.contains(ticket.getClientId()) ||
            (ticket.getSupportId() != null && members.contains(ticket.getSupportId())));
    }

    public static boolean canManageTickets(User user) {
        return user != null && "TI".equals(normalize(user.getDepartment()));
    }
}
