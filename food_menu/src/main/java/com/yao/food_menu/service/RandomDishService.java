package com.yao.food_menu.service;

import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.dto.RandomFilterDto;
import java.util.Map;

/**
 * 随机点餐服务接口
 */
public interface RandomDishService {

    /**
     * 快速随机推荐一道菜
     * 
     * @return 随机菜品
     */
    DishDto getRandomDish();

    /**
     * 根据筛选条件随机推荐一道菜
     * 
     * @param filter 筛选条件
     * @return 随机菜品
     */
    DishDto getRandomDishWithFilter(RandomFilterDto filter);

    /**
     * 获取筛选选项
     * 
     * @return 筛选选项(分类列表、标签列表、价格区间)
     */
    Map<String, Object> getFilterOptions();
}
