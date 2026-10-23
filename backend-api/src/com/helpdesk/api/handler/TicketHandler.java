package com.helpdesk.api.handler;

import com.helpdesk.api.dao.TicketDao;
import com.helpdesk.api.dao.TicketTimelineDao;
import com.helpdesk.api.dao.UserDao;
import com.helpdesk.api.model.Ticket;
import com.helpdesk.api.model.TicketTimeline;
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
import java.util.Set;

public class TicketHandler implements HttpHandler {

    private final TicketDao ticketDao = new TicketDao();
    private final TicketTimelineDao timelineDao = new TicketTimelineDao();
    private final UserDao userDao = new UserDao();
    private static final Set<String> STATUSES = Set.of("NEW", "ASSIGNED", "CLOSED", "UNRESOLVED");
    private static final Set<String> PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH", "URGENT");

    private static Integer positiveId(Object value) {
        if (value == null) return null;
        if (!(value instanceof Number)) throw new IllegalArgumentException("Identificador inválido");
        double number = ((Number) value).doubleValue();
        if (!Double.isFinite(number) || number <= 0 || number > Integer.MAX_VALUE || number != Math.floor(number)) {
            throw new IllegalArgumentException("Identificador inválido");
        }
        return (int) number;
    }

    private static void validateDetails(Ticket ticket) {
        if (ticket.getTitle() == null || ticket.getTitle().isBlank() ||
            ticket.getDescription() == null || ticket.getDescription().isBlank() ||
            ticket.getCategoryId() <= 0 || ticket.getPriority() == null || !PRIORITIES.contains(ticket.getPriority())) {
            throw new IllegalArgumentException("Informe título, descrição, categoria e prioridade válidos");
        }
    }

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
            User actor = userDao.findById(userId);
            if (actor == null) {
                HttpHelper.sendError(exchange, 401, "Usuário não encontrado");
                return;
            }
            if (("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && !DepartmentPolicy.canManageTickets(actor)) {
                HttpHelper.sendError(exchange, 403, "Somente o departamento de TI pode alterar ou excluir chamados");
                return;
            }
            if ("GET".equalsIgnoreCase(method) && "/api/chamados".equals(path)) {
                // Listar chamados com filtros
                Map<String, String> params = HttpHelper.getQueryParams(exchange.getRequestURI().getQuery());
                String status = params.get("status");
                Integer clientId = params.containsKey("clientId") && !params.get("clientId").isEmpty() ? Integer.parseInt(params.get("clientId")) : null;
                Integer supportId = params.containsKey("supportId") && !params.get("supportId").isEmpty() ? Integer.parseInt(params.get("supportId")) : null;

                List<Ticket> tickets = ticketDao.findAll(status, clientId, supportId);
                if (!DepartmentPolicy.canManageTickets(actor)) {
                    Set<Integer> members = DepartmentPolicy.departmentMemberIds(actor, userDao.findAll());
                    tickets = tickets.stream().filter(ticket -> DepartmentPolicy.canViewTicket(actor, ticket, members)).toList();
                }
                HttpHelper.sendJson(exchange, 200, tickets);

            } else if ("POST".equalsIgnoreCase(method) && "/api/chamados".equals(path)) {
                // Criar chamado
                String body = HttpHelper.readBody(exchange);
                Map<String, Object> data = JsonUtil.fromJson(body, Map.class);

                Ticket t = new Ticket();
                t.setTitle((String) data.get("title"));
                t.setDescription((String) data.get("description"));
                t.setPriority((String) data.get("priority"));
                Integer categoryId = positiveId(data.get("categoryId"));
                t.setCategoryId(categoryId == null ? 0 : categoryId);
                t.setClientId(userId);
                Integer supportId = positiveId(data.get("supportId"));
                User assignee = supportId == null ? null : userDao.findById(supportId);
                if (supportId != null && assignee == null) {
                    HttpHelper.sendError(exchange, 400, "Responsável não encontrado");
                    return;
                }
                t.setSupportId(supportId);
                t.setStatus("NEW");
                validateDetails(t);

                Ticket created = ticketDao.insert(t);
                if (created != null) {
                    // Adicionar timeline
                    TicketTimeline tl = new TicketTimeline();
                    tl.setTicketId(created.getId());
                    tl.setAuthorId(userId);
                    tl.setEventType("CREATION");
                    tl.setMessage("Chamado criado com prioridade " + created.getPriority() +
                        (assignee == null ? ". Em aberto." : ". Atribuído a " + assignee.getName() + " (" + assignee.getDepartment() + ")."));
                    timelineDao.insert(tl);

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

                if ("POST".equalsIgnoreCase(method) && path.equals("/api/chamados/" + ticketId + "/iniciar")) {
                    if (!DepartmentPolicy.canManageTickets(actor)) {
                        HttpHelper.sendError(exchange, 403, "Somente o departamento de TI pode iniciar o atendimento");
                        return;
                    }
                    if (ticketDao.findById(ticketId) == null) {
                        HttpHelper.sendError(exchange, 404, "Chamado não encontrado");
                        return;
                    }
                    boolean started = ticketDao.startService(ticketId, userId,
                        "Atendimento iniciado por " + actor.getName() + " (TI) ao abrir o chamado.");
                    HttpHelper.sendJson(exchange, 200, Map.of("started", started));
                } else if ("GET".equalsIgnoreCase(method)) {
                    // Detalhes + timeline
                    Ticket ticket = ticketDao.findById(ticketId);
                    if (ticket == null) {
                        HttpHelper.sendError(exchange, 404, "Chamado não encontrado");
                        return;
                    }
                    Set<Integer> members = DepartmentPolicy.canManageTickets(actor) ? Set.of()
                        : DepartmentPolicy.departmentMemberIds(actor, userDao.findAll());
                    if (!DepartmentPolicy.canViewTicket(actor, ticket, members)) {
                        HttpHelper.sendError(exchange, 404, "Chamado n?o encontrado");
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
                    String message = (String) data.get("message");

                    if (newStatus == null || message == null) {
                        HttpHelper.sendError(exchange, 400, "Status e message são obrigatórios");
                        return;
                    }

                    if (!STATUSES.contains(newStatus)) {
                        HttpHelper.sendError(exchange, 400, "Status inválido");
                        return;
                    }
                    Ticket ticket = ticketDao.findById(ticketId);
                    if (ticket == null) {
                        HttpHelper.sendError(exchange, 404, "Chamado não encontrado");
                        return;
                    }
                    Integer newSupportId = data.containsKey("supportId") ? positiveId(data.get("supportId")) : ticket.getSupportId();
                    if ("NEW".equals(newStatus) && !data.containsKey("supportId")) newSupportId = null;
                    if (newSupportId != null && userDao.findById(newSupportId) == null) {
                        HttpHelper.sendError(exchange, 400, "Responsável não encontrado");
                        return;
                    }
                    if (("ASSIGNED".equals(newStatus) || "CLOSED".equals(newStatus)) && newSupportId == null) {
                        HttpHelper.sendError(exchange, 400, "Este status exige um responsável");
                        return;
                    }
                    ticket.setStatus(newStatus);
                    ticket.setSupportId(newSupportId);
                    if (data.containsKey("title")) ticket.setTitle((String) data.get("title"));
                    if (data.containsKey("description")) ticket.setDescription((String) data.get("description"));
                    if (data.containsKey("priority")) ticket.setPriority((String) data.get("priority"));
                    if (data.containsKey("categoryId")) {
                        Integer categoryId = positiveId(data.get("categoryId"));
                        ticket.setCategoryId(categoryId == null ? 0 : categoryId);
                    }
                    validateDetails(ticket);

                    boolean updated = ticketDao.update(ticket);
                    if (updated) {
                        TicketTimeline tl = new TicketTimeline();
                        tl.setTicketId(ticketId);
                        tl.setAuthorId(userId);
                        tl.setEventType("STATUS_CHANGE"); // simplificado
                        tl.setMessage(message);
                        timelineDao.insert(tl);

                        HttpHelper.sendJson(exchange, 200, "{\"success\": true}");
                    } else {
                        HttpHelper.sendError(exchange, 404, "Chamado não encontrado");
                    }
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    // Deletar chamado
                    boolean deleted = ticketDao.deleteById(ticketId);
                    if (deleted) {
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
        } catch (IllegalArgumentException | ClassCastException e) {
            HttpHelper.sendError(exchange, 400, "Dados inválidos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Erro interno do servidor");
        }
    }
}
