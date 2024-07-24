package com.arraywork.photowise;

import java.io.File;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.arraywork.photowise.service.LibraryService;
import com.arraywork.springforce.filewatch.FileSystemListener;

/**
 * Library Listener
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/06/04
 */
@Component
public class LibraryListener implements FileSystemListener {

    @Resource
    private LibraryService libraryService;

    @Override
    public void onStarted(File file, int count, int total) {
        System.out.println("onStarted---------------：" + file.getPath());
    }

    @Override
    public void onAdded(final File file, int count, int total) {
        System.out.println("onAdded---------------：" + file.getPath());
    }

    @Override
    public void onModified(final File file, int count, int total) {
        System.out.println("onModified---------------：" + file.getPath());
    }

    @Override
    public void onDeleted(final File file, int count, int total) {
        System.out.println("onDeleted---------------：" + file.getPath());
    }

}