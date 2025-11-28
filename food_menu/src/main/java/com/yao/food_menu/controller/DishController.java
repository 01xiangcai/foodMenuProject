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
        log.info(dishDto.toString());
        ensureImage(dishDto);
        handleImageFields(dishDto);
        validateMainImageInList(dishDto);
        dishService.saveWithFlavor(dishDto);
        return Result.success("Dish added successfully");
    }

    @Operation(summary = "分页查询菜品", description = "分页查询菜品列表,支持按名称模糊查询")
    @GetMapping("/page")
    public Result<Page<DishDto>> page(
            int page,
            int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
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
        log.info(dishDto.toString());
        ensureImage(dishDto);
        handleImageFields(dishDto);
        validateMainImageInList(dishDto);
        dishService.updateWithFlavor(dishDto);
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
        if (!StringUtils.hasText(dishDto.getImage())) {
            dishDto.setImage(DEFAULT_IMAGE);
        }
    }

    /**
     * 处理图片字段，确保本地存储的图片路径同时保存到localImage字段
     */
    private void handleImageFields(DishDto dishDto) {
        String image = dishDto.getImage();
        if (!StringUtils.hasText(image)) {
            return;
        }

        // 如果使用本地存储，且image字段是相对路径（不是完整URL），则同时保存到localImage
        if (fileStorageProperties.isLocal() && !image.startsWith("http://") && !image.startsWith("https://")) {
            dishDto.setLocalImage(image);
            log.debug("设置本地图片路径: {}", image);
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

        // 1. 优先使用本地图片
        if (StringUtils.hasText(dishDto.getLocalImage())) {
            String urlPrefix = localStorageProperties.getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
            dishDto.setImage(urlPrefix + dishDto.getLocalImage());
            return;
        }

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

    /**
     * Convert OSS object key to presigned URL for Dish entity
     */
    private void convertDishImageToPresignedUrl(Dish dish) {
        if (dish == null) {
            return;
        }

        // 1. 优先使用本地图片
        if (StringUtils.hasText(dish.getLocalImage())) {
            String urlPrefix = localStorageProperties.getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
            dish.setImage(urlPrefix + dish.getLocalImage());
            return;
        }

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

    /**
     * 验证主图是否在图片列表中
     */
    private void validateMainImageInList(DishDto dishDto) {
        String localImage = dishDto.getLocalImage();
        String localImages = dishDto.getLocalImages();
        
        if (!StringUtils.hasText(localImage)) {
            return; // 如果没有主图，跳过验证
        }
        
        if (!StringUtils.hasText(localImages)) {
            // 如果没有图片列表，将主图作为唯一图片
            try {
                List<String> imageList = new ArrayList<>();
                imageList.add(localImage);
                dishDto.setLocalImages(objectMapper.writeValueAsString(imageList));
            } catch (Exception e) {
                log.error("转换图片列表失败", e);
            }
            return;
        }
        
        try {
            List<String> imageList = objectMapper.readValue(localImages, new TypeReference<List<String>>() {});
            
            // 如果主图不在列表中，将其添加到列表开头
            if (!imageList.contains(localImage)) {
                imageList.add(0, localImage);
                dishDto.setLocalImages(objectMapper.writeValueAsString(imageList));
            }
        } catch (Exception e) {
            log.warn("解析 localImages 失败: {}", localImages, e);
            // 如果解析失败，将主图作为唯一图片
            try {
                List<String> imageList = new ArrayList<>();
                imageList.add(localImage);
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
        // 优先使用本地图片路径
        if (fileStorageProperties.isLocal()) {
            // 使用本地存储 URL 前缀
            try {
                String urlPrefix = localStorageProperties.getUrlPrefix();
                if (!urlPrefix.endsWith("/")) {
                    urlPrefix += "/";
                }
                return urlPrefix + imagePath;
            } catch (Exception e) {
                log.warn("获取本地存储 URL 前缀失败", e);
                return imagePath;
            }
        } else {
            // 如果使用 OSS，尝试生成预签名 URL
            try {
                return ossService.generatePresignedUrl(imagePath);
            } catch (Exception e) {
                log.warn("生成预签名 URL 失败: {}", imagePath, e);
                return imagePath;
            }
        }
    }
}
