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
          
          <view class="order-count">{{ order.items?.length || 0 }}个菜品</view>

          <!-- 订单商品列表 -->
          <view class="order-items">
            <view class="order-item" v-for="item in order.items" :key="item.id" @tap.stop="navigateToDishDetail(item.dishId)">
              <!-- 如果有图片URL就显示图片，否则显示占位符 -->
              <image v-if="item.image || item.localImage" class="item-image" :src="getDishImage(item)" mode="aspectFill" @error="handleImageError" />
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
              <!-- 待接单订单显示取消和详情按钮 -->
              <view class="action-btns" v-if="order.status === 0">
                <view class="btn-cancel" @tap.stop="cancelOrder(order.id)">
                  <text>取消订单</text>
                </view>
                <view class="btn-detail" @tap.stop="goToDetail(order.id)">
                  <text>详情</text>
                </view>
              </view>
              
              <!-- 其他状态只显示详情按钮 -->
              <view class="action-btns" v-else>
                <view class="btn-delete" v-if="order.status === 3 || order.status === 4" @tap.stop="handleDelete(order.id)">
                  <text>删除</text>
                </view>
                <view class="btn-detail" @tap.stop="goToDetail(order.id)">
                  <text>详情</text>
                </view>
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
        <view class="btn-go-shop" @tap="goToMenu">
          <text>去点餐</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderList, updateOrderStatus, deleteOrder } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { getDishImage } from '@/utils/image'

const { themeConfig, loadTheme } = useTheme()
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const noMore = ref(false)
const cancellingId = ref(null)

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

// 加载订单列表
const loadOrders = async (reset = false) => {
  if (loading.value || noMore.value) return

  // 未登录则跳转到登录页，不请求全部订单
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    uni.navigateTo({
      url: '/pages/login/login'
    })
    return
  }

  if (reset) {
    page.value = 1
    orders.value = []
    noMore.value = false
  }
  
  loading.value = true
  
  try {
    const res = await getOrderList({
      page: page.value,
      pageSize: pageSize.value
    })
    
    const list = res.data?.records || []
    
    // 映射后端数据到前端格式
    const mappedList = list.map(order => ({
      ...order,
      items: (order.orderItems || []).map(item => ({
        id: item.id,
        dishId: item.dishId,
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

// 取消订单
const cancelOrder = (orderId) => {
  if (cancellingId.value) return // 防止重复点击

  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: async (res) => {
      if (res.confirm) {
        cancellingId.value = orderId
        try {
          await updateOrderStatus(orderId, 4)
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
          
          // 本地更新状态，无需重新加载列表
          const order = orders.value.find(o => o.id === orderId)
          if (order) {
            order.status = 4
          }
        } catch (error) {
          console.error('取消订单失败:', error)
          uni.showToast({
            title: '取消失败',
            icon: 'none'
          })
        } finally {
          cancellingId.value = null
        }
      }
    }
  })
}

// 删除订单
const handleDelete = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该订单吗？删除后不可恢复。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteOrder(orderId)
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          // 本地移除
          orders.value = orders.value.filter(o => o.id !== orderId)
        } catch (error) {
          console.error('删除失败:', error)
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 支付订单
const payOrder = (orderId) => {
  uni.showToast({
    title: '跳转支付...',
    icon: 'none'
  })
}

// 去点餐
const goToMenu = () => {
  uni.switchTab({
    url: '/pages/menu/menu'
  })
}

// 跳转到菜品详情
const navigateToDishDetail = (dishId) => {
  if (!dishId) return
  uni.navigateTo({ 
    url: `/pages/detail/detail?id=${dishId}` 
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
  // 每次页面重新显示时刷新订单列表，确保状态及时更新
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
  margin-bottom: 16rpx;
}

.header-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.order-label {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
}

.order-no {
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 500;
  transition: color 0.3s ease;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
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
  transition: color 0.3s ease;
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
  transition: color 0.3s ease;
}

.item-quantity {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
}

.item-price {
  font-size: 32rpx;
  color: v-bind('themeConfig.errorColor');
  font-weight: 700;
  transition: color 0.3s ease;
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
  transition: color 0.3s ease;
}

.total-price {
  font-size: 36rpx;
  font-weight: 700;
  color: v-bind('themeConfig.errorColor');
  transition: color 0.3s ease;
}

.footer-actions {
  display: flex;
  align-items: center;
}

.action-btns {
  display: flex;
  gap: 16rpx;
}

.btn-cancel,
.btn-delete,
.btn-pay,
.btn-detail {
  padding: 12rpx 32rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
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

.btn-delete {
  background: rgba(255, 59, 48, 0.1);
  border: 1px solid rgba(255, 59, 48, 0.3);
  color: #ff3b30;
}

.btn-pay,
.btn-detail {
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  box-shadow: v-bind('themeConfig.shadowLight');
}

.loading,
.empty {
  padding: 100rpx 40rpx;
  text-align: center;
}

.loading text {
  color: v-bind('themeConfig.textSecondary');
  font-size: 28rpx;
  transition: color 0.3s ease;
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
    margin-bottom: 40rpx;
    transition: color 0.3s ease;
  }
}

.btn-go-shop {
  display: inline-block;
  padding: 24rpx 60rpx;
  background: v-bind('themeConfig.primaryGradient');
  border-radius: 40rpx;
  color: #fff;
  font-size: 28rpx;
  box-shadow: v-bind('themeConfig.shadowMedium');
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
    opacity: 0.9;
  }
}
</style>
