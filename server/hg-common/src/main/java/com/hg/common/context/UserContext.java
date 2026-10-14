package com.hg.common.context;

import com.hg.common.core.LoginUser;

/**
 * 当前登录用户上下文，基于 ThreadLocal。
 * 由框架层（拦截器 / Sa-Token）在请求进入时写入，请求结束时清理。
 * 业务代码通过此类获取当前操作人，避免直接依赖具体安全框架。
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static String getUsername() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    public static Long getOrgId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getOrgId();
    }
}
