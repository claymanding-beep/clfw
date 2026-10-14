package com.hg.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 日志归档配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "hg.log.archive")
public class LogArchiveProperties {

    /** 是否启用定时归档，默认关闭 */
    private boolean enabled = false;

    /** 日志保留天数，超过该天数的日志将被归档并从主表删除 */
    private int retentionDays = 90;
}
