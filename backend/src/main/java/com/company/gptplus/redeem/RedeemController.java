package com.company.gptplus.redeem;

import com.company.gptplus.common.*;
import com.company.gptplus.inventory.InventoryReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class RedeemController {
    private static final ConcurrentHashMap<String, Object> LOCKS = new ConcurrentHashMap<>();

    private final JdbcTemplate jdbcTemplate;
    private final AuthSupport authSupport;
    private final CryptoService cryptoService;
    private final InventoryReservationService inventoryReservationService;

    public RedeemController(JdbcTemplate jdbcTemplate, AuthSupport authSupport, CryptoService cryptoService,
                            InventoryReservationService inventoryReservationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSupport = authSupport;
        this.cryptoService = cryptoService;
        this.inventoryReservationService = inventoryReservationService;
    }

    @PostMapping("/api/redeem")
    @Transactional
    public ApiResponse<?> redeem(HttpServletRequest servletRequest, @Valid @RequestBody RedeemRequest request) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        Object lock = LOCKS.computeIfAbsent(request.cdkCode(), ignored -> new Object());
        synchronized (lock) {
            try {
                return ApiResponse.ok(doRedeem(user, request.cdkCode(), servletRequest.getRemoteAddr()));
            } finally {
                LOCKS.remove(request.cdkCode());
            }
        }
    }

    private Map<String, Object> doRedeem(AuthSupport.CurrentUser user, String cdkCode, String clientIp) {
        List<Map<String, Object>> cdkeys = jdbcTemplate.queryForList("select * from cdk_record where cdk_code = ? and owner_user_id = ?", cdkCode, user.id());
        if (cdkeys.isEmpty()) {
            writeFail(user.id(), cdkCode, "CDKey 不存在", clientIp);
            throw new BizException(40001, "CDKey 不存在");
        }
        Map<String, Object> cdkey = cdkeys.get(0);
        if (!"BOUND".equals(String.valueOf(cdkey.get("status")))) {
            writeFail(user.id(), cdkCode, "CDKey 已使用或不可用", clientIp);
            throw new BizException(40002, "CDKey 已使用");
        }
        Map<String, Object> inventory;
        try {
            inventory = inventoryReservationService.claimForOrder(
                    ((Number) cdkey.get("product_id")).longValue(),
                    String.valueOf(cdkey.get("order_no")),
                    user.id()
            );
        } catch (BizException ex) {
            writeFail(user.id(), cdkCode, ex.getMessage(), clientIp);
            throw ex;
        }
        int cdkUsed = jdbcTemplate.update("update cdk_record set status='USED', used_at=current_timestamp, used_by_user_id=?, updated_at=current_timestamp where id=? and status='BOUND'",
                user.id(), cdkey.get("id"));
        if (cdkUsed == 0) {
            throw new BizException(40002, "CDKey 已使用");
        }
        String redeemNo = Ids.redeemNo();
        jdbcTemplate.update("""
                insert into redeem_record(redeem_no, user_id, cdk_id, cdk_code, product_id, inventory_id, result, client_ip)
                values (?, ?, ?, ?, ?, ?, 'SUCCESS', ?)
                """, redeemNo, user.id(), cdkey.get("id"), cdkCode, cdkey.get("product_id"), inventory.get("id"), clientIp);
        markOrderDeliveredIfDone(String.valueOf(cdkey.get("order_no")));
        return Map.of(
                "redeemNo", redeemNo,
                "deliveryType", "ACCOUNT",
                "account", inventory.get("resource_account"),
                "password", cryptoService.decrypt(String.valueOf(inventory.get("resource_password_cipher"))),
                "notice", "请尽快登录并修改安全设置"
        );
    }

    private void markOrderDeliveredIfDone(String orderNo) {
        Integer remaining = jdbcTemplate.queryForObject("""
                select count(*) from inventory_account
                where assigned_order_no = ? and status = 'RESERVED'
                """, Integer.class, orderNo);
        if (remaining != null && remaining == 0) {
            jdbcTemplate.update("""
                    update order_info set delivery_status='DELIVERED', updated_at=current_timestamp
                    where order_no=? and pay_status='PAID'
                    """, orderNo);
        }
    }

    @GetMapping("/api/user/redeems")
    public ApiResponse<PageResponse<Map<String, Object>>> myRedeems(HttpServletRequest servletRequest,
                                                                    @RequestParam(defaultValue = "1") int pageNo,
                                                                    @RequestParam(defaultValue = "20") int pageSize) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        Long total = jdbcTemplate.queryForObject("select count(*) from redeem_record where user_id = ?", Long.class, user.id());
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select r.*, p.name as product_name from redeem_record r left join product p on p.id = r.product_id
                where r.user_id = ? order by r.id desc limit ? offset ?
                """, user.id(), pageSize, offset);
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    private void writeFail(long userId, String cdkCode, String reason, String clientIp) {
        jdbcTemplate.update("""
                insert into redeem_record(redeem_no, user_id, cdk_code, result, fail_reason, client_ip)
                values (?, ?, ?, 'FAIL', ?, ?)
                """, Ids.redeemNo(), userId, cdkCode, reason, clientIp);
    }

    public record RedeemRequest(@NotBlank String cdkCode) {
    }
}
