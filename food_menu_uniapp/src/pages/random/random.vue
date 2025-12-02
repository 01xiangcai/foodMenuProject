<template>
  <view class="page">
    <!-- 筛选条件面板 -->
    <view class="filter-section" v-if="filterVisible">
      <view class="filter-header">
        <text class="filter-title">筛选条件</text>
        <view class="filter-actions">
          <text class="reset-btn" @tap="resetFilter">重置</text>
          <text class="confirm-btn" @tap="applyFilter">确定</text>
        </view>
      </view>
      
      <!-- 价格区间 -->
      <view class="filter-item">
        <text class="filter-label">价格区间</text>
        <view class="price-range">
          <text class="price-text">¥{{ filter.priceMin || 0 }}</text>
          <slider 
            class="price-slider"
            :min="priceRange.min" 
            :max="priceRange.max"
            :value="filter.priceMin || priceRange.min"
            @change="onPriceMinChange"
            activeColor="#14b8ff"
          />
          <text class="price-separator">-</text>
          <slider 
            class="price-slider"
            :min="priceRange.min" 
            :max="priceRange.max"
            :value="filter.priceMax || priceRange.max"
            @change="onPriceMaxChange"
            activeColor="#14b8ff"
          />
          <text class="price-text">¥{{ filter.priceMax || priceRange.max }}</text>
        </view>
      </view>
      
      <!-- 分类选择 -->
      <view class="filter-item">
        <text class="filter-label">菜品分类</text>
        <view class="category-list">
          <view 
            class="category-tag"
            :class="{ active: filter.categoryIds.includes(cat.id) }"
            v-for="cat in categories" 
            :key="cat.id"
            @tap="toggleCategory(cat.id)"
          >
            <text>{{ cat.name }}</text>
          </view>
        </view>
      </view>
      
      <!-- 标签选择 -->
      <view class="filter-item">
        <text class="filter-label">菜品标签</text>
        <view class="tag-list">
          <view 
            class="filter-tag"
            :class="{ active: filter.tags.includes(tag.name) }"
            v-for="tag in tags" 
            :key="tag.id"
            @tap="toggleTag(tag.name)"
          >
            <text>{{ tag.icon }} {{ tag.name }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 主内容区 -->
    <view class="content">
      <!-- 筛选按钮 -->
      <view class="filter-toggle" @tap="toggleFilter">
        <text class="filter-icon">🔍</text>
        <text class="filter-text">{{ filterVisible ? '收起筛选' : '展开筛选' }}</text>
      </view>
      
      <!-- 随机动画区域 -->
      <view class="random-area" v-if="!currentDish || animating">
        <view class="card-container" :class="{ animating: animating }">
          <view class="card" v-for="i in 5" :key="i" :style="getCardStyle(i)">
            <view class="card-back">
              <text class="card-icon" :class="{ rotating: animating }">🎲</text>
              <text class="card-text">随机点餐</text>
            </view>
          </view>
        </view>
        <text class="hint-text" v-if="!animating">点击下方按钮开始随机</text>
      </view>
      
      <!-- 结果展示区 -->
      <view class="result-area" v-if="currentDish && !animating">
        <view class="dish-card">
          <image class="dish-image" :src="getDishImage(currentDish)" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-name">{{ currentDish.name }}</text>
            <view class="dish-tags" v-if="currentDish.tags">
              <view class="tag" v-for="(tag, index) in parseTags(currentDish.tags)" :key="index">
                <text>{{ tag }}</text>
              </view>
            </view>
            <text class="dish-desc">{{ currentDish.description || '暂无描述' }}</text>
            <view class="dish-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ currentDish.price }}</text>
              <text class="price-unit">/份</text>
            </view>
          </view>
        </view>
        
        <!-- 操作按钮 -->
        <view class="action-buttons">
          <view class="action-btn secondary" @tap="goToDetail">
            <text>查看详情</text>
          </view>
          <view class="action-btn primary" @tap="addToCart">
            <text>加入购物车</text>
          </view>
        </view>
      </view>
      
      <!-- 随机按钮 -->
      <view class="random-button" @tap="startRandom" :class="{ disabled: loading, loading: loading }">
        <text class="button-icon">🎲</text>
        <text class="button-text">{{ loading ? '随机中...' : (currentDish ? '再来一次' : '开始随机') }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRandomDish, getRandomDishWithFilter, getRandomFilterOptions } from '@/api/index'
import { useCartStore } from '@/stores/cart'
import { getDishImage } from '@/utils/image'

const cartStore = useCartStore()

// 状态
const loading = ref(false)
const animating = ref(false)
const currentDish = ref(null)
const filterVisible = ref(false)

// 筛选条件
const filter = ref({
  priceMin: null,
  priceMax: null,
  categoryIds: [],
  tags: []
})

// 筛选选项
const categories = ref([])
const tags = ref([])
const priceRange = ref({ min: 0, max: 100 })

// 去重列表(最近5次)
const excludeIds = ref([])
const MAX_EXCLUDE_COUNT = 5

// 加载筛选选项
const loadFilterOptions = async () => {
  try {
    const res = await getRandomFilterOptions()
    if (res.data) {
      categories.value = res.data.categories || []
      tags.value = res.data.tags || []
      if (res.data.priceRange) {
        priceRange.value = {
          min: Number(res.data.priceRange.min) || 0,
          max: Number(res.data.priceRange.max) || 100
        }
      }
    }
  } catch (error) {
    console.error('加载筛选选项失败:', error)
  }
}

// 开始随机
const startRandom = async () => {
  if (loading.value || animating.value) return
  
  loading.value = true
  animating.value = true
  
  try {
    // 构建筛选条件
    const filterData = {
      priceMin: filter.value.priceMin,
      priceMax: filter.value.priceMax,
      categoryIds: filter.value.categoryIds.length > 0 ? filter.value.categoryIds : null,
      tags: filter.value.tags.length > 0 ? filter.value.tags : null,
      excludeIds: excludeIds.value.length > 0 ? excludeIds.value : null
    }
    
    // 调用API
    const res = filterData.priceMin || filterData.priceMax || filterData.categoryIds || filterData.tags
      ? await getRandomDishWithFilter(filterData)
      : await getRandomDish()
    
    if (res.data) {
      // 延迟显示结果(等待动画)
      setTimeout(() => {
        currentDish.value = res.data
        animating.value = false
        
        // 添加到排除列表
        addToExcludeList(res.data.id)
        
        uni.showToast({
          title: '随机成功!',
          icon: 'success',
          duration: 1500
        })
      }, 2000)
    } else {
      animating.value = false
      uni.showToast({
        title: res.msg || '没有符合条件的菜品',
        icon: 'none'
      })
    }
  } catch (error) {
    animating.value = false
    console.error('随机失败:', error)
    uni.showToast({
      title: '随机失败,请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 添加到排除列表
const addToExcludeList = (dishId) => {
  if (!excludeIds.value.includes(dishId)) {
    excludeIds.value.push(dishId)
    if (excludeIds.value.length > MAX_EXCLUDE_COUNT) {
      excludeIds.value.shift()
    }
    uni.setStorageSync('random_exclude_ids', excludeIds.value)
  }
}

// 从本地存储加载排除列表
const loadExcludeList = () => {
  const saved = uni.getStorageSync('random_exclude_ids')
  if (saved && Array.isArray(saved)) {
    excludeIds.value = saved
  }
}

// 切换筛选面板
const toggleFilter = () => {
  filterVisible.value = !filterVisible.value
}

// 重置筛选
const resetFilter = () => {
  filter.value = {
    priceMin: null,
    priceMax: null,
    categoryIds: [],
    tags: []
  }
}

// 应用筛选
const applyFilter = () => {
  filterVisible.value = false
  uni.showToast({
    title: '筛选条件已应用',
    icon: 'success'
  })
}

// 价格区间变化
const onPriceMinChange = (e) => {
  filter.value.priceMin = e.detail.value
}

const onPriceMaxChange = (e) => {
  filter.value.priceMax = e.detail.value
}

// 切换分类
const toggleCategory = (categoryId) => {
  const index = filter.value.categoryIds.indexOf(categoryId)
  if (index > -1) {
    filter.value.categoryIds.splice(index, 1)
  } else {
    filter.value.categoryIds.push(categoryId)
  }
}

// 切换标签
const toggleTag = (tagName) => {
  const index = filter.value.tags.indexOf(tagName)
  if (index > -1) {
    filter.value.tags.splice(index, 1)
  } else {
    filter.value.tags.push(tagName)
  }
}

// 解析标签
const parseTags = (tags) => {
  if (!tags) return []
  return typeof tags === 'string' ? tags.split(/[,，]/).filter(Boolean) : []
}

// 卡片样式
const getCardStyle = (index) => {
  if (!animating.value) {
    return {
      transform: `translateX(${(index - 3) * 20}rpx) scale(${1 - Math.abs(index - 3) * 0.1})`,
      opacity: 1 - Math.abs(index - 3) * 0.2,
      zIndex: 5 - Math.abs(index - 3)
    }
  }
  return {
    animation: `cardFlip 0.5s ease ${index * 0.1}s`
  }
}

// 加入购物车
const addToCart = () => {
  if (!currentDish.value) return
  
  cartStore.addToCart(currentDish.value)
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}

// 查看详情
const goToDetail = () => {
  if (!currentDish.value) return
  
  uni.navigateTo({
    url: `/pages/detail/detail?id=${currentDish.value.id}`
  })
}

onMounted(() => {
  loadFilterOptions()
  loadExcludeList()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 20rpx;
}

/* 筛选面板 */
.filter-section {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.filter-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}

.filter-actions {
  display: flex;
  gap: 20rpx;
}

.reset-btn,
.confirm-btn {
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.reset-btn {
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.1);
}

.confirm-btn {
  color: #fff;
  background: linear-gradient(135deg, #14b8ff, #0ea5e9);
}

.filter-item {
  margin-bottom: 30rpx;
}

.filter-label {
  display: block;
  font-size: 28rpx;
  color: #cbd5e1;
  margin-bottom: 16rpx;
  font-weight: 600;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.price-text {
  font-size: 26rpx;
  color: #14b8ff;
  font-weight: 600;
  min-width: 80rpx;
}

.price-slider {
  flex: 1;
}

.price-separator {
  color: #64748b;
  font-size: 24rpx;
}

.category-list,
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.category-tag,
.filter-tag {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #cbd5e1;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
  
  &.active {
    color: #fff;
    background: linear-gradient(135deg, #14b8ff, #0ea5e9);
    border-color: transparent;
  }
}

/* 主内容区 */
.content {
  flex: 1;
}

.filter-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 20rpx;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 24rpx;
  margin-bottom: 30rpx;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.filter-icon {
  font-size: 32rpx;
}

.filter-text {
  font-size: 28rpx;
  color: #cbd5e1;
  font-weight: 600;
}

/* 随机动画区 */
.random-area {
  min-height: 600rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
}

.card-container {
  position: relative;
  width: 100%;
  height: 500rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card {
  position: absolute;
  width: 280rpx;
  height: 400rpx;
  background: linear-gradient(135deg, #1e293b, #334155);
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(20, 184, 255, 0.3);
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.card-back {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.card-icon {
  font-size: 100rpx;
  filter: drop-shadow(0 0 20rpx rgba(20, 184, 255, 0.5));
  transition: transform 0.3s;
  
  &.rotating {
    animation: diceRotate 0.6s linear infinite;
  }
}

.card-text {
  font-size: 32rpx;
  color: #14b8ff;
  font-weight: 700;
  text-shadow: 0 0 20rpx rgba(20, 184, 255, 0.5);
}

.card-container.animating .card {
  animation: cardFlip 0.6s ease-in-out;
}

@keyframes cardFlip {
  0%, 100% {
    transform: rotateY(0deg) scale(1);
  }
  50% {
    transform: rotateY(180deg) scale(1.1);
  }
}

.hint-text {
  margin-top: 40rpx;
  font-size: 28rpx;
  color: #64748b;
  text-align: center;
}

/* 结果展示区 */
.result-area {
  margin-bottom: 30rpx;
}

.dish-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 24rpx;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 20rpx;
}

.dish-image {
  width: 100%;
  height: 400rpx;
}

.dish-info {
  padding: 30rpx;
}

.dish-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16rpx;
  display: block;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;
  display: block;
}

.tag {
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  color: #14b8ff;
  background: rgba(20, 184, 255, 0.1);
  border: 1px solid rgba(20, 184, 255, 0.3);
}

.dish-desc {
  font-size: 26rpx;
  color: #94a3b8;
  line-height: 1.6;
  margin-bottom: 20rpx;
  display: block;
}

.dish-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 28rpx;
  color: #f59e0b;
  font-weight: 700;
}

.price-value {
  font-size: 48rpx;
  color: #f59e0b;
  font-weight: 700;
  margin: 0 8rpx;
}

.price-unit {
  font-size: 24rpx;
  color: #94a3b8;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  padding: 28rpx;
  border-radius: 24rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 700;
  
  &.primary {
    background: linear-gradient(135deg, #14b8ff, #0ea5e9);
    color: #fff;
    box-shadow: 0 10px 30px rgba(20, 184, 255, 0.3);
  }
  
  &.secondary {
    background: rgba(255, 255, 255, 0.05);
    color: #cbd5e1;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  &:active {
    transform: scale(0.95);
  }
}

/* 随机按钮 */
.random-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 32rpx;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  border-radius: 28rpx;
  box-shadow: 0 20px 60px rgba(245, 158, 11, 0.4);
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.95);
  }
  
  &.disabled {
    opacity: 0.6;
    pointer-events: none;
  }
  
  &.loading {
    animation: buttonPulse 1.5s ease-in-out infinite;
  }
}

.button-icon {
  font-size: 48rpx;
  filter: drop-shadow(0 0 10rpx rgba(255, 255, 255, 0.5));
}

.button-text {
  font-size: 36rpx;
  color: #fff;
  font-weight: 700;
  text-shadow: 0 2px 10rpx rgba(0, 0, 0, 0.3);
}

/* 骰子旋转动画 */
@keyframes diceRotate {
  0% {
    transform: rotate(0deg) scale(1);
  }
  25% {
    transform: rotate(90deg) scale(1.1);
  }
  50% {
    transform: rotate(180deg) scale(1);
  }
  75% {
    transform: rotate(270deg) scale(1.1);
  }
  100% {
    transform: rotate(360deg) scale(1);
  }
}

/* 按钮脉冲动画 */
@keyframes buttonPulse {
  0%, 100% {
    box-shadow: 0 20px 60px rgba(245, 158, 11, 0.4);
  }
  50% {
    box-shadow: 0 20px 80px rgba(245, 158, 11, 0.6);
  }
}
</style>
