package com.arraywork.photowise.entity;

import lombok.Data;

/**
 * GEO Location
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Data
public class GeoLocation {

    private double latitude;
    private double longitude;
    private double altitude;
    private String location;

}