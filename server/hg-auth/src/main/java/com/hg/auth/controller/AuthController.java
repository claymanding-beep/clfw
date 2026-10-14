package com.hg.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.hg.auth.domain.LoginBody;
import com.hg.auth.domain.UserInfoVo;
import com.hg.auth.service.AuthService;
import com.hg.common.core.R;
import com.hg.common.log.BusinessType;
import com.hg.framework.annotation.OperLog;
import com.hg.system.domain.SysMenu;
import com.hg.system.domain.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 认证接口：登录、登出、用户信息、动态路由。
 */
@Tag(name = "认证鉴权")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginBody loginBody) {
        return R.ok(authService.login(loginBody));
    }

    @Operation(summary = "登出")
    @SaCheckLogin
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "获取当前登录用户信息")
    @SaCheckLogin
    @GetMapping("/getInfo")
    public R<UserInfoVo> getInfo() {
        return R.ok(authService.getUserInfo());
    }

    @Operation(summary = "获取当前登录用户的路由菜单")
    @SaCheckLogin
    @GetMapping("/getRouters")
    public R<List<SysMenu>> getRouters() {
        return R.ok(authService.getRouters());
    }

    @Operation(summary = "获取个人信息")
    @SaCheckLogin
    @GetMapping("/profile")
    public R<SysUser> profile() {
        return R.ok(authService.getProfile());
    }

    @Operation(summary = "修改个人信息")
    @SaCheckLogin
    @OperLog(module = "个人信息", type = BusinessType.UPDATE)
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody SysUser user) {
        authService.updateProfile(user);
        return R.ok();
    }

    @Operation(summary = "修改个人密码")
    @SaCheckLogin
    @PutMapping("/profile/updatePwd")
    public R<Void> updatePwd(@RequestParam String oldPassword, @RequestParam String newPassword) {
        authService.updatePassword(oldPassword, newPassword);
        return R.ok();
    }
}
