package com.hg.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hg.system.domain.SysOperLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 将指定时间之前的操作日志复制到归档表，返回归档行数。
     */
    @Insert("INSERT INTO sys_oper_log_his (id, module, oper_type, oper_by, oper_ip, oper_location, " +
            "request_method, oper_url, oper_param, json_result, status, error_msg, cost_time, oper_time, archive_time) " +
            "SELECT id, module, oper_type, oper_by, oper_ip, oper_location, request_method, oper_url, oper_param, " +
            "json_result, status, error_msg, cost_time, oper_time, NOW() " +
            "FROM sys_oper_log WHERE oper_time IS NOT NULL AND oper_time < #{cutoff}")
    int archiveBefore(@Param("cutoff") LocalDateTime cutoff);

    /**
     * 删除指定时间之前的操作日志，返回删除行数。
     */
    @Delete("DELETE FROM sys_oper_log WHERE oper_time IS NOT NULL AND oper_time < #{cutoff}")
    int deleteBefore(@Param("cutoff") LocalDateTime cutoff);
}
