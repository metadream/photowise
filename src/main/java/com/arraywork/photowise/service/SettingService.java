package com.arraywork.photowise.service;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.repo.SettingRepo;
import com.arraywork.springforce.external.BCryptEncoder;
import com.arraywork.springforce.util.Assert;

import jakarta.annotation.Resource;

/**
 * Setting Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Service
public class SettingService {

    private static final String SETTING_ID = "PHOTOWISE_SETTING";

    @Resource
    protected BCryptEncoder bCryptEncoder;
    @Resource
    private SettingRepo settingRepo;

    // Cache
    public AppSetting getSetting() {
        return settingRepo.findById(SETTING_ID).orElse(null);
    }

    // 保存设置
    @Transactional(rollbackFor = Exception.class)
    public AppSetting save(AppSetting _setting) {
        File library = new File(_setting.getLibrary());
        Assert.isTrue(library.exists() && library.isDirectory(), "照片库不存在或不是目录");

        AppSetting setting = getSetting();
        if (setting == null) {
            setting = _setting;
            setting.setId(SETTING_ID);
        }
        if (StringUtils.hasText(_setting.getAdminPass())) {
            setting.setAdminPass(bCryptEncoder.encode(_setting.getAdminPass()));
        }
        if (StringUtils.hasText(_setting.getGuestPass())) {
            setting.setGuestPass(bCryptEncoder.encode(_setting.getGuestPass()));
        }

        setting.setLibrary(_setting.getLibrary());
        setting.setAccessMode(_setting.getAccessMode());
        return settingRepo.save(setting);
    }

    // 保存使用空间
    @Transactional(rollbackFor = Exception.class)
    public AppSetting saveUsedSpace(long usedSpace) {
        AppSetting setting = getSetting();
        setting.setUsedSpace(usedSpace);
        return setting;
    }

}