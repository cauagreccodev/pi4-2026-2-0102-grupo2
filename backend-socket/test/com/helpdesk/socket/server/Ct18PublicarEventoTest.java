package com.helpdesk.socket.server;

import com.helpdesk.socket.support.ClienteWs;
import com.helpdesk.socket.support.ServidorDeTeste;
import com.helpdesk.socket.support.UserDaoFalso;
import com.helpdesk.socket.util.JsonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testa o CT18 (publicarEvento): a API avisa o socket que um chamado mudou.
 * Aqui o cliente de teste faz o papel da API.
 */
class Ct18PublicarEventoTest {

    private static final String SEGREDO = System.getenv().getOrDefault(
            "INTERNAL_SECRET", "segredo-interno-entre-backends");

    private ServidorDeTeste servidor;

    @BeforeEach
    void subirServidor() throws Exception {
        servidor = new ServidorDeTeste(new UserDaoFalso());
    }

    @AfterEach
    void desligarServidor() {
        servidor.close();
    }

    private Map<String, Object> eventoCompleto() {
        Map<String, Object> evento = new HashMap<>();
        evento.put("secret", SEGREDO);
        evento.put("action", "UPDATED");
        evento.put("ticketId", 21);
        evento.put("title", "Impressora sem toner");
        evento.put("status", "ASSIGNED");
        evento.put("priority", "MEDIUM");
        evento.put("clientId", 400);
        evento.put("updatedAt", "2026-11-16T14:30:00Z");
        return evento;
    }

    private String publicar(Map<String, Object> evento) throws Exception {
        try (ClienteWs api = new ClienteWs(servidor.getPorta())) {
            api.enviar(JsonUtil.buildEnvelope("publicarEvento", evento));
            return api.receber();
        }
    }

    private boolean foiRecebido(String resposta) {
        return JsonUtil.parseObject(resposta).get("received").getAsBoolean();
    }

    @Test
    @DisplayName("Evento valido: servidor responde received true")
    void eventoValido() throws Exception {
        assertEquals(true, foiRecebido(publicar(eventoCompleto())));
    }

    @Test
    @DisplayName("Secret errado: conexao fecha sem nenhuma resposta")
    void secretErrado() throws Exception {
        Map<String, Object> evento = eventoCompleto();
        evento.put("secret", "senha-errada");

        assertNull(publicar(evento));
    }

    @Test
    @DisplayName("Secret ausente: conexao fecha sem nenhuma resposta")
    void secretAusente() throws Exception {
        Map<String, Object> evento = eventoCompleto();
        evento.remove("secret");

        assertNull(publicar(evento));
    }

    @Test
    @DisplayName("Campo obrigatorio faltando: received false")
    void campoFaltando() throws Exception {
        Map<String, Object> evento = eventoCompleto();
        evento.remove("title");

        assertEquals(false, foiRecebido(publicar(evento)));
    }

    @Test
    @DisplayName("Status fora da lista: received false")
    void statusInvalido() throws Exception {
        Map<String, Object> evento = eventoCompleto();
        evento.put("status", "ABERTO");

        assertEquals(false, foiRecebido(publicar(evento)));
    }

    @Test
    @DisplayName("Mensagem sem data: received false")
    void semData() throws Exception {
        try (ClienteWs api = new ClienteWs(servidor.getPorta())) {
            api.enviar("{\"type\":\"publicarEvento\"}");
            assertEquals(false, foiRecebido(api.receber()));
        }
    }
}
