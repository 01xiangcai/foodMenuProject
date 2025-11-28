package com.yao.food_menu.dto;

import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.DishFlavor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;

    private Integer sales;

    // 点单次数统计
    private Integer orderCount;

    // 图片数组（用于前端显示，不存储到数据库）
    private List<String> localImagesArray;
}
