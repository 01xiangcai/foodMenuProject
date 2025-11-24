<template>
  <view class="page">
    <!-- 侧边栏分类 -->
    <scroll-view class="sidebar" scroll-y>
      <view 
        class="sidebar-item"
        :class="{ active: currentCategory === item.id }"
        v-for="item in categories" 
        :key="item.id"
        @tap="selectCategory(item.id)"
      >
        <view class="active-line" v-if="currentCategory === item.id"></view>
        <image v-if="item.icon" class="category-icon" :src="item.icon" mode="aspectFit" />
        <text class="category-name">{{ item.name }}</text>
      </view>
    </scroll-view>

    <!-- 右侧菜品列表 -->
    <scroll-view class="dish-scroll" scroll-y @scrolltolower="loadMore">
      <view class="dish-list">
        <view 
          class="dish-card glass-card"
          v-for="dish in dishes" 
          :key="dish.id"
          @tap="goToDetail(dish.id)"
        >
          <image class="dish-image" :src="dish.image" mode="aspectFill" />
          <view class="dish-info">
            <view class="dish-header">
              <text class="dish-name">{{ dish.name }}</text>
              <view class="tags" v-if="dish.tags">
                <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
              </view>
            </view>
            <text class="dish-desc">{{ dish.description }}</text>
            <view class="dish-footer">
              <text class="dish-price">¥{{ dish.price }}<text class="unit">/份</text></text>
              
              <!-- 数量控制器 -->
              <view class="quantity-control" @tap.stop>
                <view 
                  class="btn-minus" 
                  v-if="getDishQuantity(dish.id) > 0"
                  @tap.stop="decreaseQuantity(dish)"
                >
                  <text>-</text>
                </view>
                <text 
                  class="quantity-text" 
                  v-if="getDishQuantity(dish.id) > 0"
                >{{ getDishQuantity(dish.id) }}</text>
                <view class="btn-plus" @tap.stop="increaseQuantity(dish)">
                  <text>+</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
      
      <!-- 没有更多 -->
      <view class="no-more" v-if="noMore && dishes.length > 0">
        <text>没有更多了</text>
      </view>
      
      <!-- 空状态 -->
      <view class="empty" v-if="!loading && dishes.length === 0">
        <text>暂无菜品</text>
      </view>
      
      <!-- 底部占位 -->
      <view class="bottom-spacer"></view>
    </scroll-view>
    
    <!-- 购物车弹窗遮罩 -->
    <view 
      class="cart-mask" 
      v-if="cartPopupVisible" 
      @tap="toggleCartPopup"
      @touchmove.stop.prevent
    ></view>

    <!-- 购物车弹窗 -->
    <view class="cart-popup glass-card" :class="{ show: cartPopupVisible }">
      <view class="popup-header">
        <text class="title">已选商品</text>
        <view class="clear-btn" @tap="clearCart">
          <text class="icon">🗑️</text>
          <text>清空</text>
        </view>
      </view>
      
      <scroll-view class="popup-scroll" scroll-y>
        <view class="popup-list">
          <view class="popup-item" v-for="item in cartList" :key="item.id">
            <image class="item-img" :src="item.image" mode="aspectFill" />
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <view class="item-price-box">
                <text class="price">¥{{ item.price }}</text>
              </view>
            </view>
            
            <!-- 复用数量控制器 -->
            <view class="quantity-control">
              <view class="btn-minus" @tap.stop="decreaseQuantity(item)">
                <text>-</text>
              </view>
              <text class="quantity-text">{{ item.quantity }}</text>
              <view class="btn-plus" @tap.stop="increaseQuantity(item)">
                <text>+</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 底部悬浮购物车 -->
    <view class="cart-bar-container" :class="{ show: totalCount > 0 }">
      <view class="cart-bar" @tap="toggleCartPopup">
        <view class="cart-icon-wrapper">
          <view class="cart-icon">
            <image src="/static/tab-cart.png" mode="aspectFit" class="icon-img" />
            <view class="badge" v-if="totalCount > 0">
              <text>{{ totalCount }}</text>
            </view>
          </view>
        </view>
        
        <view class="cart-info">
          <text class="total-price">¥{{ totalPrice }}</text>
          <text class="delivery-tip">免配送费</text>
        </view>
        
        <view class="checkout-btn" @tap.stop="goToCheckout">
          <text>去结算</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCategoryList, getDishList } from '@/api/index'
import { useTheme } from '@/stores/theme'

