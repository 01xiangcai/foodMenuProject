<template>
  <view class="comment-input-wrapper">
    <!-- 遮罩层 -->
    <view 
      v-if="visible" 
      class="mask" 
      @tap="handleClose"
    />
    
    <!-- 输入框容器 -->
    <view 
      class="input-container" 
      :class="{ 'show': visible }"
      :style="{ paddingBottom: containerPaddingBottom }"
    >
      <!-- 回复提示 -->
      <view v-if="replyTo" class="reply-hint">
        <text>回复 {{ replyTo.replyToName || replyTo.authorName }}：</text>
        <view class="close-btn" @tap="cancelReply">
          <text>✕</text>
        </view>
      </view>
      
      <!-- 输入区域 -->
      <view class="input-area">
        <input 
          v-model="content"
          class="input-field"
          type="text"
          :placeholder="placeholder"
          :focus="isInputFocus"
          :maxlength="maxLength"
          :adjust-position="false"
          @confirm="handleSubmit"
          @focus="handleInputFocus"
          @blur="handleInputBlur"
        />
        
        <!-- 表情按钮 -->
        <view class="emoji-btn" @tap="toggleEmojiPanel">
          <text class="emoji-icon">{{ showEmojiPanel ? '⌨️' : '😀' }}</text>
        </view>
        
        <view class="char-count">
          <text>{{ content.length }}/{{ maxLength }}</text>
        </view>
        <view 
          class="send-btn" 
          :class="{ 'active': canSubmit }"
          @tap="handleSubmit"
        >
          <text>发送</text>
        </view>
      </view>
      
      <!-- 表情面板 -->
      <view class="emoji-panel" v-if="showEmojiPanel" :style="{ height: panelHeight + 'px' }">
        <!-- 标签页切换 -->
        <view class="panel-tabs">
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 'emoji' }"
            @tap="activeTab = 'emoji'"
          >
            <text>Emoji</text>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 'sticker' }"
            @tap="activeTab = 'sticker'"
          >
            <text>表情包</text>
          </view>
        </view>
        
        <!-- Emoji 列表 -->
        <scroll-view 
          v-if="activeTab === 'emoji'" 
          scroll-y 
          class="emoji-list"
        >
          <view class="emoji-grid">
            <view 
              v-for="(emoji, index) in emojiList" 
              :key="index"
              class="emoji-item"
              @tap="insertEmoji(emoji)"
            >
              <text>{{ emoji }}</text>
            </view>
          </view>
        </scroll-view>
        
        <!-- 表情包列表 -->
        <scroll-view 
          v-if="activeTab === 'sticker'" 
          scroll-y 
          class="sticker-list"
        >
          <view class="sticker-grid">
            <view 
              v-for="sticker in stickerList" 
              :key="sticker.id"
              class="sticker-item"
              @tap="insertSticker(sticker)"
            >
              <image :src="sticker.url" mode="aspectFit" class="sticker-img" />
              <text class="sticker-name">{{ sticker.name }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
      
      <!-- 键盘占位 -->
      <view v-else :style="{ height: keyboardHeight + 'px' }"></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useTheme } from '@/stores/theme'
import { EMOJI_LIST, STICKER_LIST } from '@/utils/emoji-data'

const { themeConfig } = useTheme()

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  replyTo: {
    type: Object,
    default: null
  },
  maxLength: {
    type: Number,
    default: 200
  }
})

const emit = defineEmits(['update:visible', 'submit', 'close'])

const content = ref('')
const isInputFocus = ref(false)
const showEmojiPanel = ref(false)
const activeTab = ref('emoji') // 'emoji' | 'sticker'
const keyboardHeight = ref(0)
const panelHeight = ref(260) // 默认面板高度

const emojiList = EMOJI_LIST
const stickerList = STICKER_LIST

const placeholder = computed(() => {
  return '写下你的评论...'
})

const canSubmit = computed(() => {
  return content.value.trim().length > 0
})

const containerPaddingBottom = computed(() => {
  // 如果显示面板或键盘弹起，padding-bottom 不需要 safe-area
  // 否则需要 safe-area
  if (showEmojiPanel.value || keyboardHeight.value > 0) {
    return '0'
  }
  return 'calc(20rpx + env(safe-area-inset-bottom))'
})

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 打开时默认聚焦
    // 稍微延迟确保组件渲染
    setTimeout(() => {
      isInputFocus.value = true
    }, 100)
  } else {
    // 关闭时重置状态
    content.value = ''
    showEmojiPanel.value = false
    keyboardHeight.value = 0
    isInputFocus.value = false
  }
})

