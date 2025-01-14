package com.arraywork.photowise.controller;

import java.io.IOException;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.service.LibraryService;
import com.arraywork.photowise.service.ScanningService;
import com.arraywork.photowise.service.SettingService;
import com.arraywork.photowise.spa.SpaRoute;

/**
 * Library Controller
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/04
 */
@Controller
public class LibraryController {

    @Resource
    private LibraryService libraryService;
    @Resource
    private SettingService settingService;
    @Resource
    private ScanningService scanningService;

    @SpaRoute
    @GetMapping("/library")
    public String library(Model model) {
        model.addAttribute("library", settingService.getLibrary());
        model.addAttribute("scanningProgress", scanningService.getProgress());
        model.addAttribute("scanningLogs", scanningService.getLogs());
        return "library";
    }

    @PostMapping("/library")
    @ResponseBody
    public void startScan(@RequestBody ScanningOption option) throws IOException {
        libraryService.startScan(option);
    }

    @PutMapping("/library")
    @ResponseBody
    public void abortScan() {
        scanningService.abort();
    }

    @DeleteMapping("/library/logs")
    @ResponseBody
    public void clearLogs() {
        scanningService.clearLogs();
    }

}