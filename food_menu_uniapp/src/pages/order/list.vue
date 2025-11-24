<template>
  <view class="page">
    <!-- 订单列表 -->
    <scroll-view class="order-scroll" scroll-y @scrolltolower="loadMore">
      <view class="order-list">
        <view 
          class="order-card glass-card"
          v-for="order in orders" 
          :key="order.id"
          @tap="goToDetail(order.id)"
        >
          <!-- 订单头部 -->
          <view class="order-header">
            <text class="order-no">订单号：{{ order.orderNumber }}</text>
            <text class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </text>
          </view>

          <!-- 订单商品 -->
          <view class="order-items">
            <view class="order-item" v-for="item in order.items" :key="item.id">
              <image class="item-image" :src="item.image" mode="aspectFill" />
              <view class="item-info">
                <text class="item-name">{{ item.name }}</text>
                <text class="item-spec">x{{ item.quantity }}</text>
              </view>
              <text class="item-price">¥{{ item.price }}</text>
            </view>
          </view>

          <!-- 订单底部 -->
          <view class="order-footer">
            <text class="total-label">合计：</text>
            <text class="total-price">¥{{ order.totalAmount }}</text>
          </view>

          <!-- 操作按钮 -->
          <view class="order-actions" v-if="order.status !== 3">
            <view class="btn-cancel" v-if="order.status === 0" @tap.stop="cancelOrder(order.id)">
              <text>取消订单</text>
            </view>
            <view class="btn-primary" v-if="order.status === 0" @tap.stop="payOrder(order.id)">
              <text>去支付</text>
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
import { getOrderList } from '@/api/index'
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
    0: '待支付',
    1: '待确认',
    2: '配送中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态样式
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-delivering',
    3: 'status-completed',
    4: 'status-cancelled'
  }
  return classMap[status] || ''
}

// 加载订单列表
const loadOrders = async (reset = false) => {
  if (loading.value || noMore.value) return
  
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
    
    if (reset) {
      orders.value = list
    } else {
      orders.value = [...orders.value, ...list]
    }
    
    if (list.length < pageSize.value) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    // 显示示例数据
    if (reset) {
      orders.value = [
        {
          id: 1,
          orderNumber: '202311240001',
          status: 1,
          totalAmount: 86,
          items: [
            {
              id: 1,
              name: '宫保鸡丁',
              quantity: 1,
              price: 38,
              image: 'https://dummyimage.com/200x200/ff6b6b/ffffff&text=宫保鸡丁'
            },
            {
              id: 2,
              name: '红烧肉',
              quantity: 1,
              price: 48,
              image: 'https://dummyimage.com/200x200/4ecdc4/ffffff&text=红烧肉'
            }
          ]
        }
      ]
    }
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

// 跳转详情
const goToDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order/detail?id=${orderId}`
  })
}

// 取消订单
const cancelOrder = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '订单已取消',
          icon: 'success'
        })
        loadOrders(true)
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

onMounted(() => {
  loadTheme()
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
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.order-no {
  font-size: 28rpx;
  color: #8b8fa3;
}

.order-status {
  font-size: 28rpx;
  font-weight: 600;
  
  &.status-pending {
    color: #ff9500;
  }
  
  &.status-confirmed {
    color: #14b8ff;
  }
  
  &.status-delivering {
    color: #667eea;
  }
  
  &.status-completed {
    color: #34c759;
  }
  
  &.status-cancelled {
    color: #8b8fa3;
  }
}

.order-items {
  margin-bottom: 20rpx;
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.item-name {
  font-size: 28rpx;
  color: #fff;
}

.item-spec {
  font-size: 24rpx;
  color: #8b8fa3;
}

.item-price {
  font-size: 28rpx;
  color: #14b8ff;
  font-weight: 600;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.total-label {
  font-size: 28rpx;
  color: #8b8fa3;
  margin-right: 10rpx;
}

.total-price {
  font-size: 36rpx;
  font-weight: 700;
  color: #14b8ff;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  margin-top: 20rpx;
}

.btn-cancel,
.btn-primary {
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #8b8fa3;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.loading,
.empty {
  padding: 100rpx 40rpx;
  text-align: center;
}

.loading text {
  color: #8b8fa3;
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
    color: #8b8fa3;
    margin-bottom: 40rpx;
  }
}

.btn-go-shop {
  display: inline-block;
  padding: 24rpx 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40rpx;
  color: #fff;
  font-size: 28rpx;
}
</style>
