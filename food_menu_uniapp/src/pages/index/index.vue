<template>
  <view class="page" :class="'theme-' + currentTheme">
    <!-- Hero 区域: 能量中心 -->
    <!-- Hero 区域: 动态能量卡片 -->
    <view class="hero-card">
      <view class="hero-bg"></view>
      <view class="hero-content">
        <view class="hero-text">
          <view class="greeting-row">
            <text class="greeting-text">{{ timeState.greeting }}</text>
            <text class="greeting-emoji">{{ timeState.emoji }}</text>
          </view>
          <text class="hero-title">今天想吃点什么？</text>
          <text class="hero-subtitle">家宴能量中心 · {{ currentTime }}</text>
        </view>
        <view class="hero-visual">
          <text class="hero-icon floated">🥘</text>
        </view>
      </view>
    </view>

    <!-- 轮播图 -->
    <view class="section carousel-section">
      <view class="section-header">
        <!-- <text class="section-title">美食轮播</text> -->
        <!-- <text class="mini-tip">左右滑动查看更多</text> -->
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

    <!-- 今日菜单 -->
    <view class="section today-menu-section">
      <view class="section-header">
        <text class="section-title">今日菜单</text>
        <text class="link-text" @tap="navigateTo('/pages/order/daily-meal-order-detail')">查看全部</text>
      </view>
      
      <view class="meal-cards">
        <view 
          class="meal-card" 
          v-for="meal in todayMeals" 
          :key="meal.period"
          :class="'meal-' + meal.period.toLowerCase()"
          @tap="goToMealDetail(meal.id)"
        >
          <view class="meal-header">
            <view class="meal-info">
              <text class="meal-icon">{{ meal.icon }}</text>
              <text class="meal-name">{{ meal.name }}</text>
            </view>
            <view class="meal-status" :class="'status-' + meal.status">
              <text>{{ meal.statusText }}</text>
            </view>
          </view>
          
          <view class="meal-stats">
            <view class="stat-item">
              <text class="stat-label">参与</text>
              <text class="stat-value">{{ meal.memberCount || 0 }}人</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">菜品</text>
              <text class="stat-value">{{ meal.dishCount || 0 }}道</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">金额</text>
              <text class="stat-value">¥{{ meal.totalAmount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
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
               <text v-if="index===0">🤖</text>
               <text v-if="index===1">👆</text>
               <text v-if="index===2"></text>
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
    <!-- AI 客服悬浮球 -->
    <movable-area v-if="!csOpen" class="cs-movable-area">
      <movable-view 
        class="cs-float-ball" 
        direction="all" 
        :x="csPosX" 
        :y="csPosY" 
        @change="onCsMove"
        @touchstart="resetCsActive"
      >
        <view :class="['cs-ball-inner', !csActive ? 'is-inactive' : '', csSnapDirection ? 'snap-' + csSnapDirection : '']">
          <!-- 侧边提示气泡 -->
          <view class="cs-prompt" :class="{ 'fade-out': !csActive }" @tap="handleOpenCs">AI专属客服</view>
          <!-- 本体 -->
          <text class="cs-ball-icon" @tap="handleOpenCs">💬</text>
          <view v-if="csActive" class="cs-ball-ping"></view>
        </view>
      </movable-view>
    </movable-area>

    <!-- 耸天层蒙层 -->
    <view v-if="csOpen" class="cs-mask" @tap.self="closeCs"></view>

    <!-- 聊天弹窗 -->
    <view v-if="csOpen" class="cs-window">
      <view class="cs-header">
        <view class="cs-header-left">
          <text class="cs-bot-emoji">🤖</text>
          <view>
            <text class="cs-bot-name">{{ csAppName }}</text>
            <view class="cs-status-row">
              <view class="cs-status-dot"></view>
              <text class="cs-status-text">在线</text>
            </view>
          </view>
        </view>
        <view class="cs-header-right">
          <text class="cs-btn" @tap.stop="resetCs">🔄</text>
          <text class="cs-btn" @tap.stop="closeCs">✕</text>
        </view>
      </view>

      <scroll-view class="cs-body" scroll-y :scroll-into-view="csScrollId" scroll-with-animation>
        <view class="cs-msg-list">
          <view
            v-for="(msg, idx) in csMessages"
            :key="idx"
            :id="'cs-msg-' + idx"
            class="cs-msg-row"
            :class="msg.role"
          >
            <text class="cs-avatar">{{ msg.role === 'assistant' ? '🤖' : '🧑' }}</text>
            <view class="cs-bubble" :style="msg.role === 'user' ? { backgroundColor: csTheme } : {}">
              <text class="cs-bubble-text" :style="msg.role === 'user' ? { color: '#fff' } : {}">{{ msg.content }}</text>
            </view>
          </view>
          <view v-if="csTyping" id="cs-typing" class="cs-msg-row assistant">
            <text class="cs-avatar">🤖</text>
            <view class="cs-bubble cs-typing">
              <view class="cs-dot"></view>
              <view class="cs-dot"></view>
              <view class="cs-dot"></view>
            </view>
          </view>
        </view>
      </scroll-view>

      <view class="cs-footer">
        <textarea
          v-model="csInput"
          class="cs-input"
          placeholder="说点什么..."
          :auto-height="true"
          :maxlength="500"
          :cursor-spacing="20"
          :show-confirm-bar="false"
        />
        <view
          class="cs-send"
          :style="{ backgroundColor: csInput.trim() && !csTyping ? csTheme : '#cbd5e1' }"
          @tap="csSend"
        >
          <text class="cs-send-text">发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { getBannerList, getTopDishes } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { getDishImage } from '@/utils/image'
import { onShareAppMessage, onShow } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { useAiChat } from '@/composables/useAiChat'

// 保存悬浮球位置，防止刷新后重置
const csPosX = ref(0)
const csPosY = ref(0)
// 悬浮球闲置半隐藏效果
const csActive = ref(true)
const csSnapDirection = ref('') // 'left' 或 'right'
let csActiveTimer = null
let csCurrentX = 0
let winWidth = 0
let ballRadius = 25 // 默认 55px 的一半

const resetCsActive = () => {
  csActive.value = true
  csSnapDirection.value = ''
  if (csActiveTimer) clearTimeout(csActiveTimer)
  
  // 唤醒时：如果在贴边边缘附近，把它稍微拽出来一点
  if (winWidth > 0) {
    if (csCurrentX <= 10) {
      csPosX.value = 10
    } else if (csCurrentX >= winWidth - ballRadius * 2 - 10) {
      csPosX.value = winWidth - ballRadius * 2 - 10
    }
  }

  csActiveTimer = setTimeout(() => {
    csActive.value = false
    // 闲置时：因为 movable-view 不允许跑出区域，所以用内部的 translateX 类来实现半隐藏
    if (winWidth > 0) {
      if (csCurrentX < winWidth / 2) {
        csPosX.value = 0
        csSnapDirection.value = 'left'
      } else {
        csPosX.value = winWidth - ballRadius * 2
        csSnapDirection.value = 'right'
      }
    }
  }, 3000)
}

const handleOpenCs = () => {
  resetCsActive()
  openCs()
}

onMounted(() => {
  const sysInfo = uni.getSystemInfoSync()
  winWidth = sysInfo.windowWidth
  const ballSize = winWidth / 750 * 110
  ballRadius = ballSize / 2
  
  const defaultX = uni.getStorageSync('cs_pos_x')
  const defaultY = uni.getStorageSync('cs_pos_y')
  if (defaultX === '' || defaultY === '') {
    csPosX.value = winWidth - ballSize - 16
    csPosY.value = sysInfo.windowHeight - ballSize - 90
  } else {
    csPosX.value = Number(defaultX)
    csPosY.value = Number(defaultY)
  }
  csCurrentX = csPosX.value
  resetCsActive()
})

const onCsMove = (e) => {
  csCurrentX = e.detail.x
  if (e.detail.source === 'touch') {
    resetCsActive()
    uni.setStorageSync('cs_pos_x', e.detail.x)
    uni.setStorageSync('cs_pos_y', e.detail.y)
  }
}

// 使用主题
const { currentTheme, loadTheme, applyCurrentTheme } = useTheme()

// 响应式数据
const currentTime = ref('')
const banners = ref([])
const quickActions = ref([
  // { label: 'AI助手', desc: '智能推荐菜品', link: '/pages/ai/ai-assistant' },
  { label: '一键叫饭', desc: '告诉家人开饭啦', link: '/pages/order/list' },
  { label: '营销活动', desc: '参与抽奖赢好礼', link: '/pages/marketing/activity-list' }
])
const featuredDishes = ref([])
const todayMeals = ref([])
const timeState = ref({
  greeting: '你好',
  emoji: '👋'
})

let timer = null

// 更新时钟
const updateClock = () => {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${h}:${m}:${s}`
}

const updateGreeting = () => {
    const hour = new Date().getHours()
    if (hour >= 5 && hour < 11) {
        timeState.value = { greeting: '早安，开启活力一天', emoji: '🍳' }
    } else if (hour >= 11 && hour < 14) {
        timeState.value = { greeting: '午安，记得按时吃饭', emoji: '🍱' }
    } else if (hour >= 14 && hour < 17) {
        timeState.value = { greeting: '午后时光，来点下午茶', emoji: '☕' }
    } else if (hour >= 17 && hour < 22) {
        timeState.value = { greeting: '晚上好，犒劳一下自己', emoji: '🍷' }
    } else {
        timeState.value = { greeting: '夜深了，早点休息', emoji: '🌙' }
    }
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

// 加载今日菜单
const loadTodayMeals = async () => {
  try {
    const token = uni.getStorageSync('fm_token')
    
    // 默认数据
    const defaultMeals = [
      { period: 'BREAKFAST', name: '早餐', icon: '🍳', status: 0, statusText: '收集中', memberCount: 0, dishCount: 0, totalAmount: 0 },
      { period: 'LUNCH', name: '中餐', icon: '🍱', status: 0, statusText: '收集中', memberCount: 0, dishCount: 0, totalAmount: 0 },
      { period: 'DINNER', name: '晚餐', icon: '🍷', status: 0, statusText: '收集中', memberCount: 0, dishCount: 0, totalAmount: 0 }
    ]
    
    // 先设置默认数据,确保界面有内容
    todayMeals.value = defaultMeals
    
    // 如果未登录,直接返回
    if (!token) {
      return
    }
    
    // 尝试加载真实数据
    try {
      const res = await request({
        url: '/uniapp/daily-meal-order/today',
        method: 'GET'
      })
      
      // 后端返回code=1表示成功
      if (res.code === 1 && res.data) {
        const data = res.data
        const mealMap = {
          'BREAKFAST': { name: '早餐', icon: '🍳' },
          'LUNCH': { name: '中餐', icon: '🍱' },
          'DINNER': { name: '晚餐', icon: '🍷' }
        }
        
        todayMeals.value = ['BREAKFAST', 'LUNCH', 'DINNER'].map(period => {
          const meal = data.find(m => m.mealPeriod === period)
          const statusMap = { 0: '收集中', 1: '已确认', 2: '已截止', 3: '已出餐' }
          
          return {
            id: meal?.id,
            period,
            name: mealMap[period].name,
            icon: mealMap[period].icon,
            status: meal?.status || 0,
            statusText: statusMap[meal?.status || 0],
            memberCount: meal?.memberCount || 0,
            dishCount: meal?.dishCount || 0,
            totalAmount: meal?.totalAmount || 0
          }
        })
      }
    } catch (apiError) {
      console.error('加载今日菜单API失败:', apiError)
      // API失败时保持默认数据
    }
  } catch (error) {
    console.error('加载今日菜单失败:', error)
    // 确保即使出错也有默认数据
    todayMeals.value = [
      { period: 'BREAKFAST', name: '早餐', icon: '🍳', status: 0, statusText: '收集中', memberCount: 0, dishCount: 0, totalAmount: 0 },
      { period: 'LUNCH', name: '中餐', icon: '🍱', status: 0, statusText: '收集中', memberCount: 0, dishCount: 0, totalAmount: 0 },
      { period: 'DINNER', name: '晚餐', icon: '🍷', status: 0, statusText: '收集中', memberCount: 0, dishCount: 0, totalAmount: 0 }
    ]
  }
}

// 跳转到大订单详情
const goToMealDetail = (id) => {
  if (!id) {
    uni.showToast({ title: '暂无订单数据', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/order/daily-meal-order-detail?id=${id}`
  })
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
  loadTheme()
  updateClock()
  updateGreeting()
  timer = setInterval(() => {
    updateClock()
    // 每分钟更新一次问候语，简单起见放在这里或者单独计时
    if (new Date().getSeconds() === 0) updateGreeting()
  }, 1000)
  loadBanners()
  loadFeaturedDishes()
  loadTodayMeals()
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
  updateGreeting()
  if (!timer) {
    timer = setInterval(updateClock, 1000)
  }
  // 刷新今日菜单数据,确保下单后数据及时更新
  loadTodayMeals()
})
// ====== AI 客服悬浮球（一行接入）======
const {
  csOpen, csAppName, csTheme,
  csMessages, csInput, csTyping, csScrollId,
  openCs, closeCs, resetCs, csSend
} = useAiChat('http://127.0.0.1:9900', 'ak_LDG0En1pIm8TLZQy1D3LRY5l')
// ==============================
</script>

