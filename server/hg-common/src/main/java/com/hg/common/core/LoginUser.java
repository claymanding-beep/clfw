package com.hg.common.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户上下文信息。
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 姓名 */
    private String nickname;
    /** 所属机构 ID */
    private Long orgId;
    /** 所属科室 ID */
    private Long deptId;
    /** 数据范围 */
    private Integer dataScope;
    /** 角色标识集合 */
    private Set<String> roles;
    /** 权限标识集合 */
    private Set<String> permissions;

    public boolean isSuperAdmin() {
        return userId != null && userId == 1L;
    }
}
