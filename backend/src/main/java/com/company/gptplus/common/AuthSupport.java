package com.company.gptplus.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class AuthSupport {
    private final String secret;

    public AuthSupport(@Value("${gpt-plus.security.token-secret}") String secret) {
        this.secret = secret;
    }

    public String issueToken(long id, String username, String role) {
        long exp = Instant.now().plusSeconds(7 * 24 * 3600).getEpochSecond();
        String payload = id + "|" + username + "|" + role + "|" + exp;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + sign(payload);
    }

    public CurrentUser requireUser(HttpServletRequest request) {
        CurrentUser user = parse(request);
        if (!"USER".equals(user.role())) {
            throw new BizException(10001, "未登录");
        }
        return user;
    }

    public CurrentUser requireAdmin(HttpServletRequest request) {
        CurrentUser user = parse(request);
        if (!"ADMIN".equals(user.role())) {
            throw new BizException(50001, "管理员权限不足");
        }
        return user;
    }

    private CurrentUser parse(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BizException(10001, "未登录");
        }
        String token = header.substring("Bearer ".length());
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new BizException(10002, "token 无效");
        }
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new BizException(10002, "token 无效");
        }
        if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
            throw new BizException(10002, "token 无效");
        }
        String[] fields = payload.split("\\|");
        if (fields.length != 4) {
            throw new BizException(10002, "token 无效");
        }
        try {
            if (Long.parseLong(fields[3]) < Instant.now().getEpochSecond()) {
                throw new BizException(10002, "token 无效");
            }
            return new CurrentUser(Long.parseLong(fields[0]), fields[1], fields[2]);
        } catch (NumberFormatException ex) {
            throw new BizException(10002, "token 无效");
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public record CurrentUser(long id, String username, String role) {
    }
}
