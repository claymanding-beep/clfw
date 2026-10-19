package com.clfw.common.constant;

/**
 * 缓存 Key 常量。
 */
public interface CacheConstants {

    /** 字典缓存前缀 */
    String DICT_CACHE_KEY = "sys:dict:";
    /** 参数配置缓存前缀 */
    String CONFIG_CACHE_KEY = "sys:config:";
    /** 登录用户权限缓存前缀 */
    String USER_PERM_KEY = "sys:perm:";
    /** 验证码缓存前缀 */
    String CAPTCHA_KEY = "sys:captcha:";
    /** 密码错误次数缓存前缀 */
    String PWD_ERR_CNT_KEY = "sys:pwd_err:";
}
