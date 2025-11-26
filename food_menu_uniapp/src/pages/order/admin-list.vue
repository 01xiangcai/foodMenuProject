<template>
  <view class="page">
    <!-- 订单列表 -->
    <scroll-view class="order-scroll" scroll-y @scrolltolower="loadMore">
      <view class="order-list">
        <view 
          class="order-card"
          v-for="order in orders" 
          :key="order.id"
        >
          <!-- 订单头部 -->
          <view class="order-header">
            <view class="header-left">
              <text class="order-label">订单号：</text>
              <text class="order-no">{{ order.orderNumber }}</text>
            </view>
            <view class="status-badge" :class="getStatusClass(order.status)">
              <text>{{ getStatusText(order.status) }}</text>
            </view>
          </view>
          
          <!-- 用户信息 (管理员可见) -->
          <view class="user-info-row">
            <image class="user-avatar" :src="order.userAvatar || 'https://dummyimage.com/100x100/ccc/fff'" mode="aspectFill" />
            <view class="user-details">
              <text class="user-name">{{ order.userNickname || '未知用户' }}</text>
              <text class="user-phone">{{ order.userPhone || '无手机号' }}</text>
            </view>
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
          </view>
          
          <view class="order-count">{{ order.items?.length || 0 }}个菜品</view>

          <!-- 订单商品列表 -->
          <view class="order-items">
            <view class="order-item" v-for="item in order.items" :key="item.id">
              <image v-if="item.image" class="item-image" :src="item.image" mode="aspectFill" @error="handleImageError" />
              <view v-else class="item-placeholder">
                <text class="placeholder-text">family dish</text>
              </view>
              <view class="item-info">
                <text class="item-name">{{ item.name }}</text>
                <text class="item-quantity">x{{ item.quantity }}</text>
              </view>
              <text class="item-price">¥{{ item.price }}</text>
            </view>
          </view>

          <!-- 订单底部 -->
          <view class="order-footer">
            <view class="footer-left">
              <text class="total-label">合计：</text>
              <text class="total-price">¥{{ order.totalAmount }}</text>
            </view>
            
            <view class="footer-actions">
              <!-- 待接单: 接单, 取消 -->
              <view class="action-btns" v-if="order.status === 0">
                <view class="btn-cancel" @tap.stop="handleStatusUpdate(order.id, 4)">
                  <text>取消</text>
                </view>
                <view class="btn-primary" @tap.stop="handleStatusUpdate(order.id, 1)">
                  <text>接单</text>
                </view>
              </view>
              
              <!-- 准备中: 配送 -->
              <view class="action-btns" v-else-if="order.status === 1">
                <view class="btn-primary" @tap.stop="handleStatusUpdate(order.id, 2)">
                  <text>配送</text>
                </view>
              </view>
              
              <!-- 配送中: 完成 -->
              <view class="action-btns" v-else-if="order.status === 2">
                <view class="btn-success" @tap.stop="handleStatusUpdate(order.id, 3)">
                  <text>完成</text>
                </view>
              </view>
              
              <view class="btn-detail" @tap.stop="goToDetail(order.id)">
                <text>详情</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>

      <!-- 空状态 -->
      <view class="empty" v-if="!loading && orders.length === 0">
        <text class="icon">📋</text>
        <text class="text">暂无订单</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAllOrders, updateOrderStatus } from '@/api/index'
import { useTheme } from '@/stores/theme'

const { themeConfig, loadTheme } = useTheme()
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const noMore = ref(false)

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待接单',
    1: '准备中',
    2: '配送中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态样式
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-waiting',
    1: 'status-preparing',
    2: 'status-delivering',
    3: 'status-completed',
    4: 'status-cancelled'
  }
  return classMap[status] || ''
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 加载订单列表
const loadOrders = async (reset = false) => {
  if (loading.value || noMore.value) return

  const token = uni.getStorageSync('fm_token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  if (reset) {
    page.value = 1
    orders.value = []
    noMore.value = false
  }
  
  loading.value = true
  
  try {
    const res = await getAllOrders({
      page: page.value,
      pageSize: pageSize.value
    })
    
    const list = res.data?.records || []
    
    // 映射后端数据到前端格式
    const mappedList = list.map(order => ({
      ...order,
      items: (order.orderItems || []).map(item => ({
        id: item.id,
        name: item.dishName,
        image: item.dishImage,
        price: item.price,
        quantity: item.quantity
      }))
    }))
    
    if (reset) {
      orders.value = mappedList
    } else {
      orders.value = [...orders.value, ...mappedList]
    }
    
    if (list.length < pageSize.value) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadOrders()
  }
}

