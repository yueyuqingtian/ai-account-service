package com.company.gptplus.verification;

import com.company.gptplus.common.BizException;
import com.company.gptplus.common.SystemConfigService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GmailImapVerificationService {
    private static final Pattern CODE_PATTERN = Pattern.compile("(?<!\\d)(\\d{6})(?!\\d)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROUTING_LINE_PATTERN = Pattern.compile("(?im)^(to|delivered-to|x-forwarded-to|x-original-to|original-recipient|envelope-to|apparently-to|收件人|发送至)\\s*[:：].*$");
    private static final Set<String> ROUTING_HEADERS = Set.of(
            "Delivered-To",
            "X-Forwarded-To",
            "X-Original-To",
            "Original-Recipient",
            "Envelope-To",
            "X-Envelope-To",
            "Apparently-To"
    );
    private static final List<String> OPENAI_KEYWORDS = List.of("openai", "chatgpt", "chat.openai.com", "验证码", "verification code", "code");
    private static final int MAX_MESSAGES_TO_SCAN = 160;
    private static final int MAX_TEXT_CHARS = 160_000;

    private final SystemConfigService systemConfigService;

    public GmailImapVerificationService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    public VerificationCodeResult findLatestCode(String resourceAccount) {
        String gmail = systemConfigService.getPlain("gmail.imap.username")
                .map(String::trim)
                .orElseThrow(() -> new BizException(60001, "后台尚未配置 Google 邮箱"));
        String appPassword = systemConfigService.getSecret("gmail.imap.app_password")
                .map(value -> value.replaceAll("\\s+", ""))
                .orElseThrow(() -> new BizException(60002, "后台尚未配置 Google 邮箱应用密钥"));
        String host = systemConfigService.getPlain("gmail.imap.host").map(String::trim).filter(v -> !v.isBlank()).orElse("imap.gmail.com");
        String folderName = systemConfigService.getPlain("gmail.imap.folder").map(String::trim).filter(v -> !v.isBlank()).orElse("INBOX");

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.ssl.trust", host);
        props.put("mail.imaps.connectiontimeout", "10000");
        props.put("mail.imaps.timeout", "12000");
        props.put("mail.imaps.writetimeout", "12000");
        props.put("mail.imaps.partialfetch", "true");
        props.put("mail.imaps.fetchsize", "65536");
        applyProxy(props);

        Store store = null;
        Folder folder = null;
        try {
            Session session = Session.getInstance(props);
            store = session.getStore("imaps");
            store.connect(host, gmail, appPassword);
            folder = store.getFolder(folderName);
            if (folder == null || !folder.exists()) {
                throw new BizException(60008, "邮箱文件夹不存在：" + folderName);
            }
            folder.open(Folder.READ_ONLY);

            Instant sinceInstant = Instant.now().minus(30, ChronoUnit.MINUTES);
            Date since = Date.from(sinceInstant);
            SearchTerm recent = new ReceivedDateTerm(ReceivedDateTerm.GE, since);
            Message[] messages = folder.search(recent);
            Arrays.sort(messages, Comparator.comparing(this::safeMessageDate).reversed());

            String normalizedAccount = normalizeEmail(resourceAccount);
            List<CodeCandidate> candidates = new ArrayList<>();
            int scanned = 0;
            for (Message message : messages) {
                if (scanned++ >= MAX_MESSAGES_TO_SCAN) {
                    break;
                }
                Date receivedAt = safeMessageDate(message);
                if (receivedAt.toInstant().isBefore(sinceInstant)) {
                    continue;
                }
                String subject = Optional.ofNullable(message.getSubject()).orElse("");
                RoutingMatch routingMatch = matchRouting(message, normalizedAccount);
                String body = extractText(message);
                RoutingMatch bodyMatch = matchBodyRouting(body, normalizedAccount);
                RoutingMatch bestMatch = routingMatch.score() >= bodyMatch.score() ? routingMatch : bodyMatch;
                if (bestMatch.score() <= 0) {
                    continue;
                }
                collectCodeCandidates(candidates, normalizedAccount, subject, body, receivedAt, bestMatch);
            }

            return candidates.stream()
                    .max(Comparator.comparingInt(CodeCandidate::score)
                            .thenComparing(CodeCandidate::receivedAt))
                    .map(candidate -> new VerificationCodeResult(candidate.code(), candidate.subject(), candidate.receivedAt(), mask(gmail), candidate.matchedBy()))
                    .orElseThrow(() -> new BizException(60003, "最近 30 分钟内未找到该账号的验证码邮件"));
        } catch (BizException ex) {
            throw ex;
        } catch (AuthenticationFailedException ex) {
            throw new BizException(60004, "Google 邮箱认证失败，请检查邮箱、应用密钥和 IMAP 是否开启");
        } catch (MessagingException ex) {
            throw new BizException(60005, "IMAP 查询失败：" + ex.getMessage());
        } catch (Exception ex) {
            throw new BizException(60005, "IMAP 查询失败：" + ex.getMessage());
        } finally {
            closeQuietly(folder);
            closeQuietly(store);
        }
    }

    private void collectCodeCandidates(List<CodeCandidate> candidates,
                                       String account,
                                       String subject,
                                       String body,
                                       Date receivedAt,
                                       RoutingMatch match) {
        String combined = subject + "\n" + body;
        Matcher matcher = CODE_PATTERN.matcher(combined);
        while (matcher.find()) {
            String code = matcher.group(1);
            int score = match.score() * 1000 + codeContextScore(combined, matcher.start(), matcher.end());
            if (combined.toLowerCase(Locale.ROOT).contains(account)) {
                score += 80;
            }
            candidates.add(new CodeCandidate(code, subject, receivedAt, match.source(), score));
        }
    }

    private int codeContextScore(String text, int start, int end) {
        int from = Math.max(0, start - 180);
        int to = Math.min(text.length(), end + 180);
        String context = text.substring(from, to).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : OPENAI_KEYWORDS) {
            if (context.contains(keyword)) {
                score += 40;
            }
        }
        if (context.contains("login") || context.contains("登录") || context.contains("verify")) {
            score += 30;
        }
        if (context.contains("do not share") || context.contains("不要分享") || context.contains("minutes") || context.contains("分钟")) {
            score += 20;
        }
        return score;
    }

    private RoutingMatch matchRouting(Message message, String account) throws MessagingException {
        List<String> matched = new ArrayList<>();
        int score = 0;
        if (containsAddress(message.getRecipients(Message.RecipientType.TO), account)) {
            matched.add("TO");
            score = Math.max(score, 5);
        }
        if (containsAddress(message.getRecipients(Message.RecipientType.CC), account)) {
            matched.add("CC");
            score = Math.max(score, 4);
        }
        for (String header : ROUTING_HEADERS) {
            String[] values = message.getHeader(header);
            if (values != null && containsEmail(Arrays.asList(values), account)) {
                matched.add(header);
                score = Math.max(score, 6);
            }
        }
        return new RoutingMatch(score, String.join(",", matched));
    }

    private RoutingMatch matchBodyRouting(String body, String account) {
        int score = 0;
        List<String> matched = new ArrayList<>();
        Matcher routingLine = ROUTING_LINE_PATTERN.matcher(body);
        while (routingLine.find()) {
            String line = routingLine.group();
            if (containsEmail(List.of(line), account)) {
                score = Math.max(score, 3);
                matched.add("FORWARDED_BODY_HEADER");
            }
        }
        if (containsEmail(List.of(body), account)) {
            score = Math.max(score, 1);
            matched.add("BODY");
        }
        return new RoutingMatch(score, String.join(",", new LinkedHashSet<>(matched)));
    }

    private boolean containsAddress(Address[] addresses, String account) {
        if (addresses == null) {
            return false;
        }
        for (Address address : addresses) {
            if (address instanceof InternetAddress internetAddress) {
                if (normalizeEmail(internetAddress.getAddress()).equals(account)) {
                    return true;
                }
            } else if (extractEmails(String.valueOf(address)).contains(account)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEmail(Collection<String> values, String account) {
        for (String value : values) {
            if (extractEmails(value).contains(account)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> extractEmails(String value) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = EMAIL_PATTERN.matcher(Optional.ofNullable(value).orElse(""));
        while (matcher.find()) {
            result.add(normalizeEmail(matcher.group()));
        }
        return result;
    }

    private String normalizeEmail(String value) {
        return Optional.ofNullable(value).orElse("").trim().toLowerCase(Locale.ROOT);
    }

    private void applyProxy(Properties props) {
        boolean enabled = Boolean.parseBoolean(systemConfigService.getPlain("gmail.imap.proxy.enabled").orElse("true"));
        if (!enabled) {
            return;
        }
        String proxyUrl = systemConfigService.getPlain("gmail.imap.proxy.url")
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
                props.put("mail.imaps.socks.host", host);
                props.put("mail.imaps.socks.port", String.valueOf(port));
            } else {
                props.put("mail.imaps.proxy.host", host);
                props.put("mail.imaps.proxy.port", String.valueOf(port));
            }
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                props.put("mail.imaps.proxy.user", parts[0]);
                if (parts.length > 1) {
                    props.put("mail.imaps.proxy.password", parts[1]);
                }
            }
        } catch (Exception ex) {
            throw new BizException(60009, "IMAP 代理地址格式错误：" + proxyUrl);
        }
    }

    private Date safeMessageDate(Message message) {
        try {
            Date received = message.getReceivedDate();
            return received == null ? new Date(0) : received;
        } catch (MessagingException ex) {
            return new Date(0);
        }
    }

    private String extractText(Part part) throws Exception {
        StringBuilder builder = new StringBuilder();
        appendPartText(part, builder);
        return builder.length() > MAX_TEXT_CHARS ? builder.substring(0, MAX_TEXT_CHARS) : builder.toString();
    }

    private void appendPartText(Part part, StringBuilder builder) throws Exception {
        if (builder.length() >= MAX_TEXT_CHARS) {
            return;
        }
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            Object content = part.getContent();
            if (content != null) {
                String text = String.valueOf(content);
                if (part.isMimeType("text/html")) {
                    text = text.replaceAll("(?is)<script.*?</script>", " ")
                            .replaceAll("(?is)<style.*?</style>", " ")
                            .replaceAll("(?is)<[^>]+>", " ");
                }
                builder.append('\n').append(text);
            }
            return;
        }
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                appendPartText(multipart.getBodyPart(i), builder);
                if (builder.length() >= MAX_TEXT_CHARS) {
                    break;
                }
            }
        }
    }

    private void closeQuietly(Folder folder) {
        if (folder == null) {
            return;
        }
        try {
            if (folder.isOpen()) {
                folder.close(false);
            }
        } catch (Exception ignored) {
        }
    }

    private void closeQuietly(Store store) {
        if (store == null) {
            return;
        }
        try {
            store.close();
        } catch (Exception ignored) {
        }
    }

    private String mask(String value) {
        if (value == null || value.length() < 6) {
            return "***";
        }
        int at = value.indexOf('@');
        if (at > 2) {
            return value.substring(0, 2) + "***" + value.substring(at);
        }
        return value.substring(0, 3) + "***";
    }

    private record RoutingMatch(int score, String source) {
    }

    private record CodeCandidate(String code, String subject, Date receivedAt, String matchedBy, int score) {
    }

    public record VerificationCodeResult(String code, String subject, Date receivedAt, String sourceMailbox, String matchedBy) {
    }
}
