<template>
  <view class="page">
    <!-- 用户信息卡片 -->
    <view class="user-card glass-card">
      <image class="avatar" :src="userInfo.avatar" mode="aspectFill" />
      <view class="user-info">
        <text class="username">{{ userInfo.name || '未登录' }}</text>
        <text class="phone">{{ userInfo.phone || '点击登录' }}</text>
      </view>
    </view>

    <!-- 订单入口 -->
    <view class="order-section glass-card">
      <view class="section-title">
        <text>我的订单</text>
        <view class="more" @tap="goToOrders">
          <text>查看全部</text>
          <text class="arrow">→</text>
        </view>
      </view>
      <view class="order-types">
        <view class="order-type" @tap="goToOrders">
          <text class="icon">📋</text>
          <text class="label">全部订单</text>
        </view>
        <view class="order-type">
          <text class="icon">⏰</text>
          <text class="label">待确认</text>
        </view>
        <view class="order-type">
          <text class="icon">🚚</text>
          <text class="label">配送中</text>
        </view>
        <view class="order-type">
          <text class="icon">✅</text>
          <text class="label">已完成</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section glass-card">
      <view class="menu-item" @tap="goToFavorites">
        <text class="icon">❤️</text>
        <text class="label">我的收藏</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item">
        <text class="icon">📍</text>
        <text class="label">收货地址</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item">
        <text class="icon">⚙️</text>
        <text class="label">设置</text>
        <text class="arrow">→</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" @tap="logout" v-if="userInfo.name">
      <text>退出登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserInfo } from '@/api/index'

const userInfo = ref({
  name: '',
  phone: '',
  avatar: 'https://dummyimage.com/200x200/6366f1/ffffff&text=User'
})

const loadUserInfo = async () => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    return
  }
  
  try {
    const res = await getUserInfo()
    if (res.data) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const goToOrders = () => {
  uni.navigateTo({
    url: '/pages/order/list'
  })
}

const goToFavorites = () => {
  uni.navigateTo({
    url: '/pages/favorites/favorites'
  })
}

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('fm_token')
        userInfo.value = {
          name: '',
          phone: '',
          avatar: 'https://dummyimage.com/200x200/6366f1/ffffff&text=User'
        }
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
      }
    }
  })
}

onShow(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #050a1f;
  padding: 20rpx;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 40rpx;
  margin-bottom: 20rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: 30rpx;
}

.user-info {
  flex: 1;
}

.username {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10rpx;
}

.phone {
  display: block;
  font-size: 28rpx;
  color: #8b8fa3;
}

.order-section {
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  
  text {
    font-size: 32rpx;
    font-weight: 700;
    color: #fff;
  }
  
  .more {
    display: flex;
    align-items: center;
    gap: 10rpx;
    font-size: 28rpx;
    color: #8b8fa3;
    
    .arrow {
      font-size: 24rpx;
    }
  }
}

.order-types {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.order-type {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  
  .icon {
    font-size: 48rpx;
  }
  
  .label {
    font-size: 24rpx;
    color: #8b8fa3;
  }
}

.menu-section {
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  
  &:last-child {
    border-bottom: none;
  }
  
  .icon {
    font-size: 40rpx;
    margin-right: 20rpx;
  }
  
  .label {
    flex: 1;
    font-size: 28rpx;
    color: #fff;
  }
  
  .arrow {
    font-size: 24rpx;
    color: #8b8fa3;
  }
}

.logout-btn {
  margin: 40rpx 0;
  padding: 30rpx;
  background: rgba(255, 59, 48, 0.1);
  border: 1px solid rgba(255, 59, 48, 0.3);
  border-radius: 16rpx;
  text-align: center;
  
  text {
    font-size: 32rpx;
    color: #ff3b30;
    font-weight: 600;
  }
}
</style>