// 使用主题
const { themeConfig, loadTheme } = useTheme()

const currentCategory = ref(0)
const categories = ref([
  { id: 0, name: '全部', icon: '/static/icons/all.png' }
])
const dishes = ref([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 购物车数据：{ dishId: quantity }
const cart = ref({})
const cartPopupVisible = ref(false)

// 计算购物车总数量
const totalCount = computed(() => {
  return Object.values(cart.value).reduce((sum, count) => sum + count, 0)
})

// 计算购物车总金额
const totalPrice = computed(() => {
  let total = 0
  // 遍历购物车中的商品ID
  Object.keys(cart.value).forEach(id => {
    const count = cart.value[id]
    if (count > 0) {
      // 在当前已加载的菜品中查找价格
      const dish = dishes.value.find(d => d.id == id)
      if (dish) {
        total += dish.price * count
      }
    }
  })
  return total.toFixed(2)
})

// 购物车列表详情
const cartList = computed(() => {
  return Object.keys(cart.value).map(id => {
    const dish = dishes.value.find(d => d.id == id)
    return dish ? { ...dish, quantity: cart.value[id] } : null
  }).filter(item => item && item.quantity > 0)
})

// 获取单个菜品的数量
const getDishQuantity = (dishId) => {
  return cart.value[dishId] || 0
}

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.data && res.data.length > 0) {
      categories.value = [
        { id: 0, name: '全部', icon: '' },
        ...res.data.map(item => ({
          ...item,
          icon: item.icon || ''
        }))
      ]
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载菜品
const loadDishes = async (reset = false) => {
  if (loading.value) return
  
  if (reset) {
    page.value = 1
    dishes.value = []
    noMore.value = false
  }
  
  loading.value = true
  
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    
    if (currentCategory.value !== 0) {
      params.categoryId = currentCategory.value
    }
    
    const res = await getDishList(params)
    let list = []
    if (Array.isArray(res.data)) {
      list = res.data
    } else if (res.data?.records) {
      list = res.data.records
    }
    
    if (reset) {
      dishes.value = list
    } else {
      dishes.value = [...dishes.value, ...list]
    }
    
    if (list.length < pageSize.value) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载菜品失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  currentCategory.value = categoryId
  loadDishes(true)
}

// 加载更多
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadDishes()
  }
}

