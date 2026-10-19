package com.clfw.common.constant;

/**
 * 数据范围（dataScope）常量。
 * 对应 sys_role.data_scope 字段，决定登录用户可见的数据边界。
 */
public interface DataScope {

    /** 全部数据 */
    int ALL = 1;
    /** 本机构及以下（含子机构） */
    int ORG_AND_CHILD = 2;
    /** 本机构 */
    int ORG = 3;
    /** 本科室 */
    int DEPT = 4;
    /** 仅本人 */
    int SELF = 5;
    /** 自定义（需角色-机构关联，暂未启用） */
    int CUSTOM = 6;
}
