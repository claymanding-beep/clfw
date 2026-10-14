package com.hg.system.task;

import com.hg.system.config.LogArchiveProperties;
import com.hg.system.service.SysLogArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 日志归档定时任务：按配置的保留天数定期将历史日志归档。
 * 通过 hg.log.archive.enabled 控制是否执行，默认关闭。
 */
@Component
public class SysLogArchiveTask {

    private static final Logger log = LoggerFactory.getLogger(SysLogArchiveTask.class);

    private final SysLogArchiveService archiveService;
    private final LogArchiveProperties properties;

    public SysLogArchiveTask(SysLogArchiveService archiveService, LogArchiveProperties properties) {
        this.archiveService = archiveService;
        this.properties = properties;
    }

    @Scheduled(cron = "${hg.log.archive.cron:0 0 2 * * ?}")
    public void run() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            Map<String, Object> result = archiveService.archiveBefore(properties.getRetentionDays());
            log.info("定时日志归档执行完成: {}", result);
        } catch (Exception e) {
            log.error("定时日志归档执行失败", e);
        }
    }
}
