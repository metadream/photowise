package com.arraywork.photowise.enums;

import com.arraywork.springforce.databind.GenericEnum;
import com.arraywork.springforce.databind.GenericEnumConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * View Mode
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@AllArgsConstructor
@Getter
public enum ViewMode implements GenericEnum<Integer> {

    BRICK(1),
    GRID(2);

    private final Integer code;

    public static class Converter extends GenericEnumConverter<ViewMode, Integer> {}

}