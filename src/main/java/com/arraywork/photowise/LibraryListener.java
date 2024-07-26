package com.arraywork.photowise;

import java.io.File;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.arraywork.photowise.service.LibraryService;
import com.arraywork.springforce.filesystem.DirectoryListener;

/**
 * Library Listener
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/06/04
 */
@Component
public class LibraryListener implements DirectoryListener {

    @Resource
    private LibraryService libraryService;

    @Override
    public void onScan(File file, int count, int total) {}

    @Override
    public void onAdd(final File file, int count, int total) {
        libraryService.buildPhotoIndex(file, count, total, false);
    }

    @Override
    public void onModify(final File file, int count, int total) {
        libraryService.buildPhotoIndex(file, count, total, true);
    }

    @Override
    public void onDelete(final File file, int count, int total) {
        // TODO delete index
        //        libraryService.buildPhotoIndex(file, count, total, false);
    }

}