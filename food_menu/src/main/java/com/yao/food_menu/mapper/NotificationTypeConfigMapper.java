package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.NotificationTypeConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 通知类型配置Mapper接口
 */
@Mapper
public interface NotificationTypeConfigMapper extends BaseMapper<NotificationTypeConfig> {

    /**
     * 根据编码查询配置
     */
    @Select("SELECT * FROM notification_type_config WHERE code = #{code}")
    NotificationTypeConfig selectByCode(String code);
}
