-- ==============================================================
-- 1. 家庭组表 (增加菜系避重冷却配置天数)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_family_group
(
    group_id
    BIGSERIAL
    NOT
    NULL,
    group_name
    VARCHAR
(
    100
) NOT NULL,
    creator_user_id BIGINT NOT NULL,
    cooldown_days INT NOT NULL DEFAULT 7, -- 相同菜系防重复冷却天数
-- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    group_id
)
    );
COMMENT
ON TABLE diet_family_group IS '业务-家庭组表';
COMMENT
ON COLUMN diet_family_group.group_id IS '主键ID';
COMMENT
ON COLUMN diet_family_group.group_name IS '家庭组自定义名称';
COMMENT
ON COLUMN diet_family_group.creator_user_id IS '创建组的做饭人用户ID(关联sys_user.user_id)';
COMMENT
ON COLUMN diet_family_group.cooldown_days IS '相同菜系防重复的冷却避重配置天数';
COMMENT
ON COLUMN diet_family_group.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_family_group.create_by IS '创建人';
COMMENT
ON COLUMN diet_family_group.update_by IS '更新人';
COMMENT
ON COLUMN diet_family_group.create_time IS '创建时间';
COMMENT
ON COLUMN diet_family_group.update_time IS '更新时间';

-- ==============================================================
-- 2. 做饭人擅长菜品关联表 (取代原擅长菜系表)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_cook_skilled_dish
(
    relation_id
    BIGSERIAL
    NOT
    NULL,
    user_id
    BIGINT
    NOT
    NULL, -- 关联 sys_user.user_id (做饭人)
    dish_id
    BIGINT
    NOT
    NULL, -- 关联 diet_dish.dish_id
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    relation_id
),
    CONSTRAINT uniq_cook_skilled_dish UNIQUE
(
    user_id,
    dish_id
)
    );
COMMENT
ON TABLE diet_cook_skilled_dish IS '业务-做饭人擅长菜品关联表';
COMMENT
ON COLUMN diet_cook_skilled_dish.relation_id IS '主键ID';
COMMENT
ON COLUMN diet_cook_skilled_dish.user_id IS '做饭人系统用户ID(关联sys_user.user_id)';
COMMENT
ON COLUMN diet_cook_skilled_dish.dish_id IS '做饭人所擅长的具体菜品ID';
COMMENT
ON COLUMN diet_cook_skilled_dish.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_cook_skilled_dish.create_by IS '创建人';
COMMENT
ON COLUMN diet_cook_skilled_dish.update_by IS '更新人';
COMMENT
ON COLUMN diet_cook_skilled_dish.create_time IS '创建时间';
COMMENT
ON COLUMN diet_cook_skilled_dish.update_time IS '更新时间';

-- ==============================================================
-- 3. 用户个人健康档案表（可关联 sys_user，user_id 可为 NULL 以支持离线成员）
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_user_health_profile
(
    profile_id
    BIGSERIAL
    NOT
    NULL,
    user_id
    BIGINT
    DEFAULT
    NULL, -- 若为离线就餐人，此处填 NULL
    group_id
    BIGINT
    DEFAULT
    0,
    group_role
    SMALLINT
    NOT
    NULL
    DEFAULT
    2,    -- 1-做饭人, 2-普通成员
    member_name
    VARCHAR
(
    50
) NOT NULL, -- 就餐人姓名/称呼
    member_relation VARCHAR
(
    20
), -- 与做饭人的亲属关系
    member_gender SMALLINT NOT NULL, -- 1-男, 2-女
    member_height DECIMAL
(
    5,
    2
) NOT NULL, -- 身高 cm
    member_weight DECIMAL
(
    5,
    2
) NOT NULL, -- 体重 kg
    member_age INT NOT NULL,
    activity_level SMALLINT NOT NULL, -- 1-久坐, 2-轻度活动, 3-中度活动, 4-重度活动
    target_weight DECIMAL
(
    5,
    2
) NOT NULL, -- 目标体重 kg
    diet_speed DECIMAL
(
    3,
    2
) NOT NULL DEFAULT 0.5, -- 减重速度
    bmr_calories DECIMAL
(
    6,
    2
), -- 基础代谢率 kcal
    tdee_calories DECIMAL
(
    6,
    2
), -- 每日总消耗 kcal
    daily_target_calories DECIMAL
(
    6,
    2
), -- 每日目标摄入热量 kcal
-- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    profile_id
)
    );
