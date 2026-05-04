package com.company.gptplus.cdkey;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CdkeyController {
    private final JdbcTemplate jdbcTemplate;
    private final AuthSupport authSupport;

    public CdkeyController(JdbcTemplate jdbcTemplate, AuthSupport authSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSupport = authSupport;
    }

    @GetMapping("/api/user/cdkeys")
    public ApiResponse<PageResponse<Map<String, Object>>> myCdkeys(HttpServletRequest servletRequest,
                                                                   @RequestParam(defaultValue = "1") int pageNo,
                                                                   @RequestParam(defaultValue = "20") int pageSize) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        int offset = (Math.max(pageNo, 1) - 1) * pageSize;
        Long total = jdbcTemplate.queryForObject("select count(*) from cdk_record where owner_user_id = ?", Long.class, user.id());
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                select c.*, p.name as product_name from cdk_record c left join product p on p.id = c.product_id
                where c.owner_user_id = ? order by c.id desc limit ? offset ?
                """, user.id(), pageSize, offset);
        return ApiResponse.ok(new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records));
    }

    @GetMapping("/api/user/cdkeys/{cdkCode}")
    public ApiResponse<Map<String, Object>> detail(HttpServletRequest servletRequest, @PathVariable String cdkCode) {
        AuthSupport.CurrentUser user = authSupport.requireUser(servletRequest);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from cdk_record where owner_user_id = ? and cdk_code = ?", user.id(), cdkCode);
        return ApiResponse.ok(record);
    }
}
