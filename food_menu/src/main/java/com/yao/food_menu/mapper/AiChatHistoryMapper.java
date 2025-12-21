package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.AiChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI对话历史Mapper
 */
@Mapper
public interface AiChatHistoryMapper extends BaseMapper<AiChatHistory> {

    /**
     * 获取用户最近的对话历史
     * 
     * @param wxUserId 微信用户ID
     * @param limit    限制数量
     * @return 对话历史列表
     */
    List<AiChatHistory> getRecentHistory(@Param("wxUserId") Long wxUserId, @Param("limit") Integer limit);

    /**
     * 清除用户的对话历史
     * 
     * @param wxUserId 微信用户ID
     * @return 删除数量
     */
    int clearHistory(@Param("wxUserId") Long wxUserId);
}
