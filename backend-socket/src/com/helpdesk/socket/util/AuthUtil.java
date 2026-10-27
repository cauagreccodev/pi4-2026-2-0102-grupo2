package com.helpdesk.socket.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Validação de token HMAC-SHA256 100% compatível com o AuthUtil do backend-api.
 * Formato do token gerado pela API (CT1): base64Url(userId:expirationTimestamp).signature
 */
public class AuthUtil {

    private static final String SECRET = System.getenv().getOrDefault(
            "JWT_SECRET", "minha-chave-secreta-super-segura-hmac-256");

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular HMAC-SHA256", e);
        }
    }

    /**
     * Gera um token assinado com HMAC-SHA256 (mesmo formato do backend-api, útil para testes).
     */
    public static String generateToken(int userId) {
        long expirationTime = System.currentTimeMillis() + (24L * 60 * 60 * 1000); // 24h
        String payload = userId + ":" + expirationTime;
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = hmacSha256(payloadBase64, SECRET);
        return payloadBase64 + "." + signature;
    }

    /**
     * Confere assinatura HMAC e validade temporal do token (CT19).
     * Retorna o userId (Integer) se válido, ou null se ausente, adulterado ou expirado.
     */
    public static Integer validateToken(String token) {
        if (token == null || token.isBlank() || !token.contains(".")) {
            return null;
        }

        // Remove eventual prefixo "Bearer " caso o frontend envie
        String cleanToken = token.trim();
        if (cleanToken.startsWith("Bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
        }

        try {
            String[] parts = cleanToken.split("\\.");
            if (parts.length != 2) return null;

            String payloadBase64 = parts[0];
            String signature = parts[1];

            // 1. Confere se a assinatura bate com o JWT_SECRET (protege contra adulteração)
            String expectedSignature = hmacSha256(payloadBase64, SECRET);
            if (!MessageDigest.isEqual(
                    signature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return null;
            }

            // 2. Decodifica o payload (userId:expirationTimestamp)
            String payload = new String(
                    Base64.getUrlDecoder().decode(payloadBase64), StandardCharsets.UTF_8);
            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 2) return null;

            int userId = Integer.parseInt(payloadParts[0]);
            long expirationTime = Long.parseLong(payloadParts[1]);

            // 3. Verifica se o token já venceu
            if (System.currentTimeMillis() > expirationTime) {
                return null;
            }

            return userId;
        } catch (Exception e) {
            return null;
        }
    }
}
