<template>
  <view class="page">
    <!-- 编辑资料弹窗 -->
    <view class="edit-modal">
      <view class="modal-content glass-card">
        <view class="modal-header">
          <text class="title">编辑资料</text>
          <view class="close-btn" @tap="goBack">
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
              placeholder="用户_long"
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
import { ref, onMounted } from 'vue'
import { getUserInfo } from '@/api/index'

const formData = ref({
  nickname: '',
  avatar: '',
  avatarKey: '', // 用于保存到数据库的objectKey
  tempAvatarPath: '' // 临时文件路径，用于延迟上传
})

const goBack = () => {
  uni.navigateBack()
}

// 加载用户信息
const loadUserInfo = async () => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    return
  }
  
  try {
    const res = await getUserInfo()
    if (res.data) {
      formData.value = {
        nickname: res.data.nickname || '',
        avatar: res.data.avatar || '', // 后端返回的预签名URL
        avatarKey: '', // 初始为空,只有上传新头像才会设置
        tempAvatarPath: '' // 临时文件路径
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 选择头像
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 保存临时路径用于预览和后续上传
      formData.value.avatar = tempFilePath
      formData.value.tempAvatarPath = tempFilePath
    }
  })
}

// 保存
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
    
    const { updateUserInfo, uploadFile } = await import('@/api/index')
    const updateData = {
      nickname: formData.value.nickname
    }
    
    // 如果选择了新头像，先上传
    if (formData.value.tempAvatarPath) {
      try {
        const uploadRes = await uploadFile(formData.value.tempAvatarPath)
        if (uploadRes.data) {
          // 使用 objectKey 保存到数据库
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
    await updateUserInfo(updateData)
    
    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success',
      duration: 1500
    })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('保存失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

onMounted(() => {
  loadUserInfo()
})
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

.edit-modal {
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

.avatar-upload {
  width: 100%;
  height: 300rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 2px dashed rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
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
    color: #8b8fa3;
  }
}

.save-btn {
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
</style>
