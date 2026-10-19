package com.clfw.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clfw.system.domain.SysMenu;

import java.util.List;
import java.util.Set;

/**
 * 菜单服务。
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单树（管理端使用，含目录/菜单/按钮）。
     */
    List<SysMenu> selectMenuTree();

    /**
     * 根据用户 ID 查询其可见的菜单树（目录 + 菜单），用于构建前端路由。
     * 超级管理员返回全部。
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 查询用户拥有的权限标识集合。超级管理员返回 *:*:*。
     */
    Set<String> selectPermsByUserId(Long userId);

    /**
     * 根据角色 ID 查询已分配的菜单 ID 列表。
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 将平铺菜单列表构建为树形结构。
     */
    List<SysMenu> buildTree(List<SysMenu> menus);
}
