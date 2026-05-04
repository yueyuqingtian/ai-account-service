package com.company.gptplus.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoServiceTests {
    @Test
    void encryptAndDecryptInventoryPassword() {
        CryptoService cryptoService = new CryptoService("0123456789abcdef");
        String encrypted = cryptoService.encrypt("secret-value");
        assertThat(encrypted).startsWith("ENC:");
        assertThat(cryptoService.decrypt(encrypted)).isEqualTo("secret-value");
    }
}
