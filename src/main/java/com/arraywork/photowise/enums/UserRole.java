package com.arraywork.photowise.enums;

import com.arraywork.springforce.databind.GenericEnum;
import com.arraywork.springforce.databind.GenericEnumConverter;
import com.arraywork.springforce.security.SecurityRole;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User Role
 * @author AiChen
 * @created 2024/07/12
 */
@AllArgsConstructor
@Getter
public enum UserRole implements SecurityRole, GenericEnum<Integer> {

    GUEST(1, "访客"),
    ADMIN(9, "管理员");

    private final Integer code;
    private final String label;

    public static class Converter extends GenericEnumConverter<UserRole, Integer> {}

}