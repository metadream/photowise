package com.arraywork.photowise.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Scanning Result
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ScanningResult {

    SUCCESS("成功"),
    SKIPPED("跳过"),
    FAILED("失败"),
    FINISHED("完成");

    private final String label;

    /** Serializing literal field */
    public String getName() {
        return this.name();
    }

}