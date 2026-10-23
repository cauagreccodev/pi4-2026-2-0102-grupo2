package com.helpdesk.socket.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Tradutor do Java ServerSocket para o WebSocket (RFC 6455) e HTTP (GET /health, GET /).
 * Implementação do protocolo WebSocket (RFC 6455) em Java Puro.
 * Realiza o Handshake HTTP -> WebSocket (101 Switching Protocols),
 * lê/escreve frames WebSocket e responde 200 OK aos Health Checks do Render.
 */
public class WebSocketUtil {

    private static final String WS_MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * Realiza o handshake HTTP -> WebSocket.
     * - Se tiver Sec-WebSocket-Key: promove a conexão para WebSocket (101 Switching Protocols) e retorna true.
     * - Se for Health Check do Render (GET /health, GET /, HEAD): responde 200 OK e retorna false.
     */
    public static boolean performHandshake(InputStream in, OutputStream out) throws IOException { //handshake: aperto de mao
        String requestLine = readHttpLine(in);
        if (requestLine == null || requestLine.isEmpty()) {
            return false;
        }

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readHttpLine(in)) != null && !line.isEmpty()) {
            int separator = line.indexOf(':');
            if (separator > 0) {
                String key = line.substring(0, separator).trim().toLowerCase();
                String value = line.substring(separator + 1).trim();
                headers.put(key, value);
            }
        }

        String wsKey = headers.get("sec-websocket-key");
        if (wsKey == null) {
            // Health Check / Keep-Alive do Render (GET /health, GET /api/health, GET /, HEAD /)
            boolean isHead = requestLine.startsWith("HEAD ");
            String body = "{\"status\":\"UP\",\"service\":\"backend-socket\",\"protocol\":\"websocket\",\"version\":\"v0.4.3\",\"frontend\":\"https://help-desk-pi-iv.vercel.app\"}";
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            String httpResponse = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json; charset=utf-8\r\n"
                    + "Access-Control-Allow-Origin: *\r\n"
                    + "Content-Length: " + bodyBytes.length + "\r\n"
                    + "Connection: close\r\n\r\n";
            out.write(httpResponse.getBytes(StandardCharsets.UTF_8));
            if (!isHead) {
                out.write(bodyBytes);
            }
            out.flush();
            return false;
        }

        // Upgrade oficial para WebSocket (RFC 6455)
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] acceptHash = sha1.digest((wsKey + WS_MAGIC_GUID).getBytes(StandardCharsets.UTF_8));
            String acceptKey = Base64.getEncoder().encodeToString(acceptHash);

            String handshakeResponse = "HTTP/1.1 101 Switching Protocols\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

            out.write(handshakeResponse.getBytes(StandardCharsets.UTF_8));
            out.flush();
            return true;
        } catch (Exception e) {
            throw new IOException("Falha ao gerar chave Sec-WebSocket-Accept", e);
        }
    }

    /**
     * Lê uma linha HTTP byte a byte (terminada em \r\n ou \n) sem usar BufferedReader,
     * garantindo que nenhum byte do primeiro frame WebSocket seja consumido indevidamente.
     */
    private static String readHttpLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder(128);
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                int next = in.read();
                if (next == '\n' || next == -1) {
                    break;
                }
                sb.append((char) b).append((char) next);
            } else if (b == '\n') {
                break;
            } else {
                sb.append((char) b);
            }
            if (sb.length() > 8192) {
                throw new IOException("Cabeçalho HTTP excede limite de 8KB");
            }
        }
        if (b == -1 && sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    /**
     * Lê o próximo frame de texto WebSocket (desmascarando os bytes segundo a RFC 6455).
     * Retorna null se a conexão fechar ou receber um Close Frame (opcode 0x8).
     */
    public static String readTextFrame(InputStream in, OutputStream out) throws IOException {
        while (true) {
            int b1 = in.read();
            if (b1 == -1) return null;

            int opcode = b1 & 0x0F;
            if (opcode == 0x8) {
                // Close frame
                return null;
            }

            int b2 = in.read();
            if (b2 == -1) return null;

            boolean masked = (b2 & 0x80) != 0;
            long payloadLen = b2 & 0x7F;

            if (payloadLen == 126) {
                payloadLen = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
            } else if (payloadLen == 127) {
                payloadLen = 0;
                for (int i = 0; i < 8; i++) {
                    payloadLen = (payloadLen << 8) | (in.read() & 0xFF);
                }
            }

            if (payloadLen > 65536) {
                throw new IOException("Frame WebSocket excede tamanho máximo permitido (64KB)");
            }

            byte[] maskingKey = new byte[4];
            if (masked) {
                readFully(in, maskingKey);
            }

            byte[] payload = new byte[(int) payloadLen];
            readFully(in, payload);

            if (masked) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
                }
            }

            if (opcode == 0x9) {
                // Ping frame -> responde Pong (0xA)
                sendFrame(out, 0xA, payload);
                continue;
            }
            if (opcode == 0xA) {
                // Pong frame -> ignora e lê próximo
                continue;
            }
            if (opcode == 0x1) {
                return new String(payload, StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Envia um frame WebSocket de texto UTF-8 do servidor para o cliente (sem máscara, conforme RFC 6455).
     */
    public static void writeTextFrame(OutputStream out, String text) throws IOException {
        byte[] payload = text.getBytes(StandardCharsets.UTF_8);
        sendFrame(out, 0x1, payload);
    }

    /**
     * Envia frame WebSocket de fechamento (opcode 0x8).
     */
    public static void writeCloseFrame(OutputStream out) {
        try {
            sendFrame(out, 0x8, new byte[0]);
        } catch (IOException ignored) {
        }
    }

    private static synchronized void sendFrame(OutputStream out, int opcode, byte[] payload) throws IOException {
        int len = payload.length;
        out.write(0x80 | (opcode & 0x0F)); // FIN = 1

        if (len <= 125) {
            out.write(len);
        } else if (len <= 65535) {
            out.write(126);
            out.write((len >> 8) & 0xFF);
            out.write(len & 0xFF);
        } else {
            out.write(127);
            long longLen = len;
            for (int i = 7; i >= 0; i--) {
                out.write((int) ((longLen >> (8 * i)) & 0xFF));
            }
        }

        out.write(payload);
        out.flush();
    }

    private static void readFully(InputStream in, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int read = in.read(buffer, offset, buffer.length - offset);
            if (read == -1) {
                throw new EOFException("Fim inesperado do stream WebSocket");
            }
            offset += read;
        }
    }
}
