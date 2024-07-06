package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.arraywork.photowise.enums.LogLevel;
import com.arraywork.springforce.id.NanoIdGeneration;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Scanning Log
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@Data
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class ScanningLog {

    @Id
    @NanoIdGeneration
    @Column(length = 24, insertable = false, updatable = false)
    private String id;

    @UpdateTimestamp
    private LocalDateTime time = LocalDateTime.now();

    private int total;
    private int count;
    private int success;
    private String path;
    private String message;
    private LogLevel level;

}