package com.diet.modules.auth.controller;

import com.diet.modules.auth.model.dto.LoginDTO;
import com.diet.modules.auth.model.vo.LoginVO;
import com.diet.modules.auth.model.vo.SysPermissionVO;
import com.diet.modules.auth.security.SecurityProperties;
import com.diet.modules.auth.service.AuthService;
import com.diet.modules.common.constant.CacheKeyConstant;
import com.diet.modules.common.entity.Result;
import com.diet.modules.common.util.RedisUtil;
import com.diet.modules.common.util.SecurityUtils;
import com.diet.modules.system.model.entity.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller（登录 / 身份证模拟登录 / 登出 / 权限菜单查询）
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "系统管理-认证模块")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityProperties securityProperties;

    @Operation(summary = "获取加密公钥")
    @GetMapping("/public-key")
    public Result<String> getPublicKey() {
        return Result.success(authService.getPublicKey());
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        LoginVO loginVO = authService.login(dto);
        return Result.success(loginVO);
    }

    @Operation(summary = "身份证免密登录")
    @PostMapping("/loginByIdCard")
    public Result<LoginVO> loginByIdCard(@RequestParam("idCardNum") String idCardNum) {
        LoginVO loginVO = authService.loginByIdCardNum(idCardNum);
        return Result.success(loginVO);
    }

    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String headerValue = request.getHeader(securityProperties.getTokenHeader());
        if (StringUtils.hasText(headerValue) && headerValue.startsWith(securityProperties.getTokenPrefix())) {
            Long userId = SecurityUtils.getCurrentUserId();
            String cacheKeyUser = CacheKeyConstant.USER_CACHE_PREFIX + userId;
            String cacheKeyPerm = CacheKeyConstant.USER_PERM_PREFIX + userId;
            RedisUtil.del(cacheKeyUser);
            RedisUtil.del(cacheKeyPerm);
            String token = headerValue.substring(securityProperties.getTokenPrefix().length());
            authService.logout(token);
        }
        return Result.success("退出登录成功");
    }

    @Operation(summary = "获取当前用户权限菜单")
    @GetMapping("/menu")
    public Result<SysPermissionVO> getPermission() {
        SysUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        SysPermissionVO permissionVO = authService.getPermissionAndMenuList(currentUser);
        return Result.success(permissionVO);
    }
}
