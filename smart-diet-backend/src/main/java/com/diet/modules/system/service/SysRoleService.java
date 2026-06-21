package com.diet.modules.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.system.mapper.SysRoleMapper;
import com.diet.modules.system.model.dto.SysRoleDTO;
import com.diet.modules.system.model.dto.SysRoleMenuDTO;
import com.diet.modules.system.model.entity.SysRole;
import com.diet.modules.system.model.entity.SysRoleMenu;
import com.diet.modules.system.model.po.SysRoleQueryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 角色服务业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleMenuService sysRoleMenuService;

    /**
     * 分页查询
     */
    public Page<SysRole> page(SysRoleQueryPO queryPO) {
        return lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(queryPO.getRoleName()), SysRole::getRoleName, queryPO.getRoleName())
                .orderByDesc(SysRole::getCreateTime)
                .orderByDesc(SysRole::getRoleId)
                .page(new Page<>(queryPO.getPageNo(), queryPO.getPageSize()));
    }

    /**
     * 查询全部角色
     */
    public List<SysRole> listAll() {
        return lambdaQuery()
                .orderByDesc(SysRole::getCreateTime)
                .orderByDesc(SysRole::getRoleId)
                .list();
    }

    /**
     * 保存或更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateRole(SysRoleDTO dto) {
        Long roleId = dto.getRoleId();
        boolean isUpdate = Objects.nonNull(roleId);

        // 1. 唯一性校验
        this.checkRoleNameUnique(dto.getRoleName(), roleId, isUpdate);

        // 2. 初始化更新对象
        SysRole entity = this.initRole(roleId, isUpdate);

        // 3. 属性拷贝
        BeanUtil.copyProperties(dto, entity, "roleId");

        // 4. 保存或更新角色基本信息
        saveOrUpdate(entity);

        // 5. 处理菜单绑定逻辑
        if (isUpdate) {
            sysRoleMenuService.lambdaUpdate().eq(SysRoleMenu::getRoleId, entity.getRoleId()).remove();
        }
        bindMenus(entity.getRoleId(), dto.getMenuIds());
    }

    /**
     * 校验角色名称唯一性
     */
    private void checkRoleNameUnique(String roleName, Long roleId, boolean isUpdate) {
        boolean exists = lambdaQuery()
                .eq(SysRole::getRoleName, roleName)
                .ne(isUpdate, SysRole::getRoleId, roleId)
                .exists();
        if (exists) {
            throw BusinessException.withMessageParamsError("角色名称已存在");
        }
    }

    /**
     * 初始化角色实体对象
     */
    private SysRole initRole(Long roleId, boolean isUpdate) {
        SysRole entity = isUpdate ? getById(roleId) : new SysRole();
        if (isUpdate && Objects.isNull(entity)) {
            throw BusinessException.withMessageParamsError("角色不存在");
        }
        return entity;
    }

    /**
     * 批量删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw BusinessException.withMessageParamsError("ids 不能为空");
        }
        super.removeByIds(ids);
        sysRoleMenuService.lambdaUpdate().in(SysRoleMenu::getRoleId, ids).remove();
    }

    /**
     * 为角色配置菜单（先删后增）
     */
    @Transactional(rollbackFor = Exception.class)
    public void configMenus(SysRoleMenuDTO dto) {
        SysRole role = getById(dto.getRoleId());
        if (Objects.isNull(role)) {
            throw BusinessException.withMessageParamsError("角色不存在");
        }
        sysRoleMenuService.lambdaUpdate().eq(SysRoleMenu::getRoleId, dto.getRoleId()).remove();
        bindMenus(dto.getRoleId(), dto.getMenuIds());
    }

    /**
     * 绑定菜单列表
     */
    private void bindMenus(Long roleId, List<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return;
        }
        List<SysRoleMenu> roleMenus = menuIds.stream().map(menuId -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            return rm;
        }).toList();
        sysRoleMenuService.saveBatch(roleMenus);
    }
}
