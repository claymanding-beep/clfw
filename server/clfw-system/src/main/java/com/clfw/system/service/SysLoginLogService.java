package com.clfw.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clfw.system.domain.SysLoginLog;

/**
 * 登录日志服务。
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 记录一条登录日志。
     */
    void record(String username, String ip, String browser, String os, Integer status, String msg);
}
