package com.hg.mobile.controller;

import com.hg.common.core.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 移动端健康检查占位接口。
 */
@Tag(name = "移动端-基础")
@RestController
@RequestMapping("/mobile")
public class MobileHealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "hg-mobile-api");
        data.put("status", "UP");
        data.put("time", LocalDateTime.now());
        return R.ok(data);
    }
}
