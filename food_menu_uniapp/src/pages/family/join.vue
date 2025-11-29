<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header glass-card">
      <view class="header-content">
        <text class="title">🏠 加入家庭</text>
        <text class="subtitle">输入邀请码加入您的家庭</text>
      </view>
    </view>

    <!-- 邀请码输入卡片 -->
    <view class="input-card glass-card">
      <view class="card-header">
        <text class="card-title">🎫 邀请码</text>
      </view>
      
      <view class="input-wrapper">
        <input
          class="code-input"
          v-model="inviteCode"
          placeholder="请输入邀请码"
          maxlength="10"
          :adjust-position="true"
          @input="handleInput"
        />
        <view class="input-length">{{ inviteCode.length }}/10</view>
      </view>
      
      <view class="tips">
        <text class="tip-icon">💡</text>
        <text class="tip-text">请向家庭管理员索要邀请码</text>
      </view>
    </view>

    <!-- 当前家庭信息 -->
    <view v-if="currentFamily" class="family-card glass-card">
      <view class="card-header">
        <text class="card-title">📋 当前家庭</text>
      </view>
      
      <view class="family-info">
        <view class="family-avatar">{{ currentFamily.name.charAt(0) }}</view>
        <view class="family-details">
          <text class="family-name">{{ currentFamily.name }}</text>
          <text class="family-desc">{{ currentFamily.description || '暂无描述' }}</text>
          <view class="family-code">
            <text class="code-label">邀请码：</text>
            <text class="code-value">{{ currentFamily.inviteCode }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能说明 -->
    <view class="features-card glass-card">
      <view class="card-header">
        <text class="card-title">✨ 家庭功能</text>
      </view>
      
      <view class="feature-list">
        <view class="feature-item">
          <text class="feature-icon">🍽️</text>
          <view class="feature-content">
            <text class="feature-title">共享菜单</text>
            <text class="feature-desc">家庭成员共享菜品和订单</text>
          </view>
        </view>
        
        <view class="feature-item">
          <text class="feature-icon">👨‍👩‍👧‍👦</text>
          <view class="feature-content">
            <text class="feature-title">成员管理</text>
            <text class="feature-desc">管理员可以管理家庭成员</text>
          </view>
        </view>
        
        <view class="feature-item">
          <text class="feature-icon">🔒</text>
          <view class="feature-content">
            <text class="feature-title">数据隔离</text>
            <text class="feature-desc">不同家庭数据完全隔离</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加入按钮 -->
    <view class="action-area">
      <button 
        class="join-btn"
        :class="{ disabled: !canJoin }"
        :disabled="!canJoin || loading"
        @tap="handleJoin"
      >
        <text v-if="loading">加入中...</text>
        <text v-else>{{ currentFamily ? '更换家庭' : '加入家庭' }}</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { joinFamily, getCurrentFamily } from '@/api/index'
import { useTheme } from '@/stores/theme'

const { themeConfig, loadTheme } = useTheme()

const inviteCode = ref('')
const loading = ref(false)
const currentFamily = ref(null)

const canJoin = computed(() => {
  return inviteCode.value.length >= 8
})

// 处理输入，转换为大写
const handleInput = (e) => {
  inviteCode.value = e.detail.value.toUpperCase()
}

// 加载当前家庭信息
const loadCurrentFamily = async () => {
  try {
    const res = await getCurrentFamily()
    if (res.data) {
      currentFamily.value = res.data
    }
  } catch (error) {
    console.error('获取家庭信息失败:', error)
  }
}

// 加入家庭
const handleJoin = async () => {
  if (!canJoin.value || loading.value) {
    return
  }
  
  // 确认提示
  const confirmText = currentFamily.value 
    ? '确定要更换家庭吗？更换后将无法访问原家庭的数据。' 
    : '确定要加入这个家庭吗？'
  
  uni.showModal({
    title: '提示',
    content: confirmText,
    success: async (modalRes) => {
      if (!modalRes.confirm) {
        return
      }
      
      loading.value = true
      
      try {
        const res = await joinFamily(inviteCode.value)
        
        if (res.code === 1) {
          // 更新Token
          if (res.data) {
            uni.setStorageSync('fm_token', res.data)
          }
          
          uni.showToast({
            title: '🎉 加入成功',
            icon: 'success',
            duration: 2000
          })
          
          // 清空输入
          inviteCode.value = ''
          
          // 重新加载家庭信息
          setTimeout(() => {
            loadCurrentFamily()
          }, 500)
          
          // 延迟返回
          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        } else {
          uni.showToast({
            title: res.msg || '加入失败',
            icon: 'none',
            duration: 2000
          })
        }
      } catch (error) {
        console.error('加入家庭失败:', error)
        uni.showToast({
          title: error.message || '加入失败，请检查邀请码',
          icon: 'none',
          duration: 2000
        })
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadTheme()
  loadCurrentFamily()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding: 20rpx;
  transition: background-color 0.3s ease;
}

.header {
  padding: 60rpx 40rpx;
  margin-bottom: 24rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 24rpx;
  text-align: center;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.title {
  font-size: 48rpx;
  font-weight: 700;
  background: v-bind('themeConfig.primaryGradient');
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
}

.input-card,
.family-card,
.features-card {
  padding: 40rpx;
  margin-bottom: 24rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 24rpx;
  transition: all 0.3s ease;
}

.card-header {
  margin-bottom: 32rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  transition: color 0.3s ease;
}

.input-wrapper {
  position: relative;
  margin-bottom: 24rpx;
}

.code-input {
  width: 100%;
  height: 100rpx;
  background: v-bind('themeConfig.inputBg');
  border: 2px solid v-bind('themeConfig.borderColor');
  border-radius: 16rpx;
  padding: 0 120rpx 0 32rpx;
  font-size: 36rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  text-align: center;
  color: v-bind('themeConfig.textPrimary');
  transition: all 0.3s ease;
  text-transform: uppercase;
  
  &:focus {
    border-color: v-bind('themeConfig.primaryColor');
    background: v-bind('themeConfig.cardBg');
  }
}

.input-length {
  position: absolute;
  right: 32rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  font-weight: 600;
}

.tips {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx;
  background: v-bind('themeConfig.primaryColor + "1a"');
  border-radius: 12rpx;
  border: 1px solid v-bind('themeConfig.primaryColor + "33"');
}

.tip-icon {
  font-size: 32rpx;
}

.tip-text {
  font-size: 24rpx;
  color: v-bind('themeConfig.primaryColor');
  flex: 1;
}

.family-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.family-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 20rpx;
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  font-weight: 700;
  box-shadow: v-bind('themeConfig.shadowLight');
  flex-shrink: 0;
}

.family-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.family-name {
  font-size: 32rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  transition: color 0.3s ease;
}

.family-desc {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
}

.family-code {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 8rpx;
}

.code-label {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
}

.code-value {
  font-family: 'Courier New', monospace;
  font-size: 26rpx;
  font-weight: 700;
  color: v-bind('themeConfig.primaryColor');
  padding: 4rpx 12rpx;
  background: v-bind('themeConfig.primaryColor + "1a"');
  border-radius: 6rpx;
  letter-spacing: 2rpx;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.feature-icon {
  font-size: 40rpx;
  flex-shrink: 0;
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.feature-title {
  font-size: 28rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
  transition: color 0.3s ease;
}

.feature-desc {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
}

.action-area {
  padding: 40rpx 20rpx;
}

.join-btn {
  width: 100%;
  height: 100rpx;
  background: v-bind('themeConfig.primaryGradient');
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
  border: none;
  
  &:active:not(.disabled) {
    opacity: 0.9;
    transform: scale(0.98);
  }
  
  &.disabled {
    opacity: 0.5;
    background: v-bind('themeConfig.textSecondary');
  }
  
  text {
    font-size: 32rpx;
    color: #fff;
    font-weight: 700;
  }
}

.glass-card {
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
}
</style>
