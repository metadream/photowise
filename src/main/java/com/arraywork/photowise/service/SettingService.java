package com.arraywork.photowise.service;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.entity.AppUser;
import com.arraywork.photowise.enums.AccessMode;
import com.arraywork.photowise.enums.UserRole;
import com.arraywork.photowise.repo.SettingRepo;
import com.arraywork.springforce.external.BCryptEncoder;
import com.arraywork.springforce.security.Principal;
import com.arraywork.springforce.security.SecurityService;
import com.arraywork.springforce.util.Assert;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * Setting Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Service
public class SettingService implements SecurityService {

    private static final String ADMIN_USER = "photowise";
    private static final String SETTING_ID = "PHOTOWISE_SETTING";

    @Resource
    protected BCryptEncoder bCryptEncoder;
    @Resource
    private SettingRepo settingRepo;

    // 初始化设置
    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void initSetting() {
        AppSetting setting = getSetting();
        if (setting != null) return;

        setting = new AppSetting();
        setting.setId(SETTING_ID);
        setting.setAccessMode(AccessMode.PRIVATE);
        setting.setAdminUser(ADMIN_USER);
        setting.setAdminPass(bCryptEncoder.encode(ADMIN_USER));
        settingRepo.save(setting);
    }

    // 获取设置
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
        Assert.isTrue(isAdmin || isGuest, "账号或密码错误");

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setRole(isAdmin ? UserRole.ADMIN : UserRole.GUEST);
        appUser.setSettled(StringUtils.hasText(setting.getLibrary()));
        return appUser;
    }

    // 保存设置
    @Transactional(rollbackFor = Exception.class)
    public AppSetting save(AppSetting _setting) {
        File library = new File(_setting.getLibrary());
        Assert.isTrue(library.exists() && library.isDirectory(), "照片库不存在或不是目录");

        AppSetting setting = getSetting();
        setting.setLibrary(_setting.getLibrary());
        setting.setAccessMode(_setting.getAccessMode());

        if (StringUtils.hasText(_setting.getAdminPass())) {
            setting.setAdminPass(bCryptEncoder.encode(_setting.getAdminPass()));
        }
        if (StringUtils.hasText(_setting.getGuestPass())) {
            setting.setGuestPass(bCryptEncoder.encode(_setting.getGuestPass()));
        }
        return setting;
    }

    // 保存使用空间
    @Transactional(rollbackFor = Exception.class)
    public AppSetting saveUsedSpace(long usedSpace) {
        AppSetting setting = getSetting();
        setting.setUsedSpace(usedSpace);
        return setting;
    }

}