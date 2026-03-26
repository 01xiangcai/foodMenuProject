<template>
  <view>
    <!-- 悬浮球 -->
    <view
      v-if="!isOpen"
      class="float-ball"
      :style="{ background: 'linear-gradient(135deg, ' + themeColor + ', ' + darkenColor + ')' }"
      @tap="openChat"
    >
      <text class="float-ball-icon">💬</text>
      <!-- 呼吸光圈 -->
      <view class="ball-ping" :style="{ borderColor: themeColor }"></view>
    </view>

    <!-- 聊天弹窗蒙层 -->
    <view v-if="isOpen" class="chat-mask" @tap.self="closeChat"></view>

    <!-- 聊天弹窗 -->
    <view v-if="isOpen" class="chat-window" :class="{ 'chat-window-in': isOpen }">
      <!-- 弹窗头部 -->
      <view class="chat-header" :style="{ background: 'linear-gradient(135deg, ' + themeColor + ', ' + darkenColor + ')' }">
        <view class="header-left">
          <view class="bot-avatar">🤖</view>
          <view>
            <text class="bot-name">{{ appName }}</text>
            <view class="bot-status">
              <view class="status-dot"></view>
              <text class="status-text">在线</text>
            </view>
          </view>
        </view>
        <view class="header-right">
          <text class="header-btn" @tap.stop="resetChat">🔄</text>
          <text class="header-btn" @tap.stop="closeChat">✕</text>
        </view>
      </view>

      <!-- 消息列表 -->
      <scroll-view
        class="chat-body"
        scroll-y
        :scroll-into-view="scrollTargetId"
        scroll-with-animation
      >
        <view class="msg-list">
          <view
            v-for="(msg, index) in messages"
            :key="index"
            :id="'chat-msg-' + index"
            class="msg-row"
            :class="msg.role"
          >
            <view class="msg-avatar">{{ msg.role === 'assistant' ? '🤖' : '🧑' }}</view>
            <view class="bubble" :style="msg.role === 'user' ? { backgroundColor: themeColor } : {}">
              <text>{{ msg.content }}</text>
            </view>
          </view>

          <!-- 打字动画 -->
          <view v-if="isTyping" id="chat-msg-typing" class="msg-row assistant">
            <view class="msg-avatar">🤖</view>
            <view class="bubble typing">
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 输入区 -->
      <view class="chat-footer">
        <textarea
          v-model="inputText"
          class="chat-input"
          placeholder="说点什么..."
          :auto-height="true"
          :maxlength="500"
          :cursor-spacing="20"
          :show-confirm-bar="false"
        />
        <view
          class="send-btn"
          :class="{ 'send-disabled': !inputText.trim() || isTyping }"
          :style="{ backgroundColor: inputText.trim() && !isTyping ? themeColor : '' }"
          @tap="sendMessage"
        >
          <text>发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'

// ============= 在此配置 =============
const SERVER_URL = 'http://localhost:9900'
const APP_KEY = 'ak_TzPYVd5y8rum'
// ====================================

const isOpen = ref(false)
const appName = ref('AI 客服')
const themeColor = ref('#7C3AED')
const welcomeMsg = ref('你好！有什么可以帮您的吗？')
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const scrollTargetId = ref('')

// 派生深色版本（用于渐变）
const darkenColor = computed(() => {
  return themeColor.value === '#7C3AED' ? '#4e4376' : '#2b2b2b'
})

// 会话 ID（基于 AppKey 隔离）
let sessionId = uni.getStorageSync('aics_sid_' + APP_KEY)
if (!sessionId) {
  sessionId = 'wx_' + Math.random().toString(36).substring(2, 10)
  uni.setStorageSync('aics_sid_' + APP_KEY, sessionId)
}

let configLoaded = false

const openChat = () => {
  isOpen.value = true
  if (!configLoaded) {
    loadConfig()
  }
}

const closeChat = () => {
  isOpen.value = false
}

const resetChat = () => {
  uni.showModal({
    title: '重置对话',
    content: '将清空本次对话记录，是否继续？',
    success: (res) => {
      if (res.confirm) {
        sessionId = 'wx_' + Math.random().toString(36).substring(2, 10)
        uni.setStorageSync('aics_sid_' + APP_KEY, sessionId)
        messages.value = [{ role: 'assistant', content: welcomeMsg.value }]
      }
    }
  })
}

