package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.entity.Family;
import com.yao.food_menu.service.FamilyService;
import com.yao.food_menu.service.WxUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.yao.food_menu.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 家庭管理Controller（Uniapp端）
 */
@Slf4j
@RestController
@RequestMapping("/uniapp/family")
@Tag(name = "家庭管理接口（Uniapp端）")
public class FamilyControllerForUniapp {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 通过邀请码加入家庭
     */
    @PostMapping("/join/{inviteCode}")
    @Operation(summary = "通过邀请码加入家庭")
    public Result<String> joinFamily(@PathVariable String inviteCode) {
        log.info("用户加入家庭，邀请码: {}", inviteCode);

        // 查询家庭
        Family family = familyService.getByInviteCode(inviteCode);
        if (family == null) {
            return Result.error("邀请码无效");
        }

        if (family.getStatus() == 0) {
            return Result.error("该家庭已被禁用");
        }

        // 获取当前用户ID
        Long userId = FamilyContext.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 更新用户的家庭ID
        wxUserService.updateUserFamily(userId, family.getId());

        // 生成新的Token，包含更新后的familyId
        // 由于这里没有username，暂时使用占位符，因为拦截器主要使用userId和familyId
        String token = jwtUtil.generateToken(userId, "WechatUser", family.getId(), 0);

        return Result.success(token);
    }

    /**
     * 查询当前用户的家庭信息
     */
    @GetMapping("/current")
    @Operation(summary = "查询当前用户的家庭信息")
    public Result<Family> getCurrentFamily() {
        log.info("查询当前用户的家庭信息");

        Long familyId = FamilyContext.getFamilyId();
        if (familyId == null) {
            return Result.error("用户未绑定家庭");
        }

        Family family = familyService.getById(familyId);
        return Result.success(family);
    }
}
