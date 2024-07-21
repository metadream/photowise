package com.arraywork.photowise.controller;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.arraywork.photowise.service.LibraryService;

/**
 * Home Controller
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Controller
public class HomeController {

    @Resource
    private LibraryService libraryService;

    @Value("${photowise.version}")
    private String version;

    @Value("${photowise.title}")
    private String title;

    @GetMapping("/")
    public String index(Model model) {
        return "redirect:/photos";
    }

    /** This method is controlled by SpaAspector */
    public String layout(Model model) {
        model.addAttribute("version", version);
        model.addAttribute("title", title);
        model.addAttribute("spaceInfo", libraryService.getSpaceInfo());
        return "index";
    }

}