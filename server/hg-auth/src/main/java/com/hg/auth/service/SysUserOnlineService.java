package com.hg.auth.service;

import com.hg.auth.domain.SysUserOnline;

import java.util.List;

/**
 * 在线用户服务。
 *
 * <p>接口内定义的 {@code TS_*} 常量为在线元数据在 Sa-Token Token-Session 中的存储键，
 * 由登录流程（{@code AuthServiceImpl}）写入、由本服务读取，二者共用同一组键以保证一致。</p>
 */
public interface SysUserOnlineService {

    String TS_USER_ID = "online_userId";
    String TS_USERNAME = "online_username";
    String TS_NICKNAME = "online_nickname";
    String TS_IP = "online_ip";
    String TS_BROWSER = "online_browser";
    String TS_OS = "online_os";
    String TS_LOGIN_TIME = "online_loginTime";

    /**
     * 查询在线用户列表，支持按 IP、用户名模糊过滤。
     */
    List<SysUserOnline> list(String ip, String username);

    /**
     * 强制退出指定 token 的会话。
     */
    void forceLogout(String tokenValue);
}
