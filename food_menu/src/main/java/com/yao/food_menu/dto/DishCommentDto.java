package com.yao.food_menu.dto;

import com.yao.food_menu.entity.DishComment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DishCommentDto extends DishComment {

    private List<DishCommentDto> replies = new ArrayList<>();
}

