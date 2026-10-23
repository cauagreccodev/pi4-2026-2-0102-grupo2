package com.helpdesk.api.handler;

import com.helpdesk.api.dao.UserDao;
import com.helpdesk.api.model.User;
import com.helpdesk.api.util.AuthUtil;
import com.helpdesk.api.util.DepartmentPolicy;
import com.helpdesk.api.util.HttpHelper;
import com.helpdesk.api.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UserHandler implements HttpHandler {

    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendOptions(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && (path.equals("/api/users") || path.equals("/users"))) {
                // Lista de usuários (usado para atribuição)
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    HttpHelper.sendError(exchange, 401, "Não autenticado");
                    return;
                }
                String token = authHeader.substring(7);
                Integer userId = AuthUtil.validateToken(token);
                if (userId == null) {
                    HttpHelper.sendError(exchange, 401, "Token inválido ou expirado");
                    return;
                }

                List<User> users = userDao.findAll();
                HttpHelper.sendJson(exchange, 200, users);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) && (path.equals("/api/register") || path.equals("/register"))) {
                // Registro (público)
                String body = HttpHelper.readBody(exchange);
                Map<String, String> data = JsonUtil.fromJson(body, Map.class);
                
                String email = data.get("email");
                String password = data.get("password");
                String name = data.get("name");
                String role = data.get("role");

                if (email == null || password == null || name == null || role == null) {
                    HttpHelper.sendError(exchange, 400, "Dados incompletos");
                    return;
                }

                if (userDao.findByEmail(email) != null) {
                    HttpHelper.sendError(exchange, 400, "Email já cadastrado");
                    return;
                }

                User newUser = new User();
                newUser.setName(name);
                newUser.setEmail(email);
                String department = DepartmentPolicy.normalize(data.get("department"));
                if (department == null) {
                    HttpHelper.sendError(exchange, 400, "Selecione um setor válido");
                    return;
                }
                // Public registration never grants administrator/support roles.
                newUser.setRole("CLIENT");
                newUser.setDepartment(department);
                newUser.setJobTitle(data.get("jobTitle"));

                User created = userDao.insert(newUser, password);
                if (created != null) {
                    HttpHelper.sendJson(exchange, 201, created);
                } else {
                    HttpHelper.sendError(exchange, 500, "Erro ao criar usuário");
                }
            } else {
                HttpHelper.sendError(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Erro interno do servidor");
        }
    }
}
