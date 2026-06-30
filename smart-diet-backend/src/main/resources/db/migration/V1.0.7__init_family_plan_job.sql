-- ============================================================
-- 初始化家庭膳食计划进度天数自动向前推进任务
-- ============================================================
INSERT INTO sys_quartz_job (job_name, job_group, cron_expression, job_description, handle_class, method_param, del_flag, permanent_state, job_type, create_by)
SELECT '家庭膳食计划进度推进任务',
       'BIZ_JOB',
       '0 0 1 * * ?',
       '每天凌晨1点自动为所有进行中的家庭组膳食计划进度天数向前推进1天',
       'dietFamilyPlanProgressJob',
       '',
       0,
       1,
       2,
       '系统管理员'
WHERE NOT EXISTS (SELECT 1
                  FROM sys_quartz_job
                  WHERE handle_class = 'dietFamilyPlanProgressJob');
