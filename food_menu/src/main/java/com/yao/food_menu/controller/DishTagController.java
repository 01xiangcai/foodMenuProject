package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.entity.DishTag;
import com.yao.food_menu.service.DishTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜品标签管理Controller
 */
@Tag(name = "菜品标签管理", description = "菜品标签的增删改查")
@RestController
@RequestMapping("/dish-tag")
@Slf4j
public class DishTagController {

    @Autowired
    private DishTagService dishTagService;

    /**
     * 分页查询标签列表(管理端)
     */
    @Operation(summary = "分页查询标签列表", description = "管理端分页查询标签")
    @GetMapping("/page")
    public Result<Page<DishTag>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long familyId) {

        log.info("分页查询标签: page={}, pageSize={}, name={}, type={}, status={}, familyId={}",
                page, pageSize, name, type, status, familyId);

        Page<DishTag> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<DishTag> queryWrapper = new LambdaQueryWrapper<>();

        // 条件查询
        queryWrapper.like(name != null && !name.isEmpty(), DishTag::getName, name)
                .eq(type != null, DishTag::getType, type)
                .eq(status != null, DishTag::getStatus, status)
                .eq(familyId != null, DishTag::getFamilyId, familyId)
                .orderByAsc(DishTag::getType)
                .orderByAsc(DishTag::getSort);

        dishTagService.page(pageInfo, queryWrapper);
        return Result.success(pageInfo);
    }

    /**
     * 获取所有启用的标签(小程序端)
     */
    @Operation(summary = "获取启用标签列表", description = "获取所有启用状态的标签")
    @GetMapping("/list")
    public Result<List<DishTag>> list(@RequestParam(required = false) Integer type) {
        log.info("获取标签列表: type={}", type);
        List<DishTag> tags;
        if (type != null) {
            tags = dishTagService.getTagsByType(type);
        } else {
            tags = dishTagService.getEnabledTags();
        }
        return Result.success(tags);
    }

    /**
     * 获取标签图标映射(小程序端)
     */
    @Operation(summary = "获取标签图标映射", description = "获取标签名称到图标的映射")
    @GetMapping("/icon-map")
    public Result<Map<String, String>> getIconMap() {
        log.info("获取标签图标映射");
        Map<String, String> iconMap = dishTagService.getTagIconMap();
        return Result.success(iconMap);
    }

    /**
     * 根据ID获取标签详情
     */
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详情")
    @GetMapping("/{id}")
    public Result<DishTag> getById(@PathVariable Long id) {
        log.info("获取标签详情: id={}", id);
        DishTag tag = dishTagService.getById(id);
        if (tag == null) {
            return Result.error("标签不存在");
        }
        return Result.success(tag);
    }

    /**
     * 新增标签
     */
    @Operation(summary = "新增标签", description = "添加新的菜品标签")
    @PostMapping
    public Result<String> save(@RequestBody DishTag dishTag) {
        log.info("新增标签: {}", dishTag);

        // 检查同一家庭内标签名称是否已存在
        LambdaQueryWrapper<DishTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishTag::getName, dishTag.getName())
                .eq(dishTag.getFamilyId() != null, DishTag::getFamilyId, dishTag.getFamilyId());
        if (dishTagService.count(queryWrapper) > 0) {
            return Result.error("该家庭下标签名称已存在");
        }

        // 设置默认值
        if (dishTag.getStatus() == null) {
            dishTag.setStatus(1);
        }
        if (dishTag.getSort() == null) {
            dishTag.setSort(0);
        }
        if (dishTag.getType() == null) {
            dishTag.setType(5); // 默认为其他类型
        }

        dishTagService.save(dishTag);
        return Result.success("添加成功");
    }

    /**
     * 更新标签
     */
    @Operation(summary = "更新标签", description = "更新菜品标签信息")
    @PutMapping
    public Result<String> update(@RequestBody DishTag dishTag) {
        log.info("更新标签: {}", dishTag);

        if (dishTag.getId() == null) {
            return Result.error("标签ID不能为空");
        }

        // 检查同一家庭内标签名称是否与其他标签重复
        LambdaQueryWrapper<DishTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishTag::getName, dishTag.getName())
                .ne(DishTag::getId, dishTag.getId())
                .eq(dishTag.getFamilyId() != null, DishTag::getFamilyId, dishTag.getFamilyId());
        if (dishTagService.count(queryWrapper) > 0) {
            return Result.error("该家庭下标签名称已存在");
        }

        dishTagService.updateById(dishTag);
        return Result.success("更新成功");
    }

    /**
     * 删除标签
     */
    @Operation(summary = "删除标签", description = "删除菜品标签")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除标签: id={}", id);
        dishTagService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除标签
     */
    @Operation(summary = "批量删除标签", description = "批量删除菜品标签")
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除标签: ids={}", ids);
        dishTagService.removeByIds(ids);
        return Result.success("删除成功");
    }

    /**
     * 更新标签状态
     */
    @Operation(summary = "更新标签状态", description = "启用或禁用标签")
    @PutMapping("/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新标签状态: id={}, status={}", id, status);

        DishTag tag = dishTagService.getById(id);
        if (tag == null) {
            return Result.error("标签不存在");
        }

        tag.setStatus(status);
        dishTagService.updateById(tag);
        return Result.success("更新成功");
    }
}
