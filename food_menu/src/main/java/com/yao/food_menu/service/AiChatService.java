package com.yao.food_menu.service;

import com.yao.food_menu.dto.ai.ChatMessage;
import com.yao.food_menu.entity.AiChatHistory;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.AiChatHistoryMapper;
import com.yao.food_menu.service.ai.AiService;
import com.yao.food_menu.service.ai.AiServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI对话服务
 * 管理用户与AI的对话,包括历史记录
 */
@Slf4j
@Service
public class AiChatService {

    @Autowired
    private AiServiceFactory aiServiceFactory;

    @Autowired
    private AiChatHistoryMapper aiChatHistoryMapper;

    @Autowired
    private WxUserService wxUserService;

    /**
     * 发送消息给AI并获取回复
     * 
     * @param wxUserId       微信用户ID
     * @param message        用户消息
     * @param includeHistory 是否包含历史对话
     * @return AI回复
     */
    public String chat(Long wxUserId, String message, boolean includeHistory) {
        try {
            // 获取用户信息
            WxUser wxUser = wxUserService.getById(wxUserId);
            if (wxUser == null) {
                return "用户不存在";
            }

            // 获取AI服务
            AiService aiService = aiServiceFactory.getAiService();

            // 获取历史对话
            List<ChatMessage> history = new ArrayList<>();
            if (includeHistory) {
                history = getRecentHistory(wxUserId, 10); // 获取最近10条
            }

            // 调用AI服务
            String reply = aiService.chat(message, history);

            // 保存对话历史
            saveHistory(wxUserId, wxUser.getFamilyId(), "user", message);
            saveHistory(wxUserId, wxUser.getFamilyId(), "assistant", reply);

            return reply;

        } catch (Exception e) {
            log.error("AI对话失败", e);
            return "抱歉,AI服务暂时不可用,请稍后再试";
        }
    }

    /**
     * 获取最近的对话历史
     * 
     * @param wxUserId 微信用户ID
     * @param limit    限制数量
     * @return 对话历史列表
     */
    public List<ChatMessage> getRecentHistory(Long wxUserId, Integer limit) {
        try {
            List<AiChatHistory> historyList = aiChatHistoryMapper.getRecentHistory(wxUserId, limit);

            if (historyList == null || historyList.isEmpty()) {
                return new ArrayList<>();
            }

            // 反转顺序(从旧到新)
            Collections.reverse(historyList);

            // 转换为ChatMessage
            List<ChatMessage> messages = new ArrayList<>();
            for (AiChatHistory history : historyList) {
                messages.add(new ChatMessage(history.getRole(), history.getContent()));
            }

            return messages;

        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存对话历史
     * 
     * @param wxUserId 微信用户ID
     * @param familyId 家庭ID
     * @param role     角色
     * @param content  内容
     */
    private void saveHistory(Long wxUserId, Long familyId, String role, String content) {
        try {
            AiChatHistory history = new AiChatHistory();
            history.setWxUserId(wxUserId);
            history.setFamilyId(familyId);
            history.setRole(role);
            history.setContent(content);
            history.setCreateTime(LocalDateTime.now());

            aiChatHistoryMapper.insert(history);

        } catch (Exception e) {
            log.error("保存对话历史失败", e);
        }
    }

    /**
     * 清除用户的对话历史
     * 
     * @param wxUserId 微信用户ID
     * @return 是否成功
     */
    public boolean clearHistory(Long wxUserId) {
        try {
            aiChatHistoryMapper.clearHistory(wxUserId);
            return true;
        } catch (Exception e) {
            log.error("清除对话历史失败", e);
            return false;
        }
    }
}
