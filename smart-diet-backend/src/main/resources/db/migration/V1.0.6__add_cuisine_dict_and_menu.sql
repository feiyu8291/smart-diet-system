-- ============================================================
-- 1. 表结构更新：在 diet_dish 表中增加 image_ids 字段
-- ============================================================
ALTER TABLE diet_dish
    ADD COLUMN IF NOT EXISTS image_ids VARCHAR(256);
COMMENT ON COLUMN diet_dish.image_ids IS '成品图片ID列表(最多3张，逗号分隔)';

-- ============================================================
-- 2. 初始化菜系数据字典 (sys_data_dictionary)
-- ============================================================
-- 先清理可能已存在的 cuisine_type 字典以防冲突
DELETE
FROM sys_data_dictionary
WHERE data_type = 'cuisine_type';

INSERT INTO sys_data_dictionary (data_type_name, data_type, data_code, data_value, parent_type, parent_code,
                                 data_remark, web_read_only, default_state, init_system_flag, dict_sort, create_by)
VALUES ('菜系类型', 'cuisine_type', 'chuan', '川菜', '', '', '八大菜系-川菜', 1, 0, 1, 1, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'yue', '粤菜', '', '', '八大菜系-粤菜', 1, 0, 1, 2, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'lu', '鲁菜', '', '', '八大菜系-鲁菜', 1, 0, 1, 3, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'su', '苏菜', '', '', '八大菜系-苏菜', 1, 0, 1, 4, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'zhe', '浙菜', '', '', '八大菜系-浙菜', 1, 0, 1, 5, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'xiang', '湘菜', '', '', '八大菜系-湘菜', 1, 0, 1, 6, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'min', '闽菜', '', '', '八大菜系-闽菜', 1, 0, 1, 7, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'hui', '徽菜', '', '', '八大菜系-徽菜', 1, 0, 1, 8, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'light', '减脂餐/轻食', '', '', '特色场景-减脂餐', 1, 0, 1, 9, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'home', '家常菜', '', '', '特色场景-家常菜', 1, 0, 1, 10, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'cheat', '放纵餐', '', '', '特色场景-放纵餐', 1, 0, 1, 11, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'western', '西餐', '', '', '风味场景-西餐', 1, 0, 1, 12, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'jp_kr', '日韩料理', '', '', '风味场景-日韩料理', 1, 0, 1, 13, '系统管理员'),
       ('菜系类型', 'cuisine_type', 'bakery', '面点甜品', '', '', '风味场景-面点甜品', 1, 0, 1, 14, '系统管理员');

-- ============================================================
-- 3. 补充川菜所需的基本原材料及调辅料 (diet_ingredient)
-- ============================================================
INSERT INTO diet_ingredient (ingredient_name, calories, protein, fat, carbs, measure_unit, condiment_flag)
VALUES ('五花肉', 349.00, 13.20, 32.90, 0.00, 'g', 0),
       ('蒜苗', 33.00, 2.10, 0.20, 5.60, 'g', 0),
       ('豆豉', 244.00, 20.00, 9.00, 21.00, 'g', 1),
       ('花生米', 589.00, 24.00, 48.00, 15.00, 'g', 0),
       ('干辣椒', 324.00, 12.00, 8.00, 51.00, 'g', 1),
       ('花椒', 258.00, 11.00, 6.00, 40.00, 'g', 1),
       ('水发木耳', 24.00, 1.50, 0.20, 4.00, 'g', 0),
       ('胡萝卜', 37.00, 1.00, 0.20, 8.00, 'g', 0),
       ('豆芽', 18.00, 2.10, 0.10, 2.00, 'g', 0),
       ('芹菜', 16.00, 0.80, 0.10, 3.00, 'g', 0),
       ('牛杂', 130.00, 16.00, 7.00, 1.00, 'g', 0),
       ('辣椒红油', 800.00, 0.00, 90.00, 5.00, 'ml', 1),
       ('香醋', 31.00, 1.00, 0.00, 6.00, 'ml', 1),
       ('白糖', 400.00, 0.00, 0.00, 99.00, 'g', 1),
       ('鸡肉块', 167.00, 19.30, 9.40, 1.30, 'g', 0),
       ('泡红辣椒', 38.00, 1.20, 0.40, 7.50, 'g', 1),
       ('淀粉', 348.00, 0.10, 0.10, 85.00, 'g', 1),
       ('芝麻', 559.00, 19.40, 46.10, 17.50, 'g', 1),
       ('酸菜', 12.00, 0.90, 0.10, 2.00, 'g', 0),
       ('鱼片', 90.00, 17.60, 2.00, 0.00, 'g', 0)
ON CONFLICT (ingredient_name) DO NOTHING;

