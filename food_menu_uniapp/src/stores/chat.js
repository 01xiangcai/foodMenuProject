/**
 * 聊天状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as chatApi from '../api/chat'
import chatWebSocket from '../utils/websocket'

export const useChatStore = defineStore('chat', () => {
    // 会话列表
    const conversations = ref([])

    // 当前会话
    const currentConversation = ref(null)

    // 当前会话消息列表
    const messages = ref([])

    // 总未读数
    const totalUnreadCount = ref(0)

    // 是否已连接WebSocket
    const isConnected = ref(false)

    // 加载状态
    const loading = ref(false)
    const loadingMore = ref(false)
    const hasMore = ref(true)

    // 计算总未读数
    const computedUnreadCount = computed(() => {
        return conversations.value.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0)
    })

    // 是否已初始化WebSocket监听器
    const wsInitialized = ref(false)

    /**
     * 初始化WebSocket连接
     */
    const initWebSocket = () => {
        // 防止重复注册事件监听器
        if (!wsInitialized.value) {
            // 监听新消息
            chatWebSocket.on('newMessage', (message) => {
                handleNewMessage(message)
            })

            // 监听消息撤回
            chatWebSocket.on('messageRevoked', (data) => {
                handleMessageRevoked(data)
            })

            // 监听连接状态
            chatWebSocket.on('connected', () => {
                isConnected.value = true
            })

            chatWebSocket.on('close', () => {
                isConnected.value = false
            })

            wsInitialized.value = true
        }

        // 建立连接（如果已连接会直接返回）
        chatWebSocket.connect()
    }

    /**
     * 处理新消息
     */
    const handleNewMessage = async (message) => {
        console.log('[ChatStore] 收到新消息:', message)

        // 如果是当前会话的消息，添加到消息列表
        if (currentConversation.value && message.conversationId === currentConversation.value.id) {
            console.log('[ChatStore] 消息属于当前会话，添加到消息列表')
            // 检查是否已存在（避免重复）
            const exists = messages.value.some(m => m.id === message.id)
            if (!exists) {
                messages.value.push(message)
                // 如果窗口在前台，自动标记已读
                chatWebSocket.sendReadAck(message.conversationId, message.id)
                // 更新会话列表预览
                updateConversationLastMessage(message, true)
            }
        } else {
            console.log('[ChatStore] 消息不属于当前会话，更新会话列表')
            // 检查会话是否存在于列表中
            const exists = conversations.value.find(c => c.id === message.conversationId)
            if (exists) {
                updateConversationLastMessage(message, false)
            } else {
                // 会话不存在（可能是被删除了，或者新会话），重新拉取会话详情
                console.log('[ChatStore] 会话列表不存在该会话，正在拉取详情...')
                try {
                    const res = await chatApi.getConversationDetail(message.conversationId)
                    if (res.data) {
                        conversations.value.unshift(res.data)
                    }
                } catch (e) {
                    console.error('[ChatStore] 拉取会话详情失败', e)
                }
            }
        }
    }

    /**
     * 处理消息撤回
     */
    const handleMessageRevoked = (data) => {
        const { messageId, conversationId } = data
        // 更新消息状态
        const index = messages.value.findIndex(m => m.id === messageId)
        if (index > -1) {
            messages.value[index].status = 1
            messages.value[index].content = '此消息已撤回'
        }
    }

    /**
     * 更新会话最后消息
     */
    const updateConversationLastMessage = (message, isCurrent = false) => {
        const conv = conversations.value.find(c => c.id === message.conversationId)
        if (conv) {
            let content = message.content
            // 如果是群聊且有发送者昵称，显示昵称
            if (conv.type === 2 && message.senderName) {
                content = `${message.senderName}: ${content}`
            }
            conv.lastMessageContent = content
            conv.lastMessageTime = message.createTime
            conv.lastMessageTimeFormatted = '刚刚'
            if (!isCurrent) {
                conv.unreadCount = (conv.unreadCount || 0) + 1
            }

            // 将该会话移到列表顶部
            const index = conversations.value.indexOf(conv)
            if (index > 0) {
                conversations.value.splice(index, 1)
                conversations.value.unshift(conv)
            }
        }

        // 更新总未读数
        totalUnreadCount.value = computedUnreadCount.value
    }

    /**
     * 获取会话列表
     */
    const fetchConversations = async () => {
        try {
            loading.value = true
            const res = await chatApi.getConversationList()
            conversations.value = res.data || []
            totalUnreadCount.value = computedUnreadCount.value
        } catch (e) {
            console.error('获取会话列表失败', e)
        } finally {
            loading.value = false
        }
    }

    /**
     * 获取消息历史
     */
    const fetchMessages = async (conversationId, page = 1) => {
        try {
            if (page === 1) {
                loading.value = true
                messages.value = []
                hasMore.value = true
            } else {
                loadingMore.value = true
            }

            const res = await chatApi.getMessageHistory(conversationId, page, 20)
            const newMessages = res.data?.records || []

            if (page === 1) {
                // 第一页，倒序显示（服务端返回的是倒序，需要反转）
                messages.value = newMessages.reverse()
            } else {
                // 加载更多，插入到开头
                messages.value = [...newMessages.reverse(), ...messages.value]
            }

            hasMore.value = newMessages.length >= 20

            // 标记最后一条消息已读
            if (messages.value.length > 0) {
                const lastMsg = messages.value[messages.value.length - 1]
                chatWebSocket.sendReadAck(conversationId, lastMsg.id)
            }
        } catch (e) {
            console.error('获取消息历史失败', e)
        } finally {
            loading.value = false
            loadingMore.value = false
        }
    }

    /**
     * 加载更多消息
     */
    const loadMoreMessages = async () => {
        if (!currentConversation.value || loadingMore.value || !hasMore.value) return

        if (messages.value.length === 0) return

        const firstMsg = messages.value[0]
        try {
            loadingMore.value = true
            const res = await chatApi.getMessagesBeforeId(
                currentConversation.value.id,
                firstMsg.id,
                20
            )
            const olderMessages = res.data || []
            messages.value = [...olderMessages.reverse(), ...messages.value]
            hasMore.value = olderMessages.length >= 20
        } catch (e) {
            console.error('加载更多消息失败', e)
        } finally {
            loadingMore.value = false
        }
    }

    /**
     * 发送消息
     */
    const sendMessage = async (content, type = 1) => {
        if (!currentConversation.value) return false

        // 优先使用WebSocket发送
        const sent = chatWebSocket.sendChatMessage(
            currentConversation.value.id,
            content,
            type
        )

        if (!sent) {
            // WebSocket未连接，使用HTTP发送
            try {
                const res = await chatApi.sendMessage({
                    conversationId: currentConversation.value.id,
                    content,
                    type
                })
                if (res.data) {
                    messages.value.push(res.data)
                    updateConversationLastMessage(res.data, true)
                }
                return true
            } catch (e) {
                console.error('发送消息失败', e)
                return false
            }
        }

        return true
    }

    /**
     * 撤回消息
     */
    const revokeMessage = async (messageId) => {
        try {
            await chatApi.revokeMessage(messageId)
            // 更新本地消息状态
            const index = messages.value.findIndex(m => m.id === messageId)
            if (index > -1) {
                messages.value[index].status = 1
                messages.value[index].content = '此消息已撤回'
            }
            return { success: true }
        } catch (e) {
            console.error('撤回消息失败', e)
            // 提取后端返回的错误信息
            const errorMsg = e.response?.data?.msg || e.message || '撤回失败，请稍后重试'
            return { success: false, errorMsg }
        }
    }

    /**
     * 标记会话已读
     */
    const markConversationAsRead = async (conversation) => {
        if (!conversation || !conversation.lastMessageId) return

        try {
            await chatApi.markAsRead(conversation.id, conversation.lastMessageId)

            // 更新本地状态
            const conv = conversations.value.find(c => c.id === conversation.id)
            if (conv) {
                const oldCount = conv.unreadCount || 0
                conv.unreadCount = 0
                totalUnreadCount.value = Math.max(0, totalUnreadCount.value - oldCount)
            }
            return true
        } catch (e) {
            console.error('标记已读失败', e)
            return false
        }
    }

    /**
     * 设置当前会话
     */
    const setCurrentConversation = (conversation) => {
        currentConversation.value = conversation
        if (conversation) {
            // 清除该会话的未读数
            const conv = conversations.value.find(c => c.id === conversation.id)
            if (conv) {
                conv.unreadCount = 0
            }
        }
    }

    /**
     * 获取或创建私聊会话
     */
    const openPrivateChat = async (targetUserId) => {
        try {
            const res = await chatApi.getOrCreatePrivateConversation(targetUserId)
            const conv = res.data
            if (conv) {
                // 乐观更新：如果在列表中不存在，手动添加
                const exists = conversations.value.find(c => c.id === conv.id)
                if (!exists) {
                    // 初始化一些用于显示的字段
                    conv.unreadCount = 0
                    conv.lastMessageContent = ''
                    conv.lastMessageTimeFormatted = ''
                    conversations.value.unshift(conv)
                }
            }
            return conv
        } catch (e) {
            console.error('创建私聊会话失败', e)
            return null
        }
    }

    /**
     * 获取或创建家庭群聊
     */
    const openFamilyChat = async () => {
        try {
            const res = await chatApi.getOrCreateFamilyConversation()
            return res.data
        } catch (e) {
            console.error('获取家庭群聊失败', e)
            return null
        }
    }

    /**
     * 删除会话
     */
    const deleteConversation = async (conversationId) => {
        try {
            await chatApi.deleteConversation(conversationId)
            // 移除本地列表中的会话
            const index = conversations.value.findIndex(c => c.id === conversationId)
            if (index > -1) {
                conversations.value.splice(index, 1)
                // 更新总未读数
                totalUnreadCount.value = computedUnreadCount.value
            }
            return true
        } catch (e) {
            console.error('删除会话失败', e)
            return false
        }
    }

    /**
     * 断开WebSocket
     */
    const disconnect = () => {
        chatWebSocket.close()
        isConnected.value = false
    }

    return {
        // 状态
        conversations,
        currentConversation,
        messages,
        totalUnreadCount,
        isConnected,
        loading,
        loadingMore,
        hasMore,

        // 方法
        initWebSocket,
        fetchConversations,
        fetchMessages,
        loadMoreMessages,
        sendMessage,
        revokeMessage,
        markConversationAsRead,
        setCurrentConversation,
        openPrivateChat,
        openFamilyChat,
        deleteConversation,
        disconnect
    }
})
