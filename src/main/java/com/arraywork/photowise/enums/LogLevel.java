package com.arraywork.photowise.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Log Level
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LogLevel {

    INFO(1, "信息"),
    SKIPPED(2, "跳过"),
    CLEAN(3, "清理"),
    ERROR(8, "错误"),
    FINISHED(9, "完成");

    private final Integer code;
    private final String label;

    // 序列化字面量属性
    public String getName() {
        return this.name();
    }

}