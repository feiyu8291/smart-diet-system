package com.diet.modules.biz.model.dto;

import lombok.Data;

/**
 * 忌口标记 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietDislikeDTO {
    private Long profileId;
    private Long groupId;
    private Long dishId;
}
