package com.clfw.auth.security;

import cn.dev33.satoken.stp.StpInterface;
import com.clfw.common.constant.CommonConstants;
import com.clfw.system.domain.SysRole;
import com.clfw.system.service.SysMenuService;
import com.clfw.system.service.SysRoleService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限数据源：为鉴权注解提供用户的权限码与角色码。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysMenuService menuService;
    private final SysRoleService roleService;

    public StpInterfaceImpl(SysMenuService menuService, SysRoleService roleService) {
        this.menuService = menuService;
        this.roleService = roleService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        Set<String> perms = menuService.selectPermsByUserId(userId);
        return new ArrayList<>(perms);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        if (CommonConstants.SUPER_ADMIN_ID.equals(userId)) {
            return List.of(CommonConstants.SUPER_ADMIN_KEY);
        }
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        return roles.stream().map(SysRole::getRoleKey).collect(Collectors.toList());
    }
}
