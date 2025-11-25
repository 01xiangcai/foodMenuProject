<template>
  <view class="page">
    <!-- 订单状态 -->
    <view class="status-card glass-card">
      <text class="status-icon">{{ getStatusIcon(order.status) }}</text>
      <text class="status-text">{{ getStatusText(order.status) }}</text>
      <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
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
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 10rpx;
}

.status-desc {
  display: block;
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
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
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
}

.delivery-info {
  display: flex;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
    width: 160rpx;
  }
  
  .value {
    flex: 1;
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
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
  color: v-bind('themeConfig.textPrimary');
}

.item-spec {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
}

.item-price {
  font-size: 28rpx;
  color: v-bind('themeConfig.primaryColor');
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
    border-top: 1px solid v-bind('themeConfig.borderColor');
  }
  
  .label {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  .value {
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
    
    &.price {
      font-size: 36rpx;
      font-weight: 700;
      color: v-bind('themeConfig.primaryColor');
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: v-bind('themeConfig.bgSecondary');
  backdrop-filter: blur(20px);
  border-top: 1px solid v-bind('themeConfig.borderColor');
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
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  color: v-bind('themeConfig.textSecondary');
}

.btn-primary {
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
}
</style>
