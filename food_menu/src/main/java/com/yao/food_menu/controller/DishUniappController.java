package com.yao.food_menu.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.config.FileStorageProperties;
import com.yao.food_menu.common.config.LocalStorageProperties;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Category;
import com.yao.food_menu.service.CategoryService;
import com.yao.food_menu.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小程序端菜品控制器
 */
@Tag(name = "小程序端-菜品管理", description = "小程序端菜品管理相关接口")
@RestController
@RequestMapping("/uniapp/dish")
@Slf4j
@RequiredArgsConstructor
public class DishUniappController {

    private final DishService dishService;
    private final CategoryService categoryService;
    private final FileStorageProperties fileStorageProperties;
    private final LocalStorageProperties localStorageProperties;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_IMAGE = "https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish";

    /**
     * 添加菜品（仅小程序管理员）
     */
    @Operation(summary = "添加菜品", description = "小程序管理员添加菜品及其口味信息")
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.INSERT, operationModule = "菜品", operationDesc = "小程序端添加菜品")
    @PostMapping
    public Result<String> addDish(@RequestBody DishDto dishDto) {
        // 权限校验：仅小程序管理员可添加菜品
        Integer currentUserRole = FamilyContext.getUserRole();
        if (currentUserRole == null || currentUserRole != 1) {
            log.warn("非管理员用户尝试添加菜品: userId={}, role={}",
                    FamilyContext.getUserId(), currentUserRole);
            return Result.error("无权限操作，仅管理员可添加菜品");
        }

        Long userId = FamilyContext.getUserId();
        Long familyId = FamilyContext.getFamilyId();

        log.info("小程序管理员添加菜品: userId={}, familyId={}, dishName={}",
                userId, familyId, dishDto.getName());

        // 数据隔离：设置家庭ID
        if (familyId != null) {
            dishDto.setFamilyId(familyId);
        } else {
            log.error("用户家庭ID为空，无法添加菜品: userId={}", userId);
            return Result.error("用户未加入家庭，无法添加菜品");
        }

        // 参数校验
        Result<String> validateResult = validateDishDto(dishDto);
        if (validateResult != null) {
            return validateResult;
        }

        // 处理图片字段
        ensureImage(dishDto);
        handleImageFields(dishDto);
        validateMainImageInList(dishDto);

        // 保存菜品
        try {
            dishService.saveWithFlavor(dishDto);
            log.info("小程序端添加菜品成功: dishId={}, dishName={}, familyId={}",
                    dishDto.getId(), dishDto.getName(), familyId);
            return Result.success("添加菜品成功");
        } catch (Exception e) {
            log.error("小程序端添加菜品失败: dishName={}, familyId={}",
                    dishDto.getName(), familyId, e);
            return Result.error("添加菜品失败: " + e.getMessage());
        }
    }

    /**
     * 更新菜品（仅小程序管理员）
     */
    @Operation(summary = "更新菜品", description = "小程序管理员更新菜品及其口味信息")
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.UPDATE, operationModule = "菜品", operationDesc = "小程序端更新菜品")
    @PutMapping
    public Result<String> updateDish(@RequestBody DishDto dishDto) {
        // 权限校验：仅小程序管理员可更新菜品
        Integer currentUserRole = FamilyContext.getUserRole();
        if (currentUserRole == null || currentUserRole != 1) {
            log.warn("非管理员用户尝试更新菜品: userId={}, role={}",
                    FamilyContext.getUserId(), currentUserRole);
            return Result.error("无权限操作，仅管理员可更新菜品");
        }

        Long userId = FamilyContext.getUserId();
        Long familyId = FamilyContext.getFamilyId();

        // 校验菜品ID
        if (dishDto.getId() == null) {
            return Result.error("菜品ID不能为空");
        }

        log.info("小程序管理员更新菜品: userId={}, familyId={}, dishId={}, dishName={}",
                userId, familyId, dishDto.getId(), dishDto.getName());

        // 数据隔离：验证菜品属于当前家庭
        com.yao.food_menu.entity.Dish existingDish = dishService.getById(dishDto.getId());
        if (existingDish == null) {
            log.warn("菜品不存在: dishId={}", dishDto.getId());
            return Result.error("菜品不存在");
        }

        if (existingDish.getFamilyId() != null && !existingDish.getFamilyId().equals(familyId)) {
            log.warn("尝试更新其他家庭的菜品: dishId={}, dishFamilyId={}, currentFamilyId={}",
                    dishDto.getId(), existingDish.getFamilyId(), familyId);
            return Result.error("无权限操作，只能更新本家庭的菜品");
        }

        // 确保家庭ID不被修改
        dishDto.setFamilyId(existingDish.getFamilyId());

        // 参数校验
        Result<String> validateResult = validateDishDto(dishDto);
        if (validateResult != null) {
            return validateResult;
        }

        // 处理图片字段
        ensureImage(dishDto);
        handleImageFields(dishDto);
        validateMainImageInList(dishDto);

        // 更新菜品
        try {
            dishService.updateWithFlavor(dishDto);
            log.info("小程序端更新菜品成功: dishId={}, dishName={}, familyId={}",
                    dishDto.getId(), dishDto.getName(), familyId);
            return Result.success("更新菜品成功");
        } catch (Exception e) {
            log.error("小程序端更新菜品失败: dishId={}, dishName={}, familyId={}",
                    dishDto.getId(), dishDto.getName(), familyId, e);
            return Result.error("更新菜品失败: " + e.getMessage());
        }
    }

    /**
     * 
     * 校验菜品DTO参数
     * 
     * @param dishDto 菜品DTO
     * @return 如果校验失败返回错误结果，否则返回null
     */
    private Result<String> validateDishDto(DishDto dishDto) {
        // 校验菜品名称
        if (dishDto.getName() == null || dishDto.getName().trim().isEmpty()) {
            return Result.error("菜品名称不能为空");
        }
        if (dishDto.getName().trim().length() > 50) {
            return Result.error("菜品名称长度不能超过50个字符");
        }

        // 校验分类ID - 支持多分类
        List<Long> categoryIdsToValidate = new ArrayList<>();
        if (dishDto.getCategoryIds() != null && !dishDto.getCategoryIds().isEmpty()) {
            categoryIdsToValidate = dishDto.getCategoryIds();
        } else if (dishDto.getCategoryId() != null) {
            // 兼容旧版本单分类
            categoryIdsToValidate = List.of(dishDto.getCategoryId());
        }

        if (categoryIdsToValidate.isEmpty()) {
            return Result.error("所属分类不能为空");
        }

        // 验证所有分类是否存在且属于当前家庭
        Long currentFamilyId = FamilyContext.getFamilyId();
        for (Long categoryId : categoryIdsToValidate) {
            Category category = categoryService.getById(categoryId);
            if (category == null) {
                return Result.error("所选分类不存在");
            }

            // 数据隔离:验证分类属于当前家庭
            if (category.getFamilyId() != null && !category.getFamilyId().equals(currentFamilyId)) {
                log.warn("尝试使用其他家庭的分类: categoryId={}, categoryFamilyId={}, currentFamilyId={}",
                        category.getId(), category.getFamilyId(), currentFamilyId);
                return Result.error("所选分类不存在");
            }
        }

        // 校验价格
        if (dishDto.getPrice() == null) {
            return Result.error("价格不能为空");
        }
        if (dishDto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return Result.error("价格不能小于0");
        }
        if (dishDto.getPrice().compareTo(new java.math.BigDecimal("99999.99")) > 0) {
            return Result.error("价格不能超过99999.99");
        }

        // 校验图片（本地存储模式）
        if (fileStorageProperties.isLocal()) {
            // 校验主图
            if (dishDto.getLocalImage() == null || dishDto.getLocalImage().trim().isEmpty()) {
                // 如果没有主图，检查图片列表
                if (dishDto.getLocalImages() == null || dishDto.getLocalImages().trim().isEmpty()) {
                    return Result.error("请至少上传一张图片");
                }
                // 解析图片列表
                try {
                    List<String> imageList = objectMapper.readValue(dishDto.getLocalImages(),
                            new TypeReference<List<String>>() {
                            });
                    List<String> validImages = imageList.stream()
                            .filter(img -> img != null && !img.trim().isEmpty())
                            .collect(Collectors.toList());
                    if (validImages.isEmpty()) {
                        return Result.error("请至少上传一张图片");
                    }
                } catch (Exception e) {
                    log.warn("解析图片列表失败: {}", dishDto.getLocalImages(), e);
                    return Result.error("图片列表格式错误");
                }
            }
        } else {
            // OSS存储模式：校验image字段
            if (dishDto.getImage() == null || dishDto.getImage().trim().isEmpty()) {
                return Result.error("请至少上传一张图片");
            }
        }

        // 校验描述长度（可选字段）
        if (dishDto.getDescription() != null && dishDto.getDescription().length() > 500) {
            return Result.error("菜品描述长度不能超过500个字符");
        }

        // 校验卡路里格式（可选字段）
        if (dishDto.getCalories() != null && dishDto.getCalories().trim().length() > 50) {
            return Result.error("卡路里信息长度不能超过50个字符");
        }

        // 校验标签长度（可选字段）
        if (dishDto.getTags() != null && dishDto.getTags().length() > 200) {
            return Result.error("标签长度不能超过200个字符");
        }

        return null; // 校验通过
    }

    /**
     * 确保图片字段正确设置
     */
    private void ensureImage(DishDto dishDto) {
        // 本地存储模式下，如果有localImage，不需要设置image字段
        // OSS存储模式下，如果没有image，设置默认图片
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式下，只使用localImage，立即清空image字段，避免后续处理时被错误使用
            if (StringUtils.hasText(dishDto.getLocalImage())) {
                dishDto.setImage(null);
            }
            return;
        } else {
            // OSS存储模式下，如果没有image，设置默认图片
            if (!StringUtils.hasText(dishDto.getImage())) {
                dishDto.setImage(DEFAULT_IMAGE);
            }
        }
    }

    /**
     * 处理图片字段，根据存储方式保存到对应字段
     */
    private void handleImageFields(DishDto dishDto) {
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式：只使用localImage字段
            // 如果localImage字段已经有值（前端直接传递的），优先使用localImage，不要被image字段覆盖
            if (StringUtils.hasText(dishDto.getLocalImage())) {
                // localImage字段已经有值，只清空image字段即可
                dishDto.setImage(null);
            } else {
                // 如果localImage为空，才从image字段获取（向后兼容）
                String image = dishDto.getImage();
                if (StringUtils.hasText(image)) {
                    // 如果是完整URL，提取相对路径
                    if (image.startsWith("http://") || image.startsWith("https://")) {
                        String urlPrefix = localStorageProperties.getUrlPrefix();
                        if (!urlPrefix.endsWith("/")) {
                            urlPrefix += "/";
                        }
                        if (image.startsWith(urlPrefix)) {
                            // 提取相对路径
                            String relativePath = image.substring(urlPrefix.length());
                            dishDto.setLocalImage(relativePath);
                        }
                    } else {
                        // 如果不是完整URL，直接保存到localImage
                        dishDto.setLocalImage(image);
                    }
                    // 清空image字段，本地存储模式下不保存image
                    dishDto.setImage(null);
                }
            }
        } else {
            // OSS存储模式：只使用image字段（OSS object key）
            // 清空localImage字段，OSS存储模式下不使用localImage
            dishDto.setLocalImage(null);
        }
    }

    /**
     * 验证主图是否在图片列表中
     * 同时将图片列表中的完整 URL 转换为相对路径
     */
    private void validateMainImageInList(DishDto dishDto) {
        String localImage = dishDto.getLocalImage();
        String localImages = dishDto.getLocalImages();

        // 处理图片列表：将完整 URL 转换为相对路径
        if (fileStorageProperties.isLocal() && StringUtils.hasText(localImages)) {
            try {
                List<String> imageList = objectMapper.readValue(localImages, new TypeReference<List<String>>() {
                });
                List<String> processedList = new ArrayList<>();
                String urlPrefix = localStorageProperties.getUrlPrefix();
                if (!urlPrefix.endsWith("/")) {
                    urlPrefix += "/";
                }

                for (String imagePath : imageList) {
                    if (!StringUtils.hasText(imagePath)) {
                        continue;
                    }

                    // 如果是完整 URL，提取相对路径
                    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                        if (imagePath.startsWith(urlPrefix)) {
                            String relativePath = imagePath.substring(urlPrefix.length());
                            processedList.add(relativePath);
                        } else {
                            // 如果不是本地 URL，保留原值（可能是外部链接）
                            processedList.add(imagePath);
                        }
                    } else {
                        // 已经是相对路径，直接使用
                        processedList.add(imagePath);
                    }
                }

                // 去重并更新列表
                List<String> uniqueList = new ArrayList<>();
                for (String path : processedList) {
                    if (!uniqueList.contains(path)) {
                        uniqueList.add(path);
                    }
                }

                dishDto.setLocalImages(objectMapper.writeValueAsString(uniqueList));
            } catch (Exception e) {
                log.warn("处理 localImages 失败: {}", localImages, e);
            }
        }

        // 处理主图：将完整 URL 转换为相对路径
        if (fileStorageProperties.isLocal() && StringUtils.hasText(localImage)) {
            String urlPrefix = localStorageProperties.getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }

            if (localImage.startsWith("http://") || localImage.startsWith("https://")) {
                if (localImage.startsWith(urlPrefix)) {
                    String relativePath = localImage.substring(urlPrefix.length());
                    dishDto.setLocalImage(relativePath);
                }
            }
        }

        // 确保主图在图片列表中（使用转换后的值）
        String processedLocalImage = dishDto.getLocalImage();
        String processedLocalImages = dishDto.getLocalImages();

        if (!StringUtils.hasText(processedLocalImage)) {
            return; // 如果没有主图，跳过验证
        }

        if (!StringUtils.hasText(processedLocalImages)) {
            // 如果没有图片列表，将主图作为唯一图片
            try {
                List<String> imageList = new ArrayList<>();
                imageList.add(processedLocalImage);
                dishDto.setLocalImages(objectMapper.writeValueAsString(imageList));
            } catch (Exception e) {
                log.error("转换图片列表失败", e);
            }
        }
    }
}
