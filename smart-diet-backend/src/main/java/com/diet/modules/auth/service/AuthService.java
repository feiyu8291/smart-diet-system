package com.diet.modules.auth.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.diet.modules.auth.model.dto.LoginDTO;
import com.diet.modules.auth.model.vo.LoginVO;
import com.diet.modules.auth.model.vo.SysPermissionVO;
import com.diet.modules.auth.security.SecurityProperties;
import com.diet.modules.common.config.RsaProperties;
import com.diet.modules.common.constant.CacheKeyConstant;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.common.util.JwtUtil;
import com.diet.modules.common.util.PasswordUtil;
import com.diet.modules.common.util.RedisUtil;
import com.diet.modules.common.util.RsaUtil;
import com.diet.modules.system.model.entity.SysMenu;
import com.diet.modules.system.model.entity.SysRoleMenu;
import com.diet.modules.system.model.entity.SysUser;
import com.diet.modules.system.model.entity.SysUserRole;
import com.diet.modules.system.model.vo.SysMenuTreeVO;
import com.diet.modules.system.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证服务 Service 业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService sysUserService;
    private final SysRoleMenuService sysRoleMenuService;
    private final SysUserRoleService sysUserRoleService;
    private final SysMenuService sysMenuService;
    private final SecurityProperties securityProperties;
    private final SysLoginLogService sysLoginLogService;
    private final RsaProperties rsaProperties;

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO dto) {
        if (Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getUsername())) {
            throw BusinessException.withMessage("用户名不能为空");
        }

        String username = dto.getUsername();
        String ip = getClientIp();
        try {
            // 根据用户名/手机号/身份证/工号查询用户
            SysUser user = sysUserService.lambdaQuery()
                    .nested(w -> w.eq(SysUser::getPhoneNum, username)
                            .or().eq(SysUser::getUsername, username)
                            .or().eq(SysUser::getIdCardNum, username))
                    .one();

            if (Objects.isNull(user)) {
                sysLoginLogService.recordLoginLog(username, "未知用户", ip, 1, "账号或密码错误");
                throw BusinessException.withMessageParamsError("账号或密码错误");
            }

            // 校验用户状态
            try {
                this.checkUserStatus(user);
            } catch (BusinessException e) {
                sysLoginLogService.recordLoginLog(user.getUsername(), user.getRealName(), ip, 1, e.getMessage());
                throw e;
            }

            // 校验密码
            try {
                this.verifyPassword(dto.getUserPassword(), user.getUserPassword());
            } catch (BusinessException e) {
                sysLoginLogService.recordLoginLog(user.getUsername(), user.getRealName(), ip, 1, e.getMessage());
                throw e;
            }

            // 组装并返回登录结果
            sysLoginLogService.recordLoginLog(user.getUsername(), user.getRealName(), ip, 0, "登录成功");
            return this.assembleLoginResult(user);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            sysLoginLogService.recordLoginLog(username, "登录异常", ip, 1, e.getMessage());
            throw e;
        }
    }

    /**
     * 模拟登录（通过身份证号）
     */
    public LoginVO loginByIdCardNum(String idCardNum) {
        if (CharSequenceUtil.isBlank(idCardNum)) {
            throw BusinessException.withMessage("身份证号不能为空");
        }

        String ip = getClientIp();
        try {
            SysUser user = sysUserService.lambdaQuery()
                    .eq(SysUser::getIdCardNum, idCardNum)
                    .one();

            if (Objects.isNull(user)) {
                sysLoginLogService.recordLoginLog(idCardNum, "未知身份证号用户", ip, 1, "身份证号不存在");
                throw BusinessException.withMessage("未找到该身份证对应的用户");
            }

            // 校验用户状态
            try {
                this.checkUserStatus(user);
            } catch (BusinessException e) {
                sysLoginLogService.recordLoginLog(user.getUsername(), user.getRealName(), ip, 1, e.getMessage());
                throw e;
            }

            // 组装并返回登录结果
            sysLoginLogService.recordLoginLog(user.getUsername(), user.getRealName(), ip, 0, "身份证免密登录成功");
            return this.assembleLoginResult(user);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            sysLoginLogService.recordLoginLog(idCardNum, "免密登录异常", ip, 1, e.getMessage());
            throw e;
        }
    }

    private void checkUserStatus(SysUser user) {
        if (Objects.nonNull(user.getUseStatus()) && user.getUseStatus() == 1) {
            throw BusinessException.withMessageParamsError("该账户已被禁用，请联系管理员");
        }
    }

    private void verifyPassword(String rawPassword, String encodedPassword) {
        try {
            String decryptedPassword = RsaUtil.decryptByPrivateKey(rsaProperties.getPrivateKey(), rawPassword);
            boolean isMatch = PasswordUtil.matches(decryptedPassword, encodedPassword);
            if (!isMatch) {
                throw BusinessException.withMessageParamsError("用户名或密码错误");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码RSA解密校验异常: {}", e.getMessage(), e);
            throw BusinessException.withMessage("密码安全解密异常，请重试！");
        }
    }

    /**
     * 获取 RSA 公钥 (从配置的 PKCS8 格式私钥中派生出 X509 格式公钥)
     */
    public String getPublicKey() {
        try {
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(
                    org.apache.tomcat.util.codec.binary.Base64.decodeBase64(rsaProperties.getPrivateKey()));
            java.security.KeyFactory factory = java.security.KeyFactory.getInstance("RSA");
            java.security.interfaces.RSAPrivateCrtKey privateKey = (java.security.interfaces.RSAPrivateCrtKey) factory.generatePrivate(spec);
            java.security.spec.RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privateKey.getModulus(),
                    privateKey.getPublicExponent());
            java.security.PublicKey publicKey = factory.generatePublic(publicKeySpec);
            return org.apache.tomcat.util.codec.binary.Base64.encodeBase64String(publicKey.getEncoded());
        } catch (Exception e) {
            log.error("从私钥提取公钥失败: {}", e.getMessage(), e);
            throw BusinessException.withMessage("系统安全参数配置异常");
        }
    }


    private LoginVO assembleLoginResult(SysUser user) {
        List<String> permUrls = this.loadUserPermissions(user.getUserId());
        String token = JwtUtil.generateToken(user.getUserId());
        long expireSeconds = securityProperties.getTokenExpire();

        RedisUtil.set(CacheKeyConstant.USER_CACHE_PREFIX + user.getUserId(), user, expireSeconds);
        RedisUtil.set(CacheKeyConstant.USER_PERM_PREFIX + user.getUserId(), permUrls, expireSeconds);

        return LoginVO.builder()
                .token(token)
                .userId(user.getUserId())
                .realName(user.getRealName())
                .loginPhone(user.getPhoneNum())
                .permUrls(permUrls)
                .expireAt(LocalDateTime.now().plusSeconds(expireSeconds))
                .sysUser(user)
                .build();
    }

    public void logout(String token) {
        if (CharSequenceUtil.isBlank(token)) {
            return;
        }
        Long userId = JwtUtil.parseUserId(token);
        if (Objects.nonNull(userId)) {
            RedisUtil.del(CacheKeyConstant.USER_CACHE_PREFIX + userId);
            RedisUtil.del(CacheKeyConstant.USER_PERM_PREFIX + userId);
            log.info("用户 {} 已退出登录", userId);
        }
    }

    public List<String> loadUserPermissions(Long userId) {
        List<SysMenu> menus = this.getMenuListByUserId(userId);
        List<String> permissions = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (CharSequenceUtil.isNotBlank(menu.getRequestUrl())) {
                permissions.add(menu.getRequestUrl());
            }
            if (CharSequenceUtil.isNotBlank(menu.getMenuCode())) {
                permissions.add(menu.getMenuCode());
            }
        }
        return permissions.stream().distinct().toList();
    }

    public List<SysMenu> getMenuListByUserId(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleService.lambdaQuery().eq(SysUserRole::getUserId, userId).list();
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());

        List<SysRoleMenu> roleMenus = sysRoleMenuService.lambdaQuery().in(SysRoleMenu::getRoleId, roleIds).list();
        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());

        return sysMenuService.lambdaQuery().in(SysMenu::getMenuId, menuIds).orderByAsc(SysMenu::getSortOrder).list();
    }

    public SysPermissionVO getPermissionAndMenuList(SysUser currentUser) {
        Long userId = currentUser.getUserId();
        SysPermissionVO permissionVO = new SysPermissionVO()
                .setCurrentUser(currentUser)
                .setPermUrls(new ArrayList<>())
                .setMenuCodes(new ArrayList<>());
        List<SysMenu> menuList = this.getMenuListByUserId(userId);
        if (CollUtil.isEmpty(menuList)) {
            return permissionVO;
        }
        List<SysMenuTreeVO> menuVOList = new ArrayList<>();
        for (SysMenu sysMenu : menuList) {
            Short menuType = sysMenu.getMenuType();
            if (Objects.nonNull(menuType) && (menuType == 0 || menuType == 1 || menuType == 4)) {
                menuVOList.add(this.convertToTreeVO(sysMenu));
            }
            String requestUrl = sysMenu.getRequestUrl();
            if (CharSequenceUtil.isNotBlank(requestUrl)) {
                permissionVO.getPermUrls().add(requestUrl);
            }
            String menuCode = sysMenu.getMenuCode();
            if (CharSequenceUtil.isNotBlank(menuCode)) {
                permissionVO.getMenuCodes().add(menuCode);
            }
        }
        if (CollUtil.isNotEmpty(permissionVO.getMenuCodes())) {
            permissionVO.setMenuCodes(permissionVO.getMenuCodes().stream().distinct().toList());
        }
        List<SysMenuTreeVO> menuTreeList = buildMenuTree(menuVOList);
        permissionVO.setMenuTreeList(menuTreeList);
        return permissionVO;
    }

    private SysMenuTreeVO convertToTreeVO(SysMenu menu) {
        SysMenuTreeVO vo = new SysMenuTreeVO();
        vo.setMenuId(menu.getMenuId());
        vo.setParentId(menu.getParentId());
        vo.setMenuName(menu.getMenuName());
        vo.setMenuCode(menu.getMenuCode());
        vo.setRequestUrl(menu.getRequestUrl());
        vo.setMenuType(menu.getMenuType());
        vo.setMenuDescription(menu.getMenuDescription());
        vo.setMenuIcon(menu.getMenuIcon());
        vo.setSortOrder(menu.getSortOrder());
        return vo;
    }

    private List<SysMenuTreeVO> buildMenuTree(List<SysMenuTreeVO> menuList) {
        List<SysMenuTreeVO> tree = new ArrayList<>();
        for (SysMenuTreeVO node : menuList) {
            if (Objects.isNull(node.getParentId())) {
                tree.add(node);
            }
            for (SysMenuTreeVO it : menuList) {
                if (node.getMenuId().equals(it.getParentId())) {
                    node.addChildren(it);
                }
            }
        }
        return tree;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "127.0.0.1";
        }
        jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
