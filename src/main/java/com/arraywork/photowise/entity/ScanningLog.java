package com.arraywork.photowise.entity;

import com.arraywork.photowise.enums.ScanningAction;
import com.arraywork.photowise.enums.ScanningResult;
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

    private ScanningAction action;
    private ScanningResult result;
    private String path;
    private String message;
    private int count;
    private int total;
    private int progress;
    private long time = System.currentTimeMillis();

}