package com.arraywork.photowise.enums;

import com.arraywork.springforce.databind.GenericEnum;

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

    BRICK(1, "brick"),
    GRID(2, "grid");

    private final Integer code;
    private final String label;

}