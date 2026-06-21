/*==============================================================*/
/* DBMS name:      PostgreSQL                                   */
/*==============================================================*/

DROP TABLE IF EXISTS qrtz_fired_triggers;
DROP TABLE IF EXISTS qrtz_paused_trigger_grps;
DROP TABLE IF EXISTS qrtz_scheduler_state;
DROP TABLE IF EXISTS qrtz_locks;
DROP TABLE IF EXISTS qrtz_simple_triggers;
DROP TABLE IF EXISTS qrtz_cron_triggers;
DROP TABLE IF EXISTS qrtz_simprop_triggers;
DROP TABLE IF EXISTS qrtz_blob_triggers;
DROP TABLE IF EXISTS qrtz_triggers;
DROP TABLE IF EXISTS qrtz_job_details;
DROP TABLE IF EXISTS qrtz_calendars;

/*==============================================================*/
/* Table: qrtz_job_details                                      */
/*==============================================================*/
CREATE TABLE qrtz_job_details
(
    sched_name        VARCHAR(120) NOT NULL,
    job_name          VARCHAR(200) NOT NULL,
    job_group         VARCHAR(200) NOT NULL,
    description       VARCHAR(250) NULL,
    job_class_name    VARCHAR(250) NOT NULL,
    is_durable        BOOL         NOT NULL,
    is_nonconcurrent  BOOL         NOT NULL,
    is_update_data    BOOL         NOT NULL,
    requests_recovery BOOL         NOT NULL,
    job_data          BYTEA        NULL,
    PRIMARY KEY (sched_name, job_name, job_group)
);

COMMENT ON TABLE qrtz_job_details IS 'Quartz-任务详细信息表';
COMMENT ON COLUMN qrtz_job_details.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_job_details.job_name IS '任务名称';
COMMENT ON COLUMN qrtz_job_details.job_group IS '任务分组';
COMMENT ON COLUMN qrtz_job_details.description IS '任务描述';
COMMENT ON COLUMN qrtz_job_details.job_class_name IS '任务执行类(全路径)';
COMMENT ON COLUMN qrtz_job_details.is_durable IS '持久化标识(0否1是)';
COMMENT ON COLUMN qrtz_job_details.is_nonconcurrent IS '并发控制标识(0否1是)';
COMMENT ON COLUMN qrtz_job_details.is_update_data IS '数据更新标识(0否1是)';
COMMENT ON COLUMN qrtz_job_details.requests_recovery IS '故障恢复标识(0否1是)';
COMMENT ON COLUMN qrtz_job_details.job_data IS '任务参数(二进制)';

CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details (sched_name, requests_recovery);
CREATE INDEX idx_qrtz_j_grp ON qrtz_job_details (sched_name, job_group);

/*==============================================================*/
/* Table: qrtz_triggers                                         */
/*==============================================================*/
CREATE TABLE qrtz_triggers
(
    sched_name     VARCHAR(120) NOT NULL,
    trigger_name   VARCHAR(200) NOT NULL,
    trigger_group  VARCHAR(200) NOT NULL,
    job_name       VARCHAR(200) NOT NULL,
    job_group      VARCHAR(200) NOT NULL,
    description    VARCHAR(250) NULL,
    next_fire_time BIGINT       NULL,
    prev_fire_time BIGINT       NULL,
    priority       INTEGER      NULL,
    trigger_state  VARCHAR(16)  NOT NULL,
    trigger_type   VARCHAR(8)   NOT NULL,
    start_time     BIGINT       NOT NULL,
    end_time       BIGINT       NULL,
    calendar_name  VARCHAR(200) NULL,
    misfire_instr  SMALLINT     NULL,
    job_data       BYTEA        NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, job_name, job_group) REFERENCES qrtz_job_details (sched_name, job_name, job_group)
);

