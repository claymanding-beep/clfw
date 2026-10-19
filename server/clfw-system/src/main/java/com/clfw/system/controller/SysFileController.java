package com.clfw.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.clfw.common.core.R;
import com.clfw.framework.file.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传（MinIO 对象存储）。
 */
@Tag(name = "系统管理-文件")
@RestController
@RequestMapping("/system/file")
public class SysFileController {

    private final MinioService minioService;

    public SysFileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @Operation(summary = "上传文件")
    @SaCheckLogin
    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = minioService.upload(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("fileName", file.getOriginalFilename());
        return R.ok(result);
    }
}
