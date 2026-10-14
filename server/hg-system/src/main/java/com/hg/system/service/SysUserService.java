package com.hg.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hg.common.core.PageResult;
import com.hg.system.domain.SysUser;
import com.hg.system.domain.dto.SysUserQuery;

import java.util.List;

/**
 * 用户服务。
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户。
     */
    PageResult<SysUser> pageUsers(SysUserQuery query);

    /**
     * 根据用户名查询用户（登录用）。
     */
    SysUser getByUsername(String username);

    /**
     * 新增用户（含角色关联），密码自动加密。
     */
    void createUser(SysUser user);

    /**
     * 修改用户（含角色关联）。
     */
    void updateUser(SysUser user);

    /**
     * 重置密码。
     */
    void resetPassword(Long userId, String rawPassword);

    /**
     * 查询用户已分配的角色 ID 列表。
     */
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 为用户分配角色。
     */
    void assignRoles(Long userId, List<Long> roleIds);
}
