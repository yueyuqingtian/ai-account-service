package com.company.gptplus.admin;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.SystemConfigService;
import com.company.gptplus.payment.PaymentGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/payment-config")
public class AdminPaymentConfigController {
    private final AuthSupport authSupport;
    private final PaymentGatewayService paymentGatewayService;
    private final SystemConfigService systemConfigService;

    public AdminPaymentConfigController(AuthSupport authSupport, PaymentGatewayService paymentGatewayService, SystemConfigService systemConfigService) {
        this.authSupport = authSupport;
        this.paymentGatewayService = paymentGatewayService;
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    public ApiResponse<?> config(HttpServletRequest request) {
        authSupport.requireAdmin(request);
        return ApiResponse.ok(paymentGatewayService.readiness());
    }

    @PostMapping
    public ApiResponse<?> save(HttpServletRequest request, @RequestBody PaymentConfigRequest body) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(request);
        putPlain("payment.alipay.app_id", body.alipayAppId());
        putPlain("payment.alipay.merchant_id", body.alipayMerchantId());
        putPlain("payment.alipay.gateway", body.alipayGateway());
        putPlain("payment.alipay.qr_url", body.alipayQrUrl());
        putSecret("payment.alipay.private_key", body.alipayPrivateKey());

        putPlain("payment.wechat.app_id", body.wechatAppId());
        putPlain("payment.wechat.mch_id", body.wechatMchId());
        putPlain("payment.wechat.notify_url", body.wechatNotifyUrl());
        putPlain("payment.wechat.qr_url", body.wechatQrUrl());
        putSecret("payment.wechat.api_v3_key", body.wechatApiV3Key());
        return ApiResponse.ok(Map.of("updatedBy", admin.username(), "config", paymentGatewayService.readiness()));
    }

    private void putPlain(String key, String value) {
        if (value != null) {
            systemConfigService.putPlain(key, value, key);
        }
    }

    private void putSecret(String key, String value) {
        if (value != null && !value.isBlank()) {
            systemConfigService.putSecret(key, value, key);
        }
    }

    public record PaymentConfigRequest(
            String alipayAppId,
            String alipayMerchantId,
            String alipayPrivateKey,
            String alipayGateway,
            String alipayQrUrl,
            String wechatAppId,
            String wechatMchId,
            String wechatApiV3Key,
            String wechatNotifyUrl,
            String wechatQrUrl
    ) {
    }
}
