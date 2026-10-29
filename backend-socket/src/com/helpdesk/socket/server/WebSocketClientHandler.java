package com.helpdesk.socket.server;

import com.google.gson.JsonObject;
import com.helpdesk.socket.dao.UserDao;
import com.helpdesk.socket.model.SocketMessage;
import com.helpdesk.socket.model.TicketEventPayload;
import com.helpdesk.socket.model.UserSessionInfo;
import com.helpdesk.socket.util.AuthUtil;
import com.helpdesk.socket.util.JsonUtil;
import com.helpdesk.socket.util.WebSocketUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * Supervisora de conexão WebSocket (Porta pública PORT / 5000).
 * Atende 100% via protocolo WebSocket (RFC 6455):
 * - CT19 (autenticarConexao): autentica sessões do Frontend consultando o Neon DB e mantém o WebSocket ativo para o CT22.
 * - CT18 (publicarEvento): recebe eventos da Backend API via WebSocket, valida INTERNAL_SECRET e dispara o CT22.
 */
public class WebSocketClientHandler implements Runnable {

    private static final int AUTH_TIMEOUT_MS = 15_000;
    private static final String INTERNAL_SECRET = System.getenv().getOrDefault(
            "INTERNAL_SECRET", "segredo-interno-entre-backends");

    private final Socket socket;
    private final UserDao userDao;

    public WebSocketClientHandler(Socket socket, UserDao userDao) {
        this.socket = socket;
        this.userDao = userDao;
    }

