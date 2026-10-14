package com.hg.common.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码加密工具，使用 BCrypt。
 */
public class PasswordUtils {

    private PasswordUtils() {
    }

    /**
     * 加密明文密码。
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码与密文是否匹配。
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
