package com.diet.modules.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.system.mapper.SysRoleMenuMapper;
import com.diet.modules.system.model.entity.SysRoleMenu;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色菜单关联服务业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
public class SysRoleMenuService extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> {

    /**
     * 根据角色ID查询已关联的菜单ID列表
     */
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        return lambdaQuery()
                .eq(SysRoleMenu::getRoleId, roleId)
                .list()
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }
}
