package com.arraywork.photowise.entity;

import com.arraywork.springforce.util.Numbers;

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
    private String coordinates;
    private OsmAddress address;

    public String getCoordinates() {
        return latitude > 0 && longitude > 0
            ? Numbers.formatDecimal(latitude, 4) + ", " + Numbers.formatDecimal(longitude, 4) : "";
    }
}