<style lang="scss" scoped>
.page {
  padding: 0 20rpx; /* 左右间距 20px */
  min-height: 100vh;
  /* 基础背景色 */
  background-color: var(--bg-page); 
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
  padding-bottom: 40rpx;
}

/* 通用部分样式 */
.page {
  position: relative;
}
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

/* Header / 动态能量卡片 */
.hero-card {
    position: relative;
    /* height: 320rpx; 高度自适应或固定 */
    border-radius: 32rpx;
    margin-bottom: 32rpx;
    overflow: hidden;
    /* 浅色模式下背景变浅，文字改为深色以保证对比度 */
    color: #555; 
    /* 基础阴影 */
    box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.08);
}

.theme-dark .hero-card {
    /* 深色模式保持白字 */
    color: #fff;
}

/* 动态背景 - 极光效果 */
.hero-bg {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    /* 调浅颜色：柔和杏色/桃色渐变 */
    background: linear-gradient(135deg, #ffecd2, #fcb69f, #ffecd2);
    // background: linear-gradient(135deg, #fad0c4, #ffd1ff); /* 备选：糖果粉 */
    background-size: 200% 200%;
    animation: aurora 10s ease infinite;
    z-index: 0;
}

/* 深色模式背景调整 - 降低饱和度，更柔和 */
.theme-dark .hero-bg {
    /* 柔和的深夜蓝紫 */
    background: linear-gradient(135deg, #2b5876, #4e4376, #2b5876);
}

@keyframes aurora {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
}

.hero-content {
    position: relative;
    z-index: 2;
    padding: 40rpx 30rpx;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
}

.hero-text {
    flex: 1;
}

.greeting-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
  animation: slideUp 0.6s ease-out;
}

.greeting-text {
    font-size: 28rpx;
    font-weight: 500;
    opacity: 0.9;
    margin-right: 10rpx;
}

.greeting-emoji {
    font-size: 32rpx;
}

.hero-title {
    display: block;
    font-size: 44rpx; /* 大标题 */
    font-weight: 800;
    margin-bottom: 12rpx;
    letter-spacing: 1rpx;
    /* 浅色模式阴影减弱 */
    text-shadow: 0 2rpx 5rpx rgba(255,255,255,0.5);
    animation: slideUp 0.6s ease-out 0.1s backwards;
}

.theme-dark .hero-title {
    text-shadow: 0 2rpx 10rpx rgba(0,0,0,0.1);
}

.hero-subtitle {
    display: block;
    font-size: 22rpx;
    opacity: 0.8;
    font-weight: 400;
    animation: slideUp 0.6s ease-out 0.2s backwards;
}

.hero-visual {
    position: absolute;
    right: 20rpx;
    top: 20rpx;
    width: 140rpx;
    height: 140rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none; /* 穿透点击 */
}

.hero-icon {
    font-size: 100rpx;
    filter: drop-shadow(0 10rpx 10rpx rgba(0,0,0,0.1));
}

/* 悬浮动画 */
.floated {
    animation: float 4s ease-in-out infinite;
}

@keyframes float {
    0% { transform: translateY(0px) rotate(0deg); }
    50% { transform: translateY(-10px) rotate(2deg); }
    100% { transform: translateY(0px) rotate(0deg); }
}

@keyframes slideUp {
    from { opacity: 0; transform: translateY(10rpx); }
    to { opacity: 1; transform: translateY(0); }
}

/* 行动号召按钮区 */
.hero-action {
    position: relative;
    z-index: 2;
    margin: 0 30rpx 30rpx;
    
    /* 浅色模式按钮：半透明白底 + 深色文字 */
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);
    border-radius: 50rpx;
    padding: 16rpx 30rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 240rpx; /* 胶囊宽度 */
    transition: transform 0.2s;
    border: 1px solid rgba(255, 255, 255, 0.6);
}

