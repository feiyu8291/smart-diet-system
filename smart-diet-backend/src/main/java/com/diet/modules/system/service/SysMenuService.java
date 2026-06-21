package com.diet.modules.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.system.mapper.SysMenuMapper;
import com.diet.modules.system.model.dto.SysMenuDTO;
import com.diet.modules.system.model.entity.SysMenu;
import com.diet.modules.system.model.vo.SysMenuVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 Service 业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    /**
     * 查询所有菜单并构建树形结构
     */
    public List<SysMenuVO> getAllTree() {
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getMenuId));
        return buildTree(all, null);
    }

    /**
     * 构建菜单树（按 parentId 递归）
     */
    private List<SysMenuVO> buildTree(List<SysMenu> all, Long parentId) {
        Map<Long, List<SysMenu>> groupByParent = all.stream()
                .collect(Collectors.groupingBy(m -> Objects.isNull(m.getParentId()) ? 0L : m.getParentId()));

        Long key = Objects.isNull(parentId) ? 0L : parentId;
        List<SysMenu> children = groupByParent.getOrDefault(key, new ArrayList<>());
        return children.stream().map(menu -> {
            SysMenuVO vo = new SysMenuVO();
            vo.copy(menu);
            vo.setChildren(buildTree(all, menu.getMenuId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 保存或更新菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateMenu(SysMenuDTO dto) {
        Long menuId = dto.getMenuId();
        boolean isUpdate = Objects.nonNull(menuId);

        // 1. 唯一性校验
        this.checkMenuCodeUnique(dto.getMenuCode(), menuId, isUpdate);

        // 2. 初始化更新对象
        SysMenu entity = this.initMenu(menuId, isUpdate);

        // 3. 属性拷贝
        BeanUtil.copyProperties(dto, entity, "menuId");

        // 4. 保存或更新
        saveOrUpdate(entity);
    }

    /**
     * 初始化菜单实体对象
     */
    private SysMenu initMenu(Long menuId, boolean isUpdate) {
        SysMenu entity = isUpdate ? getById(menuId) : new SysMenu();
        if (isUpdate && Objects.isNull(entity)) {
            throw BusinessException.withMessageParamsError("菜单不存在");
        }
        return entity;
    }

    /**
     * 校验 menuCode 唯一性
     */
    private void checkMenuCodeUnique(String menuCode, Long menuId, boolean isUpdate) {
        if (CharSequenceUtil.isBlank(menuCode)) {
            return;
        }
        boolean exists = lambdaQuery()
                .eq(SysMenu::getMenuCode, menuCode)
                .ne(isUpdate, SysMenu::getMenuId, menuId)
                .exists();
        if (exists) {
            throw BusinessException.withMessageParamsError("菜单编码已存在：" + menuCode);
        }
    }

    /**
     * 批量删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw BusinessException.withMessageParamsError("ids 不能为空");
        }
        removeByIds(ids);
    }
}