    @Override
    public void run() {
        ClientConnection clientConnection = null;
        try {
            socket.setSoTimeout(AUTH_TIMEOUT_MS);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // 1. Handshake HTTP -> WebSocket (ou resposta 200 OK para Health Check do Render)
            boolean isWebSocket = WebSocketUtil.performHandshake(in, out);
            if (!isWebSocket) {
                socket.close();
                return;
            }

            // 2. Lê o primeiro frame WebSocket enviado pelo cliente (Frontend ou Backend API)
            String firstFrame = WebSocketUtil.readTextFrame(in, out);
            if (firstFrame == null) {
                socket.close();
                return;
            }

            SocketMessage firstMsg;
            try {
                firstMsg = JsonUtil.fromJson(firstFrame, SocketMessage.class);
            } catch (Exception e) {
                sendErrorAndClose(out, "NAO_AUTENTICADO");
                return;
            }

            if (firstMsg == null || firstMsg.getType() == null) {
                sendErrorAndClose(out, "NAO_AUTENTICADO");
                return;
            }

            // Caso A: A Backend API conectou via WebSocket para publicar um evento (CT18: publicarEvento ou NOTIFICATION_PING)
            if ("publicarEvento".equals(firstMsg.getType()) || "NOTIFICATION_PING".equals(firstMsg.getType())) {
                handleCt18OverWebSocket(firstMsg, out);
                return;
            }

            // Caso B: O Frontend conectou via WebSocket — a 1ª mensagem DEVE ser autenticarConexao (CT19)
            if (!"autenticarConexao".equals(firstMsg.getType())) {
                sendErrorAndClose(out, "NAO_AUTENTICADO");
                return;
            }

            JsonObject data = firstMsg.getData();
            String token = (data != null && data.has("token") && !data.get("token").isJsonNull())
                    ? data.get("token").getAsString()
                    : null;

            Integer userId = AuthUtil.validateToken(token);
            if (userId == null) {
                sendErrorAndClose(out, "TOKEN_INVALIDO");
                return;
            }

            // 3. Consulta o usuário na tabela 'users' do Neon.tech (CT13)
            UserSessionInfo user = userDao.findSessionInfoById(userId);
            if (user == null) {
                sendErrorAndClose(out, "TOKEN_INVALIDO");
                return;
            }

            if (!user.isActive()) {
                sendErrorAndClose(out, "USUARIO_INATIVO");
                return;
            }

            // 4. Autenticação aprovada! Remove timeout curto de leitura e registra a sessão WebSocket no ServerState
            socket.setSoTimeout(0);
            clientConnection = new ClientConnection(socket, out, user.getId(), user.getRole());
            clientConnection.startWriter();
            ServerState.getInstance().registerSession(clientConnection);

            // 5. Envia resposta oficial do CT19 via WebSocket: conexaoAutenticada
            String authSuccessJson = JsonUtil.buildEnvelope("conexaoAutenticada", Map.of(
                    "userId", user.getId(),
                    "role", user.getRole()
            ));
            clientConnection.enqueueMessage(authSuccessJson);

            // 6. Loop de supervisão da sessão WebSocket (mantém viva, responde a pings e detecta fechamento)
            while (!clientConnection.isClosed()) {
                String frame = WebSocketUtil.readTextFrame(in, out);
                if (frame == null) {
                    break; // Navegador fechou a aba ou desconectou o WebSocket
                }
            }
        } catch (IOException e) {
            // Conexão WebSocket encerrada ou timeout de autenticação
        } finally {
            if (clientConnection != null) {
                ServerState.getInstance().removeSession(clientConnection);
            } else {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Processa o contrato CT18 (publicarEvento) recebido via WebSocket a partir da Backend API.
     */
    private void handleCt18OverWebSocket(SocketMessage message, OutputStream out) throws IOException {
        try {
            if (message.getData() == null) {
                WebSocketUtil.writeTextFrame(out, JsonUtil.toJson(Map.of("received", false)));
                WebSocketUtil.writeCloseFrame(out);
                return;
            }

            TicketEventPayload payload;
            try {
                payload = JsonUtil.fromJson(message.getData().toString(), TicketEventPayload.class);
            } catch (Exception e) {
                WebSocketUtil.writeTextFrame(out, JsonUtil.toJson(Map.of("received", false)));
                WebSocketUtil.writeCloseFrame(out);
                return;
            }

            // Regra 1 do CT18: secret errado ou ausente -> fecha a conexão imediatamente SEM responder
            if (payload == null || payload.getSecret() == null || !isSecretValid(payload.getSecret())) {
                System.err.println("[CT18 - WebSocket] Evento rejeitado: INTERNAL_SECRET ausente ou invalido.");
                return;
            }

            // Suporte à ADR 0002 (NOTIFICATION_PING)
            if ("NOTIFICATION_PING".equals(message.getType())) {
                if (message.getData().has("userId")) {
                    int targetUserId = message.getData().get("userId").getAsInt();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rawData = JsonUtil.fromJson(message.getData().toString(), Map.class);
                    rawData.remove("secret");
                    ServerState.getInstance().sendNotificationPing(targetUserId, rawData);
                    WebSocketUtil.writeTextFrame(out, JsonUtil.toJson(Map.of("received", true)));
                } else {
                    WebSocketUtil.writeTextFrame(out, JsonUtil.toJson(Map.of("received", false)));
                }
                WebSocketUtil.writeCloseFrame(out);
                return;
            }

            // Regra 2 do CT18: Validar campos obrigatórios
            if (!payload.hasRequiredFields()) {
                WebSocketUtil.writeTextFrame(out, JsonUtil.toJson(Map.of("received", false)));
                WebSocketUtil.writeCloseFrame(out);
                return;
            }

            // Regra 3 do CT18: Enfileira o broadcast do CT22 (chamadoAtualizado) e responde {"received": true}
            boolean queued = ServerState.getInstance().publishTicketEventAsync(payload);
            WebSocketUtil.writeTextFrame(out, JsonUtil.toJson(Map.of("received", queued)));
            WebSocketUtil.writeCloseFrame(out);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean isSecretValid(String receivedSecret) {
        return MessageDigest.isEqual(
                receivedSecret.getBytes(StandardCharsets.UTF_8),
                INTERNAL_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private void sendErrorAndClose(OutputStream out, String errorCode) {
        try {
            String errorJson = JsonUtil.buildError(errorCode);
            WebSocketUtil.writeTextFrame(out, errorJson);
            WebSocketUtil.writeCloseFrame(out);
        } catch (IOException ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
