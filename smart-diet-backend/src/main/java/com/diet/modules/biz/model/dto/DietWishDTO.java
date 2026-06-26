package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 意向菜品 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "心愿菜保存传输对象")
public class DietWishDTO {
    @Schema(description = "profileId")
    private Long profileId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "wishDate")
    private String wishDate;
}
