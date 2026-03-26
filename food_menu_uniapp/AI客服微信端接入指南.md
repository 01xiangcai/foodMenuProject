# 微信小程序 / UniApp AI 智能客服悬浮球接入指南

这是一套目前微信小程序端公认的**最优悬浮球方案**。不仅实现了类似原生 App 级的顺滑拖拽，还加入了极具细节的**智能闲置吸附避让机制**（闲置时半透明缩进屏幕，不遮挡页面内容）。

## ✨ 核心特性
1. **原生级拖拽**：使用微信官方的高性能 `<movable-area>` ，彻底解决传统 `view` 拖拽发卡及防穿透问题。
2. **智能边缘吸附**：3 秒以上无交互，整个悬浮球会自动变成 50% 半透明，并向所在的屏幕边缘（左/右）隐去一半，只漏出一个小月牙，极大减少对正文页的视觉遮挡。
3. **秒级唤醒提示**：只要手指触碰，立刻满血弹回并恢复动画。气泡中自带呼吸“专属客服”提示。
4. **极简逻辑注入**：几百行的请求、维持状态及滚动定位相关的复杂代码都被抽取到了全局的 Composable 钩子里，任意页面仅需一行引入。

---

## 🛠️ 三步极速接入

### 1. 拷贝内核逻辑脚本 (Composable)
在项目的根或者 `src/composables/` 目录下创建一个名为 `useAiChat.js` 的文件。

```javascript
import { ref, nextTick } from 'vue'

/**
 * AI 客服核心逻辑 Hook
 * @param {string} serverUrl  后端地址，例如 'https://api.yourdomain.com'
 * @param {string} appKey     机器人 AppKey（从管理后台复制）
 */
export function useAiChat(serverUrl = 'http://127.0.0.1:9900', appKey = '') {
  const csOpen = ref(false)
  const csAppName = ref('AI 客服')
  const csTheme = ref('#7C3AED')
  const csWelcome = ref('你好！有什么可以帮您的吗？')
  const csMessages = ref([])
  const csInput = ref('')
  const csTyping = ref(false)
  const csScrollId = ref('')

  // 会话隔离：只给本用户、本机器人单独分配会话上下文
  let sessionId = uni.getStorageSync('aics_sid_' + appKey) || ''
  if (!sessionId) {
    sessionId = 'wx_' + Math.random().toString(36).substring(2, 10)
    uni.setStorageSync('aics_sid_' + appKey, sessionId)
  }

  let configLoaded = false

  const openCs = () => {
    csOpen.value = true
    if (!configLoaded) {
      uni.request({
        url: `${serverUrl}/open/chat/${appKey}/info`,
        method: 'GET',
        success: (res) => {
          if (res.data?.code === 200 && res.data?.data) {
            const d = res.data.data
            csAppName.value = d.appName || csAppName.value
            csTheme.value = d.themeColor || csTheme.value
            csWelcome.value = d.welcomeMsg || csWelcome.value
          }
          _loadHistory()
          configLoaded = true
        },
        fail: () => { _loadHistory(); configLoaded = true }
      })
    }
  }

  const closeCs = () => { csOpen.value = false }

  const resetCs = () => {
    uni.showModal({
      title: '重置对话',
      content: '将清空本次对话，是否继续？',
      success: (r) => {
        if (r.confirm) {
          sessionId = 'wx_' + Math.random().toString(36).substring(2, 10)
          uni.setStorageSync('aics_sid_' + appKey, sessionId)
          csMessages.value = [{ role: 'assistant', content: csWelcome.value }]
        }
      }
    })
  }

  const csSend = () => {
    if (!csInput.value.trim() || csTyping.value) return
    const text = csInput.value
    csMessages.value.push({ role: 'user', content: text })
    csInput.value = ''
    csTyping.value = true
    _scrollToBottom()

    uni.request({
      url: `${serverUrl}/open/chat/${appKey}/message`,
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { message: text, sessionId, userId: sessionId },
      success: (res) => {
        csMessages.value.push({
          role: 'assistant',
          content: (res.data?.code === 200 && res.data?.data) ? res.data.data : '暂时无回复'
        })
      },
      fail: (err) => {
        csMessages.value.push({ role: 'assistant', content: '网络错误，请检查连接' })
      },
      complete: () => { csTyping.value = false; _scrollToBottom() }
    })
  }

  const _loadHistory = () => {
    uni.request({
      url: `${serverUrl}/open/chat/${appKey}/history`,
      data: { sessionId, limit: 30 },
      method: 'GET',
      success: (res) => {
        if (res.data?.code === 200 && res.data?.data?.length > 0) {
          csMessages.value = res.data.data.map(m => ({ role: m.role, content: m.content }))
        } else {
          csMessages.value = [{ role: 'assistant', content: csWelcome.value }]
        }
        _scrollToBottom()
      },
      fail: () => { csMessages.value = [{ role: 'assistant', content: csWelcome.value }] }
    })
  }

  const _scrollToBottom = () => {
    nextTick(() => {
      csScrollId.value = csTyping.value ? 'cs-typing' : 'cs-msg-' + (csMessages.value.length - 1)
    })
  }

  return {
    csOpen, csAppName, csTheme, csWelcome,
    csMessages, csInput, csTyping, csScrollId,
    openCs, closeCs, resetCs, csSend
  }
}
```