.theme-dark .hero-action {
    /* 深色模式按钮 */
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.3);
}

.hero-action:active {
    transform: scale(0.96);
    background: rgba(255, 255, 255, 0.8);
}

.theme-dark .hero-action:active {
    background: rgba(255, 255, 255, 0.3);
}

.action-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #333; /* 浅色模式为深色字 */
}

.theme-dark .action-text {
    color: #fff;
}

.action-arrow {
    font-size: 28rpx;
    color: #333;
    font-weight: 700;
    transition: transform 0.3s;
}

.theme-dark .action-arrow {
    color: #fff;
}

.hero-action:active .action-arrow {
    transform: translateX(6rpx);
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
        &.quick-card-0 { /* AI助手 - 紫/粉 */
             background: linear-gradient(135deg, rgba(168, 85, 247, 0.1) 0%, rgba(236, 72, 153, 0.1) 100%);
             .quick-icon-placeholder { background: linear-gradient(135deg, #a855f7, #ec4899); }
        }
        &.quick-card-1 { /* 一键叫饭 - 蓝/紫 */
             background: linear-gradient(135deg, rgba(20, 184, 255, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
             .quick-icon-placeholder { background: linear-gradient(135deg, #14b8ff, #a855f7); }
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
            min-width: 0; /* 关键：允许 flex 子元素在空间不足时收缩 */
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 120rpx;
            padding-right: 10rpx;
        }

        .dish-title {
            font-size: 32rpx;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 4rpx;
            /* 防止标题过长也挤压布局 */
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            display: block;
        }

        .dish-desc {
            font-size: 24rpx;
            color: var(--text-secondary);
            margin-bottom: 8rpx;
            /* 严格的多行截断逻辑 */
            display: -webkit-box;
            -webkit-line-clamp: 1;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            word-break: break-all;
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

/* 今日菜单样式 */
.today-menu-section {
  .meal-cards {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }
  
  .meal-card {
    background: var(--bg-card);
    border-radius: 24rpx;
    padding: 24rpx;
    box-shadow: var(--shadow-soft);
    transition: all 0.3s ease;
    animation: slideUp 0.4s ease-out;
    
    &:active {
      transform: scale(0.98);
    }
    
    &.meal-breakfast {
      border-left: 6rpx solid #f59e0b;
    }
    
    &.meal-lunch {
      border-left: 6rpx solid #10b981;
    }
    
    &.meal-dinner {
      border-left: 6rpx solid #8b5cf6;
    }
  }
  
  .meal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  
  .meal-info {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }
  
  .meal-icon {
    font-size: 36rpx;
  }
  
  .meal-name {
    font-size: 32rpx;
    font-weight: 700;
    color: var(--text-primary);
  }
  
  .meal-status {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
    font-weight: 600;
    
    &.status-0 {
      background: rgba(59, 130, 246, 0.1);
      color: #3b82f6;
    }
    
    &.status-1 {
      background: rgba(16, 185, 129, 0.1);
      color: #10b981;
    }
    
    &.status-2 {
      background: rgba(107, 114, 128, 0.1);
      color: #6b7280;
    }
  }
  
  .meal-stats {
    display: flex;
    justify-content: space-around;
    padding-top: 16rpx;
    border-top: 2rpx solid var(--border-color);
  }
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6rpx;
  }
  
  .stat-label {
    font-size: 22rpx;
    color: var(--text-secondary);
  }
  
  .stat-value {
    font-size: 26rpx;
    font-weight: 700;
    color: var(--text-primary);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
/* ====== AI 客服悬浮球样式 ====== */
/* === 拖拽区域 ===
   占满全屏，但 pointer-events: none 不会拦截底层点击和滚动
*/
.cs-movable-area {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
}

/* 悬浮球 (现在由 movable-view 控制位置，去除 fixed) */
.cs-float-ball {
  width: 110rpx;
  height: 110rpx;
  overflow: visible;
  /* 非常关键：恢复子元素的交互事件，让悬浮球本身能够被拖拽和点击 */
  pointer-events: auto;
}

/* 小球内置容器：做形变、半缩进用 */
.cs-ball-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #7C3AED, #4e4376);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(124, 58, 237, 0.45);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.5s ease;
  position: relative;
}
.cs-ball-inner.is-inactive {
  opacity: 0.5;
}
.cs-ball-inner.snap-left {
  transform: translateX(-40rpx);
}
.cs-ball-inner.snap-right {
  transform: translateX(40rpx);
}

/* 提示气泡 */
.cs-prompt {
  position: absolute;
  right: 124rpx; /* 在悬浮球左侧 */
  top: 50%;
  transform: translateY(-50%);
  background: rgba(124, 58, 237, 0.9);
  color: #fff;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx 4rpx 30rpx 30rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(124, 58, 237, 0.3);
  animation: promptBreathe 3s infinite ease-in-out;
  pointer-events: auto;
  transition: opacity 0.5s ease;
}
.cs-prompt.fade-out {
  opacity: 0;
  pointer-events: none;
}
@keyframes promptBreathe {
  0%, 100% { transform: translateY(-50%) scale(1); }
  50% { transform: translateY(-50%) scale(1.05); }
}

.cs-ball-icon { font-size: 52rpx; z-index: 1; }

.cs-ball-ping {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3rpx solid #7C3AED;
  opacity: 0;
  animation: csPing 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}
@keyframes csPing {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.8); opacity: 0; }
}

/* 聊天窗口 */
.cs-window {
  position: fixed;
  right: 30rpx;
  bottom: 200rpx;
  width: 640rpx;
  height: 860rpx;
  background: #f8f9fb;
  border-radius: 32rpx;
  box-shadow: 0 20rpx 80rpx rgba(0,0,0,0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1001;
  animation: csWindowIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: right bottom;
}
@keyframes csWindowIn {
  from { transform: scale(0.1); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

/* 蒙层 */
.cs-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

/* 头部 */
.cs-header {
  background: linear-gradient(135deg, #7C3AED, #4e4376);
  padding: 28rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}
.cs-header-left { display: flex; align-items: center; gap: 16rpx; }
.cs-bot-emoji { font-size: 50rpx; }
.cs-bot-name { color: #fff; font-size: 30rpx; font-weight: 700; display: block; }
.cs-status-row { display: flex; align-items: center; gap: 8rpx; margin-top: 4rpx; }
.cs-status-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #4ade80; }
.cs-status-text { font-size: 22rpx; color: rgba(255,255,255,0.8); }
.cs-header-right { display: flex; gap: 16rpx; }
.cs-btn { font-size: 30rpx; color: rgba(255,255,255,0.9); padding: 10rpx; }

/* 消息列表 */
.cs-body { flex: 1; overflow: hidden; }
.cs-msg-list { padding: 24rpx 20rpx; }
.cs-msg-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 28rpx;
  gap: 14rpx;
}
.cs-msg-row.user { flex-direction: row-reverse; }
.cs-avatar { font-size: 38rpx; flex-shrink: 0; }
.cs-bubble {
  max-width: 78%;
  padding: 18rpx 24rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
}
.cs-msg-row.assistant .cs-bubble { border-bottom-left-radius: 4rpx; }
.cs-msg-row.user .cs-bubble { border-bottom-right-radius: 4rpx; }
.cs-bubble-text { font-size: 28rpx; line-height: 1.6; color: #334155; }

/* 打字动画 */
.cs-typing { display: flex; align-items: center; gap: 8rpx; padding: 22rpx 28rpx; }
.cs-dot {
  width: 10rpx; height: 10rpx;
  background: #94a3b8; border-radius: 50%;
  animation: csDot 1.4s infinite;
}
.cs-dot:nth-child(2) { animation-delay: 0.2s; }
.cs-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes csDot {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10rpx); }
}

/* 输入区 */
.cs-footer {
  padding: 16rpx 20rpx;
  background: #fff;
  border-top: 1px solid #f1f5f9;
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  flex-shrink: 0;
}
.cs-input {
  flex: 1;
  background: #f1f5f9;
  border-radius: 24rpx;
  padding: 16rpx 24rpx;
  font-size: 28rpx;
  max-height: 160rpx;
  min-height: 72rpx;
}
.cs-send {
  width: 120rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cs-send-text { font-size: 26rpx; font-weight: 600; color: #fff; }
/* ====== AI 客服结束 ====== */
</style>
