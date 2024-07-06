package com.arraywork.photowise.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping("/library")
    public String library(Model model) {
        return "library";
    }

    @PostMapping("/library")
    @ResponseBody
    public void scan() {
        libraryService.scan();
    }

    @GetMapping("/library/status")
    @ResponseBody
    public SseEmitter status() throws IOException {
        SseEmitter emitter = new SseEmitter();

        // try {
        // while (true) {
        // System.out.println("----------------111----send log");
        // libraryService.sendLog(emitter);
        // }
        // } catch (IOException e) {
        // emitter.completeWithError(e);
        // }

        return emitter;
    }

}