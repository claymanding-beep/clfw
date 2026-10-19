package com.clfw.org.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clfw.common.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 科室，隶属于机构。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    /** 所属机构 ID */
    @NotNull(message = "所属机构不能为空")
    private Long orgId;

    /** 父科室 ID，顶级为 0 */
    private Long parentId;

    /** 科室名称 */
    @NotBlank(message = "科室名称不能为空")
    private String deptName;

    /** 科室编码 */
    private String deptCode;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 显示顺序 */
    private Integer orderNum;

    /** 状态（1 正常 0 停用） */
    private Integer status;
}