// 跳转订单详情
const goToDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order/detail?id=${orderId}`
  })
}

// 更新订单状态
const handleStatusUpdate = (orderId, status) => {
  let actionText = ''
  switch(status) {
    case 1: actionText = '接单'; break;
    case 2: actionText = '开始配送'; break;
    case 3: actionText = '完成订单'; break;
    case 4: actionText = '取消订单'; break;
  }

  uni.showModal({
    title: '提示',
    content: `确定要${actionText}吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateOrderStatus(orderId, status)
          uni.showToast({
            title: '操作成功',
            icon: 'success'
          })
          loadOrders(true)
        } catch (error) {
          console.error('操作失败:', error)
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 图片加载失败处理
const handleImageError = (e) => {
  console.warn('Image load failed:', e)
}

onMounted(() => {
  loadTheme()
  loadOrders(true)
})

onShow(() => {
  loadOrders(true)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  transition: background-color 0.3s ease;
}

.order-scroll {
  height: 100vh;
}

.order-list {
  padding: 20rpx;
}

.order-card {
  margin-bottom: 20rpx;
  padding: 30rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.order-label {
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
}

.order-no {
  font-size: 26rpx;
  color: v-bind('themeConfig.textPrimary');
  font-family: monospace;
}

.user-info-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 16rpx;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 26rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 500;
}

.user-phone {
  font-size: 22rpx;
  color: v-bind('themeConfig.textSecondary');
}

.order-time {
  font-size: 22rpx;
  color: v-bind('themeConfig.textSecondary');
}

.status-badge {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 600;
  
  &.status-waiting {
    background: rgba(255, 149, 0, 0.15);
    color: #ff9500;
    border: 1px solid rgba(255, 149, 0, 0.3);
  }
  
  &.status-preparing {
    background: rgba(20, 184, 255, 0.15);
    color: #14b8ff;
    border: 1px solid rgba(20, 184, 255, 0.3);
  }
  
  &.status-delivering {
    background: rgba(102, 126, 234, 0.15);
    color: #667eea;
    border: 1px solid rgba(102, 126, 234, 0.3);
  }
  
  &.status-completed {
    background: rgba(52, 199, 89, 0.15);
    color: #34c759;
    border: 1px solid rgba(52, 199, 89, 0.3);
  }
  
  &.status-cancelled {
    background: rgba(139, 143, 163, 0.15);
    color: v-bind('themeConfig.textSecondary');
    border: 1px solid rgba(139, 143, 163, 0.3);
  }
}

.order-count {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  margin-bottom: 20rpx;
}

.order-items {
  margin-bottom: 20rpx;
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.item-image,
.item-placeholder {
  width: 100rpx;
  height: 100rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.item-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  
  .placeholder-text {
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.3);
    text-align: center;
    line-height: 1.4;
  }
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.item-name {
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 500;
}

.item-quantity {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
}

.item-price {
  font-size: 32rpx;
  color: v-bind('themeConfig.errorColor');
  font-weight: 700;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1px solid v-bind('themeConfig.borderColor');
}

.footer-left {
  display: flex;
  align-items: center;
}

.total-label {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
  margin-right: 10rpx;
}

.total-price {
  font-size: 36rpx;
  font-weight: 700;
  color: v-bind('themeConfig.errorColor');
}

.footer-actions {
  display: flex;
  align-items: center;
}

.action-btns {
  display: flex;
  gap: 16rpx;
  margin-right: 16rpx;
}

.btn-cancel,
.btn-primary,
.btn-success,
.btn-detail {
  padding: 10rpx 28rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
    opacity: 0.9;
  }
}

.btn-cancel {
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  color: v-bind('themeConfig.textSecondary');
}

.btn-primary {
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  box-shadow: v-bind('themeConfig.shadowLight');
}

.btn-success {
  background: linear-gradient(135deg, #34c759 0%, #30b350 100%);
  color: #fff;
  box-shadow: v-bind('themeConfig.shadowLight');
}

.btn-detail {
  background: rgba(255, 255, 255, 0.1);
  color: v-bind('themeConfig.textPrimary');
}

.loading,
.empty {
  padding: 100rpx 40rpx;
  text-align: center;
}

.loading text {
  color: v-bind('themeConfig.textSecondary');
  font-size: 28rpx;
}

.empty {
  .icon {
    display: block;
    font-size: 120rpx;
    margin-bottom: 30rpx;
  }
  
  .text {
    display: block;
    font-size: 32rpx;
    color: v-bind('themeConfig.textSecondary');
  }
}
</style>
