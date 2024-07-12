package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.arraywork.photowise.enums.UserRole;
import com.arraywork.springforce.id.NanoIdGeneration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Users
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Entity
@Data
public class AppUser {

    @Id
    @NanoIdGeneration
    @Column(length = 24, insertable = false, updatable = false)
    private String id;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Username cannot be blank")
    private String password;

    private UserRole role;
    private boolean disabled;

    @UpdateTimestamp
    private LocalDateTime lastModified;

}