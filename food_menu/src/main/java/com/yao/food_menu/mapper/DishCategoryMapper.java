package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.DishCategory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜品分类关联 Mapper 接口
 */
@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {

    /**
     * 物理删除指定菜品的所有分类关联（绕过逻辑删除）
     * 
     * @param dishId 菜品ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM dish_category WHERE dish_id = #{dishId}")
    int physicalDeleteByDishId(@Param("dishId") Long dishId);

    /**
     * 物理批量删除指定菜品的所有分类关联（绕过逻辑删除）
     * 
     * @param dishIds 菜品ID列表
     * @return 删除的行数
     */
    @Delete("<script>DELETE FROM dish_category WHERE dish_id IN " +
            "<foreach collection='dishIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int physicalDeleteBatchByDishIds(@Param("dishIds") List<Long> dishIds);
}
