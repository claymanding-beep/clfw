package com.clfw.system.domain.dto;

import com.clfw.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQuery extends PageQuery {

    /** 用户名（模糊） */
    private String username;
    /** 姓名（模糊） */
    private String nickname;
    /** 手机号（模糊） */
    private String phone;
    /** 状态 */
    private Integer status;
    /** 机构 ID */
    private Long orgId;
    /** 角色 ID */
    private Long roleId;
}
