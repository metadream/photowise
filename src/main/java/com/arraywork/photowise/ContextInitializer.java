package com.arraywork.photowise;

import java.io.File;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.arraywork.photowise.service.SettingService;

/**
 * Application Context Initializer
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Component
public class ContextInitializer implements ServletContextListener {

    private FileAlterationMonitor libraryMonitor;
    @Resource
    private Environment env;
    @Resource
    private SettingService settingService;

    /**
     * Create the required storage directories (must be before JPA starts)
     * and start library monitor
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

        // Start library monitor
        String library = settingService.getSetting().getLibrary();
        FileAlterationObserver observer = new FileAlterationObserver(library);
        observer.addListener(new LibraryListener());
        libraryMonitor = new FileAlterationMonitor(3000);
        libraryMonitor.addObserver(observer);
        try {
            libraryMonitor.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Stop monitor when context destroyed */
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            libraryMonitor.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}