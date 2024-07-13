package com.arraywork.photowise.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Controller
public class SpaController {

    @Value("${photowise.version}")
    private String version;

    @Value("${photowise.title}")
    private String title;

    @GetMapping("/")
    public String index(Model model) {
        return "redirect:/photos";
    }

    // This method is controlled by SpaAspector
    public String layout(Model model) {
        model.addAttribute("version", version);
        model.addAttribute("title", title);
        return "index";
    }

}