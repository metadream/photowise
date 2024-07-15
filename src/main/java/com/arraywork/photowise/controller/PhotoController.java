package com.arraywork.photowise.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.enums.MediaType;
import com.arraywork.photowise.service.PhotoService;
import com.arraywork.photowise.service.SettingService;
import com.arraywork.photowise.spa.SpaRoute;

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

    @SpaRoute
    @GetMapping("/photos")
    public String photos(Model model) {
        PhotoIndex condition = new PhotoIndex();
        condition.setMediaType(MediaType.IMAGE);
        model.addAttribute("photoGroup", photoService.getIndexes(condition));
        return "photos";
    }

    @SpaRoute
    @GetMapping("/videos")
    public String videos(Model model) {
        PhotoIndex condition = new PhotoIndex();
        condition.setMediaType(MediaType.VIDEO);
        model.addAttribute("photoGroup", photoService.getIndexes(condition));
        return "photos";
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
        PhotoIndex condition = new PhotoIndex();
        condition.setFavored(true);
        model.addAttribute("photoGroup", photoService.getIndexes(condition));
        return "photos";
    }

    @SpaRoute
    @GetMapping("/folders")
    public String folders(Model model) {
        return "folders";
    }

    @SpaRoute
    @GetMapping("/trash")
    public String trash(Model model) {
        PhotoIndex condition = new PhotoIndex();
        condition.setTrashed(true);
        model.addAttribute("photoGroup", photoService.getIndexes(condition));
        return "/photos";
    }

    @PutMapping("/favorite")
    @ResponseBody
    public int favorite(@RequestBody Map<String, Object> map) {
        String[] photoIds = ((String) map.get("photoIds")).split(",");
        boolean favored = (Boolean) map.get("favored");
        return photoService.favorite(photoIds, favored);
    }

    @PutMapping("/trash")
    @ResponseBody
    public int trash(@RequestBody Map<String, Object> map) throws IOException {
        String[] photoIds = ((String) map.get("photoIds")).split(",");
        return photoService.trash(photoIds);
    }

}