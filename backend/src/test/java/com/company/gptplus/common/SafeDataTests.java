package com.company.gptplus.common;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SafeDataTests {
    @Test
    void masksSensitiveAdminFields() {
        Map<String, Object> masked = SafeData.maskRow(Map.of(
                "resource_account", "demo@example.com",
                "resource_password_cipher", "ENC:test",
                "callback_raw", "{secret}",
                "code_value", "123456",
                "cdk_code", "ABCDEF-123456-XYZ"
        ));

        assertThat(masked.get("resource_account")).isEqualTo("d***@example.com");
        assertThat(masked.get("resource_password_cipher")).isEqualTo("******");
        assertThat(masked.get("callback_raw")).isEqualTo("[已隐藏]");
        assertThat(masked.get("code_value")).isEqualTo("****56");
        assertThat(masked.get("cdk_code")).isEqualTo("ABCDEF***-XYZ");
    }
}