### 2. 在业务逻辑层 (`script`) 一行激活
在你需要展示悬浮球的页面（比如 `index.vue`），引入上面写好的文件，加上关于坐标系的控制：

```javascript
import { ref, onMounted } from 'vue'
import { useAiChat } from '@/composables/useAiChat'

// --- AI 客服组件注册 (配置你的 ServerUrl 和 AppKey) ---
const {
  csOpen, csAppName, csTheme, csMessages, csInput, csTyping, csScrollId,
  openCs, closeCs, resetCs, csSend
} = useAiChat('http://127.0.0.1:9900', 'YOUR_APP_KEY_HERE')

// --- 悬浮球的动画坐标与贴边休眠控制 ---
const csPosX = ref(0)
const csPosY = ref(0)
const csActive = ref(true)
const csSnapDirection = ref('') // 'left' | 'right'
let csActiveTimer = null
let csCurrentX = 0
let winWidth = 0
let ballRadius = 25 // 默认 50px 小球

onMounted(() => {
  const sysInfo = uni.getSystemInfoSync()
  winWidth = sysInfo.windowWidth
  const ballSize = winWidth / 750 * 110
  ballRadius = ballSize / 2
  
  const defaultX = uni.getStorageSync('cs_pos_x')
  const defaultY = uni.getStorageSync('cs_pos_y')
  if (defaultX === '' || defaultY === '') {
    // 默认靠右下角
    csPosX.value = winWidth - ballSize - 16
    csPosY.value = sysInfo.windowHeight - ballSize - 90
  } else {
    csPosX.value = Number(defaultX)
    csPosY.value = Number(defaultY)
  }
  csCurrentX = csPosX.value
  resetCsActive()
})

const resetCsActive = () => {
  csActive.value = true
  csSnapDirection.value = ''
  if (csActiveTimer) clearTimeout(csActiveTimer)
  
  // 唤醒：贴边状态轻轻弹拉出
  if (winWidth > 0) {
    if (csCurrentX <= 10) {
      csPosX.value = 10
    } else if (csCurrentX >= winWidth - ballRadius * 2 - 10) {
      csPosX.value = winWidth - ballRadius * 2 - 10
    }
  }

  // 3秒无动作后，执行透明缩边
  csActiveTimer = setTimeout(() => {
    csActive.value = false
    if (winWidth > 0) {
      if (csCurrentX < winWidth / 2) {
        csPosX.value = 0
        csSnapDirection.value = 'left'
      } else {
        csPosX.value = winWidth - ballRadius * 2
        csSnapDirection.value = 'right'
      }
    }
  }, 3000)
}

const onCsMove = (e) => {
  csCurrentX = e.detail.x
  if (e.detail.source === 'touch') {
    resetCsActive()
    uni.setStorageSync('cs_pos_x', e.detail.x)
    uni.setStorageSync('cs_pos_y', e.detail.y)
  }
}

const handleOpenCs = () => {
  resetCsActive()
  openCs()
}
```

### 3. 复制模板页面代码 (`template` & `style`) 

在页面的 `<template>` 中，最外层容器结束的最下方，原封不动贴入以下片段：

