package com.company.gptplus.common;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PostgresSchemaInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public PostgresSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("schema-postgresql.sql");
        String schema = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        for (String statement : schema.split(";")) {
            String sql = statement.trim();
            if (!sql.isBlank()) {
                jdbcTemplate.execute(sql);
            }
        }
        System.out.println("PostgreSQL schema initialization completed.");
    }
}