const loadConfig = () => {
  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/info`,
    method: 'GET',
    success: (res) => {
      if (res.data?.code === 200 && res.data?.data) {
        const info = res.data.data
        appName.value = info.appName || appName.value
        themeColor.value = info.themeColor || themeColor.value
        welcomeMsg.value = info.welcomeMsg || welcomeMsg.value
      }
      loadHistory()
      configLoaded = true
    },
    fail: () => {
      loadHistory()
      configLoaded = true
    }
  })
}

const loadHistory = () => {
  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/history`,
    data: { sessionId, limit: 30 },
    method: 'GET',
    success: (res) => {
      if (res.data?.code === 200 && res.data?.data?.length > 0) {
        messages.value = res.data.data.map(m => ({ role: m.role, content: m.content }))
      } else {
        messages.value = [{ role: 'assistant', content: welcomeMsg.value }]
      }
      scrollToBottom()
    },
    fail: () => {
      messages.value = [{ role: 'assistant', content: welcomeMsg.value }]
    }
  })
}

const sendMessage = () => {
  if (!inputText.value.trim() || isTyping.value) return

  const text = inputText.value
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  isTyping.value = true
  scrollToBottom()

  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/message`,
    method: 'POST',
    data: { message: text, sessionId, userId: sessionId },
    success: (res) => {
      if (res.data?.code === 200 && res.data?.data) {
        messages.value.push({ role: 'assistant', content: res.data.data })
      } else {
        messages.value.push({ role: 'assistant', content: '抱歉，暂时无法回复，请稍后重试。' })
      }
    },
    fail: () => {
      messages.value.push({ role: 'assistant', content: '网络错误，请检查连接后重试。' })
    },
    complete: () => {
      isTyping.value = false
      scrollToBottom()
    }
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (isTyping.value) {
      scrollTargetId.value = 'chat-msg-typing'
    } else {
      scrollTargetId.value = 'chat-msg-' + (messages.value.length - 1)
    }
  })
}
</script>

<style scoped>
/* 悬浮球 */
.float-ball {
  position: fixed;
  right: 40rpx;
  bottom: 200rpx;
  width: 108rpx;
  height: 108rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(124, 58, 237, 0.4);
  z-index: 999;
  overflow: visible;
}

.float-ball-icon {
  font-size: 52rpx;
  z-index: 1;
}

/* 呼吸光圈 */
.ball-ping {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3rpx solid;
  opacity: 0;
  animation: ping 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}

@keyframes ping {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.8); opacity: 0; }
}

/* 蒙层 */
.chat-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

/* 聊天窗口 */
.chat-window {
  position: fixed;
  right: 30rpx;
  bottom: 200rpx;
  width: 640rpx;
  height: 860rpx;
  background: #f8f9fb;
  border-radius: 32rpx;
  box-shadow: 0 20rpx 80rpx rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1001;
  transform-origin: right bottom;
  animation: windowIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes windowIn {
  from { transform: scale(0.1); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

/* 头部 */
.chat-header {
  padding: 28rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.bot-avatar {
  font-size: 52rpx;
}

.bot-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  display: block;
}

.bot-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #4ade80;
}

.status-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}

.header-right {
  display: flex;
  gap: 20rpx;
}

.header-btn {
  font-size: 30rpx;
  color: rgba(255,255,255,0.85);
  padding: 10rpx;
}

/* 消息主体 */
.chat-body {
  flex: 1;
  overflow: hidden;
}

.msg-list {
  padding: 24rpx 20rpx;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 28rpx;
  gap: 14rpx;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  font-size: 38rpx;
  flex-shrink: 0;
}

.bubble {
  max-width: 78%;
  padding: 18rpx 24rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  background: #fff;
  color: #334155;
}

.msg-row.assistant .bubble {
  border-bottom-left-radius: 4rpx;
}

.msg-row.user .bubble {
  color: #fff;
  border-bottom-right-radius: 4rpx;
}

/* 打字动画 */
.typing {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 22rpx 28rpx;
}

.dot {
  width: 10rpx;
  height: 10rpx;
  background: #94a3b8;
  border-radius: 50%;
  animation: dotBounce 1.4s infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10rpx); }
}

/* 输入区 */
.chat-footer {
  padding: 16rpx 20rpx;
  background: #fff;
  border-top: 1px solid #f1f5f9;
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  flex-shrink: 0;
}

.chat-input {
  flex: 1;
  background: #f1f5f9;
  border-radius: 24rpx;
  padding: 16rpx 24rpx;
  font-size: 28rpx;
  max-height: 160rpx;
  min-height: 72rpx;
}

.send-btn {
  width: 120rpx;
  height: 72rpx;
  border-radius: 24rpx;
  background: #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.send-btn text {
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
}

.send-disabled {
  opacity: 0.6;
}
</style>
