package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.arraywork.photowise.enums.ViewMode;
import com.arraywork.springforce.id.NanoIdGeneration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
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
    @NanoIdGeneration
    @Column(length = 24, insertable = false, updatable = false)
    private String id;

    @NotBlank(message = "Photo library cannot be blank")
    private String library;

    private ViewMode viewMode;

    private boolean isPublic;

    @UpdateTimestamp
    private LocalDateTime lastModified;

}