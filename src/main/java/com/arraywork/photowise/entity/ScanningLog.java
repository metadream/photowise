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

    public ScanningLog(String path, int count, int total) {
        this.path = path;
        this.count = count;
        this.total = total;
        this.progress = total > 0 ? 100 * count / total : -1;
    }

}