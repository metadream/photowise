package com.arraywork.photowise.enums;

import com.arraywork.springforce.security.SecurityRole;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User Role
 *
 * @author AiChen
 * @created 2024/07/12
 */
@AllArgsConstructor
@Getter
public enum UserRole implements SecurityRole {

    GUEST,
    ADMIN;

}