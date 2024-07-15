package com.arraywork.photowise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.arraywork.springforce.security.Authority;

/**
 * Setting Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/15
 */
@Controller
public class SettingController {

    @GetMapping("/init")
    public String init(Model model) {
        return "settings";
    }

    @GetMapping("/settings")
    @Authority("ADMIN")
    public String settings(Model model) {
        return "settings";
    }

}