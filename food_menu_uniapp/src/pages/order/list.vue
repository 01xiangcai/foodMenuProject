<template>
  <view class="page">
    <!-- 顶部状态页签 -->
    <scroll-view 
      class="status-tabs" 
      scroll-x 
      :scroll-into-view="'tab-' + currentTab" 
      scroll-with-animation
      enable-flex
      :show-scrollbar="false"
    >
      <view class="tabs-wrapper">
        <view 
          class="tab-item" 
          v-for="tab in statusTabs" 
          :key="tab.value"
          :id="'tab-' + tab.value"
          :class="{ active: currentTab === tab.value }"
          @tap="switchTab(tab.value)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view class="tab-line-container" v-if="currentTab === tab.value">
             <view class="tab-line"></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 订单列表 -->
    <scroll-view class="order-scroll" scroll-y @scrolltolower="loadMore" :style="{ height: 'calc(100vh - 88rpx)' }">
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
            <view class="status-badge" :class="getStatusClass(order.status, order.payStatus)">
              <text>{{ getStatusText(order.status, order.payStatus) }}</text>
            </view>
          </view>
          
          <view class="order-count">{{ order.items?.length || 0 }}个菜品</view>

          <!-- 订单商品列表 -->
          <view class="order-items">
            <!-- 只显示前3个或者全部(如果已展开) -->
            <view 
              class="order-item" 
              v-for="item in getDisplayItems(order)" 
              :key="item.id"
              @tap.stop="navigateToDishDetail(item.dishId, item.dishStatus)"
            >
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
            
            <!-- 展开/收起按钮 -->
            <view 
              class="expand-btn" 
              v-if="order.items && order.items.length > 3"
              @tap.stop="toggleExpand(order)"
            >
              <text>{{ order.expanded ? '收起' : `展开剩余${order.items.length - 3}个菜品` }}</text>
              <text class="arrow" :class="{ up: order.expanded }">▼</text>
            </view>
          </view>

          <!-- 订单底部 -->
          <view class="order-footer">
            <view class="footer-left">
              <text class="total-label">合计：</text>
              <text class="total-price">¥{{ order.totalAmount }}</text>
            </view>
            
            <view class="footer-actions">
              <!-- 待支付或待接单订单显示取消和详情按钮 -->
              <view class="action-btns" v-if="order.status === 0 || order.status === 5">
                <view class="btn-cancel" @tap.stop="cancelOrder(order.id)">
                  <text>取消订单</text>
                </view>
                <view class="btn-pay" v-if="order.status === 5" @tap.stop="goToDetail(order.id)">
                  <text>去支付</text>
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

      <!-- 没有更多数据 -->
      <view class="no-more" v-if="!loading && noMore && orders.length > 0">
        <text>没有更多订单了</text>
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
const currentTab = ref(-1)

const statusTabs = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 5 },
  { label: '待接单', value: 0 },
  { label: '准备中', value: 1 },
  { label: '配送中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

// 切换Tab
const switchTab = (value) => {
  if (currentTab.value === value) return
  currentTab.value = value
  loadOrders(true)
}

// 获取订单状态文本
const getStatusText = (status, payStatus) => {
  if (status === 5) {
    return '待支付'
  }
  const statusMap = {
    0: '待接单',
    1: '准备中',
    2: '配送中',
    3: '已完成',
    4: '已取消',
    5: '待支付'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态样式
const getStatusClass = (status, payStatus) => {
  if (status === 5) {
    return 'status-unpaid' // 待支付样式
  }
  const classMap = {
    0: 'status-waiting',
    1: 'status-preparing',
    2: 'status-delivering',
    3: 'status-completed',
    4: 'status-cancelled',
    5: 'status-unpaid'
  }
  return classMap[status] || ''
}

// 获取显示的菜品列表
const getDisplayItems = (order) => {
  if (!order.items) return []
  if (order.expanded) return order.items
  return order.items.slice(0, 3)
}

// 切换展开/收起状态
const toggleExpand = (order) => {
  order.expanded = !order.expanded
}

// 加载订单列表
const loadOrders = async (reset = false) => {
  if (loading.value) return
  if (!reset && noMore.value) return

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
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    
    // 如果不是全部状态，则添加状态筛选
    if (currentTab.value !== -1) {
      params.status = currentTab.value
    }

    const res = await getOrderList(params)
    
    const list = res.data?.records || []
    
    // 映射后端数据到前端格式
    const mappedList = list.map(order => ({
      ...order,
      expanded: false, // 默认收起
      items: (order.orderItems || []).map(item => ({
        id: item.id,
        dishId: item.dishId,
        name: item.dishName,
        image: item.dishImage,
        price: item.price,
        quantity: item.quantity,
        dishStatus: item.dishStatus !== undefined && item.dishStatus !== null ? item.dishStatus : 0 // 如果后端没有返回状态，默认认为已下架（更安全）
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
const navigateToDishDetail = (dishId, dishStatus) => {
  if (!dishId) return
  
  // 检查菜品状态，只有明确是在售状态（status === 1）才允许查看详情
  // 如果状态是undefined、null、0或其他值，都阻止查看
  if (dishStatus !== 1) {
    uni.showToast({
      title: '该菜品已下架',
      icon: 'none',
      duration: 2000
    })
    return
  }
  
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
  
  &.status-unpaid {
    background: rgba(255, 59, 48, 0.15); /* 红色提醒 */
    color: #ff3b30;
    border: 1px solid rgba(255, 59, 48, 0.3);
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

.expand-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
  
  .arrow {
    margin-left: 8rpx;
    font-size: 20rpx;
    transition: transform 0.3s ease;
    
    &.up {
      transform: rotate(180deg);
    }
  }
  
  &:active {
    opacity: 0.7;
  }
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

.no-more {
  padding: 30rpx 0 50rpx;
  text-align: center;
  
  text {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
    opacity: 0.6;
  }
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

/* 顶部状态页签样式 */
.status-tabs {
  width: 100%;
  height: 96rpx;
  background: v-bind('themeConfig.cardBg');
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 10;
}

.tabs-wrapper {
  display: flex;
  flex-wrap: nowrap;
  height: 100%;
  padding: 0 10rpx;
  align-items: center;
}

.tab-item {
  position: relative;
  flex-shrink: 0; /* 防止挤压换行 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 32rpx;
  height: 100%;
  white-space: nowrap;
  
  .tab-text {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  &.active .tab-text {
    color: v-bind('themeConfig.primaryColor');
    font-weight: 600;
    font-size: 32rpx;
    transform: scale(1.05);
  }
  
  .tab-line-container {
    position: absolute;
    bottom: 12rpx;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
  }
  
  .tab-line {
    width: 32rpx;
    height: 8rpx;
    background: v-bind('themeConfig.primaryColor'); /* 或使用 primaryGradient */
    border-radius: 999px;
    box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
  }
}
</style>
