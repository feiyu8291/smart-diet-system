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
@TableName("diet_cook_skilled_dish")
@Schema(description = "做饭人拿手菜关联实体类")
public class DietCookSkilledDish extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联主键ID")
    private Long relationId;
    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "菜谱ID")
    private Long dishId;

}
