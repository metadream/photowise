package com.arraywork.photowise.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.entity.ScanningInfo;
import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.entity.SpaceInfo;
import com.arraywork.photowise.enums.ScanningAction;
import com.arraywork.photowise.enums.ScanningResult;
import com.arraywork.springforce.channel.ChannelService;
import com.arraywork.springforce.filesystem.DirectoryWatcher;
import com.arraywork.springforce.util.Assert;
import com.arraywork.springforce.util.FileUtils;

/**
 * Library Service
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Service
public class LibraryService {

    @Resource
    private DirectoryWatcher watcher;
    @Resource
    private ScanningInfo scanningInfo;
    @Resource
    private ChannelService channelService;
    @Resource
    private SettingService settingService;
    @Resource
    private PhotoService photoService;

    @Value("${photowise.thumbnails}")
    private String thumbnails;

    /** Start library watcher */
    @PostConstruct
    public void startWatcher() {
        String library = settingService.getLibrary();
        if (library != null) {
            watcher.start(library);
        }
    }

    /** Stop watcher before context destroyed */
    @PreDestroy
    public void destroyWatcher() {
        watcher.stop();
    }

    /** Scan the library asynchronously */
    @Async
    public void startScan(ScanningOption option) throws IOException {
        String library = settingService.getLibrary();
        Assert.notNull(library, "请先设置照片库");
        File lib = new File(library);
        Assert.isTrue(lib.exists() && lib.isDirectory(), "照片库不存在或不是目录");

        if (scanningInfo.getProgress() > -1) return;
        scanningInfo.setProgress(0);
        long startTime = System.currentTimeMillis();

        // Clean up invalid indexes
        if (option.isCleanIndexes()) {
            cleanPhotoIndexes(library);
        }

        // Traversal file to build photo index
        List<File> files = FileUtils.walk(Path.of(library));
        int total = files.size();
        int count = 0, success = 0;
        for (File file : files) {
            if (scanningInfo.getProgress() > -1) return;
            success += buildPhotoIndex(file, ++count, total, option.isFullScan());
        }

        // Finish the scan
        ScanningLog log = scanningInfo.createLog(ScanningAction.SCAN, null, count, total);
        log.setMessage("本次扫描共发现 " + total + " 个文件，成功创建 " + success + " 个索引，"
            + "耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        scanningInfo.sendLog(log);
    }

    /** Build photo index by file */
    public int buildPhotoIndex(File file, int count, int total, boolean overwrite) {
        String library = settingService.getLibrary();
        String relativePath = file.getPath().substring(library.length());
        ScanningLog log = scanningInfo.createLog(ScanningAction.SCAN, relativePath, ++count, total);
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
            scanningInfo.sendLog(log);
        }
        return success;
    }

    /** Get the library storage */
    public SpaceInfo getSpaceInfo() {
        String library = settingService.getLibrary();
        SpaceInfo spaceInfo = new SpaceInfo();
        if (library != null) {
            File lib = new File(library);
            if (lib.exists() && lib.isDirectory()) {
                long usedSpace = photoService.getUsedSpace();
                long totalSpace = usedSpace + new File(library).getUsableSpace();
                spaceInfo.setUsedSpace(FileUtils.formatSize(usedSpace));
                spaceInfo.setTotalSpace(FileUtils.formatSize(totalSpace));
                spaceInfo.setPercent(100.0 * usedSpace / totalSpace);
            }
        }
        return spaceInfo;
    }

    /** Abort the scan */
    public void abortScan() {
        scanningInfo.setProgress(-1);
    }

    /** Clear the logs */
    public void clearLogs() {
        scanningInfo.clearLogs();
    }

    /** Clean up invalid indexes */
    private void cleanPhotoIndexes(String library) {
        long startTime = System.currentTimeMillis();
        List<PhotoIndex> photos = photoService.getPhotos();
        int total = photos.size();
        int count = 0, success = 0;

        // Traverse photo indexes
        for (PhotoIndex photo : photos) {
            if (scanningInfo.getProgress() == -1) return;
            count++;

            String photoPath = photo.getPath();
            Path path = Path.of(library, photoPath);
            if (path.toFile().exists()) continue;

            // Delete indexes and thumbnails where the file path does not exist
            ScanningLog log = scanningInfo.createLog(ScanningAction.PURGE, photoPath, count, total);
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
                scanningInfo.sendLog(log);
            }
        }

        // Finished
        ScanningLog log = scanningInfo.createLog(ScanningAction.PURGE, null, count, total);
        log.setResult(ScanningResult.FINISHED);
        log.setMessage("本次扫描共发现 " + total + " 个索引，成功清理 " + success + " 个，"
            + "耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        scanningInfo.sendLog(log);
    }

}