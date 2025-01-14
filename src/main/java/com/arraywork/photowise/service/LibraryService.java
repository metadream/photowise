package com.arraywork.photowise.service;

import java.io.IOException;
import java.nio.file.Path;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.entity.SpaceInfo;
import com.arraywork.vernal.helper.DirectoryWatcher;
import com.arraywork.vernal.util.FileUtils;

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
    private DirectoryWatcher directoryWatcher;
    @Resource
    private SettingService settingService;
    @Resource
    private PhotoService photoService;

    /** Start library watcher */
    @PostConstruct
    public void startWatcher() throws IOException {
        directoryWatcher.start(settingService.getLibrary(), true);
    }

    /** Stop watcher before context destroyed */
    @PreDestroy
    public void destroyWatcher() throws IOException {
        directoryWatcher.stop();
    }

    /** Scan the library */
    public void startScan(ScanningOption option) throws IOException {
        directoryWatcher.scan(option);
    }

    /** Get the library storage */
    public SpaceInfo getSpaceInfo() {
        SpaceInfo spaceInfo = new SpaceInfo();
        Path library = settingService.getLibrary();
        long usedSpace = photoService.getUsedSpace();
        long totalSpace = usedSpace + library.toFile().getUsableSpace();
        spaceInfo.setUsedSpace(FileUtils.formatSize(usedSpace));
        spaceInfo.setTotalSpace(FileUtils.formatSize(totalSpace));
        spaceInfo.setPercent(100.0 * usedSpace / totalSpace);
        return spaceInfo;
    }

}