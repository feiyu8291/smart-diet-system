-- ============================================================
-- 1. 初始化系统菜单与角色
-- ============================================================
TRUNCATE TABLE sys_menu RESTART IDENTITY CASCADE;
TRUNCATE TABLE sys_role RESTART IDENTITY CASCADE;
TRUNCATE TABLE sys_role_menu RESTART IDENTITY CASCADE;

-- 插入一级及二级菜单
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_code, request_url, menu_type, menu_icon, sort_order,
                      menu_description, create_by)
VALUES (1, NULL, '系统设置', 'system', 'system', 0, 'system', 999, '系统设置根菜单', '系统管理员'),
       (2, 1, '用户管理', 'system/user/index', '/sys/user/page', 1, 'user', 1, '用户管理菜单', '系统管理员'),
       (3, 1, '菜单管理', 'system/menu/index', '/sys/menu/tree', 1, 'menu', 2, '菜单管理菜单', '系统管理员'),
       (4, 1, '角色管理', 'system/role/index', '/sys/role/page', 1, 'solution', 3, '角色管理菜单', '系统管理员'),
       (5, 1, '字典管理', 'system/dict/index', '/sys/dict/page', 1, 'dict', 4, '字典管理菜单', '系统管理员'),
       (6, 1, '定时任务', 'system/quartz/index', '/sys/quartzJob/page', 1, 'time', 5, '定时任务菜单', '系统管理员'),
       (7, 1, '文件存储', 'system/file/index', '/sys/fileStorage/page', 1, 'file', 6, '文件存储菜单', '系统管理员'),

-- 用户管理 按钮与接口
       (8, 2, '用户新增', 'system/user/add', '/sys/user/save', 2, NULL, NULL, '用户管理-新增', '系统管理员'),
       (9, 2, '用户修改', 'system/user/edit', '/sys/user/update', 2, NULL, NULL, '用户管理-修改', '系统管理员'),
       (10, 2, '用户删除', 'system/user/delete', '/sys/user/delete', 2, NULL, NULL, '用户管理-删除', '系统管理员'),
       (11, 2, '重置密码', 'system/user/resetPassword', '/sys/user/resetPassword', 2, NULL, NULL, '用户管理-重置密码',
        '系统管理员'),
       (12, 2, '启用/禁用', 'system/user/updateStatus', '/sys/user/updateStatus', 2, NULL, NULL, '用户管理-状态修改',
        '系统管理员'),
       (13, 2, '分配角色', 'system/user/saveRoles', '/sys/user/saveRoles', 2, NULL, NULL, '用户管理-角色分配',
        '系统管理员'),

-- 菜单管理 按钮与接口
       (14, 3, '菜单新增', 'system/menu/add', '/sys/menu/save', 2, NULL, NULL, '菜单管理-新增', '系统管理员'),
       (15, 3, '菜单修改', 'system/menu/edit', '/sys/menu/update', 2, NULL, NULL, '菜单管理-修改', '系统管理员'),
       (16, 3, '菜单删除', 'system/menu/delete', '/sys/menu/delete', 2, NULL, NULL, '菜单管理-删除', '系统管理员'),

-- 角色管理 按钮与接口
       (17, 4, '角色新增', 'system/role/add', '/sys/role/save', 2, NULL, NULL, '角色管理-新增', '系统管理员'),
       (18, 4, '角色修改', 'system/role/edit', '/sys/role/update', 2, NULL, NULL, '角色管理-修改', '系统管理员'),
       (19, 4, '角色删除', 'system/role/delete', '/sys/role/delete', 2, NULL, NULL, '角色管理-删除', '系统管理员'),
       (20, 4, '角色配置', 'system/role/configMenus', '/sys/role/configMenus', 2, NULL, NULL, '角色管理-配置角色',
        '系统管理员'),

-- 字典管理 按钮与接口
       (21, 5, '字典新增', 'system/dict/save', '/sys/dict/save', 2, NULL, NULL, '数据字典-新增', '系统管理员'),
       (22, 5, '字典修改', 'system/dict/update', '/sys/dict/update', 2, NULL, NULL, '数据字典-修改', '系统管理员'),
       (23, 5, '字典删除', 'system/dict/delete', '/sys/dict/delete', 2, NULL, NULL, '数据字典-删除', '系统管理员'),
       (24, 5, '字典刷新缓存', 'system/dict/refreshCache', '/sys/dict/refreshCache', 2, NULL, NULL, '数据字典-刷新缓存',
        '系统管理员'),

-- 定时任务 按钮与接口
       (25, 6, '任务新增', 'system/quartz/save', '/sys/quartzJob/save', 2, NULL, NULL, '定时任务-新增', '系统管理员'),
       (26, 6, '任务修改', 'system/quartz/update', '/sys/quartzJob/update', 2, NULL, NULL, '定时任务-修改',
        '系统管理员'),
       (27, 6, '任务删除', 'system/quartz/delete', '/sys/quartzJob/delete', 2, NULL, NULL, '定时任务-删除',
        '系统管理员'),
       (28, 6, '暂停/恢复任务', 'system/quartz/updateStatus', '/sys/quartzJob/updateStatus', 2, NULL, NULL,
        '定时任务-状态切换', '系统管理员'),
       (29, 6, '立即执行任务', 'system/quartz/runOnce', '/sys/quartzJob/runOnce', 2, NULL, NULL, '定时任务-立即执行',
        '系统管理员'),

