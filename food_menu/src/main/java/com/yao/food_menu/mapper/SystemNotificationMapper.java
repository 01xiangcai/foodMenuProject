package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.SystemNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 系统通知Mapper接口
 */
@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM system_notification WHERE user_id = #{userId} AND is_read = 0")
    Integer countUnreadByUserId(Long userId);

    /**
     * 标记用户所有通知为已读
     */
    @Update("UPDATE system_notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsReadByUserId(Long userId);
}
