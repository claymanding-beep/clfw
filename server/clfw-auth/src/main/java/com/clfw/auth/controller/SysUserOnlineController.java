package com.clfw.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clfw.auth.domain.SysUserOnline;
import com.clfw.auth.service.SysUserOnlineService;
import com.clfw.common.core.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 在线用户监控：查看当前在线会话并支持强制下线。
 */
@Tag(name = "在线用户")
@RestController
@RequestMapping("/system/online")
public class SysUserOnlineController {

    private final SysUserOnlineService onlineService;

    public SysUserOnlineController(SysUserOnlineService onlineService) {
        this.onlineService = onlineService;
    }

    @Operation(summary = "在线用户列表")
    @SaCheckPermission("system:online:list")
    @GetMapping("/list")
    public R<List<SysUserOnline>> list(@RequestParam(required = false) String ip,
                                       @RequestParam(required = false) String username) {
        return R.ok(onlineService.list(ip, username));
    }

    @Operation(summary = "强制退出指定会话")
    @SaCheckPermission("system:online:forceLogout")
    @DeleteMapping("/{tokenValue}")
    public R<Void> forceLogout(@PathVariable String tokenValue) {
        onlineService.forceLogout(tokenValue);
        return R.ok();
    }
}
