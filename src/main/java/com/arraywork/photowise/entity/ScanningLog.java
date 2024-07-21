package com.arraywork.photowise.entity;

import com.arraywork.photowise.enums.LogLevel;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * Scanning Log
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@Data
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class ScanningLog {

    private LogLevel level;
    private String path;
    private String message;
    private int total;
    private int count;
    private int progress;
    private long time = System.currentTimeMillis();

    public ScanningLog(LogLevel level, int total, int count) {
        this.level = level;
        this.total = total;
        this.count = count;
        this.progress = total > 0 ? 100 * count / total : -1;
    }

}