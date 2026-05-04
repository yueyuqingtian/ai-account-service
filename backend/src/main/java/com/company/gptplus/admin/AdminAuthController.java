package com.company.gptplus.admin;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.BizException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthSupport authSupport;

    public AdminAuthController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, AuthSupport authSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.authSupport = authSupport;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> admin;
        try {
            admin = jdbcTemplate.queryForMap("select * from sys_admin where username = ?", request.username());
        } catch (EmptyResultDataAccessException ex) {
            throw new BizException(50001, "管理员账号或密码错误");
        }
        String hash = String.valueOf(admin.get("password_hash"));
        boolean bcrypt = hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
        boolean matched = bcrypt ? passwordEncoder.matches(request.password(), hash) : request.password().equals(hash);
        if (!matched) {
            throw new BizException(50001, "管理员账号或密码错误");
        }
        if (!bcrypt) {
            jdbcTemplate.update("update sys_admin set password_hash=?, updated_at=current_timestamp where id=?",
                    passwordEncoder.encode(request.password()), admin.get("id"));
        }
        jdbcTemplate.update("update sys_admin set last_login_at = current_timestamp where id = ?", admin.get("id"));
        long id = ((Number) admin.get("id")).longValue();
        return ApiResponse.ok(Map.of(
                "token", authSupport.issueToken(id, String.valueOf(admin.get("username")), "ADMIN"),
                "adminInfo", Map.of("id", id, "username", admin.get("username"), "roleCode", admin.get("role_code"))
        ));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
