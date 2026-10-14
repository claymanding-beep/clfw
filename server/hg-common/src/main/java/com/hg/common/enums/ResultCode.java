package com.hg.common.enums;

/**
 * 统一状态码。
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "登录状态已过期，请重新登录"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "请求资源不存在"),

    /* 业务相关 1xxx */
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USERNAME_EXISTS(1004, "用户名已存在"),
    OLD_PASSWORD_ERROR(1005, "原密码不正确"),
    CAPTCHA_ERROR(1006, "验证码错误"),
    USER_ACCOUNT_LOCKED(1007, "密码错误次数过多，账号已被锁定");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
