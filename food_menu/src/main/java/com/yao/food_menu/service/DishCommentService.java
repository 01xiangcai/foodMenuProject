package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.DishCommentDto;
import com.yao.food_menu.entity.DishComment;

import java.util.List;

public interface DishCommentService extends IService<DishComment> {

    List<DishCommentDto> listByDishId(Long dishId);
}