-- ============================================================
-- 4. 插入常见川菜主表数据 (diet_dish)
-- ============================================================
INSERT INTO diet_dish (dish_name, cuisine_type, diet_mode, calories, protein, fat, carbs)
VALUES ('经典回锅肉', '川菜', 0, 290.00, 9.50, 26.50, 3.50),
       ('宫保鸡丁', '川菜', 0, 165.00, 14.20, 10.50, 3.80),
       ('经典鱼香肉丝', '川菜', 0, 135.00, 11.50, 8.20, 4.50),
       ('夫妻肺片', '川菜', 0, 185.00, 15.00, 13.00, 2.00),
       ('歌乐山辣子鸡丁', '川菜', 0, 210.00, 16.50, 14.50, 3.00),
       ('川味酸菜鱼', '川菜', 0, 105.00, 12.00, 5.50, 2.20)
ON CONFLICT (dish_name) DO NOTHING;

-- ============================================================
-- 5. 插入菜品食材配比关联数据 (diet_dish_ingredient)
-- ============================================================
-- 先安全清理已有的关联记录，支持重复运行脚本
DELETE
FROM diet_dish_ingredient
WHERE dish_id IN (SELECT dish_id
                  FROM diet_dish
                  WHERE dish_name IN ('经典回锅肉', '宫保鸡丁', '经典鱼香肉丝', '夫妻肺片', '歌乐山辣子鸡丁', '川味酸菜鱼'));

-- 1. 经典回锅肉
INSERT INTO diet_dish_ingredient (dish_id, ingredient_id, use_amount, main_material_flag)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '五花肉'), 200.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '蒜苗'), 80.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '豆瓣酱'), 15.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '豆豉'), 5.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '葱姜蒜'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '食用油'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '白糖'), 3.00, 0);

-- 2. 宫保鸡丁
INSERT INTO diet_dish_ingredient (dish_id, ingredient_id, use_amount, main_material_flag)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '鸡胸肉'), 200.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '花生米'), 50.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '干辣椒'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '花椒'), 3.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '葱姜蒜'), 15.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '生抽'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '食用油'), 12.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '白糖'), 8.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '淀粉'), 5.00, 0);

-- 3. 经典鱼香肉丝
INSERT INTO diet_dish_ingredient (dish_id, ingredient_id, use_amount, main_material_flag)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '猪里脊肉'), 150.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '水发木耳'), 50.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '胡萝卜'), 50.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '青椒'), 50.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '泡红辣椒'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '葱姜蒜'), 15.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '生抽'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '香醋'), 12.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '白糖'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '淀粉'), 5.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '食用油'), 12.00, 0);

-- 4. 夫妻肺片
INSERT INTO diet_dish_ingredient (dish_id, ingredient_id, use_amount, main_material_flag)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '牛里脊'), 100.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '牛杂'), 150.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '辣椒红油'), 25.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '花椒'), 3.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '葱姜蒜'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '生抽'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '芝麻'), 5.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '花生米'), 10.00, 0);

-- 5. 歌乐山辣子鸡丁
INSERT INTO diet_dish_ingredient (dish_id, ingredient_id, use_amount, main_material_flag)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '鸡肉块'), 250.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '干辣椒'), 30.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '花椒'), 5.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '葱姜蒜'), 15.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '生抽'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '食用油'), 25.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '芝麻'), 5.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '白糖'), 3.00, 0);

-- 6. 川味酸菜鱼
INSERT INTO diet_dish_ingredient (dish_id, ingredient_id, use_amount, main_material_flag)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '鱼片'), 300.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '酸菜'), 150.00, 1),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '干辣椒'), 10.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '花椒'), 3.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '葱姜蒜'), 20.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '食用油'), 20.00, 0),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), (SELECT ingredient_id FROM diet_ingredient WHERE ingredient_name = '淀粉'), 5.00, 0);

-- ============================================================
-- 6. 插入菜品烹饪具体加工工序 (diet_dish_step_relation)
-- ============================================================
DELETE
FROM diet_dish_step_relation
WHERE dish_id IN (SELECT dish_id
                  FROM diet_dish
                  WHERE dish_name IN ('经典回锅肉', '宫保鸡丁', '经典鱼香肉丝', '夫妻肺片', '歌乐山辣子鸡丁', '川味酸菜鱼'));

-- 1. 经典回锅肉 步骤
INSERT INTO diet_dish_step_relation (dish_id, step_pool_id, step_num, custom_detail)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), 1, 1, '将五花肉洗净，青蒜苗切成斜段，准备好郫县豆瓣酱和豆豉。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), 3, 2, '锅中加入冷水，放入五花肉、姜片和料酒，大火烧开转中火煮至肉刚断生（约20分钟），捞出晾凉切成薄片。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), 4, 3, '热锅下少许油，放入肉片中火煸炒，至肉片吐油出香、微微卷曲呈“灯盏窝”状。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), 5, 4, '下入郫县豆瓣酱、豆豉和姜蒜片，炒出红油和香味，再调入少许白糖。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典回锅肉'), 6, 5, '最后倒入青蒜苗段，大火快速翻炒至蒜苗断生即可出锅。');

