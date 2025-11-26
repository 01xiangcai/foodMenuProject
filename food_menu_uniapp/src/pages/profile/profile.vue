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
      <view class="menu-item" v-if="isAdmin" @tap="goToOrderManage">
        <text class="icon">🧾</text>
        <text class="label">订单管理</text>
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
    
    <!-- 编辑资料弹窗 -->
    <view class="edit-mask" v-if="showEditModal" @tap="closeEditModal" @touchmove.stop.prevent></view>
    <view class="edit-modal" :class="{ show: showEditModal }">
      <view class="modal-content glass-card">
        <view class="modal-header">
          <text class="title">编辑资料</text>
          <view class="close-btn" @tap="closeEditModal">
            <text>✕</text>
          </view>
        </view>

        <view class="form">
          <!-- 昵称 -->
          <view class="form-item">
            <text class="label">昵称</text>
            <input 
              class="input" 
              v-model="formData.nickname"
              placeholder="请输入昵称"
              placeholder-class="placeholder"
            />
          </view>

          <!-- 头像 -->
          <view class="form-item">
            <text class="label">头像</text>
            <view class="avatar-upload" @tap="chooseAvatar">
              <image 
                v-if="formData.avatar" 
                class="avatar-preview" 
                :src="formData.avatar" 
                mode="aspectFill"
              />
              <view v-else class="avatar-placeholder">
                <text class="icon">📷</text>
                <text class="text">点击选择头像</text>
              </view>
            </view>
          </view>

          <!-- 保存按钮 -->
          <view class="save-btn" @tap="handleSave">
            <text>保存</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getWxUserInfo } from '@/api/index'
import { useTheme } from '@/stores/theme'

const userInfo = ref({
  nickname: '',
  phone: '',
  avatar: 'https://dummyimage.com/200x200/6366f1/ffffff&text=User',
  role: 0
})

// 使用主题store
const { currentTheme, themeConfig, toggleTheme, loadTheme } = useTheme()

// 编辑弹窗状态
const showEditModal = ref(false)
const formData = ref({
  nickname: '',
  avatar: '',
  avatarKey: '',
  tempAvatarPath: ''
})

// 判断是否已登录
const isLoggedIn = computed(() => {
  return !!userInfo.value.nickname
})

// 判断是否为管理员
const isAdmin = computed(() => {
  return userInfo.value.role === 1
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
    const res = await getWxUserInfo()
    if (res.data) {
      // 后端已经返回完整的预签名URL,直接使用即可
      if (!res.data.avatar) {
        // 如果没有头像,使用默认头像
        res.data.avatar = 'https://dummyimage.com/200x200/6366f1/ffffff&text=User'
      }

      userInfo.value = res.data
      // 将角色缓存到本地,便于其他页面使用
      uni.setStorageSync('fm_role', res.data.role || 0)
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
  // 打开编辑弹窗，并初始化表单数据
  formData.value = {
    nickname: userInfo.value.nickname || '',
    avatar: userInfo.value.avatar || '',
    avatarKey: '',
    tempAvatarPath: ''
  }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
}

// 选择头像
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      formData.value.avatar = tempFilePath
      formData.value.tempAvatarPath = tempFilePath
    }
  })
}

// 保存编辑
const handleSave = async () => {
  if (!formData.value.nickname) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '保存中...',
      mask: true
    })
    
    const { updateWxUserInfo, uploadFile } = await import('@/api/index')
    const updateData = {
      nickname: formData.value.nickname
    }
    
    // 如果选择了新头像，先上传
    if (formData.value.tempAvatarPath) {
      try {
        const uploadRes = await uploadFile(formData.value.tempAvatarPath)
        if (uploadRes.data) {
          updateData.avatar = uploadRes.data.objectKey
        }
      } catch (uploadError) {
        console.error('上传头像失败:', uploadError)
        uni.hideLoading()
        uni.showToast({
          title: '头像上传失败',
          icon: 'none'
        })
        return
      }
    }
    
    // 更新用户信息
    await updateWxUserInfo(updateData)
    
    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success',
      duration: 1500
    })
    
    // 关闭弹窗并刷新用户信息
    showEditModal.value = false
    setTimeout(() => {
      loadUserInfo()
    }, 500)
  } catch (error) {
    console.error('保存失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
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

// 跳转订单管理(管理员)
const goToOrderManage = () => {
  if (!checkLogin()) {
    return
  }
  if (!isAdmin.value) {
    uni.showToast({
      title: '仅管理员可访问订单管理',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: '/pages/order/admin-list'
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

/* 编辑弹窗样式 */
.edit-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

.edit-modal {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  top: 50%;
  transform: translateY(-50%) scale(0.9);
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &.show {
    opacity: 1;
    visibility: visible;
    transform: translateY(-50%) scale(1);
  }
}

.modal-content {
  max-width: 600rpx;
  margin: 0 auto;
  padding: 0;
  overflow: hidden;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(20px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowHeavy');
  border-radius: 32rpx;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  flex-shrink: 0;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  transition: color 0.3s ease;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  &:active {
    opacity: 0.7;
  }
  
  text {
    font-size: 40rpx;
    color: v-bind('themeConfig.textSecondary');
    transition: color 0.3s ease;
  }
}

.form {
  padding: 40rpx;
  overflow-y: auto;
  flex: 1;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 16rpx;
  transition: color 0.3s ease;
}

.input {
  width: 100%;
  height: 90rpx;
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  transition: all 0.3s ease;
  
  &:focus {
    border-color: v-bind('themeConfig.primaryColor');
  }
}

.placeholder {
  color: v-bind('themeConfig.textSecondary');
}

.avatar-upload {
  width: 100%;
  height: 300rpx;
  background: v-bind('themeConfig.inputBg');
  border: 2px dashed v-bind('themeConfig.borderColor');
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:active {
    border-color: v-bind('themeConfig.primaryColor');
  }
}

.avatar-preview {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  
  .icon {
    font-size: 80rpx;
  }
  
  .text {
    font-size: 26rpx;
    color: v-bind('themeConfig.textSecondary');
    transition: color 0.3s ease;
  }
}

.save-btn {
  margin-top: 40rpx;
  height: 90rpx;
  background: v-bind('themeConfig.primaryGradient');
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
  
  text {
    font-size: 32rpx;
    color: #fff;
    font-weight: 600;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>

