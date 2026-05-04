package com.company.gptplus.payment;

import com.company.gptplus.common.BizException;
import com.company.gptplus.common.Ids;
import com.company.gptplus.inventory.InventoryReservationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PaymentSettlementService {
    private final JdbcTemplate jdbcTemplate;
    private final InventoryReservationService inventoryReservationService;

    public PaymentSettlementService(JdbcTemplate jdbcTemplate, InventoryReservationService inventoryReservationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.inventoryReservationService = inventoryReservationService;
    }

    @Transactional
    public Map<String, Object> markPaymentPaid(String paymentNo, String tradeNo, String callbackRaw) {
        List<Map<String, Object>> payments = jdbcTemplate.queryForList("select * from payment_record where payment_no = ?", paymentNo);
        if (payments.isEmpty()) {
            throw new BizException(30001, "支付单不存在");
        }
        Map<String, Object> payment = payments.get(0);
        String orderNo = String.valueOf(payment.get("order_no"));
        Map<String, Object> order = jdbcTemplate.queryForMap("select * from order_info where order_no = ?", orderNo);
        if ("SUCCESS".equals(String.valueOf(payment.get("status")))) {
            return Map.of("paymentNo", paymentNo, "orderNo", orderNo, "status", "SUCCESS", "idempotent", true);
        }
        ensurePayable(order);
        BigDecimal payAmount = (BigDecimal) order.get("pay_amount");
        BigDecimal paymentAmount = (BigDecimal) payment.get("amount");
        if (payAmount.compareTo(paymentAmount) != 0) {
            throw new BizException(30005, "支付金额不匹配");
        }
        ensureInventoryReserved(order);
        int paid = jdbcTemplate.update("""
                update payment_record set status='SUCCESS', third_party_trade_no=?, callback_raw=?, paid_at=current_timestamp, updated_at=current_timestamp
                where payment_no=? and status='INIT'
                """, tradeNo, callbackRaw, paymentNo);
        if (paid == 0) {
            return Map.of("paymentNo", paymentNo, "orderNo", orderNo, "status", "SUCCESS", "idempotent", true);
        }
        settleOrder(order, paymentNo);
        return Map.of("paymentNo", paymentNo, "orderNo", orderNo, "status", "SUCCESS", "idempotent", false);
    }

    @Transactional
    public Map<String, Object> markOrderPaidManually(String orderNo, long adminId) {
        Map<String, Object> order = jdbcTemplate.queryForMap("select * from order_info where order_no = ?", orderNo);
        if ("PAID".equals(String.valueOf(order.get("pay_status")))) {
            return Map.of("orderNo", orderNo, "status", "SUCCESS", "idempotent", true);
        }
        ensurePayable(order);
        ensureInventoryReserved(order);
        String paymentNo = Ids.paymentNo();
        jdbcTemplate.update("""
                insert into payment_record(payment_no, order_no, channel, amount, status, third_party_trade_no, callback_raw, paid_at)
                values (?, ?, 'MANUAL', ?, 'SUCCESS', ?, ?, current_timestamp)
                """, paymentNo, orderNo, order.get("pay_amount"), "MANUAL_" + orderNo, "admin manual confirm:" + adminId);
        settleOrder(order, paymentNo);
        return Map.of("paymentNo", paymentNo, "orderNo", orderNo, "status", "SUCCESS", "idempotent", false);
    }

    private void settleOrder(Map<String, Object> order, String paymentNo) {
        String orderNo = String.valueOf(order.get("order_no"));
        int settled = jdbcTemplate.update("""
                update order_info set order_status='PAID', pay_status='PAID', delivery_status='PENDING', paid_at=current_timestamp, updated_at=current_timestamp
                where order_no=? and pay_status='UNPAID' and order_status='UNPAID'
                """, orderNo);
        if (settled == 0) {
            Map<String, Object> latest = jdbcTemplate.queryForMap("select * from order_info where order_no = ?", orderNo);
            if ("PAID".equals(String.valueOf(latest.get("pay_status")))) {
                return;
            }
            throw new BizException(30007, "订单已关闭，无法完成支付");
        }
        Integer quantity = ((Number) order.get("quantity")).intValue();
        Integer cdkExists = jdbcTemplate.queryForObject("select count(*) from cdk_record where order_no = ?", Integer.class, orderNo);
        int missing = quantity - (cdkExists == null ? 0 : cdkExists);
        for (int i = 0; i < missing; i++) {
            jdbcTemplate.update("""
                    insert into cdk_record(cdk_code, product_id, order_id, order_no, owner_user_id, status, delivery_snapshot)
                    values (?, ?, ?, ?, ?, 'BOUND', ?)
                    """, Ids.cdk(), order.get("product_id"), order.get("id"), orderNo, order.get("user_id"),
                    "{\"source\":\"payment\",\"paymentNo\":\"" + paymentNo + "\"}");
        }
    }

    private void ensureInventoryReserved(Map<String, Object> order) {
        inventoryReservationService.ensureReserved(
                ((Number) order.get("product_id")).longValue(),
                String.valueOf(order.get("order_no")),
                ((Number) order.get("user_id")).longValue(),
                ((Number) order.get("quantity")).intValue()
        );
    }

    private void ensurePayable(Map<String, Object> order) {
        if (!"UNPAID".equals(String.valueOf(order.get("pay_status")))) {
            return;
        }
        if (!"UNPAID".equals(String.valueOf(order.get("order_status")))) {
            throw new BizException(30007, "订单已关闭，无法完成支付");
        }
    }
}
