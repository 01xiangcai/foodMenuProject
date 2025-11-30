package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.DishCommentDto;
import com.yao.food_menu.entity.DishComment;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.DishCommentMapper;
import com.yao.food_menu.service.DishCommentService;
import com.yao.food_menu.service.WxUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DishCommentServiceImpl extends ServiceImpl<DishCommentMapper, DishComment> implements DishCommentService {

    @Autowired
    private WxUserService wxUserService;

    @Override
    public List<DishCommentDto> listByDishId(Long dishId) {
        LambdaQueryWrapper<DishComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishComment::getDishId, dishId)
                .orderByDesc(DishComment::getCreateTime); // 改为倒序，最新的在前
        List<DishComment> comments = this.list(queryWrapper);

        // 收集所有需要查询的用户ID（评论作者和被回复人）
        Set<Long> userIds = comments.stream()
                .filter(c -> c.getWxUserId() != null)
                .map(DishComment::getWxUserId)
                .collect(Collectors.toSet());
        comments.stream()
                .filter(c -> c.getReplyToUserId() != null)
                .forEach(c -> userIds.add(c.getReplyToUserId()));

        // 批量查询用户信息
        Map<Long, WxUser> userMap = userIds.isEmpty() ? Map.of() : 
            wxUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(WxUser::getId, user -> user));

        Map<Long, List<DishCommentDto>> childrenMap = comments.stream()
                .filter(comment -> comment.getParentId() != null)
                .map(comment -> convert(comment, userMap))
                .collect(Collectors.groupingBy(comment -> comment.getParentId()));

        return comments.stream()
                .filter(comment -> comment.getParentId() == null)
                .map(comment -> {
                    DishCommentDto dto = convert(comment, userMap);
                    dto.setReplies(childrenMap.getOrDefault(comment.getId(), List.of()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换评论实体为DTO，并实时获取用户信息
     */
    private DishCommentDto convert(DishComment comment, Map<Long, WxUser> userMap) {
        DishCommentDto dto = new DishCommentDto();
        dto.setId(comment.getId());
        dto.setDishId(comment.getDishId());
        dto.setParentId(comment.getParentId());
        dto.setWxUserId(comment.getWxUserId());
        
        // 实时获取评论作者的最新信息
        WxUser author = comment.getWxUserId() != null ? userMap.get(comment.getWxUserId()) : null;
        if (author != null) {
            // 优先使用实时数据
            dto.setAuthorName(StringUtils.hasText(author.getNickname()) ? author.getNickname() : "家庭成员");
            dto.setAvatarUrl(StringUtils.hasText(author.getAvatar()) ? author.getAvatar() : 
                           (StringUtils.hasText(author.getLocalAvatar()) ? author.getLocalAvatar() : null));
        } else {
            // 如果用户不存在，使用历史数据
            dto.setAuthorName(StringUtils.hasText(comment.getAuthorName()) ? comment.getAuthorName() : "家庭成员");
            dto.setAvatarUrl(comment.getAvatarUrl());
        }
        
        // 实时获取被回复人的最新信息
        if (comment.getReplyToUserId() != null) {
            WxUser replyToUser = userMap.get(comment.getReplyToUserId());
            if (replyToUser != null) {
                dto.setReplyToName(StringUtils.hasText(replyToUser.getNickname()) ? replyToUser.getNickname() : "家庭成员");
            } else {
                // 如果用户不存在，使用历史数据
                dto.setReplyToName(comment.getReplyToName());
            }
        } else {
            // 兼容旧数据（没有 reply_to_user_id 的情况）
            dto.setReplyToName(comment.getReplyToName());
        }
        
        dto.setContent(comment.getContent());
        dto.setCreateTime(comment.getCreateTime());
        return dto;
    }
}
