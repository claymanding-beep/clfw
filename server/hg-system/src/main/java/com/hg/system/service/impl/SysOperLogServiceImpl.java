package com.hg.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hg.system.domain.SysOperLog;
import com.hg.system.mapper.SysOperLogMapper;
import com.hg.system.service.SysOperLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现。
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {
}
