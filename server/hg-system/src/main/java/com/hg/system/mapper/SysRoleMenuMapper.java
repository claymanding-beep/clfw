package com.hg.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hg.system.domain.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联 Mapper。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
