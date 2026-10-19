package com.clfw.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clfw.common.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 系统角色。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /** 角色标识（权限字符串），如 super_admin */
    @NotBlank(message = "角色标识不能为空")
    private String roleKey;

    /** 显示顺序 */
    private Integer roleSort;

    /**
     * 数据范围：1 全部数据 2 本机构及以下 3 本机构 4 本科室 5 仅本人 6 自定义
     */
    private Integer dataScope;

    /** 状态（1 正常 0 停用） */
    private Integer status;

    /** 菜单 ID 列表（非数据库字段，用于分配权限） */
    @TableField(exist = false)
    private List<Long> menuIds;
}
