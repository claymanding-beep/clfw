-- =============================================================
-- CLFW Platform - 系统管理域建表脚本
-- 数据库: MySQL 8.0  字符集: utf8mb4
-- 设计原则: 公共字段 / org_id 数据隔离 / 逻辑删除 / 字典驱动 / 无外键约束
-- =============================================================

CREATE DATABASE IF NOT EXISTS `clfw`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `clfw`;

-- ----------------------------
-- 机构表
-- ----------------------------
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `parent_id`   BIGINT       DEFAULT 0 COMMENT '父机构ID，顶级为0',
    `org_name`    VARCHAR(100) NOT NULL COMMENT '机构名称',
    `org_code`    VARCHAR(64)  NOT NULL COMMENT '机构编码',
    `org_type`    TINYINT      DEFAULT NULL COMMENT '机构类型(字典 sys_org_type)',
    `leader`      VARCHAR(64)  DEFAULT NULL COMMENT '联系人',
    `phone`       VARCHAR(32)  DEFAULT NULL COMMENT '联系电话',
    `order_num`   INT          DEFAULT 0 COMMENT '显示顺序',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_org_code` (`org_code`),
    KEY `idx_org_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构表';

-- ----------------------------
-- 科室表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `org_id`      BIGINT       NOT NULL COMMENT '所属机构ID',
    `parent_id`   BIGINT       DEFAULT 0 COMMENT '父科室ID，顶级为0',
    `dept_name`   VARCHAR(100) NOT NULL COMMENT '科室名称',
    `dept_code`   VARCHAR(64)  DEFAULT NULL COMMENT '科室编码',
    `leader`      VARCHAR(64)  DEFAULT NULL COMMENT '负责人',
    `phone`       VARCHAR(32)  DEFAULT NULL COMMENT '联系电话',
    `order_num`   INT          DEFAULT 0 COMMENT '显示顺序',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_dept_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名(登录账号)',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`    VARCHAR(64)  NOT NULL COMMENT '姓名',
    `phone`       VARCHAR(255) DEFAULT NULL COMMENT '手机号(加密存储)',
    `email`       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `sex`         TINYINT      DEFAULT 0 COMMENT '性别(0未知 1男 2女)',
    `org_id`      BIGINT       DEFAULT NULL COMMENT '所属机构ID',
    `dept_id`     BIGINT       DEFAULT NULL COMMENT '所属科室ID',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `login_ip`    VARCHAR(64)  DEFAULT NULL COMMENT '最后登录IP',
    `login_date`  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `role_name`   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    `role_key`    VARCHAR(64)  NOT NULL COMMENT '角色标识',
    `role_sort`   INT          DEFAULT 0 COMMENT '显示顺序',
    `data_scope`  TINYINT      DEFAULT 3 COMMENT '数据范围(1全部 2本机构及以下 3本机构 4本科室 5仅本人 6自定义)',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 菜单表
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `parent_id`   BIGINT       DEFAULT 0 COMMENT '父菜单ID，顶级为0',
    `menu_name`   VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    `menu_type`   CHAR(1)      DEFAULT 'C' COMMENT '菜单类型(M目录 C菜单 F按钮)',
    `icon`        VARCHAR(64)  DEFAULT NULL COMMENT '菜单图标',
    `path`        VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    `component`   VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `perms`       VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    `order_num`   INT          DEFAULT 0 COMMENT '显示顺序',
    `is_frame`    TINYINT      DEFAULT 0 COMMENT '是否外链(1是 0否)',
    `visible`     TINYINT      DEFAULT 1 COMMENT '是否显示(1显示 0隐藏)',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_menu_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------
-- 用户-角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 角色-菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `dict_name`   VARCHAR(100) NOT NULL COMMENT '字典名称',
    `dict_type`   VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ----------------------------
