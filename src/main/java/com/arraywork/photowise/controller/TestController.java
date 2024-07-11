package com.arraywork.photowise.controller;

import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arraywork.photowise.OpenCv;

@RestController
public class TestController {

    @GetMapping("/test/video/opencv")
    public String test() {
        StopWatch sw = new StopWatch();
        sw.start();
        OpenCv.captureVideo("./video.mp4", "./video.jpg", 480);
        sw.stop();
        return "Opencv video processed: " + sw.getTotalTimeMillis() + "ms";
    }

}