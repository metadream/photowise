package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.arraywork.photowise.enums.UserRole;
import com.arraywork.springforce.id.NanoIdGeneration;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * App User
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

    @Column(unique = true, updatable = false)
    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名不能超过 {max} 个字符")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称不能超过 {max} 个字")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 60, message = "密码必须在 {min}-{max} 个字符之间")
    private String password;

    @NotNull(message = "角色不能为空")
    @Convert(converter = UserRole.Converter.class)
    private UserRole role;

    private boolean disabled;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime creationTime;

    @UpdateTimestamp
    private LocalDateTime lastModified;

}