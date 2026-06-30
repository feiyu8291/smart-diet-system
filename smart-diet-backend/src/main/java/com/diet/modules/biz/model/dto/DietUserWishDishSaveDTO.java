package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户心愿单保存 DTO")
public class DietUserWishDishSaveDTO {
    @Schema(description = "就餐人档案ID")
    private Long profileId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜品ID")
    private Long dishId;
    @Schema(description = "做法分支ID")
    private Long branchId;
    @Schema(description = "就餐期望日期，格式：yyyy-MM-dd")
    private String wishDate;
    @Schema(description = "备注留言")
    private String wishNote;
}
