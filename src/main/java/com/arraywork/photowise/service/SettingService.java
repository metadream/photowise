package com.arraywork.photowise.service;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.entity.AppUser;
import com.arraywork.photowise.enums.UserRole;
import com.arraywork.photowise.repo.SettingRepo;
import com.arraywork.springforce.external.BCryptEncoder;
import com.arraywork.springforce.security.Principal;
import com.arraywork.springforce.security.SecurityService;
import com.arraywork.springforce.util.Assert;

import jakarta.annotation.Resource;

/**
 * Setting Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Service
public class SettingService implements SecurityService {

    private static final String SETTING_ID = "PHOTOWISE_SETTING";

    @Resource
    protected BCryptEncoder bCryptEncoder;
    @Resource
    private SettingRepo settingRepo;

    // Cache
    public AppSetting getSetting() {
        return settingRepo.findById(SETTING_ID).orElse(null);
    }

    // 登录
    @Override
    public Principal login(String username, String rawPassword) {
        AppSetting setting = getSetting();
        boolean isAdmin = username.equals(setting.getAdminUser())
            && bCryptEncoder.matches(rawPassword, setting.getAdminPass());
        boolean isGuest = username.equals(setting.getGuestUser())
            && bCryptEncoder.matches(rawPassword, setting.getGuestPass());

        AppUser appUser = null;
        if (isAdmin || isGuest) {
            UserRole role = isAdmin ? UserRole.ADMIN : UserRole.GUEST;
            appUser = new AppUser();
            appUser.setUsername(username);
            appUser.setRole(role);
        }
        return appUser;
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