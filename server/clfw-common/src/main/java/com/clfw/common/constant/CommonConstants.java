package com.clfw.common.constant;

/**
 * 通用常量。
 */
public interface CommonConstants {

    /** 逻辑删除：正常 */
    int NOT_DELETED = 0;
    /** 逻辑删除：已删除 */
    int DELETED = 1;

    /** 状态：启用 */
    int STATUS_ENABLE = 1;
    /** 状态：禁用 */
    int STATUS_DISABLE = 0;

    /** 是否：是 */
    int YES = 1;
    /** 是否：否 */
    int NO = 0;

    /** 超级管理员角色标识 */
    String SUPER_ADMIN_KEY = "super_admin";
    /** 超级管理员用户 ID */
    Long SUPER_ADMIN_ID = 1L;

    /** 菜单类型：目录 */
    String MENU_TYPE_DIR = "M";
    /** 菜单类型：菜单 */
    String MENU_TYPE_MENU = "C";
    /** 菜单类型：按钮 */
    String MENU_TYPE_BUTTON = "F";

    /** 默认初始密码 */
    String DEFAULT_PASSWORD = "123456";

    /** 顶级父节点 ID */
    Long ROOT_PARENT_ID = 0L;
}
