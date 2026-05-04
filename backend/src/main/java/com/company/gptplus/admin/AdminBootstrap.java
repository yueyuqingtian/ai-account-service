package com.company.gptplus.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminBootstrap(JdbcTemplate jdbcTemplate,
                          PasswordEncoder passwordEncoder,
                          @Value("${gpt-plus.bootstrap.admin.username:}") String username,
                          @Value("${gpt-plus.bootstrap.admin.password:}") String password) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }
        Integer existing = jdbcTemplate.queryForObject(
                "select count(*) from sys_admin where username = ?",
                Integer.class,
                username
        );
        if (existing != null && existing > 0) {
            return;
        }
        jdbcTemplate.update("""
                insert into sys_admin(username, password_hash, role_code, status)
                values (?, ?, 'SUPER_ADMIN', 'ENABLED')
                """, username, passwordEncoder.encode(password));
    }
}
