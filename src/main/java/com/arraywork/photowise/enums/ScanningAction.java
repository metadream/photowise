package com.arraywork.photowise.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Scanning Action
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ScanningAction {

    SCAN("扫描"),
    ADD("新增"),
    MODIFY("更新"),
    DELETE("删除"),
    PURGE("清除");

    private final String label;

    /** Serializing literal field */
    public String getName() {
        return this.name();
    }

}