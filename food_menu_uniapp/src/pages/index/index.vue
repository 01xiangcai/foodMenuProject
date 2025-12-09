<template>
  <view class="page">
    <!-- Hero 区域 -->
    <view class="hero glass-card">
      <view class="hero-text">
        <text class="eyebrow">家宴能量中心</text>
        <text class="hero-title">现在是 {{ currentTime }}</text>
        <text class="hero-sub">今晚吃什么？一起投票决定</text>
      </view>
      <view class="hero-ring pulse"></view>
    </view>

    <!-- 轮播图 -->
    <view class="section glass-card">
      <view class="section-header">
        <text class="section-title">美食轮播</text>
        <text class="mini-tip">左右滑动查看更多</text>
      </view>
      <swiper 
        class="banner-swiper" 
        :indicator-dots="banners.length > 1"
        indicator-color="rgba(255,255,255,0.3)"
        indicator-active-color="#14b8ff"
        :autoplay="banners.length > 1"
        :circular="banners.length > 1"
        interval="3000"
        duration="500"
      >
        <swiper-item v-for="(item, index) in banners" :key="item.id">
          <view class="banner-card">
            <image 
              class="banner-image" 
              mode="aspectFill" 
              :src="item.image"
              @error="onBannerImageError(index)"
            />
            <view class="banner-overlay" v-if="item.title || item.description">
              <view class="banner-content">
                <text class="banner-title" v-if="item.title">{{ item.title }}</text>
                <text class="banner-desc" v-if="item.description">{{ item.description }}</text>
              </view>
            </view>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 快捷操作 -->
    <view class="section glass-card">
      <view class="section-header">
        <text class="section-title">快捷操作</text>
        <text class="mini-tip">一键触达家人</text>
      </view>
      <view class="quick-grid">
        <view 
          class="quick-card" 
          v-for="item in quickActions" 
          :key="item.label"
          @tap="navigateTo(item.link)"
        >
          <text class="label">{{ item.label }}</text>
          <text class="desc">{{ item.desc }}</text>
        </view>
      </view>
    </view>

    <!-- 明星菜 -->
    <view class="section glass-card">
      <view class="section-header">
        <text class="section-title">明星菜</text>
        <text class="mini-tip">AI 推荐 · 适配家庭口味</text>
      </view>
      <view class="dish-list">
        <view class="dish-card" v-for="item in featuredDishes" :key="item.id" @tap="navigateToDishDetail(item.id)">
          <image class="dish-image" :src="item.image" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-title">{{ item.title }}</text>
            <text class="dish-desc" v-if="item.description">{{ item.description }}</text>
            <view class="dish-meta">
              <text class="dish-tag">{{ item.tag }}</text>
              <text class="dish-energy" v-if="item.energy">{{ item.energy }}</text>
            </view>
          </view>
          <view class="dish-count" v-if="item.orderCount !== undefined">
            <text class="count-icon">🔥</text>
            <text class="count-number">{{ item.orderCount }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getBannerList, getTopDishes } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { getDishImage } from '@/utils/image'

// 使用主题
const { themeConfig, loadTheme } = useTheme()

// 响应式数据
const currentTime = ref('')
const banners = ref([])
const quickActions = ref([
  { label: '一键叫饭', desc: '告诉家人开饭啦', link: '/pages/order/list' },
  { label: '今日菜单', desc: '挑选 3 道拿手菜', link: '/pages/menu/menu' },
  { label: '营销活动', desc: '参与抽奖赢好礼', link: '/pages/marketing/activity-list' }
])
const featuredDishes = ref([])

let timer = null

// 更新时钟
const updateClock = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
}

// 加载明星菜
const loadFeaturedDishes = async () => {
  try {
    const res = await getTopDishes()
    if (res.data) {
      // 过滤掉已下架的菜品（只显示在售的，status === 1）
      const availableDishes = res.data.filter(item => item.status === 1)
      
      featuredDishes.value = availableDishes.map(item => {
        return {
          id: item.id,
          title: item.name,
          description: item.description || '',
          tag: item.tags ? item.tags.split(',')[0] : '热销', // 没有标签时显示"热销"
          energy: item.calories ? `${item.calories} kcal` : '', // 有卡路里时加单位
          image: getDishImage(item), // 使用工具函数获取主图
          orderCount: item.orderCount || 0 // 点菜次数
        }
      })
    }
  } catch (error) {
    console.error('加载明星菜失败:', error)
    // 失败时显示默认数据
    featuredDishes.value = [
      { title: '妈妈的招牌番茄牛腩', tag: '暖胃', energy: '482 kcal' },
      { title: '爸爸的柠檬烤鱼', tag: '低油', energy: '328 kcal' },
      { title: '可可的芝士焗南瓜', tag: '甜蜜', energy: '266 kcal' }
    ]
  }
}

// 加载轮播图
const loadBanners = async () => {
  try {
    const res = await getBannerList()
    const list = (res.data || []).map(item => ({
      id: item.id,
      image: item.image || 'https://dummyimage.com/800x400/6366f1/ffffff&text=Banner',
      title: item.title,
      description: item.description,
      linkUrl: item.linkUrl
    }))
    
    if (list.length === 0) {
      list.push({
        id: 1,
        image: 'https://dummyimage.com/800x400/6366f1/ffffff&text=家庭美食',
        title: '家庭美食精选',
        description: '每日新鲜食材，用心烹饪每一道菜'
      })
    }
    
    banners.value = list
  } catch (error) {
    console.error('加载轮播图失败:', error)
    banners.value = [{
      id: 1,
      image: 'https://dummyimage.com/800x400/6366f1/ffffff&text=家庭美食',
      title: '家庭美食精选',
      description: '每日新鲜食材，用心烹饪每一道菜'
    }]
  }
}

