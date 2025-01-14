package com.arraywork.photowise.service;

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.enums.ScanningEvent;
import com.arraywork.vernal.channel.ChannelService;

import lombok.Getter;
import lombok.Setter;

/**
 * Scanning Info (Singleton)
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/09
 */
@Service
public class ScanningService {

    private static final String CHANNEL_NAME = "library";
    private static final List<ScanningLog> logs = new ArrayList<>();

    @Setter
    @Getter
    private int progress = -1;

    @Resource
    private ChannelService channelService;

    public boolean isScanning() {
        return progress == -1;
    }

    public void abort() {
        setProgress(-1);
    }

    public void reset() {
        setProgress(0);
    }

    /** Create log and set global progress */
    public ScanningLog createLog(ScanningEvent event, String path, int count, int total) {
        ScanningLog log = new ScanningLog();
        progress = total > 0 && total >= count ? 100 * count / total : -1;
        log.setEvent(event);
        log.setPath(path);
        log.setCount(count);
        log.setTotal(total);
        log.setProgress(progress);
        return log;
    }

    /** Send log to channel and add log to list */
    public void sendLog(ScanningLog log) {
        channelService.broadcast(CHANNEL_NAME, log);
        logs.add(0, log);
    }

    /** Get all logs */
    public List<ScanningLog> getLogs() {
        return logs;
    }

    /** Clear all logs */
    public void clearLogs() {
        logs.clear();
    }

}