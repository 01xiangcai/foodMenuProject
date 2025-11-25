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
          <view class="dish-media">
            <image class="dish-image" :src="dish.image" mode="aspectFill" />
            <view 
              class="favorite-btn" 
              :class="{ active: dish.isFavorite }"
              @tap.stop="toggleFavorite(dish)"
            >
              <text class="heart">{{ dish.isFavorite ? '❤' : '♡' }}</text>
            </view>
          </view>
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
                  v-if="cartStore.getDishQuantity(dish.id) > 0"
                  @tap.stop="cartStore.removeFromCart(dish)"
                >
                  <text>-</text>
                </view>
                <text 
                  class="quantity-text" 
                  v-if="cartStore.getDishQuantity(dish.id) > 0"
                >{{ cartStore.getDishQuantity(dish.id) }}</text>
                <view class="btn-plus" @tap.stop="cartStore.addToCart(dish)">
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
    
    <!-- 购物车弹窗组件 -->
    <CartPopup 
      v-model:visible="cartPopupVisible" 
      @close="cartPopupVisible = false"
    />
    
    <!-- 底部悬浮购物车 -->
    <view class="cart-bar-container" :class="{ show: cartStore.totalCount > 0 }">
      <view class="cart-bar" @tap="toggleCartPopup">
        <view class="cart-icon-wrapper">
          <view class="cart-icon">
            <image src="/static/tab-cart.png" mode="aspectFit" class="icon-img" />
            <view class="badge" v-if="cartStore.totalCount > 0">
              <text>{{ cartStore.totalCount }}</text>
            </view>
          </view>
        </view>
        
        <view class="cart-info">
          <text class="total-price">¥{{ cartStore.totalPrice }}</text>
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
import { ref, onMounted } from 'vue'
import { getCategoryList, getDishList, addFavorite, removeFavorite, checkFavoriteBatch } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { useCartStore } from '@/stores/cart'
import CartPopup from '@/components/CartPopup.vue'

// 使用主题
const { themeConfig, loadTheme } = useTheme()
const cartStore = useCartStore()

const currentCategory = ref(0)
const categories = ref([
  { id: 0, name: '全部', icon: '/static/icons/all.png' }
])
const dishes = ref([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = ref(10)
const favoriteIds = ref(new Set())
const cartPopupVisible = ref(false)

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

    list = list.map(item => ({
      ...item,
      isFavorite: favoriteIds.value.has(item.id)
    }))
    
    // 更新 Store 中的缓存
    cartStore.updateDishCache(list)
    
    if (reset) {
      dishes.value = list
    } else {
      dishes.value = [...dishes.value, ...list]
    }
    
    if (list.length < pageSize.value) {
      noMore.value = true
    }

    await syncFavoritesForCurrentList()
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

// 切换购物车弹窗
const toggleCartPopup = () => {
  if (cartStore.totalCount > 0) {
    cartPopupVisible.value = !cartPopupVisible.value
  }
}

// 去结算
const goToCheckout = () => {
  if (cartStore.totalCount === 0) return

  // 将当前购物车商品作为 items 传递给确认订单页
  const selectedItems = cartStore.cartList
  uni.navigateTo({
    url: '/pages/order/confirm?items=' + encodeURIComponent(JSON.stringify(selectedItems))
  })
}

const ensureLogin = () => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    uni.showModal({
      title: '提示',
      content: '登录后才能收藏菜品，是否立即前往登录？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({
            url: '/pages/login/login'
          })
        }
      }
    })
    return false
  }
  return true
}

const updateFavoriteMark = () => {
  dishes.value = dishes.value.map(dish => ({
    ...dish,
    isFavorite: favoriteIds.value.has(dish.id)
  }))
}

const syncFavoritesForCurrentList = async () => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    favoriteIds.value = new Set()
    updateFavoriteMark()
    return
  }
  const ids = dishes.value.map(d => d.id)
  if (!ids.length) return
  try {
    const res = await checkFavoriteBatch(ids)
    const set = new Set(res.data || [])
    favoriteIds.value = set
    updateFavoriteMark()
  } catch (error) {
    console.error('批量获取收藏状态失败:', error)
  }
}

const toggleFavorite = async (dish) => {
  if (!ensureLogin()) return
  const isFavorite = !!dish.isFavorite
  try {
    if (isFavorite) {
      await removeFavorite(dish.id)
      const nextSet = new Set(favoriteIds.value)
      nextSet.delete(dish.id)
      favoriteIds.value = nextSet
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      await addFavorite(dish.id)
      const nextSet = new Set(favoriteIds.value)
      nextSet.add(dish.id)
      favoriteIds.value = nextSet
      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })
    }
    updateFavoriteMark()
  } catch (error) {
    console.error('切换收藏状态失败:', error)
  }
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

.dish-media {
  position: relative;
  margin-right: 20rpx;
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.favorite-btn {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 54rpx;
  height: 54rpx;
  border-radius: 50%;
  background: rgba(5, 10, 31, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.25);
  transition: all 0.2s ease;
  backdrop-filter: blur(8px);
}

.favorite-btn:active {
  transform: scale(0.92);
}

.favorite-btn .heart {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
}

.favorite-btn.active {
  background: v-bind('themeConfig.primaryGradient');
  border-color: transparent;
  box-shadow: v-bind('themeConfig.shadowLight');
}

.favorite-btn.active .heart {
  color: #fff;
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

/* 底部悬浮购物车样式 */
.cart-bar-container {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: calc(var(--window-bottom) + 20rpx);
  z-index: 100;
  transform: translateY(200%);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &.show {
    transform: translateY(0);
  }
}

.cart-bar {
  display: flex;
  align-items: center;
  height: 100rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(20px);
  border-radius: 50rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowHeavy');
  padding-right: 10rpx;
}

.cart-icon-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  margin-top: -30rpx;
  margin-left: 10rpx;
}

.cart-icon {
  width: 100rpx;
  height: 100rpx;
  background: v-bind('themeConfig.cardBg');
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowMedium');
}

.icon-img {
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
  border: 2rpx solid #fff;
  min-width: 32rpx;
  text-align: center;
}

.cart-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-left: 20rpx;
}

.total-price {
  font-size: 36rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
}

.delivery-tip {
  font-size: 20rpx;
  color: v-bind('themeConfig.textSecondary');
}

.checkout-btn {
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
    opacity: 0.9;
  }
}
</style>
