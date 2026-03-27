package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.dto.RandomFilterDto;
import com.yao.food_menu.service.RandomDishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 随机点餐控制器(小程序端)
 */
@Tag(name = "随机点餐", description = "随机推荐菜品功能")
@RestController
@RequestMapping("/uniapp/random-dish")
@Slf4j
public class RandomDishController {

    @Autowired
    private RandomDishService randomDishService;

    @Autowired
    private com.yao.food_menu.common.config.FileStorageProperties fileStorageProperties;

    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localStorageProperties;

    @Autowired
    private com.yao.food_menu.service.OssService ossService;

    @Operation(summary = "快速随机推荐", description = "一键随机推荐一道菜品")
    @GetMapping("/quick")
    public Result<DishDto> getRandomDish() {
        log.info("快速随机推荐请求");

        try {
            DishDto dish = randomDishService.getRandomDish();

            if (dish == null) {
                return Result.error("暂无可推荐的菜品");
            }

            processDishImage(dish);
            return Result.success(dish);
        } catch (Exception e) {
            log.error("快速随机推荐失败", e);
            return Result.error("随机推荐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "条件筛选随机推荐", description = "根据筛选条件随机推荐菜品")
    @PostMapping("/filter")
    public Result<DishDto> getRandomDishWithFilter(@RequestBody RandomFilterDto filter) {
        log.info("条件筛选随机推荐请求,筛选条件: {}", filter);

        try {
            DishDto dish = randomDishService.getRandomDishWithFilter(filter);

            if (dish == null) {
                return Result.error("没有符合条件的菜品,请调整筛选条件");
            }

            processDishImage(dish);
            return Result.success(dish);
        } catch (Exception e) {
            log.error("条件筛选随机推荐失败", e);
            return Result.error("随机推荐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取筛选选项", description = "获取可用的筛选选项(分类、标签、价格区间)")
    @GetMapping("/filter-options")
    public Result<Map<String, Object>> getFilterOptions() {
        log.info("获取筛选选项请求");

        try {
            Map<String, Object> options = randomDishService.getFilterOptions();
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取筛选选项失败", e);
            return Result.error("获取筛选选项失败: " + e.getMessage());
        }
    }

    private void processDishImage(DishDto dish) {
        String urlPrefix = localStorageProperties.getUrlPrefix();
        if (fileStorageProperties.isLocal()) {
            dish.setImage(com.yao.food_menu.common.util.ImageUtils.processImageUrl(dish.getLocalImage(), urlPrefix));
        } else if (org.springframework.util.StringUtils.hasText(dish.getImage()) && !dish.getImage().startsWith("http")) {
            try {
                dish.setImage(ossService.generatePresignedUrl(dish.getImage()));
            } catch (Exception e) {
                log.warn("转换随机菜单图片URL失败: {}", dish.getImage());
            }
        }
    }
}
