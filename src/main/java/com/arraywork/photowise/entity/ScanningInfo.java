package com.arraywork.photowise.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.arraywork.photowise.enums.ScanningAction;
import com.arraywork.springforce.channel.ChannelService;

import lombok.Getter;
import lombok.Setter;

/**
 * Scanning Info (Singleton)
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/09
 */
@Component
public class ScanningInfo {

    private static final String CHANNEL_NAME = "library";
    private static final List<ScanningLog> logs = new ArrayList<>();

    @Setter
    @Getter
    private int progress = -1;

    @Resource
    private ChannelService channelService;

    /** Create log and set global progress */
    public ScanningLog createLog(ScanningAction action, String path, int count, int total) {
        ScanningLog log = new ScanningLog();
        progress = total > 0 && total > count ? 100 * count / total : -1;
        log.setAction(action);
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

    /** Clear all logs */
    public List<ScanningLog> getLogs() {
        return logs;
    }

    /** Clear all logs */
    public void clearLogs() {
        logs.clear();
    }

}