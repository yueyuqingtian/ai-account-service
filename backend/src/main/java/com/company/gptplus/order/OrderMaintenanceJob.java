package com.company.gptplus.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderMaintenanceJob {
    private final JdbcTemplate jdbcTemplate;
    private final int unpaidTimeoutMinutes;

    public OrderMaintenanceJob(JdbcTemplate jdbcTemplate,
                               @Value("${gpt-plus.order.unpaid-timeout-minutes}") int unpaidTimeoutMinutes) {
        this.jdbcTemplate = jdbcTemplate;
        this.unpaidTimeoutMinutes = unpaidTimeoutMinutes;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeExpiredUnpaidOrders() {
        Timestamp threshold = Timestamp.valueOf(LocalDateTime.now().minusMinutes(unpaidTimeoutMinutes));
        List<String> expiredOrderNos = jdbcTemplate.queryForList("""
                select order_no from order_info
                where order_status='UNPAID' and pay_status='UNPAID' and created_at < ?
                """, String.class, threshold);
        if (expiredOrderNos.isEmpty()) {
            return;
        }
        jdbcTemplate.update("""
                update order_info set order_status='CLOSED', closed_reason='超时未支付自动关闭', updated_at=current_timestamp
                where order_status='UNPAID' and pay_status='UNPAID' and created_at < ?
                """, threshold);
        for (String orderNo : expiredOrderNos) {
            jdbcTemplate.update("""
                    update inventory_account
                    set status='AVAILABLE', assigned_order_no=null, assigned_user_id=null, assigned_at=null, updated_at=current_timestamp
                    where assigned_order_no=? and status='RESERVED'
                    """, orderNo);
        }
    }
}
