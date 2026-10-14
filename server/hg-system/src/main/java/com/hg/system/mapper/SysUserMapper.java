package com.hg.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hg.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
