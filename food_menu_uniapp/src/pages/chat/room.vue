<template>
    <view class="chat-room-page" :class="{ 'dark-mode': isDarkMode }">
        <!-- 消息列表 -->
        <scroll-view
            scroll-y
            class="message-list"
            :scroll-top="scrollTop"
            :scroll-with-animation="scrollWithAnimation"
            @scrolltoupper="onLoadMore"
            :refresher-enabled="true"
            :refresher-triggered="isRefreshing"
            @refresherrefresh="onLoadMore"
        >
            <!-- 加载更多 -->
            <view v-if="chatStore.loadingMore" class="loading-more">
                <view class="loading-spinner-small"></view>
                <text class="loading-text">加载更多...</text>
            </view>
            <view v-else-if="!chatStore.hasMore" class="no-more">
                <text>没有更多消息了</text>
            </view>

            <!-- 消息列表 -->
            <view
                v-for="(msg, index) in chatStore.messages"
                :key="msg.id"
                class="message-item"
                :class="{ 'message-self': msg.isSelf }"
            >
                <!-- 时间分割线 -->
                <view v-if="shouldShowTime(index)" class="time-divider">
                    <text>{{ formatTime(msg.createTime) }}</text>
                </view>

                <!-- 消息内容 -->
                <view class="message-wrapper">
                    <!-- 头像（非自己的消息显示在左边） -->
                    <image
                        v-if="!msg.isSelf"
                        class="message-avatar avatar-left"
                        :src="msg.senderAvatar || defaultAvatar"
                        mode="aspectFill"
                    />

                    <!-- 消息主体 -->
                    <view class="message-body" @longpress="onMessageLongPress(msg)">
                        <!-- 发送者名称（群聊中显示） -->
                        <text v-if="!msg.isSelf && isGroup" class="sender-name">
                            {{ msg.senderName }}
                        </text>

                        <!-- 消息气泡 -->
                        <view class="message-bubble" :class="{ 'revoked': msg.status === 1 }">
                            <text class="message-text">{{ msg.content }}</text>
                        </view>

                        <!-- 发送状态（自己的消息） -->
                        <view v-if="msg.isSelf" class="message-status">
                            <text v-if="msg.sending" class="status-sending">发送中</text>
                            <text v-else-if="msg.status === 1" class="status-revoked">已撤回</text>
                        </view>
                    </view>

                    <!-- 头像（自己的消息显示在右边） -->
                    <image
                        v-if="msg.isSelf"
                        class="message-avatar avatar-right"
                        :src="msg.senderAvatar || defaultAvatar"
                        mode="aspectFill"
                    />
                </view>
            </view>
        </scroll-view>

        <!-- 输入区域 -->
        <view class="input-area">
            <!-- 工具栏 -->
            <view class="input-toolbar">
                <view class="toolbar-item" @click="toggleEmoji">
                    <text class="toolbar-icon">😊</text>
                </view>
            </view>

            <!-- 输入框 -->
            <view class="input-wrapper">
                <textarea
                    class="message-input"
                    v-model="inputContent"
                    placeholder="输入消息..."
                    :maxlength="500"
                    :auto-height="true"
                    :adjust-position="true"
                    :show-confirm-bar="false"
                    @confirm="sendMessage"
                    @keydown="onKeyDown"
                />
            </view>

            <!-- 发送按钮 -->
            <view
                class="send-btn"
                :class="{ 'active': inputContent.trim() }"
                @click="sendMessage"
            >
                <text class="send-text">发送</text>
            </view>
        </view>

        <!-- 表情面板 -->
        <view v-if="showEmojiPanel" class="emoji-panel">
            <scroll-view scroll-y class="emoji-scroll">
                <view class="emoji-grid">
                    <text
                        v-for="emoji in emojis"
                        :key="emoji"
                        class="emoji-item"
                        @click="insertEmoji(emoji)"
                    >{{ emoji }}</text>
                </view>
            </scroll-view>
        </view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useChatStore } from '../../stores/chat'
import { useTheme } from '../../stores/theme'
import chatWebSocket from '../../utils/websocket'

// Props
const props = defineProps({
    id: {
        type: [String, Number],
        required: true
    },
    name: {
        type: String,
        default: ''
    },
    type: {
        type: [String, Number],
        default: 1
    }
})

const chatStore = useChatStore()
const { currentTheme } = useTheme()

// 状态
const inputContent = ref('')
const scrollTop = ref(0)
const showEmojiPanel = ref(false)
const isRefreshing = ref(false)

// 默认头像
const defaultAvatar = '/static/default-avatar.png'

// 是否深色模式
const isDarkMode = computed(() => currentTheme.value === 'dark')

// 是否群聊
const isGroup = computed(() => {
    return chatStore.currentConversation?.type === 2
})

