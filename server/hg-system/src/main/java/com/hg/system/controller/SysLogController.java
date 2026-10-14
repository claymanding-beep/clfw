package com.hg.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hg.common.core.PageQuery;
import com.hg.common.core.PageResult;
import com.hg.common.core.R;
import com.hg.common.log.BusinessType;
import com.hg.framework.annotation.OperLog;
import com.hg.system.domain.SysLoginLog;
import com.hg.system.domain.SysOperLog;
import com.hg.system.service.SysLoginLogService;
import com.hg.system.service.SysLogArchiveService;
import com.hg.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 日志管理：操作日志 + 登录日志。
 */
@Tag(name = "系统管理-日志")
@RestController
@RequestMapping("/system/log")
public class SysLogController {

    private final SysOperLogService operLogService;
    private final SysLoginLogService loginLogService;
    private final SysLogArchiveService archiveService;

    public SysLogController(SysOperLogService operLogService, SysLoginLogService loginLogService,
                            SysLogArchiveService archiveService) {
        this.operLogService = operLogService;
        this.loginLogService = loginLogService;
        this.archiveService = archiveService;
    }

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("system:operlog:list")
    @GetMapping("/oper/page")
    public R<PageResult<SysOperLog>> operPage(PageQuery query, String module, String operBy, Integer status,
                                              String beginTime, String endTime) {
        LocalDateTime begin = parseTime(beginTime, true);
        LocalDateTime end = parseTime(endTime, false);
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>()
                .like(StringUtils.hasText(module), SysOperLog::getModule, module)
                .like(StringUtils.hasText(operBy), SysOperLog::getOperBy, operBy)
                .eq(status != null, SysOperLog::getStatus, status)
                .ge(begin != null, SysOperLog::getOperTime, begin)
                .le(end != null, SysOperLog::getOperTime, end)
                .orderByDesc(SysOperLog::getOperTime);
        Page<SysOperLog> page = operLogService.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "清空操作日志")
    @SaCheckPermission("system:operlog:remove")
    @OperLog(module = "操作日志", type = BusinessType.CLEAN)
    @DeleteMapping("/oper/clean")
    public R<Void> cleanOper() {
        operLogService.remove(new LambdaQueryWrapper<>());
        return R.ok();
    }

    @Operation(summary = "分页查询登录日志")
    @SaCheckPermission("system:loginlog:list")
    @GetMapping("/login/page")
    public R<PageResult<SysLoginLog>> loginPage(PageQuery query, String username, String ip, Integer status,
                                                String beginTime, String endTime) {
        LocalDateTime begin = parseTime(beginTime, true);
        LocalDateTime end = parseTime(endTime, false);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .like(StringUtils.hasText(username), SysLoginLog::getUsername, username)
                .like(StringUtils.hasText(ip), SysLoginLog::getIp, ip)
                .eq(status != null, SysLoginLog::getStatus, status)
                .ge(begin != null, SysLoginLog::getLoginTime, begin)
                .le(end != null, SysLoginLog::getLoginTime, end)
                .orderByDesc(SysLoginLog::getLoginTime);
        Page<SysLoginLog> page = loginLogService.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "清空登录日志")
    @SaCheckPermission("system:loginlog:remove")
    @OperLog(module = "登录日志", type = BusinessType.CLEAN)
    @DeleteMapping("/login/clean")
    public R<Void> cleanLogin() {
        loginLogService.remove(new LambdaQueryWrapper<>());
        return R.ok();
    }

    @Operation(summary = "操作日志详情")
    @SaCheckPermission("system:operlog:query")
    @GetMapping("/oper/{id}")
    public R<SysOperLog> operDetail(@PathVariable Long id) {
        return R.ok(operLogService.getById(id));
    }

    @Operation(summary = "手动归档历史日志")
    @SaCheckPermission("system:operlog:remove")
    @OperLog(module = "日志管理", type = BusinessType.CLEAN)
    @PostMapping("/archive")
    public R<Map<String, Object>> archive(@RequestParam(required = false) Integer days) {
        int retention = (days != null && days >= 0) ? days : 90;
        return R.ok(archiveService.archiveBefore(retention));
    }

    /**
     * 解析时间范围边界。支持 yyyy-MM-dd 与 yyyy-MM-dd HH:mm:ss 两种格式，
     * 仅日期时：起始取当天 00:00:00，结束取当天 23:59:59.999999999。
     */
    private LocalDateTime parseTime(String value, boolean start) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        try {
            if (text.length() <= 10) {
                LocalDate date = LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return start ? date.atStartOfDay() : date.atTime(23, 59, 59);
            }
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}
