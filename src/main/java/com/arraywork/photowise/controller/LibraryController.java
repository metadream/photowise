package com.arraywork.photowise.controller;

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

import jakarta.annotation.Resource;

/**
 * Library Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/04
 */
@Controller
public class LibraryController {

    @Resource
    private LibraryService libraryService;

    @SpaRoute
    @GetMapping("/library")
    public String library(Model model) {
        model.addAttribute("scanningProgress", libraryService.getProgress());
        model.addAttribute("scanningLogs", libraryService.getLogs());
        return "library";
    }

    @PostMapping("/library")
    @ResponseBody
    public void startScan(@RequestBody ScanningOption option) {
        libraryService.startScan(option);
    }

    @PutMapping("/library")
    @ResponseBody
    public void abortScan() {
        libraryService.abortScan();
    }

    @DeleteMapping("/library/logs")
    @ResponseBody
    public void clearLogs() {
        libraryService.clearLogs();
    }

}