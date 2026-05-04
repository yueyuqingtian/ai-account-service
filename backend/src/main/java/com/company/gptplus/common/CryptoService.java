package com.company.gptplus.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class CryptoService {
    private final SecretKeySpec keySpec;

    public CryptoService(@Value("${gpt-plus.security.inventory-crypto-key}") String key) {
        String normalized = (key == null ? "" : key).trim();
        if (normalized.length() < 16) {
            normalized = (normalized + "0000000000000000").substring(0, 16);
        }
        if (normalized.length() > 16) {
            normalized = normalized.substring(0, 16);
        }
        this.keySpec = new SecretKeySpec(normalized.getBytes(StandardCharsets.UTF_8), "AES");
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return "ENC:" + Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new BizException(90002, "敏感信息加密失败");
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || !cipherText.startsWith("ENC:")) {
            return cipherText;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText.substring(4));
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BizException(90003, "敏感信息解密失败");
        }
    }
}
