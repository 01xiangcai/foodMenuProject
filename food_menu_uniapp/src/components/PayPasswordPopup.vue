<template>
  <view class="pay-password-popup">
    <!-- 遮罩层 -->
    <view 
      class="popup-mask" 
      v-if="visible" 
      @tap="handleClose"
    ></view>
    
    <!-- 弹窗内容 -->
    <view class="popup-content" :class="{ show: visible }">
      <view class="popup-header">
        <text class="title">{{ title }}</text>
        <view class="close-btn" @tap="handleClose">
          <text>✕</text>
        </view>
      </view>
      
      <view class="popup-body">
        <text class="tip">请输入6位支付密码</text>
        
        <!-- 密码显示 -->
        <view class="password-display">
          <view 
            v-for="i in 6" 
            :key="i" 
            class="password-dot"
            :class="{ filled: password.length >= i }"
          ></view>
        </view>
        
        <!-- 错误提示 -->
        <text v-if="errorMsg" class="error-msg">{{ errorMsg }}</text>
        
        <!-- 数字键盘 -->
        <view class="keyboard">
          <view class="keyboard-row" v-for="(row, index) in keyboardRows" :key="index">
            <view 
              v-for="key in row" 
              :key="key.value" 
              class="key"
              :class="key.class"
              @tap="handleKeyPress(key.value)"
            >
              <text v-if="key.value === 'del'">⌫</text>
              <text v-else-if="key.value === ''"></text>
              <text v-else>{{ key.value }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue'
import { useTheme } from '@/stores/theme'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '请输入支付密码'
  }
})

const emit = defineEmits(['update:visible', 'submit', 'close'])

const { themeConfig } = useTheme()

const password = ref('')
const errorMsg = ref('')

// 键盘布局
const keyboardRows = [
  [{ value: '1' }, { value: '2' }, { value: '3' }],
  [{ value: '4' }, { value: '5' }, { value: '6' }],
  [{ value: '7' }, { value: '8' }, { value: '9' }],
  [{ value: '', class: 'empty' }, { value: '0' }, { value: 'del', class: 'del' }]
]

// 监听visible变化，重置状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    password.value = ''
    errorMsg.value = ''
  }
})

// 处理按键
const handleKeyPress = (key) => {
  if (key === '') return
  
  errorMsg.value = ''
  
  if (key === 'del') {
    password.value = password.value.slice(0, -1)
    return
  }
  
  if (password.value.length >= 6) return
  
  password.value += key
  
  // 输入完成6位后自动提交
  if (password.value.length === 6) {
    emit('submit', password.value)
  }
}

// 设置错误消息（供父组件调用）
const setError = (msg) => {
  errorMsg.value = msg
  password.value = ''
}

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 暴露方法给父组件
defineExpose({
  setError,
  reset: () => {
    password.value = ''
    errorMsg.value = ''
  }
})
</script>

<style lang="scss" scoped>
.pay-password-popup {
  .popup-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(4px);
    z-index: 1000;
    animation: fadeIn 0.2s ease;
  }
  
  .popup-content {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1001;
    background: v-bind('themeConfig.cardBg');
    backdrop-filter: blur(20px);
    border: 1px solid v-bind('themeConfig.cardBorder');
    border-radius: 32rpx 32rpx 0 0;
    transform: translateY(100%);
    transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    
    &.show {
      transform: translateY(0);
    }
  }
  
  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 30rpx 20rpx;
    border-bottom: 1px solid v-bind('themeConfig.borderColor');
    
    .title {
      font-size: 32rpx;
      font-weight: 600;
      color: v-bind('themeConfig.textPrimary');
    }
    
    .close-btn {
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      
      text {
        font-size: 28rpx;
        color: v-bind('themeConfig.textSecondary');
      }
    }
  }
  
  .popup-body {
    padding: 30rpx;
    
    .tip {
      display: block;
      text-align: center;
      font-size: 26rpx;
      color: v-bind('themeConfig.textSecondary');
      margin-bottom: 30rpx;
    }
    
    .password-display {
      display: flex;
      justify-content: center;
      gap: 20rpx;
      margin-bottom: 20rpx;
      
      .password-dot {
        width: 56rpx;
        height: 56rpx;
        border: 2px solid v-bind('themeConfig.borderColor');
        border-radius: 12rpx;
        background: v-bind('themeConfig.inputBg');
        transition: all 0.2s ease;
        
        &.filled {
          background: v-bind('themeConfig.primaryColor');
          border-color: v-bind('themeConfig.primaryColor');
          box-shadow: 0 0 10rpx v-bind('themeConfig.primaryColor + "40"');
        }
      }
    }
    
    .error-msg {
      display: block;
      text-align: center;
      font-size: 24rpx;
      color: #f43f5e;
      margin-bottom: 20rpx;
    }
    
    .keyboard {
      padding: 10rpx 0 env(safe-area-inset-bottom);
      
      .keyboard-row {
        display: flex;
        justify-content: center;
        gap: 16rpx;
        margin-bottom: 16rpx;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
      
      .key {
        flex: 1;
        max-width: 200rpx;
        height: 100rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: v-bind('themeConfig.inputBg');
        border-radius: 16rpx;
        transition: all 0.15s ease;
        
        &:active {
          transform: scale(0.95);
          background: v-bind('themeConfig.borderColor');
        }
        
        &.empty {
          background: transparent;
          pointer-events: none;
        }
        
        &.del {
          text {
            font-size: 36rpx;
          }
        }
        
        text {
          font-size: 44rpx;
          font-weight: 500;
          color: v-bind('themeConfig.textPrimary');
        }
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
