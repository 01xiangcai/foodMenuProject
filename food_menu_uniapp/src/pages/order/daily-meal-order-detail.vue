<template>
  <view class="page" :class="'theme-' + currentTheme">
    <!-- 头部信息 -->
    <view class="header-card">
      <view class="header-info">
        <text class="meal-period">{{ mealPeriodName }}</text>
        <text class="order-date">{{ orderDate }}</text>
      </view>
      <view class="status-badge" :class="'status-' + orderDetail.status">
        <text>{{ statusText }}</text>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">¥{{ orderDetail.totalAmount || 0 }}</text>
        <text class="stat-label">总金额</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ orderDetail.memberCount || 0 }}</text>
        <text class="stat-label">参与人数</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ orderDetail.dishCount || 0 }}</text>
        <text class="stat-label">菜品数量</text>
      </view>
    </view>

    <!-- 迟到订单审核 (仅管理员可见) -->
    <view class="section" v-if="isAdmin && lateMemberOrders.length > 0">
      <view class="section-header">
        <view class="section-title-group">
          <text class="section-title">待审核迟到订单</text>
          <text class="late-tip">⏰ 仅限迟到单</text>
        </view>
      </view>
      <view class="member-list">
        <view 
          class="member-card late-card" 
          v-for="member in lateMemberOrders" 
          :key="member.orderId"
        >
          <view class="member-header">
            <view class="member-info">
              <image 
                class="member-avatar" 
                :src="getAvatarUrl(member.avatar)" 
                mode="aspectFill"
              />
              <view class="member-detail">
                <text class="member-name">{{ member.nickname }}</text>
                <text class="order-time">{{ formatTime(member.createTime) }}</text>
              </view>
            </view>
            <view class="late-actions">
               <button class="review-btn btn-reject" @tap="handleReview(member.orderId, 2)">拒绝</button>
               <button class="review-btn btn-accept" @tap="handleReview(member.orderId, 1)">接受</button>
            </view>
          </view>
          
          <view class="dish-list">
            <view 
              class="dish-item" 
              v-for="item in member.items" 
              :key="item.id"
            >
              <image 
                class="dish-image" 
                :src="getDishImageUrl(item.dishImage)" 
                mode="aspectFill"
              />
              <view class="dish-info">
                <text class="dish-name">{{ item.dishName }}</text>
                <text class="dish-price">¥{{ item.price }} × {{ item.quantity }}</text>
              </view>
              <text class="dish-subtotal">¥{{ item.subtotal }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 成员订单列表 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">家庭成员订单</text>
        <view class="select-all" v-if="isAdmin && orderDetail.status === 0" @tap="toggleSelectAll">
          <view class="checkbox" :class="{ checked: isAllSelected }">
            <text v-if="isAllSelected">✓</text>
          </view>
          <text class="select-text">全选</text>
        </view>
      </view>
      <view class="member-list">
        <view 
          class="member-card" 
          v-for="member in memberOrders" 
          :key="member.orderId"
        >
          <view class="member-header">
            <view class="member-info">
              <image 
                class="member-avatar" 
                :src="getAvatarUrl(member.avatar)" 
                mode="aspectFill"
              />
              <view class="member-detail">
                <text class="member-name">{{ member.nickname }}</text>
                <text class="late-tag-mini" v-if="member.isLateOrder === 1">已接受迟到</text>
                <text class="order-time">{{ formatTime(member.createTime) }}</text>
              </view>
            </view>
            <text class="member-amount">¥{{ member.totalAmount }}</text>
          </view>
          
          <view class="dish-list">
            <view 
              class="dish-item" 
              v-for="item in member.items" 
              :key="item.id"
              @tap="toggleDishSelection(item.id)"
            >
              <view class="checkbox" v-if="isAdmin && orderDetail.status === 0" :class="{ checked: selectedDishes.has(item.id) }">
                <text v-if="selectedDishes.has(item.id)">✓</text>
              </view>
              <image 
                class="dish-image" 
                :src="getDishImageUrl(item.dishImage)" 
                mode="aspectFill"
              />
              <view class="dish-info">
                <text class="dish-name">{{ item.dishName }}</text>
                <text class="dish-price">¥{{ item.price }} × {{ item.quantity }}</text>
              </view>
              <text class="dish-subtotal">¥{{ item.subtotal }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="footer-actions" v-if="isAdmin && orderDetail.status === 0">
      <view class="selection-stats">
        <text class="stats-text">已选 {{ selectedDishes.size }} 项</text>
        <text class="stats-amount">¥{{ selectedTotalAmount }}</text>
      </view>
      <button 
        class="confirm-btn" 
        :class="{ disabled: selectedDishes.size === 0 }"
        :disabled="selectedDishes.size === 0"
        @tap="confirmOrder"
      >
        确认发布选中菜品
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useTheme } from '@/stores/theme'
import { request } from '@/utils/request'
import { getImageUrl } from '@/utils/image'
import { reviewLateOrder } from '@/api/index'

const { currentTheme } = useTheme()

// 响应式数据
const orderId = ref('')
const orderDetail = ref({})
const memberOrders = ref([])
const lateMemberOrders = ref([]) // 待审核迟到订单
const isAdmin = ref(false)
const selectedDishes = ref(new Set()) // 选中的菜品ID集合

const defaultAvatar = 'https://dummyimage.com/100x100/cccccc/ffffff&text=头像'
const defaultDishImage = 'https://dummyimage.com/100x100/FF7D58/ffffff&text=菜品'

// 获取头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return defaultAvatar
  return getImageUrl(avatar)
}

