package com.helpdesk.api.handler;

import com.helpdesk.api.dao.TicketDao;
import com.helpdesk.api.dao.TicketTimelineDao;
import com.helpdesk.api.model.Ticket;
import com.helpdesk.api.model.TicketTimeline;
import com.helpdesk.api.socket.SocketEventPublisher;
import com.helpdesk.api.util.AuthUtil;
import com.helpdesk.api.util.HttpHelper;
import com.helpdesk.api.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketHandler implements HttpHandler {

    private final TicketDao ticketDao = new TicketDao();
    private final TicketTimelineDao timelineDao = new TicketTimelineDao();

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
            if ("GET".equalsIgnoreCase(method) && "/api/chamados".equals(path)) {
                // Listar chamados com filtros
                Map<String, String> params = HttpHelper.getQueryParams(exchange.getRequestURI().getQuery());
                String status = params.get("status");
                Integer clientId = params.containsKey("clientId") && !params.get("clientId").isEmpty() ? Integer.parseInt(params.get("clientId")) : null;
                Integer supportId = params.containsKey("supportId") && !params.get("supportId").isEmpty() ? Integer.parseInt(params.get("supportId")) : null;

                List<Ticket> tickets = ticketDao.findAll(status, clientId, supportId);
                HttpHelper.sendJson(exchange, 200, tickets);

            } else if ("POST".equalsIgnoreCase(method) && "/api/chamados".equals(path)) {
                // Criar chamado
                String body = HttpHelper.readBody(exchange);
                Map<String, Object> data = JsonUtil.fromJson(body, Map.class);

                Ticket t = new Ticket();
                t.setTitle((String) data.get("title"));
                t.setDescription((String) data.get("description"));
                t.setPriority((String) data.get("priority"));
                t.setCategoryId(((Double) data.get("categoryId")).intValue());
                t.setClientId(userId);

                Ticket created = ticketDao.insert(t);
                if (created != null) {
                    // Adicionar timeline
                    TicketTimeline tl = new TicketTimeline();
                    tl.setTicketId(created.getId());
                    tl.setAuthorId(userId);
                    tl.setEventType("CREATION");
                    tl.setMessage("Chamado criado com prioridade " + created.getPriority());
                    timelineDao.insert(tl);

                    // CT18: avisa o socket que um chamado foi criado
                    SocketEventPublisher.publishTicketChanged("CREATION", created.getId(), created.getStatus(), tl.getMessage());

                    HttpHelper.sendJson(exchange, 201, created);
                } else {
                    HttpHelper.sendError(exchange, 500, "Erro ao criar chamado");
                }

            } else if (path.startsWith("/api/chamados/")) {
                String idStr = HttpHelper.getPathParam(path, 3);
                if (idStr == null) {
                    HttpHelper.sendError(exchange, 400, "ID não informado");
                    return;
                }
                int ticketId = Integer.parseInt(idStr);

                if ("GET".equalsIgnoreCase(method)) {
                    // Detalhes + timeline
                    Ticket ticket = ticketDao.findById(ticketId);
                    if (ticket == null) {
                        HttpHelper.sendError(exchange, 404, "Chamado não encontrado");
                        return;
                    }
                    List<TicketTimeline> timeline = timelineDao.findByTicketId(ticketId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("ticket", ticket);
                    response.put("timeline", timeline);
                    
                    HttpHelper.sendJson(exchange, 200, response);

                } else if ("PUT".equalsIgnoreCase(method)) {
                    // Atualizar status/suporte
                    String body = HttpHelper.readBody(exchange);
                    Map<String, Object> data = JsonUtil.fromJson(body, Map.class);
                    
                    String newStatus = (String) data.get("status");
                    Integer newSupportId = data.containsKey("supportId") && data.get("supportId") != null ? 
                        ((Double) data.get("supportId")).intValue() : null;
                    String message = (String) data.get("message");

                    if (newStatus == null || message == null) {
                        HttpHelper.sendError(exchange, 400, "Status e message são obrigatórios");
                        return;
                    }

                    boolean updated = ticketDao.updateStatus(ticketId, newStatus, newSupportId);
                    if (updated) {
                        TicketTimeline tl = new TicketTimeline();
                        tl.setTicketId(ticketId);
                        tl.setAuthorId(userId);
                        tl.setEventType("STATUS_CHANGE"); // simplificado
                        tl.setMessage(message);
                        timelineDao.insert(tl);

                        // CT18: avisa o socket que o chamado mudou
                        SocketEventPublisher.publishTicketChanged("STATUS_CHANGE", ticketId, newStatus, message);

                        HttpHelper.sendJson(exchange, 200, "{\"success\": true}");
                    } else {
                        HttpHelper.sendError(exchange, 404, "Chamado não encontrado");
                    }
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    // Deletar chamado
                    boolean deleted = ticketDao.deleteById(ticketId);
                    if (deleted) {
                        // CT18: avisa o socket que o chamado foi excluído
                        SocketEventPublisher.publishTicketChanged("DELETION", ticketId, null, "Chamado #" + ticketId + " excluído");
                        HttpHelper.sendJson(exchange, 200, "{\"success\": true}");
                    } else {
                        HttpHelper.sendError(exchange, 404, "Chamado não encontrado");
                    }
                } else {
                    HttpHelper.sendError(exchange, 405, "Method not allowed");
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
