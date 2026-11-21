package com.helpdesk.socket.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa o aperto de mao do WebSocket, o health check e o formato das mensagens.
 */
class WebSocketUtilTest {

    @Test
    @DisplayName("Aperto de mao devolve a chave de aceite do exemplo oficial da RFC 6455")
    void apertoDeMaoGeraAChaveCerta() throws Exception {
        String pedido = "GET /ws HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n\r\n";
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        boolean virouWebSocket = WebSocketUtil.performHandshake(
                new ByteArrayInputStream(pedido.getBytes(StandardCharsets.UTF_8)), saida);

        String resposta = saida.toString(StandardCharsets.UTF_8);
        assertTrue(virouWebSocket);
        assertTrue(resposta.contains("101 Switching Protocols"));
        assertTrue(resposta.contains("Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo="));
    }

    @Test
    @DisplayName("Pedido comum (health check do Render) recebe 200 e nao vira WebSocket")
    void healthCheckResponde200() throws Exception {
        String pedido = "GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        boolean virouWebSocket = WebSocketUtil.performHandshake(
                new ByteArrayInputStream(pedido.getBytes(StandardCharsets.UTF_8)), saida);

        String resposta = saida.toString(StandardCharsets.UTF_8);
        assertFalse(virouWebSocket);
        assertTrue(resposta.contains("200 OK"));
        assertTrue(resposta.contains("\"status\":\"UP\""));
    }

    @Test
    @DisplayName("Mensagem de texto escrita pelo servidor tem o formato esperado")
    void mensagemDoServidorSaiSemMascara() throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        WebSocketUtil.writeTextFrame(saida, "oi");

        byte[] bytes = saida.toByteArray();
        assertEquals(4, bytes.length);
        assertEquals(0x81, bytes[0] & 0xFF); // texto, mensagem completa
        assertEquals(2, bytes[1]);           // tamanho 2, sem mascara
        assertEquals('o', bytes[2]);
        assertEquals('i', bytes[3]);
    }

    @Test
    @DisplayName("Mensagem mascarada do cliente e lida de volta no texto original")
    void leMensagemMascaradaDoCliente() throws Exception {
        byte[] mascara = {1, 2, 3, 4};
        byte[] texto = "ola".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(0x81);
        frame.write(0x80 | texto.length);
        frame.write(mascara);
        for (int i = 0; i < texto.length; i++) {
            frame.write(texto[i] ^ mascara[i % 4]);
        }

        String lido = WebSocketUtil.readTextFrame(
                new ByteArrayInputStream(frame.toByteArray()), new ByteArrayOutputStream());

        assertEquals("ola", lido);
    }
}
