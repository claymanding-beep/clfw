package com.hg.framework.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** 服务端点，如 http://192.168.1.184:9000 */
    private String endpoint;

    /** 访问密钥 */
    private String accessKey;

    /** 私密密钥 */
    private String secretKey;

    /** 存储桶名称 */
    private String bucketName;

    /**
     * 对外访问地址前缀（用于内网上传后返回可公网访问的地址）。
     * 为空时使用 endpoint 拼接。
     */
    private String domain;

    /** 单文件大小上限（字节），默认 10MB */
    private long maxSize = 10L * 1024 * 1024;

    /** 允许上传的扩展名（小写，逗号分隔），为空表示不限制 */
    private String allowedExtensions = "jpg,jpeg,png,gif,bmp,webp,doc,docx,xls,xlsx,pdf,ppt,pptx,txt,zip,rar";
}
