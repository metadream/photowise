package com.arraywork.photowise.entity;

import com.arraywork.photowise.enums.LogLevel;
import com.fasterxml.jackson.annotation.JsonFormat;

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

    private int total;
    private int count;
    private int success;
    private String path;
    private String message;
    private LogLevel level;
    private long time = System.currentTimeMillis();

}