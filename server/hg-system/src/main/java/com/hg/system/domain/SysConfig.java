package com.hg.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hg.common.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    /** 参数名称 */
    @NotBlank(message = "参数名称不能为空")
    private String configName;

    /** 参数键名 */
    @NotBlank(message = "参数键名不能为空")
    private String configKey;

    /** 参数键值 */
    @NotBlank(message = "参数键值不能为空")
    private String configValue;

    /** 所属分组（模块） */
    private String configGroup;

    /** 是否系统内置（1 是 0 否），内置参数不允许删除 */
    private Integer configType;
}
