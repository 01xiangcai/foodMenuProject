<template>
    <view class="chat-list-page" :class="{ 'dark-mode': isDarkMode }">
        <!-- 顶部搜索栏 -->
        <view class="search-bar">
            <view class="search-input-wrapper">
                <text class="search-icon">🔍</text>
                <input class="search-input" placeholder="搜索" v-model="searchKeyword" />
            </view>
            <view class="add-btn" @click="showActionSheet">
                <text class="add-icon">+</text>
            </view>
        </view>

        <!-- 会话列表 -->
        <scroll-view scroll-y class="conversation-list" @scrolltolower="onScrollToLower">
            <!-- 加载中 -->
            <view v-if="chatStore.loading" class="loading-wrapper">
                <view class="loading-spinner"></view>
                <text class="loading-text">加载中...</text>
            </view>

            <!-- 空状态 -->
            <view v-else-if="filteredConversations.length === 0" class="empty-state">
                <text class="empty-icon">💬</text>
                <text class="empty-text">暂无消息</text>
                <text class="empty-hint">点击右上角+开始聊天</text>
            </view>

            <!-- 会话列表 -->
            <view v-else>
                <view
                    v-for="conv in filteredConversations"
                    :key="conv.id"
                    class="conversation-item"
                    @click="enterChat(conv)"
                    @longpress="onLongPress(conv)"
                >
                    <!-- 头像 -->
                    <!-- 头像 -->
                    <view class="avatar-wrapper">
                        <!-- 群聊九宫格头像 -->
                        <view
                            v-if="(!conv.avatar || conv.avatar.includes('default-avatar')) && conv.memberAvatars && conv.memberAvatars.length > 0"
                            class="avatar avatar-grid"
                        >
                            <image
                                v-for="(url, index) in conv.memberAvatars.slice(0, 9)"
                                :key="index"
                                :src="url"
                                mode="aspectFill"
                                class="grid-item"
                                :class="getGridClass(conv.memberAvatars.length)"
                            />
                        </view>
                        <!-- 单个头像 -->
                        <image
                            v-else-if="conv.avatar"
                            class="avatar"
                            :src="conv.avatar"
                            mode="aspectFill"
                        />
                        <!-- 默认文字头像 -->
                        <view v-else-if="conv.type === 2" class="avatar avatar-default">
                            👨‍👩‍👧‍👦
                        </view>
                        <view v-else class="avatar avatar-default">
                             {{ getInitial(conv.name) }}
                        </view>
                        <!-- 未读角标 -->
                        <view v-if="conv.unreadCount > 0" class="unread-badge">
                            {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
                        </view>
                    </view>

                    <!-- 内容 -->
                    <view class="conversation-content">
                        <view class="conversation-header">
                            <text class="conversation-name">{{ conv.name || '未知用户' }}</text>
                            <text class="conversation-time">{{ conv.lastMessageTimeFormatted }}</text>
                        </view>
                        <view class="conversation-preview">
                            <text v-if="conv.muted" class="muted-icon">🔕</text>
                            <text class="preview-text">{{ conv.lastMessageContent || '暂无消息' }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useChatStore } from '../../stores/chat'
import { useTheme } from '../../stores/theme'
import { getFamilyMembers } from '../../api/chat'

const chatStore = useChatStore()
const { currentTheme } = useTheme()

// ... (省略中间代码)

// 页面加载
onMounted(() => {
    // 初始化WebSocket
    chatStore.initWebSocket()
})

// 页面显示
onShow(() => {
    // 每次显示页面时刷新会话列表，确保数据最新
    chatStore.fetchConversations()
    // 清除当前会话状态（防止从聊天页返回后状态未清除）
    chatStore.setCurrentConversation(null)
})

// 搜索关键词
const searchKeyword = ref('')

// 是否深色模式
const isDarkMode = computed(() => currentTheme.value === 'dark')

// 过滤后的会话列表
const filteredConversations = computed(() => {
    if (!searchKeyword.value) {
        return chatStore.conversations
    }
    return chatStore.conversations.filter(conv =>
        conv.name && conv.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
})

// 获取名称首字母
const getInitial = (name) => {
    return name ? name.substring(0, 1) : '#'
}

const getGridClass = (len) => {
    // 5人以上用3x3(9宫格)，否则用2x2(4宫格)
    return len >= 5 ? 'grid-item-9' : 'grid-item-4'
}



// 进入聊天页面
const enterChat = (conv) => {
    chatStore.setCurrentConversation(conv)
    uni.navigateTo({
        url: `/pages/chat/room?id=${conv.id}&name=${encodeURIComponent(conv.name || '')}&type=${conv.type}`
    })
}

// 长按操作
const onLongPress = (conv) => {
    uni.showActionSheet({
        itemList: ['标记已读', '删除会话'], // 暂时移除免打扰，先实现核心的
        success: (res) => {
            if (res.tapIndex === 0) {
                // 标记已读 (如果还有未读)
                if (conv.unreadCount > 0) {
                    // TODO: 调用标记会话已读接口
                    // 这里可以作为后续优化，先简单提示
                    uni.showToast({ title: '功能开发中', icon: 'none' })
                }
            } else if (res.tapIndex === 1) {
                // 删除会话
                uni.showModal({
                    title: '提示',
                    content: '确定要删除该会话吗？',
                    success: async (modalRes) => {
                        if (modalRes.confirm) {
                            uni.showLoading({ title: '删除中' })
                            const success = await chatStore.deleteConversation(conv.id)
                            uni.hideLoading()
                            if (success) {
                                uni.showToast({ title: '已删除', icon: 'success' })
                            } else {
                                uni.showToast({ title: '删除失败', icon: 'none' })
                            }
                        }
                    }
                })
            }
        }
    })
}

// 显示操作菜单
const showActionSheet = async () => {
    uni.showActionSheet({
        itemList: ['发起私聊', '家庭群聊'],
        success: async (res) => {
            if (res.tapIndex === 0) {
                // 发起私聊 - 选择家庭成员
                try {
                    const result = await getFamilyMembers()
                    const members = result.data || []
                    if (members.length === 0) {
                        uni.showToast({ title: '暂无其他家庭成员', icon: 'none' })
                        return
                    }
                    // 显示成员选择
                    uni.showActionSheet({
                        itemList: members.map(m => m.nickname || m.username || '未知用户'),
                        success: async (memberRes) => {
                            const targetUser = members[memberRes.tapIndex]
                            const conv = await chatStore.openPrivateChat(targetUser.id)
                            if (conv) {
                                enterChat({
                                    id: conv.id,
                                    name: targetUser.nickname || targetUser.username,
                                    type: 1
                                })
                            }
                        }
                    })
                } catch (e) {
                    console.error('获取家庭成员失败', e)
                }
            } else if (res.tapIndex === 1) {
                // 家庭群聊
                const conv = await chatStore.openFamilyChat()
                if (conv) {
                    enterChat({
                        id: conv.id,
                        name: conv.name || '家庭群聊',
                        type: 2
                    })
                }
            }
        }
    })
}

// 滚动到底部
const onScrollToLower = () => {
    // 可实现加载更多会话
}



// 页面卸载
onUnmounted(() => {
    // 断开连接（如果需要）
    // chatStore.disconnect()
})
</script>

<style lang="scss" scoped>
.chat-list-page {
    min-height: 100vh;
    background-color: var(--bg-color, #f5f5f5);

    &.dark-mode {
        --bg-color: #1a1a1a;
        --card-bg: #2d2d2d;
        --text-color: #e0e0e0;
        --text-secondary: #888;
        --border-color: #3d3d3d;
    }
}

.search-bar {
    display: flex;
    align-items: center;
    padding: 20rpx 30rpx;
    background-color: var(--card-bg, #fff);
    border-bottom: 1rpx solid var(--border-color, #eee);
}

.search-input-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    background-color: var(--bg-color, #f5f5f5);
    border-radius: 36rpx;
    padding: 16rpx 24rpx;
}

.search-icon {
    font-size: 28rpx;
    margin-right: 16rpx;
}

.search-input {
    flex: 1;
    font-size: 28rpx;
    color: var(--text-color, #333);
}

.add-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 20rpx;
}

.add-icon {
    font-size: 48rpx;
    color: var(--text-color, #333);
}

.conversation-list {
    height: calc(100vh - 120rpx);
}

.loading-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
}

.loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid var(--border-color, #eee);
    border-top-color: #07c160;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.loading-text {
    margin-top: 20rpx;
    font-size: 26rpx;
    color: var(--text-secondary, #999);
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 150rpx 0;
}

.empty-icon {
    font-size: 120rpx;
    margin-bottom: 30rpx;
}

.empty-text {
    font-size: 32rpx;
    color: var(--text-color, #333);
    margin-bottom: 16rpx;
}

.empty-hint {
    font-size: 26rpx;
    color: var(--text-secondary, #999);
}

.conversation-item {
    display: flex;
    align-items: center;
    padding: 24rpx 30rpx;
    background-color: var(--card-bg, #fff);
    border-bottom: 1rpx solid var(--border-color, #eee);

    &:active {
        background-color: var(--bg-color, #f5f5f5);
    }
}

.avatar-wrapper {
    position: relative;
    margin-right: 24rpx;
}

.avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 16rpx;
}

.avatar-default {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0ad4e;
    color: #fff;
    font-size: 40rpx;
    font-weight: bold;
}

.avatar-grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
    background: #dddee0;
    overflow: hidden;
    padding: 2rpx;
    box-sizing: border-box;
}

.grid-item {
    margin: 1rpx;
    background: #f2f2f2;
}

.grid-item-9 {
    width: 31%;
    height: 31%;
}

.grid-item-4 {
    width: 47%;
    height: 47%;
}

.unread-badge {
    position: absolute;
    top: -8rpx;
    right: -8rpx;
    min-width: 36rpx;
    height: 36rpx;
    line-height: 36rpx;
    text-align: center;
    font-size: 22rpx;
    color: #fff;
    background-color: #f44336;
    border-radius: 18rpx;
    padding: 0 8rpx;
}

.conversation-content {
    flex: 1;
    overflow: hidden;
}

.conversation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10rpx;
}

.conversation-name {
    font-size: 30rpx;
    font-weight: 500;
    color: var(--text-color, #333);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 400rpx;
}

.conversation-time {
    font-size: 24rpx;
    color: var(--text-secondary, #999);
}

.conversation-preview {
    display: flex;
    align-items: center;
}

.muted-icon {
    font-size: 24rpx;
    margin-right: 8rpx;
}

.preview-text {
    font-size: 26rpx;
    color: var(--text-secondary, #999);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>
