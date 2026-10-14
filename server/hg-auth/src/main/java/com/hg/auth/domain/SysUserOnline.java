package com.hg.auth.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 在线用户视图对象。
 *
 * <p>数据来源为 Sa-Token 的 Token-Session：每个登录 token 独立存一份在线元数据，
 * 支持同一账号并发登录时区分不同会话（不同 IP / 登录时间）。</p>
 */
@Data
public class SysUserOnline implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话 token（强退时使用） */
    private String tokenValue;
    /** 用户 ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 姓名 */
    private String nickname;
    /** 登录 IP */
    private String ip;
    /** 浏览器 */
    private String browser;
    /** 操作系统 */
    private String os;
    /** 登录时间（毫秒时间戳） */
    private Long loginTime;
}
