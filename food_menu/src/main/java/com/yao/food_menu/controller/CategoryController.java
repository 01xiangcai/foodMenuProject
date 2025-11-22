package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.entity.Category;
import com.yao.food_menu.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "菜品分类的增删改查")
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "添加分类", description = "添加菜品分类")
    @PostMapping
    public Result<String> save(@RequestBody Category category) {
        log.info("category:{}", category);
        categoryService.save(category);
        return Result.success("Category added successfully");
    }

    @Operation(summary = "分页查询分类", description = "分页查询分类列表")
    @GetMapping("/page")
    public Result<Page<Category>> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);
        return Result.success(pageInfo);
    }

    @Operation(summary = "删除分类", description = "根据ID删除分类")
    @DeleteMapping
    public Result<String> delete(Long id) {
        log.info("Delete category, id: {}", id);
        categoryService.removeById(id);
        return Result.success("Category deleted successfully");
    }

    @Operation(summary = "更新分类", description = "更新分类信息")
    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        log.info("Update category: {}", category);
        categoryService.updateById(category);
        return Result.success("Category updated successfully");
    }

    @Operation(summary = "查询分类列表", description = "查询所有分类或按类型筛选")
    @GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return Result.success(list);
    }
}
