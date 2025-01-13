package com.arraywork.photowise.entity;

import com.arraywork.vernal.util.NumberUtils;

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
            ? NumberUtils.formatDecimal(latitude, 4) + ", " + NumberUtils.formatDecimal(longitude, 4) : "";
    }
}