```html
<!-- AI 客服全区可拖拽组件 -->
<movable-area v-if="!csOpen" class="cs-movable-area">
  <movable-view 
    class="cs-float-ball" 
    direction="all" 
    :x="csPosX" 
    :y="csPosY" 
    @change="onCsMove"
    @touchstart="resetCsActive"
  >
    <!-- 内部容器 (处理变形缩进使用) -->
    <view :class="['cs-ball-inner', !csActive ? 'is-inactive' : '', csSnapDirection ? 'snap-' + csSnapDirection : '']">
      <view class="cs-prompt" :class="{ 'fade-out': !csActive }" @tap="handleOpenCs">AI专属客服</view>
      <text class="cs-ball-icon" @tap="handleOpenCs">💬</text>
      <view v-if="csActive" class="cs-ball-ping"></view>
    </view>
  </movable-view>
</movable-area>

<!-- AI客服大窗口聊天蒙版 -->
<view v-if="csOpen" class="cs-mask" @tap.self="closeCs"></view>
<!-- AI客服大窗口聊天主体 -->
<view v-if="csOpen" class="cs-window">
  <view class="cs-header">
    <view class="cs-header-left">
      <text class="cs-bot-emoji">🤖</text>
      <view>
        <text class="cs-bot-name">{{ csAppName }}</text>
        <view class="cs-status-row">
          <view class="cs-status-dot"></view>
          <text class="cs-status-text">在线</text>
        </view>
      </view>
    </view>
    <view class="cs-header-right">
      <text class="cs-btn" @tap.stop="resetCs">🔄</text>
      <text class="cs-btn" @tap.stop="closeCs">✕</text>
    </view>
  </view>

  <!-- 聊天内容体 -->
  <scroll-view class="cs-body" scroll-y :scroll-into-view="csScrollId" scroll-with-animation>
    <view class="cs-msg-list">
      <view v-for="(msg, idx) in csMessages" :key="idx" :id="'cs-msg-' + idx" class="cs-msg-row" :class="msg.role">
        <text class="cs-avatar">{{ msg.role === 'assistant' ? '🤖' : '🧑' }}</text>
        <view class="cs-bubble" :style="msg.role === 'user' ? { backgroundColor: csTheme } : {}">
          <text class="cs-bubble-text" :style="msg.role === 'user' ? { color: '#fff' } : {}">{{ msg.content }}</text>
        </view>
      </view>
      <view v-if="csTyping" id="cs-typing" class="cs-msg-row assistant">
        <text class="cs-avatar">🤖</text>
        <view class="cs-bubble cs-typing">
          <view class="cs-dot"></view>
          <view class="cs-dot"></view>
          <view class="cs-dot"></view>
        </view>
      </view>
    </view>
  </scroll-view>

  <view class="cs-footer">
    <textarea v-model="csInput" class="cs-input" placeholder="说点什么..." :auto-height="true" :maxlength="500" :cursor-spacing="20" :show-confirm-bar="false" />
    <view class="cs-send" :style="{ backgroundColor: csInput.trim() && !csTyping ? csTheme : '#cbd5e1' }" @tap="csSend">
      <text class="cs-send-text">发送</text>
    </view>
  </view>
</view>
```

在页面的 `<style lang="scss">` 或 CSS 里追加：