// 表情列表
const emojis = [
    '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
    '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
    '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜',
    '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐',
    '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬',
    '😮', '🤯', '😲', '🥳', '🤠', '🥺', '😢', '😭',
    '😤', '😡', '🤬', '👍', '👎', '👏', '🙏', '💪',
    '❤️', '💔', '💯', '✨', '🎉', '🎊', '🔥', '⭐'
]

// 判断是否需要显示时间
const shouldShowTime = (index) => {
    if (index === 0) return true
    const current = chatStore.messages[index]
    const prev = chatStore.messages[index - 1]
    if (!current.createTime || !prev.createTime) return false
    
    // 间隔超过5分钟显示时间
    const diff = new Date(current.createTime) - new Date(prev.createTime)
    return diff > 5 * 60 * 1000
}

// 格式化时间
const formatTime = (time) => {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    
    if (date.toDateString() === now.toDateString()) {
        return `${hours}:${minutes}`
    }
    
    const yesterday = new Date(now)
    yesterday.setDate(yesterday.getDate() - 1)
    if (date.toDateString() === yesterday.toDateString()) {
        return `昨天 ${hours}:${minutes}`
    }
    
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${month}-${day} ${hours}:${minutes}`
}

// 发送消息
const sendMessage = async () => {
    const content = inputContent.value.trim()
    if (!content) return
    
    // 清空输入框
    inputContent.value = ''
    showEmojiPanel.value = false
    
    // 发送消息
    await chatStore.sendMessage(content, 1)
    
    // 滚动到底部（带动画）
    scrollToBottom(true)
}

// 加载更多消息
const onLoadMore = async () => {
    if (chatStore.loadingMore || !chatStore.hasMore) {
        isRefreshing.value = false
        return
    }
    
    await chatStore.loadMoreMessages()
    isRefreshing.value = false
}

// 消息长按操作
const onMessageLongPress = (msg) => {
    const items = ['复制']
    // 只有自己发送的且未撤回的消息可以撤回
    if (msg.isSelf && msg.status !== 1) {
        items.push('撤回')
    }
    
    uni.showActionSheet({
        itemList: items,
        success: async (res) => {
            if (res.tapIndex === 0) {
                // 复制
                uni.setClipboardData({
                    data: msg.content,
                    success: () => {
                        uni.showToast({ title: '已复制', icon: 'success' })
                    }
                })
            } else if (res.tapIndex === 1 && msg.isSelf) {
                // 撤回
                const success = await chatStore.revokeMessage(msg.id)
                if (!success) {
                    uni.showToast({ title: '撤回失败', icon: 'none' })
                }
            }
        }
    })
}

// 切换表情面板
const toggleEmoji = () => {
    showEmojiPanel.value = !showEmojiPanel.value
}

// 插入表情
const insertEmoji = (emoji) => {
    inputContent.value += emoji
}

// 键盘事件
const onKeyDown = (e) => {
    // 可以处理Enter发送等
}

// 滚动动画开关
const scrollWithAnimation = ref(false)

// 滚动到底部
const scrollToBottom = (animated = false) => {
    // 设置是否动画：首次加载不设动画，发送/接收消息时设动画
    scrollWithAnimation.value = animated
    
    // 设置个稍微小点的值先触发变更（可选）
    scrollTop.value = scrollTop.value - 1
    
    nextTick(() => {
        // 增加延时，确保视图渲染完成
        setTimeout(() => {
            // 设置一个足够大的值以滚动到底部
            scrollTop.value = 9999999
        }, 100)
    })
}

// 监听消息变化，自动滚动到底部
watch(
    () => chatStore.messages.length,
    () => {
        // 消息列表变化时滚动到底部（带动画）
        scrollToBottom(true)
    }
)

// 页面加载
onMounted(async () => {
    // 设置导航栏标题
    uni.setNavigationBarTitle({
        title: decodeURIComponent(props.name) || '聊天'
    })
    
    // 确保WebSocket连接已建立
    chatStore.initWebSocket()
    
    // 设置当前会话
    chatStore.setCurrentConversation({
        id: Number(props.id),
        name: decodeURIComponent(props.name),
        type: Number(props.type)
    })
    
    // 加载消息
    await chatStore.fetchMessages(Number(props.id))
    
    // 滚动到底部
    scrollToBottom()
})

// 页面卸载
onUnmounted(() => {
    chatStore.setCurrentConversation(null)
})
</script>

// 非scoped样式设置page背景，防止橡皮筋效果露底
<style lang="scss">
page {
    background-color: #ededed;
    
    &.dark-mode {
        background-color: #1a1a1a;
    }
}
</style>

<style lang="scss" scoped>
.chat-room-page {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: var(--bg-color, #ededed);
    overflow: hidden; // 防止整体页面滚动

    &.dark-mode {
        --bg-color: #1a1a1a;
        --card-bg: #2d2d2d;
        --text-color: #e0e0e0;
        --text-secondary: #888;
        --border-color: #3d3d3d;
        --bubble-self: #07c160;
        --bubble-other: #3d3d3d;
    }
}

.message-list {
    flex: 1;
    padding: 20rpx;
    background-color: var(--bg-color, #ededed); // 显式设置背景色
    box-sizing: border-box;
    overflow-y: hidden; // 让scroll-view处理滚动
}

.loading-more, .no-more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx;
    color: var(--text-secondary, #999);
    font-size: 24rpx;
}

.loading-spinner-small {
    width: 30rpx;
    height: 30rpx;
    border: 3rpx solid var(--border-color, #eee);
    border-top-color: #07c160;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-right: 10rpx;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.time-divider {
    display: flex;
    justify-content: center;
    padding: 20rpx 0;

    text {
        font-size: 22rpx;
        color: var(--text-secondary, #999);
        background-color: rgba(0, 0, 0, 0.05);
        padding: 6rpx 16rpx;
        border-radius: 8rpx;
    }
}

.message-item {
    margin-bottom: 30rpx;

    &.message-self {
        .message-wrapper {
            justify-content: flex-end;
        }

        .message-body {
            align-items: flex-end;
        }

        .message-bubble {
            background-color: var(--bubble-self, #07c160);
            color: #fff;
            
            /* 右侧气泡箭头 */
            &::after {
                content: '';
                position: absolute;
                top: 20rpx;
                right: -14rpx;
                width: 0;
                height: 0;
                border-top: 12rpx solid transparent;
                border-bottom: 12rpx solid transparent;
                border-left: 16rpx solid var(--bubble-self, #07c160);
            }
            
            /* 移除可能存在的左侧箭头配置（如果有的话） */
            &::before {
                display: none;
            }
        }
    }
}

.message-wrapper {
    display: flex;
    align-items: flex-start;
}

.message-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
}

.message-body {
    display: flex;
    flex-direction: column;
    max-width: 65%;
    margin: 0 16rpx;
}

.sender-name {
    font-size: 22rpx;
    color: var(--text-secondary, #999);
    margin-bottom: 8rpx;
}

.message-bubble {
    position: relative;
    padding: 20rpx 24rpx;
    background-color: var(--bubble-other, #fff);
    border-radius: 16rpx;
    word-break: break-all;
    
    /* 左侧气泡箭头 */
    &::before {
        content: '';
        position: absolute;
        top: 20rpx;
        left: -14rpx;
        width: 0;
        height: 0;
        border-top: 12rpx solid transparent;
        border-bottom: 12rpx solid transparent;
        border-right: 16rpx solid var(--bubble-other, #fff);
    }

    &.revoked {
        background-color: transparent;
        border: 1rpx dashed var(--text-secondary, #999);
        
        .message-text {
            color: var(--text-secondary, #999);
            font-style: italic;
        }
    }
}

.message-text {
    font-size: 30rpx;
    line-height: 1.5;
    color: var(--text-color, #333);
}

.message-status {
    margin-top: 6rpx;
    font-size: 20rpx;
    color: var(--text-secondary, #999);
}

.input-area {
    display: flex;
    align-items: flex-end;
    padding: 16rpx 20rpx;
    background-color: var(--card-bg, #f7f7f7);
    border-top: 1rpx solid var(--border-color, #e0e0e0);
}

.input-toolbar {
    display: flex;
    align-items: center;
    margin-right: 16rpx;
}

.toolbar-item {
    padding: 12rpx;
}

.toolbar-icon {
    font-size: 48rpx;
}

.input-wrapper {
    flex: 1;
    background-color: var(--bg-color, #fff);
    border-radius: 16rpx;
    padding: 16rpx 20rpx;
    max-height: 200rpx;
}

.message-input {
    width: 100%;
    font-size: 30rpx;
    line-height: 1.4;
    color: var(--text-color, #333);
    max-height: 150rpx;
}

.send-btn {
    margin-left: 16rpx;
    padding: 16rpx 28rpx;
    background-color: var(--text-secondary, #ccc);
    border-radius: 12rpx;
    transition: all 0.2s;

    &.active {
        background-color: #07c160;
    }
}

.send-text {
    font-size: 28rpx;
    color: #fff;
}

.emoji-panel {
    height: 400rpx;
    background-color: var(--card-bg, #fff);
    border-top: 1rpx solid var(--border-color, #e0e0e0);
}

.emoji-scroll {
    height: 100%;
}

.emoji-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx;
}

.emoji-item {
    width: calc(100% / 8);
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;

    &:active {
        background-color: var(--bg-color, #f5f5f5);
        border-radius: 12rpx;
    }
}
</style>
