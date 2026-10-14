package com.hg.org.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.hg.common.core.R;
import com.hg.common.log.BusinessType;
import com.hg.framework.annotation.OperLog;
import com.hg.org.domain.SysDept;
import com.hg.org.service.SysDeptService;
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
 * 科室管理。
 */
@Tag(name = "系统管理-科室")
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    private final SysDeptService deptService;

    public SysDeptController(SysDeptService deptService) {
        this.deptService = deptService;
    }

    @Operation(summary = "查询科室列表")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    public R<List<SysDept>> list(Long orgId) {
        System.err.println("asdfasdfasdfasdfasdfasdfads");
        return R.ok(deptService.selectByOrgId(orgId));
    }

    @Operation(summary = "获取科室详情")
    @SaCheckPermission("system:dept:query")
    @GetMapping("/{id}")
    public R<SysDept> getInfo(@PathVariable Long id) {
        return R.ok(deptService.getById(id));
    }

    @Operation(summary = "新增科室")
    @SaCheckPermission("system:dept:add")
    @OperLog(module = "科室管理", type = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDept dept) {
        deptService.save(dept);
        return R.ok();
    }

    @Operation(summary = "修改科室")
    @SaCheckPermission("system:dept:edit")
    @OperLog(module = "科室管理", type = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysDept dept) {
        deptService.updateById(dept);
        return R.ok();
    }

    @Operation(summary = "删除科室")
    @SaCheckPermission("system:dept:remove")
    @OperLog(module = "科室管理", type = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        deptService.removeById(id);
        return R.ok();
    }
}
