package com.helpdesk.socket.model;

import com.helpdesk.socket.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa as regras dos campos do CT18 (o que a ficha manda recusar).
 */
class TicketEventPayloadTest {

    private Map<String, Object> eventoCompleto() {
        Map<String, Object> evento = new HashMap<>();
        evento.put("action", "CREATED");
        evento.put("ticketId", 10);
        evento.put("title", "Computador nao liga");
        evento.put("status", "NEW");
        evento.put("priority", "HIGH");
        evento.put("clientId", 3);
        evento.put("updatedAt", "2026-11-16T10:00:00Z");
        return evento;
    }

    private TicketEventPayload montar(Map<String, Object> evento) {
        return JsonUtil.fromJson(JsonUtil.toJson(evento), TicketEventPayload.class);
    }

    @Test
    @DisplayName("Evento completo e aceito")
    void eventoCompletoEValido() {
        assertTrue(montar(eventoCompleto()).hasRequiredFields());
    }

    @Test
    @DisplayName("Tecnico responsavel e opcional")
    void tecnicoNaoEObrigatorio() {
        Map<String, Object> evento = eventoCompleto();
        evento.put("supportId", 5);
        evento.put("supportName", "Carlos");
        assertTrue(montar(evento).hasRequiredFields());
    }

    @Test
    @DisplayName("Campo obrigatorio faltando e recusado")
    void campoFaltandoERecusado() {
        for (String campo : new String[]{"action", "ticketId", "title", "status", "priority", "clientId", "updatedAt"}) {
            Map<String, Object> evento = eventoCompleto();
            evento.remove(campo);
            assertFalse(montar(evento).hasRequiredFields(), "Deveria recusar sem o campo " + campo);
        }
    }

    @Test
    @DisplayName("Valores fora da lista da ficha sao recusados")
    void valorInvalidoERecusado() {
        Map<String, Object> acao = eventoCompleto();
        acao.put("action", "DELETED");
        assertFalse(montar(acao).hasRequiredFields());

        Map<String, Object> status = eventoCompleto();
        status.put("status", "ABERTO");
        assertFalse(montar(status).hasRequiredFields());

        Map<String, Object> prioridade = eventoCompleto();
        prioridade.put("priority", "CRITICA");
        assertFalse(montar(prioridade).hasRequiredFields());
    }

    @Test
    @DisplayName("Ids zerados ou titulo em branco sao recusados")
    void idEtituloRuins() {
        Map<String, Object> chamado = eventoCompleto();
        chamado.put("ticketId", 0);
        assertFalse(montar(chamado).hasRequiredFields());

        Map<String, Object> cliente = eventoCompleto();
        cliente.put("clientId", 0);
        assertFalse(montar(cliente).hasRequiredFields());

        Map<String, Object> titulo = eventoCompleto();
        titulo.put("title", "   ");
        assertFalse(montar(titulo).hasRequiredFields());
    }

    @Test
    @DisplayName("A mensagem que vai para o frontend (CT22) nunca leva o secret")
    void mensagemParaOFrontendNaoTemSecret() {
        Map<String, Object> evento = eventoCompleto();
        evento.put("secret", "segredo-que-nao-pode-vazar");

        Map<String, Object> saida = montar(evento).toCt22DataMap();

        assertFalse(saida.containsKey("secret"));
        assertEquals(10, saida.get("ticketId"));
    }
}
