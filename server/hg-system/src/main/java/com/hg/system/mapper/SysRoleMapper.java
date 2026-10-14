package com.hg.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hg.system.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的角色。
     */
    @Select("""
            select r.* from sys_role r
            join sys_user_role ur on r.id = ur.role_id
            where ur.user_id = #{userId} and r.status = 1 and r.deleted = 0
            order by r.role_sort
            """)
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
