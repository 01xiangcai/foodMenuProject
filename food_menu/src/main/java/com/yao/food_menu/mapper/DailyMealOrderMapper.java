package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.DailyMealOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 大订单Mapper
 */
@Mapper
public interface DailyMealOrderMapper extends BaseMapper<DailyMealOrder> {

    /**
     * 查询家庭某天的所有大订单
     */
    List<DailyMealOrder> selectByFamilyAndDate(@Param("familyId") Long familyId,
            @Param("orderDate") LocalDate orderDate);
}
