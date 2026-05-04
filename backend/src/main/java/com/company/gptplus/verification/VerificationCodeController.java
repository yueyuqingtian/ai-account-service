package com.company.gptplus.verification;

import com.company.gptplus.common.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/verification-code")
public class VerificationCodeController {
    private final AuthSupport authSupport;
    private final JdbcTemplate jdbcTemplate;
    private final CryptoService cryptoService;
    private final GmailImapVerificationService gmailImapVerificationService;

    public VerificationCodeController(AuthSupport authSupport,
                                      JdbcTemplate jdbcTemplate,
                                      CryptoService cryptoService,
                                      GmailImapVerificationService gmailImapVerificationService) {
        this.authSupport = authSupport;
        this.jdbcTemplate = jdbcTemplate;
        this.cryptoService = cryptoService;
        this.gmailImapVerificationService = gmailImapVerificationService;
    }

    @PostMapping("/query")
    public ApiResponse<?> query(HttpServletRequest request, @Valid @RequestBody VerificationCodeRequest body) {
        AuthSupport.CurrentUser user = authSupport.requireUser(request);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select * from inventory_account
                where assigned_user_id = ? and resource_account = ? and status = 'ASSIGNED'
                order by assigned_at desc
                """, user.id(), body.accountEmail());
        if (rows.isEmpty()) {
            writeLog(user.id(), body.accountEmail(), "FAIL", null, null, "账号不属于当前用户或尚未兑换");
            throw new BizException(60010, "账号不属于当前用户或尚未兑换");
        }
        boolean passwordMatched = rows.stream().anyMatch(row ->
                body.accountPassword().equals(cryptoService.decrypt(String.valueOf(row.get("resource_password_cipher"))))
        );
        if (!passwordMatched) {
            writeLog(user.id(), body.accountEmail(), "FAIL", null, null, "账号密码校验失败");
            throw new BizException(60011, "账号邮箱或密码错误");
        }

        try {
            GmailImapVerificationService.VerificationCodeResult result = CompletableFuture
                    .supplyAsync(() -> gmailImapVerificationService.findLatestCode(body.accountEmail()))
                    .get(30, TimeUnit.SECONDS);
            writeLog(user.id(), body.accountEmail(), "SUCCESS", result.code(), result.subject(), null);
            return ApiResponse.ok(Map.of(
                    "accountEmail", body.accountEmail(),
                    "code", result.code(),
                    "subject", result.subject(),
                    "receivedAt", result.receivedAt(),
                    "sourceMailbox", result.sourceMailbox(),
                    "matchedBy", result.matchedBy()
            ));
        } catch (BizException ex) {
            writeLog(user.id(), body.accountEmail(), "FAIL", null, null, ex.getMessage());
            throw ex;
        } catch (TimeoutException ex) {
            String message = "IMAP 查询超时，请检查邮箱配置或稍后重试";
            writeLog(user.id(), body.accountEmail(), "FAIL", null, null, message);
            throw new BizException(60006, message);
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof BizException bizException) {
                writeLog(user.id(), body.accountEmail(), "FAIL", null, null, bizException.getMessage());
                throw bizException;
            }
            String message = "验证码查询失败：" + ex.getMessage();
            writeLog(user.id(), body.accountEmail(), "FAIL", null, null, message);
            throw new BizException(60007, message);
        }
    }

    private void writeLog(long userId, String account, String result, String code, String subject, String failReason) {
        jdbcTemplate.update("""
                insert into verification_code_query_log(user_id, resource_account, result, code_value, message_subject, fail_reason)
                values (?, ?, ?, ?, ?, ?)
                """, userId, account, result, code, subject, failReason);
    }

    public record VerificationCodeRequest(@Email @NotBlank String accountEmail, @NotBlank String accountPassword) {
    }
}
