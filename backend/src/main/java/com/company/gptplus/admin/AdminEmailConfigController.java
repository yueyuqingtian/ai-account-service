package com.company.gptplus.admin;

import com.company.gptplus.common.ApiResponse;
import com.company.gptplus.common.AuthSupport;
import com.company.gptplus.common.SystemConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/email-config")
public class AdminEmailConfigController {
    private final AuthSupport authSupport;
    private final SystemConfigService systemConfigService;

    public AdminEmailConfigController(AuthSupport authSupport, SystemConfigService systemConfigService) {
        this.authSupport = authSupport;
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    public ApiResponse<?> detail(HttpServletRequest request) {
        authSupport.requireAdmin(request);
        String username = systemConfigService.getPlain("gmail.imap.username").orElse("");
        String host = systemConfigService.getPlain("gmail.imap.host").orElse("imap.gmail.com");
        String folder = systemConfigService.getPlain("gmail.imap.folder").orElse("INBOX");
        boolean hasAppPassword = systemConfigService.getSecret("gmail.imap.app_password").filter(v -> !v.isBlank()).isPresent();
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("username", username);
        detail.put("host", host);
        detail.put("imapProxyEnabled", systemConfigService.getPlain("gmail.imap.proxy.enabled").orElse("true"));
        detail.put("imapProxyUrl", systemConfigService.getPlain("gmail.imap.proxy.url").orElse("http://127.0.0.1:7897"));
        detail.put("smtpHost", systemConfigService.getPlain("gmail.smtp.host").orElse("smtp.gmail.com"));
        detail.put("smtpPort", "587");
        detail.put("smtpProxyEnabled", systemConfigService.getPlain("gmail.smtp.proxy.enabled")
                .or(() -> systemConfigService.getPlain("gmail.imap.proxy.enabled")).orElse("true"));
        detail.put("smtpProxyUrl", systemConfigService.getPlain("gmail.smtp.proxy.url")
                .or(() -> systemConfigService.getPlain("gmail.imap.proxy.url")).orElse("http://127.0.0.1:7897"));
        detail.put("folder", folder);
        detail.put("hasAppPassword", hasAppPassword);
        detail.put("ready", !username.isBlank() && hasAppPassword);
        return ApiResponse.ok(detail);
    }

    @PostMapping
    public ApiResponse<?> save(HttpServletRequest request, @Valid @RequestBody EmailConfigRequest body) {
        AuthSupport.CurrentUser admin = authSupport.requireAdmin(request);
        systemConfigService.putPlain("gmail.imap.username", body.username().trim(), "Gmail IMAP username");
        systemConfigService.putPlain("gmail.imap.host", body.host() == null || body.host().isBlank() ? "imap.gmail.com" : body.host(), "Gmail IMAP host");
        systemConfigService.putPlain("gmail.imap.proxy.enabled", body.imapProxyEnabled() == null ? "true" : String.valueOf(body.imapProxyEnabled()), "Gmail IMAP proxy enabled");
        systemConfigService.putPlain("gmail.imap.proxy.url", body.imapProxyUrl() == null || body.imapProxyUrl().isBlank() ? "http://127.0.0.1:7897" : body.imapProxyUrl().trim(), "Gmail IMAP proxy URL");
        systemConfigService.putPlain("gmail.smtp.host", body.smtpHost() == null || body.smtpHost().isBlank() ? "smtp.gmail.com" : body.smtpHost().trim(), "Gmail SMTP host");
        systemConfigService.putPlain("gmail.smtp.port", "587", "Gmail SMTP port");
        systemConfigService.putPlain("gmail.smtp.proxy.enabled", body.smtpProxyEnabled() == null ? String.valueOf(body.imapProxyEnabled() == null || body.imapProxyEnabled()) : String.valueOf(body.smtpProxyEnabled()), "Gmail SMTP proxy enabled");
        systemConfigService.putPlain("gmail.smtp.proxy.url", body.smtpProxyUrl() == null || body.smtpProxyUrl().isBlank()
                ? (body.imapProxyUrl() == null || body.imapProxyUrl().isBlank() ? "http://127.0.0.1:7897" : body.imapProxyUrl().trim())
                : body.smtpProxyUrl().trim(), "Gmail SMTP proxy URL");
        systemConfigService.putPlain("gmail.imap.folder", body.folder() == null || body.folder().isBlank() ? "INBOX" : body.folder(), "Gmail IMAP folder");
        if (body.appPassword() != null && !body.appPassword().isBlank()) {
            systemConfigService.putSecret("gmail.imap.app_password", body.appPassword().replaceAll("\\s+", ""), "Gmail app password");
        }
        return ApiResponse.ok(Map.of("updatedBy", admin.username(), "ready", true));
    }

    public record EmailConfigRequest(@Email @NotBlank String username,
                                     String appPassword,
                                     String host,
                                     Boolean imapProxyEnabled,
                                     String imapProxyUrl,
                                     String smtpHost,
                                     String smtpPort,
                                     Boolean smtpProxyEnabled,
                                     String smtpProxyUrl,
                                     String folder) {
    }

}
