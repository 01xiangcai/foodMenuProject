<template>
  <view class="page">
    <!-- 头部渐变背景 + 头像 -->
    <view class="header-section">
      <view class="avatar-container" @tap="isMyself && chooseAvatar()">
        <image class="avatar" :src="userInfo.avatar" mode="aspectFill" />
        <view class="avatar-ring"></view>
        <view class="camera-badge" v-if="isMyself">
          <text class="camera-icon">📷</text>
        </view>
      </view>
    </view>

    <!-- 基本信息卡片 -->
    <view class="info-card glass-card">
      <view class="card-title">基本信息</view>
      
      <view class="info-row" @tap="isMyself && editField('nickname')">
        <text class="label">昵称</text>
        <view class="value-container">
          <input 
            v-if="editingField === 'nickname' && isMyself"
            class="input-field"
            v-model="formData.nickname"
            :focus="editingField === 'nickname'"
            @input="onFieldInput('nickname')"
            @blur="saveField('nickname')"
          />
          <text v-else class="value">{{ formData.nickname || userInfo.nickname || '未设置' }}</text>
          <text v-if="editingField !== 'nickname' && isMyself" class="edit-icon">✏️</text>
        </view>
      </view>

      <view class="info-row readonly">
        <text class="label">用户名</text>
        <text class="value readonly-value">{{ userInfo.username || '未设置' }}</text>
      </view>

      <view class="info-row" @tap="isMyself && editField('phone')">
        <text class="label">手机号</text>
        <view class="value-container">
          <input 
            v-if="editingField === 'phone' && isMyself"
            class="input-field"
            v-model="formData.phone"
            type="number"
            :focus="editingField === 'phone'"
            @input="onFieldInput('phone')"
            @blur="saveField('phone')"
          />
          <text v-else class="value">{{ formData.phone || userInfo.phone || '未设置' }}</text>
          <text v-if="editingField !== 'phone' && isMyself" class="edit-icon">✏️</text>
        </view>
      </view>

      <view class="info-row" @tap="isMyself && showGenderSelector()">
        <text class="label">性别</text>
        <view class="value-container">
          <text class="value">{{ genderText }}</text>
          <text class="arrow" v-if="isMyself">›</text>
        </view>
      </view>
    </view>

    <!-- 账户信息卡片 -->
    <view class="info-card glass-card">
      <view class="card-title">账户信息</view>
      
      <view class="info-row readonly">
        <text class="label">所属家庭</text>
        <view class="value-container">
          <text v-if="familyInfo || userInfo.familyName" class="value family-badge">
            🏠 {{ familyInfo ? familyInfo.name : userInfo.familyName }}
          </text>
          <text v-else class="value readonly-value">未加入家庭</text>
        </view>
      </view>

      <view class="info-row readonly">
        <text class="label">角色</text>
        <view class="role-badge" :class="{ admin: userInfo.role === 1 }">
          <text>{{ userInfo.role === 1 ? '管理员' : '普通用户' }}</text>
        </view>
      </view>

      <view class="info-row readonly">
        <text class="label">注册时间</text>
        <text class="value readonly-value">{{ formatDate(userInfo.createTime) }}</text>
      </view>

      <!-- 修改密码入口（仅自己可见） -->
      <view class="info-row" v-if="isMyself" @tap="showPasswordModal">
        <text class="label">登录密码</text>
        <view class="value-container">
          <text class="value">修改密码</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="action-buttons" v-if="hasChanges">
      <view class="cancel-btn" @tap="cancelChanges">
        <text>取消</text>
      </view>
      <view class="save-btn" @tap="saveChanges">
        <text>保存</text>
      </view>
    </view>

    <!-- 性别选择器弹窗 -->
    <view class="gender-mask" v-if="showGenderPicker" @tap="closeGenderPicker"></view>
    <view class="gender-selector" :class="{ show: showGenderPicker }">
      <view class="selector-header">
        <text class="title">选择性别</text>
        <view class="close-btn" @tap="closeGenderPicker">
          <text>✕</text>
        </view>
      </view>
      <view class="gender-options">
        <view 
          v-for="(option, index) in genderOptionsData" 
          :key="index"
          class="gender-card"
          :class="{ selected: formData.gender === index }"
          @tap="selectGender(index)"
        >
          <view class="card-content">
            <text class="card-icon">{{ option.icon }}</text>
            <text class="card-label">{{ option.label }}</text>
          </view>
          <view class="selection-indicator" v-if="formData.gender === index">
            <text class="check-mark">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <view class="password-mask" v-if="showPasswordPicker" @tap="closePasswordModal"></view>
    <view class="password-modal" :class="{ show: showPasswordPicker }">
      <view class="modal-header">
        <text class="modal-title">修改登录密码</text>
        <view class="close-btn" @tap="closePasswordModal">
          <text>✕</text>
        </view>
      </view>
      <view class="modal-body">
        <view class="input-group">
          <text class="input-label">原密码</text>
          <input 
            class="password-input"
            type="password"
            v-model="passwordForm.oldPassword"
            placeholder="请输入原密码（首次设置可留空）"
            :password="true"
          />
        </view>
        <view class="input-group">
          <text class="input-label">新密码</text>
          <input 
            class="password-input"
            type="password"
            v-model="passwordForm.newPassword"
            placeholder="请输入新密码（至少6位）"
            :password="true"
          />
        </view>
        <view class="input-group">
          <text class="input-label">确认密码</text>
          <input 
            class="password-input"
            type="password"
            v-model="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
            :password="true"
          />
        </view>
        
        <!-- 错误提示 -->
        <view class="error-tip" v-if="passwordError">
          <text>{{ passwordError }}</text>
        </view>
      </view>
      <view class="modal-footer">
        <view class="cancel-btn" @tap="closePasswordModal">
          <text>取消</text>
        </view>
        <view class="confirm-btn" @tap="submitPasswordChange">
          <text>确认修改</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getWxUserInfo, getCurrentFamily } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { onLoad } from '@dcloudio/uni-app'

