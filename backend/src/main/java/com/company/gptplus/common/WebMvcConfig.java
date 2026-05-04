package com.company.gptplus.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final String uploadDir;
    private final String allowedOrigins;
    private final String webOrigin;
    private final String adminOrigin;

    public WebMvcConfig(@Value("${gpt-plus.upload.dir:uploads}") String uploadDir,
                        @Value("${gpt-plus.cors.allowed-origins:*}") String allowedOrigins,
                        @Value("${gpt-plus.cors.web-origin:}") String webOrigin,
                        @Value("${gpt-plus.cors.admin-origin:}") String adminOrigin) {
        this.uploadDir = uploadDir;
        this.allowedOrigins = allowedOrigins;
        this.webOrigin = webOrigin;
        this.adminOrigin = adminOrigin;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(parseAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    private String[] parseAllowedOrigins() {
        Set<String> origins = new LinkedHashSet<>();
        addOrigins(origins, allowedOrigins);
        addOrigins(origins, webOrigin);
        addOrigins(origins, adminOrigin);
        return origins.isEmpty() ? new String[]{"*"} : origins.toArray(String[]::new);
    }

    private void addOrigins(Set<String> origins, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .map(this::normalizeOrigin)
                .forEach(origins::add);
    }

    private String normalizeOrigin(String origin) {
        if ("*".equals(origin) || origin.startsWith("http://") || origin.startsWith("https://")) {
            return origin;
        }
        return "https://" + origin;
    }
}
