<template>
  <view class="page">
    <!-- 订单详情卡片 -->
    <view class="card">
      <view class="card-header">
        <text class="card-title">订单详情</text>
      </view>
      
      <view class="order-items">
        <view class="order-item" v-for="item in items" :key="item.id">
          <image 
            class="item-image" 
            :src="item.image" 
            mode="aspectFill" 
            @error="handleImageError(item)"
          />
          <view class="item-content">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-price">¥{{ item.price }}</text>
          </view>
          <text class="item-quantity">x{{ item.quantity }}</text>
        </view>
      </view>

      <view class="divider"></view>

      <view class="summary-row">
        <text class="label">小计</text>
        <text class="value">¥{{ goodsAmount }}</text>
      </view>
    </view>

    <!-- 备注卡片 -->
    <view class="card">
      <view class="card-header">
        <text class="card-title">备注</text>
      </view>
      <view class="textarea-wrapper">
        <textarea 
          class="textarea" 
          placeholder="口味偏好、餐具数量等" 
          placeholder-style="color: rgba(255, 255, 255, 0.5);"
          v-model="remark"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <view class="total-info">
        <text class="label">合计:</text>
        <text class="price">¥{{ goodsAmount }}</text>
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
import { useTheme } from '@/stores/theme'

const { themeConfig } = useTheme()



const items = ref([])
const remark = ref('')

// 商品金额
const goodsAmount = computed(() => {
  return items.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0).toFixed(2)
})

// 设计图中合计与小计一致，这里 totalAmount 直接使用 goodsAmount 即可
const totalAmount = computed(() => goodsAmount.value)



// 处理图片加载错误
const handleImageError = (item) => {
  item.image = '/static/logo.png' // 使用默认图片
}

// 提交订单
const submitOrder = async () => {


  try {
    const orderData = {
      // 后端当前暂未使用地址字段，这里先保留占位
      remark: remark.value,
      orderItems: items.value.map(item => ({
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
          url: `/pages/order/detail?id=${res.data}`
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
  background-color: #111526; /* 深蓝背景 */
  padding: 24rpx;
  padding-bottom: 180rpx;
  box-sizing: border-box;
}

.card {
  background-color: #1e2235; /* 卡片背景 */
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.card-header {
  margin-bottom: 30rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.item-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  background-color: #2a2f45;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8rpx;
}

.item-name {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 500;
}

.item-price {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 600;
}

.item-quantity {
  font-size: 28rpx;
  color: #ffffff;
  margin-left: 20rpx;
}

.divider {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.1);
  margin: 30rpx 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-row .label {
  font-size: 30rpx;
  color: #ffffff;
  font-weight: 600;
}

.summary-row .value {
  font-size: 36rpx;
  color: #ffffff;
  font-weight: 700;
}

.textarea-wrapper {
  background-color: #2a2f45;
  border-radius: 12rpx;
  padding: 20rpx;
}

.textarea {
  width: 100%;
  height: 160rpx;
  font-size: 28rpx;
  color: #ffffff;
  line-height: 1.5;
}

.bottom-bar {
  position: fixed;
  bottom: 40rpx;
  left: 24rpx;
  right: 24rpx;
  height: 110rpx;
  background-color: #1e2235;
  border-radius: 55rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10rpx 0 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.05);
  z-index: 100;
}

.total-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.total-info .label {
  font-size: 30rpx;
  color: #ffffff;
  font-weight: 600;
}

.total-info .price {
  font-size: 36rpx;
  color: #00e5ff; /* 青色高亮 */
  font-weight: 700;
}

.btn-submit {
  width: 240rpx;
  height: 90rpx;
  background: linear-gradient(90deg, #66ffff 0%, #ff99ff 100%);
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 30rpx;
    color: #ffffff;
    font-weight: 600;
  }
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}
</style>
