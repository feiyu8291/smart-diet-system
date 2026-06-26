package com.diet.modules.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.config.RsaProperties;
import com.diet.modules.common.constant.CacheKeyConstant;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.common.util.PasswordUtil;
import com.diet.modules.common.util.RedisUtil;
import com.diet.modules.common.util.RsaUtil;
import com.diet.modules.common.util.SecurityUtils;
import com.diet.modules.system.mapper.SysUserMapper;
import com.diet.modules.system.model.dto.ChangePasswordDTO;
import com.diet.modules.system.model.dto.SysUserDTO;
import com.diet.modules.system.model.dto.SysUserSimpleDTO;
import com.diet.modules.system.model.entity.SysRole;
import com.diet.modules.system.model.entity.SysUser;
import com.diet.modules.system.model.entity.SysUserRole;
import com.diet.modules.system.model.po.SysUserQueryPO;
import com.diet.modules.system.model.vo.SysUserInfoVO;
import com.diet.modules.system.model.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户 Service 业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserRoleService sysUserRoleService;
    private final SysRoleService sysRoleService;
    private final RsaProperties rsaProperties;

    public SysUser getByUsername(String username) {
        if (CharSequenceUtil.isBlank(username)) {
            return null;
        }
        return lambdaQuery().eq(SysUser::getUsername, username).one();
    }

    public SysUser getByPhoneNum(String phoneNum) {
        if (CharSequenceUtil.isBlank(phoneNum)) {
            return null;
        }
        return lambdaQuery().eq(SysUser::getPhoneNum, phoneNum).one();
    }

    public SysUser getByIdCardNum(String idCardNum) {
        if (CharSequenceUtil.isBlank(idCardNum)) {
            return null;
        }
        return lambdaQuery().eq(SysUser::getIdCardNum, idCardNum).one();
    }

    /**
     * 分页查询用户
     */
    public IPage<SysUserVO> page(SysUserQueryPO queryPO) {
        Page<SysUser> userPage = lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(queryPO.getRealName()), SysUser::getRealName, queryPO.getRealName())
                .like(CharSequenceUtil.isNotBlank(queryPO.getPhoneNum()), SysUser::getPhoneNum, queryPO.getPhoneNum())
                .orderByDesc(SysUser::getCreateTime)
                .page(new Page<>(queryPO.getPageNo(), queryPO.getPageSize()));

        List<SysUser> records = userPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        }

        // 1. 批量获取用户ID
        List<Long> userIds = records.stream().map(SysUser::getUserId).toList();

        // 2. 批量查出用户角色关联
        List<SysUserRole> userRoles = sysUserRoleService.lambdaQuery()
                .in(SysUserRole::getUserId, userIds)
                .list();

        // 3. 批量查出涉及的角色信息并映射为ID -> 角色名的 Map
        Map<Long, String> roleMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
            if (CollUtil.isNotEmpty(roleIds)) {
                roleMap = sysRoleService.lambdaQuery()
                        .in(SysRole::getRoleId, roleIds)
                        .list()
                        .stream()
                        .collect(Collectors.toMap(SysRole::getRoleId,
                                SysRole::getRoleName, (v1, v2) -> v1));
            }
        }

        // 按 userId 分组关联的用户角色
        Map<Long, List<SysUserRole>> userRoleGroup = userRoles.stream().collect(Collectors.groupingBy(SysUserRole::getUserId));

        Map<Long, String> finalRoleMap = roleMap;
        return userPage.convert(user -> {
            SysUserVO vo = new SysUserVO();
            vo.copy(user);

            List<SysUserRole> roles = userRoleGroup.getOrDefault(user.getUserId(), Collections.emptyList());
            List<Long> roleIds = roles.stream().map(SysUserRole::getRoleId).toList();
            List<String> roleNames = roles.stream()
                    .map(ur -> finalRoleMap.get(ur.getRoleId()))
                    .filter(Objects::nonNull)
                    .toList();

            vo.setRoleIds(roleIds);
            vo.setRoleNames(roleNames);
            return vo;
        });
    }

    /**
     * 保存或更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateUser(SysUserDTO dto) {
        Long userId = dto.getUserId();
        boolean isUpdate = Objects.nonNull(userId);

        // 1. 唯一性校验
        this.checkUserUnique(dto, isUpdate);

        // 2. 初始化更新对象
        SysUser entity = this.initUser(userId, isUpdate);

        // 3. 属性拷贝 (排除敏感或只读字段)
        BeanUtil.copyProperties(dto, entity, "userId", "userPassword");

        // 4. 处理新增特有逻辑 (密码、状态)
        if (!isUpdate) {
            if (CharSequenceUtil.isBlank(dto.getUserPassword())) {
                throw BusinessException.withMessageParamsError("密码不能为空");
            }
            // 默认状态：启用 (0代表启用，1代表禁用)
            entity.setUseStatus(0);
            try {
                String decryptedPassword = RsaUtil.decryptByPrivateKey(rsaProperties.getPrivateKey(), dto.getUserPassword());
                entity.setUserPassword(PasswordUtil.encode(decryptedPassword));
            } catch (Exception e) {
                log.error("新增用户密码解密失败", e);
                throw BusinessException.withMessage("安全密码解密失败，请重试！");
            }
        }

        // 5. 保存或更新基本信息
        saveOrUpdate(entity);

        // 6. 处理角色关联 (覆盖式：先删后增)
        if (Objects.nonNull(dto.getRoleIds())) {
            sysUserRoleService.lambdaUpdate().eq(SysUserRole::getUserId, entity.getUserId()).remove();
            List<SysUserRole> userRoles = dto.getRoleIds().stream().map(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(entity.getUserId());
                ur.setRoleId(roleId);
                return ur;
            }).toList();
            if (!userRoles.isEmpty()) {
                sysUserRoleService.saveBatch(userRoles);
            }
        }

        // 7. 清理/刷新缓存
        RedisUtil.del(CacheKeyConstant.USER_CACHE_PREFIX + entity.getUserId());
        RedisUtil.del(CacheKeyConstant.USER_PERM_PREFIX + entity.getUserId());
        this.refreshUserSimpleCache(entity.getUserId());
    }

    /**
     * 校验用户唯一性
     */
    private void checkUserUnique(SysUserDTO dto, boolean isUpdate) {
        // 校验手机号唯一
        boolean existsPhone = lambdaQuery()
                .eq(SysUser::getPhoneNum, dto.getPhoneNum())
                .ne(isUpdate, SysUser::getUserId, dto.getUserId())
                .exists();
        if (existsPhone) {
            throw BusinessException.withMessageParamsError("该手机号已注册");
        }
        // 验证身份证号唯一
        boolean existsIdCardNum = lambdaQuery()
                .eq(SysUser::getIdCardNum, dto.getIdCardNum())
                .ne(isUpdate, SysUser::getUserId, dto.getUserId())
                .exists();
        if (existsIdCardNum) {
            throw BusinessException.withMessageParamsError("该身份证号已存在");
        }
        // 验证用户名唯一
        boolean existsUsername = lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .ne(isUpdate, SysUser::getUserId, dto.getUserId())
                .exists();
        if (existsUsername) {
            throw BusinessException.withMessageParamsError("该用户名已存在");
        }
    }

    /**
     * 初始化用户实体对象
     */
    private SysUser initUser(Long userId, boolean isUpdate) {
        SysUser entity = isUpdate ? getById(userId) : new SysUser();
        if (isUpdate && Objects.isNull(entity)) {
            throw BusinessException.withMessageParamsError("用户不存在");
        }
        return entity;
    }

    /**
     * 启用/禁用用户
     */
    public void updateStatus(Long userId, Integer useStatus) {
        if (Objects.isNull(userId)) {
            throw BusinessException.withMessageParamsError("userId 不能为空");
        }
        lambdaUpdate().eq(SysUser::getUserId, userId).set(SysUser::getUseStatus, useStatus).update();
        // 刷新缓存
        this.refreshUserSimpleCache(userId);
    }

    /**
     * 批量删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw BusinessException.withMessageParamsError("ids 不能为空");
        }
        this.removeByIds(ids);
        // 批量清理缓存
        ids.forEach(id -> {
            RedisUtil.del(CacheKeyConstant.USER_CACHE_PREFIX + id);
            RedisUtil.del(CacheKeyConstant.USER_PERM_PREFIX + id);
        });
    }

    /**
     * 重置用户密码为123456
     */
    public void resetPassword(Long userId) {
        if (Objects.isNull(userId)) {
            throw BusinessException.withMessageParamsError("userId 不能为空");
        }

        SysUser user = getById(userId);
        if (Objects.isNull(user)) {
            throw BusinessException.withMessageParamsError("用户不存在");
        }

        String defaultPasswordHash = PasswordUtil.encode(DEFAULT_PASSWORD);
        lambdaUpdate().eq(SysUser::getUserId, userId).set(SysUser::getUserPassword, defaultPasswordHash).update();
        // 刷新缓存
        this.refreshUserSimpleCache(userId);
    }

    /**
     * 修改密码
     */
    public void changePassword(ChangePasswordDTO dto) {
        if (CharSequenceUtil.isBlank(dto.getUserId())) {
            throw BusinessException.withMessageParamsError("userId 不能为空");
        }

        SysUser user = getById(dto.getUserId());
        if (Objects.isNull(user)) {
            throw BusinessException.withMessageParamsError("用户不存在");
        }

        try {
            // 将前端传入的加密旧密码解密后进行匹配
            String oldPassword = RsaUtil.decryptByPrivateKey(rsaProperties.getPrivateKey(), dto.getOldPassword());
            if (PasswordUtil.mismatch(oldPassword, user.getUserPassword())) {
                throw BusinessException.withMessageParamsError("原密码错误");
            }

            // 将前端传入的加密新密码解密后使用 BCrypt 加密
            String newPassword = RsaUtil.decryptByPrivateKey(rsaProperties.getPrivateKey(), dto.getNewPassword());
            String newPasswordHash = PasswordUtil.encode(newPassword);

            lambdaUpdate().eq(SysUser::getUserId, dto.getUserId()).set(SysUser::getUserPassword, newPasswordHash).update();
            // 刷新缓存
            this.refreshUserSimpleCache(Long.valueOf(dto.getUserId()));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.withMessage("修改密码失败，请检查！");
        }
    }

    /**
     * 获取当前登录用户的信息（VO转换）
     */
    public SysUserInfoVO getCurrentUserInfo() {
        SysUser currentUser = SecurityUtils.getCurrentUser();

        SysUserInfoVO vo = new SysUserInfoVO();
        BeanUtil.copyProperties(currentUser, vo);

        // 获取当前用户的所有角色ID
        List<SysUserRole> userRoles = sysUserRoleService.lambdaQuery()
                .eq(SysUserRole::getUserId, currentUser.getUserId())
                .list();
        if (CollUtil.isNotEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
            vo.setRoleIds(roleIds);
        }
        return vo;
    }

    /**
     * 根据用户 ID 查询用户简要信息（带 Redis 永久缓存）
     */
    public SysUserSimpleDTO getSimpleInfoByIdWithCache(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }

        String userKey = CacheKeyConstant.USER_SIMPLE_CACHE_PREFIX + userId;
        // 1. 优先从缓存获取
        SysUserSimpleDTO simpleDto = RedisUtil.get(userKey, SysUserSimpleDTO.class);
        if (Objects.nonNull(simpleDto)) {
            return simpleDto;
        }

        // 2. 缓存未命中，执行刷新逻辑
        return this.refreshUserSimpleCache(userId);
    }

    /**
     * 刷新用户简要信息缓存
     */
    public SysUserSimpleDTO refreshUserSimpleCache(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }

        SysUser dbUser = getById(userId);
        String cacheKey = CacheKeyConstant.USER_SIMPLE_CACHE_PREFIX + userId;
        if (Objects.isNull(dbUser)) {
            RedisUtil.del(cacheKey);
            return null;
        }

        SysUserSimpleDTO simpleDTO = new SysUserSimpleDTO();
        BeanUtil.copyProperties(dbUser, simpleDTO);

        RedisUtil.set(cacheKey, simpleDTO, CacheKeyConstant.REDIS_KEY_EXPIRE_2_HOUR);
        return simpleDTO;
    }

    /**
     * 根据角色 ID 列表查询可用用户列表
     */
    public List<SysUser> listUsersByRoles(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        List<Long> userIds = sysUserRoleService.lambdaQuery()
                .in(SysUserRole::getRoleId, roleIds)
                .list()
                .stream()
                .map(SysUserRole::getUserId)
                .toList();
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .eq(SysUser::getUseStatus, 0)
                .in(SysUser::getUserId, userIds)
                .orderByAsc(SysUser::getRealName)
                .list();
    }

    /**
     * 根据权限标识查询可用用户列表
     */
    public List<SysUser> listUsersByPermission(String menuCode) {
        if (CharSequenceUtil.isBlank(menuCode)) {
            return Collections.emptyList();
        }
        List<Long> userIds = sysUserRoleService.listUserIdsByMenuCode(menuCode);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .eq(SysUser::getUseStatus, 0)
                .in(SysUser::getUserId, userIds)
                .orderByAsc(SysUser::getRealName)
                .list();
    }

    /**
     * 获取系统中所有启用的系统用户
     */
    public List<SysUser> listAll() {
        return lambdaQuery()
                .eq(SysUser::getUseStatus, 0)
                .orderByAsc(SysUser::getRealName)
                .list();
    }
}
