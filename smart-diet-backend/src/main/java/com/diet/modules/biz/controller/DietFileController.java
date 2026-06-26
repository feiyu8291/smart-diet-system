package com.diet.modules.biz.controller;

import com.diet.modules.biz.service.DietFileStorageService;
import com.diet.modules.common.entity.Result;
import com.diet.modules.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 业务侧文件上传 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DietFileController {

    private final DietFileStorageService fileStorageService;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam("groupId") Long groupId,
                                     @RequestParam("dishId") Long dishId,
                                     @RequestParam(value = "creator", defaultValue = "admin") String creator) {
        try {
            String url = fileStorageService.uploadDishImage(file, groupId, dishId, creator);
            return Result.success(url);
        } catch (Exception e) {
            throw BusinessException.withMessage(e.getMessage());
        }
    }
}
