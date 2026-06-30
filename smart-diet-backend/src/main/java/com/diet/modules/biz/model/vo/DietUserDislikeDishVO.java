package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户不喜欢/避坑单展示 VO")
public class DietUserDislikeDishVO {
    @Schema(description = "避坑关联ID(对应前端item.id)")
    private Long id;
    @Schema(description = "档案ID")
    private Long profileId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜品ID")
    private Long dishId;
    @Schema(description = "菜品名称")
    private String dishName;
    @Schema(description = "已吐槽次数累计")
    private Integer count;
}
