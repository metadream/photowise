package com.arraywork.photowise.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.entity.ScanningStatus;
import com.arraywork.photowise.entity.SpaceInfo;
import com.arraywork.photowise.enums.LogLevel;
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
    private ChannelService channelService;
    @Resource
    private SettingService settingService;
    @Resource
    private PhotoService photoService;
    @Resource
    private ExifService exifService;

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

    /** Scan the library */
    public void startScan(ScanningOption option) throws IOException {
        String library = settingService.getLibrary();
        Assert.notNull(library, "请先设置照片库");
        File lib = new File(library);
        Assert.isTrue(lib.exists() && lib.isDirectory(), "照片库不存在或不是目录");

        if (ScanningStatus.progress > -1) return;
        ScanningStatus.progress = 0;
        watcher.scan();

        long startTime = System.currentTimeMillis();

        // Clean up invalid indexes
        if (option.isCleanIndexes()) {
            cleanIndexes(library);
        }
    }

    /** Get the library storage */
    public SpaceInfo getSpaceInfo() {
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

    /** Get the progress of the scan */
    public int getProgress() {
        return scanningProgress;
    }

    /** Abort the scan */
    public void abortScan() {
        scanningProgress = -1;
    }

    /** Get the scan logs */
    public List<ScanningLog> getLogs() {
        return scanningLogs;
    }

    /** Clear the log */
    public void clearLogs() {
        scanningLogs.clear();
    }

    /** Clean up invalid indexes */
    private void cleanIndexes(String library) {
        long startTime = System.currentTimeMillis();
        List<PhotoIndex> photos = photoService.getPhotos();
        int total = photos.size();
        int count = 0, success = 0;

        for (PhotoIndex photo : photos) {
            if (scanningProgress == -1) return;
            count++;

            // Delete indexes and thumbnails where the file path does not exist
            Path path = Path.of(library, photo.getPath());
            if (!path.toFile().exists()) {
                File thumbnail = Path.of(thumbnails, photo.getPath() + ".jpg").toFile();
                thumbnail.delete();
                photoService.delete(photo);

                ScanningLog log = new ScanningLog(LogLevel.CLEAN, total, count);
                log.setPath(photo.getPath());
                channelService.broadcast("library", log);
                scanningLogs.add(0, log);
                success++;
            }
        }

        // 完成清理
        ScanningLog log = new ScanningLog(LogLevel.FINISHED, total, success);
        log.setMessage("发现 " + total + " 个索引，清理 " + success + " 个，"
            + "共耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        channelService.broadcast("library", log);
        scanningLogs.add(0, log);
    }

}