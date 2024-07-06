package com.arraywork.photowise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.arraywork.photowise.service.PhotoService;

import jakarta.annotation.Resource;

/**
 * Photo Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@Controller
public class PhotoController {

    private static String viewMode = "grid";

    @Resource
    private PhotoService photoService;

    @SpaRoute
    @GetMapping("/timeline")
    public String timeline(Model model) {
        viewMode = "brick".equals(viewMode) ? "grid" : "brick";
        model.addAttribute("pagination", photoService.getPhotos(null, null));
        model.addAttribute("viewMode", viewMode);
        return "timeline";
    }

}