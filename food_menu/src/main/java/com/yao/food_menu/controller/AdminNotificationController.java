package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.service.SystemNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端-系统公告控制器
 */
@Tag(name = "管理端-系统公告", description = "发送系统公告")
@RestController
@RequestMapping("/admin/notification")
@Slf4j
@RequiredArgsConstructor
public class AdminNotificationController {

    private final SystemNotificationService systemNotificationService;

    @Operation(summary = "发送系统公告", description = "发送系统公告，familyId为空表示全平台推送")
    @PostMapping("/broadcast")
    public Result<String> broadcast(@RequestBody Map<String, Object> params) {
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        Long familyId = params.get("familyId") != null ? Long.valueOf(params.get("familyId").toString()) : null;

        if (title == null || title.trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        log.info("发送系统公告: familyId={}, title={}", familyId, title);
        systemNotificationService.sendSystemAnnounce(familyId, title, content);
        return Result.success("发送成功");
    }
}
