package com.company.gptplus.common;

import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Locale;
import java.util.Properties;

@Service
public class SmtpMailService {
    private static final String GMAIL_SMTP_PORT = "587";

    private final SystemConfigService systemConfigService;

    public SmtpMailService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    public boolean ready() {
        return systemConfigService.getPlain("gmail.imap.username").filter(v -> !v.isBlank()).isPresent()
                && systemConfigService.getSecret("gmail.imap.app_password").filter(v -> !v.isBlank()).isPresent();
    }

    public void sendRegisterCode(String to, String code) {
        String username = systemConfigService.getPlain("gmail.imap.username")
                .map(String::trim)
                .orElseThrow(() -> new BizException(10010, "后台尚未配置发信邮箱"));
        String appPassword = systemConfigService.getSecret("gmail.imap.app_password")
                .map(value -> value.replaceAll("\\s+", ""))
                .orElseThrow(() -> new BizException(10011, "后台尚未配置邮箱应用密钥"));
        String smtpHost = systemConfigService.getPlain("gmail.smtp.host").map(String::trim).filter(v -> !v.isBlank()).orElse("smtp.gmail.com");
        String smtpPort = GMAIL_SMTP_PORT;

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

    private void applyProxy(Properties props) {
        boolean enabled = Boolean.parseBoolean(systemConfigService.getPlain("gmail.smtp.proxy.enabled")
                .or(() -> systemConfigService.getPlain("gmail.imap.proxy.enabled"))
                .orElse("true"));
        if (!enabled) {
            return;
        }
        String proxyUrl = systemConfigService.getPlain("gmail.smtp.proxy.url")
                .or(() -> systemConfigService.getPlain("gmail.imap.proxy.url"))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .orElse("http://127.0.0.1:7897");
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
        } catch (Exception ex) {
            throw new BizException(10013, "SMTP 代理地址格式错误：" + proxyUrl);
        }
    }
}
