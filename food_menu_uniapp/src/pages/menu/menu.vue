<template>
  <view class="page">
    <!-- 搜索框 -->
    <view class="search-bar">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          v-model="searchKeyword" 
          placeholder="搜索菜品名称..."
          @input="onSearchInput"
          @confirm="onSearchConfirm"
          confirm-type="search"
        />
        <view 
          class="clear-btn" 
          v-if="searchKeyword"
          @tap="clearSearch"
        >
          <text>✕</text>
        </view>
      </view>
      
      <!-- 随机点餐按钮 -->
      <view class="random-btn" @tap="goToRandom">
        <text class="random-icon">🎲</text>
      </view>

      <!-- 视图切换按钮 -->
      <view class="view-toggle-btn" @tap="toggleViewMode">
        <text class="view-icon">{{ isCompactMode ? '🔲' : '☰' }}</text>
      </view>
    </view>
    
    <!-- 内容区域 -->
    <view class="page-content">
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
      <view class="dish-list" :class="{ 'compact-mode': isCompactMode }">
        <view 
          class="dish-card"
          :class="{ 'is-compact': isCompactMode }"
          v-for="dish in dishes" 
          :key="dish.id"
          @tap="goToDetail(dish.id)"
        >
          <!-- 图片区域 -->
          <view class="dish-image-wrapper">
            <image class="dish-image" :src="getDishImage(dish)" mode="aspectFill" />
            <view class="image-overlay"></view>
            <view 
              class="favorite-btn" 
              :class="{ active: dish.isFavorite }"
              @tap.stop="toggleFavorite(dish)"
            >
              <text class="heart-icon">{{ dish.isFavorite ? '❤️' : '🤍' }}</text>
            </view>
            <!-- 分类标签 -->
            <view class="category-badge" v-if="dish.categoryName">
              <text>{{ dish.categoryName }}</text>
            </view>
          </view>
          
          <!-- 信息区域 -->
          <view class="dish-content">
            <!-- 标题行 -->
            <view class="title-row">
              <text class="dish-name">{{ dish.name }}</text>
            </view>
            
            <!-- 标签行 -->
            <view class="tags-row" v-if="dish.tags && dish.tags.length">
              <view class="tag" v-for="(tag, index) in dish.tags.slice(0, isCompactMode ? 2 : 3)" :key="tag">
                <text class="tag-icon">{{ getTagIcon(tag) }}</text>
                <text class="tag-text">{{ tag }}</text>
              </view>
            </view>
            
            <!-- 描述 -->
            <text class="dish-desc">{{ dish.description }}</text>
            
            <!-- 底部操作栏 -->
            <view class="action-bar">
              <view class="price-section">
                <text class="price-symbol">¥</text>
                <text class="price-value">{{ dish.price }}</text>
                <text class="price-unit">/份</text>
              </view>
              
              <!-- 数量控制器 -->
              <view class="quantity-control" @tap.stop>
                <template v-if="isLoggedIn">
                  <view 
                    class="btn-minus" 
                    v-if="cartStore.getDishQuantity(dish.id) > 0"
                    @tap.stop="cartStore.removeFromCart(dish)"
                  >
                    <text>−</text>
                  </view>
                  <text 
                    class="quantity-num" 
                    v-if="cartStore.getDishQuantity(dish.id) > 0"
                  >{{ cartStore.getDishQuantity(dish.id) }}</text>
                  <view class="btn-plus" @tap.stop="cartStore.addToCart(dish)">
                    <text>+</text>
                  </view>
                </template>
                <view class="login-tip" v-else @tap.stop="goToLogin">
                  <text>登录点餐</text>
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
    </view>
    
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
import { ref } from 'vue'
import { getCategoryList, getDishList, addFavorite, removeFavorite, checkFavoriteBatch, getTagIconMap } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { useCartStore } from '@/stores/cart'
import CartPopup from '@/components/CartPopup.vue'
import { getDishImage } from '@/utils/image'

// 使用主题
const { themeConfig, loadTheme } = useTheme()
const cartStore = useCartStore()

const isLoggedIn = ref(false)

const checkLoginStatus = () => {
  const token = uni.getStorageSync('fm_token')
  isLoggedIn.value = !!token
}

const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/login/login'
  })
}

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
const tagIconMap = ref({})
const searchKeyword = ref('')
const isCompactMode = ref(false)
let searchTimer = null

// 切换视图模式
const toggleViewMode = () => {
  isCompactMode.value = !isCompactMode.value
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
    
    // 添加搜索关键词
    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.name = searchKeyword.value.trim()
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
      isFavorite: favoriteIds.value.has(item.id),
      tags: item.tags && typeof item.tags === 'string' ? item.tags.split(/[,，]/).filter(Boolean) : []
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
  // 选择分类时不清空搜索关键词，但重新加载数据
  loadDishes(true)
}

