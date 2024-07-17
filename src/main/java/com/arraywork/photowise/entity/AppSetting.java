package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.arraywork.photowise.enums.AccessMode;
import com.arraywork.springforce.util.Validator;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "照片库路径不能为空", groups = Validator.Update.class)
    @Size(max = 255, message = "照片库路径不能超过 {max} 个字符")
    private String library;

    @NotNull(message = "访问模式不能为空")
    @Convert(converter = AccessMode.Converter.class)
    private AccessMode accessMode;

    @NotBlank(message = "管理员账号不能为空")
    @Size(max = 20, message = "管理员账号不能超过 {max} 个字符")
    private String adminUser;

    @Size(min = 6, max = 60, message = "管理员密码必须在 {min}-{max} 个字符之间")
    private String adminPass;

    @Size(max = 20, message = "访客账号不能超过 {max} 个字符")
    private String guestUser;

    @Size(min = 6, max = 60, message = "访客密码必须在 {min}-{max} 个字符之间")
    private String guestPass;

    private long usedSpace;

    @UpdateTimestamp
    private LocalDateTime lastModified;

    @Transient
    private boolean libChanged;

}