-- 2. 宫保鸡丁 步骤
INSERT INTO diet_dish_step_relation (dish_id, step_pool_id, step_num, custom_detail)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), 1, 1, '将鸡胸肉切成1.5cm见方的丁；大葱切葱段；干辣椒切小段。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), 2, 2, '鸡丁中加入少许盐、生抽、淀粉与料酒，抓匀腌制 10-15 分钟备用。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), 5, 3, '提前调制宫保小荔枝汁：碗中放入白糖、生抽、醋、淀粉和少许水搅拌均匀。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), 4, 4, '热锅冷油，下干辣椒段、花椒爆香，然后下入腌好的鸡丁大火炒至变色。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '宫保鸡丁'), 6, 5, '放入大葱段、熟花生米，倒入调好的汁水，大火收浓芡汁，翻炒均匀即成。');

-- 3. 经典鱼香肉丝 步骤
INSERT INTO diet_dish_step_relation (dish_id, step_pool_id, step_num, custom_detail)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), 1, 1, '猪里脊肉切细丝；木耳、胡萝卜、青椒洗净后全部切成等长等粗的细丝。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), 2, 2, '肉丝加入少许食盐、生抽、料酒与淀粉，用手抓匀码味腌制。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), 5, 3, '调制鱼香味汁：碗中加入白糖、香醋、生抽、水淀粉、少许盐调匀。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), 4, 4, '起油锅，肉丝滑散至变色盛出。底油中爆香葱姜蒜和泡红辣椒碎，再倒入胡萝卜丝、木耳丝和青椒丝炒至断生。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '经典鱼香肉丝'), 6, 5, '重新倒入肉丝，并淋入鱼香味汁，大火翻炒至芡汁粘稠裹匀，撒入葱段即可。');

-- 4. 夫妻肺片 步骤
INSERT INTO diet_dish_step_relation (dish_id, step_pool_id, step_num, custom_detail)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), 1, 1, '牛里脊及牛杂彻底清洗干净。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), 5, 2, '入锅加葱姜、花椒及卤水料，将肉和牛杂煮熟卤透，捞出晾干彻底变凉。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), 1, 3, '将卤好的牛里脊、牛肚、牛舌等用利刃切成极大、极薄的薄片。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), 5, 4, '取红油、生抽、少许卤汤、花椒面混合调制成红油川味拌汁。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '夫妻肺片'), 6, 5, '将肉片和牛杂片装盘整理好，淋上调好的红油汁，撒上花生碎、熟芝麻和香菜即成。');

-- 5. 歌乐山辣子鸡丁 步骤
INSERT INTO diet_dish_step_relation (dish_id, step_pool_id, step_num, custom_detail)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), 1, 1, '将鸡胸肉剁成小指头大小的小方块，干辣椒剪成段备用。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), 2, 2, '鸡块中加入料酒、生抽、少许盐和淀粉，抓匀码味 15 分钟。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), 4, 3, '锅中倒入宽油，烧至七成热，下入鸡丁炸至金黄捞出，再复炸一次使其表面酥脆捞出。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), 4, 4, '锅中留底油，放入花椒、大量干辣椒段和姜蒜末小火炒香。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '歌乐山辣子鸡丁'), 6, 5, '下入鸡块、少许白糖大火快速翻炒均匀，最后撒上熟芝麻出锅。');

-- 6. 川味酸菜鱼 步骤
INSERT INTO diet_dish_step_relation (dish_id, step_pool_id, step_num, custom_detail)
VALUES ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), 1, 1, '草鱼片浆制：鱼片中加入少许盐、料酒、淀粉抓出粘性备用；酸菜切成段，姜蒜切片。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), 4, 2, '热锅放油，下入姜蒜片、干辣椒和花椒炒香，倒入酸菜大火煸炒出酸香味。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), 5, 3, '加入开水或高汤，大火烧开，加少许生抽调味，煮出酸菜的味道（约5分钟）。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), 5, 4, '将酸菜捞出垫底；锅内汤汁微开，改小火将鱼片一片片展平滑入汤中，大火煮熟（约1-2分钟）捞出平铺在酸菜上。'),
       ((SELECT dish_id FROM diet_dish WHERE dish_name = '川味酸菜鱼'), 6, 5, '锅中汤汁浇入盆中，撒上干辣椒和花椒，泼上一勺滚烫的食用油激发出香气。');
