package com.helpdesk.socket.model;

import com.google.gson.JsonObject;

/**
 * Envelope padrão de mensagens WebSocket (equivalente à classe Comunicado):
 * {"type": "<operação>", "data": { ... }}
 */
public class SocketMessage {

    private String type;
    private JsonObject data;

    public SocketMessage() {
    }

    public SocketMessage(String type, JsonObject data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
