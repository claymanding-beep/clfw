package com.clfw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clfw.system.domain.SysLoginLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 登录日志 Mapper。
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 将指定时间之前的登录日志复制到归档表，返回归档行数。
     */
    @Insert("INSERT INTO sys_login_log_his (id, username, ip, login_location, browser, os, status, msg, login_time, archive_time) " +
            "SELECT id, username, ip, login_location, browser, os, status, msg, login_time, NOW() " +
            "FROM sys_login_log WHERE login_time IS NOT NULL AND login_time < #{cutoff}")
    int archiveBefore(@Param("cutoff") LocalDateTime cutoff);

    /**
     * 删除指定时间之前的登录日志，返回删除行数。
     */
    @Delete("DELETE FROM sys_login_log WHERE login_time IS NOT NULL AND login_time < #{cutoff}")
    int deleteBefore(@Param("cutoff") LocalDateTime cutoff);
}
