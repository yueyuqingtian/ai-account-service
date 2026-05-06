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
    private final GmailImapVerificationService gmailImapVerificationService;

    public VerificationCodeController(AuthSupport authSupport,
                                      JdbcTemplate jdbcTemplate,
                                      GmailImapVerificationService gmailImapVerificationService) {
        this.authSupport = authSupport;
        this.jdbcTemplate = jdbcTemplate;
        this.gmailImapVerificationService = gmailImapVerificationService;
    }

    @PostMapping("/query")
    public ApiResponse<?> query(HttpServletRequest request, @Valid @RequestBody VerificationCodeRequest body) {
        AuthSupport.CurrentUser user = authSupport.requireUser(request);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select ia.*
                from redeem_record rr
                join inventory_account ia on ia.id = rr.inventory_id
                left join cdk_record cr on cr.id = rr.cdk_id
                where rr.user_id = ?
                  and lower(rr.cdk_code) = lower(?)
                  and lower(ia.resource_account) = lower(?)
                  and rr.result = 'SUCCESS'
                  and ia.status = 'ASSIGNED'
                  and (cr.id is null or cr.used_by_user_id = ?)
                order by rr.created_at desc
                limit 1
                """, user.id(), body.cdkCode(), body.accountEmail(), user.id());
        if (rows.isEmpty()) {
            writeLog(user.id(), body.accountEmail(), body.cdkCode(), "FAIL", null, null, "账号邮箱与 CDKey 不匹配或无权查询");
            throw new BizException(60010, "账号邮箱与 CDKey 不匹配或无权查询");
        }

        try {
            GmailImapVerificationService.VerificationCodeResult result = CompletableFuture
                    .supplyAsync(() -> gmailImapVerificationService.findLatestCode(body.accountEmail()))
                    .get(30, TimeUnit.SECONDS);
            writeLog(user.id(), body.accountEmail(), body.cdkCode(), "SUCCESS", result.code(), result.subject(), null);
            return ApiResponse.ok(Map.of(
                    "accountEmail", body.accountEmail(),
                    "cdkCode", body.cdkCode(),
                    "code", result.code(),
                    "subject", result.subject(),
                    "receivedAt", result.receivedAt(),
                    "sourceMailbox", result.sourceMailbox(),
                    "matchedBy", result.matchedBy()
            ));
        } catch (BizException ex) {
            writeLog(user.id(), body.accountEmail(), body.cdkCode(), "FAIL", null, null, ex.getMessage());
            throw ex;
        } catch (TimeoutException ex) {
            String message = "IMAP 查询超时，请检查邮箱配置或稍后重试";
            writeLog(user.id(), body.accountEmail(), body.cdkCode(), "FAIL", null, null, message);
            throw new BizException(60006, message);
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof BizException bizException) {
                writeLog(user.id(), body.accountEmail(), body.cdkCode(), "FAIL", null, null, bizException.getMessage());
                throw bizException;
            }
            String message = "验证码查询失败：" + ex.getMessage();
            writeLog(user.id(), body.accountEmail(), body.cdkCode(), "FAIL", null, null, message);
            throw new BizException(60007, message);
        }
    }

    private void writeLog(long userId, String account, String cdkCode, String result, String code, String subject, String failReason) {
        jdbcTemplate.update("""
                insert into verification_code_query_log(user_id, resource_account, cdk_code, result, code_value, message_subject, fail_reason)
                values (?, ?, ?, ?, ?, ?, ?)
                """, userId, account, cdkCode, result, code, subject, failReason);
    }

    public record VerificationCodeRequest(@Email @NotBlank String accountEmail, @NotBlank String cdkCode) {
    }
}
