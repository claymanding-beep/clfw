package com.clfw.framework.config;

import com.clfw.common.utils.AesUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 加密密钥初始化器：启动时把配置项 {@code clfw.crypto.aes-key} 注入 {@link AesUtils}。
 *
 * <p>未配置时保留 {@link AesUtils} 内置默认密钥。生产环境务必通过配置/环境变量覆盖，
 * 且所有加载加密实体的服务使用同一密钥，否则跨服务无法解密。</p>
 */
@Component
public class CryptoInitializer {

    @Value("${clfw.crypto.aes-key:}")
    private String aesKey;

    @PostConstruct
    public void init() {
        AesUtils.setKey(aesKey);
    }
}
