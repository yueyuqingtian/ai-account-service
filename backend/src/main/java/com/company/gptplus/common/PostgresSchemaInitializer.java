package com.company.gptplus.common;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
@Profile("prod")
public class PostgresSchemaInitializer {
    private final JdbcTemplate jdbcTemplate;

    public PostgresSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void run() throws Exception {
        ClassPathResource resource = new ClassPathResource("schema-postgresql.sql");
        String schema = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        for (String statement : schema.split(";")) {
            String sql = statement.trim();
            if (!sql.isBlank()) {
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception ex) {
                    System.err.println("PostgreSQL schema initialization failed at SQL: " + sql);
                    throw ex;
                }
            }
        }
        System.out.println("PostgreSQL schema initialization completed.");
    }
}