COMMENT ON TABLE qrtz_triggers IS 'Quartz-触发器信息表';
COMMENT ON COLUMN qrtz_triggers.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_triggers.trigger_name IS '触发器名称';
COMMENT ON COLUMN qrtz_triggers.trigger_group IS '触发器分组';
COMMENT ON COLUMN qrtz_triggers.job_name IS '任务名称';
COMMENT ON COLUMN qrtz_triggers.job_group IS '任务分组';
COMMENT ON COLUMN qrtz_triggers.description IS '触发器描述';
COMMENT ON COLUMN qrtz_triggers.next_fire_time IS '下次触发时间';
COMMENT ON COLUMN qrtz_triggers.prev_fire_time IS '上次触发时间';
COMMENT ON COLUMN qrtz_triggers.priority IS '优先级';
COMMENT ON COLUMN qrtz_triggers.trigger_state IS '触发器状态';
COMMENT ON COLUMN qrtz_triggers.trigger_type IS '触发器类型';
COMMENT ON COLUMN qrtz_triggers.start_time IS '有效开始时间';
COMMENT ON COLUMN qrtz_triggers.end_time IS '有效结束时间';
COMMENT ON COLUMN qrtz_triggers.calendar_name IS '日历名称';
COMMENT ON COLUMN qrtz_triggers.misfire_instr IS '过期补偿指令';
COMMENT ON COLUMN qrtz_triggers.job_data IS '任务参数(二进制)';