// 使用主题
const { themeConfig } = useTheme()

// 用户信息
const userInfo = ref({
  nickname: '',
  username: '',
  phone: '',
  gender: 0,
  avatar: 'https://dummyimage.com/200x200/6366f1/ffffff&text=User',
  role: 0,
  createTime: null
})

// 家庭信息
const familyInfo = ref(null)

// 表单数据
const formData = ref({
  nickname: '',
  phone: '',
  gender: 0,
  avatar: ''
})

// 编辑状态
const editingField = ref('')
const hasChanges = ref(false)

// 性别选择器
const showGenderPicker = ref(false)
const genderOptions = ['未知', '男', '女']
const genderOptionsData = [
  { icon: '❓', label: '未知' },
  { icon: '♂️', label: '男' },
  { icon: '♀️', label: '女' }
]
const genderIndex = computed(() => formData.value.gender || userInfo.value.gender || 0)
const genderText = computed(() => {
  const currentGender = formData.value.gender !== undefined ? formData.value.gender : (userInfo.value.gender || 0)
  return `${genderOptionsData[currentGender].icon} ${genderOptionsData[currentGender].label}`
})

// 修改密码弹窗
const showPasswordPicker = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordError = ref('')

// 当前查看的用户ID (null表示自己)
const currentViewUserId = ref(null)

// 是否是自己
const isMyself = computed(() => {
  return !currentViewUserId.value
})

