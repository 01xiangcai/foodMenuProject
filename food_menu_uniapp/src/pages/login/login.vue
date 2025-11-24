<template>
  <view class="page">
    <view class="login-container">
      <!-- Logo -->
      <view class="logo-section">
        <text class="logo-icon">🍽️</text>
        <text class="app-name">家宴菜单</text>
        <text class="app-desc">美食就在指尖</text>
      </view>

      <!-- 登录表单 -->
      <view class="form-section glass-card">
        <view class="form-item">
          <text class="label">手机号</text>
          <input 
            class="input" 
            type="number" 
            maxlength="11"
            placeholder="请输入手机号" 
            v-model="phone"
          />
        </view>

        <view class="form-item">
          <text class="label">验证码</text>
          <view class="code-input">
            <input 
              class="input" 
              type="number" 
              maxlength="6"
              placeholder="请输入验证码" 
              v-model="code"
            />
            <view 
              class="btn-code" 
              :class="{ disabled: countdown > 0 }"
              @tap="sendCode"
            >
              <text>{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</text>
            </view>
          </view>
        </view>

        <view class="btn-login" @tap="login">
          <text>登录</text>
        </view>

        <view class="tips">
          <text>登录即表示同意</text>
          <text class="link">《用户协议》</text>
          <text>和</text>
          <text class="link">《隐私政策》</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { login as apiLogin, sendCode as apiSendCode } from '@/api/index'

const phone = ref('')
const code = ref('')
const countdown = ref(0)
let timer = null

// 发送验证码
const sendCode = async () => {
  if (countdown.value > 0) return
  
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }
  
  try {
    await apiSendCode(phone.value)
    uni.showToast({
      title: '验证码已发送',
      icon: 'success'
    })
    
    // 开始倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    // 测试环境直接显示验证码
    uni.showToast({
      title: '测试验证码: 1234',
      icon: 'none',
      duration: 3000
    })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  }
}

// 登录
const login = async () => {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }
  
  if (!code.value) {
    uni.showToast({
      title: '请输入验证码',
      icon: 'none'
    })
    return
  }
  
  try {
    const res = await apiLogin({
      phone: phone.value,
      code: code.value
    })
    
    if (res.data && res.data.token) {
      uni.setStorageSync('fm_token', res.data.token)
      uni.showToast({
        title: '登录成功',
        icon: 'success'
      })
      
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/index/index'
        })
      }, 1500)
    }
  } catch (error) {
    console.error('登录失败:', error)
    uni.showToast({
      title: '登录失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-container {
  width: 100%;
  max-width: 600rpx;
}

.logo-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.logo-icon {
  display: block;
  font-size: 120rpx;
  margin-bottom: 20rpx;
}

.app-name {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10rpx;
}

.app-desc {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-section {
  padding: 60rpx 40rpx;
}

.form-item {
  margin-bottom: 40rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #fff;
  margin-bottom: 16rpx;
}

.input {
  width: 100%;
  height: 88rpx;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  color: #fff;
}

.code-input {
  display: flex;
  gap: 20rpx;
  
  .input {
    flex: 1;
  }
}

.btn-code {
  flex-shrink: 0;
  padding: 0 30rpx;
  height: 88rpx;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.disabled {
    opacity: 0.5;
  }
  
  text {
    font-size: 24rpx;
    color: #fff;
    white-space: nowrap;
  }
}

.btn-login {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.9) 100%);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 60rpx;
  
  text {
    font-size: 32rpx;
    font-weight: 600;
    color: #667eea;
  }
}

.tips {
  text-align: center;
  margin-top: 40rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
  
  .link {
    color: #fff;
  }
}
</style>
