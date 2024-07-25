package com.arraywork.photowise;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.entity.ScanningStatus;
import com.arraywork.photowise.enums.LogLevel;
import com.arraywork.photowise.service.ExifService;
import com.arraywork.photowise.service.SettingService;
import com.arraywork.springforce.channel.ChannelService;
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

    public static final String CHANNEL_NAME = "library";
    public static List<ScanningLog> scanningLogs = new ArrayList<>();

    @Resource
    private ChannelService channelService;
    @Resource
    private SettingService settingService;
    @Resource
    private ExifService exifService;

    @Override
    public void onScan(File file, int count, int total) {
        if (ScanningStatus.progress == -1) return;

        String library = settingService.getLibrary();
        String filePath = file.getPath();
        String relativePath = filePath.substring(library.length()); // TODO 是否已经是相对路径？

        ScanningLog log = new ScanningLog(relativePath, count, total);
        ScanningStatus.progress = log.getProgress();

        try {
            exifService.build(file, true);
            log.setLevel(LogLevel.INFO);
            success++;
        } catch (Exception e) {
            log.setLevel(LogLevel.ERROR);
            log.setMessage(e.getMessage());
            sendLog(log);
        }

        // Finish the scan
        if (count == total) {
            log = new ScanningLog(LogLevel.FINISHED, count, total);
            log.setMessage("发现 " + total + " 个文件，成功创建 " + success + " 个索引，"
                + "共耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
            sendLog(log);
            ScanningStatus.progress = -1;
        }
    }

    @Override
    public void onAdd(final File file, int count, int total) {
        System.out.println("onAdded---------------：" + file.getPath());
    }

    @Override
    public void onModify(final File file, int count, int total) {
        System.out.println("onModified---------------：" + file.getPath());
    }

    @Override
    public void onDelete(final File file, int count, int total) {
        System.out.println("onDeleted---------------：" + file.getPath());
    }

    /** Send message to channel and add log to list */
    private void sendLog(ScanningLog log) {
        channelService.broadcast(CHANNEL_NAME, log);
        scanningLogs.add(0, log);
    }

}