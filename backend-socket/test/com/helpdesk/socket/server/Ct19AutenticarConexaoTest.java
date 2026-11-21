package com.helpdesk.socket.server;

import com.google.gson.JsonObject;
import com.helpdesk.socket.support.ClienteWs;
import com.helpdesk.socket.support.ServidorDeTeste;
import com.helpdesk.socket.support.Tokens;
import com.helpdesk.socket.support.UserDaoFalso;
import com.helpdesk.socket.util.AuthUtil;
import com.helpdesk.socket.util.JsonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testa o CT19 (autenticarConexao): o frontend abre o WebSocket e manda o token.
 * Usuarios de teste: 1 = cliente ativo, 2 = cliente inativo.
 */
class Ct19AutenticarConexaoTest {

    private ServidorDeTeste servidor;

    @BeforeEach
    void subirServidor() throws Exception {
        UserDaoFalso dao = new UserDaoFalso();
        dao.adicionar(1, "CLIENT", true);
        dao.adicionar(2, "CLIENT", false);
        servidor = new ServidorDeTeste(dao);
    }

    @AfterEach
    void desligarServidor() {
        servidor.close();
    }

    private String autenticarComToken(String token) throws Exception {
        try (ClienteWs cliente = new ClienteWs(servidor.getPorta())) {
            cliente.enviar(JsonUtil.buildEnvelope("autenticarConexao", Map.of("token", token)));
            return cliente.receber();
        }
    }

    private String tipoDaMensagem(String json) {
        return JsonUtil.parseObject(json).get("type").getAsString();
    }

    private String codigoDoErro(String json) {
        return JsonUtil.parseObject(json).getAsJsonObject("data").get("code").getAsString();
    }

    @Test
    @DisplayName("Token valido: servidor responde conexaoAutenticada com id e perfil")
    void tokenValido() throws Exception {
        String resposta = autenticarComToken(AuthUtil.generateToken(1));

        JsonObject mensagem = JsonUtil.parseObject(resposta);
        assertEquals("conexaoAutenticada", mensagem.get("type").getAsString());
        assertEquals(1, mensagem.getAsJsonObject("data").get("userId").getAsInt());
        assertEquals("CLIENT", mensagem.getAsJsonObject("data").get("role").getAsString());
    }

    @Test
    @DisplayName("Token adulterado: erro TOKEN_INVALIDO")
    void tokenAdulterado() throws Exception {
        String resposta = autenticarComToken(Tokens.adulterado(2, 1));

        assertEquals("erro", tipoDaMensagem(resposta));
        assertEquals("TOKEN_INVALIDO", codigoDoErro(resposta));
    }

    @Test
    @DisplayName("Token vencido: erro TOKEN_INVALIDO")
    void tokenVencido() throws Exception {
        String resposta = autenticarComToken(Tokens.vencido(1));

        assertEquals("TOKEN_INVALIDO", codigoDoErro(resposta));
    }

    @Test
    @DisplayName("Token de usuario que nao existe: erro TOKEN_INVALIDO")
    void usuarioNaoExiste() throws Exception {
        String resposta = autenticarComToken(AuthUtil.generateToken(999));

        assertEquals("TOKEN_INVALIDO", codigoDoErro(resposta));
    }

    @Test
    @DisplayName("Usuario inativo: erro USUARIO_INATIVO")
    void usuarioInativo() throws Exception {
        String resposta = autenticarComToken(AuthUtil.generateToken(2));

        assertEquals("USUARIO_INATIVO", codigoDoErro(resposta));
    }

    @Test
    @DisplayName("Mensagem sem token: erro TOKEN_INVALIDO")
    void mensagemSemToken() throws Exception {
        try (ClienteWs cliente = new ClienteWs(servidor.getPorta())) {
            cliente.enviar(JsonUtil.buildEnvelope("autenticarConexao", Map.of()));
            assertEquals("TOKEN_INVALIDO", codigoDoErro(cliente.receber()));
        }
    }

    @Test
    @DisplayName("Mensagem enviada antes de autenticar: erro NAO_AUTENTICADO")
    void mensagemAntesDeAutenticar() throws Exception {
        try (ClienteWs cliente = new ClienteWs(servidor.getPorta())) {
            cliente.enviar(JsonUtil.buildEnvelope("outraCoisa", Map.of("texto", "oi")));
            assertEquals("NAO_AUTENTICADO", codigoDoErro(cliente.receber()));
        }
    }

    @Test
    @DisplayName("Texto que nao e JSON: erro NAO_AUTENTICADO")
    void textoQueNaoEJson() throws Exception {
        try (ClienteWs cliente = new ClienteWs(servidor.getPorta())) {
            cliente.enviar("isso nao e json");
            assertEquals("NAO_AUTENTICADO", codigoDoErro(cliente.receber()));
        }
    }
}
