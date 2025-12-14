package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品分类关联 Mapper 接口
 */
@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {
}
