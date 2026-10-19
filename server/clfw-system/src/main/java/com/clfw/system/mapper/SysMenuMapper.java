package com.clfw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clfw.system.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询用户拥有的权限标识集合。
     */
    @Select("""
            select distinct m.perms from sys_menu m
            join sys_role_menu rm on m.id = rm.menu_id
            join sys_user_role ur on rm.role_id = ur.role_id
            join sys_role r on r.id = ur.role_id
            where ur.user_id = #{userId} and m.status = 1 and r.status = 1
              and m.deleted = 0 and r.deleted = 0
              and m.perms is not null and m.perms <> ''
            """)
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户可见的菜单（目录 + 菜单），用于构建前端路由。
     */
    @Select("""
            select distinct m.* from sys_menu m
            join sys_role_menu rm on m.id = rm.menu_id
            join sys_user_role ur on rm.role_id = ur.role_id
            join sys_role r on r.id = ur.role_id
            where ur.user_id = #{userId} and m.status = 1 and r.status = 1
              and m.deleted = 0 and m.menu_type in ('M','C')
            order by m.parent_id, m.order_num
            """)
    List<SysMenu> selectMenuTreeByUserId(@Param("userId") Long userId);

    /**
     * 查询全部菜单（超级管理员使用）。
     */
    @Select("""
            select * from sys_menu
            where status = 1 and deleted = 0 and menu_type in ('M','C')
            order by parent_id, order_num
            """)
    List<SysMenu> selectMenuTreeAll();
}
