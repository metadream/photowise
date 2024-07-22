package com.arraywork.photowise.enums;

import com.arraywork.springforce.databind.GenericEnum;
import com.arraywork.springforce.databind.GenericEnumConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Photo Tag
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/22
 */
@AllArgsConstructor
@Getter
public enum PhotoTag implements GenericEnum<Integer> {

    PLACE(1, "地点"),
    PEOPLE(2, "人物"),
    ANIMAL(3, "动物"),
    THINGS(4, "事物"),
    ALBUM(9, "影集");

    private final Integer code;
    private final String label;

    public static class Converter extends GenericEnumConverter<PhotoTag, Integer> {}

}