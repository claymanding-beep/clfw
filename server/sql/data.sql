-- =============================================================
-- 系统管理域 - 初始化种子数据
-- 默认管理员账号: admin / 123456
-- =============================================================
USE `clfw`;

-- ----------------------------
-- 机构
-- ----------------------------
INSERT INTO `sys_org` (`id`, `parent_id`, `org_name`, `org_code`, `org_type`, `leader`, `phone`, `order_num`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1, 0, '根机构', 'ORG_ROOT', 1, '管理员', '0537-0000000', 1, 1, 'system', NOW(), 0);

-- ----------------------------
-- 科室
-- ----------------------------
INSERT INTO `sys_dept` (`id`, `org_id`, `parent_id`, `dept_name`, `dept_code`, `order_num`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1, 1, 0, '部门1', 'DEPT_NFM', 1, 1, 'system', NOW(), 0),
       (2, 1, 0, '部门2', 'DEPT_XXG', 2, 1, 'system', NOW(), 0),
       (3, 1, 0, '部门3', 'DEPT_JKGL', 3, 1, 'system', NOW(), 0);

-- ----------------------------
-- 用户（密码均为 123456 的 BCrypt 密文）
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `sex`, `org_id`, `dept_id`, `status`, `create_by`, `create_time`, `deleted`, `remark`)
VALUES (1, 'admin', '$2a$10$wHcgzavqMXGzV5LJcvs/buqGLvbYWKegUCFlfSAtbF2gbcd5zn3zO', '超级管理员', 1, 1, NULL, 1, 'system', NOW(), 0, '系统内置超级管理员');

-- ----------------------------
-- 角色（预设角色）
-- ----------------------------
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `create_by`, `create_time`, `deleted`, `remark`)
VALUES (1, '超级管理员', 'super_admin', 1, 1, 1, 'system', NOW(), 0, '拥有全部权限'),
       (2, '机构管理员', 'org_admin', 2, 3, 1, 'system', NOW(), 0, '本机构数据管理');

-- ----------------------------
-- 用户角色关联
-- ----------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- ----------------------------
-- 菜单：系统管理目录
-- ----------------------------
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1, 0, '系统管理', 'M', 'setting', '/system', NULL, NULL, 1, 1, 1, 'system', NOW(), 0);

-- 二级菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (100, 1, '用户管理', 'C', 'user', 'user', 'system/user/index', 'system:user:list', 1, 1, 1, 'system', NOW(), 0),
       (101, 1, '角色管理', 'C', 'peoples', 'role', 'system/role/index', 'system:role:list', 2, 1, 1, 'system', NOW(), 0),
       (102, 1, '菜单管理', 'C', 'tree-table', 'menu', 'system/menu/index', 'system:menu:list', 3, 1, 1, 'system', NOW(), 0),
       (103, 1, '机构管理', 'C', 'tree', 'org', 'system/org/index', 'system:org:list', 4, 1, 1, 'system', NOW(), 0),
       (104, 1, '科室管理', 'C', 'post', 'dept', 'system/dept/index', 'system:dept:list', 5, 1, 1, 'system', NOW(), 0),
       (105, 1, '数据字典', 'C', 'dict', 'dict', 'system/dict/index', 'system:dict:list', 6, 1, 1, 'system', NOW(), 0),
       (106, 1, '参数设置', 'C', 'edit', 'config', 'system/config/index', 'system:config:list', 7, 1, 1, 'system', NOW(), 0),
       (107, 1, '操作日志', 'C', 'form', 'operlog', 'system/log/oper', 'system:operlog:list', 8, 1, 1, 'system', NOW(), 0),
       (108, 1, '登录日志', 'C', 'logininfor', 'loginlog', 'system/log/login', 'system:loginlog:list', 9, 1, 1, 'system', NOW(), 0),
       (109, 1, '在线用户', 'C', 'online', 'online', 'system/online/index', 'system:online:list', 10, 1, 1, 'system', NOW(), 0);

-- 用户管理按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1001, 100, '用户查询', 'F', 'system:user:query', 1, 1, 1, 'system', NOW(), 0),
       (1002, 100, '用户新增', 'F', 'system:user:add', 2, 1, 1, 'system', NOW(), 0),
       (1003, 100, '用户修改', 'F', 'system:user:edit', 3, 1, 1, 'system', NOW(), 0),
       (1004, 100, '用户删除', 'F', 'system:user:remove', 4, 1, 1, 'system', NOW(), 0),
       (1005, 100, '重置密码', 'F', 'system:user:resetPwd', 5, 1, 1, 'system', NOW(), 0);

