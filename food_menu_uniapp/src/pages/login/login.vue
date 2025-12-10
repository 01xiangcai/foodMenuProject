<template>
  <view class="page" :style="{ backgroundColor: themeConfig.bgPrimary }">
    <!-- 主题切换按钮 -->
    <view class="theme-switch" @tap="switchTheme">
      <text class="theme-icon">🎨</text>
      <text class="theme-text">{{ themeConfig.name }}</text>
    </view>

    <!-- 登录弹窗 -->
    <view class="login-modal">
      <view class="modal-content glass-card" :style="{ background: themeConfig.cardBg, border: `1px solid ${themeConfig.cardBorder}` }">
        <view class="modal-header" :style="{ borderBottom: `1px solid ${themeConfig.borderColor}` }">
          <text class="title" :style="{ color: themeConfig.textPrimary }">登录</text>
          <view class="close-btn" @tap="goBack">
            <text :style="{ color: themeConfig.textSecondary }">✕</text>
          </view>
        </view>

        <view class="form">
          <view class="form-item">
            <text class="label" :style="{ color: themeConfig.textPrimary }">用户名/手机号</text>
            <input 
              class="input" 
              v-model="username"
              placeholder="请输入用户名或手机号"
              placeholder-class="placeholder"
              :style="{ background: themeConfig.inputBg, border: `1px solid ${themeConfig.inputBorder}`, color: themeConfig.textPrimary }"
            />
          </view>

          <view class="form-item">
            <text class="label" :style="{ color: themeConfig.textPrimary }">密码</text>
            <view class="password-input-wrapper">
              <input 
                class="input password-input" 
                v-model="password"
                :password="!showPassword"
                placeholder="请输入密码"
                placeholder-class="placeholder"
                :style="{ background: themeConfig.inputBg, border: `1px solid ${themeConfig.inputBorder}`, color: themeConfig.textPrimary }"
              />
              <view class="eye-icon" @tap="togglePasswordVisibility">
                <text v-if="showPassword">🙉</text>
                <text v-else>🙈</text>
              </view>
            </view>
          </view>

          <view class="login-btn" :style="{ background: themeConfig.primaryGradient }" @tap="handleLogin">
            <text>登录</text>
          </view>

          <view class="footer">
            <text class="link" :style="{ color: themeConfig.primaryColor }" @tap="goToRegister">没有账号？去注册</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { wxLogin } from '@/api/index'
import { useTheme } from '@/stores/theme'

// 使用主题
const { themeConfig, toggleTheme, loadTheme } = useTheme()

const username = ref('')
const password = ref('')
const showPassword = ref(false)

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
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

const goToRegister = () => {
  uni.navigateTo({
    url: '/pages/register/register'
  })
}

onMounted(() => {
  loadTheme()
})

const handleLogin = async () => {
  if (!username.value) {
    uni.showToast({
      title: '请输入用户名或手机号',
      icon: 'none'
    })
    return
  }

  if (!password.value) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    })
    return
  }

  try {
    const res = await wxLogin({
      username: username.value,
      password: password.value,
      type: 1 // 用户名/手机号+密码登录
    })

    console.log('登录响应:', res)

    // 兼容多种响应格式
    let token = null
    if (res && res.data) {
      // 格式1: { data: { token: 'xxx' } }
      if (res.data.token) {
        token = res.data.token
      }
      // 格式2: { data: 'token_string' }
      else if (typeof res.data === 'string') {
        token = res.data
      }
    }

    if (token) {
      // 存储token
      uni.setStorageSync('fm_token', token)
      console.log('Token已存储:', token.substring(0, 20) + '...')
      
      // 验证token是否存储成功
      const storedToken = uni.getStorageSync('fm_token')
      if (!storedToken) {
        uni.showToast({
          title: 'Token存储失败',
          icon: 'none'
        })
        return
      }
      
      uni.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1500
      })
      
      // 延迟跳转，确保token已存储
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/profile/profile'
        })
      }, 1500)
    } else {
      console.error('未获取到token，响应数据:', res)
      uni.showToast({
        title: '登录失败，未获取到token',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('登录失败:', error)
    uni.showToast({
      title: error.message || '登录失败，请检查用户名和密码',
      icon: 'none',
      duration: 2000
    })
  }
}
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

.login-modal {
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

.login-btn {
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
