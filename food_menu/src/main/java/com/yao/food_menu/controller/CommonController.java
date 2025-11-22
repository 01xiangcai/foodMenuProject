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

@Tag(name = "公共模块", description = "文件上传等通用接口")
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
@Slf4j
public class CommonController {

    private final OssService ossService;

    @Operation(summary = "上传图片到 OSS", description = "返回object key和签名URL，前端应保存object key到数据库，签名URL用于预览")
    @PostMapping("/upload")
    public Result<UploadResult> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        String objectKey = ossService.upload(file);
        // Generate presigned URL for immediate preview
        String presignedUrl = ossService.generatePresignedUrl(objectKey);
        UploadResult result = new UploadResult(objectKey, presignedUrl);
        return Result.success(result);
    }
}

