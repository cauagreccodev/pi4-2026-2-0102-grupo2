package com.helpdesk.socket.util;

import com.helpdesk.socket.support.Tokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testa a conferencia do token (parte do CT19).
 * Token bom devolve o id do usuario. Qualquer problema devolve null.
 */
class AuthUtilTest {

    @Test
    @DisplayName("Token gerado pelo proprio sistema e aceito")
    void tokenValidoDevolveOIdDoUsuario() {
        String token = AuthUtil.generateToken(7);
        assertEquals(7, AuthUtil.validateToken(token));
    }

    @Test
    @DisplayName("Token com a palavra Bearer na frente tambem e aceito")
    void tokenComBearerFunciona() {
        String token = "Bearer " + AuthUtil.generateToken(8);
        assertEquals(8, AuthUtil.validateToken(token));
    }

    @Test
    @DisplayName("Token vencido e recusado")
    void tokenVencidoERecusado() {
        assertNull(AuthUtil.validateToken(Tokens.vencido(7)));
    }

    @Test
    @DisplayName("Token com o conteudo trocado e recusado")
    void tokenAdulteradoERecusado() {
        assertNull(AuthUtil.validateToken(Tokens.adulterado(7, 1)));
    }

    @Test
    @DisplayName("Token vazio, nulo ou sem ponto e recusado")
    void tokenInvalidoERecusado() {
        assertNull(AuthUtil.validateToken(null));
        assertNull(AuthUtil.validateToken(""));
        assertNull(AuthUtil.validateToken("   "));
        assertNull(AuthUtil.validateToken("textosemponto"));
        assertNull(AuthUtil.validateToken("abc.def.ghi"));
    }
}
