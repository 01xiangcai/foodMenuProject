/**
 * 聊天API接口
 */
import { request } from '../utils/request'

/**
 * 获取会话列表
 */
export const getConversationList = () => {
    return request({
        url: '/uniapp/chat/conversations',
        method: 'GET'
    })
}

/**
 * 创建会话
 */
export const createConversation = (data) => {
    return request({
        url: '/uniapp/chat/conversation',
        method: 'POST',
        data
    })
}

/**
 * 获取或创建私聊会话
 */
export const getOrCreatePrivateConversation = (targetUserId) => {
    return request({
        url: `/uniapp/chat/conversation/private/${targetUserId}`,
        method: 'GET'
    })
}

/**
 * 获取或创建家庭群聊
 */
export const getOrCreateFamilyConversation = () => {
    return request({
        url: '/uniapp/chat/conversation/family',
        method: 'GET'
    })
}

/**
 * 获取会话详情
 */
export const getConversationDetail = (conversationId) => {
    return request({
        url: `/uniapp/chat/conversation/${conversationId}`,
        method: 'GET'
    })
}

/**
 * 获取消息历史
 */
export const getMessageHistory = (conversationId, page = 1, size = 20) => {
    return request({
        url: '/uniapp/chat/messages',
        method: 'GET',
        data: { conversationId, page, size }
    })
}

/**
 * 获取更早的消息
 */
export const getMessagesBeforeId = (conversationId, beforeId, limit = 20) => {
    return request({
        url: '/uniapp/chat/messages/before',
        method: 'GET',
        data: { conversationId, beforeId, limit }
    })
}

/**
 * 发送消息（HTTP备用）
 */
export const sendMessage = (data) => {
    return request({
        url: '/uniapp/chat/message/send',
        method: 'POST',
        data
    })
}

/**
 * 撤回消息
 */
export const revokeMessage = (messageId) => {
    return request({
        url: `/uniapp/chat/message/revoke?messageId=${messageId}`,
        method: 'POST'
    })
}

/**
 * 标记消息已读
 */
export const markAsRead = (conversationId, messageId) => {
    return request({
        url: `/uniapp/chat/message/read?conversationId=${conversationId}&messageId=${messageId}`,
        method: 'POST'
    })
}

/**
 * 获取总未读消息数
 */
export const getTotalUnreadCount = () => {
    return request({
        url: '/uniapp/chat/unread/count',
        method: 'GET'
    })
}

/**
 * 获取家庭成员列表
 */
export const getFamilyMembers = () => {
    return request({
        url: '/uniapp/chat/members',
        method: 'GET'
    })
}

/**
 * 删除会话
 */
export const deleteConversation = (conversationId) => {
    return request({
        url: `/uniapp/chat/conversation/${conversationId}`,
        method: 'DELETE'
    })
}
