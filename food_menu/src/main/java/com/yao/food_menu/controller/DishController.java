package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Category;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.service.CategoryService;
import com.yao.food_menu.service.DishService;
import com.yao.food_menu.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Tag(name = "菜品管理", description = "菜品的增删改查、口味管理")
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    private static final String DEFAULT_IMAGE = "https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish";

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OssService ossService;

    @Autowired
    private com.yao.food_menu.common.config.FileStorageProperties fileStorageProperties;

    @Autowired
    private com.yao.food_menu.service.DishStatisticsService dishStatisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "添加菜品", description = "添加菜品及其口味信息")
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info("新增菜品请求: {}", dishDto);
        
        // 参数校验
        Result<String> validateResult = validateDishDto(dishDto);
        if (validateResult != null) {
            return validateResult;
        }
        
        ensureImage(dishDto);
        handleImageFields(dishDto);
        validateMainImageInList(dishDto);
        dishService.saveWithFlavor(dishDto);
        return Result.success("Dish added successfully");
    }
    
    /**
     * 校验菜品DTO参数
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
        
        // 校验分类ID
        if (dishDto.getCategoryId() == null) {
            return Result.error("所属分类不能为空");
        }
        // 验证分类是否存在
        Category category = categoryService.getById(dishDto.getCategoryId());
        if (category == null) {
            return Result.error("所选分类不存在");
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
                    List<String> imageList = objectMapper.readValue(dishDto.getLocalImages(), new TypeReference<List<String>>() {});
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
            return Result.error("家庭备注长度不能超过500个字符");
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

    @Operation(summary = "分页查询菜品", description = "分页查询菜品列表,支持按名称模糊查询")
    @GetMapping("/page")
    public Result<Page<DishDto>> page(
            int page,
            int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long familyId) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        // 超级管理员可以通过familyId参数筛选特定家庭的数据
        // 非超级管理员由拦截器自动过滤，这里不需要处理
        if (familyId != null && com.yao.food_menu.common.context.FamilyContext.isSuperAdmin()) {
            queryWrapper.eq(Dish::getFamilyId, familyId);
        }
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long dishCategoryId = item.getCategoryId();
            Category category = categoryService.getById(dishCategoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            // Convert OSS object key to presigned URL if needed
            convertImageToPresignedUrl(dishDto);
            // 将 localImages JSON 字符串转换为数组
            convertLocalImagesToArray(dishDto);
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }

    @Operation(summary = "查询菜品详情", description = "根据ID查询菜品及其口味信息")
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        // 先转换主图 URL
        convertImageToPresignedUrl(dishDto);
        // 然后将 localImages JSON 字符串转换为数组（此时主图 URL 已转换）
        convertLocalImagesToArray(dishDto);
        return Result.success(dishDto);
    }

    @Operation(summary = "更新菜品", description = "更新菜品及其口味信息")
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("更新菜品请求 - 菜品ID: {}, 名称: {}", dishDto.getId(), dishDto.getName());
        log.info("更新菜品 - 主图(localImage): {}, 图片列表(localImages): {}", dishDto.getLocalImage(), dishDto.getLocalImages());
        ensureImage(dishDto);
        handleImageFields(dishDto);
        log.info("处理图片字段后 - 主图(localImage): {}", dishDto.getLocalImage());
        validateMainImageInList(dishDto);
        log.info("验证主图后 - 主图(localImage): {}, 图片列表(localImages): {}", dishDto.getLocalImage(), dishDto.getLocalImages());
        dishService.updateWithFlavor(dishDto);
        log.info("菜品更新成功 - 菜品ID: {}", dishDto.getId());
        return Result.success("Dish updated successfully");
    }

    @Operation(summary = "删除菜品", description = "根据ID删除菜品(逻辑删除)")
    @DeleteMapping
    public Result<String> delete(Long id) {
        dishService.removeById(id);
        return Result.success("Dish deleted successfully");
    }

    @Operation(summary = "查询分类下的菜品", description = "查询指定分类下所有在售菜品，支持按名称模糊搜索")
    @GetMapping("/list")
    public Result<List<Dish>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        queryWrapper.like(StringUtils.hasText(name), Dish::getName, name);
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        // Convert OSS object keys to presigned URLs for all dishes
        list.forEach(this::convertDishImageToPresignedUrl);
        return Result.success(list);
    }

    @Operation(summary = "查询明星菜品", description = "查询销量最高的5道菜品")
    @GetMapping("/top")
    public Result<List<DishDto>> getTopDishes() {
        List<DishDto> list = dishService.getTopSellingDishes(5);

        // 批量查询菜品统计数据
        List<Long> dishIds = list.stream()
                .map(DishDto::getId)
                .collect(Collectors.toList());
        java.util.Map<Long, com.yao.food_menu.entity.DishStatistics> statisticsMap = dishStatisticsService
                .getBatchByDishIds(dishIds);

        // 填充点菜次数
        list.forEach(dishDto -> {
            com.yao.food_menu.entity.DishStatistics statistics = statisticsMap.get(dishDto.getId());
            if (statistics != null) {
                dishDto.setOrderCount(statistics.getTotalOrderCount());
            } else {
                dishDto.setOrderCount(0);
            }
            // Convert OSS object keys to presigned URLs
            convertImageToPresignedUrl(dishDto);
            // 将 localImages JSON 字符串转换为数组
            convertLocalImagesToArray(dishDto);
        });

        // 重新排序，确保按点单次数降序排列
        list.sort((a, b) -> b.getOrderCount() - a.getOrderCount());

        return Result.success(list);
    }

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

    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localStorageProperties;

    /**
     * Convert OSS object key to presigned URL if the image is an OSS object key
     * (not a full URL starting with http:// or https://)
     */
    private void convertImageToPresignedUrl(DishDto dishDto) {
        if (dishDto == null) {
            return;
        }

        // 根据存储方式处理图片URL
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式：将localImage拼接URL后设置到image字段（供前端使用）
            // 同时将拼接好的URL也设置到localImage字段（覆盖相对路径）
            if (StringUtils.hasText(dishDto.getLocalImage())) {
                String localImage = dishDto.getLocalImage();
                // 如果localImage已经是完整URL（以http://或https://开头），直接使用，不要拼接
                if (localImage.startsWith("http://") || localImage.startsWith("https://")) {
                    // 已经是完整URL，直接使用
                    dishDto.setImage(localImage);
                    // localImage字段保持原样（已经是完整URL）
                } else {
                    // 如果是相对路径，需要拼接URL前缀
                    String urlPrefix = localStorageProperties.getUrlPrefix();
                    if (!urlPrefix.endsWith("/")) {
                        urlPrefix += "/";
                    }
                    // 移除localImage开头的斜杠
                    String localPath = localImage.startsWith("/") 
                        ? localImage.substring(1) 
                        : localImage;
                    String fullUrl = urlPrefix + localPath;
                    // 将完整URL设置到image字段，供前端使用
                    dishDto.setImage(fullUrl);
                    // 同时将完整URL设置到localImage字段（前端优先使用localImage）
                    dishDto.setLocalImage(fullUrl);
                }
            }
            // 本地存储模式下，如果image字段存在但不是完整URL，忽略它（可能是旧数据）
        } else {
            // OSS存储模式：使用image字段（OSS object key），转换为预签名URL
            if (!StringUtils.hasText(dishDto.getImage())) {
                return;
            }
            String image = dishDto.getImage();
            // If image is not a full URL (doesn't start with http:// or https://),
            // treat it as OSS object key and convert to presigned URL
            if (!image.startsWith("http://") && !image.startsWith("https://")) {
                try {
                    String presignedUrl = ossService.generatePresignedUrl(image);
                    dishDto.setImage(presignedUrl);
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for object key: {}, using default image", image, e);
                    dishDto.setImage(DEFAULT_IMAGE);
                }
            }
        }
    }

    /**
     * Convert OSS object key to presigned URL for Dish entity
     * 用于小程序端的 /dish/list 接口
     */
    private void convertDishImageToPresignedUrl(Dish dish) {
        if (dish == null) {
            return;
        }

        // 根据存储方式处理图片URL
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式：将localImage拼接URL后设置到image字段（供前端使用）
            // 同时将拼接好的URL也设置到localImage字段（覆盖相对路径）
            if (StringUtils.hasText(dish.getLocalImage())) {
                String localImage = dish.getLocalImage();
                // 如果localImage已经是完整URL（以http://或https://开头），直接使用，不要拼接
                if (localImage.startsWith("http://") || localImage.startsWith("https://")) {
                    // 已经是完整URL，直接使用
                    dish.setImage(localImage);
                    // localImage字段保持原样（已经是完整URL）
                } else {
                    // 如果是相对路径，需要拼接URL前缀
                    String urlPrefix = localStorageProperties.getUrlPrefix();
                    if (!urlPrefix.endsWith("/")) {
                        urlPrefix += "/";
                    }
                    // 移除localImage开头的斜杠
                    String localPath = localImage.startsWith("/") 
                        ? localImage.substring(1) 
                        : localImage;
                    String fullUrl = urlPrefix + localPath;
                    // 将完整URL设置到image字段，供前端使用
                    dish.setImage(fullUrl);
                    // 同时将完整URL设置到localImage字段（前端优先使用localImage）
                    dish.setLocalImage(fullUrl);
                }
            }
            // 本地存储模式下，如果image字段存在但不是完整URL，忽略它（可能是旧数据）
        } else {
            // OSS存储模式：使用image字段（OSS object key），转换为预签名URL
            if (!StringUtils.hasText(dish.getImage())) {
                return;
            }
            String image = dish.getImage();
            // If image is not a full URL (doesn't start with http:// or https://),
            // treat it as OSS object key and convert to presigned URL
            if (!image.startsWith("http://") && !image.startsWith("https://")) {
                try {
                    String presignedUrl = ossService.generatePresignedUrl(image);
                    dish.setImage(presignedUrl);
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for object key: {}, using default image", image, e);
                    dish.setImage(DEFAULT_IMAGE);
                }
            }
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
                List<String> imageList = objectMapper.readValue(localImages, new TypeReference<List<String>>() {});
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
            return;
        }
        
        try {
            List<String> imageList = objectMapper.readValue(processedLocalImages, new TypeReference<List<String>>() {});
            
            // 检查主图是否在列表中（支持相对路径和完整URL的匹配）
            boolean mainImageInList = false;
            int mainImageIndex = -1;
            String urlPrefix = localStorageProperties.getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
            
            for (int i = 0; i < imageList.size(); i++) {
                String imagePath = imageList.get(i);
                if (imagePath == null) {
                    continue;
                }
                
                // 直接匹配
                if (imagePath.equals(processedLocalImage)) {
                    mainImageInList = true;
                    mainImageIndex = i;
                    break;
                }
                
                // 尝试匹配完整URL和相对路径
                if (fileStorageProperties.isLocal()) {
                    // 如果列表中的路径是完整URL，提取相对路径进行比较
                    if (imagePath.startsWith(urlPrefix)) {
                        String relativePath = imagePath.substring(urlPrefix.length());
                        if (relativePath.equals(processedLocalImage)) {
                            mainImageInList = true;
                            mainImageIndex = i;
                            // 将列表中的完整URL替换为相对路径
                            imageList.set(i, processedLocalImage);
                            break;
                        }
                    }
                    // 如果主图是完整URL，列表中是相对路径，提取相对路径进行比较
                    if (processedLocalImage.startsWith(urlPrefix)) {
                        String mainRelativePath = processedLocalImage.substring(urlPrefix.length());
                        if (mainRelativePath.equals(imagePath)) {
                            mainImageInList = true;
                            mainImageIndex = i;
                            break;
                        }
                    }
                }
            }
            
            // 如果主图不在列表中，将其添加到列表开头
            if (!mainImageInList) {
                imageList.add(0, processedLocalImage);
            } else if (mainImageIndex > 0) {
                // 如果主图在列表中但不是第一个，将其移动到第一个位置
                imageList.remove(mainImageIndex);
                imageList.add(0, processedLocalImage);
            }
            
            // 更新图片列表
            dishDto.setLocalImages(objectMapper.writeValueAsString(imageList));
            
            // 确保主图字段是相对路径
            if (processedLocalImage.startsWith("http://") || processedLocalImage.startsWith("https://")) {
                if (fileStorageProperties.isLocal() && processedLocalImage.startsWith(urlPrefix)) {
                    dishDto.setLocalImage(processedLocalImage.substring(urlPrefix.length()));
                }
            }
            
            log.info("验证主图完成 - 主图: {}, 图片列表: {}", dishDto.getLocalImage(), imageList);
        } catch (Exception e) {
            log.warn("解析 localImages 失败: {}", processedLocalImages, e);
            // 如果解析失败，将主图作为唯一图片
            try {
                List<String> imageList = new ArrayList<>();
                imageList.add(processedLocalImage);
                dishDto.setLocalImages(objectMapper.writeValueAsString(imageList));
            } catch (Exception ex) {
                log.error("转换图片列表失败", ex);
            }
        }
    }

    /**
     * 将 localImages JSON 字符串转换为数组（用于前端显示）
     * 同时将图片路径转换为完整 URL
     */
    private void convertLocalImagesToArray(DishDto dishDto) {
        String localImages = dishDto.getLocalImages();
        List<String> imagePathList = new ArrayList<>();
        
        // 解析 JSON 字符串为路径数组
        if (StringUtils.hasText(localImages)) {
            try {
                imagePathList = objectMapper.readValue(localImages, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("解析 localImages JSON 失败: {}", localImages, e);
                // 如果解析失败，尝试从主图生成
                if (StringUtils.hasText(dishDto.getLocalImage())) {
                    imagePathList.add(dishDto.getLocalImage());
                }
            }
        } else if (StringUtils.hasText(dishDto.getLocalImage())) {
            // 如果没有 localImages，从 localImage 生成数组
            imagePathList.add(dishDto.getLocalImage());
        }
        
        // 将图片路径转换为完整 URL
        List<String> imageUrlList = new ArrayList<>();
        for (String imagePath : imagePathList) {
            if (imagePath == null || imagePath.isEmpty()) {
                continue;
            }
            String imageUrl = convertImagePathToUrl(imagePath, dishDto);
            imageUrlList.add(imageUrl);
        }
        
        // 设置到 DTO 中供前端使用
        dishDto.setLocalImagesArray(imageUrlList);
    }
    
    /**
     * 将图片路径转换为完整 URL（与 convertImageToPresignedUrl 逻辑一致）
     */
    private String convertImagePathToUrl(String imagePath, DishDto dishDto) {
        if (imagePath == null || imagePath.isEmpty()) {
            return DEFAULT_IMAGE;
        }
        
        // 如果已经是完整 URL，直接返回（不要再次转换）
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        
        // 如果是主图，使用已经转换好的 dishDto.getImage()（在调用此方法前应该已经转换）
        if (StringUtils.hasText(dishDto.getLocalImage()) && imagePath.equals(dishDto.getLocalImage())) {
            // 直接使用已转换的主图 URL
            String mainImageUrl = dishDto.getImage();
            if (StringUtils.hasText(mainImageUrl) && (mainImageUrl.startsWith("http://") || mainImageUrl.startsWith("https://"))) {
                return mainImageUrl;
            }
            // 如果主图 URL 还没转换，继续下面的转换逻辑
        }
        
        // 其他图片，使用与主图相同的转换逻辑
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式：使用本地存储 URL 前缀
            try {
                String urlPrefix = localStorageProperties.getUrlPrefix();
                if (!urlPrefix.endsWith("/")) {
                    urlPrefix += "/";
                }
                // 移除imagePath开头的斜杠
                String key = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                return urlPrefix + key;
            } catch (Exception e) {
                log.warn("转换本地图片URL失败: {}", imagePath, e);
                return DEFAULT_IMAGE;
            }
        } else {
            // OSS存储模式：转换为预签名URL
            try {
                String presignedUrl = ossService.generatePresignedUrl(imagePath);
                return presignedUrl;
            } catch (Exception e) {
                log.warn("生成OSS预签名URL失败: {}", imagePath, e);
                return DEFAULT_IMAGE;
            }
        }
    }
}
