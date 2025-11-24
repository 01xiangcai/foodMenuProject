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
              <view class="add-btn" @tap.stop="addToCart(dish)">
                <text class="plus">+</text>
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
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategoryList, getDishList } from '@/api/index'

const currentCategory = ref(0)
const categories = ref([
  { id: 0, name: '全部', icon: '/static/icons/all.png' }
])
const dishes = ref([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.data && res.data.length > 0) {
      // 假设后端返回的分类包含 icon 字段，如果没有可以使用默认图标
      categories.value = [
        { id: 0, name: '全部', icon: '' }, // 可以添加默认图标
        ...res.data.map(item => ({
          ...item,
          icon: item.icon || '' // 确保有 icon 字段
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
    // 兼容两种数据格式：直接返回数组 或 分页对象(records)
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
    // 即使失败也显示一些模拟数据用于展示 UI 效果（如果需要的话，或者保持空状态）
    if (reset && dishes.value.length === 0) {
       // 仅在开发环境或演示时使用模拟数据，生产环境应移除
       // dishes.value = [...] 
    }
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

// 加入购物车
const addToCart = (dish) => {
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}

onMounted(() => {
  loadCategories()
  loadDishes(true)
})
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  height: 100vh;
  background-color: #050a1f;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 180rpx;
  height: 100%;
  background: rgba(10, 17, 32, 0.6);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.sidebar-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30rpx 10rpx;
  color: #8b8fa3;
  transition: all 0.3s;
  
  &.active {
    background: rgba(255, 255, 255, 0.05);
    color: #fff;
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
  background: linear-gradient(to bottom, #14b8ff, #a855f7);
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
  background-color: #050a1f;
}

.dish-list {
  padding-bottom: 40rpx;
}

.dish-card {
  display: flex;
  padding: 20rpx;
  margin-bottom: 20rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16rpx;
  border: 1px solid rgba(255, 255, 255, 0.05);
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
  color: #fff;
  margin-bottom: 8rpx;
}

.tags {
  display: flex;
  gap: 8rpx;
}

.tag {
  font-size: 20rpx;
  color: #14b8ff;
  background: rgba(20, 184, 255, 0.1);
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}

.dish-desc {
  font-size: 24rpx;
  color: #8b8fa3;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 36rpx;
  font-weight: 700;
  color: #ff6b6b;
  
  .unit {
    font-size: 24rpx;
    color: #8b8fa3;
    font-weight: normal;
    margin-left: 4rpx;
  }
}

.add-btn {
  width: 50rpx;
  height: 50rpx;
  background: linear-gradient(135deg, #14b8ff 0%, #a855f7 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 10rpx rgba(20, 184, 255, 0.3);
  
  .plus {
    font-size: 36rpx;
    color: #fff;
    line-height: 1;
    margin-top: -4rpx;
  }
}

.loading,
.no-more,
.empty {
  padding: 40rpx;
  text-align: center;
  color: #8b8fa3;
  font-size: 28rpx;
}
</style>
