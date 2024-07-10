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

    private static boolean isLoaded = false;
    // Load opencv library

    // @PostConstruct
    // public void test() {
    // System.out.println("===================================");
    // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    // }

    public static void main(String[] args) {
        SpringApplication.run(PhotowiseApplication.class, args);
    }

}