COMMENT
ON TABLE diet_user_health_profile IS '业务-用户健康档案表';
COMMENT
ON COLUMN diet_user_health_profile.profile_id IS '主键ID';
COMMENT
ON COLUMN diet_user_health_profile.user_id IS '系统用户ID(离线成员为NULL，在线关联sys_user.user_id)';
COMMENT
ON COLUMN diet_user_health_profile.group_id IS '关联家庭组ID';
COMMENT
ON COLUMN diet_user_health_profile.group_role IS '家庭组内的角色(1-做饭人/管理员, 2-普通家庭成员)';
COMMENT
ON COLUMN diet_user_health_profile.member_name IS '吃饭人的名字或称呼';
COMMENT
ON COLUMN diet_user_health_profile.member_relation IS '与做饭人的亲属关系';
COMMENT
ON COLUMN diet_user_health_profile.member_gender IS '成员性别(1-男, 2-女)';
COMMENT
ON COLUMN diet_user_health_profile.member_height IS '成员身高(cm)';
COMMENT
ON COLUMN diet_user_health_profile.member_weight IS '当前实际体重(kg)';
COMMENT
ON COLUMN diet_user_health_profile.member_age IS '成员年龄';
COMMENT
ON COLUMN diet_user_health_profile.activity_level IS '日常活动强度(1-久坐, 2-轻度, 3-中度, 4-重度)';
COMMENT
ON COLUMN diet_user_health_profile.target_weight IS '目标体重(kg)';
COMMENT
ON COLUMN diet_user_health_profile.diet_speed IS '周科学减重速度(kg/周，如0.5)';
COMMENT
ON COLUMN diet_user_health_profile.bmr_calories IS '计算的基础代谢率(kcal)';
COMMENT
ON COLUMN diet_user_health_profile.tdee_calories IS '每日能量总消耗(TDEE, kcal)';
COMMENT
ON COLUMN diet_user_health_profile.daily_target_calories IS '每日推荐目标摄入热量(kcal)';
COMMENT
ON COLUMN diet_user_health_profile.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_user_health_profile.create_by IS '创建人';
COMMENT
ON COLUMN diet_user_health_profile.update_by IS '更新人';
COMMENT
ON COLUMN diet_user_health_profile.create_time IS '创建时间';
COMMENT
ON COLUMN diet_user_health_profile.update_time IS '更新时间';

-- ==============================================================
-- 4. 减脂计划模板表
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_plan
(
    plan_id
    BIGSERIAL
    NOT
    NULL,
    plan_name
    VARCHAR
(
    100
) NOT NULL,
    total_days INT NOT NULL,
    plan_description TEXT,
    -- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    plan_id
)
    );
COMMENT
ON TABLE diet_plan IS '业务-减脂计划模板表';
COMMENT
ON COLUMN diet_plan.plan_id IS '主键ID';
COMMENT
ON COLUMN diet_plan.plan_name IS '计划名称';
COMMENT
ON COLUMN diet_plan.total_days IS '计划天数';
COMMENT
ON COLUMN diet_plan.plan_description IS '计划说明简介';
COMMENT
ON COLUMN diet_plan.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_plan.create_by IS '创建人';
COMMENT
ON COLUMN diet_plan.update_by IS '更新人';
COMMENT
ON COLUMN diet_plan.create_time IS '创建时间';
COMMENT
ON COLUMN diet_plan.update_time IS '更新时间';

-- ==============================================================
-- 5. 家庭计划执行进度表 (current_day 由 Quartz 定时更新)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_family_plan_progress
(
    progress_id
    BIGSERIAL
    NOT
    NULL,
    group_id
    BIGINT
    NOT
    NULL,
    plan_id
    BIGINT
    NOT
    NULL,
    start_date
    DATE
    NOT
    NULL,
    current_day
    INT
    NOT
    NULL
    DEFAULT
    1,
    progress_status
    SMALLINT
    NOT
    NULL
    DEFAULT
    1, -- 1-进行中, 2-已完成, 3-已中断
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    progress_id
)
    );
COMMENT
ON TABLE diet_family_plan_progress IS '业务-家庭计划执行进度表';
COMMENT
ON COLUMN diet_family_plan_progress.progress_id IS '主键ID';
COMMENT
ON COLUMN diet_family_plan_progress.group_id IS '关联的家庭组ID';
COMMENT
ON COLUMN diet_family_plan_progress.plan_id IS '关联的计划模板ID';
COMMENT
ON COLUMN diet_family_plan_progress.start_date IS '计划启动的公历日期';
COMMENT
ON COLUMN diet_family_plan_progress.current_day IS '当前执行处于第几天(每天由定时任务自动累加计算)';
COMMENT
ON COLUMN diet_family_plan_progress.progress_status IS '执行进度状态(1-进行中, 2-已完成, 3-已中断)';
COMMENT
ON COLUMN diet_family_plan_progress.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_family_plan_progress.create_by IS '创建人';
COMMENT
ON COLUMN diet_family_plan_progress.update_by IS '更新人';
COMMENT
ON COLUMN diet_family_plan_progress.create_time IS '创建时间';
COMMENT
ON COLUMN diet_family_plan_progress.update_time IS '更新时间';

