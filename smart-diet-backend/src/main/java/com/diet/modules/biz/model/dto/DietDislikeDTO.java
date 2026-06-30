package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 忌口标记 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "忌口/偏好设置保存传输对象")
public class DietDislikeDTO {
    @Schema(description = "就餐成员档案ID")
    private Long profileId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜谱ID")
    private Long dishId;
}
