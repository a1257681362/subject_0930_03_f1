package com.evops.museum.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 业务单号生成器：前缀 + 时间戳 + 随机段，数据库唯一约束兜底。
 */
@Component
public class BizNoGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String next(String prefix) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + LocalDateTime.now().format(FORMATTER) + "-" + random;
    }
}
