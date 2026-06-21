package com.diet.modules.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.system.mapper.SysUserRoleMapper;
import com.diet.modules.system.model.entity.SysUserRole;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户角色关联服务业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
public class SysUserRoleService extends ServiceImpl<SysUserRoleMapper, SysUserRole> {

    /**
     * 根据用户ID查询关联的角色ID列表
     */
    public List<Long> listRoleIdsByUserId(Long userId) {
        return lambdaQuery()
                .eq(SysUserRole::getUserId, userId)
                .list()
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    /**
     * 根据权限标识查询拥有该权限的所有用户ID
     */
    public List<Long> listUserIdsByMenuCode(String menuCode) {
        return baseMapper.selectUserIdsByMenuCode(menuCode);
    }

    /**
     * 根据用户ID和权限标识，判断用户是否具有对应权限
     */
    public boolean hasPermission(Long userId, String menuCode) {
        return baseMapper.countByUserIdAndMenuCode(userId, menuCode) > 0;
    }
}
