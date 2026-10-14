package com.hg.common.utils;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 对称加密工具（AES），用于手机号等敏感字段的落库加密。
 *
 * <p>采用 AES/ECB/PKCS5Padding —— 确定性加密（相同明文得到相同密文），
 * 以支持按手机号精确匹配查询。密钥默认内置，可由 {@code hg.crypto.aes-key} 配置覆盖，
 * 由 {@code CryptoInitializer} 在启动时注入。ECB 对短字段（手机号）足够，
 * 且避免 IV 管理带来的密文不可比对问题。</p>
 *
 * <p>{@link #decrypt(String)} 对无法解密的值原样返回，以兼容历史明文数据。</p>
 */
public class AesUtils {

    /** 默认密钥（16 字节）。生产环境应通过配置或环境变量覆盖，且各服务保持一致。 */
    private static final String DEFAULT_KEY = "HgHealth@2026Key";

    /** SymmetricCrypto 内部对 Cipher 操作加锁，线程安全，可作为静态单例共享。 */
    private static volatile SymmetricCrypto crypto = build(DEFAULT_KEY);

    private AesUtils() {
    }

    /**
     * 设置加密密钥（空白则忽略，保留默认）。非 16/24/32 字节的密钥将经 SHA-256 归一化为 16 字节。
     */
    public static void setKey(String key) {
        if (key != null && !key.isBlank()) {
            crypto = build(key.trim());
        }
    }

    private static SymmetricCrypto build(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            keyBytes = Arrays.copyOf(DigestUtil.sha256(key), 16);
        }
        // 用 Hutool AES(Mode, Padding, key) 构造，内部以 "AES" 生成 SecretKeySpec；
        // 若直接传 "AES/ECB/PKCS5Padding" 会抛 InvalidKeyException: Wrong algorithm: AES or Rijndael required
        return new AES(Mode.ECB, Padding.PKCS5Padding, keyBytes);
    }

    /**
     * 加密明文，返回 Base64 密文；空值原样返回。
     */
    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        return crypto.encryptBase64(plain, StandardCharsets.UTF_8);
    }

    /**
     * 解密 Base64 密文；无法解密（如历史明文）时原样返回，保证向前兼容。
     */
    public static String decrypt(String cipher) {
        if (cipher == null || cipher.isEmpty()) {
            return cipher;
        }
        try {
            return crypto.decryptStr(cipher, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return cipher;
        }
    }
}
