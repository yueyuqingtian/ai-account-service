package com.company.gptplus.admin;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.PageResponse;
import com.company.gptplus.common.SafeData;
import com.company.gptplus.payment.PaymentSettlementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin")
public class AdminQueryController {
    private static final Set<String> PAGE_TABLES = Set.of("order_info", "payment_record", "cdk_record", "redeem_record");

    private final JdbcTemplate jdbcTemplate;
    private final AuthSupport authSupport;
    private final PaymentSettlementService paymentSettlementService;

    public AdminQueryController(JdbcTemplate jdbcTemplate, AuthSupport authSupport, PaymentSettlementService paymentSettlementService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSupport = authSupport;
        this.paymentSettlementService = paymentSettlementService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard(HttpServletRequest servletRequest) {
        authSupport.requireAdmin(servletRequest);
        BigDecimal totalRevenue = sumPaidAmount("");
        BigDecimal todayRevenue = sumPaidAmount("and paid_at >= current_date");
        Long orders = count("order_info");
        Long paidOrders = countWhere("order_info", "pay_status='PAID'");
        Long createdCdkeys = count("cdk_record");
        Long successfulRedeems = countWhere("redeem_record", "result='SUCCESS'");
        List<Map<String, Object>> revenueTrend = jdbcTemplate.queryForList("""
                select cast(paid_at as date) as day, count(*) as paid_orders, coalesce(sum(pay_amount), 0) as revenue
                from order_info
                where pay_status='PAID' and paid_at >= ?
                group by cast(paid_at as date)
                order by day
                """, Timestamp.valueOf(LocalDate.now().minusDays(6).atStartOfDay()));
        List<Map<String, Object>> orderStatus = jdbcTemplate.queryForList("""
                select order_status, pay_status, delivery_status, count(*) as total
                from order_info
                group by order_status, pay_status, delivery_status
                order by total desc
                """);
        List<Map<String, Object>> channelRevenue = jdbcTemplate.queryForList("""
                select channel, count(*) as payments, coalesce(sum(amount), 0) as revenue
                from payment_record
                where status='SUCCESS'
                group by channel
                order by revenue desc
                """);
        List<Map<String, Object>> lowStockProducts = jdbcTemplate.queryForList("""
                select p.id, p.name, p.status,
                       (select count(*) from inventory_account i where i.product_id=p.id and i.status='AVAILABLE') as available_stock,
                       (select count(*) from inventory_account i where i.product_id=p.id and i.status='RESERVED') as reserved_stock,
                       (select count(*) from inventory_account i where i.product_id=p.id and i.status='ASSIGNED') as assigned_stock
                from product p
                where p.status <> 'DELETED'
                order by available_stock asc, p.id desc
                limit 8
                """);
        List<Map<String, Object>> recentOrders = jdbcTemplate.queryForList("""
                select order_no, product_name_snapshot, pay_amount, order_status, pay_status, delivery_status, created_at, paid_at
                from order_info order by id desc limit 8
                """).stream().map(SafeData::maskRow).toList();
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("products", count("product"));
        dashboard.put("orders", orders);
        dashboard.put("paidOrders", paidOrders);
        dashboard.put("redeems", successfulRedeems);
        dashboard.put("availableInventory", countWhere("inventory_account", "status='AVAILABLE'"));
        dashboard.put("reservedInventory", countWhere("inventory_account", "status='RESERVED'"));
        dashboard.put("assignedInventory", countWhere("inventory_account", "status='ASSIGNED'"));
        dashboard.put("totalRevenue", totalRevenue);
        dashboard.put("todayRevenue", todayRevenue);
        dashboard.put("payConversionRate", rate(paidOrders, orders));
        dashboard.put("redeemRate", rate(successfulRedeems, createdCdkeys));
        dashboard.put("revenueTrend", revenueTrend);
        dashboard.put("orderStatus", orderStatus);
        dashboard.put("channelRevenue", channelRevenue);
        dashboard.put("lowStockProducts", lowStockProducts);
        dashboard.put("recentOrders", recentOrders);
        return ApiResponse.ok(dashboard);
    }

    @GetMapping("/orders")
    public ApiResponse<PageResponse<Map<String, Object>>> orders(HttpServletRequest servletRequest,
                                                                 @RequestParam(defaultValue = "1") int pageNo,
                                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                                 @RequestParam(defaultValue = "") String keyword) {
        authSupport.requireAdmin(servletRequest);
        return page("order_info", "order_no", pageNo, pageSize, keyword);
    }

    @PostMapping("/orders/{orderNo}/mark-paid")
    public ApiResponse<?> markOrderPaid(HttpServletRequest servletRequest, @PathVariable String orderNo) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        Map<String, Object> result = paymentSettlementService.markOrderPaidManually(orderNo, admin.id());
        jdbcTemplate.update("insert into admin_operation_log(admin_id, module, operation_type, target_id) values (?, 'ORDER', 'MARK_PAID', ?)",
                admin.id(), orderNo);
        return ApiResponse.ok(result);
    }

