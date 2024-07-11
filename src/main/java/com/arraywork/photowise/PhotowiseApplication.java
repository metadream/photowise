package com.arraywork.photowise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import com.arraywork.springforce.BaseApplication;

/**
 * Application Bootstrap
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
public class PhotowiseApplication extends BaseApplication {

    public static void main(String[] args) {
        OpenCv.loadLibrary();
        SpringApplication.run(PhotowiseApplication.class, args);
    }

}