-- 角色管理按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1011, 101, '角色查询', 'F', 'system:role:query', 1, 1, 1, 'system', NOW(), 0),
       (1012, 101, '角色新增', 'F', 'system:role:add', 2, 1, 1, 'system', NOW(), 0),
       (1013, 101, '角色修改', 'F', 'system:role:edit', 3, 1, 1, 'system', NOW(), 0),
       (1014, 101, '角色删除', 'F', 'system:role:remove', 4, 1, 1, 'system', NOW(), 0);

-- 菜单管理按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1021, 102, '菜单查询', 'F', 'system:menu:query', 1, 1, 1, 'system', NOW(), 0),
       (1022, 102, '菜单新增', 'F', 'system:menu:add', 2, 1, 1, 'system', NOW(), 0),
       (1023, 102, '菜单修改', 'F', 'system:menu:edit', 3, 1, 1, 'system', NOW(), 0),
       (1024, 102, '菜单删除', 'F', 'system:menu:remove', 4, 1, 1, 'system', NOW(), 0);

-- 在线用户按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1091, 109, '强退', 'F', 'system:online:forceLogout', 1, 1, 1, 'system', NOW(), 0);

-- 操作日志按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1071, 107, '操作日志查询', 'F', 'system:operlog:query', 1, 1, 1, 'system', NOW(), 0),
       (1072, 107, '操作日志清空', 'F', 'system:operlog:remove', 2, 1, 1, 'system', NOW(), 0);

-- 登录日志按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `perms`, `order_num`, `visible`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (1081, 108, '登录日志清空', 'F', 'system:loginlog:remove', 1, 1, 1, 'system', NOW(), 0);

-- ----------------------------
-- 字典类型
-- ----------------------------
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `deleted`, `remark`)
VALUES (1, '用户性别', 'sys_user_sex', 1, 'system', NOW(), 0, '用户性别列表'),
       (2, '通用状态', 'sys_common_status', 1, 'system', NOW(), 0, '启用停用状态'),
       (3, '是否', 'sys_yes_no', 1, 'system', NOW(), 0, '是否列表'),
       (5, '菜单类型', 'sys_menu_type', 1, 'system', NOW(), 0, '菜单目录按钮类型');

-- ----------------------------
-- 字典数据
-- ----------------------------
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `list_class`, `status`, `create_by`, `create_time`, `deleted`)
VALUES (100, 'sys_user_sex', '未知', '0', 1, 'info', 1, 'system', NOW(), 0),
       (101, 'sys_user_sex', '男', '1', 2, 'primary', 1, 'system', NOW(), 0),
       (102, 'sys_user_sex', '女', '2', 3, 'danger', 1, 'system', NOW(), 0),
       (110, 'sys_common_status', '启用', '1', 1, 'success', 1, 'system', NOW(), 0),
       (111, 'sys_common_status', '停用', '0', 2, 'danger', 1, 'system', NOW(), 0),
       (120, 'sys_yes_no', '是', '1', 1, 'success', 1, 'system', NOW(), 0),
       (121, 'sys_yes_no', '否', '0', 2, 'info', 1, 'system', NOW(), 0),
       (140, 'sys_menu_type', '目录', 'M', 1, 'warning', 1, 'system', NOW(), 0),
       (141, 'sys_menu_type', '菜单', 'C', 2, 'primary', 1, 'system', NOW(), 0),
       (142, 'sys_menu_type', '按钮', 'F', 3, 'info', 1, 'system', NOW(), 0);

-- ----------------------------
-- 参数配置
-- ----------------------------
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_group`, `config_type`, `create_by`, `create_time`, `deleted`, `remark`)
VALUES (1, '用户初始密码', 'sys.user.initPassword', '123456', '用户管理', 1, 'system', NOW(), 0, '新增用户默认密码'),
       (2, '密码错误锁定次数', 'sys.pwd.maxRetry', '5', '安全策略', 1, 'system', NOW(), 0, '连续错误次数后锁定'),
       (3, '账号锁定时长(分钟)', 'sys.pwd.lockTime', '10', '安全策略', 1, 'system', NOW(), 0, ''),
       (4, '会话超时(秒)', 'sys.session.timeout', '86400', '安全策略', 1, 'system', NOW(), 0, ''),
       (5, '登录验证码开关', 'sys.account.captchaEnabled', 'true', '安全策略', 1, 'system', NOW(), 0, '登录是否需要图形验证码');
