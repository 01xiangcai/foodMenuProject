<template>
  <view class="page" :style="{ backgroundColor: themeConfig.bgPrimary }">
    <!-- 主题切换按钮 -->
    <view class="theme-switch" @tap="switchTheme">
      <text class="theme-icon">🎨</text>
      <text class="theme-text">{{ themeConfig.name }}</text>
    </view>

    <!-- 注册弹窗 -->
    <view class="register-modal">
      <view class="modal-content glass-card" :style="{ background: themeConfig.cardBg, border: `1px solid ${themeConfig.cardBorder}` }">
        <view class="modal-header" :style="{ borderBottom: `1px solid ${themeConfig.borderColor}` }">
          <text class="title" :style="{ color: themeConfig.textPrimary }">注册</text>
          <view class="close-btn" @tap="goBack">
            <text :style="{ color: themeConfig.textSecondary }">✕</text>
          </view>
        </view>

        <view class="form">
          <view class="form-item">
            <text class="label" :style="{ color: themeConfig.textPrimary }">用户名</text>
            <input 
              class="input" 
              v-model="formData.username"
              placeholder="请输入用户名"
              placeholder-class="placeholder"
              :style="{ background: themeConfig.inputBg, border: `1px solid ${themeConfig.inputBorder}`, color: themeConfig.textPrimary }"
            />
          </view>

          <view class="form-item">
            <text class="label" :style="{ color: themeConfig.textPrimary }">昵称</text>
            <input 
              class="input" 
              v-model="formData.nickname"
              placeholder="请输入昵称（可选）"
              placeholder-class="placeholder"
              :style="{ background: themeConfig.inputBg, border: `1px solid ${themeConfig.inputBorder}`, color: themeConfig.textPrimary }"
            />
          </view>

          <view class="form-item">
            <text class="label" :style="{ color: themeConfig.textPrimary }">手机号</text>
            <input 
              class="input" 
              v-model="formData.phone"
              type="number"
              placeholder="请输入手机号（可选）"
              placeholder-class="placeholder"
              :style="{ background: themeConfig.inputBg, border: `1px solid ${themeConfig.inputBorder}`, color: themeConfig.textPrimary }"
            />
          </view>

          <view class="form-item">
            <text class="label" :style="{ color: themeConfig.textPrimary }">密码</text>
            <view class="password-input-wrapper">
              <input 
                class="input password-input" 
                v-model="formData.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                placeholder-class="placeholder"
                :style="{ background: themeConfig.inputBg, border: `1px solid ${themeConfig.inputBorder}`, color: themeConfig.textPrimary }"
              />
              <view class="eye-icon" @tap="togglePasswordVisibility">
                <text>{{ showPassword ? '🙉' : '🙈' }}</text>
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="label" :style="{ color: themeConfig.textPrimary }">确认密码</text>
            <view class="password-input-wrapper">
              <input 
                class="input password-input" 
                v-model="formData.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                placeholder-class="placeholder"
                :style="{ background: themeConfig.inputBg, border: `1px solid ${themeConfig.inputBorder}`, color: themeConfig.textPrimary }"
              />
              <view class="eye-icon" @tap="toggleConfirmPasswordVisibility">
                <text>{{ showConfirmPassword ? '🙉' : '🙈' }}</text>
              </view>
            </view>
          </view>

          <view class="register-btn" :style="{ background: themeConfig.primaryGradient }" @tap="handleRegister">
            <text>注册</text>
          </view>

          <view class="footer">
            <text class="link" :style="{ color: themeConfig.primaryColor }" @tap="goToLogin">已有账号？去登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { wxRegister } from '@/api/index'
import { useTheme } from '@/stores/theme'

// 使用主题
const { themeConfig, toggleTheme, loadTheme } = useTheme()

const formData = ref({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

const goBack = () => {
  uni.navigateBack()
}

const switchTheme = () => {
  toggleTheme()
  uni.showToast({
    title: `已切换到${themeConfig.value.name}主题`,
    icon: 'none',
    duration: 1500
  })
}

const goToLogin = () => {
  uni.navigateBack()
}

const handleRegister = async () => {
  // 验证用户名
  if (!formData.value.username || formData.value.username.trim() === '') {
    uni.showToast({
      title: '请输入用户名',
      icon: 'none'
    })
    return
  }

  // 验证密码
  if (!formData.value.password || formData.value.password.trim() === '') {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    })
    return
  }

  // 验证密码长度
  if (formData.value.password.length < 6) {
    uni.showToast({
      title: '密码长度至少6位',
      icon: 'none'
    })
    return
  }

  // 验证确认密码
  if (formData.value.password !== formData.value.confirmPassword) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    })
    return
  }

  // 验证手机号（如果填写了）
  if (formData.value.phone && formData.value.phone.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  try {
    const res = await wxRegister({
      username: formData.value.username.trim(),
      password: formData.value.password,
      nickname: formData.value.nickname.trim() || undefined,
      phone: formData.value.phone.trim() || undefined
    })

    uni.showToast({
      title: '注册成功',
      icon: 'success',
      duration: 1500
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('注册失败:', error)
    
    // 检查是否是注册功能关闭的错误
    const errorMsg = error.message || '注册失败，请重试'
    if (errorMsg.includes('注册功能已关闭') || errorMsg.includes('已关闭')) {
      uni.showModal({
        title: '提示',
        content: '用户注册功能已关闭，请联系管理员开通账号',
        showCancel: false,
        confirmText: '知道了',
        success: () => {
          uni.navigateBack()
        }
      })
    } else {
      uni.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000
      })
    }
  }
}

onMounted(() => {
  loadTheme()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  position: relative;
}

.theme-switch {
  position: absolute;
  top: 40rpx;
  right: 40rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: v-bind('themeConfig.cardBg');
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 40rpx;
  z-index: 10;
  cursor: pointer;
  
  .theme-icon {
    font-size: 28rpx;
  }
  
  .theme-text {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
  }
}

.register-modal {
  width: 100%;
  max-width: 600rpx;
}

.modal-content {
  padding: 0;
  overflow: hidden;
  border-radius: 24rpx;
  box-shadow: v-bind('themeConfig.shadowMedium');
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  text {
    font-size: 40rpx;
  }
}

.form {
  padding: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  margin-bottom: 16rpx;
}

.input {
  width: 100%;
  height: 90rpx;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.password-input-wrapper {
  position: relative;
  width: 100%;
}

.password-input {
  padding-right: 80rpx;
}

.eye-icon {
  position: absolute;
  right: 0;
  top: 0;
  height: 90rpx;
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  cursor: pointer;
  
  text {
    font-size: 36rpx;
  }
}

.placeholder {
  color: v-bind('themeConfig.textSecondary');
}

.register-btn {
  margin-top: 40rpx;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: v-bind('themeConfig.shadowLight');
  cursor: pointer;
  
  text {
    font-size: 32rpx;
    color: #fff;
    font-weight: 600;
  }
}

.footer {
  margin-top: 30rpx;
  text-align: center;
}

.link {
  font-size: 26rpx;
  cursor: pointer;
}
</style>