-- ==============================================================
-- 6. 食材/配料字典表 (高复用食材库)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_ingredient
(
    ingredient_id
    BIGSERIAL
    NOT
    NULL,
    ingredient_name
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    calories DECIMAL
(
    6,
    2
) NOT NULL,
    protein DECIMAL
(
    5,
    2
) NOT NULL,
    fat DECIMAL
(
    5,
    2
) NOT NULL,
    carbs DECIMAL
(
    5,
    2
) NOT NULL,
    measure_unit VARCHAR
(
    10
) DEFAULT 'g',
    condiment_flag SMALLINT DEFAULT 0, -- 0-主材料, 1-调料
-- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    ingredient_id
)
    );
COMMENT
ON TABLE diet_ingredient IS '业务-食材配料字典表';
COMMENT
ON COLUMN diet_ingredient.ingredient_id IS '主键ID';
COMMENT
ON COLUMN diet_ingredient.ingredient_name IS '食材名称';
COMMENT
ON COLUMN diet_ingredient.calories IS '每100克/毫升热量(kcal)';
COMMENT
ON COLUMN diet_ingredient.protein IS '每100克/毫升蛋白质(g)';
COMMENT
ON COLUMN diet_ingredient.fat IS '每100克/毫升脂肪(g)';
COMMENT
ON COLUMN diet_ingredient.carbs IS '每100克/毫升碳水(g)';
COMMENT
ON COLUMN diet_ingredient.measure_unit IS '计量单位(如g/ml)';
COMMENT
ON COLUMN diet_ingredient.condiment_flag IS '调味辅料标识(0-主配料, 1-调味品)';
COMMENT
ON COLUMN diet_ingredient.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_ingredient.create_by IS '创建人';
COMMENT
ON COLUMN diet_ingredient.update_by IS '更新人';
COMMENT
ON COLUMN diet_ingredient.create_time IS '创建时间';
COMMENT
ON COLUMN diet_ingredient.update_time IS '更新时间';

-- ==============================================================
-- 7. 菜品表
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_dish
(
    dish_id
    BIGSERIAL
    NOT
    NULL,
    dish_name
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    cuisine_type VARCHAR
(
    50
) NOT NULL,
    diet_mode SMALLINT NOT NULL DEFAULT 0, -- 0-正常饮食, 1-轻食减脂, 2-放纵餐
    calories DECIMAL
(
    6,
    2
) NOT NULL,
    protein DECIMAL
(
    5,
    2
) NOT NULL,
    fat DECIMAL
(
    5,
    2
) NOT NULL,
    carbs DECIMAL
(
    5,
    2
) NOT NULL,
    cover_image_id BIGINT, -- 关联 sys_file_storage.storage_id
-- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    dish_id
)
    );
COMMENT
ON TABLE diet_dish IS '业务-菜品表';
COMMENT
ON COLUMN diet_dish.dish_id IS '主键ID';
COMMENT
ON COLUMN diet_dish.dish_name IS '菜品名称';
COMMENT
ON COLUMN diet_dish.cuisine_type IS '菜系类型';
COMMENT
ON COLUMN diet_dish.diet_mode IS '建议就餐模式(0-正常饮食, 1-轻食减脂, 2-放纵餐)';
COMMENT
ON COLUMN diet_dish.calories IS '每100克成品菜热量(kcal)';
COMMENT
ON COLUMN diet_dish.protein IS '每100克成品含蛋白质(g)';
COMMENT
ON COLUMN diet_dish.fat IS '每100克成品含脂肪(g)';
COMMENT
ON COLUMN diet_dish.carbs IS '每100克成品含碳水(g)';
COMMENT
ON COLUMN diet_dish.cover_image_id IS '封面图片ID(关联sys_file_storage.storage_id)';
COMMENT
ON COLUMN diet_dish.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_dish.create_by IS '创建人';
COMMENT
ON COLUMN diet_dish.update_by IS '更新人';
COMMENT
ON COLUMN diet_dish.create_time IS '创建时间';
COMMENT
ON COLUMN diet_dish.update_time IS '更新时间';

