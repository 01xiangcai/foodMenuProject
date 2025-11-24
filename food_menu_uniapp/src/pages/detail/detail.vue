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
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDishDetail } from '@/api/index'
import { useTheme } from '@/stores/theme'

const { themeConfig, loadTheme } = useTheme()

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

onMounted(() => {
  loadTheme()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding-bottom: 160rpx;
  transition: background-color 0.3s ease;
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
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.9);
  }
  
  text {
    font-size: 40rpx;
    color: #fff;
  }
}

.info-section {
  margin: 20rpx;
  padding: 30rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
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
  color: v-bind('themeConfig.textPrimary');
  transition: color 0.3s ease;
}

.dish-price {
  font-size: 48rpx;
  font-weight: 700;
  color: v-bind('themeConfig.errorColor');
  transition: color 0.3s ease;
}

.dish-desc {
  display: block;
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
  line-height: 1.6;
  margin-bottom: 30rpx;
  transition: color 0.3s ease;
}

.quantity-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 30rpx;
  border-top: 1px solid v-bind('themeConfig.borderColor');
  
  .label {
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
  }
  
  .selector {
    display: flex;
    align-items: center;
    gap: 30rpx;
    
    .btn {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
      
      &:first-child {
        background: transparent;
        border: 1px solid v-bind('themeConfig.textSecondary');
        color: v-bind('themeConfig.textSecondary');
      }
      
      &:last-child {
        background: v-bind('themeConfig.primaryGradient');
        border: none;
        color: #fff;
        box-shadow: v-bind('themeConfig.shadowLight');
      }
      
      &:active {
        transform: scale(0.9);
      }
      
      text {
        font-size: 32rpx;
      }
    }
    
    .quantity {
      font-size: 36rpx;
      font-weight: 600;
      color: v-bind('themeConfig.textPrimary');
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
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: v-bind('themeConfig.bgSecondary');
  backdrop-filter: blur(20px);
  border-top: 1px solid v-bind('themeConfig.borderColor');
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  z-index: 100;
}

.total {
  .label {
    display: block;
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
    margin-bottom: 8rpx;
  }
  
  .price {
    font-size: 40rpx;
    font-weight: 700;
    color: v-bind('themeConfig.errorColor');
  }
}

.actions {
  display: flex;
  gap: 20rpx;
}

.btn-cart,
.btn-buy {
  padding: 20rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
    opacity: 0.9;
  }
}

.btn-cart {
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  color: v-bind('themeConfig.textPrimary');
}

.btn-buy {
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  box-shadow: v-bind('themeConfig.shadowMedium');
}
</style>