// 加载用户信息（核心逻辑重构）
const loadPageData = async (optionId) => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
    return
  }

  try {
    // 1. 总是先获取"我"的信息，以拿到 myId
    const myRes = await getWxUserInfo()
    if (!myRes.data) throw new Error('获取个人信息失败')
    
    const myInfo = myRes.data
    const myId = myInfo.id

    // 2. 判断是否是自己
    let targetId = null
    if (optionId && String(optionId) !== String(myId)) {
      targetId = optionId
    }

    // 设置状态
    currentViewUserId.value = targetId // null means myself

    if (isMyself.value) {
      // --- 查看自己 ---
      userInfo.value = myInfo
      // 初始化表单
      formData.value = {
        nickname: myInfo.nickname || '',
        phone: myInfo.phone || '',
        gender: myInfo.gender || 0,
        avatar: myInfo.avatar || ''
      }
      // 加载家庭信息
      loadFamilyInfo()
    } else {
      // --- 查看他人 ---
      // 加载他人信息
      const { getWxOtherUserInfo } = await import('@/api/index')
      const otherRes = await getWxOtherUserInfo(targetId)
      if (otherRes.data) {
        userInfo.value = otherRes.data
        // 清空表单（只读模式不需要表单数据）
        formData.value = {}
      }
      // 他人模式下，不加载家庭信息（或只显示脱敏信息）
      familyInfo.value = null 
    }

  } catch (error) {
    console.error('加载页面数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 加载家庭信息
const loadFamilyInfo = async () => {
  try {
    const res = await getCurrentFamily()
    if (res.data) {
      familyInfo.value = res.data
    }
  } catch (error) {
    console.error('获取家庭信息失败:', error)
  }
}

// 编辑字段
const editField = (field) => {
  if (field === 'nickname' || field === 'phone') {
    editingField.value = field
  }
}

// 字段输入时检测变化
const onFieldInput = (field) => {
  // 实时检查是否有变化
  if (formData.value[field] !== userInfo.value[field]) {
    hasChanges.value = true
  } else {
    // 检查所有字段是否都没有变化
    const noChanges = 
      formData.value.nickname === userInfo.value.nickname &&
      formData.value.phone === userInfo.value.phone &&
      formData.value.gender === userInfo.value.gender &&
      formData.value.avatar === userInfo.value.avatar
    hasChanges.value = !noChanges
  }
}

// 保存字段
const saveField = (field) => {
  editingField.value = ''
  // 最后检查一次是否有变化
  if (formData.value[field] !== userInfo.value[field]) {
    hasChanges.value = true
  }
}

// 显示性别选择器
const showGenderSelector = () => {
  showGenderPicker.value = true
}

// 关闭性别选择器
const closeGenderPicker = () => {
  showGenderPicker.value = false
}

// 选择性别
const selectGender = (index) => {
  formData.value.gender = index
  if (index !== userInfo.value.gender) {
    hasChanges.value = true
  }
  // 延迟关闭，让用户看到选中效果
  setTimeout(() => {
    showGenderPicker.value = false
  }, 300)
}

// 选择头像
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      
      uni.showLoading({
        title: '上传中...',
        mask: true
      })

      try {
        const { uploadFile } = await import('@/api/index')
        const uploadRes = await uploadFile(tempFilePath)
        
        if (uploadRes.data) {
          formData.value.avatar = uploadRes.data.objectKey
          hasChanges.value = true
          
          // 立即更新显示
          userInfo.value.avatar = tempFilePath
          
          uni.hideLoading()
          uni.showToast({
            title: '头像已选择',
            icon: 'success'
          })
        }
      } catch (error) {
        console.error('上传头像失败:', error)
        uni.hideLoading()
        
        const errorMessage = error.message || '头像上传失败'
        if (errorMessage.includes('上限') || errorMessage.includes('次数')) {
          uni.showModal({
            title: '提示',
            content: errorMessage,
            showCancel: false,
            confirmText: '知道了'
          })
        } else {
          uni.showToast({
            title: errorMessage,
            icon: 'none'
          })
        }
      }
    }
  })
}

// 取消更改
const cancelChanges = () => {
  // 恢复原始数据
  formData.value = {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    gender: userInfo.value.gender || 0,
    avatar: userInfo.value.avatar || ''
  }
  hasChanges.value = false
  editingField.value = ''
}

// 保存更改
const saveChanges = async () => {
  if (!formData.value.nickname) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }

  // 验证手机号格式
  if (formData.value.phone) {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(formData.value.phone)) {
      uni.showToast({
        title: '请输入正确的手机号',
        icon: 'none',
        duration: 2000
      })
      return
    }
  }

  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    const { updateWxUserInfo } = await import('@/api/index')
    const updateData = {
      nickname: formData.value.nickname,
      phone: formData.value.phone,
      gender: formData.value.gender
    }

    if (formData.value.avatar && formData.value.avatar !== userInfo.value.avatar) {
      updateData.avatar = formData.value.avatar
    }

    await updateWxUserInfo(updateData)

    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success',
      duration: 1500
    })

    hasChanges.value = false
    
    // 刷新用户信息
    setTimeout(() => {
      loadPageData(null)
    }, 500)
  } catch (error) {
    console.error('保存失败:', error)
    uni.hideLoading()
    
    // 提取错误信息
    let errorMessage = '保存失败'
    if (error && error.message) {
      errorMessage = error.message
      // 如果错误信息包含"更新失败:"前缀，提取后面的具体信息
      if (errorMessage.includes('更新失败:')) {
        const parts = errorMessage.split('更新失败:')
        if (parts.length > 1) {
          // 提取具体错误信息，去除换行和多余空格
          errorMessage = parts[1].trim().replace(/\r\n/g, ' ').replace(/\s+/g, ' ')
          // 如果包含数据库错误信息，提取更友好的提示
          if (errorMessage.includes('Duplicate entry')) {
            errorMessage = '手机号已被其他用户使用'
          } else if (errorMessage.includes('手机号已被其他用户使用')) {
            errorMessage = '手机号已被其他用户使用'
          }
        }
      }
    }
    
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 3000
    })
  }
}


// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  // 解决部分环境 new Date() 不支持 "yyyy-MM-dd HH:mm:ss" 的问题
  const normalizedDate = typeof dateString === 'string' ? dateString.replace(/-/g, '/').replace(' ', ' ') : dateString
  const date = new Date(normalizedDate)
  
  if (isNaN(date.getTime())) return dateString // 如果解析失败，直接显示原字符串
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 显示修改密码弹窗
const showPasswordModal = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordError.value = ''
  showPasswordPicker.value = true
}

// 关闭修改密码弹窗
const closePasswordModal = () => {
  showPasswordPicker.value = false
}

// 提交密码修改
const submitPasswordChange = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value
  
  // 清除之前的错误
  passwordError.value = ''
  
  // 验证新密码
  if (!newPassword || newPassword.length < 6) {
    passwordError.value = '新密码不能少于6位'
    return
  }
  
  // 验证确认密码
  if (newPassword !== confirmPassword) {
    passwordError.value = '两次输入的密码不一致'
    return
  }
  
  uni.showLoading({
    title: '正在修改...',
    mask: true
  })
  
  try {
    const { updateLoginPassword } = await import('@/api/index')
    await updateLoginPassword(oldPassword, newPassword)
    
    uni.hideLoading()
    uni.showToast({
      title: '密码修改成功',
      icon: 'success'
    })
    
    closePasswordModal()
  } catch (error) {
    console.error('修改密码失败:', error)
    uni.hideLoading()
    
    // 在弹窗内显示错误
    passwordError.value = error.message || '修改密码失败'
  }
}


onMounted(() => {
  // onMounted 逻辑已移至 onLoad 统一处理
})

onLoad((options) => {
  const id = options?.id || null
  loadPageData(id)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding-bottom: 120rpx;
  transition: background-color 0.3s ease;
}

// 头部区域
.header-section {
  height: 400rpx;
  background: v-bind('themeConfig.primaryGradient');
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    animation: pulse 3s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.avatar-container {
  position: relative;
  z-index: 1;
}

.avatar {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.avatar-ring {
  position: absolute;
  top: -10rpx;
  left: -10rpx;
  right: -10rpx;
  bottom: -10rpx;
  border-radius: 50%;
  border: 3rpx solid v-bind('themeConfig.primaryColor');
  opacity: 0.6;
  animation: breathe 2s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.9;
  }
}

.camera-badge {
  position: absolute;
  bottom: 10rpx;
  right: 10rpx;
  width: 60rpx;
  height: 60rpx;
  background: v-bind('themeConfig.primaryColor');
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  
  .camera-icon {
    font-size: 32rpx;
  }
}

// 信息卡片
.info-card {
  margin: 20rpx;
  padding: 30rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 16rpx;
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid v-bind('themeConfig.borderColor');
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid v-bind('themeConfig.borderColor');
  transition: background 0.2s ease;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:not(.readonly):active {
    background: v-bind('themeConfig.inputBg');
  }
  
  .label {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
    flex-shrink: 0;
  }
  
  .value-container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12rpx;
  }
  
  .value {
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
    text-align: right;
  }
  
  .readonly-value {
    color: v-bind('themeConfig.textSecondary');
  }
  
  .input-field {
    flex: 1;
    text-align: right;
    font-size: 28rpx;
    color: v-bind('themeConfig.primaryColor');
    background: transparent;
    border: none;
    outline: none;
  }
  
  .edit-icon {
    font-size: 24rpx;
    opacity: 0.6;
  }
  
  .arrow {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  .family-badge {
    color: v-bind('themeConfig.primaryColor');
    font-weight: 600;
  }
}

.role-badge {
  padding: 8rpx 20rpx;
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  border-radius: 20rpx;
  
  text {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  &.admin {
    background: v-bind('themeConfig.primaryColor + "1a"');
    border-color: v-bind('themeConfig.primaryColor + "4d"');
    
    text {
      color: v-bind('themeConfig.primaryColor');
      font-weight: 600;
    }
  }
}

// 操作按钮
.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: v-bind('themeConfig.bgPrimary');
  border-top: 1px solid v-bind('themeConfig.borderColor');
  display: flex;
  gap: 20rpx;
  z-index: 100;
}

.cancel-btn,
.save-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  text {
    font-size: 32rpx;
    font-weight: 600;
  }
  
  &:active {
    transform: scale(0.98);
  }
}

.cancel-btn {
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  
  text {
    color: v-bind('themeConfig.textSecondary');
  }
}

.save-btn {
  background: v-bind('themeConfig.primaryGradient');
  box-shadow: 0 4px 16px v-bind('themeConfig.primaryColor + "40"');
  
  text {
    color: #ffffff;
  }
}

// 玻璃拟态效果
.glass-card {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

// 性别选择器
.gender-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.gender-selector {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: v-bind('themeConfig.cardBg');
  border-radius: 32rpx 32rpx 0 0;
  z-index: 1000;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &.show {
    transform: translateY(0);
  }
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  
  .title {
    font-size: 36rpx;
    font-weight: 700;
    color: v-bind('themeConfig.textPrimary');
  }
  
  .close-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:active {
      opacity: 0.6;
    }
    
    text {
      font-size: 40rpx;
      color: v-bind('themeConfig.textSecondary');
    }
  }
}

.gender-options {
  display: flex;
  gap: 20rpx;
  padding: 30rpx 20rpx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 30rpx);
}