// 搜索输入处理（防抖）
const onSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  // 延迟500ms后执行搜索
  searchTimer = setTimeout(() => {
    if (searchKeyword.value.trim()) {
      loadDishes(true)
    } else {
      // 如果搜索框为空，重新加载所有菜品
      loadDishes(true)
    }
  }, 500)
}

// 搜索确认
const onSearchConfirm = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  loadDishes(true)
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
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
  } else {
    uni.showToast({
      title: '购物车是空的，快去选购吧~',
      icon: 'none',
      duration: 2000
    })
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

// 加载标签图标映射
const loadTagIconMap = async () => {
  try {
    const res = await getTagIconMap()
    if (res.data) {
      tagIconMap.value = res.data
    }
  } catch (error) {
    console.error('加载标签图标映射失败:', error)
    // 如果加载失败，使用默认图标
    tagIconMap.value = {}
  }
}

// 获取标签图标
const getTagIcon = (tag) => {
  return tagIconMap.value[tag] || '🔸'
}

// 跳转到随机点餐页面
const goToRandom = () => {
  uni.navigateTo({
    url: '/pages/random/random'
  })
}


import { onShow } from '@dcloudio/uni-app'

onShow(() => {
  loadTheme()
  checkLoginStatus()
  loadTagIconMap()
  loadCategories()
  loadDishes(true)
})
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  overflow: hidden;
  transition: background-color 0.3s ease;
}

/* 搜索框样式 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: v-bind('themeConfig.cardBg');
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  transition: all 0.3s ease;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  flex: 1;
  align-items: center;
  background: v-bind('themeConfig.inputBg');
  border-radius: 50rpx;
  padding: 0 30rpx;
  height: 80rpx;
  border: 1px solid v-bind('themeConfig.borderColor');
  transition: all 0.3s ease;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
  color: v-bind('themeConfig.textSecondary');
  transition: color 0.3s ease;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  background: transparent;
  border: none;
  outline: none;
  transition: color 0.3s ease;
  
  &::placeholder {
    color: v-bind('themeConfig.textSecondary');
  }
}

.clear-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: v-bind('themeConfig.bgTertiary');
  margin-left: 16rpx;
  transition: all 0.3s ease;
  
  text {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  &:active {
    background: v-bind('themeConfig.borderColor');
    transform: scale(0.9);
  }
}

/* 随机点餐按钮 */
.random-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.3);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.9);
  }
}

.random-icon {
  font-size: 40rpx;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

/* 视图切换按钮 */
.view-toggle-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: v-bind('themeConfig.bgSecondary');
  border-radius: 50%;
  border: 1px solid v-bind('themeConfig.borderColor');
  margin-left: 10rpx; /* 与随机按钮保持一点距离 */
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.9);
    background: v-bind('themeConfig.inputBg');
  }
}

.view-icon {
  font-size: 36rpx;
  color: v-bind('themeConfig.textPrimary');
}


.page-content {
  display: flex;
  flex: 1;
  overflow: hidden;
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

/* 卡片样式适配 */
.dish-card {
  position: relative;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background: v-bind('themeConfig.cardBg');
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  backdrop-filter: blur(20px);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  flex-direction: column;

  &:active {
    transform: scale(0.98);
    box-shadow: 
      0 2px 8px rgba(0, 0, 0, 0.12),
      0 0 0 1px rgba(255, 255, 255, 0.05) inset;
  }
}

/* 紧凑模式样式 */
.dish-card.is-compact {
  flex-direction: row;
  height: auto;
  min-height: 220rpx;
  
  .dish-image-wrapper {
    width: 220rpx;
    height: 220rpx;
    flex-shrink: 0;
  }
  
  .dish-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 16rpx 20rpx;
    overflow: hidden;
    height: 220rpx; /* Constrain height to match image for alignment consistency */
    box-sizing: border-box;
  }
  
  .title-row {
    margin-bottom: 4rpx;
    flex-shrink: 0;
  }
  
  .dish-name {
    font-size: 30rpx;
  }
  
  .tags-row {
    margin-bottom: 4rpx;
    display: flex;
    flex-wrap: wrap; /* Allow partial wrapping to inspect overflow */
    overflow: hidden; /* Hide anything that overflows the container */
    max-height: 40rpx; /* Restrict to roughly one line height + padding */
    width: 100%;
    flex-shrink: 0;
    gap: 8rpx; /* Add some spacing between tags */
    
    .tag {
      padding: 2rpx 8rpx;
      font-size: 18rpx;
      flex-shrink: 0; /* Prevent tags from shrinking too much */
    }
  }
  
  /* Show description in compact mode, max 1 line */
  .dish-desc {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
    font-size: 22rpx;
    margin-top: 4rpx;
    color: v-bind('themeConfig.textSecondary');
    margin-bottom: 0;
  }
  
  /* Use spacer or auto margin to push action bar to bottom */
  .action-bar {
    padding-top: 0;
    border-top: none;
    margin-top: auto; /* Aligns to bottom */
    display: flex;
    align-items: flex-end; /* Align items to bottom */
    width: 100%;
    flex-shrink: 0;
  }
  
  .favorite-btn {
    top: 8rpx;
    right: auto;
    left: 8rpx;
    width: 48rpx;
    height: 48rpx;
    
    .heart-icon {
      font-size: 24rpx;
    }
  }
  
  .category-badge {
    bottom: 8rpx;
    left: 8rpx;
    padding: 4rpx 10rpx;
    font-size: 18rpx;
    
    text {
      font-size: 18rpx;
    }
  }
}