-- ==============================================================
-- 8. 菜品-食材关联表
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_dish_ingredient
(
    relation_id
    BIGSERIAL
    NOT
    NULL,
    dish_id
    BIGINT
    NOT
    NULL,
    ingredient_id
    BIGINT
    NOT
    NULL,
    use_amount
    DECIMAL
(
    6,
    2
) NOT NULL,
    main_material_flag SMALLINT DEFAULT 1,
    -- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    relation_id
)
    );
COMMENT
ON TABLE diet_dish_ingredient IS '业务-菜品食材关联表';
COMMENT
ON COLUMN diet_dish_ingredient.relation_id IS '主键ID';
COMMENT
ON COLUMN diet_dish_ingredient.dish_id IS '关联菜品主键ID';
COMMENT
ON COLUMN diet_dish_ingredient.ingredient_id IS '关联食材主键ID';
COMMENT
ON COLUMN diet_dish_ingredient.use_amount IS '食材用量';
COMMENT
ON COLUMN diet_dish_ingredient.main_material_flag IS '主料/配料标识(1-核心主料, 0-辅料调味)';
COMMENT
ON COLUMN diet_dish_ingredient.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_dish_ingredient.create_by IS '创建人';
COMMENT
ON COLUMN diet_dish_ingredient.update_by IS '更新人';
COMMENT
ON COLUMN diet_dish_ingredient.create_time IS '创建时间';
COMMENT
ON COLUMN diet_dish_ingredient.update_time IS '更新时间';

-- ==============================================================
-- 9. 做饭人个人菜品烹饪熟练度与拿手菜统计表 (绑定做饭人用户ID)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_cook_dish_stat
(
    stat_id
    BIGSERIAL
    NOT
    NULL,
    user_id
    BIGINT
    NOT
    NULL, -- 关联 sys_user.user_id (做饭人)
    dish_id
    BIGINT
    NOT
    NULL, -- 关联 diet_dish.dish_id
    cook_count
    INT
    NOT
    NULL
    DEFAULT
    0,    -- 烹饪次数
    signature_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,    -- 0-普通, 1-拿手菜
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    stat_id
),
    CONSTRAINT uniq_cook_dish_stat UNIQUE
(
    user_id,
    dish_id
)
    );
COMMENT
ON TABLE diet_cook_dish_stat IS '业务-做饭人个人菜品烹饪熟练度与拿手菜表';
COMMENT
ON COLUMN diet_cook_dish_stat.stat_id IS '主键ID';
COMMENT
ON COLUMN diet_cook_dish_stat.user_id IS '做饭人系统用户ID(关联sys_user.user_id)';
COMMENT
ON COLUMN diet_cook_dish_stat.dish_id IS '关联的菜品ID';
COMMENT
ON COLUMN diet_cook_dish_stat.cook_count IS '该做饭人烹饪此菜的累计次数';
COMMENT
ON COLUMN diet_cook_dish_stat.signature_flag IS '拿手菜标志(0-非拿手菜, 1-该做饭人的拿手菜)';
COMMENT
ON COLUMN diet_cook_dish_stat.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_cook_dish_stat.create_by IS '创建人';
COMMENT
ON COLUMN diet_cook_dish_stat.update_by IS '更新人';
COMMENT
ON COLUMN diet_cook_dish_stat.create_time IS '创建时间';
COMMENT
ON COLUMN diet_cook_dish_stat.update_time IS '更新时间';

-- ==============================================================
-- 10. 烹饪步骤标准模板池
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_cooking_step_pool
(
    step_pool_id
    BIGSERIAL
    NOT
    NULL,
    step_name
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    step_detail TEXT NOT NULL,
    -- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    step_pool_id
)
    );
COMMENT
ON TABLE diet_cooking_step_pool IS '业务-烹饪步骤模板池';
COMMENT
ON COLUMN diet_cooking_step_pool.step_pool_id IS '主键ID';
COMMENT
ON COLUMN diet_cooking_step_pool.step_name IS '标准步骤名称(如:焯水)';
COMMENT
ON COLUMN diet_cooking_step_pool.step_detail IS '默认步骤详细描述';
COMMENT
ON COLUMN diet_cooking_step_pool.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_cooking_step_pool.create_by IS '创建人';
COMMENT
ON COLUMN diet_cooking_step_pool.update_by IS '更新人';
COMMENT
ON COLUMN diet_cooking_step_pool.create_time IS '创建时间';
COMMENT
ON COLUMN diet_cooking_step_pool.update_time IS '更新时间';

