package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.websocket.ChatWebSocketHandler;
import com.yao.food_menu.dto.ChatConversationDTO;
import com.yao.food_menu.dto.ChatMessageDTO;
import com.yao.food_menu.dto.CreateConversationDTO;
import com.yao.food_menu.dto.SendMessageDTO;
import com.yao.food_menu.entity.ChatConversation;
import com.yao.food_menu.entity.ChatMessage;
import com.yao.food_menu.entity.Family;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.FamilyMapper;
import com.yao.food_menu.mapper.WxUserMapper;
import com.yao.food_menu.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/uniapp/chat")
@Tag(name = "聊天模块", description = "小程序端聊天接口")
@RequiredArgsConstructor
public class UniappChatController {

    private final ChatService chatService;
    private final ChatWebSocketHandler webSocketHandler;
    private final WxUserMapper wxUserMapper;
    private final FamilyMapper familyMapper;

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public Result<List<ChatConversationDTO>> getConversationList() {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        List<ChatConversationDTO> list = chatService.getConversationList(wxUserId);
        return Result.success(list);
    }

    /**
     * 创建会话
     */
    @PostMapping("/conversation")
    @Operation(summary = "创建会话")
    public Result<ChatConversation> createConversation(@RequestBody CreateConversationDTO dto) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        ChatConversation conversation = chatService.createConversation(wxUserId, dto);
        return Result.success(conversation);
    }

    /**
     * 获取或创建私聊会话
     */
    @GetMapping("/conversation/private/{targetUserId}")
    @Operation(summary = "获取或创建私聊会话")
    public Result<ChatConversation> getOrCreatePrivateConversation(@PathVariable Long targetUserId) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        ChatConversation conversation = chatService.getOrCreatePrivateConversation(wxUserId, targetUserId);
        return Result.success(conversation);
    }

    /**
     * 获取或创建家庭群聊
     */
    @GetMapping("/conversation/family")
    @Operation(summary = "获取或创建家庭群聊")
    public Result<ChatConversation> getOrCreateFamilyConversation() {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        WxUser wxUser = wxUserMapper.selectById(wxUserId);
        if (wxUser == null || wxUser.getFamilyId() == null) {
            return Result.error("您尚未加入家庭");
        }
        Family family = familyMapper.selectById(wxUser.getFamilyId());
        String familyName = family != null ? family.getName() : "我的家庭";
        ChatConversation conversation = chatService.getOrCreateFamilyConversation(wxUser.getFamilyId(), familyName);

        // 确保当前用户是群成员
        if (!chatService.isMember(conversation.getId(), wxUserId)) {
            // 如果不是成员，加入群聊（这种情况发生在用户后来加入家庭时）
            CreateConversationDTO dto = new CreateConversationDTO();
            dto.setType(ChatConversation.TYPE_FAMILY);
            dto.setFamilyId(wxUser.getFamilyId());
            chatService.createConversation(wxUserId, dto);
        }

        return Result.success(conversation);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/conversation/{conversationId}")
    @Operation(summary = "获取会话详情")
    public Result<ChatConversationDTO> getConversationDetail(@PathVariable Long conversationId) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        ChatConversationDTO detail = chatService.getConversationDetail(conversationId, wxUserId);
        return Result.success(detail);
    }

    /**
     * 获取消息历史
     */
    @GetMapping("/messages")
    @Operation(summary = "获取消息历史")
    public Result<IPage<ChatMessageDTO>> getMessageHistory(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        IPage<ChatMessageDTO> messages = chatService.getMessageHistory(conversationId, wxUserId, page, size);
        return Result.success(messages);
    }

    /**
     * 获取更早的消息（加载更多）
     */
    @GetMapping("/messages/before")
    @Operation(summary = "获取更早的消息")
    public Result<List<ChatMessageDTO>> getMessagesBeforeId(
            @RequestParam Long conversationId,
            @RequestParam Long beforeId,
            @RequestParam(defaultValue = "20") int limit) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        List<ChatMessageDTO> messages = chatService.getMessagesBeforeId(conversationId, beforeId, limit, wxUserId);
        return Result.success(messages);
    }

    /**
     * 发送消息（HTTP备用接口，主要使用WebSocket）
     */
    @PostMapping("/message/send")
    @Operation(summary = "发送消息")
    public Result<ChatMessageDTO> sendMessage(@RequestBody SendMessageDTO dto) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        ChatMessage message = chatService.sendMessage(wxUserId, dto);
        ChatMessageDTO messageDTO = chatService.convertToDTO(message, wxUserId);
        return Result.success(messageDTO);
    }

    /**
     * 撤回消息
     */
    @PostMapping("/message/revoke")
    @Operation(summary = "撤回消息")
    public Result<Void> revokeMessage(@RequestParam Long messageId) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        boolean success = chatService.revokeMessage(messageId, wxUserId);
        if (success) {
            // 通知其他用户消息已撤回 - 需要获取会话ID
            // 这里简化处理，实际应该从消息中获取conversationId
            return Result.success();
        }
        return Result.error("撤回失败");
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/message/read")
    @Operation(summary = "标记消息已读")
    public Result<Void> markAsRead(
            @RequestParam Long conversationId,
            @RequestParam Long messageId) {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        chatService.markAsRead(conversationId, messageId, wxUserId);
        return Result.success();
    }

    /**
     * 获取总未读消息数
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取总未读消息数")
    public Result<Integer> getTotalUnreadCount() {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        int count = chatService.getTotalUnreadCount(wxUserId);
        return Result.success(count);
    }

    /**
     * 获取家庭成员列表（用于创建私聊）
     */
    @GetMapping("/members")
    @Operation(summary = "获取家庭成员列表")
    public Result<List<WxUser>> getFamilyMembers() {
        Long wxUserId = FamilyContext.getCurrentWxUserId();
        WxUser currentUser = wxUserMapper.selectById(wxUserId);
        if (currentUser == null || currentUser.getFamilyId() == null) {
            return Result.error("您尚未加入家庭");
        }

        // 查询同一家庭的其他成员
        List<WxUser> members = wxUserMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WxUser>()
                        .eq(WxUser::getFamilyId, currentUser.getFamilyId())
                        .ne(WxUser::getId, wxUserId)
                        .eq(WxUser::getDeleted, 0));

        // 清除敏感信息
        members.forEach(m -> {
            m.setPassword(null);
            m.setOpenid(null);
        });

        return Result.success(members);
    }
}
