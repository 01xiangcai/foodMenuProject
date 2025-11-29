package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.FamilyDto;
import com.yao.food_menu.dto.FamilyQueryDto;
import com.yao.food_menu.entity.Family;
import com.yao.food_menu.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 家庭管理Controller（Admin端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/family")
@Tag(name = "家庭管理接口（Admin端）")
public class FamilyControllerForAdmin {

    @Autowired
    private FamilyService familyService;

    /**
     * 分页查询家庭列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询家庭列表")
    public Result<Page<Family>> page(FamilyQueryDto queryDto) {
        log.info("分页查询家庭列表: {}", queryDto);
        Page<Family> page = familyService.pageFamilies(queryDto);
        return Result.success(page);
    }

    /**
     * 创建家庭（仅超级管理员）
     */
    @PostMapping
    @Operation(summary = "创建家庭", description = "只有超级管理员可以创建家庭")
    public Result<Family> create(@RequestBody FamilyDto familyDto) {
        log.info("创建家庭: {}", familyDto);
        
        // 权限控制：只有超级管理员可以创建家庭
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        boolean isSuperAdmin = (currentUserRole != null && currentUserRole == 2);
        if (!isSuperAdmin) {
            return Result.error("只有超级管理员可以创建家庭");
        }
        
        Family family = familyService.createFamily(familyDto);
        return Result.success(family);
    }

    /**
     * 更新家庭信息
     */
    @PutMapping
    @Operation(summary = "更新家庭信息")
    public Result<String> update(@RequestBody FamilyDto familyDto) {
        log.info("更新家庭信息: {}", familyDto);
        familyService.updateFamily(familyDto);
        return Result.success("更新成功");
    }

    /**
     * 删除家庭
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除家庭")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除家庭: {}", id);
        familyService.deleteFamily(id);
        return Result.success("删除成功");
    }

    /**
     * 查询家庭详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询家庭详情")
    public Result<Family> getById(@PathVariable Long id) {
        log.info("查询家庭详情: {}", id);
        Family family = familyService.getFamilyById(id);
        return Result.success(family);
    }

    /**
     * 通过邀请码查询家庭
     */
    @GetMapping("/invite/{inviteCode}")
    @Operation(summary = "通过邀请码查询家庭")
    public Result<Family> getByInviteCode(@PathVariable String inviteCode) {
        log.info("通过邀请码查询家庭: {}", inviteCode);
        Family family = familyService.getByInviteCode(inviteCode);
        return Result.success(family);
    }

    /**
     * 获取所有家庭列表（不分页，用于下拉选择）
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有家庭列表")
    public Result<java.util.List<Family>> list() {
        log.info("获取所有家庭列表");
        java.util.List<Family> families = familyService.listAllFamilies();
        return Result.success(families);
    }
}