-- 文件存储 按钮与接口
       (30, 7, '文件上传', 'system/file/upload', '/sys/fileStorage/upload', 2, NULL, NULL, '文件存储-文件上传',
        '系统管理员'),
       (31, 7, '文件删除', 'system/file/delete', '/sys/fileStorage/delete', 2, NULL, NULL, '文件存储-文件删除',
        '系统管理员'),
       (32, 7, '文件下载/预览', 'system/file/preview', '/sys/fileStorage/preview', 2, NULL, NULL, '文件存储-预览与下载',
        '系统管理员'),

-- 日志管理 按钮与接口（新增一级菜单 200 日志管理）
       (200, NULL, '日志管理', 'log_manage', 'log_manage', 0, 'Memo', 5, '系统日志管理根菜单', '系统管理员'),
       (201, 200, '登录日志', 'system/loginLog', '/sys/log/login/page', 1, 'document', 1, '用户系统登录历史',
        '系统管理员'),
       (202, 200, '操作日志', 'system/opLog', '/sys/log/operation/page', 1, 'document', 2, '核心业务流转轨迹审计',
        '系统管理员');

-- 初始化系统角色
INSERT INTO sys_role (role_id, role_name, role_description, create_by)
VALUES (1, '系统管理员', '系统最高权限管理员，管理全平台', '系统管理员'),
       (2, '做饭人', '家庭配餐中负责做饭、采购、记录菜品熟悉度的用户角色', '系统管理员'),
       (3, '吃饭人', '家庭配餐中的就餐主体用户，包含偏好与饮食限制记录', '系统管理员');

-- 系统管理员菜单绑定
INSERT INTO sys_role_menu (role_id, menu_id, create_by)
SELECT 1, menu_id, '系统管理员'
FROM sys_menu;

-- 重置序列值
SELECT setval('sys_menu_menu_id_seq', (SELECT COALESCE(MAX(menu_id), 1) FROM sys_menu));
SELECT setval('sys_role_role_id_seq', (SELECT COALESCE(MAX(role_id), 1) FROM sys_role));
SELECT setval('sys_role_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_menu));


-- ============================================================
-- 2. 初始化核心数据字典
-- ============================================================
TRUNCATE TABLE sys_data_dictionary RESTART IDENTITY;

INSERT INTO sys_data_dictionary (data_type_name, data_type, data_code, data_value, parent_type, parent_code,
                                 data_remark, web_read_only, default_state, init_system_flag, dict_sort, create_by)
VALUES
    -- 性别类型
    ('性别类型', 'gender_type', '1', '男', '', '', '性别-男', 1, 0, 1, 1, '系统管理员'),
    ('性别类型', 'gender_type', '2', '女', '', '', '性别-女', 1, 0, 1, 2, '系统管理员'),
    ('性别类型', 'gender_type', '0', '未知', '', '', '性别-未知', 1, 1, 1, 3, '系统管理员'),

    -- 是否禁用状态
    ('是否禁用状态', 'use_status_type', '0', '启用', '', '', '状态-启用', 1, 1, 1, 1, '系统管理员'),
    ('是否禁用状态', 'use_status_type', '1', '禁用', '', '', '状态-禁用', 1, 0, 1, 2, '系统管理员'),

    -- 菜单类型
    ('菜单类型', 'menu_type', '0', '目录', '', '', '类型-目录', 1, 0, 1, 1, '系统管理员'),
    ('菜单类型', 'menu_type', '1', '菜单', '', '', '类型-菜单', 1, 1, 1, 2, '系统管理员'),
    ('菜单类型', 'menu_type', '2', '按钮', '', '', '类型-按钮', 1, 0, 1, 3, '系统管理员'),
    ('菜单类型', 'menu_type', '3', '权限', '', '', '类型-权限', 1, 0, 1, 4, '系统管理员'),
    ('菜单类型', 'menu_type', '4', '外链', '', '', '类型-外链', 1, 0, 1, 5, '系统管理员'),
    ('菜单类型', 'menu_type', '5', '其他', '', '', '类型-其他', 1, 0, 1, 6, '系统管理员'),

    -- 定时任务类型
    ('定时任务类型', 'quartz_job_type', '1', '系统任务', '', '', '状态-系统任务', 1, 1, 1, 1, '系统管理员'),
    ('定时任务类型', 'quartz_job_type', '2', '业务任务', '', '', '状态-业务任务', 1, 0, 1, 2, '系统管理员'),

    -- 通用标识
    ('通用标识', 'common_flag', '0', '否', '', '', '标识-否', 1, 1, 1, 1, '系统管理员'),
    ('通用标识', 'common_flag', '1', '是', '', '', '标识-是', 1, 0, 1, 2, '系统管理员');

SELECT setval('sys_data_dictionary_dict_id_seq', (SELECT COALESCE(MAX(dict_id), 1) FROM sys_data_dictionary));


-- ============================================================
-- 3. 初始化超级管理员账户与角色关联
-- ============================================================
TRUNCATE TABLE sys_user RESTART IDENTITY CASCADE;
TRUNCATE TABLE sys_user_role RESTART IDENTITY CASCADE;

INSERT INTO sys_user (user_id, real_name, phone_num, user_password, username, id_card_num,
                      use_status, del_flag, create_by)
VALUES (1, '系统超级管理员', '13800000000', '$2a$10$ScYtPyGq/HWJPmKTEAGs1.i1YX13O1FlNZvpOXX0CEP16AyCQ4whS',
        'admin', '110101199003072345', 0, 0, '系统管理员');

SELECT setval('sys_user_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM sys_user));

INSERT INTO sys_user_role (user_id, role_id, del_flag, create_by)
VALUES (1, 1, 0, '系统管理员');

SELECT setval('sys_user_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_role));
