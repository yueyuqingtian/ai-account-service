package com.company.gptplus.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Service
public class SmtpMailService {
    private static final String GMAIL_SMTP_PORT = "587";

    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String mailProvider;
    private final String resendApiKey;
    private final String resendFrom;
    private final String smtpHostFallback;
    private final String smtpPortFallback;
    private final String smtpProxyEnabledFallback;
    private final String smtpProxyUrlFallback;

    public SmtpMailService(SystemConfigService systemConfigService,
                           @Value("${GPT_PLUS_MAIL_PROVIDER:auto}") String mailProvider,
                           @Value("${RESEND_API_KEY:}") String resendApiKey,
                           @Value("${RESEND_FROM:}") String resendFrom,
                           @Value("${SMTP_HOST:smtp.gmail.com}") String smtpHostFallback,
                           @Value("${SMTP_PORT:587}") String smtpPortFallback,
                           @Value("${SMTP_PROXY_ENABLED:}") String smtpProxyEnabledFallback,
                           @Value("${SMTP_PROXY_URL:}") String smtpProxyUrlFallback) {
        this.systemConfigService = systemConfigService;
        this.mailProvider = mailProvider;
        this.resendApiKey = resendApiKey;
        this.resendFrom = resendFrom;
        this.smtpHostFallback = smtpHostFallback;
        this.smtpPortFallback = smtpPortFallback;
        this.smtpProxyEnabledFallback = smtpProxyEnabledFallback;
        this.smtpProxyUrlFallback = smtpProxyUrlFallback;
    }

    public boolean ready() {
        return resendReady() || smtpReady();
    }

    public void sendRegisterCode(String to, String code) {
        if (useResend()) {
            sendRegisterCodeByResend(to, code);
            return;
        }
        sendRegisterCodeBySmtp(to, code);
    }

    private boolean smtpReady() {
        return systemConfigService.getPlain("gmail.imap.username").filter(v -> !v.isBlank()).isPresent()
                && systemConfigService.getSecret("gmail.imap.app_password").filter(v -> !v.isBlank()).isPresent();
    }

    private void sendRegisterCodeBySmtp(String to, String code) {
        String username = systemConfigService.getPlain("gmail.imap.username")
                .map(String::trim)
                .orElseThrow(() -> new BizException(10010, "后台尚未配置发信邮箱"));
        String appPassword = systemConfigService.getSecret("gmail.imap.app_password")
                .map(value -> value.replaceAll("\\s+", ""))
                .orElseThrow(() -> new BizException(10011, "后台尚未配置邮箱应用密钥"));
        String smtpHost = systemConfigService.getPlain("gmail.smtp.host")
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .orElse(smtpHostFallback == null || smtpHostFallback.isBlank() ? "smtp.gmail.com" : smtpHostFallback);
        String smtpPort = systemConfigService.getPlain("gmail.smtp.port")
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .orElse(smtpPortFallback == null || smtpPortFallback.isBlank() ? GMAIL_SMTP_PORT : smtpPortFallback);

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.ssl.trust", smtpHost);
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "12000");
            props.put("mail.smtp.writetimeout", "12000");
            applyProxy(props);
            Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, appPassword);
                }
            });
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "GPT Plus Service"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("注册验证码", "UTF-8");
            message.setText("你的注册验证码是：" + code + "，10 分钟内有效。", "UTF-8");
            Transport.send(message);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(10012, "验证码邮件发送失败：" + ex.getMessage());
        }
    }

    private void sendRegisterCodeByResend(String to, String code) {
        String apiKey = systemConfigService.getSecret("mail.resend.api_key")
                .or(() -> notBlank(resendApiKey))
                .orElseThrow(() -> new BizException(10014, "后台尚未配置 Resend API Key"));
        String from = systemConfigService.getPlain("mail.resend.from")
                .or(() -> notBlank(resendFrom))
                .orElseThrow(() -> new BizException(10015, "后台尚未配置 Resend 发信地址"));
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "from", from,
                    "to", List.of(to),
                    "subject", "注册验证码",
                    "text", "你的注册验证码是：" + code + "，10 分钟内有效。"
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException(10016, "验证码邮件发送失败：Resend 返回 " + response.statusCode());
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(10012, "验证码邮件发送失败：" + ex.getMessage());
        }
    }

    private void applyProxy(Properties props) {
        boolean enabled = Boolean.parseBoolean(systemConfigService.getPlain("gmail.smtp.proxy.enabled")
                .or(() -> systemConfigService.getPlain("gmail.imap.proxy.enabled"))
                .or(() -> notBlank(smtpProxyEnabledFallback))
                .orElse("false"));
        if (!enabled) {
            return;
        }
        String proxyUrl = systemConfigService.getPlain("gmail.smtp.proxy.url")
                .or(() -> systemConfigService.getPlain("gmail.imap.proxy.url"))
                .or(() -> notBlank(smtpProxyUrlFallback))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .orElseThrow(() -> new BizException(10013, "SMTP 代理已启用但未配置代理地址"));
        try {
            URI uri = proxyUrl.contains("://") ? URI.create(proxyUrl) : URI.create("http://" + proxyUrl);
            String host = uri.getHost();
            int port = uri.getPort();
            if (host == null || host.isBlank() || port <= 0) {
                throw new IllegalArgumentException("proxy host or port missing");
            }
            String scheme = uri.getScheme() == null ? "http" : uri.getScheme().toLowerCase(Locale.ROOT);
            if ("socks".equals(scheme) || "socks5".equals(scheme)) {
                props.put("mail.smtp.socks.host", host);
                props.put("mail.smtp.socks.port", String.valueOf(port));
            } else {
                props.put("mail.smtp.proxy.host", host);
                props.put("mail.smtp.proxy.port", String.valueOf(port));
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    props.put("mail.smtp.proxy.user", parts[0]);
                    if (parts.length > 1) {
                        props.put("mail.smtp.proxy.password", parts[1]);
                    }
                }
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(10013, "SMTP 代理地址格式错误：" + proxyUrl);
        }
    }

    private boolean useResend() {
        String provider = systemConfigService.getPlain("mail.provider")
                .or(() -> notBlank(mailProvider))
                .orElse("auto")
                .trim()
                .toLowerCase(Locale.ROOT);
        return "resend".equals(provider) || ("auto".equals(provider) && resendReady());
    }

    private boolean resendReady() {
        return systemConfigService.getSecret("mail.resend.api_key").filter(v -> !v.isBlank()).isPresent()
                || notBlank(resendApiKey).isPresent();
    }

    private Optional<String> notBlank(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
    }
}
