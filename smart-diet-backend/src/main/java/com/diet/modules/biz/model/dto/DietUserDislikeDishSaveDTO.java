package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户不喜欢/避坑单保存 DTO")
public class DietUserDislikeDishSaveDTO {
    @Schema(description = "就餐人档案ID")
    private Long profileId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜品ID")
    private Long dishId;
}
