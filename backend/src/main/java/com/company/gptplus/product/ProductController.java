package com.company.gptplus.product;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.BizException;
import com.company.gptplus.common.PageResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final JdbcTemplate jdbcTemplate;

    public ProductController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ApiResponse<PageResponse<Map<String, Object>>> list(@RequestParam(defaultValue = "1") int pageNo,
                                                               @RequestParam(defaultValue = "10") int pageSize,
                                                               @RequestParam(defaultValue = "") String keyword) {
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        String like = "%" + keyword + "%";
        Long total = jdbcTemplate.queryForObject("""
                select count(*) from product
                where status = 'ON_SHELF' and (? = '' or name like ? or product_code like ?)
                """, Long.class, keyword, like, like);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select p.*, (select count(*) from inventory_account i where i.product_id = p.id and i.status = 'AVAILABLE') as available_stock
                from product p
                where p.status = 'ON_SHELF' and (? = '' or p.name like ? or p.product_code like ?)
                order by p.sort desc, p.id desc
                limit ? offset ?
                """, keyword, like, like, pageSize, offset);
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable long id) {
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select p.*, (select count(*) from inventory_account i where i.product_id = p.id and i.status = 'AVAILABLE') as available_stock
                from product p where p.id = ? and p.status = 'ON_SHELF'
                """, id);
        if (records.isEmpty()) {
            throw new BizException(20001, "商品不存在或已下架");
        }
        return ApiResponse.ok(records.get(0));
    }
}
