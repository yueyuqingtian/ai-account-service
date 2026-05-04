package com.company.gptplus.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public ApiResponse<?> health() {
        return ApiResponse.ok(Map.of("status", "UP", "version", "5.0.0"));
    }
}
