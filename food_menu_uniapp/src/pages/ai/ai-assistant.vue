<template>
  <view class="ai-page" :class="'theme-' + currentTheme">
    <!-- 顶部标题栏 -->
    <view class="header">
      <text class="header-title">AI美食助手</text>
      <view class="header-actions">
        <text class="action-btn" @tap="clearHistory">清空</text>
      </view>
    </view>

    <!-- 对话区域 -->
    <scroll-view 
      class="chat-container" 
      scroll-y 
      :scroll-into-view="scrollToView"
      scroll-with-animation
    >
      <view class="chat-list">
        <!-- 欢迎消息 -->
        <view class="message-item assistant" v-if="messages.length === 0">
          <view class="message-avatar">🤖</view>
          <view class="message-bubble">
            <text class="message-text">你好!我是AI美食助手,可以帮你推荐菜品、解答问题。试试问我"今天吃什么"吧!</text>
          </view>
        </view>

        <!-- 对话消息 -->
        <view 
          v-for="(msg, index) in messages" 
          :key="index"
          :id="'msg-' + index"
          class="message-item"
          :class="msg.role"
        >
          <view class="message-avatar" v-if="msg.role === 'assistant'">🤖</view>
          <view class="message-bubble">
            <text class="message-text">{{ msg.content }}</text>
          </view>
          <view class="message-avatar" v-if="msg.role === 'user'">👤</view>
        </view>

        <!-- 加载中 -->
        <view class="message-item assistant" v-if="loading">
          <view class="message-avatar">🤖</view>
          <view class="message-bubble loading-bubble">
            <view class="loading-dots">
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view 
        class="quick-btn" 
        v-for="(item, index) in quickActions" 
        :key="index"
        @tap="sendQuickMessage(item.text)"
      >
        <text class="quick-icon">{{ item.icon }}</text>
        <text class="quick-text">{{ item.label }}</text>
      </view>
    </view>

    <!-- 输入区域 -->
    <view class="input-area">
      <input 
        class="message-input" 
        v-model="inputText"
        placeholder="输入消息..."
        confirm-type="send"
        @confirm="sendMessage"
      />
      <view class="send-btn" @tap="sendMessage" :class="{ active: inputText.trim() }">
        <text>发送</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useTheme } from '@/stores/theme'
import { request } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const { currentTheme, applyCurrentTheme } = useTheme()

// 响应式数据
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const scrollToView = ref('')

// 快捷操作
const quickActions = ref([
  { icon: '🍽️', label: '今天吃什么', text: '今天吃什么?' },
  { icon: '🥗', label: '清淡菜品', text: '推荐一些清淡的菜品' },
  { icon: '🍖', label: '荤菜推荐', text: '推荐一些荤菜' }
])

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: text
  })

  inputText.value = ''
  loading.value = true

  // 滚动到底部
  await nextTick()
  scrollToView.value = 'msg-' + (messages.value.length - 1)

  try {
    // 调用AI接口
    const res = await request({
      url: '/wx/ai/chat',
      method: 'POST',
      data: {
        message: text,
        includeHistory: true
      }
    })

    if (res.code === 1 && res.data) {
      // 添加AI回复
      messages.value.push({
        role: 'assistant',
        content: res.data.reply
      })

      // 滚动到底部
      await nextTick()
      scrollToView.value = 'msg-' + (messages.value.length - 1)
    } else {
      uni.showToast({
        title: res.msg || 'AI服务异常',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('AI对话失败:', error)
    uni.showToast({
      title: 'AI服务暂时不可用',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 发送快捷消息
const sendQuickMessage = (text) => {
  inputText.value = text
  sendMessage()
}

// 清空对话历史
const clearHistory = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空对话历史吗?',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: '/wx/ai/clear-history',
            method: 'DELETE'
          })
          messages.value = []
          uni.showToast({
            title: '已清空',
            icon: 'success'
          })
        } catch (error) {
          console.error('清空历史失败:', error)
        }
      }
    }
  })
}

onShow(() => {
  applyCurrentTheme()
  
  // 检查用户ID是否存在
  const wxUserId = uni.getStorageSync('wx_user_id')
  const token = uni.getStorageSync('fm_token')
  
  console.log('AI助手页面 - Token:', token ? '已存在' : '不存在')
  console.log('AI助手页面 - 用户ID:', wxUserId || '不存在')
  
  if (!wxUserId) {
    uni.showModal({
      title: '提示',
      content: '检测到您还未重新登录，AI功能需要用户信息。请退出登录后重新登录。',
      confirmText: '去登录',
      cancelText: '稍后',
      success: (res) => {
        if (res.confirm) {
          // 清除token
          uni.removeStorageSync('fm_token')
          uni.removeStorageSync('wx_user_id')
          // 跳转到登录页
          uni.reLaunch({
            url: '/pages/login/login'
          })
        }
      }
    })
  }
})
</script>

<style lang="scss" scoped>
.ai-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-page);
}

// 顶部标题栏
.header {
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-card);
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

  .header-title {
    font-size: 36rpx;
    font-weight: 700;
    color: var(--text-primary);
  }

  .action-btn {
    font-size: 28rpx;
    color: var(--accent-orange);
    padding: 10rpx 20rpx;
  }
}

// 对话区域
.chat-container {
  flex: 1;
  padding: 20rpx;
  overflow-y: auto;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  animation: slideIn 0.3s ease-out;

  &.user {
    flex-direction: row-reverse;

    .message-bubble {
      background: linear-gradient(135deg, #FF9F43, #FF7D58);
      color: #fff;
    }
  }

  &.assistant {
    .message-bubble {
      background: var(--bg-card);
      color: var(--text-primary);
    }
  }
}

.message-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background: var(--bg-input);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
}

.message-bubble {
  max-width: 500rpx;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);

  .message-text {
    font-size: 28rpx;
    line-height: 1.6;
    word-wrap: break-word;
  }
}

// 加载动画
.loading-bubble {
  padding: 30rpx 40rpx;
}

.loading-dots {
  display: flex;
  gap: 10rpx;

  .dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    background: var(--text-secondary);
    animation: bounce 1.4s infinite ease-in-out;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }

    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

// 快捷操作
.quick-actions {
  padding: 20rpx;
  display: flex;
  gap: 16rpx;
  background: var(--bg-page);
}

.quick-btn {
  flex: 1;
  padding: 20rpx;
  background: var(--bg-card);
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;

  &:active {
    transform: scale(0.96);
  }

  .quick-icon {
    font-size: 40rpx;
  }

  .quick-text {
    font-size: 22rpx;
    color: var(--text-secondary);
  }
}

// 输入区域
.input-area {
  padding: 20rpx;
  background: var(--bg-card);
  display: flex;
  gap: 16rpx;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.message-input {
  flex: 1;
  padding: 20rpx 24rpx;
  background: var(--bg-input);
  border-radius: 50rpx;
  font-size: 28rpx;
  color: var(--text-primary);
}

.send-btn {
  padding: 20rpx 32rpx;
  background: var(--bg-input);
  border-radius: 50rpx;
  font-size: 28rpx;
  color: var(--text-secondary);
  transition: all 0.3s;

  &.active {
    background: linear-gradient(135deg, #FF9F43, #FF7D58);
    color: #fff;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
