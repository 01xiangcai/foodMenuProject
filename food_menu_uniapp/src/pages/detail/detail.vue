<template>
  <view class="page">
    <!-- 菜品图片 -->
    <view class="image-container">
      <image class="dish-image" :src="dish.image" mode="aspectFill" />
      <view class="back-btn" @tap="goBack">
        <text>←</text>
      </view>
    </view>

    <!-- 菜品信息 -->
    <view class="info-section glass-card">
      <view class="header">
        <text class="dish-name">{{ dish.name }}</text>
        <text class="dish-price">¥{{ dish.price }}</text>
      </view>
      <text class="dish-desc">{{ dish.description }}</text>
      
      <!-- 数量选择 -->
      <view class="quantity-selector">
        <text class="label">数量</text>
        <view class="selector">
          <view class="btn" @tap="decreaseQuantity">
            <text>-</text>
          </view>
          <text class="quantity">{{ quantity }}</text>
          <view class="btn" @tap="increaseQuantity">
            <text>+</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="total">
        <text class="label">小计</text>
        <text class="price">¥{{ totalPrice }}</text>
      </view>
      <view class="actions">
        <view class="btn-cart" @tap="addToCart">
          <text>加入购物车</text>
        </view>
        <view class="btn-buy" @tap="buyNow">
          <text>立即购买</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDishDetail } from '@/api/index'

const dish = ref({
  id: 0,
  name: '加载中...',
  description: '',
  price: 0,
  image: 'https://dummyimage.com/800x600/6366f1/ffffff&text=Loading'
})
const quantity = ref(1)

const totalPrice = computed(() => {
  return (dish.value.price * quantity.value).toFixed(2)
})

const loadDishDetail = async (id) => {
  try {
    const res = await getDishDetail(id)
    if (res.data) {
      dish.value = res.data
    }
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    // 显示默认数据
    dish.value = {
      id: id,
      name: '宫保鸡丁',
      description: '经典川菜，选用优质鸡肉，配以花生米、干辣椒等食材，口感香辣酥脆，色泽红亮，是一道深受欢迎的传统名菜。',
      price: 38,
      image: 'https://dummyimage.com/800x600/ff6b6b/ffffff&text=宫保鸡丁'
    }
  }
}

const increaseQuantity = () => {
  quantity.value++
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = () => {
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}

const buyNow = () => {
  uni.navigateTo({
    url: `/pages/order/confirm?dishId=${dish.value.id}&quantity=${quantity.value}`
  })
}

const goBack = () => {
  uni.navigateBack()
}

onLoad((options) => {
  if (options.id) {
    loadDishDetail(options.id)
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #050a1f;
  padding-bottom: 160rpx;
}

.image-container {
  position: relative;
  width: 100%;
  height: 600rpx;
}

.dish-image {
  width: 100%;
  height: 100%;
}

.back-btn {
  position: absolute;
  top: 40rpx;
  left: 20rpx;
  width: 80rpx;
  height: 80rpx;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 40rpx;
    color: #fff;
  }
}

.info-section {
  margin: 20rpx;
  padding: 30rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.dish-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
}

.dish-price {
  font-size: 48rpx;
  font-weight: 700;
  color: #14b8ff;
}

.dish-desc {
  display: block;
  font-size: 28rpx;
  color: #8b8fa3;
  line-height: 1.6;
  margin-bottom: 30rpx;
}

.quantity-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .label {
    font-size: 28rpx;
    color: #fff;
  }
  
  .selector {
    display: flex;
    align-items: center;
    gap: 30rpx;
    
    .btn {
      width: 60rpx;
      height: 60rpx;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      text {
        font-size: 32rpx;
        color: #fff;
      }
    }
    
    .quantity {
      font-size: 32rpx;
      color: #fff;
      min-width: 60rpx;
      text-align: center;
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
  .label {
    display: block;
    font-size: 24rpx;
    color: #8b8fa3;
    margin-bottom: 8rpx;
  }
  
  .price {
    font-size: 40rpx;
    font-weight: 700;
    color: #14b8ff;
  }
}

.actions {
  display: flex;
  gap: 20rpx;
}

.btn-cart,
.btn-buy {
  padding: 24rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-cart {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.btn-buy {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
</style>
