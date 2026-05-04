package com.company.gptplus.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.company.gptplus.common.SystemConfigService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PaymentGatewayService {
    private final boolean mockEnabled;
    private final String alipayAppId;
    private final String alipayGateway;
    private final String wechatMchId;
    private final String wechatAppId;
    private final String wechatNotifyUrl;
    private final SystemConfigService systemConfigService;

    public PaymentGatewayService(
            @Value("${gpt-plus.payment.mock-enabled:true}") boolean mockEnabled,
            @Value("${gpt-plus.payment.alipay.app-id:}") String alipayAppId,
            @Value("${gpt-plus.payment.alipay.gateway:}") String alipayGateway,
            @Value("${gpt-plus.payment.wechat.mch-id:}") String wechatMchId,
            @Value("${gpt-plus.payment.wechat.app-id:}") String wechatAppId,
            @Value("${gpt-plus.payment.wechat.notify-url:}") String wechatNotifyUrl,
            SystemConfigService systemConfigService) {
        this.mockEnabled = mockEnabled;
        this.alipayAppId = alipayAppId;
        this.alipayGateway = alipayGateway;
        this.wechatMchId = wechatMchId;
        this.wechatAppId = wechatAppId;
        this.wechatNotifyUrl = wechatNotifyUrl;
        this.systemConfigService = systemConfigService;
    }

    public PaymentCreateResult create(String channel, String paymentNo, String orderNo, BigDecimal amount) {
        String normalized = channel.toUpperCase();
        return switch (normalized) {
            case "MOCK" -> new PaymentCreateResult(paymentNo, normalized, "MOCK_QR", "/pay/result?paymentNo=" + paymentNo + "&channel=MOCK", Map.of("mockEnabled", mockEnabled));
            case "ALIPAY" -> new PaymentCreateResult(paymentNo, normalized, "QR_CODE", first("payment.alipay.qr_url", alipayGateway + "?out_trade_no=" + paymentNo), Map.of(
                    "configured", isAlipayConfigured(),
                    "appId", mask(alipayAppId()),
                    "merchantId", mask(first("payment.alipay.merchant_id", "")),
                    "qrUrl", first("payment.alipay.qr_url", ""),
                    "orderNo", orderNo,
                    "amount", amount
            ));
            case "WECHAT" -> new PaymentCreateResult(paymentNo, normalized, "QR_CODE", first("payment.wechat.qr_url", "weixin://wxpay/bizpayurl?pr=" + paymentNo), Map.of(
                    "configured", isWechatConfigured(),
                    "appId", mask(wechatAppId()),
                    "mchId", mask(wechatMchId()),
                    "notifyUrl", wechatNotifyUrl(),
                    "qrUrl", first("payment.wechat.qr_url", ""),
                    "orderNo", orderNo,
                    "amount", amount
            ));
            default -> throw new IllegalArgumentException("unsupported payment channel");
        };
    }

    public Map<String, Object> readiness() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mock", Map.of("enabled", mockEnabled, "ready", true));
        result.put("alipay", Map.of(
                "ready", isAlipayConfigured(),
                "appId", mask(alipayAppId()),
                "merchantId", mask(first("payment.alipay.merchant_id", "")),
                "gateway", first("payment.alipay.gateway", alipayGateway),
                "qrUrl", first("payment.alipay.qr_url", ""),
                "qrEnabled", !first("payment.alipay.qr_url", "").isBlank(),
                "hasPrivateKey", systemConfigService.getSecret("payment.alipay.private_key").filter(v -> !v.isBlank()).isPresent()
        ));
        result.put("wechat", Map.of(
                "ready", isWechatConfigured(),
                "appId", mask(wechatAppId()),
                "mchId", mask(wechatMchId()),
                "notifyUrl", wechatNotifyUrl(),
                "qrUrl", first("payment.wechat.qr_url", ""),
                "qrEnabled", !first("payment.wechat.qr_url", "").isBlank(),
                "hasApiKey", systemConfigService.getSecret("payment.wechat.api_v3_key").filter(v -> !v.isBlank()).isPresent()
        ));
        return result;
    }

    private boolean isAlipayConfigured() {
        return !alipayAppId().isBlank() || !first("payment.alipay.qr_url", "").isBlank();
    }

    private boolean isWechatConfigured() {
        return (!wechatMchId().isBlank() && !wechatAppId().isBlank()) || !first("payment.wechat.qr_url", "").isBlank();
    }

    private String alipayAppId() {
        return first("payment.alipay.app_id", alipayAppId);
    }

    private String wechatMchId() {
        return first("payment.wechat.mch_id", wechatMchId);
    }

    private String wechatAppId() {
        return first("payment.wechat.app_id", wechatAppId);
    }

    private String wechatNotifyUrl() {
        return first("payment.wechat.notify_url", wechatNotifyUrl);
    }

    private String first(String key, String fallback) {
        return systemConfigService.getPlain(key).filter(v -> !v.isBlank()).orElse(fallback == null ? "" : fallback);
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= 6) {
            return "***";
        }
        return value.substring(0, 3) + "***" + value.substring(value.length() - 3);
    }

    public record PaymentCreateResult(String paymentNo, String channel, String payType, String payUrl, Map<String, Object> gatewayPayload) {
    }
}
