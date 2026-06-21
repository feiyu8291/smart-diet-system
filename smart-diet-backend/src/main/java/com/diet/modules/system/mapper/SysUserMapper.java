package com.diet.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息 Mapper 接口
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