-- ==============================================================
-- 11. 菜品烹饪步骤关联表
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_dish_step_relation
(
    relation_id
    BIGSERIAL
    NOT
    NULL,
    dish_id
    BIGINT
    NOT
    NULL,
    step_pool_id
    BIGINT
    NOT
    NULL,
    step_num
    INT
    NOT
    NULL,
    custom_detail
    TEXT,
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    relation_id
)
    );
COMMENT
ON TABLE diet_dish_step_relation IS '业务-菜品烹饪步骤关联表';
COMMENT
ON COLUMN diet_dish_step_relation.relation_id IS '主键ID';
COMMENT
ON COLUMN diet_dish_step_relation.dish_id IS '关联菜品主键ID';
COMMENT
ON COLUMN diet_dish_step_relation.step_pool_id IS '关联步骤模板ID';
COMMENT
ON COLUMN diet_dish_step_relation.step_num IS '步骤序号';
COMMENT
ON COLUMN diet_dish_step_relation.custom_detail IS '菜品专属描述(为空取模板描述)';
COMMENT
ON COLUMN diet_dish_step_relation.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_dish_step_relation.create_by IS '创建人';
COMMENT
ON COLUMN diet_dish_step_relation.update_by IS '更新人';
COMMENT
ON COLUMN diet_dish_step_relation.create_time IS '创建时间';
COMMENT
ON COLUMN diet_dish_step_relation.update_time IS '更新时间';

-- ==============================================================
-- 12. 用户“想吃计划”菜品表 (增加想吃就餐目标日期，关联 profile_id)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_user_wish_dish
(
    wish_id
    BIGSERIAL
    NOT
    NULL,
    profile_id
    BIGINT
    NOT
    NULL, -- 关联 diet_user_health_profile.profile_id
    group_id
    BIGINT
    NOT
    NULL,
    dish_id
    BIGINT
    NOT
    NULL,
    wish_date
    DATE
    DEFAULT
    NULL, -- 希望就餐的目标日期，允许为 NULL (表示不限定日期)
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    wish_id
)
    );
COMMENT
ON TABLE diet_user_wish_dish IS '业务-用户想吃计划菜品表';
COMMENT
ON COLUMN diet_user_wish_dish.wish_id IS '主键ID';
COMMENT
ON COLUMN diet_user_wish_dish.profile_id IS '就餐人档案ID(关联user_health_profile.profile_id)';
COMMENT
ON COLUMN diet_user_wish_dish.group_id IS '关联家庭组ID';
COMMENT
ON COLUMN diet_user_wish_dish.dish_id IS '关联想吃的菜品ID';
COMMENT
ON COLUMN diet_user_wish_dish.wish_date IS '期望能吃上该菜品的指定日期(离线/在线做饭匹配)';
COMMENT
ON COLUMN diet_user_wish_dish.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_user_wish_dish.create_by IS '创建人';
COMMENT
ON COLUMN diet_user_wish_dish.update_by IS '更新人';
COMMENT
ON COLUMN diet_user_wish_dish.create_time IS '创建时间';
COMMENT
ON COLUMN diet_user_wish_dish.update_time IS '更新时间';

-- ==============================================================
-- 13. 用户“不喜欢菜品”登记表 (被标记3次彻底屏蔽，关联 profile_id)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_user_dislike_dish
(
    dislike_id
    BIGSERIAL
    NOT
    NULL,
    profile_id
    BIGINT
    NOT
    NULL, -- 关联 diet_user_health_profile.profile_id
    group_id
    BIGINT
    NOT
    NULL,
    dish_id
    BIGINT
    NOT
    NULL,
    dislike_count
    SMALLINT
    NOT
    NULL
    DEFAULT
    1,
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    dislike_id
)
    );
COMMENT
ON TABLE diet_user_dislike_dish IS '业务-用户不喜欢菜品偏好表';
COMMENT
ON COLUMN diet_user_dislike_dish.dislike_id IS '主键ID';
COMMENT
ON COLUMN diet_user_dislike_dish.profile_id IS '就餐人健康档案ID(关联user_health_profile.profile_id)';
COMMENT
ON COLUMN diet_user_dislike_dish.group_id IS '关联的家庭组ID';
COMMENT
ON COLUMN diet_user_dislike_dish.dish_id IS '关联不喜欢的菜品ID';
COMMENT
ON COLUMN diet_user_dislike_dish.dislike_count IS '标记不喜欢此菜的累计次数(3次及以上彻底拉黑)';
COMMENT
ON COLUMN diet_user_dislike_dish.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_user_dislike_dish.create_by IS '创建人';
COMMENT
ON COLUMN diet_user_dislike_dish.update_by IS '更新人';
COMMENT
ON COLUMN diet_user_dislike_dish.create_time IS '创建时间';
COMMENT
ON COLUMN diet_user_dislike_dish.update_time IS '更新时间';

