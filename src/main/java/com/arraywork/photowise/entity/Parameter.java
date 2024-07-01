package com.arraywork.photowise.entity;

import lombok.Data;

/**
 * Photo Shooting Parameter
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Data
public class Parameter {

    private String makeModel;
    private String apertureValue;
    private String shutterSpeed;
    private String isoSpeed;
    private String exposureBias;
    private String focalLength;

}