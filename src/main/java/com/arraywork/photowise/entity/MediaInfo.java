package com.arraywork.photowise.entity;

import lombok.Data;

/**
 * Media Information
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Data
public class MediaInfo {

    private String mimeType;
    private int width;
    private int height;
    private String duration;

}