-- ==============================================================
-- 14. 用户上传自制菜品成品图片表 (MinIO 优先覆盖)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_user_dish_image
(
    image_id
    BIGSERIAL
    NOT
    NULL,
    group_id
    BIGINT
    NOT
    NULL,
    dish_id
    BIGINT
    NOT
    NULL,
    storage_id
    BIGINT
    NOT
    NULL, -- 关联系统已有 sys_file_storage.storage_id
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    image_id
),
    CONSTRAINT uniq_group_dish UNIQUE
(
    group_id,
    dish_id
)
    );
COMMENT
ON TABLE diet_user_dish_image IS '业务-用户上传自制菜品成品图片表';
COMMENT
ON COLUMN diet_user_dish_image.image_id IS '主键ID';
COMMENT
ON COLUMN diet_user_dish_image.group_id IS '所属的家庭组ID';
COMMENT
ON COLUMN diet_user_dish_image.dish_id IS '成品关联菜品ID';
COMMENT
ON COLUMN diet_user_dish_image.storage_id IS '文件存储ID(关联sys_file_storage.storage_id)';
COMMENT
ON COLUMN diet_user_dish_image.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_user_dish_image.create_by IS '创建人';
COMMENT
ON COLUMN diet_user_dish_image.update_by IS '更新人';
COMMENT
ON COLUMN diet_user_dish_image.create_time IS '创建时间';
COMMENT
ON COLUMN diet_user_dish_image.update_time IS '更新时间';

-- ==============================================================
-- 15. 家庭膳食每日生成食谱表
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_family_meal_plan
(
    meal_plan_id
    BIGSERIAL
    NOT
    NULL,
    group_id
    BIGINT
    NOT
    NULL,
    meal_date
    DATE
    NOT
    NULL,
    meal_period
    SMALLINT
    NOT
    NULL, -- 1-早餐, 2-午餐, 3-晚餐
    meal_diet_mode
    SMALLINT
    NOT
    NULL, -- 0-正常, 1-轻食, 2-放纵
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    meal_plan_id
)
    );
COMMENT
ON TABLE diet_family_meal_plan IS '业务-家庭膳食每日生成食谱表';
COMMENT
ON COLUMN diet_family_meal_plan.meal_plan_id IS '主键ID';
COMMENT
ON COLUMN diet_family_meal_plan.group_id IS '家庭组ID';
COMMENT
ON COLUMN diet_family_meal_plan.meal_date IS '用餐日期';
COMMENT
ON COLUMN diet_family_meal_plan.meal_period IS '餐次(1-早餐, 2-午餐, 3-晚餐)';
COMMENT
ON COLUMN diet_family_meal_plan.meal_diet_mode IS '就餐模式(0-正常, 1-轻食, 2-放纵)';
COMMENT
ON COLUMN diet_family_meal_plan.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_family_meal_plan.create_by IS '创建人';
COMMENT
ON COLUMN diet_family_meal_plan.update_by IS '更新人';
COMMENT
ON COLUMN diet_family_meal_plan.create_time IS '创建时间';
COMMENT
ON COLUMN diet_family_meal_plan.update_time IS '更新时间';

-- ==============================================================
-- 16. 家庭每日生成食谱-菜品关联表 (中间表)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_family_meal_plan_dish
(
    relation_id
    BIGSERIAL
    NOT
    NULL,
    meal_plan_id
    BIGINT
    NOT
    NULL,
    dish_id
    BIGINT
    NOT
    NULL,
    -- 审计字段
    del_flag
    SMALLINT
    NOT
    NULL
    DEFAULT
    0,
    create_by
    VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    relation_id
)
    );
COMMENT
ON TABLE diet_family_meal_plan_dish IS '业务-每日生成食谱-菜品关联表';
COMMENT
ON COLUMN diet_family_meal_plan_dish.relation_id IS '主键ID';
COMMENT
ON COLUMN diet_family_meal_plan_dish.meal_plan_id IS '膳食计划ID';
COMMENT
ON COLUMN diet_family_meal_plan_dish.dish_id IS '包含的菜品ID';
COMMENT
ON COLUMN diet_family_meal_plan_dish.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_family_meal_plan_dish.create_by IS '创建人';
COMMENT
ON COLUMN diet_family_meal_plan_dish.update_by IS '更新人';
COMMENT
ON COLUMN diet_family_meal_plan_dish.create_time IS '创建时间';
COMMENT
ON COLUMN diet_family_meal_plan_dish.update_time IS '更新时间';

