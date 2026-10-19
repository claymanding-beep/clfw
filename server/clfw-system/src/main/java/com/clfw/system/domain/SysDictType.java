package com.clfw.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clfw.common.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    /** 字典名称 */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /** 字典类型编码，如 sys_user_sex */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /** 状态（1 正常 0 停用） */
    private Integer status;
}
