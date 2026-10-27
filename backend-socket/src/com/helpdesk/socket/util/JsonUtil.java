package com.helpdesk.socket.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

/**
 * Utilitário de serialização e deserialização JSON usando Gson (lib/gson.jar).
 * Monta os envelopes padrão dos contratos WebSocket (CT18, CT19 e CT22).
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder().create();

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static JsonObject parseObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    /**
     * Monta o envelope padrão exigido pelos contratos WebSocket:
     * {"type": "<type>", "data": { ... }}
     */
    public static String buildEnvelope(String type, Object data) {
        return gson.toJson(Map.of(
                "type", type,
                "data", data
        ));
    }

    /**
     * Monta o envelope de erro padrão do CT19:
     * {"type": "erro", "data": {"code": "<CODE>"}}
     */
    public static String buildError(String code) {
        return buildEnvelope("erro", Map.of("code", code));
    }
}
