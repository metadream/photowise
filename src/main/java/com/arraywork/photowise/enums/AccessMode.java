package com.arraywork.photowise.enums;

import com.arraywork.springforce.databind.GenericEnum;
import com.arraywork.springforce.databind.GenericEnumConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Access Mode
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/15
 */
@AllArgsConstructor
@Getter
public enum AccessMode implements GenericEnum<Integer> {

    PUBLIC(1, "任何人均可访问"),
    PROTECTED(2, "以访客身份访问"),
    PRIVATE(3, "仅管理员可访问");

    private final Integer code;
    private final String label;

    public static class Converter extends GenericEnumConverter<AccessMode, Integer> {
    }

}