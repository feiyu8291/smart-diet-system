package com.diet.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.system.model.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联 Mapper 接口
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
