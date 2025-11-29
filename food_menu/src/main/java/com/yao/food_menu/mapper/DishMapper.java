package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 查询销量前N的菜品
     * @param limit 限制数量
     * @param familyId 家庭ID（可为null，超级管理员时传null）
     * @param isSuperAdmin 是否为超级管理员
     * @return 菜品列表
     */
    List<DishDto> selectTopSelling(@Param("limit") int limit, 
                                    @Param("familyId") Long familyId, 
                                    @Param("isSuperAdmin") boolean isSuperAdmin);
}