-- ==============================================================
-- 17. 家庭膳食采购明细表 (中间表，完全废除 JSON)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_family_meal_grocery
(
    grocery_id
    BIGSERIAL
    NOT
    NULL,
    meal_plan_id
    BIGINT
    NOT
    NULL,
    ingredient_id
    BIGINT
    NOT
    NULL,
    use_amount
    DECIMAL
(
    8,
    2
) NOT NULL, -- 累计采购量
-- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    grocery_id
)
    );
COMMENT
ON TABLE diet_family_meal_grocery IS '业务-家庭膳食采购明细表';
COMMENT
ON COLUMN diet_family_meal_grocery.grocery_id IS '主键ID';
COMMENT
ON COLUMN diet_family_meal_grocery.meal_plan_id IS '关联食谱计划ID';
COMMENT
ON COLUMN diet_family_meal_grocery.ingredient_id IS '关联食材ID';
COMMENT
ON COLUMN diet_family_meal_grocery.use_amount IS '本次膳食烹饪需购买的累计克数/毫升数';
COMMENT
ON COLUMN diet_family_meal_grocery.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_family_meal_grocery.create_by IS '创建人';
COMMENT
ON COLUMN diet_family_meal_grocery.update_by IS '更新人';
COMMENT
ON COLUMN diet_family_meal_grocery.create_time IS '创建时间';
COMMENT
ON COLUMN diet_family_meal_grocery.update_time IS '更新时间';

-- ==============================================================
-- 18. 家庭分餐食量推荐表 (主体关联至 profile_id，兼容离线成员)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_family_meal_portion
(
    portion_id
    BIGSERIAL
    NOT
    NULL,
    meal_plan_id
    BIGINT
    NOT
    NULL,
    profile_id
    BIGINT
    NOT
    NULL, -- 对应 diet_user_health_profile.profile_id
    dish_id
    BIGINT
    NOT
    NULL,
    recommend_weight
    DECIMAL
(
    6,
    2
) NOT NULL,
    -- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    portion_id
)
    );
COMMENT
ON TABLE diet_family_meal_portion IS '业务-家庭分餐食量推荐表';
COMMENT
ON COLUMN diet_family_meal_portion.portion_id IS '主键ID';
COMMENT
ON COLUMN diet_family_meal_portion.meal_plan_id IS '关联食谱计划ID';
COMMENT
ON COLUMN diet_family_meal_portion.profile_id IS '就餐人健康档案ID(关联user_health_profile.profile_id)';
COMMENT
ON COLUMN diet_family_meal_portion.dish_id IS '就餐菜品ID';
COMMENT
ON COLUMN diet_family_meal_portion.recommend_weight IS '推荐食用量克数(g)';
COMMENT
ON COLUMN diet_family_meal_portion.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_family_meal_portion.create_by IS '创建人';
COMMENT
ON COLUMN diet_family_meal_portion.update_by IS '更新人';
COMMENT
ON COLUMN diet_family_meal_portion.create_time IS '创建时间';
COMMENT
ON COLUMN diet_family_meal_portion.update_time IS '更新时间';

-- ==============================================================
-- 19. 体重记录历史表 (主体关联至 profile_id，仅做客观测重记录)
-- ==============================================================
CREATE TABLE IF NOT EXISTS diet_weight_record
(
    record_id
    BIGSERIAL
    NOT
    NULL,
    profile_id
    BIGINT
    NOT
    NULL, -- 对应 diet_user_health_profile.profile_id
    record_weight
    DECIMAL
(
    5,
    2
) NOT NULL,
    record_date DATE NOT NULL,
    plan_progress_id BIGINT DEFAULT 0,
    -- 审计字段
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR
(
    64
),
    update_by VARCHAR
(
    64
),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    record_id
)
    );
COMMENT
ON TABLE diet_weight_record IS '业务-用户体重记录历史表';
COMMENT
ON COLUMN diet_weight_record.record_id IS '主键ID';
COMMENT
ON COLUMN diet_weight_record.profile_id IS '就餐人健康档案ID(关联user_health_profile.profile_id)';
COMMENT
ON COLUMN diet_weight_record.record_weight IS '实际称重(kg)';
COMMENT
ON COLUMN diet_weight_record.record_date IS '测重录入日期';
COMMENT
ON COLUMN diet_weight_record.plan_progress_id IS '关联执行计划进度ID(0为日常测重)';
COMMENT
ON COLUMN diet_weight_record.del_flag IS '删除标识(0否1是)';
COMMENT
ON COLUMN diet_weight_record.create_by IS '创建人';
COMMENT
ON COLUMN diet_weight_record.update_by IS '更新人';
COMMENT
ON COLUMN diet_weight_record.create_time IS '创建时间';
COMMENT
ON COLUMN diet_weight_record.update_time IS '更新时间';


