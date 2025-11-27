<template>
  <view class="page">
    <!-- 订单状态 -->
    <view class="status-card" :class="getStatusClass(order.status)">
      <text class="status-icon">{{ getStatusIcon(order.status) }}</text>
      <view class="status-content">
        <text class="status-text">{{ getStatusText(order.status) }}</text>
        <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
      </view>
    </view>

    <!-- 订单商品 -->
    <view class="section-card">
      <view class="card-header">
        <view class="header-line"></view>
        <text class="card-title">订单商品</text>
      </view>
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
    </view>

    <!-- 备注 -->
    <view class="section-card" v-if="order.remark">
      <view class="card-header">
        <view class="header-line"></view>
        <text class="card-title">备注</text>
      </view>
      <view class="remark-content">
        <text class="remark-text">{{ order.remark }}</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="section-card">
      <view class="card-header">
        <view class="header-line"></view>
        <text class="card-title">订单信息</text>
      </view>
      <view class="info-list">
        <view class="info-row">
          <text class="label">订单号</text>
          <text class="value">{{ order.orderNumber }}</text>
        </view>
        <view class="info-row">
          <text class="label">下单时间</text>
          <text class="value">{{ order.createTime || '2024-11-24 16:00:00' }}</text>
        </view>
        <view class="info-row">
          <text class="label">支付方式</text>
          <text class="value">{{ order.payMethod || '微信支付' }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-row total">
          <text class="label">订单金额</text>
          <text class="value price">¥{{ order.totalAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar" v-if="order.status === 0">
      <view class="btn-cancel" @tap="cancelOrder">
        <text>取消订单</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getOrderDetail, updateOrderStatus } from '@/api/index'
import { useTheme } from '@/stores/theme'

const order = ref({
  id: 0,
  orderNumber: '',
  status: 0,
  totalAmount: 0,
  remark: '',
  items: []
})

const getStatusIcon = (status) => {
  const iconMap = {
    0: '⏳', // 待接单
    1: '👨‍🍳', // 准备中
    2: '🛵', // 配送中
    3: '🎉', // 已完成
    4: '❌'  // 已取消
  }
  return iconMap[status] || '📋'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待接单',
    1: '准备中',
    2: '配送中',
    3: '已完成',
    4: '已取消'
  }
  return textMap[status] || '未知状态'
}

const getStatusDesc = (status) => {
  const descMap = {
    0: '等待商家接单中',
    1: '商家正在准备餐品',
    2: '骑手正在配送中',
    3: '订单已完成，期待下次光临',
    4: '订单已取消'
  }
  return descMap[status] || ''
}

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

const { themeConfig, loadTheme } = useTheme()

const loadOrderDetail = async (id) => {
  try {
    const res = await getOrderDetail(id)
    if (res.data) {
      const data = res.data
      order.value = {
        id: data.id,
        orderNumber: data.orderNumber,
        status: data.status,
        totalAmount: data.totalAmount,
        consignee: data.consignee,
        phone: data.phone,
        address: data.address,
        createTime: data.createTime,
        payMethod: data.payMethod,
        remark: data.remark || '',
        items: (data.orderItems || []).map(item => ({
          id: item.id,
          name: item.dishName,
          quantity: item.quantity,
          price: item.price,
          image: item.dishImage
        }))
      }
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    // 显示示例数据
    order.value = {
      id: id,
      orderNumber: '202311240001',
      status: 1,
      totalAmount: 86,
      consignee: '张三',
      phone: '138****8888',
      address: '广东省深圳市南山区科技园',
      createTime: '2024-11-24 16:00:00',
      payMethod: '微信支付',
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
  }
}

const cancelOrder = () => {
  if (!order.value.id) return
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateOrderStatus(order.value.id, 4)
          // 本地立即更新状态，确保当前页面即时回显
          order.value.status = 4
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          console.error('取消订单失败:', error)
        }
      }
    }
  })
}

onLoad((options) => {
  loadTheme()
  if (options.id) {
    loadOrderDetail(options.id)
  }
})

onShow(() => {
  loadTheme()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding: 20rpx;
  padding-bottom: 160rpx;
  transition: background-color 0.3s ease;
}

/* 状态卡片优化 */
.status-card {
  display: flex;
  align-items: center;
  padding: 40rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  color: #fff;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &.status-waiting {
    background: linear-gradient(135deg, #FFB347 0%, #FFCC33 100%); /* 柔和橙色 */
  }
  
  &.status-preparing {
    background: linear-gradient(135deg, #56CCF2 0%, #2F80ED 100%); /* 清新蓝色 */
  }
  
  &.status-delivering {
    background: linear-gradient(135deg, #B24592 0%, #F15F79 100%); /* 活力紫红 */
  }
  
  &.status-completed {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); /* 清爽绿色 */
  }
  
  &.status-cancelled {
    background: linear-gradient(135deg, #bdc3c7 0%, #2c3e50 100%); /* 沉稳灰色 */
    color: #fff;
    
    .status-text { color: #fff; }
    .status-desc { color: #eee; }
  }
}

.status-icon {
  font-size: 80rpx;
  margin-right: 30rpx;
}

.status-content {
  flex: 1;
}

.status-text {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.status-desc {
  display: block;
  font-size: 26rpx;
  opacity: 0.9;
}

/* 通用卡片样式 */
.section-card {
  padding: 0;
  margin-bottom: 24rpx;
  background: v-bind('themeConfig.cardBg');
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: v-bind('themeConfig.shadowLight');
  border: 1px solid v-bind('themeConfig.cardBorder');
  transition: all 0.3s ease;
}

.card-header {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
}

.header-line {
  width: 8rpx;
  height: 32rpx;
  background: v-bind('themeConfig.primaryColor');
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
}

/* 商品列表优化 */
.order-items {
  padding: 0 30rpx;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  
  &:last-child {
    border-bottom: none;
  }
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  background: #f5f5f5;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 120rpx;
  padding: 4rpx 0;
}

.item-name {
  font-size: 30rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 500;
  line-height: 1.4;
}

.item-spec {
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
}

.item-price {
  font-size: 32rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 700;
}

/* 信息列表优化 */
.info-list {
  padding: 30rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  .value {
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
    font-weight: 500;
  }
}

.divider {
  height: 1px;
  background: v-bind('themeConfig.borderColor');
  margin: 24rpx 0;
}

.info-row.total {
  margin-top: 10rpx;
  
  .label {
    font-size: 30rpx;
    color: v-bind('themeConfig.textPrimary');
    font-weight: 600;
  }
  
  .value.price {
    font-size: 40rpx;
    color: v-bind('themeConfig.errorColor');
    font-weight: 800;
  }
}

/* 备注内容 */
.remark-content {
  padding: 30rpx;
}

.remark-text {
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  line-height: 1.6;
  word-break: break-all;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: v-bind('themeConfig.bgSecondary');
  backdrop-filter: blur(20px);
  border-top: 1px solid v-bind('themeConfig.borderColor');
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  z-index: 100;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.btn-cancel {
  padding: 20rpx 48rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  color: v-bind('themeConfig.textSecondary');
  transition: all 0.3s;
  
  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
}
</style>
