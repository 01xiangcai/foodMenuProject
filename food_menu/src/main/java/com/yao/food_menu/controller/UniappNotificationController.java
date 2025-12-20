package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.SystemNotificationDTO;
import com.yao.food_menu.service.SystemNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序端-系统通知控制器
 */
@Tag(name = "小程序端-系统通知", description = "系统通知的查询和标记已读")
@RestController
@RequestMapping("/uniapp/notification")
@Slf4j
@RequiredArgsConstructor
public class UniappNotificationController {

    private final SystemNotificationService systemNotificationService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "获取通知列表", description = "分页获取当前用户的通知列表")
    @GetMapping("/list")
    public Result<Page<SystemNotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        Page<SystemNotificationDTO> page = systemNotificationService.getUserNotifications(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取未读数量", description = "获取当前用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        Integer count = systemNotificationService.getUnreadCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @Operation(summary = "标记单条已读", description = "标记指定通知为已读")
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        systemNotificationService.markAsRead(id, userId);
        return Result.success("操作成功");
    }

    @Operation(summary = "标记全部已读", description = "标记当前用户所有通知为已读")
    @PutMapping("/read-all")
    public Result<String> markAllAsRead(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        systemNotificationService.markAllAsRead(userId);
        return Result.success("操作成功");
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserId(token);
    }
}
