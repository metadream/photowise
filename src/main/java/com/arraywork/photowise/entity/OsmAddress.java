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
public class OsmAddress {

    private String osmId;
    private String displayName;
    private String country_code;
    private String country;     // 国家
    private String state;       // 省、州、直辖市
    private String region;      // 市郡
    private String city;        // 市郡
    private String district;    // 区
    private String county;      // 县
    private String town;        // 乡镇
    private String village;     // 村
    private String road;        // 道路

    /** 获取显示全名 */
    public String getDisplayName() {
        // Arrays.asList允许插入null，而List.of不允许
        // Arrays.asList不可修改，因此需要new以便删除null元素
        List<String> address = new ArrayList<>(Arrays.asList(
            country, state, region, city, district, county, town, village, road
        ));
        address.removeIf(Objects::isNull);

        if (country_code != null && country_code.matches("cn|tw|hk|mo")) {
            return String.join("", address);
        }
        return String.join(", ", address);
    }

    /** 获取地点名称（通常为城市） */
    public String getCity() {
        if (city != null) return city;
        if (region != null) return region;
        if (district != null) return district;
        if (county != null) return county;
        return state;
    }

}