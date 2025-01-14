package com.arraywork.photowise;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.enums.ScanningEvent;
import com.arraywork.photowise.enums.ScanningResult;
import com.arraywork.photowise.service.PhotoService;
import com.arraywork.photowise.service.ScanningService;
import com.arraywork.photowise.service.SettingService;
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
    private SettingService settingService;
    @Resource
    private PhotoService photoService;
    @Resource
    private ScanningService scanningService;

    @Value("${photowise.thumbnails}")
    private String thumbnails;

    @Override
    public void onScan(List<File> files, Object objOption) {
        if (scanningService.isScanning()) return;
        scanningService.reset();

        // Clean up the invalid indexes
        ScanningOption option = (ScanningOption) objOption;
        if (option.isCleanInvalidIndexes()) {
            cleanPhotoIndexes();
        }
        // Traverse to build photo indexes
        long startTime = System.currentTimeMillis();
        int total = files.size();
        int count = 0, success = 0;
        for (File file : files) {
            if (!scanningService.isScanning()) return;
            success += buildPhotoIndex(file, count, total, option.isForceOverwriteIndex());
        }
        // Finish the scan
        ScanningLog log = scanningService.createLog(ScanningEvent.SCAN, null, count, total);
        log.setMessage("本次扫描共发现 " + total + " 个文件，成功创建 " + success + " 个索引，"
            + "耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        scanningService.sendLog(log);
    }

    @Override
    public void onCreate(final File file) {
        if (file.isFile()) buildPhotoIndex(file, 1, 1, false);
    }

    @Override
    public void onModify(final File file) {
        if (file.isFile()) buildPhotoIndex(file, 1, 1, true);
    }

    @Override
    public void onDelete(final File file) {
        // TODO delete index
        if (file.isFile()) buildPhotoIndex(file, 1, 1, false);
    }

    /** Build photo index by file */
    private int buildPhotoIndex(File file, int count, int total, boolean overwrite) {
        String library = settingService.getLibrary().toString();
        String relativePath = file.getPath().substring(library.length());
        ScanningLog log = scanningService.createLog(ScanningEvent.SCAN, relativePath, ++count, total);
        int success = 0;

        try {
            PhotoIndex photo = photoService.build(file, overwrite);
            if (photo != null) {
                log.setResult(ScanningResult.SUCCESS);
                success++;
            } else {
                log.setResult(ScanningResult.SKIPPED);
            }
        } catch (Exception e) {
            log.setResult(ScanningResult.FAILED);
            log.setMessage(e.getMessage());
        } finally {
            scanningService.sendLog(log);
        }
        return success;
    }

    /** Clean up invalid indexes */
    public void cleanPhotoIndexes() {
        long startTime = System.currentTimeMillis();
        Path library = settingService.getLibrary();
        List<PhotoIndex> photos = photoService.getPhotos();
        int total = photos.size();
        int count = 0, success = 0;

        // Traverse photo indexes
        for (PhotoIndex photo : photos) {
            if (!scanningService.isScanning()) return;
            count++;
            String photoPath = photo.getPath();
            Path fullPath = library.resolve(photoPath);
            if (fullPath.toFile().exists()) continue;

            // Delete indexes and thumbnails where the file path does not exist
            ScanningLog log = scanningService.createLog(ScanningEvent.PURGE, photoPath, count, total);
            try {
                File thumbnail = Path.of(thumbnails, photoPath + ".jpg").toFile();
                thumbnail.delete();
                photoService.delete(photo);
                log.setResult(ScanningResult.SUCCESS);
                success++;
            } catch (Exception e) {
                log.setMessage(e.getMessage());
                log.setResult(ScanningResult.FAILED);
            } finally {
                scanningService.sendLog(log);
            }
        }

        // Finished
        ScanningLog log = scanningService.createLog(ScanningEvent.PURGE, null, count, total);
        log.setResult(ScanningResult.FINISHED);
        log.setMessage("本次扫描共发现 " + total + " 个索引，成功清理 " + success + " 个，"
            + "耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        scanningService.sendLog(log);
    }

}