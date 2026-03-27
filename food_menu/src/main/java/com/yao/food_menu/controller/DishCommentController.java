package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.DishCommentDto;
import com.yao.food_menu.entity.DishComment;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.service.DishCommentService;
import com.yao.food_menu.service.WxUserService;
import com.yao.food_menu.service.OssService;
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
    private com.yao.food_menu.common.config.FileStorageProperties fileStorageProperties;

    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localStorageProperties;

    @Autowired
    private DishCommentService dishCommentService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private OssService ossService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "查询菜品评论", description = "查询指定菜品的评论及回复")
    @GetMapping("/{dishId}")
    public Result<List<DishCommentDto>> list(@PathVariable Long dishId) {
        List<DishCommentDto> comments = dishCommentService.listByDishId(dishId);

        // 转换头像URL
        comments.forEach(comment -> {
            convertAvatarUrl(comment);
            // 转换回复的头像URL
            if (comment.getReplies() != null) {
                comment.getReplies().forEach(this::convertAvatarUrl);
            }
        });

        return Result.success(comments);
    }

    /**
     * 将头像objectKey转换为预签名URL
     */
    private void convertAvatarUrl(DishCommentDto comment) {
        if (comment == null) return;
        
        String urlPrefix = localStorageProperties.getUrlPrefix();
        if (fileStorageProperties.isLocal()) {
            comment.setAvatarUrl(com.yao.food_menu.common.util.ImageUtils.processImageUrl(comment.getAvatarUrl(), urlPrefix));
        } else if (StringUtils.hasText(comment.getAvatarUrl()) && !comment.getAvatarUrl().startsWith("http")) {
            try {
                comment.setAvatarUrl(ossService.generatePresignedUrl(comment.getAvatarUrl()));
            } catch (Exception e) {
                log.warn("转换头像URL失败: {}, 错误: {}", comment.getAvatarUrl(), e.getMessage());
            }
        }
    }

    @Operation(summary = "新增评论", description = "新增菜品评论或回复")
    @com.yao.food_menu.common.annotation.RateLimiter(qps = 5, timeout = 500, message = "评论发表过于频繁，请稍后再试", limitType = com.yao.food_menu.common.annotation.RateLimiter.LimitType.USER)
    @com.yao.food_menu.common.annotation.PreventDuplicateSubmit(interval = 2000, message = "评论已提交")
    @PostMapping
    public Result<String> create(@RequestBody DishComment comment,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token == null || token.isEmpty()) {
                return Result.error("请先登录");
            }

            Long wxUserId = jwtUtil.getUserId(token);
            comment.setWxUserId(wxUserId);

            // 优先从WxUser获取用户信息
            WxUser wxUser = wxUserService.getById(wxUserId);
            if (wxUser != null) {
                if (!StringUtils.hasText(comment.getAuthorName())) {
                    comment.setAuthorName(StringUtils.hasText(wxUser.getNickname()) ? wxUser.getNickname() : "家庭成员");
                }
                if (!StringUtils.hasText(comment.getAvatarUrl()) && StringUtils.hasText(wxUser.getAvatar())) {
                    comment.setAvatarUrl(wxUser.getAvatar());
                }
            } else {
                // 如果WxUser不存在，使用默认值
                if (!StringUtils.hasText(comment.getAuthorName())) {
                    comment.setAuthorName("家庭成员");
                }
            }

            // 如果是回复评论，需要设置被回复人的用户ID
            if (comment.getParentId() != null && comment.getReplyToUserId() == null) {
                // 根据 parentId 查找被回复的评论，获取其作者的用户ID
                DishComment parentComment = dishCommentService.getById(comment.getParentId());
                if (parentComment != null && parentComment.getWxUserId() != null) {
                    comment.setReplyToUserId(parentComment.getWxUserId());
                    // 如果前端没有传递 replyToName，从父评论获取
                    if (!StringUtils.hasText(comment.getReplyToName())) {
                        comment.setReplyToName(parentComment.getAuthorName());
                    }
                }
            }

            dishCommentService.save(comment);
            return Result.success("评论发表成功");
        } catch (Exception e) {
            log.error("Create comment failed", e);
            return Result.error("评论发表失败: " + e.getMessage());
        }
    }
}
