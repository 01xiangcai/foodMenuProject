package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 查询分类列表（包含菜品数量）
     * 
     * @param category 查询条件
     * @return 分类列表
     */
    List<Category> listWithDishCount(Category category);
}
