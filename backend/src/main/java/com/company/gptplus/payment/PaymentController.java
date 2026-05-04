package com.company.gptplus.payment;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.BizException;
import com.company.gptplus.common.Ids;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class PaymentController {
    private final JdbcTemplate jdbcTemplate;
    private final AuthSupport authSupport;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentSettlementService paymentSettlementService;

    public PaymentController(JdbcTemplate jdbcTemplate,
                             AuthSupport authSupport,
                             PaymentGatewayService paymentGatewayService,
                             PaymentSettlementService paymentSettlementService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSupport = authSupport;
        this.paymentGatewayService = paymentGatewayService;
        this.paymentSettlementService = paymentSettlementService;
    }

    @PostMapping("/api/payments/create")
    @Transactional
    public ApiResponse<?> create(HttpServletRequest servletRequest, @Valid @RequestBody CreatePaymentRequest request) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        Map<String, Object> order = jdbcTemplate.queryForMap("select * from order_info where order_no = ? and user_id = ?", request.orderNo(), user.id());
        if (!"UNPAID".equals(String.valueOf(order.get("pay_status")))) {
            throw new BizException(30003, "订单无需重复支付");
        }
        if (!"UNPAID".equals(String.valueOf(order.get("order_status")))) {
            throw new BizException(30007, "订单已关闭，无法支付");
        }
        String channel = request.channel().toUpperCase();
        if (!List.of("MOCK", "ALIPAY", "WECHAT").contains(channel)) {
            throw new BizException(30004, "暂不支持的支付渠道");
        }
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("""
                select * from payment_record where order_no = ? and channel = ? and status = 'INIT' order by id desc limit 1
                """, request.orderNo(), channel);
        if (!existing.isEmpty()) {
            Map<String, Object> existed = existing.get(0);
            PaymentGatewayService.PaymentCreateResult existedPay = paymentGatewayService.create(channel, String.valueOf(existed.get("payment_no")), request.orderNo(), (BigDecimal) existed.get("amount"));
            return ApiResponse.ok(Map.of(
                    "paymentNo", existedPay.paymentNo(),
                    "payUrl", existedPay.payUrl(),
                    "payType", existedPay.payType(),
                    "channel", existedPay.channel(),
                    "gatewayPayload", existedPay.gatewayPayload(),
                    "reused", true
            ));
        }
        String paymentNo = Ids.paymentNo();
        jdbcTemplate.update("""
                insert into payment_record(payment_no, order_no, channel, amount, status)
                values (?, ?, ?, ?, 'INIT')
                """, paymentNo, request.orderNo(), channel, order.get("pay_amount"));
        PaymentGatewayService.PaymentCreateResult pay = paymentGatewayService.create(channel, paymentNo, request.orderNo(), (BigDecimal) order.get("pay_amount"));
        return ApiResponse.ok(Map.of(
                "paymentNo", pay.paymentNo(),
                "payUrl", pay.payUrl(),
                "payType", pay.payType(),
                "channel", pay.channel(),
                "gatewayPayload", pay.gatewayPayload(),
                "reused", false
        ));
    }

    @GetMapping("/api/payments/status/{orderNo}")
    public ApiResponse<?> status(HttpServletRequest servletRequest, @PathVariable String orderNo) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        Map<String, Object> order = jdbcTemplate.queryForMap("select * from order_info where order_no = ? and user_id = ?", orderNo, user.id());
        List<Map<String, Object>> payments = jdbcTemplate.queryForList("select * from payment_record where order_no = ? order by id desc", orderNo);
        return ApiResponse.ok(Map.of("order", order, "payments", payments));
    }

    @PostMapping("/api/payments/mock-success")
    public ApiResponse<?> mockSuccess(HttpServletRequest servletRequest, @RequestBody MockPayRequest request) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select p.* from payment_record p
                join order_info o on o.order_no = p.order_no
                where p.payment_no = ? and p.channel = 'MOCK' and o.user_id = ?
                """, request.paymentNo(), user.id());
        if (rows.isEmpty()) {
            throw new BizException(30006, "支付单无效");
        }
        return ApiResponse.ok(paymentSettlementService.markPaymentPaid(request.paymentNo(), "MOCK_TRADE_" + request.paymentNo(), "mock success callback"));
    }

    @PostMapping("/callback/payments/{channel}")
    public ApiResponse<?> callback(@PathVariable String channel, @RequestBody Map<String, Object> raw) {
        String paymentNo = String.valueOf(raw.getOrDefault("paymentNo", ""));
        String tradeNo = String.valueOf(raw.getOrDefault("tradeNo", channel.toUpperCase() + "_" + paymentNo));
        return ApiResponse.ok(paymentSettlementService.markPaymentPaid(paymentNo, tradeNo, raw.toString()));
    }

    public record CreatePaymentRequest(@NotBlank String orderNo, @NotBlank String channel) {
    }

    public record MockPayRequest(@NotBlank String paymentNo) {
    }
}
