package com.clfw.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志。日志表不使用逻辑删除与更新填充，独立于 BaseEntity。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String operType;

    /** 操作人 */
    private String operBy;

    /** 操作 IP */
    private String operIp;

    /** 操作地点 */
    private String operLocation;

    /** 请求方法 */
    private String requestMethod;

    /** 请求 URL */
    private String operUrl;

    /** 请求参数 */
    private String operParam;

    /** 返回结果 */
    private String jsonResult;

    /** 状态（1 正常 0 异常） */
    private Integer status;

    /** 异常信息 */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;
}
