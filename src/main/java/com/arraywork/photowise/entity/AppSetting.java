package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * App Setting
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Entity
@Data
public class AppSetting {

    @Id
    @Column(length = 24, updatable = false)
    private String id;

    @NotBlank(message = "照片库路径不能为空")
    @Size(max = 255, message = "照片库路径不能超过 {max} 个字符")
    private String library;

    private boolean isPublic;

    @UpdateTimestamp
    private LocalDateTime lastModified;

    @Transient
    private String username;
    @Transient
    private String nickname;
    @Transient
    private String password;

}