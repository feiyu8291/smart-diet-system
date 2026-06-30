package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_user_wish_dish")
@Schema(description = "成员心愿菜谱关联实体类")
public class DietUserWishDish extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "心愿单ID")
    private Long wishId;
    @Schema(description = "关联 user_health_profile.profile_id (就餐人档案)")
    private Long profileId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "希望就餐日期，可为 NULL")
    private LocalDate wishDate;

}
