package com.arraywork.photowise;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import com.arraywork.springforce.BaseApplication;
import com.arraywork.springforce.util.OpenCv;

/**
 * Application Bootstrap
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
public class PhotowiseApplication extends BaseApplication {

    @Value("${app.lib.opencv}")
    private String opencvLib;

    public static void main(String[] args) {
        SpringApplication.run(PhotowiseApplication.class, args);
    }

    @PostConstruct
    public void loadOpenCvLibrary() {
        OpenCv.loadLibrary(opencvLib);
    }

}