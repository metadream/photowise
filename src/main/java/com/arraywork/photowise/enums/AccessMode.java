package com.arraywork.photowise.enums;

import com.arraywork.springforce.databind.GenericEnum;
import com.arraywork.springforce.databind.GenericEnumConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Access Mode
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/15
 */
@AllArgsConstructor
@Getter
public enum AccessMode implements GenericEnum<Integer> {

    PUBLIC(1, "公开", "任何人无需登录即可访问"),
    PROTECTED(2, "保护", "以访客身份登录可访问"),
    PRIVATE(3, "私有", "仅管理员可访问");

    private final Integer code;
    private final String label;
    private final String desc;

    public static class Converter extends GenericEnumConverter<AccessMode, Integer> {}

}