package com.arraywork.photowise.entity;

import lombok.Data;

/**
 * Camera Information
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Data
public class CameraInfo {

    private String makeModel;
    private String apertureValue;
    private String shutterSpeed;
    private String focalLength;
    private String isoEquivalent;
    private String exposureBias;


}