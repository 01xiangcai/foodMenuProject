<template>
  <view class="page">
    <!-- 订单状态 -->
    <view class="status-card glass-card">
      <text class="status-icon">{{ getStatusIcon(order.status) }}</text>
      <text class="status-text">{{ getStatusText(order.status) }}</text>
      <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
    </view>

    <!-- 配送信息 -->
    <view class="delivery-card glass-card">
      <view class="card-title">
        <text>配送信息</text>
      </view>
      <view class="delivery-info">
        <text class="label">收货人：</text>
        <text class="value">{{ order.consignee || '张三' }}</text>
      </view>
      <view class="delivery-info">
        <text class="label">联系电话：</text>
        <text class="value">{{ order.phone || '138****8888' }}</text>
      </view>
      <view class="delivery-info">
        <text class="label">收货地址：</text>
        <text class="value">{{ order.address || '广东省深圳市南山区科技园' }}</text>
      </view>
    </view>

    <!-- 订单商品 -->
    <view class="items-card glass-card">
      <view class="card-title">
        <text>订单商品</text>
      </view>
      <view class="order-item" v-for="item in order.items" :key="item.id">
        <image class="item-image" :src="item.image" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <text class="item-spec">x{{ item.quantity }}</text>
        </view>
        <text class="item-price">¥{{ item.price }}</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-card glass-card">
      <view class="card-title">
        <text>订单信息</text>
      </view>
      <view class="info-row">
        <text class="label">订单号：</text>
        <text class="value">{{ order.orderNumber }}</text>
      </view>
      <view class="info-row">
        <text class="label">下单时间：</text>
        <text class="value">{{ order.createTime || '2024-11-24 16:00:00' }}</text>
      </view>
      <view class="info-row">
        <text class="label">支付方式：</text>
        <text class="value">{{ order.payMethod || '微信支付' }}</text>
      </view>
      <view class="info-row total">
        <text class="label">订单金额：</text>
        <text class="value price">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar" v-if="order.status !== 3 && order.status !== 4">
      <view class="btn-cancel" v-if="order.status === 0" @tap="cancelOrder">
        <text>取消订单</text>
      </view>
      <view class="btn-primary" v-if="order.status === 0" @tap="payOrder">
        <text>去支付</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail } from '@/api/index'

const order = ref({
  id: 0,
  orderNumber: '',
  status: 0,
  totalAmount: 0,
  items: []
})

const getStatusIcon = (status) => {
  const iconMap = {
    0: '⏰',
    1: '✅',
    2: '🚚',
    3: '🎉',
    4: '❌'
  }
  return iconMap[status] || '📋'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待支付',
    1: '待确认',
    2: '配送中',
    3: '已完成',
    4: '已取消'
  }
  return textMap[status] || '未知状态'
}

const getStatusDesc = (status) => {
  const descMap = {
    0: '请尽快完成支付',
    1: '商家正在确认订单',
    2: '骑手正在配送中',
    3: '订单已完成，期待下次光临',
    4: '订单已取消'
  }
  return descMap[status] || ''
}

const loadOrderDetail = async (id) => {
  try {
    const res = await getOrderDetail(id)
    if (res.data) {
      order.value = res.data
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
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '订单已取消',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    }
  })
}

const payOrder = () => {
  uni.showToast({
    title: '跳转支付...',
    icon: 'none'
  })
}

onLoad((options) => {
  if (options.id) {
    loadOrderDetail(options.id)
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #050a1f;
  padding: 20rpx;
  padding-bottom: 160rpx;
}

.status-card {
  text-align: center;
  padding: 60rpx 40rpx;
  margin-bottom: 20rpx;
}

.status-icon {
  display: block;
  font-size: 100rpx;
  margin-bottom: 20rpx;
}

.status-text {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10rpx;
}

.status-desc {
  display: block;
  font-size: 28rpx;
  color: #8b8fa3;
}

.delivery-card,
.items-card,
.order-info-card {
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.delivery-info {
  display: flex;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    font-size: 28rpx;
    color: #8b8fa3;
    width: 160rpx;
  }
  
  .value {
    flex: 1;
    font-size: 28rpx;
    color: #fff;
  }
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

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &.total {
    padding-top: 20rpx;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
  }
  
  .label {
    font-size: 28rpx;
    color: #8b8fa3;
  }
  
  .value {
    font-size: 28rpx;
    color: #fff;
    
    &.price {
      font-size: 36rpx;
      font-weight: 700;
      color: #14b8ff;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: rgba(10, 17, 32, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.btn-cancel,
.btn-primary {
  padding: 24rpx 60rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
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
</style>
