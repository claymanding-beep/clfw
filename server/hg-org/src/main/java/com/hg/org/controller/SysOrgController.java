package com.hg.org.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.hg.common.core.R;
import com.hg.common.log.BusinessType;
import com.hg.framework.annotation.OperLog;
import com.hg.org.domain.SysOrg;
import com.hg.org.service.SysOrgService;
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
 * 机构管理。
 */
@Tag(name = "系统管理-机构")
@RestController
@RequestMapping("/system/org")
public class SysOrgController {

    private final SysOrgService orgService;

    public SysOrgController(SysOrgService orgService) {
        this.orgService = orgService;
    }

    @Operation(summary = "查询机构树")
    @SaCheckPermission("system:org:list")
    @GetMapping("/tree")
    public R<List<SysOrg>> tree() {
        return R.ok(orgService.selectOrgTree());
    }

    @Operation(summary = "获取机构详情")
    @SaCheckPermission("system:org:query")
    @GetMapping("/{id}")
    public R<SysOrg> getInfo(@PathVariable Long id) {
        return R.ok(orgService.getById(id));
    }

    @Operation(summary = "新增机构")
    @SaCheckPermission("system:org:add")
    @OperLog(module = "机构管理", type = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysOrg org) {
        orgService.save(org);
        return R.ok();
    }

    @Operation(summary = "修改机构")
    @SaCheckPermission("system:org:edit")
    @OperLog(module = "机构管理", type = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysOrg org) {
        orgService.updateById(org);
        return R.ok();
    }

    @Operation(summary = "删除机构")
    @SaCheckPermission("system:org:remove")
    @OperLog(module = "机构管理", type = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        orgService.removeById(id);
        return R.ok();
    }
}
