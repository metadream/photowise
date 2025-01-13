package com.arraywork.photowise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.arraywork.vernal.BaseApplication;

/**
 * Application Bootstrap
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@SpringBootApplication
public class PhotowiseApplication extends BaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotowiseApplication.class, args);
    }

}