package com.arraywork.photowise;

import java.io.File;
import java.nio.file.Path;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Application Context Initializer
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Component
public class ContextInitializer implements ServletContextListener {

    @Resource
    private Environment env;

    // Create the storage directory required (and it must before JPA startup)
    // Auto-started order: Static Block -> ServletContextListener -> @PostConstruct
    // However, static blocks cannot get the configuration parameters
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String storage = env.getProperty("photowise.storage");
        File dir = Path.of(storage, "thumbnails").toFile();
        if (!dir.exists()) dir.mkdirs();
    }

}