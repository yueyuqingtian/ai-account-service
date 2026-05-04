package com.company.gptplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@EnableScheduling
public class GptPlusServiceApplication {
    public static void main(String[] args) {
        configureRenderPostgresUrl();
        SpringApplication.run(GptPlusServiceApplication.class, args);
    }

    private static void configureRenderPostgresUrl() {
        if (System.getenv("SPRING_DATASOURCE_URL") != null || System.getenv("POSTGRES_JDBC_URL") != null) {
            return;
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank() || databaseUrl.startsWith("jdbc:")) {
            return;
        }
        URI uri = URI.create(databaseUrl);
        if (!"postgresql".equals(uri.getScheme()) && !"postgres".equals(uri.getScheme())) {
            return;
        }
        String port = uri.getPort() < 0 ? "" : ":" + uri.getPort();
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        System.setProperty("spring.datasource.url", "jdbc:postgresql://" + uri.getHost() + port + path + query);

        String userInfo = uri.getRawUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            if (System.getenv("SPRING_DATASOURCE_USERNAME") == null && System.getenv("POSTGRES_USERNAME") == null) {
                System.setProperty("spring.datasource.username", decode(parts[0]));
            }
            if (parts.length > 1 && System.getenv("SPRING_DATASOURCE_PASSWORD") == null && System.getenv("POSTGRES_PASSWORD") == null) {
                System.setProperty("spring.datasource.password", decode(parts[1]));
            }
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
