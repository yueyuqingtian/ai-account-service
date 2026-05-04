package com.company.gptplus.order;

import com.company.gptplus.common.*;
import com.company.gptplus.inventory.InventoryReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {
    private final JdbcTemplate jdbcTemplate;
    private final AuthSupport authSupport;
    private final InventoryReservationService inventoryReservationService;

    public OrderController(JdbcTemplate jdbcTemplate, AuthSupport authSupport, InventoryReservationService inventoryReservationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSupport = authSupport;
        this.inventoryReservationService = inventoryReservationService;
    }

    @PostMapping("/api/orders")
    @Transactional
    public ApiResponse<?> create(HttpServletRequest servletRequest, @Valid @RequestBody CreateOrderRequest request) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        Map<String, Object> product = jdbcTemplate.queryForMap("select * from product where id = ?", request.productId());
        if (!"ON_SHELF".equals(String.valueOf(product.get("status")))) {
            throw new BizException(20002, "商品已下架");
        }
        int quantity = request.quantity() == null || request.quantity() < 1 ? 1 : request.quantity();
        BigDecimal price = (BigDecimal) product.get("price");
        BigDecimal amount = price.multiply(BigDecimal.valueOf(quantity));
        String orderNo = Ids.orderNo();
        inventoryReservationService.reserve(request.productId(), orderNo, user.id(), quantity);
        jdbcTemplate.update("""
                insert into order_info(order_no, user_id, product_id, product_name_snapshot, price_snapshot, quantity,
                amount, pay_amount, order_status, pay_status, delivery_status, client_type)
                values (?, ?, ?, ?, ?, ?, ?, ?, 'UNPAID', 'UNPAID', 'PENDING', ?)
                """, orderNo, user.id(), request.productId(), product.get("name"), price, quantity, amount, amount,
                request.clientType() == null ? "WEB" : request.clientType());
        return ApiResponse.ok(Map.of("orderNo", orderNo, "amount", amount, "status", "UNPAID"));
    }

    @GetMapping("/api/user/orders")
    public ApiResponse<PageResponse<Map<String, Object>>> myOrders(HttpServletRequest servletRequest,
                                                                   @RequestParam(defaultValue = "1") int pageNo,
                                                                   @RequestParam(defaultValue = "20") int pageSize) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        Long total = jdbcTemplate.queryForObject("select count(*) from order_info where user_id = ?", Long.class, user.id());
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select o.*, p.cover_url from order_info o left join product p on p.id = o.product_id
                where o.user_id = ? order by o.id desc limit ? offset ?
                """, user.id(), pageSize, offset);
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    @GetMapping("/api/user/orders/{orderNo}")
    public ApiResponse<Map<String, Object>> detail(HttpServletRequest servletRequest, @PathVariable String orderNo) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("select * from order_info where user_id = ? and order_no = ?", user.id(), orderNo);
        if (records.isEmpty()) {
            throw new BizException(20003, "订单不存在");
        }
        return ApiResponse.ok(records.get(0));
    }

    public record CreateOrderRequest(@NotNull Long productId, Integer quantity, String clientType) {
    }
}