CREATE INDEX idx_qrtz_t_j ON qrtz_triggers (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_t_jg ON qrtz_triggers (sched_name, job_group);
CREATE INDEX idx_qrtz_t_c ON qrtz_triggers (sched_name, calendar_name);
CREATE INDEX idx_qrtz_t_g ON qrtz_triggers (sched_name, trigger_group);
CREATE INDEX idx_qrtz_t_state ON qrtz_triggers (sched_name, trigger_state);
CREATE INDEX idx_qrtz_t_n_state ON qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_n_g_state ON qrtz_triggers (sched_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers (sched_name, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers (sched_name, trigger_state, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);

/*==============================================================*/
/* Table: qrtz_simple_triggers                                  */
/*==============================================================*/
CREATE TABLE qrtz_simple_triggers
(
    sched_name      VARCHAR(120) NOT NULL,
    trigger_name    VARCHAR(200) NOT NULL,
    trigger_group   VARCHAR(200) NOT NULL,
    repeat_count    BIGINT       NOT NULL,
    repeat_interval BIGINT       NOT NULL,
    times_triggered BIGINT       NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

COMMENT ON TABLE qrtz_simple_triggers IS 'Quartz-简单触发器表';
COMMENT ON COLUMN qrtz_simple_triggers.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_simple_triggers.trigger_name IS '触发器名称';
COMMENT ON COLUMN qrtz_simple_triggers.trigger_group IS '触发器分组';
COMMENT ON COLUMN qrtz_simple_triggers.repeat_count IS '重复次数';
COMMENT ON COLUMN qrtz_simple_triggers.repeat_interval IS '重复间隔(毫秒)';
COMMENT ON COLUMN qrtz_simple_triggers.times_triggered IS '已触发次数';

/*==============================================================*/
/* Table: qrtz_cron_triggers                                    */
/*==============================================================*/
CREATE TABLE qrtz_cron_triggers
(
    sched_name      VARCHAR(120) NOT NULL,
    trigger_name    VARCHAR(200) NOT NULL,
    trigger_group   VARCHAR(200) NOT NULL,
    cron_expression VARCHAR(120) NOT NULL,
    time_zone_id    VARCHAR(80)  NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

COMMENT ON TABLE qrtz_cron_triggers IS 'Quartz-Cron触发器表';
COMMENT ON COLUMN qrtz_cron_triggers.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_cron_triggers.trigger_name IS '触发器名称';
COMMENT ON COLUMN qrtz_cron_triggers.trigger_group IS '触发器分组';
COMMENT ON COLUMN qrtz_cron_triggers.cron_expression IS 'Cron表达式';
COMMENT ON COLUMN qrtz_cron_triggers.time_zone_id IS '时区ID';

/*==============================================================*/
/* Table: qrtz_simprop_triggers                                 */
/*==============================================================*/
CREATE TABLE qrtz_simprop_triggers
(
    sched_name    VARCHAR(120)   NOT NULL,
    trigger_name  VARCHAR(200)   NOT NULL,
    trigger_group VARCHAR(200)   NOT NULL,
    str_prop_1    VARCHAR(512)   NULL,
    str_prop_2    VARCHAR(512)   NULL,
    str_prop_3    VARCHAR(512)   NULL,
    int_prop_1    INT            NULL,
    int_prop_2    INT            NULL,
    long_prop_1   BIGINT         NULL,
    long_prop_2   BIGINT         NULL,
    dec_prop_1    DECIMAL(13, 4) NULL,
    dec_prop_2    DECIMAL(13, 4) NULL,
    bool_prop_1   BOOL           NULL,
    bool_prop_2   BOOL           NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

COMMENT ON TABLE qrtz_simprop_triggers IS 'Quartz-自定义属性表';
COMMENT ON COLUMN qrtz_simprop_triggers.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_simprop_triggers.trigger_name IS '触发器名称';
COMMENT ON COLUMN qrtz_simprop_triggers.trigger_group IS '触发器分组';
COMMENT ON COLUMN qrtz_simprop_triggers.str_prop_1 IS '字符参数1';
COMMENT ON COLUMN qrtz_simprop_triggers.str_prop_2 IS '字符参数2';
COMMENT ON COLUMN qrtz_simprop_triggers.str_prop_3 IS '字符参数3';
COMMENT ON COLUMN qrtz_simprop_triggers.int_prop_1 IS '整数参数1';
COMMENT ON COLUMN qrtz_simprop_triggers.int_prop_2 IS '整数参数2';
COMMENT ON COLUMN qrtz_simprop_triggers.long_prop_1 IS '长整参数1';
COMMENT ON COLUMN qrtz_simprop_triggers.long_prop_2 IS '长整参数2';
COMMENT ON COLUMN qrtz_simprop_triggers.dec_prop_1 IS '数值参数1';
COMMENT ON COLUMN qrtz_simprop_triggers.dec_prop_2 IS '数值参数2';
COMMENT ON COLUMN qrtz_simprop_triggers.bool_prop_1 IS '布尔参数1';
COMMENT ON COLUMN qrtz_simprop_triggers.bool_prop_2 IS '布尔参数2';

/*==============================================================*/
/* Table: qrtz_blob_triggers                                    */
/*==============================================================*/
CREATE TABLE qrtz_blob_triggers
(
    sched_name    VARCHAR(120) NOT NULL,
    trigger_name  VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    blob_data     BYTEA        NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

COMMENT ON TABLE qrtz_blob_triggers IS 'Quartz-Blob触发器表';
COMMENT ON COLUMN qrtz_blob_triggers.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_blob_triggers.trigger_name IS '触发器名称';
COMMENT ON COLUMN qrtz_blob_triggers.trigger_group IS '触发器分组';
COMMENT ON COLUMN qrtz_blob_triggers.blob_data IS '二进制触发数据';

/*==============================================================*/
/* Table: qrtz_calendars                                        */
/*==============================================================*/
CREATE TABLE qrtz_calendars
(
    sched_name    VARCHAR(120) NOT NULL,
    calendar_name VARCHAR(200) NOT NULL,
    calendar      BYTEA        NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);

COMMENT ON TABLE qrtz_calendars IS 'Quartz-日历排除表';
COMMENT ON COLUMN qrtz_calendars.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_calendars.calendar_name IS '日历名称';
COMMENT ON COLUMN qrtz_calendars.calendar IS '日历排除数据';

/*==============================================================*/
/* Table: qrtz_paused_trigger_grps                               */
/*==============================================================*/
CREATE TABLE qrtz_paused_trigger_grps
(
    sched_name    VARCHAR(120) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

COMMENT ON TABLE qrtz_paused_trigger_grps IS 'Quartz-暂停分组表';
COMMENT ON COLUMN qrtz_paused_trigger_grps.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_paused_trigger_grps.trigger_group IS '暂停的分组名';

/*==============================================================*/
/* Table: qrtz_fired_triggers                                   */
/*==============================================================*/
CREATE TABLE qrtz_fired_triggers
(
    sched_name        VARCHAR(120) NOT NULL,
    entry_id          VARCHAR(95)  NOT NULL,
    trigger_name      VARCHAR(200) NOT NULL,
    trigger_group     VARCHAR(200) NOT NULL,
    instance_name     VARCHAR(200) NOT NULL,
    fired_time        BIGINT       NOT NULL,
    sched_time        BIGINT       NOT NULL,
    priority          INTEGER      NOT NULL,
    state             VARCHAR(16)  NOT NULL,
    job_name          VARCHAR(200) NULL,
    job_group         VARCHAR(200) NULL,
    is_nonconcurrent  BOOL         NULL,
    requests_recovery BOOL         NULL,
    PRIMARY KEY (sched_name, entry_id)
);

COMMENT ON TABLE qrtz_fired_triggers IS 'Quartz-任务执行记录表';
COMMENT ON COLUMN qrtz_fired_triggers.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_fired_triggers.entry_id IS '执行条目ID';
COMMENT ON COLUMN qrtz_fired_triggers.trigger_name IS '触发器名称';
COMMENT ON COLUMN qrtz_fired_triggers.trigger_group IS '触发器分组';
COMMENT ON COLUMN qrtz_fired_triggers.instance_name IS '节点实例名称';
COMMENT ON COLUMN qrtz_fired_triggers.fired_time IS '实际触发时间';
COMMENT ON COLUMN qrtz_fired_triggers.sched_time IS '计划调度时间';
COMMENT ON COLUMN qrtz_fired_triggers.priority IS '优先级';
COMMENT ON COLUMN qrtz_fired_triggers.state IS '执行状态';
COMMENT ON COLUMN qrtz_fired_triggers.job_name IS '任务名称';
COMMENT ON COLUMN qrtz_fired_triggers.job_group IS '任务分组';
COMMENT ON COLUMN qrtz_fired_triggers.is_nonconcurrent IS '并发控制标识(0否1是)';
COMMENT ON COLUMN qrtz_fired_triggers.requests_recovery IS '故障恢复标识(0否1是)';

CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers (sched_name, instance_name);
CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers (sched_name, instance_name, requests_recovery);
CREATE INDEX idx_qrtz_ft_j_g ON qrtz_fired_triggers (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_ft_jg ON qrtz_fired_triggers (sched_name, job_group);
CREATE INDEX idx_qrtz_ft_t_g ON qrtz_fired_triggers (sched_name, trigger_name, trigger_group);
CREATE INDEX idx_qrtz_ft_tg ON qrtz_fired_triggers (sched_name, trigger_group);

/*==============================================================*/
/* Table: qrtz_scheduler_state                                  */
/*==============================================================*/
CREATE TABLE qrtz_scheduler_state
(
    sched_name        VARCHAR(120) NOT NULL,
    instance_name     VARCHAR(200) NOT NULL,
    last_checkin_time BIGINT       NOT NULL,
    checkin_interval  BIGINT       NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

COMMENT ON TABLE qrtz_scheduler_state IS 'Quartz-节点状态表';
COMMENT ON COLUMN qrtz_scheduler_state.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_scheduler_state.instance_name IS '节点实例名称';
COMMENT ON COLUMN qrtz_scheduler_state.last_checkin_time IS '末次检入时间';
COMMENT ON COLUMN qrtz_scheduler_state.checkin_interval IS '心跳检入间隔';

/*==============================================================*/
/* Table: qrtz_locks                                            */
/*==============================================================*/
CREATE TABLE qrtz_locks
(
    sched_name VARCHAR(120) NOT NULL,
    lock_name  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (sched_name, lock_name)
);

COMMENT ON TABLE qrtz_locks IS 'Quartz-集群锁表';
COMMENT ON COLUMN qrtz_locks.sched_name IS '调度器名称';
COMMENT ON COLUMN qrtz_locks.lock_name IS '分布式锁名称';
