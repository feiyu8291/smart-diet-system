package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户心愿单展示 VO")
public class DietUserWishDishVO {
    @Schema(description = "心愿单ID(对应前端item.id)")
    private Long id;
    @Schema(description = "档案ID")
    private Long profileId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜品ID")
    private Long dishId;
    @Schema(description = "做法分支ID")
    private Long branchId;
    @Schema(description = "菜品做法展示名称")
    private String dishName;
    @Schema(description = "就餐期望日期")
    private String wishDate;
    @Schema(description = "留言备注")
    private String wishNote;
}
