package com.arraywork.photowise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.enums.AccessMode;
import com.arraywork.photowise.service.SettingService;
import com.arraywork.photowise.spa.SpaRoute;
import com.arraywork.springforce.security.Authority;
import com.arraywork.springforce.security.SecurityController;

import jakarta.annotation.Resource;

/**
 * Setting Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/15
 */
@Controller
public class SettingController extends SecurityController {

    @Resource
    private SettingService settingService;

    @SpaRoute
    public String login(Model model) {
        return "login";
    }

    @SpaRoute
    @GetMapping("/settings")
    @Authority("ADMIN")
    public String settings(Model model) {
        model.addAttribute("setting", settingService.getSetting());
        model.addAttribute("accessModes", AccessMode.values());
        return "settings";
    }

    @PutMapping("/settings")
    @Authority("ADMIN")
    @ResponseBody
    public AppSetting settings(@Validated @RequestBody AppSetting setting) {
        return settingService.save(setting);
    }

}