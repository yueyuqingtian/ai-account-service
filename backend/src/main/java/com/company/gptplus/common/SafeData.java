package com.company.gptplus.common;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SafeData {
    private SafeData() {
    }

    public static Map<String, Object> maskRow(Map<String, Object> row) {
        Map<String, Object> safe = new LinkedHashMap<>();
        row.forEach((key, value) -> safe.put(key, maskValue(key, value)));
        return safe;
    }

    public static Object maskValue(String key, Object value) {
        if (value == null) {
            return null;
        }
        String normalized = key.toLowerCase();
        if (normalized.contains("password") || normalized.contains("cipher") || normalized.contains("secret")
                || normalized.contains("private_key") || normalized.contains("api_v3_key") || normalized.contains("token")) {
            return "******";
        }
        if (normalized.contains("callback_raw") || normalized.contains("delivery_snapshot")) {
            return "[已隐藏]";
        }
        if (normalized.equals("code_value")) {
            return maskCode(String.valueOf(value));
        }
        if (normalized.contains("email") || normalized.contains("account") || normalized.contains("mailbox")) {
            return maskEmail(String.valueOf(value));
        }
        if (normalized.equals("cdk_code")) {
            return maskMiddle(String.valueOf(value), 6, 4);
        }
        return value;
    }

    public static String maskEmail(String value) {
        int at = value.indexOf('@');
        if (at <= 1) {
            return maskMiddle(value, 1, 0);
        }
        String name = value.substring(0, at);
        String domain = value.substring(at);
        return name.charAt(0) + "***" + domain;
    }

    public static String maskCode(String value) {
        if (value.length() <= 2) {
            return "**";
        }
        return "*".repeat(value.length() - 2) + value.substring(value.length() - 2);
    }

    private static String maskMiddle(String value, int prefix, int suffix) {
        if (value.length() <= prefix + suffix) {
            return "******";
        }
        return value.substring(0, prefix) + "***" + (suffix == 0 ? "" : value.substring(value.length() - suffix));
    }
}
