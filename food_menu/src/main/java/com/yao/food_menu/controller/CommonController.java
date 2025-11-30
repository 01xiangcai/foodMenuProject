package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.UploadResult;
import com.yao.food_menu.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Tag(name = "公共模块", description = "文件上传等通用接口")
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
@Slf4j
public class CommonController {

    private final OssService ossService;
    
    // 允许的图片文件扩展名
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"));
    
    // 默认最大文件大小: 10MB
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Operation(summary = "上传图片到 OSS", description = "返回object key和签名URL，前端应保存object key到数据库，签名URL用于预览")
    @PostMapping("/upload")
    public Result<UploadResult> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        // 验证文件
        Result<String> validateResult = validateImageFile(file);
        if (validateResult != null) {
            return Result.error(validateResult.getMsg());
        }
        
        try {
            String objectKey = ossService.upload(file);
            // Generate presigned URL for immediate preview
            String presignedUrl = ossService.generatePresignedUrl(objectKey);
            UploadResult result = new UploadResult(objectKey, presignedUrl);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            log.error("文件验证失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证图片文件
     * @param file 上传的文件
     * @return 如果校验失败返回错误结果，否则返回null
     */
    private Result<String> validateImageFile(MultipartFile file) {
        // 检查文件大小
        if (file.getSize() > DEFAULT_MAX_FILE_SIZE) {
            long maxSizeMB = DEFAULT_MAX_FILE_SIZE / (1024 * 1024);
            return Result.error(String.format("文件大小超过限制，最大允许 %dMB", maxSizeMB));
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return Result.error("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            return Result.error(String.format("不支持的文件类型: %s，仅支持: %s",
                    extension, String.join(", ", ALLOWED_IMAGE_EXTENSIONS)));
        }

        return null; // 校验通过
    }
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名(小写,不包含点)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}

