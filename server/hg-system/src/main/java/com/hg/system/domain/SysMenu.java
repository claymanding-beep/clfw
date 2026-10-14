package com.hg.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hg.common.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统菜单（目录 / 菜单 / 按钮）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父菜单 ID，顶级为 0 */
    private Long parentId;

    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /** 菜单类型：M 目录 C 菜单 F 按钮 */
    private String menuType;

    /** 菜单图标 */
    private String icon;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识，如 system:user:add */
    private String perms;

    /** 显示顺序 */
    private Integer orderNum;

    /** 是否外链（1 是 0 否） */
    private Integer isFrame;

    /** 是否显示（1 显示 0 隐藏） */
    private Integer visible;

    /** 状态（1 正常 0 停用） */
    private Integer status;

    /** 子菜单（非数据库字段） */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
