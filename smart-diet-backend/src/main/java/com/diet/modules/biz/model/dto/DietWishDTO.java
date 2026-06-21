package com.diet.modules.biz.model.dto;

import lombok.Data;

/**
 * 意向菜品 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietWishDTO {
    private Long profileId;
    private Long groupId;
    private Long dishId;
    private String wishDate;
}
