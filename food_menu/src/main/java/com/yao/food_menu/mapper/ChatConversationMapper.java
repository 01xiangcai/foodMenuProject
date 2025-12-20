package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.yao.food_menu.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

        /**
         * 查询用户的会话列表
         * 
         * @param wxUserId 用户ID
         * @return 会话列表
         */
        @InterceptorIgnore(tenantLine = "true")
        @Select("SELECT c.* FROM chat_conversation c " +
                        "INNER JOIN chat_conversation_member m ON c.id = m.conversation_id " +
                        "WHERE m.wx_user_id = #{wxUserId} AND m.deleted = 0 AND c.deleted = 0 " +
                        "ORDER BY c.last_message_time DESC")
        List<ChatConversation> selectByWxUserId(@Param("wxUserId") Long wxUserId);

        /**
         * 查找两个用户之间的私聊会话
         * 
         * @param userId1 用户1 ID
         * @param userId2 用户2 ID
         * @return 会话
         */
        @InterceptorIgnore(tenantLine = "true")
        @Select("SELECT c.* FROM chat_conversation c " +
                        "WHERE c.type = 1 AND c.deleted = 0 " +
                        "AND c.id IN (" +
                        "  SELECT m1.conversation_id FROM chat_conversation_member m1 " +
                        "  INNER JOIN chat_conversation_member m2 ON m1.conversation_id = m2.conversation_id " +
                        "  WHERE m1.wx_user_id = #{userId1} AND m2.wx_user_id = #{userId2} " +
                        ") LIMIT 1")
        ChatConversation findPrivateConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

        /**
         * 查找家庭群聊会话
         * 
         * @param familyId 家庭ID
         * @return 会话
         */
        @Select("SELECT * FROM chat_conversation WHERE family_id = #{familyId} AND type = 2 AND deleted = 0 LIMIT 1")
        ChatConversation findFamilyConversation(@Param("familyId") Long familyId);
}
