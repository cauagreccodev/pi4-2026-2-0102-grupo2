package com.helpdesk.socket.server;

import com.google.gson.JsonObject;
import com.helpdesk.socket.support.ClienteWs;
import com.helpdesk.socket.support.ServidorDeTeste;
import com.helpdesk.socket.support.UserDaoFalso;
import com.helpdesk.socket.util.AuthUtil;
import com.helpdesk.socket.util.JsonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa o CT22 (chamadoAtualizado): quem recebe o aviso quando a API publica um evento.
 *
 * Usuarios de teste:
 *   201 = cliente dono do chamado
 *   202 = tecnico responsavel pelo chamado
 *   203 = outro tecnico (SUPPORT recebe tudo)
 *   204 = administrador (ADMIN recebe tudo)
 *   205 = outro cliente (nao deve receber nada)
 */
class Ct22ChamadoAtualizadoTest {

    private static final String SEGREDO = System.getenv().getOrDefault(
            "INTERNAL_SECRET", "segredo-interno-entre-backends");

    private ServidorDeTeste servidor;
    private final List<ClienteWs> conectados = new ArrayList<>();

    @BeforeEach
    void subirServidor() throws Exception {
        UserDaoFalso dao = new UserDaoFalso();
        dao.adicionar(201, "CLIENT", true);
        dao.adicionar(202, "SUPPORT", true);
        dao.adicionar(203, "SUPPORT", true);
        dao.adicionar(204, "ADMIN", true);
        dao.adicionar(205, "CLIENT", true);
        dao.adicionar(206, "CLIENT", "TI", true);
        dao.adicionar(207, "CLIENT", "FINANCEIRO", true);
        dao.adicionar(208, "CLIENT", "COMPRAS", true);
        servidor = new ServidorDeTeste(dao);
    }

    @AfterEach
    void desligarTudo() {
        for (ClienteWs cliente : conectados) {
            cliente.close();
        }
        servidor.close();
    }

    /** Conecta como o frontend e so devolve depois do servidor confirmar o login. */
    private ClienteWs conectarComo(int userId) throws Exception {
        ClienteWs cliente = new ClienteWs(servidor.getPorta());
        conectados.add(cliente);
        cliente.enviar(JsonUtil.buildEnvelope("autenticarConexao", Map.of("token", AuthUtil.generateToken(userId))));
        String resposta = cliente.receber();
        assertEquals("conexaoAutenticada", JsonUtil.parseObject(resposta).get("type").getAsString());
        return cliente;
    }

    private void publicarComoApi(Integer supportId) throws Exception {
        Map<String, Object> evento = new HashMap<>();
        evento.put("secret", SEGREDO);
        evento.put("action", "UPDATED");
        evento.put("ticketId", 77);
        evento.put("title", "Rede lenta no setor");
        evento.put("status", "ASSIGNED");
        evento.put("priority", "HIGH");
        evento.put("clientId", 201);
        evento.put("updatedAt", "2026-11-16T09:00:00Z");
        if (supportId != null) {
            evento.put("supportId", supportId);
            evento.put("supportName", "Tecnico " + supportId);
        }
        try (ClienteWs api = new ClienteWs(servidor.getPorta())) {
            api.enviar(JsonUtil.buildEnvelope("publicarEvento", evento));
            assertEquals("{\"received\":true}", api.receber());
        }
    }

    private void conferirAviso(ClienteWs cliente) throws Exception {
        String texto = cliente.receber();
        JsonObject mensagem = JsonUtil.parseObject(texto);
        JsonObject dados = mensagem.getAsJsonObject("data");

        assertEquals("chamadoAtualizado", mensagem.get("type").getAsString());
        assertEquals(77, dados.get("ticketId").getAsInt());
        assertEquals("ASSIGNED", dados.get("status").getAsString());
        assertFalse(dados.has("secret"), "O secret nao pode ir para o frontend");
    }

    @Test
    @DisplayName("Aviso chega no cliente dono, no tecnico responsavel e em todo SUPPORT e ADMIN")
    void avisoChegaNosDestinatariosCertos() throws Exception {
        ClienteWs dono = conectarComo(201);
        ClienteWs responsavel = conectarComo(202);
        ClienteWs outroTecnico = conectarComo(203);
        ClienteWs admin = conectarComo(204);
        ClienteWs outroCliente = conectarComo(205);

        publicarComoApi(202);

        conferirAviso(dono);
        conferirAviso(responsavel);
        conferirAviso(outroTecnico);
        conferirAviso(admin);
        assertTrue(outroCliente.ficouEmSilencio(700), "Outro cliente nao deveria receber o aviso");
    }

    @Test
    @DisplayName("Sem tecnico responsavel: o cliente dono e SUPPORT/ADMIN recebem, outro cliente nao")
    void avisoSemTecnicoResponsavel() throws Exception {
        ClienteWs dono = conectarComo(201);
        ClienteWs tecnico = conectarComo(203);
        ClienteWs admin = conectarComo(204);
        ClienteWs outroCliente = conectarComo(205);

        publicarComoApi(null);

        conferirAviso(dono);
        conferirAviso(tecnico);
        conferirAviso(admin);
        assertTrue(outroCliente.ficouEmSilencio(700), "Outro cliente nao deveria receber o aviso");
    }

    @Test
    @DisplayName("Cliente com duas abas abertas recebe o aviso nas duas")
    void duasAbasDoMesmoUsuario() throws Exception {
        ClienteWs abaUm = conectarComo(201);
        ClienteWs abaDois = conectarComo(201);

        publicarComoApi(null);

        conferirAviso(abaUm);
        conferirAviso(abaDois);
    }

    @Test
    @DisplayName("Usuario com departamento TI recebe aviso mesmo com role CLIENT")
    void usuarioComDepartamentoTiRecebeAviso() throws Exception {
        ClienteWs tiUser = conectarComo(206);
        publicarComoApi(null);
        conferirAviso(tiUser);
    }

    @Test
    @DisplayName("Broadcast setorial entrega aviso para colega do mesmo departamento")
    void colegaDoMesmoDepartamentoRecebeAviso() throws Exception {
        ClienteWs colegaFinanceiro = conectarComo(207);
        ClienteWs colegaCompras = conectarComo(208);

        Map<String, Object> evento = new HashMap<>();
        evento.put("secret", SEGREDO);
        evento.put("action", "CREATED");
        evento.put("ticketId", 88);
        evento.put("title", "Ajuste na planilha financeira");
        evento.put("status", "NEW");
        evento.put("priority", "MEDIUM");
        evento.put("clientId", 201);
        evento.put("department", "FINANCEIRO");
        evento.put("updatedAt", "2026-11-16T10:00:00Z");

        try (ClienteWs api = new ClienteWs(servidor.getPorta())) {
            api.enviar(JsonUtil.buildEnvelope("publicarEvento", evento));
            assertEquals("{\"received\":true}", api.receber());
        }

        String texto = colegaFinanceiro.receber();
        JsonObject mensagem = JsonUtil.parseObject(texto);
        assertEquals("chamadoAtualizado", mensagem.get("type").getAsString());
        assertEquals(88, mensagem.getAsJsonObject("data").get("ticketId").getAsInt());
        assertEquals("FINANCEIRO", mensagem.getAsJsonObject("data").get("department").getAsString());

        assertTrue(colegaCompras.ficouEmSilencio(700), "Colega de compras nao deve receber chamado do financeiro");
    }
}
