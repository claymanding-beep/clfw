package com.hg.system.listener;

import com.hg.common.log.OperLogRecord;
import com.hg.system.domain.SysOperLog;
import com.hg.system.service.SysOperLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器：接收框架层 AOP 发布的日志事件并异步落库。
 */
@Component
public class OperLogListener {

    private final SysOperLogService operLogService;

    public OperLogListener(SysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    @Async
    @EventListener(OperLogRecord.class)
    public void onOperLog(OperLogRecord record) {
        SysOperLog entity = new SysOperLog();
        BeanUtils.copyProperties(record, entity);
        operLogService.save(entity);
    }
}
