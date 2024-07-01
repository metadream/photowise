package com.arraywork.photowise.entity;

import lombok.ToString;

/**
 * Scanning Info
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/21
 */
@ToString
public class ScanningInfo {

    public boolean inProgess;
    public int total;
    public int count;
    public long elapsedTime;
    public String message;

    private static ScanningInfo singleton;

    private ScanningInfo() {}

    public static ScanningInfo getSingleton() {
        if (singleton == null) {
            singleton = new ScanningInfo();
        }
        return singleton;
    }

}