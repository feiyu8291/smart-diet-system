package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_cook_dish_stat")
@Schema(description = "做饭人菜谱统计实体类")
public class DietCookDishStat extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "统计主键ID")
    private Long statId;
    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "做饭次数")
    private Integer cookCount;
    @Schema(description = "拿手菜标志 (0-非拿手菜, 1-该做饭人的拿手菜)")
    private Integer signatureFlag;

}
