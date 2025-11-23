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
        <view class="dish-card" v-for="item in featuredDishes" :key="item.title">
          <view>
            <text class="dish-title">{{ item.title }}</text>
            <text class="dish-tag">{{ item.tag }}</text>
          </view>
          <text class="dish-energy">{{ item.energy }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getBannerList } from '../../api/index'

// 响应式数据
const currentTime = ref('')
const banners = ref([])
const quickActions = ref([
  { label: '一键叫饭', desc: '告诉家人开饭啦', link: '/pages/order/list' },
  { label: '今日菜单', desc: '挑选 3 道拿手菜', link: '/pages/menu/menu' },
  { label: '口味心愿', desc: '记录家人偏好', link: '/pages/menu/menu' }
])
const featuredDishes = ref([
  { title: '妈妈的招牌番茄牛腩', tag: '暖胃', energy: '482 kcal' },
  { title: '爸爸的柠檬烤鱼', tag: '低油', energy: '328 kcal' },
  { title: '可可的芝士焗南瓜', tag: '甜蜜', energy: '266 kcal' }
])

let timer = null

// 更新时钟
const updateClock = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
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

// 生命周期
onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  loadBanners()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.page {
  padding: 20rpx;
  min-height: 100vh;
}

.hero {
  position: relative;
  padding: 60rpx 40rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  
  .hero-text {
    position: relative;
    z-index: 2;
    
    .eyebrow {
      display: block;
      font-size: 24rpx;
      color: #14b8ff;
      margin-bottom: 16rpx;
      font-weight: 600;
      letter-spacing: 2rpx;
    }
    
    .hero-title {
      display: block;
      font-size: 48rpx;
      font-weight: 700;
      color: #fff;
      margin-bottom: 16rpx;
    }
    
    .hero-sub {
      display: block;
      font-size: 28rpx;
      color: #8b8fa3;
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
    background: radial-gradient(circle, rgba(20, 184, 255, 0.2) 0%, transparent 70%);
    z-index: 1;
  }
}

.section {
  margin-bottom: 20rpx;
  padding: 30rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 700;
      color: #fff;
    }
    
    .mini-tip {
      font-size: 24rpx;
      color: #8b8fa3;
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
    background: rgba(20, 184, 255, 0.1);
    border: 1px solid rgba(20, 184, 255, 0.2);
    border-radius: 16rpx;
    padding: 30rpx 20rpx;
    text-align: center;
    transition: all 0.3s;
    
    &:active {
      transform: scale(0.95);
      background: rgba(20, 184, 255, 0.2);
    }
    
    .label {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #14b8ff;
      margin-bottom: 8rpx;
    }
    
    .desc {
      display: block;
      font-size: 22rpx;
      color: #8b8fa3;
    }
  }
}

.dish-list {
  .dish-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    
    &:last-child {
      border-bottom: none;
    }
    
    .dish-title {
      display: block;
      font-size: 28rpx;
      color: #fff;
      margin-bottom: 8rpx;
    }
    
    .dish-tag {
      display: inline-block;
      font-size: 22rpx;
      color: #14b8ff;
      background: rgba(20, 184, 255, 0.1);
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      margin-left: 12rpx;
    }
    
    .dish-energy {
      font-size: 24rpx;
      color: #8b8fa3;
    }
  }
}
</style>
