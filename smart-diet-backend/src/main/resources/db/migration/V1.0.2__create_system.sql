/*==============================================================*/
/* DBMS name:      PostgreSQL                                   */
/*==============================================================*/

DROP TABLE IF EXISTS sys_data_dictionary;
DROP TABLE IF EXISTS sys_file_storage;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS sys_quartz_job;
DROP TABLE IF EXISTS sys_quartz_job_log;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_user_role;

/*==============================================================*/
/* Table: sys_data_dictionary                                   */
/*==============================================================*/
CREATE TABLE sys_data_dictionary
(
    dict_id          BIGSERIAL    NOT NULL,
    data_type_name   VARCHAR(64),
    data_type        VARCHAR(64)  NOT NULL,
    data_code        VARCHAR(255) NOT NULL,
    data_value       VARCHAR(255) NOT NULL,
    parent_type      VARCHAR(64),
    parent_code      VARCHAR(255),
    data_remark      VARCHAR(255),
    web_read_only    SMALLINT     NOT NULL DEFAULT 0,
    default_state    SMALLINT     NOT NULL DEFAULT 0,
    dict_sort        INTEGER               DEFAULT 0,
    init_system_flag SMALLINT     NOT NULL DEFAULT 0,
    del_flag         SMALLINT     NOT NULL DEFAULT 0,
    create_by        VARCHAR(64),
    update_by        VARCHAR(64),
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (dict_id)
);

COMMENT
ON TABLE sys_data_dictionary IS '系统-数据字典表';
COMMENT
ON COLUMN sys_data_dictionary.dict_id IS '主键ID';
COMMENT
ON COLUMN sys_data_dictionary.data_type_name IS '类型名称';
COMMENT
ON COLUMN sys_data_dictionary.data_type IS '类型编码';
COMMENT
ON COLUMN sys_data_dictionary.data_code IS '数据编码';
COMMENT
ON COLUMN sys_data_dictionary.data_value IS '数据值';
COMMENT
ON COLUMN sys_data_dictionary.parent_type IS '上级类型';
COMMENT
ON COLUMN sys_data_dictionary.parent_code IS '上级编码';
COMMENT
ON COLUMN sys_data_dictionary.data_remark IS '字典备注';
COMMENT
ON COLUMN sys_data_dictionary.web_read_only IS '页面只读(0否1是)';
COMMENT
ON COLUMN sys_data_dictionary.default_state IS '默认选中(0否1是)';
COMMENT
ON COLUMN sys_data_dictionary.dict_sort IS '排序序号';
COMMENT
ON COLUMN sys_data_dictionary.init_system_flag IS '系统预置(0否1是)';
COMMENT
ON COLUMN sys_data_dictionary.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_data_dictionary.create_by IS '创建人';
COMMENT
ON COLUMN sys_data_dictionary.update_by IS '更新人';
COMMENT
ON COLUMN sys_data_dictionary.create_time IS '创建时间';
COMMENT
ON COLUMN sys_data_dictionary.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_file_storage                                      */
/*==============================================================*/
CREATE TABLE sys_file_storage
(
    storage_id     BIGSERIAL    NOT NULL,
    bucket_number  SMALLINT     NOT NULL DEFAULT 1,
    file_name      VARCHAR(255) NOT NULL,
    file_real_name VARCHAR(255) NOT NULL,
    file_size      VARCHAR(100) NOT NULL,
    file_mime_type VARCHAR(50)  NOT NULL,
    file_type      VARCHAR(50)  NOT NULL,
    file_path      TEXT         NOT NULL,
    file_md5       VARCHAR(64),
    del_flag       SMALLINT     NOT NULL DEFAULT 0,
    create_by      VARCHAR(64),
    update_by      VARCHAR(64),
    create_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (storage_id)
);

