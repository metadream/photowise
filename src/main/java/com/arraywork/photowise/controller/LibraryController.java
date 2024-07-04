package com.arraywork.photowise.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Library Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/04
 */
@Controller
public class LibraryController {

    @Value("${photowise.version}")
    private String version;

    @Value("${photowise.title}")
    private String title;

    @GetMapping("/library")
    public String library(Model model) {
        return "library";
    }

}