package com.arraywork.photowise;

import java.io.File;
import java.util.List;
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
    public void onScan(List<File> files) {
        int total = files.size();
        int count = 0, success = 0;
        for (File file : files) {
            if (scanningInfo.getProgress() > -1) return;
            success += buildPhotoIndex(file, ++count, total, option.isFullScan());
        }

        // Finish the scan
        //        ScanningLog log = scanningInfo.createLog(ScanningAction.SCAN, null, count, total);
        //        log.setMessage("本次扫描共发现 " + total + " 个文件，成功创建 " + success + " 个索引，"
        //            + "耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        //        scanningInfo.sendLog(log);
    }

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