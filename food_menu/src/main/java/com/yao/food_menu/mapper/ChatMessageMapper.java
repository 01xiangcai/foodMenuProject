package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 分页查询会话消息（按时间倒序）
     * 
     * @param page           分页参数
     * @param conversationId 会话ID
     * @return 消息列表
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY create_time DESC")
    IPage<ChatMessage> selectByConversationId(Page<ChatMessage> page, @Param("conversationId") Long conversationId);

    /**
     * 查询指定消息ID之前的消息（用于加载更多历史消息）
     * 
     * @param conversationId 会话ID
     * @param beforeId       消息ID
     * @param limit          数量限制
     * @return 消息列表
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND id < #{beforeId} AND deleted = 0 "
            +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> selectBeforeId(@Param("conversationId") Long conversationId, @Param("beforeId") Long beforeId,
            @Param("limit") int limit);

    /**
     * 查询指定消息ID之后的消息（用于同步新消息）
     * 
     * @param conversationId 会话ID
     * @param afterId        消息ID
     * @return 消息列表
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND id > #{afterId} AND deleted = 0 ORDER BY create_time ASC")
    List<ChatMessage> selectAfterId(@Param("conversationId") Long conversationId, @Param("afterId") Long afterId);

    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param senderId  发送者ID（用于校验）
     * @return 影响行数
     */
    @Update("UPDATE chat_message SET status = 1, content = '此消息已撤回', update_time = NOW() " +
            "WHERE id = #{messageId} AND sender_id = #{senderId} AND deleted = 0")
    int revokeMessage(@Param("messageId") Long messageId, @Param("senderId") Long senderId);

    /**
     * 获取会话最新的消息
     * 
     * @param conversationId 会话ID
     * @return 最新消息
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    ChatMessage selectLatestMessage(@Param("conversationId") Long conversationId);

    /**
     * 获取用户在会话中的未读消息数
     * 
     * @param conversationId    会话ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 未读消息数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE conversation_id = #{conversationId} AND id > #{lastReadMessageId} AND deleted = 0")
    int countUnreadMessages(@Param("conversationId") Long conversationId,
            @Param("lastReadMessageId") Long lastReadMessageId);
}
