package com.company.gptplus.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class Ids {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private Ids() {
    }

    public static String orderNo() {
        return "O" + LocalDateTime.now().format(FORMATTER) + shortId();
    }

    public static String paymentNo() {
        return "P" + LocalDateTime.now().format(FORMATTER) + shortId();
    }

    public static String redeemNo() {
        return "R" + LocalDateTime.now().format(FORMATTER) + shortId();
    }

    public static String batchNo() {
        return "B" + LocalDateTime.now().format(FORMATTER) + shortId();
    }

    public static String cdk() {
        String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return raw.substring(0, 4) + "-" + raw.substring(4, 8) + "-" + raw.substring(8, 12) + "-" + raw.substring(12, 16);
    }

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
