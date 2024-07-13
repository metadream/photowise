package com.arraywork.photowise.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.entity.AppUser;
import com.arraywork.photowise.enums.UserRole;
import com.arraywork.photowise.repo.SettingRepo;
import com.arraywork.springforce.external.BCryptEncoder;

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
    private UserService userService;
    @Resource
    private SettingRepo settingRepo;

    // 保存设置
    @Transactional(rollbackFor = Exception.class)
    public AppSetting save(AppSetting _setting) {
        AppSetting setting = getSetting();

        // 如果无设置则进行初始化
        if (setting == null) {
            setting = new AppSetting();
            setting.setId(SETTING_ID);

            // 创建管理员用户
            AppUser user = new AppUser();
            user.setUsername(_setting.getUsername());
            user.setNickname(_setting.getNickname());
            user.setPassword(bCryptEncoder.encode(_setting.getPassword()));
            user.setRole(UserRole.ADMIN);
            userService.save(user);
        }
        setting.setLibrary(_setting.getLibrary());
        return settingRepo.save(setting);
    }

    // Cache
    public AppSetting getSetting() {
        return settingRepo.getReferenceById(SETTING_ID);
    }

}