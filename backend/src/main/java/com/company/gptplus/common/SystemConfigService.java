package com.company.gptplus.common;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SystemConfigService {
    private final JdbcTemplate jdbcTemplate;
    private final CryptoService cryptoService;

    public SystemConfigService(JdbcTemplate jdbcTemplate, CryptoService cryptoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.cryptoService = cryptoService;
    }

    public void putPlain(String key, String value, String remark) {
        upsert(key, value, false, remark);
    }

    public void putSecret(String key, String value, String remark) {
        upsert(key, cryptoService.encrypt(value), true, remark);
    }

    public Optional<String> getPlain(String key) {
        return get(key, false);
    }

    public Optional<String> getSecret(String key) {
        return get(key, true);
    }

    private Optional<String> get(String key, boolean secret) {
        try {
            var row = jdbcTemplate.queryForMap("select config_value, encrypted from system_config where config_key = ?", key);
            Object value = row.get("config_value");
            if (value == null) {
                return Optional.empty();
            }
            boolean encrypted = Boolean.parseBoolean(String.valueOf(row.get("encrypted")));
            String text = String.valueOf(value);
            if (secret || encrypted) {
                text = cryptoService.decrypt(text);
            }
            return Optional.of(text);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private void upsert(String key, String value, boolean encrypted, String remark) {
        int rows = jdbcTemplate.update("""
                update system_config set config_value=?, encrypted=?, remark=?, updated_at=current_timestamp
                where config_key=?
                """, value, encrypted, remark, key);
        if (rows == 0) {
            jdbcTemplate.update("""
                    insert into system_config(config_key, config_value, encrypted, remark)
                    values (?, ?, ?, ?)
                    """, key, value, encrypted, remark);
        }
    }
}
