package com.clfw.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clfw.common.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    /** 所属字典类型编码 */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /** 字典标签 */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /** 字典键值 */
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;

    /** 显示顺序 */
    private Integer dictSort;

    /** 样式标记（tag 颜色），如 primary/success/warning/danger */
    private String listClass;

    /** 状态（1 正常 0 停用） */
    private Integer status;
}