// 跳转详情
const goToDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${dishId}`
  })
}

// 增加数量
const increaseQuantity = (dish) => {
  const currentQty = cart.value[dish.id] || 0
  cart.value[dish.id] = currentQty + 1
  uni.vibrateShort()
}

// 减少数量
const decreaseQuantity = (dish) => {
  const currentQty = cart.value[dish.id] || 0
  if (currentQty > 0) {
    cart.value[dish.id] = currentQty - 1
    if (cart.value[dish.id] === 0) {
      delete cart.value[dish.id]
      // 如果购物车为空，自动关闭弹窗
      if (totalCount.value === 0) {
        cartPopupVisible.value = false
      }
    }
    uni.vibrateShort()
  }
}

// 切换购物车弹窗
const toggleCartPopup = () => {
  if (totalCount.value > 0) {
    cartPopupVisible.value = !cartPopupVisible.value
  }
}

// 清空购物车
const clearCart = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空购物车吗？',
    success: (res) => {
      if (res.confirm) {
        cart.value = {}
        cartPopupVisible.value = false
      }
    }
  })
}

// 去结算
const goToCheckout = () => {
  if (totalCount.value === 0) return
  uni.navigateTo({
    url: '/pages/order/confirm'
  })
}

onMounted(() => {
  loadTheme()
  loadCategories()
  loadDishes(true)
})
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  overflow: hidden;
  transition: background-color 0.3s ease;
}

/* 侧边栏样式 */
.sidebar {
  width: 180rpx;
  height: 100%;
  background: v-bind('themeConfig.bgSecondary');
  backdrop-filter: blur(10px);
  border-right: 1px solid v-bind('themeConfig.borderColor');
  transition: all 0.3s ease;
}

.sidebar-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30rpx 10rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: all 0.3s ease;
  
  &.active {
    background: v-bind('themeConfig.inputBg');
    color: v-bind('themeConfig.textPrimary');
    font-weight: 600;
  }
}

.active-line {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6rpx;
  height: 40rpx;
  background: v-bind('themeConfig.primaryGradient');
  border-radius: 0 6rpx 6rpx 0;
}

.category-icon {
  width: 60rpx;
  height: 60rpx;
  margin-bottom: 10rpx;
}

.category-name {
  font-size: 26rpx;
  text-align: center;
}

/* 右侧列表样式 */
.dish-scroll {
  flex: 1;
  height: 100%;
  padding: 20rpx;
  background-color: v-bind('themeConfig.bgPrimary');
  transition: background-color 0.3s ease;
}

.dish-list {
  padding-bottom: 40rpx;
}

.dish-card {
  display: flex;
  padding: 20rpx;
  margin-bottom: 20rpx;
  background: v-bind('themeConfig.cardBg');
  border-radius: 16rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.dish-name {
  font-size: 30rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 8rpx;
  transition: color 0.3s ease;
}

.tags {
  display: flex;
  gap: 8rpx;
}

.tag {
  font-size: 20rpx;
  color: v-bind('themeConfig.primaryColor');
  background: v-bind('themeConfig.primaryColor + "1a"');
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  transition: all 0.3s ease;
}

.dish-desc {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2; /* 标准属性 */
  overflow: hidden;
  transition: color 0.3s ease;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 36rpx;
  font-weight: 700;
  color: v-bind('themeConfig.errorColor');
  transition: color 0.3s ease;
  
  .unit {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
    font-weight: normal;
    margin-left: 4rpx;
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

.loading,
.no-more,
.empty {
  padding: 40rpx;
  text-align: center;
  color: v-bind('themeConfig.textSecondary');
  font-size: 28rpx;
  transition: color 0.3s ease;
}

.bottom-spacer {
  height: 140rpx; /* 为底部悬浮栏留出空间 */
}

/* 购物车弹窗样式 */
.cart-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 90;
  animation: fadeIn 0.3s ease;
}

.cart-popup {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  /* 在悬浮栏上方 */
  bottom: calc(var(--window-bottom) + 140rpx);
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(20px);
  border-radius: 32rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowHeavy');
  z-index: 95;
  transform: translateY(100vh); /* 默认隐藏到屏幕外 */
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  max-height: 60vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  visibility: hidden; /* 默认不可见，防止遮挡 */
  
  &.show {
    transform: translateY(0);
    visibility: visible; /* 显示时可见 */
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
        font-weight: 600;
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

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 底部悬浮购物车栏 */
.cart-bar-container {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  /* 抬高位置，避开TabBar */
  bottom: calc(var(--window-bottom) + 20rpx); 
  z-index: 100;
  transform: translateY(200%); /* 默认隐藏 */
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.show {
    transform: translateY(0);
  }
}

.cart-bar {
  display: flex;
  align-items: center;
  height: 100rpx;
  padding: 0 30rpx;
  border-radius: 50rpx;
  /* 强制使用深色背景，确保内容清晰可见 */
  background: #1a1a1a !important; 
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  
  .cart-icon-wrapper {
    position: relative;
    margin-right: 30rpx;
    
    .cart-icon {
      width: 80rpx;
      height: 80rpx;
      margin-top: -40rpx; /* 图标稍微突出一点 */
      background: #2c2c2c;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 4rpx solid #1a1a1a;
      box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.3);
      
      .icon-img {
        width: 44rpx;
        height: 44rpx;
        /* 如果图标是黑色的，可以使用滤镜反转为白色，或者直接使用白色图标 */
        filter: invert(1); 
      }
      
      .badge {
        position: absolute;
        top: -6rpx;
        right: -6rpx;
        background: #ff3b30;
        color: #fff;
        font-size: 20rpx;
        min-width: 36rpx;
        height: 36rpx;
        border-radius: 18rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 8rpx;
        border: 2rpx solid #fff;
        box-shadow: 0 2rpx 5rpx rgba(0,0,0,0.2);
      }
    }
  }
  
  .cart-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .total-price {
      font-size: 36rpx;
      font-weight: 700;
      color: #ffffff; /* 强制白色，配合深色背景 */
    }
    
    .delivery-tip {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.6);
    }
  }
  
  .checkout-btn {
    padding: 16rpx 40rpx;
    background: v-bind('themeConfig.primaryGradient');
    border-radius: 40rpx;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.2);
    
    text {
      color: #fff;
      font-size: 28rpx;
      font-weight: 600;
    }
    
    &:active {
      opacity: 0.9;
      transform: scale(0.95);
    }
  }
}

// 玻璃拟态卡片样式
.glass-card {
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
}
</style>
