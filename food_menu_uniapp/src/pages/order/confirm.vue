<template>
  <view class="page">
    <!-- 配送地址 -->
    <view class="address-card glass-card" @tap="selectAddress">
      <view class="address-info" v-if="address.name">
        <view class="header">
          <text class="name">{{ address.name }}</text>
          <text class="phone">{{ address.phone }}</text>
        </view>
        <text class="detail">{{ address.detail }}</text>
      </view>
      <view class="no-address" v-else>
        <text class="icon">📍</text>
        <text class="text">请选择收货地址</text>
      </view>
      <text class="arrow">→</text>
    </view>

    <!-- 商品列表 -->
    <view class="items-card glass-card">
      <view class="card-title">
        <text>商品清单</text>
      </view>
      <view class="order-item" v-for="item in items" :key="item.id">
        <image class="item-image" :src="item.image" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <text class="item-spec">x{{ item.quantity }}</text>
        </view>
        <text class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</text>
      </view>
    </view>

    <!-- 备注 -->
    <view class="remark-card glass-card">
      <text class="label">备注</text>
      <textarea 
        class="textarea" 
        placeholder="如有特殊要求请备注" 
        v-model="remark"
        maxlength="200"
      />
    </view>

    <!-- 费用明细 -->
    <view class="fee-card glass-card">
      <view class="fee-row">
        <text class="label">商品金额</text>
        <text class="value">¥{{ goodsAmount }}</text>
      </view>
      <view class="fee-row">
        <text class="label">配送费</text>
        <text class="value">¥{{ deliveryFee }}</text>
      </view>
      <view class="fee-row total">
        <text class="label">实付款</text>
        <text class="value price">¥{{ totalAmount }}</text>
      </view>
    </view>

    <!-- 底部提交 -->
    <view class="bottom-bar">
      <view class="total">
        <text class="label">实付：</text>
        <text class="price">¥{{ totalAmount }}</text>
      </view>
      <view class="btn-submit" @tap="submitOrder">
        <text>提交订单</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createOrder } from '@/api/index'

const address = ref({
  name: '张三',
  phone: '138****8888',
  detail: '广东省深圳市南山区科技园'
})

const items = ref([])
const remark = ref('')
const deliveryFee = ref(5)

// 商品金额
const goodsAmount = computed(() => {
  return items.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0).toFixed(2)
})

// 总金额
const totalAmount = computed(() => {
  return (parseFloat(goodsAmount.value) + deliveryFee.value).toFixed(2)
})

// 选择地址
const selectAddress = () => {
  uni.showToast({
    title: '地址选择功能开发中',
    icon: 'none'
  })
}

// 提交订单
const submitOrder = async () => {
  if (!address.value.name) {
    uni.showToast({
      title: '请选择收货地址',
      icon: 'none'
    })
    return
  }

  try {
    const orderData = {
      addressId: 1,
      remark: remark.value,
      items: items.value.map(item => ({
        dishId: item.id,
        quantity: item.quantity
      }))
    }

    const res = await createOrder(orderData)
    
    if (res.data) {
      uni.showToast({
        title: '订单提交成功',
        icon: 'success'
      })
      
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/order/detail?id=${res.data.id}`
        })
      }, 1500)
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    uni.showToast({
      title: '订单提交失败',
      icon: 'none'
    })
  }
}

onLoad((options) => {
  if (options.items) {
    try {
      items.value = JSON.parse(decodeURIComponent(options.items))
    } catch (error) {
      console.error('解析商品数据失败:', error)
    }
  } else if (options.dishId && options.quantity) {
    // 从详情页直接购买
    items.value = [{
      id: parseInt(options.dishId),
      name: '商品',
      price: 38,
      quantity: parseInt(options.quantity),
      image: 'https://dummyimage.com/200x200/ff6b6b/ffffff&text=商品'
    }]
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

.address-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.address-info {
  flex: 1;
}

.header {
  display: flex;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.phone {
  font-size: 28rpx;
  color: #8b8fa3;
}

.detail {
  font-size: 28rpx;
  color: #8b8fa3;
  line-height: 1.6;
}

.no-address {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20rpx;
  
  .icon {
    font-size: 40rpx;
  }
  
  .text {
    font-size: 28rpx;
    color: #8b8fa3;
  }
}

.arrow {
  font-size: 32rpx;
  color: #8b8fa3;
  margin-left: 20rpx;
}

.items-card,
.remark-card,
.fee-card {
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

.label {
  display: block;
  font-size: 28rpx;
  color: #fff;
  margin-bottom: 16rpx;
}

.textarea {
  width: 100%;
  min-height: 150rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #fff;
}

.fee-row {
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
    margin: 0;
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
  justify-content: space-between;
  align-items: center;
}

.total {
  display: flex;
  flex-direction: column;
  
  .label {
    font-size: 24rpx;
    color: #8b8fa3;
    margin: 0;
  }
  
  .price {
    font-size: 36rpx;
    font-weight: 700;
    color: #14b8ff;
  }
}

.btn-submit {
  padding: 24rpx 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40rpx;
  
  text {
    font-size: 28rpx;
    color: #fff;
    font-weight: 600;
  }
}
</style>
