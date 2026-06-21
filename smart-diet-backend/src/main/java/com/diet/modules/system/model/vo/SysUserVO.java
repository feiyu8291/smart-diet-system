package com.diet.modules.system.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息返回 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户分页返回对象")
public class SysUserVO {

    @Schema(description = "用户主键ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "登录手机号")
    private String phoneNum;

    @Schema(description = "用户身份证号")
    private String idCardNum;

    @Schema(description = "用户登录账号")
    private String username;

    @Schema(description = "是否禁用 0否 1是")
    private Integer useStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    public <T> void copy(T source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
