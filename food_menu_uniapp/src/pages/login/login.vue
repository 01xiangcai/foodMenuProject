<template>
  <view class="page">
    <!-- 登录弹窗 -->
    <view class="login-modal">
      <view class="modal-content glass-card">
        <view class="modal-header">
          <text class="title">登录</text>
          <view class="close-btn" @tap="goBack">
            <text>✕</text>
          </view>
        </view>

        <view class="form">
          <view class="form-item">
            <text class="label">用户名</text>
            <input 
              class="input" 
              v-model="username"
              placeholder="请输入用户名"
              placeholder-class="placeholder"
            />
          </view>

          <view class="form-item">
            <text class="label">密码</text>
            <input 
              class="input" 
              v-model="password"
              type="password"
              placeholder="请输入密码"
              placeholder-class="placeholder"
            />
          </view>

          <view class="login-btn" @tap="handleLogin">
            <text>登录</text>
          </view>

          <view class="footer">
            <text class="link">没有账号？去注册</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { login } from '@/api/index'

const username = ref('')
const password = ref('')

const goBack = () => {
  uni.navigateBack()
}

const handleLogin = async () => {
  if (!username.value) {
    uni.showToast({
      title: '请输入用户名',
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
    const res = await login({
      username: username.value,
      password: password.value
    })

    console.log('登录响应:', res)

    // 兼容多种响应格式
    let token = null
    if (res.data) {
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
      uni.setStorageSync('fm_token', token)
      uni.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1500
      })
      
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/profile/profile'
        })
      }, 1500)
    } else {
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
  background-color: rgba(5, 10, 31, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-modal {
  width: 100%;
  max-width: 600rpx;
}

.modal-content {
  padding: 0;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 40rpx;
    color: #8b8fa3;
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
  color: #fff;
  margin-bottom: 16rpx;
}

.input {
  width: 100%;
  height: 90rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #fff;
}

.placeholder {
  color: #8b8fa3;
}

.login-btn {
  margin-top: 40rpx;
  height: 90rpx;
  background: linear-gradient(135deg, #14b8ff 0%, #a855f7 100%);
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
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
  color: #14b8ff;
}
</style>
