package com.company.gptplus.admin;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.BizException;
import com.company.gptplus.common.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {
    private final JdbcTemplate jdbcTemplate;
    private final AuthSupport authSupport;

    public AdminProductController(JdbcTemplate jdbcTemplate, AuthSupport authSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSupport = authSupport;
    }

    @GetMapping
    public ApiResponse<PageResponse<Map<String, Object>>> list(HttpServletRequest servletRequest,
                                                               @RequestParam(defaultValue = "1") int pageNo,
                                                               @RequestParam(defaultValue = "20") int pageSize,
                                                               @RequestParam(defaultValue = "") String keyword,
                                                               @RequestParam(defaultValue = "") String status) {
        authSupport.requireAdmin(servletRequest);
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        String like = "%" + keyword + "%";
        Long total = jdbcTemplate.queryForObject("""
                select count(*) from product
                where status <> 'DELETED' and (? = '' or name like ? or product_code like ?) and (? = '' or status = ?)
                """, Long.class, keyword, like, like, status, status);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select p.*, (select count(*) from inventory_account i where i.product_id = p.id and i.status = 'AVAILABLE') as available_stock
                from product p
                where p.status <> 'DELETED' and (? = '' or p.name like ? or p.product_code like ?) and (? = '' or p.status = ?)
                order by p.sort desc, p.id desc limit ? offset ?
                """, keyword, like, like, status, status, pageSize, offset);
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    @PostMapping
    public ApiResponse<?> create(HttpServletRequest servletRequest, @Valid @RequestBody ProductRequest request) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        jdbcTemplate.update("""
                insert into product(product_code, name, subtitle, cover_url, price, original_price, delivery_type, status, description, stock_display_mode, sort)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, request.productCode(), request.name(), request.subtitle(), request.coverUrl(), request.price(),
                request.originalPrice(), request.deliveryType(), request.status(), request.description(),
                request.stockDisplayMode(), request.sort() == null ? 0 : request.sort());
        audit(admin.id(), "PRODUCT", "CREATE", request.productCode());
        return ApiResponse.ok(Map.of("productCode", request.productCode()));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(HttpServletRequest servletRequest, @PathVariable long id, @Valid @RequestBody ProductRequest request) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        int rows = jdbcTemplate.update("""
                update product set product_code=?, name=?, subtitle=?, cover_url=?, price=?, original_price=?,
                delivery_type=?, status=?, description=?, stock_display_mode=?, sort=?, updated_at=current_timestamp
                where id=?
                """, request.productCode(), request.name(), request.subtitle(), request.coverUrl(), request.price(),
                request.originalPrice(), request.deliveryType(), request.status(), request.description(),
                request.stockDisplayMode(), request.sort() == null ? 0 : request.sort(), id);
        if (rows == 0) {
            throw new BizException(20001, "商品不存在");
        }
        audit(admin.id(), "PRODUCT", "UPDATE", String.valueOf(id));
        return ApiResponse.ok(Map.of("id", id));
    }

    @PostMapping("/{id}/on-shelf")
    public ApiResponse<?> onShelf(HttpServletRequest servletRequest, @PathVariable long id) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        jdbcTemplate.update("update product set status='ON_SHELF', updated_at=current_timestamp where id=?", id);
        audit(admin.id(), "PRODUCT", "ON_SHELF", String.valueOf(id));
        return ApiResponse.ok(Map.of("id", id, "status", "ON_SHELF"));
    }

    @PostMapping("/{id}/off-shelf")
    public ApiResponse<?> offShelf(HttpServletRequest servletRequest, @PathVariable long id) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        jdbcTemplate.update("update product set status='OFF_SHELF', updated_at=current_timestamp where id=?", id);
        audit(admin.id(), "PRODUCT", "OFF_SHELF", String.valueOf(id));
        return ApiResponse.ok(Map.of("id", id, "status", "OFF_SHELF"));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(HttpServletRequest servletRequest, @PathVariable long id) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        Integer paidOrders = jdbcTemplate.queryForObject("select count(*) from order_info where product_id = ? and pay_status = 'PAID'", Integer.class, id);
        Integer activeOrders = jdbcTemplate.queryForObject("""
                select count(*) from order_info
                where product_id = ? and order_status = 'UNPAID' and pay_status = 'UNPAID'
                """, Integer.class, id);
        Integer occupiedInventory = jdbcTemplate.queryForObject("""
                select count(*) from inventory_account where product_id = ? and status in ('RESERVED','ASSIGNED')
                """, Integer.class, id);
        if ((paidOrders != null && paidOrders > 0)
                || (activeOrders != null && activeOrders > 0)
                || (occupiedInventory != null && occupiedInventory > 0)) {
            jdbcTemplate.update("update product set status='DELETED', updated_at=current_timestamp where id=?", id);
        } else {
            jdbcTemplate.update("delete from inventory_account where product_id=? and status in ('AVAILABLE','DISABLED')", id);
            jdbcTemplate.update("delete from product where id=?", id);
        }
        audit(admin.id(), "PRODUCT", "DELETE", String.valueOf(id));
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }

    private void audit(long adminId, String module, String operation, String target) {
        jdbcTemplate.update("insert into admin_operation_log(admin_id, module, operation_type, target_id) values (?, ?, ?, ?)",
                adminId, module, operation, target);
    }

    public record ProductRequest(
            @NotBlank String productCode,
            @NotBlank String name,
            String subtitle,
            String coverUrl,
            @NotNull BigDecimal price,
            BigDecimal originalPrice,
            String deliveryType,
            String status,
            String description,
            String stockDisplayMode,
            Integer sort
    ) {
        public ProductRequest {
            if (deliveryType == null || deliveryType.isBlank()) {
                deliveryType = "CDKEY";
            }
            if (status == null || status.isBlank()) {
                status = "OFF_SHELF";
            }
            if (stockDisplayMode == null || stockDisplayMode.isBlank()) {
                stockDisplayMode = "HIDE";
            }
        }
    }
}