COMMENT
ON TABLE sys_file_storage IS '系统-文件存储表(S3协议)';
COMMENT
ON COLUMN sys_file_storage.storage_id IS '主键ID';
COMMENT
ON COLUMN sys_file_storage.bucket_number IS '存储桶号';
COMMENT
ON COLUMN sys_file_storage.file_name IS '文件名称';
COMMENT
ON COLUMN sys_file_storage.file_real_name IS '物理文件名';
COMMENT
ON COLUMN sys_file_storage.file_size IS '文件大小';
COMMENT
ON COLUMN sys_file_storage.file_mime_type IS 'MIME类型';
COMMENT
ON COLUMN sys_file_storage.file_type IS '文件类型';
COMMENT
ON COLUMN sys_file_storage.file_path IS '文件路径';
COMMENT
ON COLUMN sys_file_storage.file_md5 IS '文件的MD5哈希值';
COMMENT
ON COLUMN sys_file_storage.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_file_storage.create_by IS '创建人';
COMMENT
ON COLUMN sys_file_storage.update_by IS '更新人';
COMMENT
ON COLUMN sys_file_storage.create_time IS '创建时间';
COMMENT
ON COLUMN sys_file_storage.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_menu                                              */
/*==============================================================*/
CREATE TABLE sys_menu
(
    menu_id          BIGSERIAL NOT NULL,
    parent_id        BIGINT,
    menu_name        VARCHAR(64),
    menu_code        VARCHAR(64),
    request_url      VARCHAR(255),
    menu_type        SMALLINT,
    menu_icon        VARCHAR(100),
    sort_order       INTEGER,
    menu_description VARCHAR(255),
    del_flag         SMALLINT  NOT NULL DEFAULT 0,
    create_by        VARCHAR(64),
    update_by        VARCHAR(64),
    create_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (menu_id)
);

COMMENT
ON TABLE sys_menu IS '系统-菜单信息表';
COMMENT
ON COLUMN sys_menu.menu_id IS '主键ID';
COMMENT
ON COLUMN sys_menu.parent_id IS '父级菜单ID';
COMMENT
ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT
ON COLUMN sys_menu.menu_code IS '菜单编码';
COMMENT
ON COLUMN sys_menu.request_url IS '菜单URL';
COMMENT
ON COLUMN sys_menu.menu_type IS '菜单类型(0目录 1菜单 2按钮 3权限 4外链 5其他)';
COMMENT
ON COLUMN sys_menu.menu_icon IS '菜单图标';
COMMENT
ON COLUMN sys_menu.sort_order IS '菜单排序';
COMMENT
ON COLUMN sys_menu.menu_description IS '菜单描述';
COMMENT
ON COLUMN sys_menu.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_menu.create_by IS '创建人';
COMMENT
ON COLUMN sys_menu.update_by IS '更新人';
COMMENT
ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT
ON COLUMN sys_menu.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_quartz_job                                        */
/*==============================================================*/
CREATE TABLE sys_quartz_job
(
    job_id          BIGSERIAL   NOT NULL,
    job_name        VARCHAR(64) NOT NULL,
    job_group       VARCHAR(64),
    cron_expression VARCHAR(64) NOT NULL,
    job_description VARCHAR(255),
    handle_class    VARCHAR(255),
    method_param    VARCHAR(255),
    del_flag        SMALLINT  DEFAULT 0,
    end_time        TIMESTAMP,
    permanent_state SMALLINT,
    job_type        SMALLINT,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64),
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (job_id)
);