```css
/* ===============================
   ========= AI 客服专属样式 =========
   =============================== */

/* 1. 拖拽边界区 */
.cs-movable-area {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  width: 100%;
  height: 100%;
  pointer-events: none; /* 让被盖住的内容可点击 */
  z-index: 9999;
}

/* 2. 悬浮球壳子 */
.cs-float-ball {
  width: 110rpx;
  height: 110rpx;
  overflow: visible;
  pointer-events: auto; /* 恢复自身捕捉触摸权 */
}

/* 3. 悬浮球真身 (含贴边缩进逻辑) */
.cs-ball-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #7C3AED, #4e4376);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(124, 58, 237, 0.45);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.5s ease;
  position: relative;
}
.cs-ball-inner.is-inactive { opacity: 0.5; }
.cs-ball-inner.snap-left { transform: translateX(-40rpx); }
.cs-ball-inner.snap-right { transform: translateX(40rpx); }

/* 4. 紫色边框脉冲呼吸灯 */
.cs-ball-icon { font-size: 52rpx; z-index: 1; }
.cs-ball-ping {
  position: absolute;
  width: 100%; height: 100%;
  border-radius: 50%;
  border: 4rpx solid #7C3AED;
  opacity: 0;
  animation: csPing 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}
@keyframes csPing {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.8); opacity: 0; }
}

/* 5. 沉浸式气泡引导语 */
.cs-prompt {
  position: absolute;
  right: 124rpx;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(124, 58, 237, 0.9);
  color: #fff;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx 4rpx 30rpx 30rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(124, 58, 237, 0.3);
  animation: promptBreathe 3s infinite ease-in-out;
  pointer-events: auto;
  transition: opacity 0.5s ease;
}
.cs-prompt.fade-out { opacity: 0; pointer-events: none; }
@keyframes promptBreathe {
  0%, 100% { transform: translateY(-50%) scale(1); }
  50% { transform: translateY(-50%) scale(1.05); }
}

/* 6. 大聊天窗口界面 */
.cs-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
}
.cs-window {
  position: fixed;
  right: 30rpx;
  bottom: 200rpx;
  width: 640rpx;
  height: 860rpx;
  background: #f8f9fb;
  border-radius: 32rpx;
  box-shadow: 0 20rpx 80rpx rgba(0,0,0,0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1001;
  animation: csWindowIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: bottom right;
}
@keyframes csWindowIn {
  from { transform: scale(0.1); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

/* 7. 头部区域 */
.cs-header {
  padding: 24rpx 32rpx;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2rpx solid #edf2f7;
}
.cs-header-left { display: flex; align-items: center; gap: 16rpx; }
.cs-bot-emoji { font-size: 56rpx; background: #f1f5f9; padding: 12rpx; border-radius: 16rpx; }
.cs-bot-name { font-size: 32rpx; font-weight: 600; color: #1e293b; display: block; margin-bottom: 4rpx; }
.cs-status-row { display: flex; align-items: center; gap: 8rpx; }
.cs-status-dot { width: 12rpx; height: 12rpx; background: #10b981; border-radius: 50%; }
.cs-status-text { font-size: 24rpx; color: #64748b; }
.cs-header-right { display: flex; gap: 24rpx; }
.cs-btn { font-size: 36rpx; color: #94a3b8; }

/* 8. 聊天滚动区 */
.cs-body { flex: 1; padding: 32rpx; overflow-y: auto; background: #f8f9fb; }
.cs-msg-list { display: flex; flex-direction: column; gap: 32rpx; padding-bottom: 20rpx; }
.cs-msg-row { display: flex; gap: 16rpx; max-width: 85%; }
.cs-msg-row.user { align-self: flex-end; flex-direction: row-reverse; }
.cs-msg-row.assistant { align-self: flex-start; }
.cs-avatar { font-size: 48rpx; margin-top: 4rpx; }
.cs-bubble {
  padding: 20rpx 28rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
  word-break: break-all;
}
.cs-msg-row.user .cs-bubble { border-radius: 24rpx 4rpx 24rpx 24rpx; }
.cs-msg-row.assistant .cs-bubble { border-radius: 4rpx 24rpx 24rpx 24rpx; }
.cs-bubble-text { font-size: 28rpx; color: #334155; line-height: 1.5; }

/* 9. 打字中动画 */
.cs-typing { display: flex; align-items: center; gap: 8rpx; height: 48rpx; padding: 0 24rpx; }
.cs-dot { width: 10rpx; height: 10rpx; background: #94a3b8; border-radius: 50%; animation: typing 1.4s infinite ease-in-out both; }
.cs-dot:nth-child(1) { animation-delay: -0.32s; }
.cs-dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 10. 底部发送区 */
.cs-footer {
  padding: 24rpx 32rpx;
  background: #fff;
  border-top: 2rpx solid #edf2f7;
  display: flex;
  gap: 20rpx;
  align-items: flex-end;
}
.cs-input {
  flex: 1;
  background: #f1f5f9;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  min-height: 48rpx;
  max-height: 160rpx;
  color: #1e293b;
}
.cs-send {
  padding: 0 32rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  background: #cbd5e1;
}
.cs-send-text { color: #fff; font-size: 28rpx; font-weight: 500; }
```

**接入完毕之后：**
1. 编译您的项目。
2. 机器人默认将停留在底部的黄金左侧（拖动不影响）。
3. 如果未进行交互超过 3 秒，球体会自动执行沉浸式的缩进并透明动画，最大化让出你的版面空间！
