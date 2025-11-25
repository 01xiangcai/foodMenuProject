<template>
  <view class="page">
    <scroll-view class="detail-scroll" scroll-y>
      <!-- 1. 图片卡片 -->
      <view class="card image-card">
        <image class="dish-image" :src="dish.image" mode="aspectFill" />
      </view>

      <!-- 2. 信息卡片 -->
      <view class="card info-card">
        <view class="info-header">
          <text class="dish-name">{{ dish.name }}</text>
          <view class="price-row">
            <text class="price">¥{{ dish.price }}</text>
            <text class="category-tag">· {{ dish.categoryName || '家庭菜谱' }}</text>
          </view>
        </view>
        <text class="dish-desc">{{ dish.description }}</text>
      </view>

      <!-- 3. 评论卡片 -->
      <view class="card comment-card">
        <view class="comment-header">
          <text class="comment-title">家庭评论 0 条留言</text>
        </view>
        <view class="comment-empty">
          <text>第一条味觉灵感等你来写~</text>
        </view>
      </view>
      
      <!-- 底部占位 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 购物车弹窗组件 -->
    <CartPopup 
      v-model:visible="cartPopupVisible" 
      @close="cartPopupVisible = false"
    />

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="cart-icon-wrapper" @tap="toggleCartPopup">
        <image src="/static/tab-cart.png" mode="aspectFit" class="cart-icon" />
        <view class="badge" v-if="cartStore.totalCount > 0">
          <text>{{ cartStore.totalCount }}</text>
        </view>
      </view>

      <view class="btn-add-cart" @tap="addToCart">
        <text>加入购物车</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDishDetail } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { useCartStore } from '@/stores/cart'
import CartPopup from '@/components/CartPopup.vue'

const { themeConfig, loadTheme } = useTheme()
const cartStore = useCartStore()
const cartPopupVisible = ref(false)

const dish = ref({
  id: 0,
  name: '加载中...',
  description: '',
  price: 0,
  image: 'https://dummyimage.com/800x600/6366f1/ffffff&text=Loading',
  categoryName: '家庭菜谱'
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
      image: 'https://dummyimage.com/800x600/ff6b6b/ffffff&text=宫保鸡丁',
      categoryName: '川菜经典'
    }
  }
}

const addToCart = () => {
  cartStore.addToCart(dish.value)
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}

const toggleCartPopup = () => {
  if (cartStore.totalCount > 0) {
    cartPopupVisible.value = !cartPopupVisible.value
  }
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
  height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
}

.detail-scroll {
  flex: 1;
  padding: 0 24rpx;
}

/* 通用卡片样式 */
.card {
  background: v-bind('themeConfig.cardBg');
  border-radius: 24rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  margin-bottom: 24rpx;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 1. 图片卡片 */
.image-card {
  height: 460rpx;
  padding: 12rpx; /* 内边距效果 */
  
  .dish-image {
    width: 100%;
    height: 100%;
    border-radius: 16rpx;
    background-color: v-bind('themeConfig.bgSecondary');
  }
}

/* 2. 信息卡片 */
.info-card {
  padding: 32rpx;
}

.info-header {
  margin-bottom: 24rpx;
}

.dish-name {
  font-size: 44rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 16rpx;
  display: block;
  line-height: 1.2;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.price {
  font-size: 40rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary'); /* 使用主色而非红色，更显高级 */
}

.category-tag {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
}

.dish-desc {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
  line-height: 1.6;
  display: block;
}

/* 3. 评论卡片 */
.comment-card {
  padding: 32rpx;
  min-height: 200rpx;
}

.comment-header {
  margin-bottom: 30rpx;
}

.comment-title {
  font-size: 30rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
}

.comment-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
  
  text {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
  }
}

.bottom-spacer {
  height: 180rpx;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(20px);
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1px solid v-bind('themeConfig.borderColor');
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  z-index: 100;
}

.cart-icon-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: v-bind('themeConfig.inputBg');
  border-radius: 50%;
  border: 1px solid v-bind('themeConfig.borderColor');
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
    background: v-bind('themeConfig.bgSecondary');
  }
  
  .cart-icon {
    width: 50rpx;
    height: 50rpx;
  }
  
  .badge {
    position: absolute;
    top: 0;
    right: 0;
    background: #ef4444;
    color: #fff;
    font-size: 20rpx;
    padding: 4rpx 10rpx;
    border-radius: 20rpx;
    border: 2rpx solid v-bind('themeConfig.cardBg');
    min-width: 32rpx;
    text-align: center;
  }
}

.btn-add-cart {
  flex: 1;
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  padding: 24rpx 40rpx;
  border-radius: 999rpx;
  font-size: 30rpx;
  font-weight: 600;
  text-align: center;
  box-shadow: v-bind('themeConfig.shadowMedium');
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}
</style>


