package com.diet.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.system.model.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联 Mapper 接口
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据权限标识查询拥有该权限的所有用户ID
     */
    List<Long> selectUserIdsByMenuCode(@Param("menuCode") String menuCode);

    /**
     * 根据用户ID和权限标识，判断用户是否具有对应权限数量
     */
    int countByUserIdAndMenuCode(@Param("userId") Long userId, @Param("menuCode") String menuCode);
}
