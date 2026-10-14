package com.hg.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.hg.common.core.R;
import com.hg.common.log.BusinessType;
import com.hg.framework.annotation.OperLog;
import com.hg.system.domain.SysMenu;
import com.hg.system.service.SysMenuService;
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
 * 菜单管理。
 */
@Tag(name = "系统管理-菜单")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "查询菜单树")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return R.ok(menuService.selectMenuTree());
    }

    @Operation(summary = "获取菜单详情")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{id}")
    public R<SysMenu> getInfo(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @OperLog(module = "菜单管理", type = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysMenu menu) {
        menuService.save(menu);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @OperLog(module = "菜单管理", type = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:remove")
    @OperLog(module = "菜单管理", type = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        menuService.removeById(id);
        return R.ok();
    }
}
