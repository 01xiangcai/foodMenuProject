<template>
  <view class="page">
    <!-- 购物车列表 -->
    <scroll-view class="cart-scroll" scroll-y v-if="cartItems.length > 0">
      <view class="cart-list">
        <view class="cart-item glass-card" v-for="item in cartItems" :key="item.id">
          <!-- 选择框 -->
          <view class="checkbox" :class="{ checked: item.checked }" @tap="toggleCheck(item.id)">
            <text v-if="item.checked">✓</text>
          </view>

          <!-- 商品信息 -->
          <image class="item-image" :src="item.image" mode="aspectFill" />
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-price">¥{{ item.price }}</text>
          </view>

          <!-- 数量控制 -->
          <view class="quantity-control">
            <view class="btn" @tap="decreaseQuantity(item.id)">
              <text>-</text>
            </view>
            <text class="quantity">{{ item.quantity }}</text>
            <view class="btn" @tap="increaseQuantity(item.id)">
              <text>+</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 空状态 -->
    <view class="empty" v-else>
      <text class="icon">🛒</text>
      <text class="text">购物车是空的</text>
      <view class="btn-go-shop" @tap="goToMenu">
        <text>去点餐</text>
      </view>
    </view>

    <!-- 底部结算栏 -->
    <view class="bottom-bar" v-if="cartItems.length > 0">
      <view class="select-all" @tap="toggleSelectAll">
        <view class="checkbox" :class="{ checked: isAllChecked }">
          <text v-if="isAllChecked">✓</text>
        </view>
        <text class="label">全选</text>
      </view>

      <view class="total-info">
        <view class="total">
          <text class="label">合计：</text>
          <text class="price">¥{{ totalPrice }}</text>
        </view>
        <view class="btn-checkout" @tap="checkout">
          <text>结算({{ checkedCount }})</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const cartItems = ref([
  {
    id: 1,
    name: '宫保鸡丁',
    price: 38,
    quantity: 1,
    checked: true,
    image: 'https://dummyimage.com/200x200/ff6b6b/ffffff&text=宫保鸡丁'
  },
  {
    id: 2,
    name: '红烧肉',
    price: 48,
    quantity: 2,
    checked: true,
    image: 'https://dummyimage.com/200x200/4ecdc4/ffffff&text=红烧肉'
  }
])

// 全选状态
const isAllChecked = computed(() => {
  return cartItems.value.length > 0 && cartItems.value.every(item => item.checked)
})

// 已选数量
const checkedCount = computed(() => {
  return cartItems.value.filter(item => item.checked).length
})

// 总价
const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.checked)
    .reduce((total, item) => total + item.price * item.quantity, 0)
    .toFixed(2)
})

// 切换选中状态
const toggleCheck = (id) => {
  const item = cartItems.value.find(item => item.id === id)
  if (item) {
    item.checked = !item.checked
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  const newChecked = !isAllChecked.value
  cartItems.value.forEach(item => {
    item.checked = newChecked
  })
}

// 增加数量
const increaseQuantity = (id) => {
  const item = cartItems.value.find(item => item.id === id)
  if (item) {
    item.quantity++
  }
}

// 减少数量
const decreaseQuantity = (id) => {
  const item = cartItems.value.find(item => item.id === id)
  if (item && item.quantity > 1) {
    item.quantity--
  } else if (item && item.quantity === 1) {
    // 删除商品
    uni.showModal({
      title: '提示',
      content: '确定要删除该商品吗？',
      success: (res) => {
        if (res.confirm) {
          const index = cartItems.value.findIndex(item => item.id === id)
          if (index > -1) {
            cartItems.value.splice(index, 1)
          }
        }
      }
    })
  }
}

// 去结算
const checkout = () => {
  if (checkedCount.value === 0) {
    uni.showToast({
      title: '请选择商品',
      icon: 'none'
    })
    return
  }

  const selectedItems = cartItems.value.filter(item => item.checked)
  uni.navigateTo({
    url: '/pages/order/confirm?items=' + encodeURIComponent(JSON.stringify(selectedItems))
  })
}

// 去点餐
const goToMenu = () => {
  uni.switchTab({
    url: '/pages/menu/menu'
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #050a1f;
  padding-bottom: 160rpx;
}

.cart-scroll {
  height: calc(100vh - 160rpx);
}

.cart-list {
  padding: 20rpx;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  margin-bottom: 20rpx;
  gap: 20rpx;
}

.checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  &.checked {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-color: transparent;
  }
  
  text {
    font-size: 24rpx;
    color: #fff;
  }
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.item-name {
  font-size: 28rpx;
  color: #fff;
}

.item-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #14b8ff;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-shrink: 0;
  
  .btn {
    width: 50rpx;
    height: 50rpx;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    text {
      font-size: 28rpx;
      color: #fff;
    }
  }
  
  .quantity {
    font-size: 28rpx;
    color: #fff;
    min-width: 40rpx;
    text-align: center;
  }
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  
  .icon {
    font-size: 120rpx;
    margin-bottom: 30rpx;
  }
  
  .text {
    font-size: 32rpx;
    color: #8b8fa3;
    margin-bottom: 40rpx;
  }
}

.btn-go-shop {
  padding: 24rpx 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40rpx;
  color: #fff;
  font-size: 28rpx;
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

.select-all {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .label {
    font-size: 28rpx;
    color: #fff;
  }
}

.total-info {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.total {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  
  .label {
    font-size: 24rpx;
    color: #8b8fa3;
  }
  
  .price {
    font-size: 36rpx;
    font-weight: 700;
    color: #14b8ff;
  }
}

.btn-checkout {
  padding: 24rpx 50rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40rpx;
  
  text {
    font-size: 28rpx;
    color: #fff;
    font-weight: 600;
  }
}
</style>
