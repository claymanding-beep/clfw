package com.clfw.system.service;

import com.clfw.system.mapper.SysLoginLogMapper;
import com.clfw.system.mapper.SysOperLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 日志归档服务：将超过保留期的操作/登录日志转移到归档表并从主表删除。
 */
@Service
public class SysLogArchiveService {

    private static final Logger log = LoggerFactory.getLogger(SysLogArchiveService.class);

    private final SysOperLogMapper operLogMapper;
    private final SysLoginLogMapper loginLogMapper;

    public SysLogArchiveService(SysOperLogMapper operLogMapper, SysLoginLogMapper loginLogMapper) {
        this.operLogMapper = operLogMapper;
        this.loginLogMapper = loginLogMapper;
    }

    /**
     * 归档保留天数之前的日志。
     *
     * @param retentionDays 保留天数，早于「今天 - retentionDays」零点的日志将被归档
     * @return 各类型归档行数统计
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> archiveBefore(int retentionDays) {
        LocalDateTime cutoff = LocalDate.now().minusDays(retentionDays).atStartOfDay();
        int operArchived = operLogMapper.archiveBefore(cutoff);
        int operDeleted = operLogMapper.deleteBefore(cutoff);
        int loginArchived = loginLogMapper.archiveBefore(cutoff);
        int loginDeleted = loginLogMapper.deleteBefore(cutoff);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cutoff", cutoff.toString());
        result.put("operArchived", operArchived);
        result.put("operDeleted", operDeleted);
        result.put("loginArchived", loginArchived);
        result.put("loginDeleted", loginDeleted);
        log.info("日志归档完成: cutoff={}, 操作日志归档={}条, 登录日志归档={}条", cutoff, operArchived, loginArchived);
        return result;
    }
}
