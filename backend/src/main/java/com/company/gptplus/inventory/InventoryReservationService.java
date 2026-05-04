package com.company.gptplus.inventory;

import com.company.gptplus.common.BizException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InventoryReservationService {
    private final JdbcTemplate jdbcTemplate;

    public InventoryReservationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void reserve(long productId, String orderNo, long userId, int quantity) {
        for (int i = 0; i < quantity; i++) {
            if (!reserveOne(productId, orderNo, userId)) {
                release(orderNo);
                throw new BizException(20004, "库存不足");
            }
        }
    }

    public void ensureReserved(long productId, String orderNo, long userId, int quantity) {
        Integer occupied = jdbcTemplate.queryForObject("""
                select count(*) from inventory_account
                where assigned_order_no=? and assigned_user_id=? and status in ('RESERVED','ASSIGNED')
                """, Integer.class, orderNo, userId);
        int missing = quantity - (occupied == null ? 0 : occupied);
        if (missing > 0) {
            reserve(productId, orderNo, userId, missing);
        }
    }

    public void release(String orderNo) {
        jdbcTemplate.update("""
                update inventory_account
                set status='AVAILABLE', assigned_order_no=null, assigned_user_id=null, assigned_at=null, updated_at=current_timestamp
                where assigned_order_no=? and status='RESERVED'
                """, orderNo);
    }

    public Map<String, Object> claimReserved(String orderNo, long userId) {
        List<Map<String, Object>> inventories = jdbcTemplate.queryForList("""
                select * from inventory_account
                where assigned_order_no = ? and assigned_user_id = ? and status = 'RESERVED'
                order by id asc limit 1
                """, orderNo, userId);
        if (inventories.isEmpty()) {
            throw new BizException(40004, "库存不足或已交付");
        }
        Map<String, Object> inventory = inventories.get(0);
        int claimed = jdbcTemplate.update("""
                update inventory_account
                set status='ASSIGNED', assigned_at=current_timestamp, updated_at=current_timestamp
                where id=? and assigned_order_no=? and assigned_user_id=? and status='RESERVED'
                """, inventory.get("id"), orderNo, userId);
        if (claimed == 0) {
            throw new BizException(40004, "库存不足或已交付");
        }
        return inventory;
    }

    public Map<String, Object> claimForOrder(long productId, String orderNo, long userId) {
        Integer reserved = jdbcTemplate.queryForObject("""
                select count(*) from inventory_account
                where assigned_order_no=? and assigned_user_id=? and status='RESERVED'
                """, Integer.class, orderNo, userId);
        if (reserved == null || reserved == 0) {
            if (!reserveOne(productId, orderNo, userId)) {
                throw new BizException(40004, "库存不足或已交付");
            }
        }
        return claimReserved(orderNo, userId);
    }

    private boolean reserveOne(long productId, String orderNo, long userId) {
        while (true) {
            List<Long> ids = jdbcTemplate.queryForList("""
                    select id from inventory_account
                    where product_id = ? and status = 'AVAILABLE'
                    order by id asc limit 1
                    """, Long.class, productId);
            if (ids.isEmpty()) {
                return false;
            }
            int reserved = jdbcTemplate.update("""
                    update inventory_account
                    set status='RESERVED', assigned_order_no=?, assigned_user_id=?, assigned_at=current_timestamp, updated_at=current_timestamp
                    where id=? and status='AVAILABLE'
                    """, orderNo, userId, ids.get(0));
            if (reserved == 1) {
                return true;
            }
        }
    }
}
