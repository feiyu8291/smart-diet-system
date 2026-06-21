package com.diet.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.system.model.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单信息 Mapper 接口
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
