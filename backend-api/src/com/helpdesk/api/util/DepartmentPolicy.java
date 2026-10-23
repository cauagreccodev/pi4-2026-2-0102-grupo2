package com.helpdesk.api.util;

import com.helpdesk.api.model.User;
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

    public static boolean canManageTickets(User user) {
        return user != null && "TI".equals(normalize(user.getDepartment()));
    }
}
