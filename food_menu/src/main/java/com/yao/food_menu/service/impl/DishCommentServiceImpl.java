package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.DishCommentDto;
import com.yao.food_menu.entity.DishComment;
import com.yao.food_menu.mapper.DishCommentMapper;
import com.yao.food_menu.service.DishCommentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishCommentServiceImpl extends ServiceImpl<DishCommentMapper, DishComment> implements DishCommentService {

    @Override
    public List<DishCommentDto> listByDishId(Long dishId) {
        LambdaQueryWrapper<DishComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishComment::getDishId, dishId)
                .orderByAsc(DishComment::getCreateTime);
        List<DishComment> comments = this.list(queryWrapper);

        Map<Long, List<DishCommentDto>> childrenMap = comments.stream()
                .filter(comment -> comment.getParentId() != null)
                .map(this::convert)
                .collect(Collectors.groupingBy(comment -> comment.getParentId()));

        return comments.stream()
                .filter(comment -> comment.getParentId() == null)
                .map(comment -> {
                    DishCommentDto dto = convert(comment);
                    dto.setReplies(childrenMap.getOrDefault(comment.getId(), List.of()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private DishCommentDto convert(DishComment comment) {
        DishCommentDto dto = new DishCommentDto();
        dto.setId(comment.getId());
        dto.setDishId(comment.getDishId());
        dto.setParentId(comment.getParentId());
        dto.setUserId(comment.getUserId());
        dto.setAuthorName(comment.getAuthorName());
        dto.setContent(comment.getContent());
        dto.setCreateTime(comment.getCreateTime());
        return dto;
    }
}