// 轮播图加载失败
const onBannerImageError = (index) => {
  console.error('轮播图加载失败:', index)
  if (banners.value[index]) {
    banners.value[index].image = 'https://dummyimage.com/800x400/6366f1/ffffff&text=图片加载失败'
  }
}

// 导航
const navigateTo = (url) => {
  uni.navigateTo({ url })
}

// 导航到菜品详情
const navigateToDishDetail = (dishId) => {
  uni.navigateTo({ 
    url: `/pages/detail/detail?id=${dishId}` 
  })
}

// 生命周期
onMounted(() => {
  loadTheme()
  updateClock()
  timer = setInterval(updateClock, 1000)
  loadBanners()
  loadFeaturedDishes()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

// 页面转发配置
import { onShareAppMessage } from '@dcloudio/uni-app'

onShareAppMessage((res) => {
  return {
    title: '美食菜单 - 家宴能量中心',
    path: '/pages/index/index',
    imageUrl: banners.value.length > 0 ? banners.value[0].image : '' // 使用第一张轮播图作为分享图
  }
})
</script>

<style lang="scss" scoped>
.page {
  padding: 20rpx;
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  transition: background-color 0.3s ease;
}

.hero {
  position: relative;
  padding: 60rpx 40rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 16rpx;
  transition: all 0.3s ease;
  
  .hero-text {
    position: relative;
    z-index: 2;
    
    .eyebrow {
      display: block;
      font-size: 24rpx;
      color: v-bind('themeConfig.primaryColor');
      margin-bottom: 16rpx;
      font-weight: 600;
      letter-spacing: 2rpx;
      transition: color 0.3s ease;
    }
    
    .hero-title {
      display: block;
      font-size: 48rpx;
      font-weight: 700;
      color: v-bind('themeConfig.textPrimary');
      margin-bottom: 16rpx;
      transition: color 0.3s ease;
    }
    
    .hero-sub {
      display: block;
      font-size: 28rpx;
      color: v-bind('themeConfig.textSecondary');
      transition: color 0.3s ease;
    }
  }
  
  .hero-ring {
    position: absolute;
    right: -100rpx;
    top: 50%;
    transform: translateY(-50%);
    width: 300rpx;
    height: 300rpx;
    border-radius: 50%;
    background: radial-gradient(circle, v-bind('themeConfig.primaryColor + "33"') 0%, transparent 70%);
    z-index: 1;
    transition: background 0.3s ease;
  }
}

.section {
  margin-bottom: 20rpx;
  padding: 30rpx;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 16rpx;
  transition: all 0.3s ease;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 700;
      color: v-bind('themeConfig.textPrimary');
      transition: color 0.3s ease;
    }
    
    .mini-tip {
      font-size: 24rpx;
      color: v-bind('themeConfig.textSecondary');
      transition: color 0.3s ease;
    }
  }
}

.banner-swiper {
  width: 100%;
  height: 360rpx;
  border-radius: 16rpx;
  overflow: hidden;
  
  .banner-card {
    position: relative;
    width: 100%;
    height: 100%;
    
    .banner-image {
      width: 100%;
      height: 100%;
    }
    
    .banner-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 30rpx;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
      
      .banner-title {
        display: block;
        font-size: 32rpx;
        font-weight: 700;
        color: #fff;
        margin-bottom: 8rpx;
      }
      
      .banner-desc {
        display: block;
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  
  .quick-card {
    background: v-bind('themeConfig.primaryColor + "1a"');
    border: 1px solid v-bind('themeConfig.primaryColor + "33"');
    border-radius: 16rpx;
    padding: 30rpx 20rpx;
    text-align: center;
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.95);
      background: v-bind('themeConfig.primaryColor + "33"');
    }
    
    .label {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: v-bind('themeConfig.primaryColor');
      margin-bottom: 8rpx;
      transition: color 0.3s ease;
    }
    
    .desc {
      display: block;
      font-size: 22rpx;
      color: v-bind('themeConfig.textSecondary');
      transition: color 0.3s ease;
    }
  }
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  
  .dish-card {
    display: flex;
    gap: 20rpx;
    padding: 20rpx;
    background: v-bind('themeConfig.bgSecondary');
    border-radius: 16rpx;
    border: 1px solid v-bind('themeConfig.borderColor');
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
  
  .dish-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
    background: v-bind('themeConfig.bgTertiary');
  }
  
  .dish-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
  }
  
  .dish-title {
    font-size: 30rpx;
    font-weight: 600;
    color: v-bind('themeConfig.textPrimary');
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    transition: color 0.3s ease;
  }
  
  .dish-desc {
    font-size: 24rpx;
    color: v-bind('themeConfig.textSecondary');
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
    transition: color 0.3s ease;
  }
  
  .dish-meta {
    display: flex;
    align-items: center;
    gap: 12rpx;
    flex-wrap: wrap;
  }
  
  .dish-tag {
    font-size: 22rpx;
    color: v-bind('themeConfig.primaryColor');
    background: v-bind('themeConfig.primaryColor + "1a"');
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
    transition: all 0.3s ease;
  }
  
  .dish-energy {
    font-size: 22rpx;
    color: v-bind('themeConfig.textSecondary');
    transition: color 0.3s ease;
  }
  
  .dish-count {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 8rpx 16rpx;
    background: v-bind('themeConfig.primaryColor + "1a"');
    border-radius: 12rpx;
    min-width: 80rpx;
    
    .count-icon {
      font-size: 32rpx;
      margin-bottom: 4rpx;
    }
    
    .count-number {
      font-size: 24rpx;
      font-weight: 600;
      color: v-bind('themeConfig.primaryColor');
      transition: color 0.3s ease;
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

