package com.helpdesk.api.handler;

import com.helpdesk.api.dao.CategoryDao;
import com.helpdesk.api.dao.UserDao;
import com.helpdesk.api.model.Category;
import com.helpdesk.api.model.User;
import com.helpdesk.api.util.AuthUtil;
import com.helpdesk.api.util.DepartmentPolicy;
import com.helpdesk.api.util.HttpHelper;
import com.helpdesk.api.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryHandler implements HttpHandler {

    private final CategoryDao categoryDao = new CategoryDao();
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendOptions(exchange);
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

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method) && ("/api/categorias".equals(path) || "/categorias".equals(path))) {
                List<Category> categories = categoryDao.findAll();
                HttpHelper.sendJson(exchange, 200, categories);
            } else if ("POST".equalsIgnoreCase(method) && ("/api/categorias".equals(path) || "/categorias".equals(path))) {
                // Apenas o departamento de TI pode criar categorias
                User actor = userDao.findById(userId);
                if (actor == null || !DepartmentPolicy.canManageTickets(actor)) {
                    HttpHelper.sendError(exchange, 403, "Acesso restrito ao departamento de TI");
                    return;
                }

                String body = HttpHelper.readBody(exchange);
                Map<String, String> data = JsonUtil.fromJson(body, Map.class);

                String name = data.get("name");
                if (name == null || name.isBlank()) {
                    HttpHelper.sendError(exchange, 400, "Nome da categoria é obrigatório");
                    return;
                }

                Category category = new Category();
                category.setName(name);
                category.setIcon(data.get("icon"));
                category.setDescription(data.get("description"));
                category.setActive(true);

                Category created = categoryDao.insert(category);
                if (created != null) {
                    HttpHelper.sendJson(exchange, 201, created);
                } else {
                    HttpHelper.sendError(exchange, 500, "Erro ao criar categoria");
                }
            } else {
                HttpHelper.sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Erro interno do servidor");
        }
    }
}
