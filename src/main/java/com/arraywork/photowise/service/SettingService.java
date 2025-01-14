package com.arraywork.photowise.service;

import java.io.File;
import java.nio.file.Path;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.entity.AppUser;
import com.arraywork.photowise.enums.AccessMode;
import com.arraywork.photowise.enums.UserRole;
import com.arraywork.photowise.repo.SettingRepo;
import com.arraywork.vernal.crypto.BCryptCipher;
import com.arraywork.vernal.security.Principal;
import com.arraywork.vernal.security.SecurityService;
import com.arraywork.vernal.util.Assert;

/**
 * AppSetting Service
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Service
public class SettingService implements SecurityService {

    private static final String SETTING_ID = "PHOTOWISE_SETTING";
    private static final String DEFAULT_USER_PSWD = "photowise";
    private static AppSetting appSetting;

    @Resource
    private SettingRepo settingRepo;

    /** Initialize the settings */
    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void initSetting() {
        appSetting = settingRepo.findById(SETTING_ID).orElse(null);
        if (appSetting == null) {
            appSetting = new AppSetting();
            appSetting.setId(SETTING_ID);
            appSetting.setAccessMode(AccessMode.PRIVATE);
            appSetting.setAdminUser(DEFAULT_USER_PSWD);
            appSetting.setAdminPass(BCryptCipher.encode(DEFAULT_USER_PSWD));
            settingRepo.save(appSetting);
        }
    }

    /** Get the settings */
    public AppSetting getSetting() {
        return appSetting;
    }

    /** Get the library path */
    public Path getLibrary() {
        String library = appSetting.getLibrary();
        Assert.notNull(library, "请先设置照片库");
        File lib = new File(library);
        Assert.isTrue(lib.exists() && lib.isDirectory(), "照片库不存在或不是目录");
        return Path.of(library);
    }

    /** Save settings */
    @Transactional(rollbackFor = Exception.class)
    public AppSetting save(AppSetting setting) {
        String _library = Path.of(setting.getLibrary()).toString();
        File library = new File(_library);
        Assert.isTrue(library.exists() && library.isDirectory(), "照片库不存在或不是目录");

        // TODO start scan if lib changed
        appSetting.setLibChanged(!_library.equals(appSetting.getLibrary()));
        appSetting.setLibrary(_library);
        appSetting.setAccessMode(setting.getAccessMode());
        appSetting.setAdminUser(setting.getAdminUser());
        appSetting.setGuestUser(setting.getGuestUser());

        if (StringUtils.hasText(setting.getAdminPass())) {
            appSetting.setAdminPass(BCryptCipher.encode(setting.getAdminPass()));
        }
        if (StringUtils.hasText(setting.getGuestPass())) {
            appSetting.setGuestPass(BCryptCipher.encode(setting.getGuestPass()));
        }
        return settingRepo.save(appSetting);
    }

    /** Login */
    @Override
    public Principal login(String username, String rawPassword) {
        boolean isAdmin = username.equals(appSetting.getAdminUser()) && BCryptCipher.matches(rawPassword, appSetting.getAdminPass());
        boolean isGuest = username.equals(appSetting.getGuestUser()) && BCryptCipher.matches(rawPassword, appSetting.getGuestPass());
        Assert.isTrue(isAdmin || isGuest, "账号或密码错误");

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setRole(isAdmin ? UserRole.ADMIN : UserRole.GUEST);
        appUser.setHasLibrary(StringUtils.hasText(appSetting.getLibrary()));
        return appUser;
    }

}