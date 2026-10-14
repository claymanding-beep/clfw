package com.hg.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.hg.common.core.PageResult;
import com.hg.common.core.R;
import com.hg.common.log.BusinessType;
import com.hg.framework.annotation.OperLog;
import com.hg.system.domain.SysUser;
import com.hg.system.domain.dto.SysUserQuery;
import com.hg.system.service.SysUserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理。
 */
@Tag(name = "系统管理-用户")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "分页查询用户")
    @SaCheckPermission("system:user:list")
    @GetMapping("/page")
    public R<PageResult<SysUser>> page(SysUserQuery query) {
        return R.ok(userService.pageUsers(query));
    }

    @Operation(summary = "获取用户详情")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{id}")
    public R<SysUser> getInfo(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setRoleIds(userService.selectRoleIdsByUserId(id));
        }
        return R.ok(user);
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @OperLog(module = "用户管理", type = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysUser user) {
        userService.createUser(user);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @OperLog(module = "用户管理", type = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysUser user) {
        userService.updateUser(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:remove")
    @OperLog(module = "用户管理", type = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        userService.removeByIds(ids);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @SaCheckPermission("system:user:resetPwd")
    @OperLog(module = "用户管理", type = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestParam Long userId, @RequestParam(required = false) String password) {
        userService.resetPassword(userId, password);
        return R.ok();
    }

    @Operation(summary = "修改用户状态")
    @SaCheckPermission("system:user:edit")
    @OperLog(module = "用户管理", type = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestParam Long userId, @RequestParam Integer status) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        userService.updateById(user);
        return R.ok();
    }
}
