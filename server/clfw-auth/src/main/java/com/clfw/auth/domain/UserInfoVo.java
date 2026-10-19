package com.clfw.auth.domain;

import com.clfw.system.domain.SysUser;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户信息 VO，返回给前端用于初始化权限与资料。
 */
@Data
public class UserInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户基本信息 */
    private SysUser user;
    /** 角色标识集合 */
    private Set<String> roles;
    /** 权限标识集合 */
    private Set<String> permissions;
}
