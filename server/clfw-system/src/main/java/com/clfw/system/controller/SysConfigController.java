package com.clfw.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clfw.common.core.PageQuery;
import com.clfw.common.core.PageResult;
import com.clfw.common.core.R;
import com.clfw.common.log.BusinessType;
import com.clfw.framework.annotation.OperLog;
import com.clfw.system.domain.SysConfig;
import com.clfw.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 参数设置。
 */
@Tag(name = "系统管理-参数设置")
@RestController
@RequestMapping("/system/config")
public class SysConfigController {

    private final SysConfigService configService;

    public SysConfigController(SysConfigService configService) {
        this.configService = configService;
    }

    @Operation(summary = "分页查询参数")
    @SaCheckPermission("system:config:list")
    @GetMapping("/page")
    public R<PageResult<SysConfig>> page(PageQuery query, String configGroup) {
        Page<SysConfig> page = configService.page(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<SysConfig>()
                        .eq(configGroup != null, SysConfig::getConfigGroup, configGroup)
                        .orderByAsc(SysConfig::getConfigGroup));
        return R.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "根据键名获取参数值")
    @GetMapping("/key/{configKey}")
    public R<String> getByKey(@PathVariable String configKey) {
        return R.ok(configService.getConfigValue(configKey));
    }

    @Operation(summary = "新增参数")
    @SaCheckPermission("system:config:add")
    @OperLog(module = "参数设置", type = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysConfig config) {
        configService.save(config);
        return R.ok();
    }

    @Operation(summary = "修改参数")
    @SaCheckPermission("system:config:edit")
    @OperLog(module = "参数设置", type = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysConfig config) {
        configService.updateById(config);
        return R.ok();
    }

    @Operation(summary = "删除参数")
    @SaCheckPermission("system:config:remove")
    @OperLog(module = "参数设置", type = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        configService.removeByIds(ids);
        return R.ok();
    }
}
