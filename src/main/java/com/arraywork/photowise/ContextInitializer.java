package com.arraywork.photowise;

import java.io.File;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Application Initializer
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Component
public class ContextInitializer implements ServletContextListener {

    // storage directory (includes database and thumbnails)
    @Value("${photowise.storage}")
    private String storage;

    // Create the directory required
    // Auto-started order: Static Block -> ServletContextListener -> @PostConstruct
    // However, static blocks cannot get the configuration parameters
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        File dir = Path.of(storage, "thumbnails").toFile();
        if (!dir.exists()) dir.mkdirs();
    }

}