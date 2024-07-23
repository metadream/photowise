package com.arraywork.photowise;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

/**
 * Library Listener
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/06/04
 */
public class LibraryListener extends FileAlterationListenerAdaptor {

    @Override
    public void onFileChange(final File file) {
        System.out.println("onFileChange---------------：" + file.getPath());
    }

    @Override
    public void onFileCreate(final File file) {
        System.out.println("onFileCreate---------------：" + file.getPath());
    }

    @Override
    public void onFileDelete(final File file) {
        System.out.println("onFileDelete---------------：" + file.getPath());
    }

}