package com.arraywork.photowise.service;

import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.repo.SettingRepo;

import jakarta.annotation.Resource;

/**
 * Setting Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Service
public class SettingService {

    @Resource
    private SettingRepo settingRepo;

    // Cache
    public AppSetting getSetting() {
        return settingRepo.getReferenceById("xx");
    }

}