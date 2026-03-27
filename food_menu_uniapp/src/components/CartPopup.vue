<template>
  <view>
    <!-- 购物车弹窗遮罩 -->
    <view 
      class="cart-mask" 
      v-if="visible" 
      @tap="close"
      @touchmove.stop.prevent
    ></view>

    <!-- 购物车弹窗 -->
    <view class="cart-popup glass-card" :class="{ show: visible }">
      <view class="popup-header">
        <text class="title">已选商品</text>
        <view class="clear-btn" @tap="handleClear">
          <text class="icon">🗑️</text>
          <text>清空</text>
        </view>
      </view>
      
      <scroll-view class="popup-scroll" scroll-y>
        <view class="popup-list">
          <view class="popup-item" v-for="item in cartStore.cartList" :key="item.id">
            <image class="item-img" :src="getDishImage(item, true)" mode="aspectFill" lazy-load />
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <view class="item-price-box">
                <text class="price">¥{{ item.price }}</text>
              </view>
            </view>
            
            <!-- 数量控制器 -->
            <view class="quantity-control">
              <view class="btn-minus" @tap.stop="cartStore.removeFromCart(item)">
                <text>-</text>
              </view>
              <text class="quantity-text">{{ item.quantity }}</text>
              <view class="btn-plus" @tap.stop="cartStore.addToCart(item)">
                <text>+</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { useCartStore } from '@/stores/cart'
import { getDishImage } from '@/utils/image'
import { useTheme } from '@/stores/theme'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'close'])

const cartStore = useCartStore()
const { themeConfig } = useTheme()

const close = () => {
  emit('update:visible', false)
  emit('close')
}

const handleClear = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空购物车吗？',
    success: (res) => {
      if (res.confirm) {
        cartStore.clearCart()
        close()
      }
    }
  })
}
</script>

<style lang="scss" scoped>
/* 购物车弹窗样式 */
.cart-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 900; /* 提高层级，确保在最上层 */
  animation: fadeIn 0.3s ease;
}

.cart-popup {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  /* 距离底部一定距离，避免被底部栏遮挡，或者根据需要调整 */
  bottom: calc(var(--window-bottom) + 140rpx);
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(20px);
  border-radius: 32rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowHeavy');
  z-index: 950;
  transform: translateY(100vh);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  max-height: 60vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  visibility: hidden;
  
  &.show {
    transform: translateY(0);
    visibility: visible;
  }
  
  .popup-header {
    padding: 30rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid v-bind('themeConfig.borderColor');
    
    .title {
      font-size: 32rpx;
      font-weight: 700;
      color: v-bind('themeConfig.textPrimary');
    }
    
    .clear-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      .icon {
        font-size: 28rpx;
      }
      
      text {
        font-size: 26rpx;
        color: v-bind('themeConfig.textSecondary');
      }
    }
  }
  
  .popup-scroll {
    max-height: 500rpx;
  }
  
  .popup-list {
    padding: 20rpx;
  }
  
  .popup-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    margin-bottom: 16rpx;
    
    .item-img {
      width: 100rpx;
      height: 100rpx;
      border-radius: 12rpx;
      margin-right: 20rpx;
    }
    
    .item-info {
      flex: 1;
      
      .item-name {
        font-size: 28rpx;
        color: v-bind('themeConfig.textPrimary');
        margin-bottom: 8rpx;
        display: block;
      }
      
      .price {
        font-size: 32rpx;
        font-weight: 700;
        color: v-bind('themeConfig.errorColor');
      }
    }
  }
}

/* 数量控制器样式 */
.quantity-control {
  display: flex;
  align-items: center;
  
  .btn-minus,
  .btn-plus {
    width: 50rpx;
    height: 50rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:active {
      transform: scale(0.9);
    }
    
    text {
      font-size: 32rpx;
      line-height: 1;
      margin-top: -4rpx;
    }
  }
  
  .btn-minus {
    background: transparent;
    border: 1px solid v-bind('themeConfig.textSecondary');
    
    text {
      color: v-bind('themeConfig.textSecondary');
    }
  }
  
  .btn-plus {
    background: v-bind('themeConfig.primaryGradient');
    box-shadow: v-bind('themeConfig.shadowLight');
    border: none;
    
    text {
      color: #fff;
    }
  }
  
  .quantity-text {
    font-size: 30rpx;
    color: v-bind('themeConfig.textPrimary');
    margin: 0 20rpx;
    min-width: 40rpx;
    text-align: center;
    font-weight: 600;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
