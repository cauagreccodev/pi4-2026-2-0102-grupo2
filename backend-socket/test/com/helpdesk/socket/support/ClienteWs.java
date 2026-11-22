package com.helpdesk.socket.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Cliente WebSocket bem simples, so para os testes.
 * Faz o aperto de mao, manda texto (com mascara, como todo cliente deve fazer)
 * e le o texto que o servidor devolver.
 */
public class ClienteWs implements AutoCloseable {

    private static final int TEMPO_MAXIMO_MS = 2_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Socket socket;
    private final InputStream entrada;
    private final OutputStream saida;

    public ClienteWs(int porta) throws IOException {
        socket = new Socket("127.0.0.1", porta);
        socket.setSoTimeout(TEMPO_MAXIMO_MS);
        entrada = socket.getInputStream();
        saida = socket.getOutputStream();
        fazerAperto();
    }

    private void fazerAperto() throws IOException {
        String pedido = "GET /ws HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n"
                + "Sec-WebSocket-Version: 13\r\n\r\n";
        saida.write(pedido.getBytes(StandardCharsets.UTF_8));
        saida.flush();

        // le a resposta HTTP ate a linha em branco que fecha o cabecalho
        StringBuilder resposta = new StringBuilder();
        while (!resposta.toString().endsWith("\r\n\r\n")) {
            int b = entrada.read();
            if (b == -1) {
                throw new IOException("Servidor fechou antes de terminar o aperto de mao");
            }
            resposta.append((char) b);
        }
        if (!resposta.toString().contains("101 Switching Protocols")) {
            throw new IOException("Aperto de mao recusado: " + resposta);
        }
    }

    /** Manda uma mensagem de texto para o servidor. */
    public void enviar(String texto) throws IOException {
        byte[] conteudo = texto.getBytes(StandardCharsets.UTF_8);
        byte[] mascara = new byte[4];
        RANDOM.nextBytes(mascara);

        saida.write(0x81);
        if (conteudo.length <= 125) {
            saida.write(0x80 | conteudo.length);
        } else {
            saida.write(0x80 | 126);
            saida.write((conteudo.length >> 8) & 0xFF);
            saida.write(conteudo.length & 0xFF);
        }
        saida.write(mascara);
        for (int i = 0; i < conteudo.length; i++) {
            saida.write(conteudo[i] ^ mascara[i % 4]);
        }
        saida.flush();
    }

    /**
     * Le a proxima mensagem de texto do servidor.
     * Devolve null se o servidor fechou a conexao sem mandar nada.
     * Se o servidor nao responder dentro do tempo maximo, o teste falha com timeout.
     */
    public String receber() throws IOException {
        while (true) {
            int primeiro = entrada.read();
            if (primeiro == -1) {
                return null;
            }
            int tipo = primeiro & 0x0F;

            int segundo = entrada.read();
            if (segundo == -1) {
                return null;
            }
            int tamanho = segundo & 0x7F;
            if (tamanho == 126) {
                tamanho = (entrada.read() << 8) | entrada.read();
            }

            byte[] conteudo = new byte[tamanho];
            int lidos = 0;
            while (lidos < tamanho) {
                int n = entrada.read(conteudo, lidos, tamanho - lidos);
                if (n == -1) {
                    return null;
                }
                lidos += n;
            }

            if (tipo == 0x8) {
                return null; // servidor mandou o aviso de fechamento
            }
            if (tipo == 0x1) {
                return new String(conteudo, StandardCharsets.UTF_8);
            }
        }
    }

    /** Confere que nada chega nesse cliente durante alguns milissegundos. */
    public boolean ficouEmSilencio(int milissegundos) throws IOException {
        socket.setSoTimeout(milissegundos);
        try {
            return receber() == null;
        } catch (SocketTimeoutException e) {
            return true;
        } finally {
            socket.setSoTimeout(TEMPO_MAXIMO_MS);
        }
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            // nada a fazer
        }
    }
}
