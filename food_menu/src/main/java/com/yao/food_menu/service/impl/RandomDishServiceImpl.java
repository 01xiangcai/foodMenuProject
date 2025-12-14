package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.dto.RandomFilterDto;
import com.yao.food_menu.entity.Category;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.DishStatistics;
import com.yao.food_menu.entity.DishTag;
import com.yao.food_menu.service.CategoryService;
import com.yao.food_menu.service.DishService;
import com.yao.food_menu.service.DishStatisticsService;
import com.yao.food_menu.service.DishTagService;
import com.yao.food_menu.service.RandomDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 随机点餐服务实现
 */
@Service
@Slf4j
public class RandomDishServiceImpl implements RandomDishService {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishStatisticsService dishStatisticsService;

    @Autowired
    private DishTagService dishTagService;

    @Autowired
    private com.yao.food_menu.common.config.FileStorageProperties fileStorageProperties;

    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localStorageProperties;

    @Autowired
    private com.yao.food_menu.service.OssService ossService;

    @Autowired
    private com.yao.food_menu.service.DishCategoryService dishCategoryService;

    private static final String DEFAULT_IMAGE = "https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish";

    @Override
    public DishDto getRandomDish() {
        log.info("执行快速随机推荐");
        return getRandomDishWithFilter(new RandomFilterDto());
    }

    @Override
    public DishDto getRandomDishWithFilter(RandomFilterDto filter) {
        log.info("执行条件随机推荐,筛选条件: {}", filter);

        // 构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询在售菜品
        queryWrapper.eq(Dish::getStatus, 1);

        // 价格筛选
        if (filter.getPriceMin() != null) {
            queryWrapper.ge(Dish::getPrice, filter.getPriceMin());
        }
        if (filter.getPriceMax() != null) {
            queryWrapper.le(Dish::getPrice, filter.getPriceMax());
        }

        // 分类筛选 - 使用dish_category中间表
        if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
            // 通过中间表查询属于这些分类的菜品ID
            Set<Long> dishIds = new java.util.HashSet<>();
            for (Long categoryId : filter.getCategoryIds()) {
                List<Long> ids = dishCategoryService.getDishIdsByCategoryId(categoryId);
                dishIds.addAll(ids);
            }
            if (!dishIds.isEmpty()) {
                queryWrapper.in(Dish::getId, dishIds);
            } else {
                // 如果没有找到任何菜品,返回空
                log.warn("指定的分类下没有菜品");
                return null;
            }
        }

        // 排除指定菜品(去重)
        if (filter.getExcludeIds() != null && !filter.getExcludeIds().isEmpty()) {
            queryWrapper.notIn(Dish::getId, filter.getExcludeIds());
        }

        // 查询菜品列表
        List<Dish> dishes = dishService.list(queryWrapper);