    @GetMapping("/payments")
    public ApiResponse<PageResponse<Map<String, Object>>> payments(HttpServletRequest servletRequest,
                                                                   @RequestParam(defaultValue = "1") int pageNo,
                                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                                   @RequestParam(defaultValue = "") String keyword) {
        authSupport.requireAdmin(servletRequest);
        return page("payment_record", "payment_no", pageNo, pageSize, keyword);
    }

    @GetMapping("/cdkeys")
    public ApiResponse<PageResponse<Map<String, Object>>> cdkeys(HttpServletRequest servletRequest,
                                                                 @RequestParam(defaultValue = "1") int pageNo,
                                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                                 @RequestParam(defaultValue = "") String keyword) {
        authSupport.requireAdmin(servletRequest);
        return page("cdk_record", "cdk_code", pageNo, pageSize, keyword);
    }

    @GetMapping("/redeems")
    public ApiResponse<PageResponse<Map<String, Object>>> redeems(HttpServletRequest servletRequest,
                                                                  @RequestParam(defaultValue = "1") int pageNo,
                                                                  @RequestParam(defaultValue = "20") int pageSize,
                                                                  @RequestParam(defaultValue = "") String keyword) {
        authSupport.requireAdmin(servletRequest);
        return page("redeem_record", "cdk_code", pageNo, pageSize, keyword);
    }

    @GetMapping("/logs")
    public ApiResponse<PageResponse<Map<String, Object>>> logs(HttpServletRequest servletRequest,
                                                               @RequestParam(defaultValue = "1") int pageNo,
                                                               @RequestParam(defaultValue = "20") int pageSize) {
        authSupport.requireAdmin(servletRequest);
        pageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        Long total = jdbcTemplate.queryForObject("select count(*) from admin_operation_log", Long.class);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("select * from admin_operation_log order by id desc limit ? offset ?", pageSize, offset)
                .stream().map(SafeData::maskRow).toList();
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    @GetMapping("/verification-logs")
    public ApiResponse<PageResponse<Map<String, Object>>> verificationLogs(HttpServletRequest servletRequest,
                                                                           @RequestParam(defaultValue = "1") int pageNo,
                                                                           @RequestParam(defaultValue = "20") int pageSize,
                                                                           @RequestParam(defaultValue = "") String keyword) {
        authSupport.requireAdmin(servletRequest);
        pageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        String like = "%" + keyword + "%";
        Long total = jdbcTemplate.queryForObject("""
                select count(*) from verification_code_query_log
                where (? = '' or resource_account like ? or result like ?)
                """, Long.class, keyword, like, like);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select * from verification_code_query_log
                where (? = '' or resource_account like ? or result like ?)
                order by id desc limit ? offset ?
                """, keyword, like, like, pageSize, offset).stream().map(SafeData::maskRow).toList();
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    private ApiResponse<PageResponse<Map<String, Object>>> page(String table, String keywordColumn, int pageNo, int pageSize, String keyword) {
        if (!PAGE_TABLES.contains(table)) {
            throw new IllegalArgumentException("unsupported table");
        }
        pageSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        String like = "%" + keyword + "%";
        Long total = jdbcTemplate.queryForObject("select count(*) from " + table + " where (? = '' or " + keywordColumn + " like ?)", Long.class, keyword, like);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("select * from " + table + " where (? = '' or " + keywordColumn + " like ?) order by id desc limit ? offset ?", keyword, like, pageSize, offset);
        records = records.stream().map(SafeData::maskRow).toList();
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    private Long count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }

    private Long countWhere(String table, String condition) {
        return jdbcTemplate.queryForObject("select count(*) from " + table + " where " + condition, Long.class);
    }

    private BigDecimal sumPaidAmount(String extraCondition) {
        BigDecimal value = jdbcTemplate.queryForObject("select coalesce(sum(pay_amount), 0) from order_info where pay_status='PAID' " + extraCondition, BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal rate(Long numerator, Long denominator) {
        if (numerator == null || denominator == null || denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, java.math.RoundingMode.HALF_UP);
    }
}