COMMENT
ON TABLE sys_quartz_job IS '系统-定时任务表';
COMMENT
ON COLUMN sys_quartz_job.job_id IS '主键ID';
COMMENT
ON COLUMN sys_quartz_job.job_name IS '任务名称';
COMMENT
ON COLUMN sys_quartz_job.job_group IS '任务分组';
COMMENT
ON COLUMN sys_quartz_job.cron_expression IS '表达式(Cron)';
COMMENT
ON COLUMN sys_quartz_job.job_description IS '任务描述';
COMMENT
ON COLUMN sys_quartz_job.handle_class IS '处理器类名';
COMMENT
ON COLUMN sys_quartz_job.method_param IS '方法参数';
COMMENT
ON COLUMN sys_quartz_job.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_quartz_job.end_time IS '任务结束时间';
COMMENT
ON COLUMN sys_quartz_job.permanent_state IS '永久有效(0否1是)';
COMMENT
ON COLUMN sys_quartz_job.job_type IS '任务类型(1系统 2业务)';
COMMENT
ON COLUMN sys_quartz_job.create_by IS '创建人';
COMMENT
ON COLUMN sys_quartz_job.update_by IS '更新人';
COMMENT
ON COLUMN sys_quartz_job.create_time IS '创建时间';
COMMENT
ON COLUMN sys_quartz_job.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_quartz_job_log                                    */
/*==============================================================*/
CREATE TABLE sys_quartz_job_log
(
    job_log_id      BIGSERIAL   NOT NULL,
    job_id          BIGINT      NOT NULL,
    cron_expression VARCHAR(64) NOT NULL,
    job_description VARCHAR(2048),
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (job_log_id)
);

COMMENT
ON TABLE sys_quartz_job_log IS '系统-定时任务日志表';
COMMENT
ON COLUMN sys_quartz_job_log.job_log_id IS '主键ID';
COMMENT
ON COLUMN sys_quartz_job_log.job_id IS '任务ID';
COMMENT
ON COLUMN sys_quartz_job_log.cron_expression IS '表达式(Cron)';
COMMENT
ON COLUMN sys_quartz_job_log.job_description IS '任务描述';
COMMENT
ON COLUMN sys_quartz_job_log.create_time IS '执行时间';

/*==============================================================*/
/* Table: sys_role                                              */
/*==============================================================*/
CREATE TABLE sys_role
(
    role_id          BIGSERIAL NOT NULL,
    role_name        VARCHAR(60),
    role_description VARCHAR(255),
    del_flag         SMALLINT  NOT NULL DEFAULT 0,
    create_by        VARCHAR(64),
    update_by        VARCHAR(64),
    create_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id)
);