-- ==============================================================
-- 初始化预置数据
-- ==============================================================

-- 1. 预置系统用户 (sys_user)
INSERT INTO sys_user (user_id, real_name, phone_num, user_password, username, id_card_num, use_status)
VALUES (1, '张大厨', '13800000000', 'e10adc3949ba59abbe56e057f20f883e', 'cooker', '110101199001011234',
        0) ON CONFLICT (user_id) DO NOTHING;

-- 2. 预置演示家庭组
INSERT INTO diet_family_group (group_id, group_name, creator_user_id, cooldown_days)
VALUES (1, '健康快乐一家人', 1, 3);

-- 3. 预置家庭成员健康档案 (diet_user_health_profile)
-- group_role: 1-做饭人, 2-普通成员
-- activity_level: 1久坐, 2轻度, 3中度, 4重度
-- target_calories 基于公式推断并设定基础：BMR*Activity_Level - Diet_Speed*500
-- 做饭人：张大厨 (在线)
INSERT INTO diet_user_health_profile (profile_id, user_id, group_id, group_role, member_name, member_relation,
                                      member_gender, member_height, member_weight, member_age, activity_level,
                                      target_weight, diet_speed, bmr_calories, tdee_calories, daily_target_calories)
VALUES (1, 1, 1, 1, '张大厨', '本人', 1, 175.00, 85.00, 35, 2, 75.00, 0.50, 1780.00, 2450.00, 1950.00);
-- 离线就餐成员：李四 (老婆)
INSERT INTO diet_user_health_profile (profile_id, user_id, group_id, group_role, member_name, member_relation,
                                      member_gender, member_height, member_weight, member_age, activity_level,
                                      target_weight, diet_speed, bmr_calories, tdee_calories, daily_target_calories)
VALUES (2, NULL, 1, 2, '李四', '配偶', 2, 162.00, 62.00, 32, 1, 52.00, 0.50, 1280.00, 1530.00, 1100.00);
-- 离线就餐成员：小明 (儿子)
INSERT INTO diet_user_health_profile (profile_id, user_id, group_id, group_role, member_name, member_relation,
                                      member_gender, member_height, member_weight, member_age, activity_level,
                                      target_weight, diet_speed, bmr_calories, tdee_calories, daily_target_calories)
VALUES (3, NULL, 1, 2, '小明', '子女', 1, 130.00, 32.00, 8, 3, 32.00, 0.00, 1050.00, 1600.00, 1600.00);

-- 4. 预置计划模板 (diet_plan)
INSERT INTO diet_plan (plan_id, plan_name, total_days, plan_description)
VALUES (1, '21天极速夏日轻食减脂计划', 21,
        '本计划采用 3天轻食 + 1天正常饮食 的循环模式，帮助家庭成员温和减少油脂与高碳水摄入，主要食材以鸡胸肉、牛肉及西兰花为主。');
INSERT INTO diet_plan (plan_id, plan_name, total_days, plan_description)
VALUES (2, '家庭膳食营养调理计划', 14,
        '注重每日微量元素摄入，不限制热量，以正常家常菜系搭配，保障蛋白质与膳食纤维全面平衡。');

-- 5. 预置食材字典 (diet_ingredient)
-- 每 100g 对应的热量(kcal), 蛋白质(g), 脂肪(g), 碳水(g)
INSERT INTO diet_ingredient (ingredient_id, ingredient_name, calories, protein, fat, carbs, measure_unit,
                             condiment_flag)
VALUES (1, '鸡胸肉', 133.00, 24.60, 1.90, 0.60, 'g', 0),
       (2, '牛里脊', 106.00, 22.30, 1.80, 0.20, 'g', 0),
       (3, '西兰花', 34.00, 4.10, 0.60, 4.30, 'g', 0),
       (4, '西红柿', 15.00, 0.90, 0.20, 3.30, 'g', 0),
       (5, '鸡蛋', 143.00, 13.30, 8.80, 2.80, 'g', 0),
       (6, '豆腐', 82.00, 8.10, 3.70, 4.20, 'g', 0),
       (7, '青椒', 23.00, 1.00, 0.30, 4.50, 'g', 0);
