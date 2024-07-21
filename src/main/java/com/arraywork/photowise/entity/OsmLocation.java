package com.arraywork.photowise.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.Data;

/**
 * Open Street Map Location
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/21
 */
@Data
public class OsmLocation {

    private String osmId;
    private String country;     // 国家
    private String state;       // 省州
    private String region;      // 市郡
    private String district;    // 区
    private String county;      // 县
    private String town;        // 乡镇
    private String road;        // 道路

    public String toString() {
        // Arrays.asList允许插入null，而List.of不允许
        // Arrays.asList不可修改，因此需要new以便删除null元素
        List<String> address = new ArrayList<>(Arrays.asList(country, state, region, district, county, town, road));
        address.removeIf(Objects::isNull);
        return String.join("", address);
    }

}