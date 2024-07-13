package com.arraywork.photowise.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.arraywork.photowise.service.PhotoService;
import com.arraywork.photowise.service.SettingService;

import jakarta.annotation.Resource;

/**
 * Photo Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@Controller
public class PhotoController {

    @Resource
    private SettingService settingService;
    @Resource
    private PhotoService photoService;

    @Value("${photowise.brick-size}")
    private int brickSize;

    @SpaRoute
    @GetMapping("/photos")
    public String photos(Model model) {
        model.addAttribute("photos", photoService.getPhotos(null, null));
        model.addAttribute("brickSize", brickSize);
        return "photos";
    }

    @SpaRoute
    @GetMapping("/videos")
    public String videos(Model model) {
        return "videos";
    }

    @SpaRoute
    @GetMapping("/places")
    public String places(Model model) {
        model.addAttribute("places", "beijing, shanghai");
        return "places";
    }

    @SpaRoute
    @GetMapping("/people")
    public String people(Model model) {
        model.addAttribute("people", "zhangsan, lisi");
        return "people";
    }

    @SpaRoute
    @GetMapping("/animals")
    public String animals(Model model) {
        return "animals";
    }

    @SpaRoute
    @GetMapping("/things")
    public String things(Model model) {
        return "things";
    }

    @SpaRoute
    @GetMapping("/albums")
    public String albums(Model model) {
        return "albums";
    }

    @SpaRoute
    @GetMapping("/favorites")
    public String favorites(Model model) {
        return "favorites";
    }

    @SpaRoute
    @GetMapping("/folders")
    public String folders(Model model) {
        return "folders";
    }

    @SpaRoute
    @GetMapping("/trash")
    public String trash(Model model) {
        return "/trash";
    }

    @PutMapping("/favorite")
    @ResponseBody
    public int favorite(@RequestBody Map<String, Object> map) {
        String[] photoIds = ((String) map.get("photoIds")).split(",");
        boolean favored = (Boolean) map.get("favored");
        return photoService.favorite(photoIds, favored);
    }

}