// 获取菜品图片URL
const getDishImageUrl = (image) => {
  if (!image) return defaultDishImage
  return getImageUrl(image)
}

// 计算属性
const mealPeriodName = computed(() => {
  const periodMap = {
    'BREAKFAST': '早餐',
    'LUNCH': '中餐',
    'DINNER': '晚餐'
  }
  return periodMap[orderDetail.value.mealPeriod] || '今日菜单'
})

const orderDate = computed(() => {
  if (!orderDetail.value.orderDate) return ''
  const date = new Date(orderDetail.value.orderDate)
  return `${date.getMonth() + 1}月${date.getDate()}日`
})

const statusText = computed(() => {
  const statusMap = {
    0: '收集中',
    1: '已确认',
    2: '已截止'
  }
  return statusMap[orderDetail.value.status] || '未知'
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

// 是否全选
const isAllSelected = computed(() => {
  if (memberOrders.value.length === 0) return false
  const allDishIds = getAllDishIds()
  return allDishIds.length > 0 && allDishIds.every(id => selectedDishes.value.has(id))
})

// 选中菜品的总金额
const selectedTotalAmount = computed(() => {
  let total = 0
  memberOrders.value.forEach(member => {
    member.items.forEach(item => {
      if (selectedDishes.value.has(item.id)) {
        total += item.subtotal
      }
    })
  })
  return total.toFixed(2)
})

// 获取所有菜品ID
const getAllDishIds = () => {
  const ids = []
  memberOrders.value.forEach(member => {
    member.items.forEach(item => {
      ids.push(item.id)
    })
  })
  return ids
}

// 切换菜品选中状态
const toggleDishSelection = (dishId) => {
  if (!isAdmin.value || orderDetail.value.status !== 0) return
  
  if (selectedDishes.value.has(dishId)) {
    selectedDishes.value.delete(dishId)
  } else {
    selectedDishes.value.add(dishId)
  }
  // 触发响应式更新
  selectedDishes.value = new Set(selectedDishes.value)
}

// 切换全选
const toggleSelectAll = () => {
  if (!isAdmin.value || orderDetail.value.status !== 0) return
  
  const allDishIds = getAllDishIds()
  if (isAllSelected.value) {
    // 取消全选
    selectedDishes.value.clear()
  } else {
    // 全选
    allDishIds.forEach(id => selectedDishes.value.add(id))
  }
  // 触发响应式更新
  selectedDishes.value = new Set(selectedDishes.value)
}

// 加载大订单详情
const loadOrderDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    
    const res = await request({
      url: `/uniapp/daily-meal-order/${orderId.value}`,
      method: 'GET'
    })
    
    if (res.code === 1 && res.data) {
      orderDetail.value = res.data.dailyMealOrder || {}
      memberOrders.value = res.data.memberOrders || []
      lateMemberOrders.value = res.data.lateOrders || []
    } else {
      uni.showToast({ title: res.msg || '加载失败', icon: 'none' })
    }
  } catch (error) {
    console.error('加载大订单详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 审核迟到订单
const handleReview = async (orderId, action) => {
  const actionText = action === 1 ? '接受' : '拒绝'
  try {
    const confirmRes = await uni.showModal({
      title: '审核提示',
      content: `确定要${actionText}该迟到订单吗？${action === 2 ? '\n拒绝后将自动撤销并退款。' : ''}`,
      cancelColor: '#999',
      confirmColor: action === 1 ? '#4caf50' : '#f44336'
    })

    if (!confirmRes.confirm) return

    uni.showLoading({ title: '提交中...' })
    const res = await reviewLateOrder(orderId, action)
    if (res.code === 1) {
      uni.showToast({ title: `${actionText}成功`, icon: 'success' })
      // 重新加载数据
      await loadOrderDetail()
    } else {
      uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
    }
  } catch (error) {
    console.error('审核订单失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 确认发布
const confirmOrder = async () => {
  try {
    // 检查是否有选中的菜品
    if (selectedDishes.value.size === 0) {
      uni.showToast({ title: '请至少选择一个菜品', icon: 'none' })
      return
    }
    
    const result = await uni.showModal({
      title: '确认发布',
      content: `确认发布选中的 ${selectedDishes.value.size} 个菜品吗?`
    })
    
    if (!result.confirm) return
    
    uni.showLoading({ title: '处理中...' })
    
    // 将Set转换为数组
    const dishIds = Array.from(selectedDishes.value)
    
    const res = await request({
      url: `/uniapp/daily-meal-order/confirm/${orderId.value}`,
      method: 'POST',
      data: {
        dishIds: dishIds
      }
    })
    
    if (res.code === 1) {
      uni.showToast({ title: '确认成功', icon: 'success' })
      setTimeout(() => {
        loadOrderDetail()
        // 清空选中状态
        selectedDishes.value.clear()
      }, 1500)
    } else {
      uni.showToast({ title: res.msg || '确认失败', icon: 'none' })
    }
  } catch (error) {
    console.error('确认发布失败:', error)
    uni.showToast({ title: '确认失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 检查是否为管理员
const checkAdmin = async () => {
  try {
    // 从storage中读取role,1表示管理员
    const role = uni.getStorageSync('fm_role')
    isAdmin.value = role === 1
    console.log('用户角色:', role, '是否为管理员:', isAdmin.value)
  } catch (error) {
    console.error('检查管理员权限失败:', error)
  }
}

// 生命周期
onLoad((options) => {
  if (options.id) {
    orderId.value = options.id
  }
})

onMounted(() => {
  checkAdmin()
  if (orderId.value) {
    loadOrderDetail()
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: 20rpx;
  padding-bottom: 120rpx;
}

.header-card {
  background: linear-gradient(135deg, var(--accent-orange), #ff9f43);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 125, 88, 0.3);
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.meal-period {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
}

.order-date {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

.status-badge {
  padding: 12rpx 24rpx;
  border-radius: 40rpx;
  font-size: 24rpx;
  font-weight: 600;
  
  &.status-0 {
    background: rgba(255, 255, 255, 0.3);
    color: #fff;
  }
  
  &.status-1 {
    background: rgba(16, 185, 129, 0.3);
    color: #fff;
  }
  
  &.status-2 {
    background: rgba(107, 114, 128, 0.3);
    color: #fff;
  }
}

.stats-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: var(--shadow-soft);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--accent-orange);
}

.stat-label {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background: var(--border-color);
}

.section {
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 0 10rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
}

.select-text {
  font-size: 26rpx;
  color: var(--accent-orange);
  font-weight: 600;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.member-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: var(--shadow-soft);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid var(--border-color);
}

.member-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: var(--bg-input);
}

.member-detail {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.member-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.order-time {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.member-amount {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--accent-orange);
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: var(--bg-input);
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  transition: all 0.3s;
  cursor: pointer;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}

.checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid var(--border-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  flex-shrink: 0;
  
  &.checked {
    background: linear-gradient(135deg, var(--accent-orange), #ff9f43);
    border-color: var(--accent-orange);
    
    text {
      color: #fff;
      font-size: 24rpx;
      font-weight: 700;
    }
  }
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  background: var(--bg-page);
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.dish-name {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.dish-price {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.dish-subtotal {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-primary);
  flex-shrink: 0;
}

/* 迟到订单样式 */
.section-title-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.late-tip {
  font-size: 22rpx;
  color: #ff9800;
  background: rgba(255, 152, 0, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-weight: 600;
}

.late-card {
  border-left: 8rpx solid #ff9800;
  background: v-bind('currentTheme === "dark" ? "rgba(255, 152, 0, 0.05)" : "#fffcf5"');
}

.late-tag-mini {
  font-size: 20rpx;
  color: #ff9800;
  background: rgba(255, 152, 0, 0.1);
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  display: inline-block;
  margin-left: 8rpx;
  font-weight: 500;
}

.late-actions {
  display: flex;
  gap: 12rpx;
}

.review-btn {
  margin: 0;
  padding: 0 24rpx;
  height: 54rpx;
  line-height: 54rpx;
  font-size: 24rpx;
  border-radius: 27rpx;
  border: none;
  font-weight: 600;
  
  &::after {
    display: none;
  }
  
  &:active {
    opacity: 0.8;
    transform: scale(0.95);
  }
}

.btn-reject {
  background: rgba(244, 67, 54, 0.1);
  color: #f44336;
  border: 1px solid rgba(244, 67, 54, 0.2);
}

.btn-accept {
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.2);
}

.footer-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-card);
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  z-index: 100;
}

.selection-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.stats-text {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.stats-amount {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--accent-orange);
}

.confirm-btn {
  flex-shrink: 0;
  padding: 24rpx 48rpx;
  background: linear-gradient(135deg, var(--accent-orange), #ff9f43);
  color: #fff;
  border-radius: 48rpx;
  font-size: 28rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(255, 125, 88, 0.4);
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.96);
    opacity: 0.9;
  }
  
  &.disabled {
    opacity: 0.5;
    background: var(--border-color);
    box-shadow: none;
  }
}
</style>
