package com.clfw.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clfw.common.core.PageQuery;
import com.clfw.common.core.PageResult;
import com.clfw.common.core.R;
import com.clfw.common.log.BusinessType;
import com.clfw.framework.annotation.OperLog;
import com.clfw.system.domain.SysRole;
import com.clfw.system.domain.SysUser;
import com.clfw.system.domain.dto.SysUserQuery;
import com.clfw.system.service.SysMenuService;
import com.clfw.system.service.SysRoleService;
import com.clfw.system.service.SysUserService;
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
 * 角色管理。
 */
@Tag(name = "系统管理-角色")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final SysUserService userService;

    public SysRoleController(SysRoleService roleService, SysMenuService menuService, SysUserService userService) {
        this.roleService = roleService;
        this.menuService = menuService;
        this.userService = userService;
    }

    @Operation(summary = "分页查询角色")
    @SaCheckPermission("system:role:list")
    @GetMapping("/page")
    public R<PageResult<SysRole>> page(PageQuery query) {
        Page<SysRole> page = roleService.page(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getRoleSort));
        return R.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "查询全部启用角色（下拉用）")
    @GetMapping("/list")
    public R<List<SysRole>> list() {
        return R.ok(roleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1).orderByAsc(SysRole::getRoleSort)));
    }

    @Operation(summary = "获取角色详情")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{id}")
    public R<SysRole> getInfo(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        if (role != null) {
            role.setMenuIds(menuService.selectMenuIdsByRoleId(id));
        }
        return R.ok(role);
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @OperLog(module = "角色管理", type = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysRole role) {
        roleService.save(role);
        if (role.getMenuIds() != null) {
            roleService.assignMenus(role.getId(), role.getMenuIds());
        }
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @OperLog(module = "角色管理", type = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysRole role) {
        roleService.updateById(role);
        if (role.getMenuIds() != null) {
            roleService.assignMenus(role.getId(), role.getMenuIds());
        }
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:remove")
    @OperLog(module = "角色管理", type = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        roleService.removeByIds(ids);
        return R.ok();
    }

    @Operation(summary = "分配菜单权限")
    @SaCheckPermission("system:role:edit")
    @OperLog(module = "角色管理", type = BusinessType.GRANT)
    @PutMapping("/{roleId}/menus")
    public R<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return R.ok();
    }

    @Operation(summary = "查询角色关联用户")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{roleId}/users")
    public R<PageResult<SysUser>> roleUsers(@PathVariable Long roleId, SysUserQuery query) {
        query.setRoleId(roleId);
        return R.ok(userService.pageUsers(query));
    }
}
