package com.helpdesk.socket.support;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Monta tokens de teste no mesmo formato que a API gera:
 * base64(userId:validade).assinatura
 */
public class Tokens {

    public static final String SEGREDO_JWT = System.getenv().getOrDefault(
            "JWT_SECRET", "minha-chave-secreta-super-segura-hmac-256");

    public static String comValidade(int userId, long validadeEmMs) {
        String conteudo = userId + ":" + validadeEmMs;
        String conteudoBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(conteudo.getBytes(StandardCharsets.UTF_8));
        return conteudoBase64 + "." + assinar(conteudoBase64);
    }

    public static String vencido(int userId) {
        return comValidade(userId, System.currentTimeMillis() - 60_000);
    }

    /** Token com o conteudo de um usuario e a assinatura de outro, ou seja, adulterado. */
    public static String adulterado(int userIdOriginal, int userIdTrocado) {
        String original = comValidade(userIdOriginal, System.currentTimeMillis() + 3_600_000);
        String conteudoTrocado = Base64.getUrlEncoder().withoutPadding().encodeToString(
                (userIdTrocado + ":" + (System.currentTimeMillis() + 3_600_000)).getBytes(StandardCharsets.UTF_8));
        String assinaturaOriginal = original.substring(original.indexOf('.') + 1);
        return conteudoTrocado + "." + assinaturaOriginal;
    }

    private static String assinar(String texto) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SEGREDO_JWT.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
