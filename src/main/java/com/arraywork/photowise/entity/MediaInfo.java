package com.arraywork.photowise.entity;

import com.arraywork.photowise.enums.MediaType;

import lombok.Data;

/**
 * Media Information
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Data
public class MediaInfo {

    private MediaType type;
    private int width;
    private int height;
    private long length;
    private long duration;

}