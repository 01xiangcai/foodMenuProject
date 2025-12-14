package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.service.WxUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 微信用户管理控制器(管理员端)
 */
@Tag(name = "微信用户管理(管理员)", description = "管理员对微信小程序用户的管理操作")
@RestController
@RequestMapping("/admin/wx-user")
@Slf4j
public class WxUserAdminController {

    @Autowired
    private WxUserService wxUserService;

    /**
     * 修改用户密码(管理员)
     */
    @Operation(summary = "修改用户密码", description = "管理员修改小程序用户密码,需要权限验证")
    @RequestMapping(value = "/update-password", method = { RequestMethod.POST, RequestMethod.PUT })
    public Result<String> updatePassword(@RequestBody com.yao.food_menu.dto.UpdatePasswordDto dto) {
        log.info("管理员修改小程序用户密码: userId={}", dto.getUserId());

        try {
            wxUserService.updatePassword(dto.getUserId(), dto.getNewPassword());
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
