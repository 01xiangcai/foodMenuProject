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

import java.util.List;
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

    @Operation(summary = "添加菜品", description = "添加菜品及其口味信息")
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        ensureImage(dishDto);
        handleImageFields(dishDto);
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
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }

    @Operation(summary = "查询菜品详情", description = "根据ID查询菜品及其口味信息")
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        // Convert OSS object key to presigned URL if needed
        convertImageToPresignedUrl(dishDto);
        return Result.success(dishDto);
    }

    @Operation(summary = "更新菜品", description = "更新菜品及其口味信息")
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        ensureImage(dishDto);
        handleImageFields(dishDto);
        dishService.updateWithFlavor(dishDto);
        return Result.success("Dish updated successfully");
    }

    @Operation(summary = "删除菜品", description = "根据ID删除菜品(逻辑删除)")
    @DeleteMapping
    public Result<String> delete(Long id) {
        dishService.removeById(id);
        return Result.success("Dish deleted successfully");
    }

    @Operation(summary = "查询分类下的菜品", description = "查询指定分类下所有在售菜品")
    @GetMapping("/list")
    public Result<List<Dish>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
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
        // Convert OSS object keys to presigned URLs
        list.forEach(this::convertImageToPresignedUrl);
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
}
