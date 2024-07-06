package com.arraywork.photowise.controller;

import java.io.IOException;
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

    @GetMapping("/res/**")
    public void cover(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = HttpUtils.getWildcard(request, "/res");
        resourceHandler.serve(Path.of(library, path), request, response);
    }

}