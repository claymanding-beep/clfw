package com.clfw.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clfw.system.domain.SysOperLog;
import com.clfw.system.mapper.SysOperLogMapper;
import com.clfw.system.service.SysOperLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现。
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {
}
