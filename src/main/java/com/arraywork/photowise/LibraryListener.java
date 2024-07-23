package com.arraywork.photowise;

import java.io.File;
import jakarta.annotation.Resource;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.springframework.stereotype.Component;

import com.arraywork.photowise.service.PhotoService;

/**
 * Library Listener
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/06/04
 */
@Component
public class LibraryListener extends FileAlterationListenerAdaptor {

    @Resource
    private PhotoService photoService;

    @Override
    public void onFileChange(final File file) {
        System.out.println("onFileChange---------------：" + file.getPath());
    }

    @Override
    public void onFileCreate(final File file) {
        photoService.save(file);
        System.out.println("onFileCreate---------------：" + file.getPath());
    }

    @Override
    public void onFileDelete(final File file) {
        System.out.println("onFileDelete---------------：" + file.getPath());
    }

}