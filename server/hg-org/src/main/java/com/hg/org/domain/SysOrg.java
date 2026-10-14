package com.hg.org.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hg.common.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 机构。医共体牵头院 → 成员单位，支持多级树形结构。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {

    /** 父机构 ID，顶级为 0 */
    private Long parentId;

    /** 机构名称 */
    @NotBlank(message = "机构名称不能为空")
    private String orgName;

    /** 机构编码 */
    @NotBlank(message = "机构编码不能为空")
    private String orgCode;

    /** 机构类型，字典 sys_org_type */
    private Integer orgType;

    /** 联系人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 显示顺序 */
    private Integer orderNum;

    /** 状态（1 正常 0 停用） */
    private Integer status;

    /** 子机构（非数据库字段） */
    @TableField(exist = false)
    private List<SysOrg> children = new ArrayList<>();
}
