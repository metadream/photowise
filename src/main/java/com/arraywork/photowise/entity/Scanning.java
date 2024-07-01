package com.arraywork.photowise.entity;

import lombok.ToString;

/**
 * Scanning Status Information
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/21
 */
@ToString
public class Scanning {

    public boolean inProgess;
    public int total;
    public int count;
    public long elapsedTime;
    public String message;

    private static Scanning singleton;

    private Scanning() {}

    public static Scanning getSingleton() {
        if (singleton == null) {
            singleton = new Scanning();
        }
        return singleton;
    }

}