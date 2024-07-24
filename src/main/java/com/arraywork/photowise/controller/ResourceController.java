package com.arraywork.photowise.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.arraywork.photowise.service.SettingService;
import com.arraywork.springforce.StaticResourceHandler;
import com.arraywork.springforce.util.HttpUtils;

/**
 * Resource Controller
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@Controller
public class ResourceController {

    @Resource
    private StaticResourceHandler resourceHandler;
    @Resource
    private SettingService settingService;

    @Value("${photowise.thumbnails}")
    private String thumbnails;

    @GetMapping("/original/**")
    public void original(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String library = settingService.getLibrary();
        String path = HttpUtils.getWildcard(request);
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        resourceHandler.serve(Path.of(library, path), request, response);
    }

    @GetMapping("/thumbnail/**")
    public void thumbnail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = HttpUtils.getWildcard(request) + ".jpg";
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        resourceHandler.serve(Path.of(thumbnails, path), request, response);
    }

}