package com.hg.auth.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.hg.common.constant.CacheConstants;
import com.hg.common.core.R;
import com.hg.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码接口。
 *
 * <p>生成 4 位线段干扰验证码，答案以 uuid 为键存入 Redis（2 分钟有效，一次性使用），
 * 前端拿到 base64 图片与 uuid，登录时回传 code + uuid 由 {@code AuthService} 校验。</p>
 *
 * <p>开关由参数 {@code sys.account.captchaEnabled} 控制，缺省视为开启。</p>
 */
@Tag(name = "验证码")
@RestController
@RequestMapping("/auth")
public class CaptchaController {

    /** 验证码开关参数键 */
    public static final String CAPTCHA_ENABLED_KEY = "sys.account.captchaEnabled";
    /** 验证码有效期（分钟） */
    private static final long CAPTCHA_EXPIRE_MINUTES = 2;

    private final RedisTemplate<String, Object> redisTemplate;
    private final SysConfigService configService;

    public CaptchaController(RedisTemplate<String, Object> redisTemplate, SysConfigService configService) {
        this.redisTemplate = redisTemplate;
        this.configService = configService;
    }

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        Map<String, Object> result = new HashMap<>();
        boolean enabled = captchaEnabled();
        result.put("captchaEnabled", enabled);
        if (!enabled) {
            return R.ok(result);
        }

        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String uuid = IdUtil.simpleUUID();
        // 答案统一小写存储，校验时忽略大小写
        redisTemplate.opsForValue().set(CacheConstants.CAPTCHA_KEY + uuid,
                captcha.getCode().toLowerCase(), CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        result.put("uuid", uuid);
        result.put("img", captcha.getImageBase64Data());
        return R.ok(result);
    }

    private boolean captchaEnabled() {
        String value = configService.getConfigValue(CAPTCHA_ENABLED_KEY);
        return value == null || value.isBlank() || Boolean.parseBoolean(value.trim());
    }
}
