package com.hg.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hg.common.core.BaseEntity;
import com.hg.framework.handler.EncryptTypeHandler;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user", autoResultMap = true)
public class SysUser extends BaseEntity {

    /** 用户名（唯一，登录账号） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（仅入参可写，出参不返回） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String nickname;

    /** 手机号（加密存储） */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像 */
    private String avatar;

    /** 性别（0 未知 1 男 2 女），字典 sys_user_sex */
    private Integer sex;

    /** 所属机构 ID */
    private Long orgId;

    /** 所属科室 ID */
    private Long deptId;

    /** 状态（1 正常 0 停用） */
    private Integer status;

    /** 最后登录 IP */
    private String loginIp;

    /** 最后登录时间 */
    private LocalDateTime loginDate;

    /** 角色 ID 列表（非数据库字段） */
    @TableField(exist = false)
    private List<Long> roleIds;

    /** 角色对象列表（非数据库字段） */
    @TableField(exist = false)
    private List<SysRole> roles;
}
