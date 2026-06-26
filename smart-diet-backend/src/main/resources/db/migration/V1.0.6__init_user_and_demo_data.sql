-- ============================================================
-- 1. 清理已有的演示用户与演示业务数据
-- ============================================================
DELETE
FROM sys_user
WHERE user_id = 2;
DELETE
FROM sys_user_role
WHERE user_id = 2;

TRUNCATE TABLE diet_family_group RESTART IDENTITY CASCADE;
TRUNCATE TABLE diet_user_health_profile RESTART IDENTITY CASCADE;
TRUNCATE TABLE diet_plan RESTART IDENTITY CASCADE;
TRUNCATE TABLE diet_cook_skilled_dish RESTART IDENTITY CASCADE;
TRUNCATE TABLE diet_dish_like RESTART IDENTITY CASCADE;
TRUNCATE TABLE diet_dish_collect RESTART IDENTITY CASCADE;
TRUNCATE TABLE diet_meal_feedback RESTART IDENTITY CASCADE;

-- ============================================================
-- 2. 预置做饭人演示账户与角色关联
-- ============================================================
-- 做饭人：张大厨 (分配 user_id = 2)
INSERT INTO sys_user (user_id, real_name, phone_num, user_password, username, id_card_num, use_status, del_flag, create_by)
VALUES (2, '张大厨', '13800000000', 'e10adc3949ba59abbe56e057f20f883e', 'cooker', '110101199001011234', 0, 0, '系统管理员')
ON CONFLICT (user_id) DO NOTHING;

-- 关联角色 2 (做饭人)
INSERT INTO sys_user_role (user_id, role_id, del_flag, create_by)
VALUES (2, 2, 0, '系统管理员');

-- ============================================================
-- 3. 预置演示家庭组 (diet_family_group)
-- ============================================================
INSERT INTO diet_family_group (group_id, group_name, creator_user_id, cooldown_days, create_by)
VALUES (1, '健康快乐一家人', 2, 3, '张大厨');

-- ============================================================
-- 4. 预置家庭成员健康档案 (diet_user_health_profile)
-- ============================================================
-- 做饭人：张大厨 (在线，关联 user_id = 2)
INSERT INTO diet_user_health_profile (profile_id, user_id, group_id, group_role, member_name, member_relation,
                                      member_gender, member_height, member_weight, member_birthday, activity_level,
                                      target_weight, diet_speed, bmr_calories, tdee_calories, daily_target_calories, create_by)
VALUES (1, 2, 1, 1, '张大厨', '本人', 1, 175.00, 85.00, '1991-01-01', 2, 75.00, 0.50, 1780.00, 2450.00, 1950.00, '张大厨');

-- 离线就餐成员：李四 (老婆)
INSERT INTO diet_user_health_profile (profile_id, user_id, group_id, group_role, member_name, member_relation,
                                      member_gender, member_height, member_weight, member_birthday, activity_level,
                                      target_weight, diet_speed, bmr_calories, tdee_calories, daily_target_calories, create_by)
VALUES (2, NULL, 1, 2, '李四', '配偶', 2, 162.00, 62.00, '1994-06-15', 1, 52.00, 0.50, 1280.00, 1530.00, 1100.00, '张大厨');

-- 离线就餐成员：小明 (儿子)
INSERT INTO diet_user_health_profile (profile_id, user_id, group_id, group_role, member_name, member_relation,
                                      member_gender, member_height, member_weight, member_birthday, activity_level,
                                      target_weight, diet_speed, bmr_calories, tdee_calories, daily_target_calories, create_by)
VALUES (3, NULL, 1, 2, '小明', '子女', 1, 130.00, 32.00, '2018-03-20', 3, 32.00, 0.00, 1050.00, 1600.00, 1600.00, '张大厨');

-- ============================================================
-- 5. 预置计划模板 (diet_plan)
-- ============================================================
INSERT INTO diet_plan (plan_id, plan_name, total_days, plan_description, create_by)
VALUES (1, '21天极速夏日轻食减脂计划', 21,
        '本计划采用 3天轻食 + 1天正常饮食 的循环模式，帮助家庭成员温和减少油脂与高碳水摄入，主要食材以鸡胸肉、牛肉及西兰花为主。', '系统管理员'),
       (2, '家庭膳食营养调理计划', 14,
        '注重每日微量元素摄入，不限制热量，以正常家常菜系搭配，保障蛋白质与膳食纤维全面平衡。', '系统管理员');

-- ============================================================
-- 6. 预置演示做饭人擅长菜品 (diet_cook_skilled_dish)
-- ============================================================
-- 关联做饭人 user_id = 2
INSERT INTO diet_cook_skilled_dish (user_id, dish_id, create_by)
VALUES (2, 3, '张大厨'), -- 西红柿炒鸡蛋
       (2, 4, '张大厨');
-- 家常麻婆豆腐

-- ============================================================
-- 7. 预置点赞数据 (diet_dish_like)
-- ============================================================
-- 用户 2 (张大厨) 点赞了西兰花牛肉的低卡分支(1)和西红柿炒蛋的传统分支(4)
INSERT INTO diet_dish_like (user_id, branch_id)
VALUES (2, 1),
       (2, 4);

-- ============================================================
-- 8. 预置收藏数据 (diet_dish_collect)
-- ============================================================
-- 用户 2 (张大厨) 收藏了低卡西红柿炒蛋分支(3)
INSERT INTO diet_dish_collect (user_id, branch_id)
VALUES (2, 3);

-- ============================================================
-- 9. 预置就餐打卡反馈数据 (diet_meal_feedback)
-- ============================================================
INSERT INTO diet_meal_feedback (meal_plan_id, profile_id, eat_status, skip_reason, skip_note)
VALUES (101, 1, 1, NULL, NULL),                              -- 张大厨 早餐 已餐
       (101, 2, 2, '工作加班', '昨晚加班太晚，早上没起来吃'), -- 李四 早餐 未餐
       (101, 3, 1, NULL, NULL),                              -- 小明 早餐 已餐
       (102, 1, 1, NULL, NULL),                              -- 张大厨 午餐 已餐
       (102, 2, 2, '外出就餐', '和同事在外面聚餐了');
-- 李四 午餐 未餐

-- ============================================================
-- 10. 重置自增主键序列值
-- ============================================================
SELECT setval('sys_user_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM sys_user));
SELECT setval('sys_user_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_role));
SELECT setval('diet_family_group_group_id_seq', (SELECT COALESCE(MAX(group_id), 1) FROM diet_family_group));
SELECT setval('diet_user_health_profile_profile_id_seq', (SELECT COALESCE(MAX(profile_id), 1) FROM diet_user_health_profile));
SELECT setval('diet_plan_plan_id_seq', (SELECT COALESCE(MAX(plan_id), 1) FROM diet_plan));
SELECT setval('diet_cook_skilled_dish_relation_id_seq', (SELECT COALESCE(MAX(relation_id), 1) FROM diet_cook_skilled_dish));
SELECT setval('diet_dish_like_like_id_seq', (SELECT COALESCE(MAX(like_id), 1) FROM diet_dish_like));
SELECT setval('diet_dish_collect_collect_id_seq', (SELECT COALESCE(MAX(collect_id), 1) FROM diet_dish_collect));
SELECT setval('diet_meal_feedback_feedback_id_seq', (SELECT COALESCE(MAX(feedback_id), 1) FROM diet_meal_feedback));
