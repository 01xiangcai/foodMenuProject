<template>
  <view class="page" :class="'theme-' + currentTheme">
    <!-- Hero 区域: 能量中心 -->
    <view class="header-section">
      <view class="header-content">
        <text class="eyebrow">家宴能量中心</text>
        <view class="time-display">
          <text class="time-text">现在是 {{ currentTime }}</text>
        </view>
        <text class="subtitle">今晚吃什么？一起投票决定</text>
      </view>
      <!-- 保留一个微妙的装饰圆环，适配深浅模式 -->
      <view class="decoration-ring"></view>
    </view>

    <!-- 轮播图 -->
    <view class="section carousel-section">
      <view class="section-header">
        <text class="section-title">美食轮播</text>
        <text class="mini-tip">左右滑动查看更多</text>
      </view>
      <swiper 
        class="banner-swiper" 
        :indicator-dots="banners.length > 1"
        indicator-color="rgba(255,255,255,0.4)"
        indicator-active-color="var(--accent-orange)"
        :autoplay="banners.length > 1"
        :circular="banners.length > 1"
        interval="4000"
        duration="600"
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
    <view class="section actions-section">
      <!-- 隐藏标题，直接展示卡片，或者保留标题视设计而定，这里保留标题但样式微调 -->
      <!-- <view class="section-header">
        <text class="section-title">快捷操作</text>
      </view> -->
      
      <view class="quick-grid">
        <view 
          class="quick-card" 
          v-for="(item, index) in quickActions" 
          :key="item.label"
          @tap="navigateTo(item.link)"
          :class="'quick-card-' + index"
        >
          <!-- 这里可以用图标，目前沿用纯文字设计但增加样式 -->
          <view class="quick-content">
             <!-- 模拟图标 -->
            <view class="quick-icon-placeholder">
               <text v-if="index===0">👆</text>
               <text v-if="index===1">📋</text>
               <text v-if="index===2">📣</text>
            </view>
            <text class="action-label">{{ item.label }}</text>
            <!-- <text class="action-desc">{{ item.desc }}</text> -->
          </view>
        </view>
      </view>
    </view>

    <!-- 明星菜 -->
    <view class="section menu-section">
      <view class="section-header">
        <text class="section-title">明星菜</text>
        <text class="link-text">AI 推荐 · 适配家庭口味</text>
      </view>
      <view class="dish-list">
        <view class="dish-card glass-panel" v-for="item in featuredDishes" :key="item.id" @tap="navigateToDishDetail(item.id)">
          <image class="dish-image" :src="item.image" mode="aspectFill" />
          <view class="dish-info">
            <view class="dish-main">
                <text class="dish-title">{{ item.title }}</text>
                <text class="dish-desc" v-if="item.description">{{ item.description }}</text>
            </view>
            <view class="dish-meta">
              <text class="dish-tag" :class="getTagClass(item.tag)">{{ item.tag }}</text>
              <!-- <text class="dish-energy" v-if="item.energy">{{ item.energy }}</text> -->
            </view>
          </view>
          
          <view class="dish-action">
            <view class="fire-badge">
                <text class="fire-icon">🔥</text>
                <text class="fire-count">{{ item.orderCount }}</text>
            </view>
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
import { onShareAppMessage, onShow } from '@dcloudio/uni-app'

