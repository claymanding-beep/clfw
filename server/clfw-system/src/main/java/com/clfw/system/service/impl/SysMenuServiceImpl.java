package com.clfw.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clfw.common.constant.CommonConstants;
import com.clfw.system.domain.SysMenu;
import com.clfw.system.domain.SysRoleMenu;
import com.clfw.system.mapper.SysMenuMapper;
import com.clfw.system.mapper.SysRoleMenuMapper;
import com.clfw.system.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现。
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    public SysMenuServiceImpl(SysRoleMenuMapper roleMenuMapper) {
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<SysMenu> selectMenuTree() {
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getOrderNum));
        return buildTree(all);
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus;
        if (CommonConstants.SUPER_ADMIN_ID.equals(userId)) {
            menus = baseMapper.selectMenuTreeAll();
        } else {
            menus = baseMapper.selectMenuTreeByUserId(userId);
        }
        return buildTree(menus);
    }

    @Override
    public Set<String> selectPermsByUserId(Long userId) {
        Set<String> perms = new HashSet<>();
        if (CommonConstants.SUPER_ADMIN_ID.equals(userId)) {
            perms.add("*:*:*");
            return perms;
        }
        List<String> list = baseMapper.selectPermsByUserId(userId);
        for (String p : list) {
            if (p != null && !p.isBlank()) {
                perms.add(p.trim());
            }
        }
        return perms;
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        return list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    public List<SysMenu> buildTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }
        // 以 parentId 分组
        java.util.Map<Long, List<SysMenu>> parentMap = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        // 仅保留出现在结果集中的父节点作为根，避免缺失父级导致丢节点
        Set<Long> idSet = menus.stream().map(SysMenu::getId).collect(Collectors.toSet());
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
            if (parentId == 0L || !idSet.contains(parentId)) {
                roots.add(menu);
            }
        }
        roots.forEach(root -> fillChildren(root, parentMap));
        return roots;
    }

    private void fillChildren(SysMenu menu, java.util.Map<Long, List<SysMenu>> parentMap) {
        List<SysMenu> children = parentMap.get(menu.getId());
        if (children != null && !children.isEmpty()) {
            menu.setChildren(children);
            children.forEach(child -> fillChildren(child, parentMap));
        } else {
            menu.setChildren(new ArrayList<>());
        }
    }
}
