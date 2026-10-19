package com.clfw.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clfw.system.domain.SysRole;

import java.util.List;

/**
 * 角色服务。
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 查询用户拥有的角色。
     */
    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 为角色分配菜单权限。
     */
    void assignMenus(Long roleId, List<Long> menuIds);
}
