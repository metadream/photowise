package com.arraywork.photowise;

import java.io.File;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Application Context Initializer
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Component
public class ContextInitializer implements ServletContextListener {

    @Resource
    private Environment env;

    /**
     * Create the required storage directory (must be before JPA starts)
     * Auto-started order: Static Block -> ServletContextListener -> @PostConstruct
     * However, static blocks cannot get the configuration parameters
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        File dir = new File(env.getProperty("photowise.covers"));
        if (!dir.exists()) dir.mkdirs();

        dir = new File(env.getProperty("photowise.thumbnails"));
        if (!dir.exists()) dir.mkdirs();

        dir = new File(env.getProperty("photowise.trash"));
        if (!dir.exists()) dir.mkdirs();
    }

}