        // 标签筛选(需要在内存中过滤,因为tags是字符串字段)
        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            dishes = dishes.stream()
                    .filter(dish -> {
                        if (!StringUtils.hasText(dish.getTags())) {
                            return false;
                        }
                        String[] dishTags = dish.getTags().split("[,，]");
                        Set<String> dishTagSet = Arrays.stream(dishTags)
                                .map(String::trim)
                                .collect(Collectors.toSet());
                        // 只要包含任意一个筛选标签就符合条件
                        return filter.getTags().stream()
                                .anyMatch(dishTagSet::contains);
                    })
                    .collect(Collectors.toList());
        }

        if (dishes.isEmpty()) {
            log.warn("没有符合条件的菜品");
            return null;
        }

        // 执行加权随机选择
        Dish selectedDish = selectRandomDishWithWeight(dishes);

        // 转换为DTO并处理图片URL
        DishDto dishDto = convertToDishDto(selectedDish);

        log.info("随机推荐菜品: {} (ID: {})", dishDto.getName(), dishDto.getId());
        return dishDto;
    }

    @Override
    public Map<String, Object> getFilterOptions() {
        log.info("获取筛选选项");

        Map<String, Object> options = new HashMap<>();

        // 获取所有分类
        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(Category::getType, 1); // 菜品分类
        categoryWrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryService.list(categoryWrapper);
        options.put("categories", categories);

        // 获取所有标签
        List<DishTag> tags = dishTagService.list();
        options.put("tags", tags);

        // 获取价格区间
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getStatus, 1);
        dishWrapper.select(Dish::getPrice);
        List<Dish> dishes = dishService.list(dishWrapper);

        Map<String, BigDecimal> priceRange = new HashMap<>();
        if (!dishes.isEmpty()) {
            BigDecimal minPrice = dishes.stream()
                    .map(Dish::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = dishes.stream()
                    .map(Dish::getPrice)
                    .max(BigDecimal::compareTo)
                    .orElse(new BigDecimal("100"));
            priceRange.put("min", minPrice);
            priceRange.put("max", maxPrice);
        } else {
            priceRange.put("min", BigDecimal.ZERO);
            priceRange.put("max", new BigDecimal("100"));
        }
        options.put("priceRange", priceRange);

        return options;
    }

    /**
     * 加权随机选择菜品
     * 权重计算: 基础权重(1.0) + 销量权重(0.1 * log(销量 + 1))
     * 
     * @param dishes 候选菜品列表
     * @return 随机选中的菜品
     */
    private Dish selectRandomDishWithWeight(List<Dish> dishes) {
        if (dishes.size() == 1) {
            return dishes.get(0);
        }

        // 计算每个菜品的权重
        Map<Dish, Double> weights = new HashMap<>();
        for (Dish dish : dishes) {
            DishStatistics stats = dishStatisticsService.getByDishId(dish.getId());
            int orderCount = stats != null ? stats.getTotalOrderCount() : 0;

            // 权重 = 1.0 + 0.1 * log(销量 + 1)
            double weight = 1.0 + 0.1 * Math.log(orderCount + 1);
            weights.put(dish, weight);

            log.debug("菜品: {}, 销量: {}, 权重: {}", dish.getName(), orderCount, weight);
        }

        // 计算总权重
        double totalWeight = weights.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // 生成随机数
        double random = Math.random() * totalWeight;

        // 根据权重选择
        double cumulative = 0;
        for (Map.Entry<Dish, Double> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                log.debug("随机值: {}, 累积权重: {}, 选中菜品: {}",
                        random, cumulative, entry.getKey().getName());
                return entry.getKey();
            }
        }

        // 兜底返回第一个(理论上不会执行到这里)
        log.warn("加权随机选择失败,返回第一个菜品");
        return dishes.get(0);
    }

    /**
     * 将Dish实体转换为DishDto并处理图片URL
     */
    private DishDto convertToDishDto(Dish dish) {
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties((Object) dish, dishDto);

        // 设置分类名称(获取所有分类)
        List<Long> categoryIds = dishCategoryService.getCategoryIdsByDishId(dish.getId());
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<String> categoryNames = categoryIds.stream()
                    .map(categoryService::getById)
                    .filter(java.util.Objects::nonNull)
                    .map(Category::getName)
                    .collect(Collectors.toList());
            dishDto.setCategoryNames(categoryNames);
            // 兼容旧字段,设置第一个分类名称
            if (!categoryNames.isEmpty()) {
                dishDto.setCategoryName(categoryNames.get(0));
            }
        }

        // 转换图片URL
        convertImageToPresignedUrl(dishDto);

        return dishDto;
    }

    /**
     * 转换图片为完整URL
     */
    private void convertImageToPresignedUrl(DishDto dishDto) {
        if (dishDto == null) {
            return;
        }

        // 根据存储方式处理图片URL
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式:将localImage拼接URL后设置到image字段
            if (StringUtils.hasText(dishDto.getLocalImage())) {
                String localImage = dishDto.getLocalImage();
                // 如果localImage已经是完整URL,直接使用
                if (localImage.startsWith("http://") || localImage.startsWith("https://")) {
                    dishDto.setImage(localImage);
                } else {
                    // 如果是相对路径,需要拼接URL前缀
                    String urlPrefix = localStorageProperties.getUrlPrefix();
                    if (!urlPrefix.endsWith("/")) {
                        urlPrefix += "/";
                    }
                    // 移除localImage开头的斜杠
                    String localPath = localImage.startsWith("/")
                            ? localImage.substring(1)
                            : localImage;
                    String fullUrl = urlPrefix + localPath;
                    // 将完整URL设置到image字段,供前端使用
                    dishDto.setImage(fullUrl);
                    // 同时将完整URL设置到localImage字段
                    dishDto.setLocalImage(fullUrl);
                }
            }
        } else {
            // OSS存储模式:使用image字段(OSS object key),转换为预签名URL
            if (!StringUtils.hasText(dishDto.getImage())) {
                return;
            }
            String image = dishDto.getImage();
            // 如果不是完整URL,作为OSS object key转换为预签名URL
            if (!image.startsWith("http://") && !image.startsWith("https://")) {
                try {
                    String presignedUrl = ossService.generatePresignedUrl(image);
                    dishDto.setImage(presignedUrl);
                } catch (Exception e) {
                    log.warn("生成预签名URL失败: {}, 使用默认图片", image, e);
                    dishDto.setImage(DEFAULT_IMAGE);
                }
            }
        }
    }
}