-- 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `dict_type`   VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    `dict_label`  VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value`  VARCHAR(100) NOT NULL COMMENT '字典键值',
    `dict_sort`   INT          DEFAULT 0 COMMENT '显示顺序',
    `list_class`  VARCHAR(64)  DEFAULT NULL COMMENT '样式标记(tag颜色)',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_dict_data_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ----------------------------
-- 参数配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id`           BIGINT       NOT NULL COMMENT '主键',
    `config_name`  VARCHAR(100) NOT NULL COMMENT '参数名称',
    `config_key`   VARCHAR(100) NOT NULL COMMENT '参数键名',
    `config_value` VARCHAR(500) NOT NULL COMMENT '参数键值',
    `config_group` VARCHAR(64)  DEFAULT NULL COMMENT '所属分组',
    `config_type`  TINYINT      DEFAULT 0 COMMENT '是否内置(1是 0否)',
    `create_by`    VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time`  DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`    VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time`  DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`      TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
    `id`             BIGINT       NOT NULL COMMENT '主键',
    `module`         VARCHAR(64)  DEFAULT NULL COMMENT '操作模块',
    `oper_type`      VARCHAR(32)  DEFAULT NULL COMMENT '操作类型',
    `oper_by`        VARCHAR(64)  DEFAULT NULL COMMENT '操作人',
    `oper_ip`        VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
    `oper_location`  VARCHAR(255) DEFAULT NULL COMMENT '操作地点',
    `request_method` VARCHAR(16)  DEFAULT NULL COMMENT '请求方法',
    `oper_url`       VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    `oper_param`     TEXT         DEFAULT NULL COMMENT '请求参数',
    `json_result`    TEXT         DEFAULT NULL COMMENT '返回结果',
    `status`         TINYINT      DEFAULT 1 COMMENT '状态(1正常 0异常)',
    `error_msg`      TEXT         DEFAULT NULL COMMENT '异常信息',
    `cost_time`      BIGINT       DEFAULT 0 COMMENT '耗时(毫秒)',
    `oper_time`      DATETIME     DEFAULT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_log_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id`            BIGINT       NOT NULL COMMENT '主键',
    `username`      VARCHAR(64)  DEFAULT NULL COMMENT '用户名',
    `ip`            VARCHAR(64)  DEFAULT NULL COMMENT '登录IP',
    `login_location` VARCHAR(255) DEFAULT NULL COMMENT '登录地点',
    `browser`       VARCHAR(64)  DEFAULT NULL COMMENT '浏览器',
    `os`            VARCHAR(64)  DEFAULT NULL COMMENT '操作系统',
    `status`        TINYINT      DEFAULT 1 COMMENT '状态(1成功 0失败)',
    `msg`           VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    `login_time`    DATETIME     DEFAULT NULL COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_login_log_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ----------------------------
-- 操作日志归档表（结构与主表一致，存放超过保留期的历史日志）
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log_his`;
CREATE TABLE `sys_oper_log_his` (
    `id`             BIGINT       NOT NULL COMMENT '主键',
    `module`         VARCHAR(64)  DEFAULT NULL COMMENT '操作模块',
    `oper_type`      VARCHAR(32)  DEFAULT NULL COMMENT '操作类型',
    `oper_by`        VARCHAR(64)  DEFAULT NULL COMMENT '操作人',
    `oper_ip`        VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
    `oper_location`  VARCHAR(255) DEFAULT NULL COMMENT '操作地点',
    `request_method` VARCHAR(16)  DEFAULT NULL COMMENT '请求方法',
    `oper_url`       VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    `oper_param`     TEXT         DEFAULT NULL COMMENT '请求参数',
    `json_result`    TEXT         DEFAULT NULL COMMENT '返回结果',
    `status`         TINYINT      DEFAULT 1 COMMENT '状态(1正常 0异常)',
    `error_msg`      TEXT         DEFAULT NULL COMMENT '异常信息',
    `cost_time`      BIGINT       DEFAULT 0 COMMENT '耗时(毫秒)',
    `oper_time`      DATETIME     DEFAULT NULL COMMENT '操作时间',
    `archive_time`   DATETIME     DEFAULT NULL COMMENT '归档时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_log_his_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志归档表';

-- ----------------------------
-- 登录日志归档表（结构与主表一致，存放超过保留期的历史日志）
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log_his`;
CREATE TABLE `sys_login_log_his` (
    `id`            BIGINT       NOT NULL COMMENT '主键',
    `username`      VARCHAR(64)  DEFAULT NULL COMMENT '用户名',
    `ip`            VARCHAR(64)  DEFAULT NULL COMMENT '登录IP',
    `login_location` VARCHAR(255) DEFAULT NULL COMMENT '登录地点',
    `browser`       VARCHAR(64)  DEFAULT NULL COMMENT '浏览器',
    `os`            VARCHAR(64)  DEFAULT NULL COMMENT '操作系统',
    `status`        TINYINT      DEFAULT 1 COMMENT '状态(1成功 0失败)',
    `msg`           VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    `login_time`    DATETIME     DEFAULT NULL COMMENT '登录时间',
    `archive_time`  DATETIME     DEFAULT NULL COMMENT '归档时间',
    PRIMARY KEY (`id`),
    KEY `idx_login_log_his_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志归档表';

-- ----------------------------
-- 社会化登录用户表（C 端用户，区别于 sys_user）
-- ----------------------------
DROP TABLE IF EXISTS `sys_social_user`;
CREATE TABLE `sys_social_user` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `open_id`     VARCHAR(64)  NOT NULL COMMENT '第三方平台 openid',
    `union_id`    VARCHAR(64)  DEFAULT NULL COMMENT '微信 unionid（跨应用唯一）',
    `platform`    VARCHAR(20)  NOT NULL COMMENT '平台: WECHAT_MINI / WECHAT_OAUTH / SMS',
    `nickname`    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `avatar_url`  VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
    `phone`       VARCHAR(255) DEFAULT NULL COMMENT '手机号（AES 加密存储）',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '关联的 sys_user.id（可选绑定）',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1正常 0禁用)',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除(0正常 1删除)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_openid` (`platform`, `open_id`),
    KEY `idx_social_union_id` (`union_id`),
    KEY `idx_social_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社会化登录用户表';
