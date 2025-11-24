<template>
  <view class="page">
    <!-- 用户信息卡片 -->
    <view class="user-card glass-card" @tap="handleUserCardClick">
      <image class="avatar" :src="userInfo.avatar" mode="aspectFill" />
      <view class="user-info">
        <text class="username">{{ userInfo.nickname || '未登录' }}</text>
        <text class="phone" v-if="isLoggedIn">{{ userInfo.phone || '' }}</text>
        <text class="phone" v-else>点击登录</text>
      </view>
      <view v-if="isLoggedIn" class="edit-btn" @tap.stop="editProfile">
        <text>编辑</text>
      </view>
      <view v-else class="login-btn" @tap.stop="goToLogin">
        <text>登录</text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section glass-card">
      <view class="menu-item" @tap="goToOrders">
        <text class="icon">📋</text>
        <text class="label">我的订单</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item" @tap="goToFavorites">
        <text class="icon">❤️</text>
        <text class="label">我的收藏</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item" @tap="goToAddress">
        <text class="icon">📍</text>
        <text class="label">收货地址</text>
        <text class="arrow">→</text>
      </view>
    </view>

    <!-- 其他设置 -->
    <view class="menu-section glass-card">
      <view class="menu-item" @tap="switchTheme">
        <text class="icon">🎨</text>
        <text class="label">切换主题</text>
        <text class="theme-value">{{ themeConfig.name }}</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item" @tap="goToAbout">
        <text class="icon">ℹ️</text>
        <text class="label">关于我们</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item" @tap="goToSettings">
        <text class="icon">⚙️</text>
        <text class="label">设置</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item logout-item" @tap="logout" v-if="userInfo.nickname">
        <text class="icon">📤</text>
        <text class="label">退出登录</text>
        <text class="arrow">→</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserInfo } from '@/api/index'
import { useTheme } from '@/stores/theme'

const userInfo = ref({
  nickname: '',
  phone: '',
  avatar: 'https://dummyimage.com/200x200/6366f1/ffffff&text=User'
})

// 使用主题store
const { currentTheme, themeConfig, toggleTheme, loadTheme } = useTheme()

// 判断是否已登录
const isLoggedIn = computed(() => {
  return !!userInfo.value.nickname
})

// 检查登录状态
const checkLogin = () => {
  if (!isLoggedIn.value) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          goToLogin()
        }
      }
    })
    return false
  }
  return true
}

const loadUserInfo = async () => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    return
  }
  
  try {
    const res = await getUserInfo()
    if (res.data) {
      // 后端已经返回完整的预签名URL,直接使用即可
      if (!res.data.avatar) {
        // 如果没有头像,使用默认头像
        res.data.avatar = 'https://dummyimage.com/200x200/6366f1/ffffff&text=User'
      }
      
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 用户卡片点击
const handleUserCardClick = () => {
  if (!isLoggedIn.value) {
    goToLogin()
  }
}

// 跳转登录
const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/login/login'
  })
}

const editProfile = () => {
  uni.navigateTo({
    url: '/pages/profile/edit'
  })
}

const goToOrders = () => {
  if (!checkLogin()) return
  uni.navigateTo({
    url: '/pages/order/list'
  })
}

const goToFavorites = () => {
  if (!checkLogin()) return
  uni.navigateTo({
    url: '/pages/favorites/favorites'
  })
}

const goToAddress = () => {
  if (!checkLogin()) return
  uni.showToast({
    title: '地址管理功能开发中',
    icon: 'none'
  })
}

const switchTheme = () => {
  toggleTheme()
  uni.showToast({
    title: `已切换到${themeConfig.value.name}主题`,
    icon: 'success'
  })
}

const goToAbout = () => {
  uni.showToast({
    title: '关于我们功能开发中',
    icon: 'none'
  })
}

const goToSettings = () => {
  uni.showToast({
    title: '设置功能开发中',
    icon: 'none'
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
          nickname: '',
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

onMounted(() => {
  loadTheme()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding: 20rpx;
  transition: background-color 0.3s ease;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 40rpx;
  margin-bottom: 20rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: 30rpx;
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: box-shadow 0.3s ease;
}

.user-info {
  flex: 1;
}

.username {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 10rpx;
  transition: color 0.3s ease;
}

.phone {
  display: block;
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
}

.edit-btn,
.login-btn {
  padding: 12rpx 30rpx;
  background: v-bind('themeConfig.primaryColor + "1a"');
  border: 1px solid v-bind('themeConfig.primaryColor + "4d"');
  border-radius: 30rpx;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
  }
  
  text {
    font-size: 26rpx;
    color: v-bind('themeConfig.primaryColor');
    transition: color 0.3s ease;
  }
}

.menu-section {
  padding: 20rpx;
  margin-bottom: 20rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  transition: all 0.3s ease;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: v-bind('themeConfig.inputBg');
    transform: scale(0.98);
  }
  
  &.logout-item {
    .icon,
    .label {
      color: v-bind('themeConfig.errorColor');
    }
  }
  
  .icon {
    font-size: 40rpx;
    margin-right: 20rpx;
    transition: transform 0.3s ease;
  }
  
  .label {
    flex: 1;
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
    transition: color 0.3s ease;
  }
  
  .theme-value {
    font-size: 26rpx;
    color: v-bind('themeConfig.primaryColor');
    margin-right: 10rpx;
    font-weight: 600;
    transition: color 0.3s ease;
  }
  
  .arrow {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
    transition: all 0.3s ease;
  }
}

// 玻璃拟态卡片样式
.glass-card {
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
}
</style>

