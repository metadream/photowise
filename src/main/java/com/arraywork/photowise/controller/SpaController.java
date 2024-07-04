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
        return "redirect:/timeline";
    }

    @GetMapping("/timeline")
    public String timeline(Model model) {
        return "timeline";
    }

    @GetMapping("/places")
    public String places(Model model) {
        model.addAttribute("places", "beijing, shanghai");
        return "places";
    }

    @GetMapping("/people")
    public String people(Model model) {
        model.addAttribute("people", "zhangsan, lisi");
        return "people";
    }

    @GetMapping("/animals")
    public String animals(Model model) {
        return "animals";
    }

    @GetMapping("/things")
    public String things(Model model) {
        return "things";
    }

    @GetMapping("/albums")
    public String albums(Model model) {
        return "albums";
    }

    @GetMapping("/videos")
    public String videos(Model model) {
        return "videos";
    }

    @GetMapping("/favorites")
    public String favorites(Model model) {
        return "favorites";
    }

    @GetMapping("/folders")
    public String folders(Model model) {
        return "folders";
    }

    @GetMapping("/trash")
    public String trash(Model model) {
        return "/trash";
    }

    // This method is controlled by SpaAspector
    public String layout(Model model) {
        model.addAttribute("version", version);
        model.addAttribute("title", title);
        return "index";
    }

}