// 使用主题
const { currentTheme, loadTheme, applyCurrentTheme } = useTheme()

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
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${h}:${m}:${s}`
}

// 获取标签样式类 (可选)
const getTagClass = (tag) => {
    // 简单返回统一类名，或根据tag内容返回不同颜色类
    return 'tag-default'
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
          tag: item.tags ? item.tags.split(',')[0] : '热销',
          energy: item.calories ? `${item.calories} kcal` : '',
          image: getDishImage(item),
          orderCount: item.orderCount || 0
        }
      })
    }
  } catch (error) {
    console.error('加载明星菜失败:', error)
    // 失败时显示默认数据
    featuredDishes.value = [
      { title: '妈妈的招牌番茄牛腩', tag: '暖胃', energy: '482 kcal', orderCount: 9 },
      { title: '爸爸的柠檬烤鱼', tag: '低油', energy: '328 kcal', orderCount: 5 },
      { title: '可可的芝士焗南瓜', tag: '甜蜜', energy: '266 kcal', orderCount: 3 }
    ]
  }
}

// 加载轮播图
const loadBanners = async () => {
  try {
    const res = await getBannerList()
    const list = (res.data || []).map(item => ({
      id: item.id,
      image: item.image || 'https://dummyimage.com/800x400/FF7D58/ffffff&text=Banner',
      title: item.title,
      description: item.description,
      linkUrl: item.linkUrl
    }))
    
    if (list.length === 0) {
      list.push({
        id: 1,
        image: 'https://dummyimage.com/800x400/FF7D58/ffffff&text=家庭美食',
        title: '家庭美食精选',
        description: '每日新鲜食材，用心烹饪每一道菜'
      })
    }
    
    banners.value = list
  } catch (error) {
    console.error('加载轮播图失败:', error)
    banners.value = [{
      id: 1,
      image: 'https://dummyimage.com/800x400/FF7D58/ffffff&text=家庭美食',
      title: '家庭美食精选',
      description: '每日新鲜食材，用心烹饪每一道菜'
    }]
  }
}

const onBannerImageError = (index) => {
  console.error('轮播图加载失败:', index)
  if (banners.value[index]) {
    banners.value[index].image = 'https://dummyimage.com/800x400/FF7D58/ffffff&text=图片加载失败'
  }
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

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

onShareAppMessage((res) => {
  return {
    title: '美食菜单 - 家宴能量中心',
    path: '/pages/index/index',
    imageUrl: banners.value.length > 0 ? banners.value[0].image : ''
  }
})

onShow(() => {
  applyCurrentTheme()
  updateClock()
  if (!timer) {
    timer = setInterval(updateClock, 1000)
  }
})
</script>

<style lang="scss" scoped>
.page {
  padding: 0 20rpx; /* 左右间距 20px */
  min-height: 100vh;
  background-color: var(--bg-page);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
  padding-bottom: 40rpx;
}

/* 通用部分样式 */
.section {
    margin-bottom: 32rpx; /* 模块间距 */
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    padding: 0 10rpx;

    .section-title {
        font-size: 34rpx;
        font-weight: 700;
        color: var(--text-primary);
    }

    .mini-tip, .link-text {
        font-size: 24rpx;
        color: var(--text-secondary);
    }
}

/* Header / 能量中心 */
.header-section {
    position: relative;
    padding: 60rpx 20rpx 40rpx;
    margin-bottom: 20rpx;
    text-align: center;
    overflow: hidden;

    .header-content {
        position: relative;
        z-index: 2;
    }

    .eyebrow {
        font-size: 24rpx;
        color: var(--accent-orange);
        letter-spacing: 2rpx;
        margin-bottom: 10rpx;
        display: block;
        font-weight: 600;
    }

    .time-display {
        margin: 20rpx 0;
        
        .time-text {
            font-size: 80rpx; /* 48px+ -> 48*2 = 96rpx approx */
            font-weight: 800;
            color: var(--text-primary);
            line-height: 1;
            font-variant-numeric: tabular-nums;
        }
    }

    .subtitle {
        font-size: 28rpx;
        color: var(--text-secondary);
    }

    /* 装饰圆环，模拟 Header 区域的柔和背景光效 */
    .decoration-ring {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 300rpx;
        height: 300rpx;
        background: radial-gradient(circle, var(--accent-orange) 0%, transparent 70%);
        opacity: 0.1;
        filter: blur(40px);
        z-index: 1;
        pointer-events: none;
    }
}

/* 轮播图 */
.carousel-section {
    .banner-swiper {
        height: 360rpx;
        border-radius: var(--card-radius);
        overflow: hidden;
        box-shadow: var(--shadow-soft);
    }

    .banner-card {
        width: 100%;
        height: 100%;
        position: relative;

        .banner-image {
            width: 100%;
            height: 100%;
            border-radius: var(--card-radius);
        }

        .banner-overlay {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            padding: 40rpx 30rpx 30rpx;
            background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 100%);
            border-bottom-left-radius: var(--card-radius);
            border-bottom-right-radius: var(--card-radius);

            .banner-title {
                color: #fff;
                font-size: 32rpx;
                font-weight: 700;
                display: block;
                margin-bottom: 8rpx;
            }

            .banner-desc {
                color: rgba(255,255,255,0.9);
                font-size: 24rpx;
            }
        }
    }
}

/* 快捷操作 */
.quick-grid {
    display: flex;
    justify-content: space-between;
    gap: 20rpx;

    .quick-card {
        flex: 1;
        background: var(--bg-card);
        /* backdrop-filter: blur(10px); 兼容性注意 */
        border-radius: 20rpx;
        padding: 30rpx 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        box-shadow: var(--shadow-soft);
        transition: transform 0.2s;
        
        &:active {
            transform: scale(0.96);
        }
        
        /* 每个按钮使用不同的淡彩背景 */
        &.quick-card-0 { /* 一键叫饭 - 蓝/紫 */
             background: linear-gradient(135deg, rgba(20, 184, 255, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
             .quick-icon-placeholder { background: linear-gradient(135deg, #14b8ff, #a855f7); }
        }
        &.quick-card-1 { /* 今日菜单 - 橙/红 */
            background: linear-gradient(135deg, rgba(255, 159, 67, 0.1) 0%, rgba(255, 125, 88, 0.1) 100%);
             .quick-icon-placeholder { background: linear-gradient(135deg, #ff9f43, #ff7d58); }
        }
        &.quick-card-2 { /* 营销活动 - 绿/青 */
             background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(52, 211, 153, 0.1) 100%);
             .quick-icon-placeholder { background: linear-gradient(135deg, #10b981, #34d399); }
        }

        .quick-content {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .quick-icon-placeholder {
            width: 80rpx;
            height: 80rpx;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 16rpx;
            color: #fff;
            font-size: 36rpx;
            box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.1);
        }

        .action-label {
            font-size: 26rpx;
            font-weight: 600;
            color: var(--text-primary);
        }
    }
}

/* 明星菜列表 */
.dish-list {
    display: flex;
    flex-direction: column;
    gap: 24rpx;

    .dish-card {
        background: var(--bg-card);
        border-radius: var(--card-radius);
        padding: 24rpx;
        display: flex;
        align-items: center;
        gap: 24rpx;
        box-shadow: var(--shadow-soft);
        position: relative;
        overflow: hidden;

        /* 毛玻璃效果强化 深色模式 */
        .theme-dark & {
           background: rgba(30, 41, 59, 0.7);
           border: 1px solid rgba(255,255,255,0.05);
        }

        .dish-image {
            width: 140rpx;
            height: 140rpx;
            border-radius: 20rpx;
            background-color: var(--bg-input);
            flex-shrink: 0;
        }

        .dish-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 120rpx;
            padding-right: 20rpx;
        }

        .dish-title {
            font-size: 32rpx;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 8rpx;
        }

        .dish-desc {
            font-size: 24rpx;
            color: var(--text-secondary);
            display: -webkit-box;
            -webkit-line-clamp: 1;
            line-clamp: 1;
            -webkit-box-orient: vertical;
            overflow: hidden;
            margin-bottom: 12rpx;
        }

        .dish-tag {
            align-self: flex-start;
            font-size: 20rpx;
            color: #fff;
            background: var(--accent-orange);
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
            opacity: 0.9;
        }

        .dish-action {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            
            .fire-badge {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                /* background: var(--bg-input); */
                border-radius: 12rpx;
                padding: 10rpx;
                min-width: 60rpx;

                .fire-icon {
                    font-size: 36rpx;
                    margin-bottom: 4rpx;
                    filter: drop-shadow(0 2px 4px rgba(255, 69, 0, 0.3));
                }

                .fire-count {
                    font-size: 22rpx;
                    font-weight: 700;
                    color: var(--accent-orange);
                }
            }
        }
    }
}
</style>
