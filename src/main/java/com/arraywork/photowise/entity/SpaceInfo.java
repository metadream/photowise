package com.arraywork.photowise.entity;

import lombok.Data;

/**
 * Space Information
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/21
 */
@Data
public class SpaceInfo {

    private String usedSpace;
    private String totalSpace;
    private double percent;

}