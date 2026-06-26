package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 擅长/拿手菜管理 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "拿手菜关系设置传输对象")
public class DietSkilledDTO {
    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "1-是, 0-否")
    private Integer isSkilled;
}
