package com.hg.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hg.system.domain.SysConfig;
import com.hg.system.mapper.SysConfigMapper;
import com.hg.system.service.SysConfigService;
import org.springframework.stereotype.Service;

/**
 * 参数配置服务实现。
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public String getConfigValue(String configKey) {
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey));
        return config == null ? null : config.getConfigValue();
    }
}
