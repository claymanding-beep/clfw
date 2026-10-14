package com.hg.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hg.system.domain.SysConfig;

/**
 * 参数配置服务。
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 根据参数键名获取参数值。
     */
    String getConfigValue(String configKey);
}
