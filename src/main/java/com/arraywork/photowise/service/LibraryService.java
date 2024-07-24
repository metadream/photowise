package com.arraywork.photowise.service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.entity.SpaceInfo;
import com.arraywork.photowise.enums.LogLevel;
import com.arraywork.springforce.channel.ChannelService;
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

    public static int scanningProgress = -1;
    public static List<ScanningLog> scanningLogs = new ArrayList<>();

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

    /** Scan the library asynchronously */
    @Async
    public void startScan(ScanningOption option) {
        String library = settingService.getLibrary();
        if (library == null) {
            ScanningLog log = new ScanningLog(LogLevel.ERROR, 0, 0);
            log.setMessage("请先设置照片库");
            channelService.broadcast("library", log);
            scanningLogs.add(log);
            return;
        }
        File lib = new File(library);
        if (!lib.exists() || !lib.isDirectory()) {
            ScanningLog log = new ScanningLog(LogLevel.ERROR, 0, 0);
            log.setMessage("照片库不存在或不是目录");
            channelService.broadcast("library", log);
            scanningLogs.add(log);
            return;
        }

        // Start Scanning...
        if (scanningProgress > -1) return;
        scanningProgress = 0;
        long startTime = System.currentTimeMillis();

        // Clean up invalid indexes
        if (option.isCleanIndexes()) {
            cleanIndexes(library);
        }

        // Get all the files in the library
        List<File> files = new ArrayList<>();
        FileUtils.walk(lib, files);
        int total = files.size();
        int count = 0, success = 0;

        // Traverse the files
        for (File file : files) {
            if (scanningProgress == -1) return;
            String filePath = file.getPath();
            String relativePath = filePath.substring(library.length());

            ScanningLog log = new ScanningLog(LogLevel.INFO, total, ++count);
            log.setPath(relativePath);
            scanningProgress = log.getProgress();

            // Determines whether to skip the scan based on the scan parameters, file size, and modified time
            PhotoIndex _photo = photoService.getPhotoByPath(relativePath);
            if (!option.isFullScan() && _photo != null
                && _photo.getFileLength() == file.length()
                && _photo.getModifiedTime() == file.lastModified()) {
                continue;
            }

            try {
                // Build photo index file by file
                String photoId = _photo != null ? _photo.getId() : null;
                exifService.build(file, photoId);
                success++;
            } catch (Exception e) {
                log.setLevel(LogLevel.ERROR);
                log.setMessage(e.getMessage());
                channelService.broadcast("library", log);
                scanningLogs.add(0, log);
            }
        }

        // Finish the scan
        ScanningLog log = new ScanningLog(LogLevel.FINISHED, total, count);
        log.setMessage("发现 " + total + " 个文件，成功创建 " + success + " 个索引，"
            + "共耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        channelService.broadcast("library", log);
        scanningLogs.add(0, log);
        scanningProgress = -1;
    }

    /** Get the library storage */
    public SpaceInfo getSpaceInfo() {
        SpaceInfo spaceInfo = new SpaceInfo();
        String library = settingService.getLibrary();

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