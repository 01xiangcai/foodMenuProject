package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.DishCommentDto;
import com.yao.food_menu.entity.DishComment;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.service.DishCommentService;
import com.yao.food_menu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜品评论", description = "家庭评论/回复")
@RestController
@RequestMapping("/dish/comments")
@Slf4j
public class DishCommentController {

    @Autowired
    private DishCommentService dishCommentService;

    @Autowired
    private UserService userService;

    @Operation(summary = "查询菜品评论", description = "查询指定菜品的评论及回复")
    @GetMapping("/{dishId}")
    public Result<List<DishCommentDto>> list(@PathVariable Long dishId) {
        List<DishCommentDto> comments = dishCommentService.listByDishId(dishId);
        return Result.success(comments);
    }

    @Operation(summary = "新增评论", description = "新增菜品评论或回复")
    @PostMapping
    public Result<String> create(@RequestBody DishComment comment, @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = JwtUtil.getUserId(token);
            comment.setUserId(userId);

            if (!StringUtils.hasText(comment.getAuthorName())) {
                User user = userService.getById(userId);
                comment.setAuthorName(user != null && StringUtils.hasText(user.getName()) ? user.getName() : "家庭成员");
            }

            dishCommentService.save(comment);
            return Result.success("Comment added");
        } catch (Exception e) {
            log.error("Create comment failed", e);
            return Result.error("Failed to add comment");
        }
    }
}

