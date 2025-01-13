package com.arraywork.photowise;

import java.io.File;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.arraywork.photowise.service.LibraryService;
import com.arraywork.vernal.helper.DirectoryWatcher;

/**
 * Library Listener
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/06/04
 */
@Component
public class LibraryListener implements DirectoryWatcher.ChangeListener {

    @Resource
    private LibraryService libraryService;

    @Override
    public void onCreate(final File file) {
        //        libraryService.buildPhotoIndex(file, count, total, false);
        if (file.isDirectory()) System.out.println("Directory created: " + file);
        else System.out.println("File created: " + file);
    }

    @Override
    public void onModify(final File file) {
        //        libraryService.buildPhotoIndex(file, count, total, true);
        if (file.isDirectory()) System.out.println("Directory modified: " + file);
        else System.out.println("File modified: " + file);
    }

    @Override
    public void onDelete(final File file) {
        // TODO delete index
        //        libraryService.buildPhotoIndex(file, count, total, false);
        if (file.isDirectory()) System.out.println("Directory deleted: " + file);
        else System.out.println("File deleted: " + file);
    }

}