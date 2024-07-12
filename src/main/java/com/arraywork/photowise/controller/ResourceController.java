package com.arraywork.photowise.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.arraywork.springforce.StaticResourceHandler;
import com.arraywork.springforce.util.HttpUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Resource Controller
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/06
 */
@Controller
public class ResourceController {

    @Resource
    private StaticResourceHandler resourceHandler;

    @Value("${photowise.library}")
    private String library;

    @Value("${photowise.thumbnails}")
    private String thumbnails;

    @GetMapping("/original/**")
    public void original(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveResource(request, response, library);
    }

    @GetMapping("/thumbnail/**")
    public void thumbnail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveResource(request, response, thumbnails);
    }

    private void serveResource(HttpServletRequest request, HttpServletResponse response, String root)
        throws IOException {
        String path = HttpUtils.getWildcard(request);
        path = URLDecoder.decode(path, "UTF-8");
        resourceHandler.serve(Path.of(root, path), request, response);
    }

}