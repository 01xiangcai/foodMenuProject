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
          :focus="visible"
          :maxlength="maxLength"
          @confirm="handleSubmit"
        />
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
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useTheme } from '@/stores/theme'

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

const placeholder = computed(() => {
  return props.replyTo 
    ? `回复 ${props.replyTo.replyToName || props.replyTo.authorName}` 
    : '写下你的评论...'
})

const canSubmit = computed(() => {
  return content.value.trim().length > 0
})

// 监听visible变化，重置内容
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    content.value = ''
  }
})

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
    // 使用actualParentId（如果存在）或者id
    parentId: props.replyTo?.actualParentId || props.replyTo?.id || null,
    // 传递被回复人的名字
    replyToName: props.replyTo?.replyToName || props.replyTo?.authorName || null
  })
  
  content.value = ''
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
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  transform: translateY(100%);
  transition: transform 0.3s ease;
  
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
</style>
