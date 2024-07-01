package com.arraywork.photowise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Index Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Controller
public class IndexController {

    // Index page
    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

}