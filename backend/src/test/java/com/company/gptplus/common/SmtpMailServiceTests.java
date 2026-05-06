package com.company.gptplus.common;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmtpMailServiceTests {
    @Test
    void invalidSmtpProxyUrlFailsBeforeDirectSmtpConnection() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:smtp_config_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                create table system_config (
                  config_key varchar(128) primary key,
                  config_value clob,
                  encrypted boolean not null default false,
                  remark varchar(255),
                  updated_at timestamp not null default current_timestamp
                )
                """);
        SystemConfigService configService = new SystemConfigService(jdbcTemplate, new CryptoService("0123456789abcdef"));
        configService.putPlain("gmail.imap.username", "sender@gmail.com", "username");
        configService.putSecret("gmail.imap.app_password", "app-password", "password");
        configService.putPlain("gmail.smtp.proxy.enabled", "true", "SMTP proxy enabled");
        configService.putPlain("gmail.smtp.proxy.url", "http://127.0.0.1", "bad SMTP proxy");

        SmtpMailService service = new SmtpMailService(configService, "smtp", "", "", "smtp.gmail.com", "587", "", "");

        assertThatThrownBy(() -> service.sendRegisterCode("to@example.com", "123456"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("SMTP 代理地址格式错误");
    }
}
