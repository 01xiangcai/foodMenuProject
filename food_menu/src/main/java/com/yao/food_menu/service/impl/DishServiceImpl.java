package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.DishFlavor;
import com.yao.food_menu.mapper.DishMapper;
import com.yao.food_menu.service.DishFlavorService;
import com.yao.food_menu.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private com.yao.food_menu.service.DishCategoryService dishCategoryService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.yao.food_menu.service.CategoryService categoryService;

    /**
     * 保存菜品及口味
     * 
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 自动设置familyId
        if (dishDto.getFamilyId() == null) {
            Long familyId = FamilyContext.getFamilyId();
            if (familyId != null) {
                dishDto.setFamilyId(familyId);
            }
        }

        // 保存菜品基本信息
        this.save(dishDto);

        Long dishId = dishDto.getId();

        // 保存菜品分类关联
        if (dishDto.getCategoryIds() != null && !dishDto.getCategoryIds().isEmpty()) {
            dishCategoryService.saveDishCategories(dishId, dishDto.getCategoryIds());
        } else if (dishDto.getCategoryId() != null) {
            // 兼容旧版本:如果没有categoryIds但有categoryId,则使用categoryId
            dishCategoryService.saveDishCategories(dishId, List.of(dishDto.getCategoryId()));
        }

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 保存菜品口味
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据ID获取菜品及口味
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 查询菜品分类关联
        List<Long> categoryIds = dishCategoryService.getCategoryIdsByDishId(id);
        dishDto.setCategoryIds(categoryIds);

        // 查询分类名称
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<String> categoryNames = categoryIds.stream()
                    .map(categoryId -> {
                        com.yao.food_menu.entity.Category category = categoryService.getById(categoryId);
                        return category != null ? category.getName() : null;
                    })
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            dishDto.setCategoryNames(categoryNames);
        }

        // 查询菜品口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        log.info("开始更新菜品 - ID: {}, 主图(localImage): {}", dishDto.getId(), dishDto.getLocalImage());

        // 先查询数据库中现有的菜品信息
        Dish existingDish = this.getById(dishDto.getId());
        if (existingDish == null) {
            throw new RuntimeException("菜品不存在");
        }

        log.info("更新前数据库中的主图: {}", existingDish.getLocalImage());

        // 将 DishDto 的属性复制到 Dish 实体（确保所有字段都被复制）
        BeanUtils.copyProperties(dishDto, existingDish);

        // 明确设置主图字段，确保被更新
        existingDish.setLocalImage(dishDto.getLocalImage());

        log.info("准备更新 - 主图(localImage): {}, 图片列表(localImages): {}",
                existingDish.getLocalImage(), existingDish.getLocalImages());

        // 更新菜品基本信息（使用实体对象，确保所有字段都被更新）
        boolean updateResult = this.updateById(existingDish);
        log.info("updateById 执行结果: {}", updateResult);

        // 使用 UpdateWrapper 明确更新 localImage 和 localImages 字段，确保一定会更新
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Dish::getId, dishDto.getId())
                .set(Dish::getLocalImage, dishDto.getLocalImage())
                .set(Dish::getLocalImages, dishDto.getLocalImages());
        this.update(updateWrapper);

        log.info("菜品基本信息更新完成 - ID: {}, 主图(localImage): {}", dishDto.getId(), dishDto.getLocalImage());

        // 更新菜品分类关联
        if (dishDto.getCategoryIds() != null && !dishDto.getCategoryIds().isEmpty()) {
            dishCategoryService.updateDishCategories(dishDto.getId(), dishDto.getCategoryIds());
        } else if (dishDto.getCategoryId() != null) {
            // 兼容旧版本:如果没有categoryIds但有categoryId,则使用categoryId
            dishCategoryService.updateDishCategories(dishDto.getId(), List.of(dishDto.getCategoryId()));
        }

        // 清除当前口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        // 添加新口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors = flavors.stream().map((item) -> {
                item.setId(null); // 清除id，让数据库自动生成新的id
                item.setDishId(dishDto.getId());
                return item;
            }).collect(Collectors.toList());

            dishFlavorService.saveBatch(flavors);
        }

        // 验证更新后的数据
        Dish updatedDish = this.getById(dishDto.getId());
        log.info("更新后数据库中的主图: {}", updatedDish != null ? updatedDish.getLocalImage() : "null");
        log.info("菜品更新完成 - ID: {}, 主图(localImage): {}", dishDto.getId(), dishDto.getLocalImage());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishDto> getTopSellingDishes(int limit) {
        // 获取当前用户的家庭ID和角色
        Long familyId = FamilyContext.getFamilyId();
        boolean isSuperAdmin = FamilyContext.isSuperAdmin();

        // 如果是超级管理员，familyId传null，可以查看所有家庭的菜品
        // 如果不是超级管理员，传具体的familyId，只能查看本家庭的菜品
        return baseMapper.selectTopSelling(limit, isSuperAdmin ? null : familyId, isSuperAdmin);
    }
}
