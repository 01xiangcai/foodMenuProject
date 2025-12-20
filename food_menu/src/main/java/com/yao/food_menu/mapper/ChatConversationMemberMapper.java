package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.ChatConversationMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 会话成员Mapper
 */
@Mapper
public interface ChatConversationMemberMapper extends BaseMapper<ChatConversationMember> {

        /**
         * 查询会话的所有成员
         * 
         * @param conversationId 会话ID
         * @return 成员列表
         */
        @Select("SELECT * FROM chat_conversation_member WHERE conversation_id = #{conversationId} AND deleted = 0")
        List<ChatConversationMember> selectByConversationId(@Param("conversationId") Long conversationId);

        /**
         * 查询用户在指定会话中的成员信息
         * 
         * @param conversationId 会话ID
         * @param wxUserId       用户ID
         * @return 成员信息
         */
        @Select("SELECT * FROM chat_conversation_member WHERE conversation_id = #{conversationId} AND wx_user_id = #{wxUserId} AND deleted = 0")
        ChatConversationMember selectByConversationAndUser(@Param("conversationId") Long conversationId,
                        @Param("wxUserId") Long wxUserId);

        /**
         * 获取会话中其他成员的用户ID列表（排除指定用户）
         * 
         * @param conversationId 会话ID
         * @param excludeUserId  要排除的用户ID
         * @return 用户ID列表
         */
        @Select("SELECT wx_user_id FROM chat_conversation_member WHERE conversation_id = #{conversationId} AND wx_user_id != #{excludeUserId} AND deleted = 0")
        List<Long> selectOtherMemberIds(@Param("conversationId") Long conversationId,
                        @Param("excludeUserId") Long excludeUserId);

        /**
         * 更新已读消息位置
         * 
         * @param conversationId 会话ID
         * @param wxUserId       用户ID
         * @param messageId      消息ID
         * @return 影响行数
         */
        @Update("UPDATE chat_conversation_member SET last_read_message_id = #{messageId}, unread_count = 0, update_time = NOW() "
                        +
                        "WHERE conversation_id = #{conversationId} AND wx_user_id = #{wxUserId} AND deleted = 0")
        int updateLastReadMessage(@Param("conversationId") Long conversationId, @Param("wxUserId") Long wxUserId,
                        @Param("messageId") Long messageId);

        /**
         * 增加未读消息数
         * 
         * @param conversationId 会话ID
         * @param excludeUserId  要排除的用户ID（发送者）
         * @return 影响行数
         */
        @Update("UPDATE chat_conversation_member SET unread_count = unread_count + 1, update_time = NOW(), deleted = 0 "
                        +
                        "WHERE conversation_id = #{conversationId} AND wx_user_id != #{excludeUserId}")
        int incrementUnreadCount(@Param("conversationId") Long conversationId,
                        @Param("excludeUserId") Long excludeUserId);

        /**
         * 恢复已删除的成员
         * 
         * @param conversationId 会话ID
         * @param wxUserId       用户ID
         * @return 影响行数
         */
        @Update("UPDATE chat_conversation_member SET deleted = 0, update_time = NOW() " +
                        "WHERE conversation_id = #{conversationId} AND wx_user_id = #{wxUserId}")
        int restoreMember(@Param("conversationId") Long conversationId, @Param("wxUserId") Long wxUserId);

        /**
         * 获取会话成员头像列表（用于生成群头像）
         * 
         * @param conversationId 会话ID
         * @return 头像列表
         */
        @Select("SELECT u.avatar FROM chat_conversation_member m " +
                        "INNER JOIN wx_user u ON m.wx_user_id = u.id " +
                        "WHERE m.conversation_id = #{conversationId} AND m.deleted = 0 " +
                        "ORDER BY m.join_time ASC LIMIT 9")
        List<String> selectMemberAvatars(@Param("conversationId") Long conversationId);
}
