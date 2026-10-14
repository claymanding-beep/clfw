package com.hg.common.log;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志记录载体，由框架层 AOP 采集后通过事件交由持久层落库。
 */
@Data
public class OperLogRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作模块 */
    private String module;
    /** 操作类型（OTHER/INSERT/UPDATE/DELETE/GRANT/EXPORT/IMPORT/CLEAN） */
    private String operType;
    /** 操作人 */
    private String operBy;
    /** 操作人 IP */
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
    private LocalDateTime operTime;
}
