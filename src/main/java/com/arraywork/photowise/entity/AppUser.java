package com.arraywork.photowise.entity;

import java.util.List;

import com.arraywork.photowise.enums.UserRole;
import com.arraywork.springforce.security.Principal;
import com.arraywork.springforce.security.SecurityRole;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App User
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AppUser extends Principal {

    private UserRole role;

    @Override
    public List<SecurityRole> getSecurityRoles() {
        return null;
    }

}