.gender-card {
  flex: 1;
  position: relative;
  background: v-bind('themeConfig.inputBg');
  border: 3rpx solid v-bind('themeConfig.borderColor');
  border-radius: 24rpx;
  padding: 40rpx 20rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, transparent 0%, v-bind('themeConfig.primaryColor + "0a"') 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:active {
    transform: scale(0.96);
  }
  
  &.selected {
    border-color: v-bind('themeConfig.primaryColor');
    background: v-bind('themeConfig.primaryColor + "15"');
    box-shadow: 0 8rpx 24rpx v-bind('themeConfig.primaryColor + "30"');
    
    &::before {
      opacity: 1;
    }
  }
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  position: relative;
  z-index: 1;
}

.card-icon {
  font-size: 80rpx;
  line-height: 1;
  transition: transform 0.3s ease;
  
  .gender-card.selected & {
    transform: scale(1.1);
  }
}

.card-label {
  font-size: 28rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
  transition: color 0.3s ease;
  
  .gender-card.selected & {
    color: v-bind('themeConfig.primaryColor');
  }
}

.selection-indicator {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 44rpx;
  height: 44rpx;
  background: v-bind('themeConfig.primaryColor');
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx v-bind('themeConfig.primaryColor + "40"');
  animation: popIn 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  z-index: 2;
}

@keyframes popIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.check-mark {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: bold;
}

// 修改密码弹窗
.password-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

.password-modal {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%) scale(0.8);
  width: 90%;
  max-width: 640rpx;
  background: v-bind('themeConfig.cardBg');
  border-radius: 24rpx;
  z-index: 1000;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &.show {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  
  .modal-title {
    font-size: 34rpx;
    font-weight: 700;
    color: v-bind('themeConfig.textPrimary');
  }
  
  .close-btn {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:active {
      opacity: 0.6;
    }
    
    text {
      font-size: 40rpx;
      color: v-bind('themeConfig.textSecondary');
    }
  }
}

.modal-body {
  padding: 32rpx;
}

.input-group {
  margin-bottom: 28rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .input-label {
    display: block;
    font-size: 26rpx;
    color: v-bind('themeConfig.textSecondary');
    margin-bottom: 12rpx;
  }
  
  .password-input {
    width: 100%;
    height: 88rpx;
    padding: 0 24rpx;
    background: v-bind('themeConfig.inputBg');
    border: 1px solid v-bind('themeConfig.borderColor');
    border-radius: 16rpx;
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
    box-sizing: border-box;
    
    &::placeholder {
      color: v-bind('themeConfig.textSecondary');
      opacity: 0.6;
    }
  }
}

// 错误提示
.error-tip {
  margin-top: 24rpx;
  padding: 16rpx 20rpx;
  background: rgba(255, 77, 79, 0.1);
  border: 1px solid rgba(255, 77, 79, 0.3);
  border-radius: 12rpx;
  
  text {
    font-size: 26rpx;
    color: #ff4d4f;
  }
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 32rpx 32rpx;
  
  .cancel-btn,
  .confirm-btn {
    flex: 1;
    height: 88rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    
    text {
      font-size: 30rpx;
      font-weight: 600;
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
  
  .cancel-btn {
    background: v-bind('themeConfig.inputBg');
    border: 1px solid v-bind('themeConfig.borderColor');
    
    text {
      color: v-bind('themeConfig.textSecondary');
    }
  }
  
  .confirm-btn {
    background: v-bind('themeConfig.primaryGradient');
    box-shadow: 0 4px 16px v-bind('themeConfig.primaryColor + "40"');
    
    text {
      color: #ffffff;
    }
  }
}
</style>
