<template>
    <view class="chat-list-page" :class="{ 'dark-mode': isDarkMode }">
        <!-- 顶部搜索栏 -->
        <view class="search-bar">
            <view class="search-input-wrapper">
                <text class="search-icon">🔍</text>
                <input class="search-input" placeholder="搜索" v-model="searchKeyword" />
            </view>
            <view class="add-btn" @click="onAddClick">
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
                <!-- 系统通知入口 -->
                <view class="notification-entry" @click="goToNotifications">
                    <view class="notification-icon">
                        <text class="icon-emoji">🔔</text>
                        <view v-if="unreadNotificationCount > 0" class="notification-badge">
                            {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
                        </view>
                    </view>
                    <view class="notification-content">
                        <view class="notification-header">
                            <text class="notification-title">系统通知</text>
                            <text class="notification-time" v-if="latestNotification">{{ formatNotificationTime(latestNotification.createTime) }}</text>
                        </view>
                        <text class="notification-desc">{{ latestNotificationText }}</text>
                    </view>
                    <text class="notification-arrow">›</text>
                </view>
                <view
                    v-for="conv in filteredConversations"
                    :key="conv.id"
                    class="conversation-item"
                    @click="enterChat(conv)"
                    @longpress="onLongPress(conv, $event)"
                >
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
                            @error="onAvatarError(conv)"
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

        <!-- 自定义遮罩和弹窗 -->
        <!-- 右上角菜单 -->
        <view class="mask" v-if="showDropdown" @click="showDropdown = false" @touchmove.stop.prevent></view>
        <view class="dropdown-menu" v-if="showDropdown">
            <view class="menu-arrow"></view>
            <view class="menu-item" @click="handleMenuClick(0)">
                <text class="menu-icon">💬</text>
                <text class="menu-text">发起私聊</text>
            </view>
            <view class="menu-divider"></view>
            <view class="menu-item" @click="handleMenuClick(1)">
                <text class="menu-icon">👥</text>
                <text class="menu-text">家庭群聊</text>
            </view>
        </view>

        <!-- 长按跟随菜单 -->
        <view class="mask" v-if="showActionMenu" @click="closeActionMenu" @touchmove.stop.prevent></view>
        <view class="context-menu" v-if="showActionMenu" :style="{ top: menuTop + 'px' }">
            <view class="menu-item" @click="handleActionClick(0)">
                <text class="menu-text">标记已读</text>
            </view>
            <view class="menu-divider"></view>
            <view class="menu-item" @click="handleActionClick(1)">
                <text class="menu-text" style="color: #fa5151;">删除会话</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useChatStore } from '../../stores/chat'
import { useTheme } from '../../stores/theme'
import { getFamilyMembers } from '../../api/chat'
import { getUnreadCount, getNotificationList } from '../../api/notification'

const chatStore = useChatStore()
const { currentTheme } = useTheme()

// 系统通知未读数量
const unreadNotificationCount = ref(0)
// 最新通知
const latestNotification = ref(null)

// 最新通知文本
const latestNotificationText = computed(() => {
    if (latestNotification.value) {
        return latestNotification.value.title || '查看餐次发布、菜品采纳等通知'
    }
    return '查看餐次发布、菜品采纳等通知'
})

// 自定义弹窗状态
const showDropdown = ref(false)
const showActionMenu = ref(false)
const currentConv = ref(null)
const menuTop = ref(0)

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
    // 获取系统通知未读数量
    fetchUnreadNotifications()
})

// 获取系统通知未读数量和最新通知
const fetchUnreadNotifications = async () => {
    try {
        // 获取未读数量
        const res = await getUnreadCount()
        if (res.code === 1 && res.data) {
            unreadNotificationCount.value = res.data.count || 0
        }
        // 获取最新通知
        const listRes = await getNotificationList(1, 1)
        if (listRes.code === 1 && listRes.data && listRes.data.records && listRes.data.records.length > 0) {
            latestNotification.value = listRes.data.records[0]
        }
    } catch (error) {
        console.error('获取通知信息失败:', error)
    }
}

// 格式化通知时间
const formatNotificationTime = (time) => {
    if (!time) return ''
    const date = new Date(time.replace(/-/g, '/'))
    const now = new Date()
    const diff = now - date
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    
    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    return `${date.getMonth() + 1}/${date.getDate()}`
}

// 跳转到系统通知页面
const goToNotifications = () => {
    uni.navigateTo({
        url: '/pages/notification/list'
    })
}

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

// 长按会话
const onLongPress = (conv, event) => {
    currentConv.value = conv
    let clientY = 0
    // 兼容不同平台的事件对象结构
    if (event.touches && event.touches[0]) {
        clientY = event.touches[0].clientY
    } else if (event.detail && event.detail.y) {
        clientY = event.detail.y
    }
    
    // 设置位置
    menuTop.value = clientY + 10
    
    // 简单边界处理
    try {
        const sys = uni.getSystemInfoSync()
        if (menuTop.value > sys.windowHeight - 150) {
            menuTop.value = clientY - 120
        }
    } catch (e) {}
    
    showActionMenu.value = true
}

