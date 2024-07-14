package com.arraywork.photowise.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Media Type
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/13
 */
@AllArgsConstructor
@Getter
public enum MediaType {

    IMAGE,
    VIDEO;

    public static MediaType nameOf(String type) {
        for (MediaType e : values()) {
            if (e.name().equals(type.toUpperCase())) return e;
        }
        return null;
    }

}