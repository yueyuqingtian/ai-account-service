package com.company.gptplus.auth;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.BizException;
import com.company.gptplus.common.SmtpMailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthSupport authSupport;
    private final SmtpMailService smtpMailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, AuthSupport authSupport, SmtpMailService smtpMailService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.authSupport = authSupport;
        this.smtpMailService = smtpMailService;
    }

    @PostMapping("/send-register-code")
    public ApiResponse<?> sendRegisterCode(@Valid @RequestBody SendRegisterCodeRequest request) {
        Integer exists = jdbcTemplate.queryForObject("select count(*) from sys_user where email = ?", Integer.class, request.email());
        if (exists != null && exists > 0) {
            throw new BizException(10005, "该邮箱已注册");
        }
        String code = String.valueOf(100000 + secureRandom.nextInt(900000));
        jdbcTemplate.update("update email_verification_code set status='EXPIRED' where email=? and scene='REGISTER' and status='UNUSED'", request.email());
        jdbcTemplate.update("""
                insert into email_verification_code(email, code_value, scene, status, expired_at)
                values (?, ?, 'REGISTER', 'UNUSED', ?)
                """, request.email(), code, Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
        if (!smtpMailService.ready()) {
            throw new BizException(10009, "后台尚未配置发信邮箱");
        }
        smtpMailService.sendRegisterCode(request.email(), code);
        return ApiResponse.ok(Map.of("sent", true, "email", request.email()));
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        if (request.email() == null || request.email().isBlank()) {
            throw new BizException(10006, "邮箱不能为空");
        }
        if (request.verifyCode() == null || request.verifyCode().isBlank()) {
            throw new BizException(10007, "邮箱验证码不能为空");
        }
        List<Map<String, Object>> codes = jdbcTemplate.queryForList("""
                select * from email_verification_code
                where email=? and code_value=? and scene='REGISTER' and status='UNUSED' and expired_at > current_timestamp
                order by id desc limit 1
                """, request.email(), request.verifyCode());
        if (codes.isEmpty()) {
            throw new BizException(10008, "邮箱验证码错误或已过期");
        }
        Integer exists = jdbcTemplate.queryForObject("select count(*) from sys_user where username = ?", Integer.class, request.username());
        if (exists != null && exists > 0) {
            throw new BizException(10003, "用户名已存在");
        }
        Integer emailExists = jdbcTemplate.queryForObject("select count(*) from sys_user where email = ?", Integer.class, request.email());
        if (emailExists != null && emailExists > 0) {
            throw new BizException(10005, "该邮箱已注册");
        }
        jdbcTemplate.update("""
                insert into sys_user(username, password_hash, email, status, register_ip)
                values (?, ?, ?, 'ENABLED', ?)
                """, request.username(), passwordEncoder.encode(request.password()), request.email(), servletRequest.getRemoteAddr());
        jdbcTemplate.update("update email_verification_code set status='USED', used_at=current_timestamp where id=?", codes.get(0).get("id"));
        return ApiResponse.ok(Map.of("username", request.username()));
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> user;
        try {
            user = jdbcTemplate.queryForMap("select * from sys_user where username = ?", request.username());
        } catch (EmptyResultDataAccessException ex) {
            throw new BizException(10004, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.password(), String.valueOf(user.get("password_hash")))) {
            throw new BizException(10004, "用户名或密码错误");
        }
        jdbcTemplate.update("update sys_user set last_login_at = current_timestamp where id = ?", user.get("id"));
        long id = ((Number) user.get("id")).longValue();
        return ApiResponse.ok(Map.of(
                "token", authSupport.issueToken(id, String.valueOf(user.get("username")), "USER"),
                "userInfo", Map.of("id", id, "username", user.get("username"))
        ));
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank String password, String email, String verifyCode) {
    }

    public record SendRegisterCodeRequest(@Email @NotBlank String email) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