COMMENT
ON TABLE sys_role IS '系统-角色信息表';
COMMENT
ON COLUMN sys_role.role_id IS '主键ID';
COMMENT
ON COLUMN sys_role.role_name IS '角色名称';
COMMENT
ON COLUMN sys_role.role_description IS '角色描述';
COMMENT
ON COLUMN sys_role.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_role.create_by IS '创建人';
COMMENT
ON COLUMN sys_role.update_by IS '更新人';
COMMENT
ON COLUMN sys_role.create_time IS '创建时间';
COMMENT
ON COLUMN sys_role.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_role_menu                                         */
/*==============================================================*/
CREATE TABLE sys_role_menu
(
    id          BIGSERIAL NOT NULL,
    role_id     BIGINT    NOT NULL,
    menu_id     BIGINT    NOT NULL,
    del_flag    SMALLINT  NOT NULL DEFAULT 0,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT
ON TABLE sys_role_menu IS '系统-角色菜单关联表';
COMMENT
ON COLUMN sys_role_menu.id IS '主键ID';
COMMENT
ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT
ON COLUMN sys_role_menu.menu_id IS '菜单ID';
COMMENT
ON COLUMN sys_role_menu.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_role_menu.create_by IS '创建人';
COMMENT
ON COLUMN sys_role_menu.update_by IS '更新人';
COMMENT
ON COLUMN sys_role_menu.create_time IS '创建时间';
COMMENT
ON COLUMN sys_role_menu.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_user                                              */
/*==============================================================*/
CREATE TABLE sys_user
(
    user_id       BIGSERIAL    NOT NULL,
    real_name     VARCHAR(64),
    phone_num     VARCHAR(32)  NOT NULL,
    user_password VARCHAR(255) NOT NULL,
    username      VARCHAR(64)  NOT NULL,
    id_card_num   VARCHAR(32)  NOT NULL,
    use_status    SMALLINT              DEFAULT 0,
    del_flag      SMALLINT     NOT NULL DEFAULT 0,
    create_by     VARCHAR(64),
    update_by     VARCHAR(64),
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
);

COMMENT
ON TABLE sys_user IS '系统-用户信息表';
COMMENT
ON COLUMN sys_user.user_id IS '主键ID';
COMMENT
ON COLUMN sys_user.real_name IS '真实姓名';
COMMENT
ON COLUMN sys_user.phone_num IS '手机号';
COMMENT
ON COLUMN sys_user.user_password IS '密码';
COMMENT
ON COLUMN sys_user.username IS '登录账号';
COMMENT
ON COLUMN sys_user.id_card_num IS '身份证号';
COMMENT
ON COLUMN sys_user.use_status IS '禁用状态(0否1是)';
COMMENT
ON COLUMN sys_user.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_user.create_by IS '创建人';
COMMENT
ON COLUMN sys_user.update_by IS '更新人';
COMMENT
ON COLUMN sys_user.create_time IS '创建时间';
COMMENT
ON COLUMN sys_user.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_user_role                                         */
/*==============================================================*/
CREATE TABLE sys_user_role
(
    id          BIGSERIAL NOT NULL,
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    del_flag    SMALLINT  NOT NULL DEFAULT 0,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT
ON TABLE sys_user_role IS '系统-用户角色关联表';
COMMENT
ON COLUMN sys_user_role.id IS '主键ID';
COMMENT
ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT
ON COLUMN sys_user_role.role_id IS '角色ID';
COMMENT
ON COLUMN sys_user_role.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN sys_user_role.create_by IS '创建人';
COMMENT
ON COLUMN sys_user_role.update_by IS '更新人';
COMMENT
ON COLUMN sys_user_role.create_time IS '创建时间';
COMMENT
ON COLUMN sys_user_role.update_time IS '更新时间';

/*==============================================================*/
/* Table: sys_login_log                                         */
/*==============================================================*/
CREATE TABLE sys_login_log
(
    id         BIGSERIAL NOT NULL,
    username   VARCHAR(50),
    real_name  VARCHAR(50),
    login_ip   VARCHAR(50),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status     SMALLINT  NOT NULL DEFAULT 0,
    msg        VARCHAR(255),
    PRIMARY KEY (id)
);

COMMENT
ON TABLE sys_login_log IS '系统-登录日志表';
COMMENT
ON COLUMN sys_login_log.id IS '主键ID';
COMMENT
ON COLUMN sys_login_log.username IS '用户名';
COMMENT
ON COLUMN sys_login_log.real_name IS '真实姓名';
COMMENT
ON COLUMN sys_login_log.login_ip IS '登录IP地址';
COMMENT
ON COLUMN sys_login_log.login_time IS '登录时间';
COMMENT
ON COLUMN sys_login_log.status IS '登录状态(0成功 1失败)';
COMMENT
ON COLUMN sys_login_log.msg IS '返回消息提示';

/*==============================================================*/
/* Table: sys_operation_log                                     */
/*==============================================================*/
CREATE TABLE sys_operation_log
(
    id          BIGSERIAL NOT NULL,
    username    VARCHAR(50),
    real_name   VARCHAR(50),
    ip_address  VARCHAR(50),
    op_type     VARCHAR(50),
    op_module   VARCHAR(100),
    content     TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT
ON TABLE sys_operation_log IS '系统-操作日志表';
COMMENT
ON COLUMN sys_operation_log.id IS '主键ID';
COMMENT
ON COLUMN sys_operation_log.username IS '用户名';
COMMENT
ON COLUMN sys_operation_log.real_name IS '操作人真实姓名';
COMMENT
ON COLUMN sys_operation_log.ip_address IS '操作源IP';
COMMENT
ON COLUMN sys_operation_log.op_type IS '操作类型字典编码';
COMMENT
ON COLUMN sys_operation_log.op_module IS '操作模块';
COMMENT
ON COLUMN sys_operation_log.content IS '操作描述详情';
COMMENT
ON COLUMN sys_operation_log.create_time IS '操作发生时间';

