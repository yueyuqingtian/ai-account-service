package com.company.gptplus.inventory;

import com.company.gptplus.common.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/inventory")
public class AdminInventoryController {
    private final JdbcTemplate jdbcTemplate;
    private final AuthSupport authSupport;
    private final CryptoService cryptoService;

    public AdminInventoryController(JdbcTemplate jdbcTemplate, AuthSupport authSupport, CryptoService cryptoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSupport = authSupport;
        this.cryptoService = cryptoService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Map<String, Object>>> list(HttpServletRequest servletRequest,
                                                               @RequestParam(defaultValue = "1") int pageNo,
                                                               @RequestParam(defaultValue = "20") int pageSize,
                                                               @RequestParam(defaultValue = "") String status,
                                                               @RequestParam(required = false) Long productId) {
        authSupport.requireAdmin(servletRequest);
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        Long total = jdbcTemplate.queryForObject("""
                select count(*) from inventory_account
                where (? is null or product_id = ?) and (? = '' or status = ?)
                """, Long.class, productId, productId, status, status);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select i.*, p.name as product_name from inventory_account i left join product p on p.id = i.product_id
                where (? is null or i.product_id = ?) and (? = '' or i.status = ?)
                order by i.id desc limit ? offset ?
                """, productId, productId, status, status, pageSize, offset);
        records.forEach(row -> row.put("resource_password_cipher", "******"));
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    @PostMapping("/import")
    public ApiResponse<?> importInventory(HttpServletRequest servletRequest, @Valid @RequestBody ImportRequest request) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        if (request.productId() == null || request.productId() <= 0) {
            throw new BizException(41005, "请选择要导入的商品");
        }
        Integer productExists = jdbcTemplate.queryForObject("select count(*) from product where id = ? and status <> 'DELETED'", Integer.class, request.productId());
        if (productExists == null || productExists == 0) {
            throw new BizException(41006, "商品不存在或已删除");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new BizException(41007, "请输入库存内容");
        }
        String batchNo = Ids.batchNo();
        List<String> errors = new ArrayList<>();
        int success = 0;
        String[] lines = request.content().split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(",", 3);
            if (parts.length < 2) {
                errors.add("第 " + (i + 1) + " 行格式错误，应为 account,password[,remark]");
                continue;
            }
            String account = parts[0].trim();
            String password = parts[1].trim();
            String remark = parts.length > 2 ? parts[2].trim() : null;
            if (account.isBlank() || password.isBlank()) {
                errors.add("第 " + (i + 1) + " 行账号或密码不能为空");
                continue;
            }
            if (account.length() > 255) {
                errors.add("第 " + (i + 1) + " 行账号长度不能超过 255");
                continue;
            }
            if (remark != null && remark.length() > 500) {
                errors.add("第 " + (i + 1) + " 行备注长度不能超过 500");
                continue;
            }
            try {
                String passwordCipher = cryptoService.encrypt(password);
                if (passwordCipher.length() > 512) {
                    errors.add("第 " + (i + 1) + " 行密码过长");
                    continue;
                }
                int inserted = jdbcTemplate.update("""
                        insert into inventory_account(batch_no, product_id, resource_account, resource_password_cipher, status, remark)
                        values (?, ?, ?, ?, 'AVAILABLE', ?)
                        on conflict (product_id, resource_account) do nothing
                        """, batchNo, request.productId(), account, passwordCipher, remark);
                if (inserted == 1) {
                    success++;
                } else {
                    errors.add("第 " + (i + 1) + " 行导入失败：同一商品下账号已存在");
                }
            } catch (Exception ex) {
                errors.add("第 " + (i + 1) + " 行导入失败，请检查账号、密码和备注格式");
            }
        }
        jdbcTemplate.update("insert into admin_operation_log(admin_id, module, operation_type, target_id, after_json) values (?, 'INVENTORY', 'IMPORT', ?, ?)",
                admin.id(), batchNo, "{\"success\":" + success + ",\"errors\":" + errors.size() + "}");
        return ApiResponse.ok(Map.of("batchNo", batchNo, "success", success, "errors", errors));
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<?> disable(HttpServletRequest servletRequest, @PathVariable long id) {
        authSupport.requireAdmin(servletRequest);
        jdbcTemplate.update("update inventory_account set status='DISABLED', updated_at=current_timestamp where id=? and status='AVAILABLE'", id);
        return ApiResponse.ok(Map.of("id", id, "status", "DISABLED"));
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<?> enable(HttpServletRequest servletRequest, @PathVariable long id) {
        authSupport.requireAdmin(servletRequest);
        jdbcTemplate.update("update inventory_account set status='AVAILABLE', updated_at=current_timestamp where id=? and status='DISABLED'", id);
        return ApiResponse.ok(Map.of("id", id, "status", "AVAILABLE"));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(HttpServletRequest servletRequest, @PathVariable long id, @Valid @RequestBody InventoryUpdateRequest request) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select * from inventory_account where id=?", id);
        if (rows.isEmpty()) {
            throw new BizException(41001, "库存不存在");
        }
        Map<String, Object> current = rows.get(0);
        String currentStatus = String.valueOf(current.get("status"));
        if ("ASSIGNED".equals(currentStatus) || "RESERVED".equals(currentStatus)) {
            throw new BizException(41002, "已占用库存不能编辑账号密码");
        }
        String targetStatus = request.status() == null || request.status().isBlank() ? currentStatus : request.status();
        if (!List.of("AVAILABLE", "DISABLED").contains(targetStatus)) {
            throw new BizException(41004, "库存状态无效");
        }
        String passwordCipher = request.resourcePassword() == null || request.resourcePassword().isBlank()
                ? String.valueOf(current.get("resource_password_cipher"))
                : cryptoService.encrypt(request.resourcePassword().trim());
        jdbcTemplate.update("""
                update inventory_account set product_id=?, resource_account=?, resource_password_cipher=?, status=?, remark=?, updated_at=current_timestamp
                where id=? and status in ('AVAILABLE','DISABLED')
                """, request.productId(), request.resourceAccount().trim(), passwordCipher, targetStatus, request.remark(), id);
        jdbcTemplate.update("insert into admin_operation_log(admin_id, module, operation_type, target_id) values (?, 'INVENTORY', 'UPDATE', ?)",
                admin.id(), String.valueOf(id));
        return ApiResponse.ok(Map.of("id", id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(HttpServletRequest servletRequest, @PathVariable long id) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(servletRequest);
        int rows = jdbcTemplate.update("delete from inventory_account where id=? and status in ('AVAILABLE','DISABLED')", id);
        if (rows == 0) {
            throw new BizException(41003, "仅未分配库存可以删除");
        }
        jdbcTemplate.update("insert into admin_operation_log(admin_id, module, operation_type, target_id) values (?, 'INVENTORY', 'DELETE', ?)",
                admin.id(), String.valueOf(id));
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }

    public record ImportRequest(@NotNull Long productId, @NotNull String content) {
    }

    public record InventoryUpdateRequest(@NotNull Long productId,
                                         @NotNull String resourceAccount,
                                         String resourcePassword,
                                         String status,
                                         String remark) {
    }
}