// 监听键盘高度变化
const onKeyboardHeightChange = (res) => {
  const height = res.height
  if (height > 0) {
    keyboardHeight.value = height
    // 键盘弹起时，隐藏表情面板
    showEmojiPanel.value = false
    // 更新面板高度以匹配键盘高度 (记录一次即可，或者每次更新)
    if (height > 200) { // 过滤掉异常高度
      panelHeight.value = height
    }
  } else {
    keyboardHeight.value = 0
    // 键盘收起。如果是点击了表情按钮导致的收起，showEmojiPanel 会被设为 true
    // 如果是点击空白区域收起，不做特殊处理
  }
}

onMounted(() => {
  uni.onKeyboardHeightChange(onKeyboardHeightChange)
})

onUnmounted(() => {
  uni.offKeyboardHeightChange(onKeyboardHeightChange)
})

const handleInputFocus = () => {
  isInputFocus.value = true
  showEmojiPanel.value = false
}

const handleInputBlur = () => {
  isInputFocus.value = false
}

const toggleEmojiPanel = () => {
  if (showEmojiPanel.value) {
    // 切回键盘
    showEmojiPanel.value = false
    isInputFocus.value = true
  } else {
    // 显示表情面板
    showEmojiPanel.value = true
    isInputFocus.value = false
    uni.hideKeyboard()
  }
}

const insertEmoji = (emoji) => {
  content.value += emoji
}

const insertSticker = (sticker) => {
  content.value += sticker.code
}

const handleSubmit = () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请输入评论内容',
      icon: 'none'
    })
    return
  }
  
  emit('submit', {
    content: content.value.trim(),
    parentId: props.replyTo?.actualParentId || props.replyTo?.id || null,
    replyToName: props.replyTo?.replyToName || props.replyTo?.authorName || null
  })
  
  content.value = ''
  // 发送后关闭面板
  showEmojiPanel.value = false
  emit('update:visible', false)
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const cancelReply = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
.comment-input-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  pointer-events: none;
  
  &:has(.show) {
    pointer-events: auto;
  }
}

.mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  opacity: 0;
  animation: fadeIn 0.3s ease forwards;
}

@keyframes fadeIn {
  to {
    opacity: 1;
  }
}

.input-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: v-bind('themeConfig.cardBg');
  border-top: 1px solid v-bind('themeConfig.borderColor');
  padding: 20rpx 24rpx;
  // padding-bottom 动态控制
  transform: translateY(100%);
  transition: transform 0.3s ease;
  display: flex;
  flex-direction: column;
  
  &.show {
    transform: translateY(0);
  }
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 20rpx;
  background: v-bind('themeConfig.bgSecondary');
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  
  text {
    font-size: 26rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  .close-btn {
    width: 40rpx;
    height: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    text {
      font-size: 32rpx;
      color: v-bind('themeConfig.textSecondary');
    }
    
    &:active {
      opacity: 0.6;
    }
  }
}

.input-area {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  border-radius: 48rpx;
  padding: 16rpx 24rpx;
}

.input-field {
  flex: 1;
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  min-height: 40rpx;
}

.emoji-btn {
  padding: 0 8rpx;
  
  .emoji-icon {
    font-size: 40rpx;
  }
  
  &:active {
    opacity: 0.7;
  }
}

.char-count {
  text {
    font-size: 22rpx;
    color: v-bind('themeConfig.textSecondary');
  }
}

.send-btn {
  padding: 12rpx 28rpx;
  background: v-bind('themeConfig.bgSecondary');
  border-radius: 32rpx;
  transition: all 0.3s ease;
  
  text {
    font-size: 26rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  &.active {
    background: v-bind('themeConfig.primaryGradient');
    
    text {
      color: #fff;
      font-weight: 600;
    }
  }
  
  &:active {
    transform: scale(0.95);
  }
}

/* 表情面板样式 */
.emoji-panel {
  width: 100%;
  background: v-bind('themeConfig.cardBg');
  border-top: 1px solid v-bind('themeConfig.borderColor');
  display: flex;
  flex-direction: column;
}

.panel-tabs {
  display: flex;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  
  .tab-item {
    flex: 1;
    padding: 20rpx;
    text-align: center;
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
    
    &.active {
      color: v-bind('themeConfig.primaryColor');
      font-weight: 500;
      position: relative;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: v-bind('themeConfig.primaryColor');
        border-radius: 2rpx;
      }
    }
  }
}

.emoji-list {
  flex: 1;
  height: 0; // 让flex生效
  padding: 20rpx;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 20rpx;
}

.emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  padding: 8rpx;
}

.sticker-list {
  flex: 1;
  height: 0;
  padding: 20rpx;
}

.sticker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.sticker-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  
  &:active {
    background: v-bind('themeConfig.bgSecondary');
  }
  
  .sticker-img {
    width: 100rpx;
    height: 100rpx;
  }
  
  .sticker-name {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
  }
}
</style>
