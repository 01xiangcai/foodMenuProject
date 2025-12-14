<template>
  <view class="page">
    <scroll-view 
      class="favorites-scroll" 
      scroll-y
      @scrolltolower="loadMore"
      :lower-threshold="100"
    >
      <view v-if="loading && page === 1" class="state loading">
        <view class="skeleton-card" v-for="n in 4" :key="n">
          <view class="skeleton-cover"></view>
          <view class="skeleton-line short"></view>
          <view class="skeleton-line"></view>
        </view>
      </view>

      <view v-else-if="favorites.length > 0" class="favorites-grid">
        <view 
          class="fav-card glass-card"
          v-for="dish in favorites" 
          :key="dish.id"
          @tap="goToDetail(dish.id)"
        >
          <view class="card-cover">
            <image class="dish-image" :src="getDishImage(dish)" mode="aspectFill" />
            <view class="cover-tag">{{ dish.categoryNames ? dish.categoryNames.join(', ') : (dish.categoryName || '精选菜品') }}</view>
          </view>
          <view class="card-body">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-desc">{{ dish.description || '用心烹饪的家常味道' }}</text>
            <view class="card-footer">
              <view class="price-box">
                <text class="price">¥{{ dish.price }}</text>
                <text class="unit">/份</text>
              </view>
              <view class="btn-unfavorite" @tap.stop="unfavorite(dish.id)">
                <text>{{ heartIcon }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 加载更多提示 -->
        <view v-if="loadingMore" class="loading-more">
          <text>加载中...</text>
        </view>
        <view v-else-if="!hasMore && favorites.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </view>

      <view class="state empty" v-else-if="!loading">
        <view class="icon-wrapper">
          <text class="icon">❤</text>
        </view>
        <text class="empty-title">{{ needLogin ? '登录后查看收藏' : '还没有收藏的菜品' }}</text>
        <text class="empty-subtitle">
          {{ needLogin ? '登录同步你喜爱的菜品，随时随地查看' : '将喜欢的菜品收入囊中，随时点餐更快捷' }}
        </text>
        <view 
          class="btn-go" 
          @tap="needLogin ? goToLogin() : goToMenu()"
        >
          <text>{{ needLogin ? '立即登录' : '去点餐' }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useTheme } from '@/stores/theme'
import { getFavoritePage, removeFavorite } from '@/api/index'
import { getDishImage } from '@/utils/image'

const { themeConfig, loadTheme } = useTheme()
const heartIcon = '❤'

const favorites = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const needLogin = ref(false)
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

const mapFavorite = (item) => ({
  id: item.id || item.dishId,
  name: item.name || item.dishName,
  description: item.description || item.dishDescription || '',
  categoryName: item.categoryName,
  categoryNames: item.categoryNames || (item.categoryName ? [item.categoryName] : []),
  price: item.price || item.dishPrice || 0,
  localImage: item.localImage || item.dishLocalImage,
  image: item.image || item.dishImage
})

// 加载收藏列表（分页）
const loadFavorites = async (isRefresh = false) => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    needLogin.value = true
    favorites.value = []
    return
  }

  needLogin.value = false
  
  // 如果是刷新，重置分页
  if (isRefresh) {
    page.value = 1
    favorites.value = []
    hasMore.value = true
  }

  // 如果没有更多数据，不加载
  if (!hasMore.value && !isRefresh) {
    return
  }

  // 设置加载状态
  if (page.value === 1) {
  loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getFavoritePage(page.value, pageSize.value)
    
    if (res.data && res.data.records) {
      const newFavorites = (res.data.records || []).map(mapFavorite)
      
      if (isRefresh) {
        favorites.value = newFavorites
      } else {
        favorites.value = [...favorites.value, ...newFavorites]
      }
      
      // 判断是否还有更多数据
      hasMore.value = res.data.current < res.data.pages
      
      // 如果有数据，页码加1，准备下次加载
      if (hasMore.value) {
        page.value += 1
      }
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loadingMore.value && hasMore.value && !loading.value) {
    loadFavorites(false)
  }
}

const goToDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${dishId}`
  })
}

const unfavorite = (dishId) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await removeFavorite(dishId)
          favorites.value = favorites.value.filter(item => item.id !== dishId)
          uni.showToast({
            title: '已取消收藏',
            icon: 'success'
          })
          // 如果当前页没有数据了，重新加载第一页
          if (!favorites.value.length) {
            loadFavorites(true)
          }
        } catch (error) {
          console.error('取消收藏失败:', error)
          uni.showToast({
            title: '取消收藏失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

const goToMenu = () => {
  uni.switchTab({
    url: '/pages/menu/menu'
  })
}

const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/login/login'
  })
}

onMounted(() => {
  loadTheme()
  loadFavorites(true)
})

onShow(() => {
  // 每次显示页面时刷新数据
  loadFavorites(true)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: v-bind('themeConfig.bgPrimary');
  transition: background-color 0.3s ease;
}

.favorites-scroll {
  height: 100vh;
  padding: 30rpx 24rpx 120rpx;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.loading {
  width: 100%;
}

.skeleton-card {
  width: 100%;
  border-radius: 24rpx;
  background: v-bind('themeConfig.bgSecondary');
  padding: 20rpx;
  margin-bottom: 20rpx;
  animation: pulse 1.5s infinite;
}

.skeleton-cover {
  width: 100%;
  height: 240rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.08);
  margin-bottom: 20rpx;
}

.skeleton-line {
  height: 24rpx;
  border-radius: 12rpx;
  background: rgba(255, 255, 255, 0.08);
  margin-bottom: 14rpx;
  
  &.short {
    width: 60%;
  }
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300rpx, 1fr));
  gap: 24rpx;
}

.fav-card {
  border-radius: 28rpx;
  overflow: hidden;
  background: v-bind('themeConfig.cardBg');
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
    box-shadow: v-bind('themeConfig.shadowMedium');
  }
}

.card-cover {
  position: relative;
}

.dish-image {
  width: 100%;
  height: 280rpx;
  object-fit: cover;
}

.cover-tag {
  position: absolute;
  left: 24rpx;
  top: 24rpx;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  background: rgba(0, 0, 0, 0.3);
  color: #fff;
  backdrop-filter: blur(6px);
}

.card-body {
  padding: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.dish-name {
  font-size: 32rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-desc {
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.card-footer {
  margin-top: 10rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-box {
  display: flex;
  align-items: flex-end;
  gap: 6rpx;
}

.price {
  font-size: 42rpx;
  font-weight: 700;
  color: v-bind('themeConfig.errorColor');
}

.unit {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
}

.btn-unfavorite {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background: v-bind('themeConfig.primaryGradient');
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: transform 0.2s ease;
  
  text {
    font-size: 36rpx;
    color: #fff;
  }
  
  &:active {
    transform: scale(0.9);
  }
}

.state.empty {
  text-align: center;
  padding-top: 120rpx;
  gap: 10rpx;
}

.icon-wrapper {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: v-bind('themeConfig.shadowLight');
}

.icon {
  font-size: 64rpx;
}

.empty-title {
  font-size: 34rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 600;
}

.empty-subtitle {
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
  margin-bottom: 30rpx;
  line-height: 1.6;
}

.btn-go {
  padding: 20rpx 80rpx;
  border-radius: 999rpx;
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  font-size: 28rpx;
  box-shadow: v-bind('themeConfig.shadowMedium');
}

.loading-more,
.no-more {
  width: 100%;
  padding: 40rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
  grid-column: 1 / -1;
}
</style>