/* 图片区域 */
.dish-image-wrapper {
  position: relative;
  width: 100%;
  height: 320rpx;
  overflow: hidden;
}

.dish-image {
  width: 100%;
  height: 100%;
  transition: transform 0.6s ease;
}

.dish-card:active .dish-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent);
  pointer-events: none;
}

/* 收藏按钮 */
.favorite-btn {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(0, 0, 0, 0.05) inset;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 10;
}

.favorite-btn:active {
  transform: scale(0.88);
}

.favorite-btn .heart-icon {
  font-size: 32rpx;
  line-height: 1;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.favorite-btn.active {
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
  box-shadow: 
    0 6px 20px rgba(255, 107, 107, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.3) inset;
  animation: favoritePopIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.favorite-btn.active .heart-icon {
  animation: heartBounce 0.6s ease;
}

@keyframes favoritePopIn {
  0% { transform: scale(0.8); }
  50% { transform: scale(1.15); }
  100% { transform: scale(1); }
}

@keyframes heartBounce {
  0%, 100% { transform: scale(1); }
  25% { transform: scale(1.2) rotate(-10deg); }
  50% { transform: scale(1.3) rotate(10deg); }
  75% { transform: scale(1.2) rotate(-5deg); }
}

/* 分类标签 */
.category-badge {
  position: absolute;
  bottom: 16rpx;
  left: 16rpx;
  padding: 8rpx 16rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  border-radius: 20rpx;
  z-index: 10;
  
  text {
    font-size: 22rpx;
    color: #fff;
    font-weight: 600;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}
}

/* 内容区域 */
.dish-content {
  padding: 24rpx;
}

.title-row {
  margin-bottom: 12rpx;
}

.dish-name {
  font-size: 32rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  line-height: 1.3;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
  transition: color 0.3s ease;
}

/* 标签行 */
.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 12rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
  font-weight: 600;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  
  /* 渐变背景 */
  background: linear-gradient(
    135deg,
    v-bind('themeConfig.primaryColor + "12"'),
    v-bind('themeConfig.primaryColor + "20"')
  );
  
  /* 边框 */
  border: 1px solid v-bind('themeConfig.primaryColor + "30"');
  
  /* 毛玻璃效果 */
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

/* 不同标签的颜色主题 */
.tag:nth-child(3n+1) {
  background: linear-gradient(135deg, #ff6b6b10, #ff6b6b18);
  border-color: #ff6b6b30;
  
  .tag-text {
    color: #ff6b6b;
  }
}

.tag:nth-child(3n+2) {
  background: linear-gradient(135deg, #51cf6610, #51cf6618);
  border-color: #51cf6630;
  
  .tag-text {
    color: #51cf66;
  }
}

.tag:nth-child(3n+3) {
  background: linear-gradient(135deg, #339af010, #339af018);
  border-color: #339af030;
  
  .tag-text {
    color: #339af0;
  }
}

.tag-icon {
  font-size: 20rpx;
  line-height: 1;
}

.tag-text {
  line-height: 1;
  letter-spacing: 0.3rpx;
}

.dish-desc {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  line-height: 1.6;
  margin-bottom: 20rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  transition: color 0.3s ease;
}

/* 底部操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1px solid v-bind('themeConfig.borderColor');
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.price-symbol {
  font-size: 24rpx;
  font-weight: 700;
  color: #ff6b6b;
}

.price-value {
  font-size: 40rpx;
  font-weight: 800;
  color: #ff6b6b;
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.price-unit {
  font-size: 22rpx;
    color: v-bind('themeConfig.textSecondary');
  font-weight: 500;
}

/* 数量控制器 */
.quantity-control {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
  
  .btn-minus,
  .btn-plus {
  width: 56rpx;
  height: 56rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    
    &:active {
    transform: scale(0.85);
    }
    
    text {
    font-size: 28rpx;
    font-weight: 600;
      line-height: 1;
    }
  }
  
  .btn-minus {
  background: v-bind('themeConfig.inputBg');
  border: 2px solid v-bind('themeConfig.borderColor');
    
    text {
      color: v-bind('themeConfig.textSecondary');
    }
  }
  
  .btn-plus {
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
  box-shadow: 
    0 4px 12px rgba(255, 107, 107, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2) inset;
    border: none;
    
    text {
      color: #fff;
    }
  }
  
.quantity-num {
  font-size: 28rpx;
  font-weight: 700;
    color: v-bind('themeConfig.textPrimary');
  min-width: 36rpx;
    text-align: center;
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
