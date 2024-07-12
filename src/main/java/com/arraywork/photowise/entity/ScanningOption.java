package com.arraywork.photowise.entity;

import lombok.Data;

/**
 * Scanning Option
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/09
 */
@Data
public class ScanningOption {

    private boolean isFullScan;
    private boolean isCleanIndexes;

}