// 处理底部菜单点击
const handleActionClick = async (index) => {
    if (!currentConv.value) return
    const conv = currentConv.value
    showActionMenu.value = false
    
    if (index === 0) {
        // 标记已读
        if (conv.unreadCount > 0) {
            const success = await chatStore.markConversationAsRead(conv)
            if (success) {
                uni.showToast({ title: '已标记', icon: 'success' })
            }
        } else {
            uni.showToast({ title: '暂无未读消息', icon: 'none' })
        }
    } else if (index === 1) {
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

// 关闭底部菜单
const closeActionMenu = () => {
    showActionMenu.value = false
}

// 点击右上角加号
const onAddClick = () => {
    showDropdown.value = !showDropdown.value
}

// 处理右上角菜单点击
const handleMenuClick = async (index) => {
    showDropdown.value = false
    if (index === 0) {
        // 发起私聊 - 跳转选择联系人页面
        uni.navigateTo({
            url: '/pages/chat/member-select'
        })
    } else if (index === 1) {
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

// 头像加载失败
const onAvatarError = (conv) => {
    conv.avatar = null
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

/* UI Components for Menus */
.mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: transparent;
    z-index: 998;
}

/* Dropdown Menu (Top Right) */
.dropdown-menu {
    position: fixed;
    top: 100rpx;
    right: 20rpx;
    background-color: #4c4c4c;
    border-radius: 12rpx;
    padding: 0;
    z-index: 999;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.2);
    min-width: 260rpx;
}

.menu-arrow {
    position: absolute;
    top: -12rpx;
    right: 20rpx;
    width: 0;
    height: 0;
    border-left: 12rpx solid transparent;
    border-right: 12rpx solid transparent;
    border-bottom: 12rpx solid #4c4c4c;
}

.menu-item {
    display: flex;
    align-items: center;
    padding: 24rpx 30rpx;
}

.menu-item:active {
    background-color: #5d5d5d;
    border-radius: 12rpx;
}

.menu-icon {
    font-size: 34rpx;
    margin-right: 24rpx;
    color: #fff;
}

.menu-text {
    font-size: 30rpx;
    color: #fff;
    font-weight: 400;
}

.menu-divider {
    height: 1rpx;
    background-color: #5f5f5f;
    margin: 0 20rpx;
}

/* Action Sheet (Bottom) */
.action-sheet {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: #f7f7f7;
    border-top-left-radius: 24rpx;
    border-top-right-radius: 24rpx;
    z-index: 999;
    transform: translateY(100%);
    transition: transform 0.3s;
    padding-bottom: env(safe-area-inset-bottom);
}

.action-sheet.show {
    transform: translateY(0);
}

.action-item, .action-cancel {
    background-color: #fff;
    padding: 32rpx 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 34rpx;
    color: #000;
}

.action-item:active, .action-cancel:active {
    background-color: #f2f2f2;
}

.action-item.destructive {
    color: #fa5151;
}

.action-divider {
    height: 1rpx;
    background-color: #e5e5e5;
}

.action-cancel {
    margin-top: 16rpx;
}

/* Context Menu (Floating) */
.context-menu {
    position: fixed;
    left: 50%;
    transform: translateX(-50%);
    width: 300rpx;
    background-color: #fff;
    border-radius: 12rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.2);
    z-index: 999;
    padding: 0;
}

.context-menu .menu-item:active {
    background-color: #f5f5f5;
}

.context-menu .menu-text {
    color: #333;
}

.context-menu .menu-icon {
    color: #333;
}

.context-menu .menu-divider {
    background-color: #eee;
}

/* 系统通知入口样式 */
.notification-entry {
    display: flex;
    align-items: center;
    padding: 28rpx 30rpx;
    background: linear-gradient(135deg, #fff9f5, #fff5ed);
    border-bottom: 1rpx solid #f0e6df;
    margin-bottom: 16rpx;
    
    &:active {
        background: linear-gradient(135deg, #fff3ec, #ffebe0);
    }
}

.notification-icon {
    position: relative;
    width: 96rpx;
    height: 96rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #FF7D58, #FF9A6C);
    border-radius: 20rpx;
    margin-right: 24rpx;
    box-shadow: 0 4rpx 12rpx rgba(255, 125, 88, 0.3);
    
    .icon-emoji {
        font-size: 48rpx;
    }
    
    .notification-badge {
        position: absolute;
        top: -10rpx;
        right: -10rpx;
        min-width: 36rpx;
        height: 36rpx;
        line-height: 36rpx;
        text-align: center;
        font-size: 22rpx;
        color: #fff;
        background-color: #f44336;
        border-radius: 18rpx;
        padding: 0 8rpx;
        border: 2rpx solid #fff;
    }
}

.notification-content {
    flex: 1;
    min-width: 0;
    
    .notification-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8rpx;
    }
    
    .notification-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
    }
    
    .notification-time {
        font-size: 24rpx;
        color: #999;
    }
    
    .notification-desc {
        display: block;
        font-size: 26rpx;
        color: #888;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}

.notification-arrow {
    font-size: 40rpx;
    color: #ccc;
    font-weight: 300;
}

.dark-mode {
    .notification-entry {
        background: linear-gradient(135deg, #2d2520, #352a22);
        border-bottom-color: #3d3530;
    }
    
    .notification-content .notification-title {
        color: #e0e0e0;
    }
    
    .notification-content .notification-desc {
        color: #888;
    }
    
    .notification-arrow {
        color: #666;
    }
}
</style>
