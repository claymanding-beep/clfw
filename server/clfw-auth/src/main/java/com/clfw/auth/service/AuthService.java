package com.clfw.auth.service;

import com.clfw.auth.domain.LoginBody;
import com.clfw.auth.domain.UserInfoVo;
import com.clfw.system.domain.SysMenu;
import com.clfw.system.domain.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 认证服务。
 */
public interface AuthService {

    /**
     * 登录，成功后返回 token 信息。
     */
    Map<String, Object> login(LoginBody loginBody);

    /**
     * 登出。
     */
    void logout();

    /**
     * 获取当前登录用户信息（含角色与权限）。
     */
    UserInfoVo getUserInfo();

    /**
     * 获取当前登录用户的前端路由菜单树。
     */
    List<SysMenu> getRouters();

    /**
     * 获取当前登录用户的个人资料。
     */
    SysUser getProfile();

    /**
     * 修改当前登录用户的个人资料（仅允许昵称/手机号/邮箱/性别/头像）。
     */
    void updateProfile(SysUser form);

    /**
     * 修改当前登录用户的密码（需校验原密码）。
     */
    void updatePassword(String oldPassword, String newPassword);
}
