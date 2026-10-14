package com.hg.framework.file;

import com.hg.common.enums.ResultCode;
import com.hg.common.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MinIO 文件存储服务。上传返回可直接访问的对象 URL。
 */
@Service
public class MinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioService.class);
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MinioProperties properties;
    private final MinioClient client;
    private volatile boolean bucketChecked = false;

    public MinioService(MinioProperties properties) {
        this.properties = properties;
        this.client = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    /**
     * 上传 MultipartFile，返回可访问 URL。
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "上传文件不能为空");
        }
        if (file.getSize() > properties.getMaxSize()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "文件大小超出限制（最大 " + (properties.getMaxSize() / 1024 / 1024) + "MB）");
        }
        String originalName = file.getOriginalFilename();
        String ext = extractExt(originalName);
        validateExtension(ext);
        try (InputStream in = file.getInputStream()) {
            return upload(in, file.getSize(), file.getContentType(), ext);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传字节流，返回可访问 URL。
     */
    public String upload(InputStream in, long size, String contentType, String ext) throws Exception {
        ensureBucket();
        String objectKey = buildObjectKey(ext);
        String type = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
        client.putObject(PutObjectArgs.builder()
                .bucket(properties.getBucketName())
                .object(objectKey)
                .stream(in, size, -1)
                .contentType(type)
                .build());
        return buildUrl(objectKey);
    }

    /**
     * 根据对象 URL 或 objectKey 删除文件。
     */
    public void delete(String urlOrKey) {
        if (!StringUtils.hasText(urlOrKey)) {
            return;
        }
        String objectKey = urlOrKey;
        String prefix = buildUrl("");
        if (objectKey.startsWith(prefix)) {
            objectKey = objectKey.substring(prefix.length());
        }
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("MinIO 删除失败: {}", objectKey, e);
        }
    }

    private void ensureBucket() {
        if (bucketChecked) {
            return;
        }
        synchronized (this) {
            if (bucketChecked) {
                return;
            }
            try {
                boolean exists = client.bucketExists(BucketExistsArgs.builder()
                        .bucket(properties.getBucketName()).build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder()
                            .bucket(properties.getBucketName()).build());
                    log.info("MinIO 存储桶不存在，已自动创建: {}", properties.getBucketName());
                }
                bucketChecked = true;
            } catch (Exception e) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "MinIO 存储桶初始化失败：" + e.getMessage());
            }
        }
    }

    private String buildObjectKey(String ext) {
        String dir = LocalDate.now().format(DATE_DIR);
        String name = UUID.randomUUID().toString().replace("-", "");
        return StringUtils.hasText(ext) ? "upload/" + dir + "/" + name + "." + ext : "upload/" + dir + "/" + name;
    }

    private String buildUrl(String objectKey) {
        String base = StringUtils.hasText(properties.getDomain())
                ? properties.getDomain() : properties.getEndpoint();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + properties.getBucketName() + "/" + objectKey;
    }

    private String extractExt(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1).toLowerCase(Locale.ROOT) : "";
    }

    private void validateExtension(String ext) {
        String allowed = properties.getAllowedExtensions();
        if (!StringUtils.hasText(allowed)) {
            return;
        }
        Set<String> allowSet = Arrays.stream(allowed.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (!allowSet.contains(ext)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "不支持的文件类型：" + (StringUtils.hasText(ext) ? ext : "未知"));
        }
    }
}
