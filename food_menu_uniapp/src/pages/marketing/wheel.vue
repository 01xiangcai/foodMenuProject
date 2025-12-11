<template>
  <view class="wheel-page">
    <!-- 背景装饰 (改为fixed定位) -->
    <view class="bg-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>

    <!-- 自定义导航栏 -->
    <view class="custom-nav" :class="{ 'nav-scrolled': showNavTitle }" :style="{ height: navHeight + 'px' }">
      <view class="nav-content" :style="{ marginTop: menuButtonTop + 'px', height: menuButtonHeight + 'px', lineHeight: menuButtonHeight + 'px' }">
        <view class="back-btn" @click="goBack" :style="{ width: menuButtonHeight + 'px', height: menuButtonHeight + 'px' }">
          <text class="icon">←</text>
        </view>
        <text class="nav-title" :style="{ opacity: showNavTitle ? 1 : 0 }">{{ activity.activityName || '幸运大转盘' }}</text>
      </view>
    </view>

    <!-- 页面主体内容 (增加顶部padding占位) -->
    <view class="page-content" :style="{ paddingTop: navHeight + 'px' }">
      
      <!-- 活动头部信息 (重构布局) -->
      <view class="activity-header">
        <text class="main-title">{{ activity.activityName }}</text>
        <text class="subtitle">{{ activity.activityDesc }}</text>
        
        <!-- 操作栏 -->
        <view class="action-bar">
          <view class="remain-times">
            <text class="label">剩余次数</text>
            <text class="count">{{ remainTimes === null ? '∞' : remainTimes }}</text>
          </view>
          <view class="my-prizes-btn" @click="showMyPrizes">
            <text class="icon">🎁</text>
            <text class="text">我的奖品</text>
          </view>
        </view>
      </view>

      <!-- 大转盘容器 -->
      <view class="wheel-container">
        <!-- 外圈装饰 -->
        <view class="outer-ring">
          <view v-for="i in 12" :key="i" class="ring-dot" :style="{ transform: `rotate(${i * 30}deg) translateY(-180rpx)` }"></view>
        </view>

        <!-- 转盘主体 -->
        <view class="wheel-wrapper" :style="{ transform: `rotate(${rotation}deg)` }">
          <view class="wheel" :style="getWheelStyle()">
            <!-- 奖品文字层 -->
            <view 
              v-for="(prize, index) in prizes" 
              :key="prize.id"
              class="prize-item"
              :style="getPrizeItemStyle(index)"
            >
              <view class="prize-content">
                <text class="prize-name">{{ prize.prizeName }}</text>
                <text class="prize-value" v-if="prize.prizeValue">{{ prize.prizeValue }}元</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 中心指针 -->
        <view class="pointer-wrapper" @click="startSpin">
          <view class="pointer">
            <view class="pointer-arrow"></view>
          </view>
          <view class="center-button" :class="{ spinning: isSpinning }">
            <text class="btn-text">{{ isSpinning ? '抽奖中' : '开始' }}</text>
            <text class="btn-subtext" v-if="!isSpinning">抽奖</text>
          </view>
        </view>
      </view>

      <!-- 奖品列表 -->
      <view class="prize-list">
        <view class="list-title">
          <text class="title-text">奖品列表</text>
          <view class="title-line"></view>
        </view>
        <!-- 改为普通view列表，让页面整体滚动 -->
        <view class="prize-items-container">
          <view v-for="prize in prizes" :key="prize.id" class="prize-card-item">
            <view class="prize-icon">🎁</view>
            <view class="prize-info">
              <text class="name">{{ prize.prizeName }}</text>
              <text class="desc">{{ prize.prizeType === 'COUPON' ? '优惠券' : prize.prizeType === 'POINTS' ? '积分' : '实物奖品' }}</text>
            </view>
            <view class="prize-stock">
              <text class="stock-text">剩余: {{ prize.remainQuantity === -1 ? '∞' : prize.remainQuantity }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部安全区占位 -->
      <view class="safe-area-bottom"></view>
    </view>

    <!-- 中奖弹窗 -->
    <view class="prize-modal" v-if="showPrizeModal" @click="closePrizeModal" catchtouchmove="true">
      <view class="modal-content" @click.stop>
        <view class="confetti">
          <view v-for="i in 20" :key="i" class="confetti-piece" :style="getConfettiStyle(i)"></view>
        </view>
        <view class="modal-icon">🎉</view>
        <text class="modal-title">恭喜中奖!</text>
        <text class="modal-prize">{{ winPrize?.prizeName }}</text>
        <text class="modal-value" v-if="winPrize?.prizeValue">价值 {{ winPrize.prizeValue }} 元</text>
        <text class="modal-tip">奖品已发放到您的账户</text>
        <view class="modal-actions">
          <button class="modal-btn primary" @click="closePrizeModal">太棒了</button>
          <button class="modal-btn secondary" @click="continueSpin">再抽一次</button>
        </view>
      </view>
    </view>

    <!-- 我的奖品弹窗 -->
    <view class="my-prizes-modal" v-if="showMyPrizesModal" @click="closeMyPrizes" catchtouchmove="true">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">🎁 我的奖品</text>
          <view class="close-btn" @click="closeMyPrizes">✕</view>
        </view>
        
        <scroll-view 
          scroll-y 
          class="prizes-scroll" 
          v-if="myPrizesList.length > 0"
          @scrolltolower="loadMorePrizes"
          lower-threshold="50"
        >
          <view v-for="(item, index) in myPrizesList" :key="index" class="prize-card">
            <view class="prize-icon">🎁</view>
            <view class="prize-details">
              <text class="prize-name">{{ item.prizeName }}</text>
              <text class="prize-type">{{ getPrizeTypeText(item.prizeType) }}</text>
              <text class="prize-time">{{ formatTime(item.participateTime) }}</text>
            </view>
            <view class="prize-value" v-if="item.prizeValue">
              <text class="value">{{ item.prizeValue }}</text>
              <text class="unit">元</text>
            </view>
          </view>
          
          <!-- 加载更多提示 -->
          <view class="load-more" v-if="hasMorePrizes">
            <text class="loading-text" v-if="loadingMore">加载中...</text>
            <text class="loading-text" v-else>上拉加载更多</text>
          </view>
          <view class="no-more" v-else-if="myPrizesList.length > 0">
            <text>没有更多了</text>
          </view>
        </scroll-view>
        
        <view class="empty-state" v-else>
          <text class="empty-icon">📦</text>
          <text class="empty-text">还没有中奖记录</text>
          <text class="empty-tip">快去参与抽奖吧!</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onPageScroll, onLoad } from '@dcloudio/uni-app';
import { request } from '@/utils/request';

const activity = ref({});
const prizes = ref([]);
const remainTimes = ref(null);
const rotation = ref(0);
const isSpinning = ref(false);
const showPrizeModal = ref(false);
const winPrize = ref(null);
const showMyPrizesModal = ref(false);
const myPrizesList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const hasMorePrizes = ref(true);
const loadingMore = ref(false);
const allPrizesData = ref([]); // 存储所有奖品数据

// 导航栏相关
const navHeight = ref(64);
const statusBarHeight = ref(20);
const menuButtonHeight = ref(32);
const menuButtonTop = ref(24); // 胶囊距顶部距离
const showNavTitle = ref(false);

const activityId = ref('');

// onLoad 获取参数
onLoad((options) => {
  if (options && options.id) {
    activityId.value = options.id;
    loadActivity();
    loadRemainTimes();
  }
});

// 生成转盘背景样式 - 使用conic-gradient
const getWheelStyle = () => {
  const count = prizes.value.length;
  if (count === 0) return {};
  
  const colors = [
    '#667eea',
    '#f093fb',
    '#4facfe',
    '#43e97b',
    '#fa709a',
    '#30cfd0',
    '#a8edea',
    '#ff9a9e'
  ];
  
  const angle = 360 / count;
  let gradientStops = [];
  
  for (let i = 0; i < count; i++) {
    const startAngle = i * angle;
    const endAngle = (i + 1) * angle;
    const color = colors[i % colors.length];
    gradientStops.push(`${color} ${startAngle}deg ${endAngle}deg`);
  }
  
  return {
    background: `conic-gradient(${gradientStops.join(', ')})`
  };
};

// 计算每个奖品文字的位置
const getPrizeItemStyle = (index) => {
  const count = prizes.value.length;
  const angle = 360 / count;
  const rotation = angle * index + angle / 2;
  
  return {
    transform: `rotate(${rotation}deg)`
  };
};

// 获取彩纸样式
const getConfettiStyle = (index) => {
  const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe', '#43e97b', '#38f9d7'];
  const randomColor = colors[Math.floor(Math.random() * colors.length)];
  const randomX = Math.random() * 100 - 50;
  const randomDelay = Math.random() * 0.5;
  
  return {
    left: `${50 + randomX}%`,
    backgroundColor: randomColor,
    animationDelay: `${randomDelay}s`
  };
};

// 加载活动详情
const loadActivity = async () => {
  try {
    const res = await request({
      url: `/uniapp/marketing/activity/${activityId.value}`,
      method: 'GET'
    });
    
    if (res.code === 1) {
      activity.value = res.data;
      prizes.value = res.data.prizes || [];
      
      if (prizes.value.length === 0) {
        uni.showToast({
          title: '该活动暂无奖品',
          icon: 'none'
        });
      }
    }
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  }
};

// 加载剩余次数
const loadRemainTimes = async () => {
  try {
    const res = await request({
      url: `/uniapp/marketing/activity/${activityId.value}/remain-times`,
      method: 'GET'
    });
    
    if (res.code === 1) {
      remainTimes.value = res.data;
    }
  } catch (error) {
    console.error('加载剩余次数失败:', error);
  }
};

// 开始转动
const startSpin = async () => {
  if (isSpinning.value) return;
  
  if (remainTimes.value !== null && remainTimes.value <= 0) {
    uni.showToast({
      title: '参与次数已用完',
      icon: 'none'
    });
    return;
  }
  
  isSpinning.value = true;
  
  try {
    const res = await request({
      url: `/uniapp/marketing/activity/${activityId.value}/participate`,
      method: 'POST'
    });
    
    if (res.code === 1) {
      const result = res.data;
      
      // 检查是否有奖品数据
      if (!result.prize || !result.prize.id) {
        isSpinning.value = false;
        uni.showToast({
          title: result.message || '抽奖失败',
          icon: 'none'
        });
        return;
      }
      
      // 找到中奖奖品的索引
      const prizeIndex = prizes.value.findIndex(p => p.id === result.prize.id);
      
      if (prizeIndex === -1) {
        isSpinning.value = false;
        uni.showToast({
          title: '奖品数据错误',
          icon: 'none'
        });
        return;
      }
      
      // 计算目标角度
      const angle = 360 / prizes.value.length;
      const targetAngle = 360 - (prizeIndex * angle + angle / 2);
      const spinRounds = 5; // 转5圈
      const finalRotation = rotation.value + spinRounds * 360 + targetAngle;
      
      // 开始旋转动画
      rotation.value = finalRotation;
      
      // 等待动画完成
      setTimeout(() => {
        isSpinning.value = false;
        
        // 使用深拷贝确保每次都是新的对象,避免引用问题
        winPrize.value = result.prize ? { ...result.prize } : null;
        remainTimes.value = result.remainTimes;
        
        // 动画结束后,将角度标准化到0-360范围,避免下次旋转角度过大
        // 使用setTimeout确保在UI更新后执行
        setTimeout(() => {
          rotation.value = rotation.value % 360;
        }, 50);
        
        if (result.isWin) {
          showPrizeModal.value = true;
        } else {
          uni.showToast({
            title: result.message || '谢谢参与',
            icon: 'none'
          });
        }
      }, 4000);
    } else {
      isSpinning.value = false;
      uni.showToast({
        title: res.msg || '参与失败',
        icon: 'none'
      });
    }
  } catch (error) {
    isSpinning.value = false;
    uni.showToast({
      title: '参与失败',
      icon: 'none'
    });
  }
};

// 关闭中奖弹窗
const closePrizeModal = () => {
  showPrizeModal.value = false;
};

// 继续抽奖
const continueSpin = () => {
  showPrizeModal.value = false;
  setTimeout(() => {
    startSpin();
  }, 300);
};

// 返回
const goBack = () => {
  uni.navigateBack();
};

// 显示我的奖品
const showMyPrizes = async () => {
  showMyPrizesModal.value = true;
  // 重置分页状态
  currentPage.value = 1;
  myPrizesList.value = [];
  allPrizesData.value = [];
  hasMorePrizes.value = true;
  await loadMyPrizes();
};

// 关闭我的奖品弹窗
const closeMyPrizes = () => {
  showMyPrizesModal.value = false;
};

// 加载我的奖品列表
const loadMyPrizes = async () => {
  try {
    const res = await request({
      url: `/uniapp/marketing/activity/my-prizes`,
      method: 'GET'
    });
    
    if (res.code === 1) {
      // 将奖品数据与参与记录关联
      const prizesData = res.data || [];
      
      // 获取所有中奖记录
      const recordsRes = await request({
        url: `/uniapp/marketing/activity/my-records`,
        method: 'GET'
      });
      
      if (recordsRes.code === 1) {
        const records = recordsRes.data || [];
        // 将奖品信息和参与时间合并
        allPrizesData.value = records.map(record => {
          const prize = prizesData.find(p => p.id === record.prizeId);
          return {
            ...prize,
            participateTime: record.participateTime,
            recordId: record.id
          };
        }).filter(item => item.id); // 过滤掉没有奖品信息的记录
        
        // 加载第一页
        loadPageData();
      }
    }
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  }
};

// 加载分页数据
const loadPageData = () => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  const pageData = allPrizesData.value.slice(start, end);
  
  if (currentPage.value === 1) {
    myPrizesList.value = pageData;
  } else {
    myPrizesList.value = [...myPrizesList.value, ...pageData];
  }
  
  // 判断是否还有更多数据
  hasMorePrizes.value = end < allPrizesData.value.length;
};

// 加载更多奖品
const loadMorePrizes = () => {
  if (loadingMore.value || !hasMorePrizes.value) return;
  
  loadingMore.value = true;
  
  setTimeout(() => {
    currentPage.value++;
    loadPageData();
    loadingMore.value = false;
  }, 300);
};

// 获取奖品类型文本
const getPrizeTypeText = (type) => {
  const typeMap = {
    'COUPON': '优惠券',
    'POINTS': '积分',
    'PHYSICAL': '实物奖品'
  };
  return typeMap[type] || '奖品';
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = date.getHours();
  const minute = date.getMinutes();
  return `${month}月${day}日 ${hour}:${minute < 10 ? '0' + minute : minute}`;
};

onMounted(() => {
  // 获取系统信息以适配导航栏
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight;
  
  // #ifdef MP-WEIXIN
  const menuButtonInfo = uni.getMenuButtonBoundingClientRect();
  menuButtonHeight.value = menuButtonInfo.height || 32;
  menuButtonTop.value = menuButtonInfo.top || (statusBarHeight.value + 4);
  
  // 计算导航栏高度: 胶囊底部 + 间距(胶囊距顶部-状态栏高度)
  const gap = menuButtonInfo.top - systemInfo.statusBarHeight;
  navHeight.value = menuButtonInfo.bottom + gap;
  // #endif
  
  // #ifndef MP-WEIXIN
  navHeight.value = systemInfo.statusBarHeight + 44;
  menuButtonTop.value = systemInfo.statusBarHeight + 6; // 大致居中
  // #endif
});

onPageScroll((e) => {
  showNavTitle.value = e.scrollTop > 50;
});
</script>

<style lang="scss" scoped>
.wheel-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  /* overflow: hidden; Removed to allow scrolling */
  box-sizing: border-box;
}

// 背景装饰
.bg-decoration {
  position: fixed; /* Fixed to stay in background while scrolling */
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    animation: float 20s infinite ease-in-out;
    
    &.circle-1 {
      width: 400rpx;
      height: 400rpx;
      background: linear-gradient(135deg, #667eea, #764ba2);
      top: -200rpx;
      left: -200rpx;
    }
    
    &.circle-2 {
      width: 300rpx;
      height: 300rpx;
      background: linear-gradient(135deg, #f093fb, #f5576c);
      top: 50%;
      right: -150rpx;
      animation-delay: -5s;
    }
    
    &.circle-3 {
      width: 500rpx;
      height: 500rpx;
      background: linear-gradient(135deg, #4facfe, #00f2fe);
      bottom: -250rpx;
      left: 50%;
      animation-delay: -10s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-50rpx) rotate(180deg);
  }
}

// 自定义导航栏
.custom-nav {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 100;
  pointer-events: none;
  box-sizing: border-box;
  transition: background 0.3s;
  
  &.nav-scrolled {
    background: rgba(22, 33, 62, 0.95);
    backdrop-filter: blur(10px);
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.2);
  }
  
  .nav-content {
    position: relative;
    display: flex;
    align-items: center;
    padding: 0 20rpx;
    
    .back-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.2);
      backdrop-filter: blur(10px);
      border-radius: 50%;
      pointer-events: auto;
      
      .icon {
        font-size: 36rpx;
        color: #fff;
      }
    }
    
    .nav-title {
      flex: 1;
      text-align: center;
      font-size: 32rpx;
      font-weight: bold;
      color: #fff;
      margin-right: 32px;
      transition: opacity 0.3s;
    }
  }
}

// 页面顶部信息区
.activity-header {
  padding: 20rpx 40rpx;
  text-align: center;
  position: relative;
  z-index: 10;
  
  .main-title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: #ffffff;
    margin-bottom: 12rpx;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
  }
  
  .subtitle {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 40rpx;
  }
  
  .action-bar {
    display: flex;
    justify-content: center;
    gap: 30rpx;
    
    .remain-times {
      background: linear-gradient(135deg, #667eea, #764ba2);
      padding: 12rpx 30rpx;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      gap: 10rpx;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.4);
      
      .label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.9);
      }
      
      .count {
        font-size: 32rpx;
        font-weight: bold;
        color: #ffffff;
      }
    }
    
    .my-prizes-btn {
      background: rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(10px);
      padding: 12rpx 30rpx;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      gap: 10rpx;
      border: 1px solid rgba(255, 255, 255, 0.2);
      transition: all 0.3s;
      
      &:active {
        background: rgba(255, 255, 255, 0.25);
        transform: scale(0.98);
      }
      
      .icon { font-size: 28rpx; }
      .text { font-size: 24rpx; color: #fff; font-weight: 600; }
    }
  }
}

// 转盘容器
.wheel-container {
  position: relative;
  width: 600rpx;
  height: 600rpx;
  margin: 60rpx auto;
  
  .outer-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    animation: rotate 20s linear infinite;
    
    .ring-dot {
      position: absolute;
      width: 16rpx;
      height: 16rpx;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border-radius: 50%;
      top: 50%;
      left: 50%;
      margin: -8rpx 0 0 -8rpx;
      box-shadow: 0 0 20rpx rgba(102, 126, 234, 0.8);
    }
  }
  
  .wheel-wrapper {
    position: absolute;
    width: 520rpx;
    height: 520rpx;
    top: 40rpx;
    left: 40rpx;
    transition: transform 4s cubic-bezier(0.25, 0.1, 0.25, 1);
    
    .wheel {
      position: relative;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      overflow: hidden;
      box-shadow: 0 0 0 20rpx rgba(255, 255, 255, 0.1),
                  0 0 60rpx rgba(0, 0, 0, 0.3),
                  inset 0 0 40rpx rgba(0, 0, 0, 0.2);
      
      .prize-item {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        transform-origin: center center;
        
        .prize-content {
          position: absolute;
          top: 50rpx;
          left: 50%;
          transform: translateX(-50%);
          text-align: center;
          width: 140rpx;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          
          .prize-name {
            display: block;
            font-size: 26rpx;
            font-weight: bold;
            color: #ffffff;
            text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.8);
            margin-bottom: 8rpx;
            line-height: 1.2;
            word-break: break-all;
          }
          
          .prize-value {
            display: block;
            font-size: 22rpx;
            color: #fff;
            text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.8);
            font-weight: 700;
            background: rgba(0, 0, 0, 0.3);
            padding: 4rpx 12rpx;
            border-radius: 20rpx;
          }
        }
      }
    }
  }
  
  .pointer-wrapper {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 10;
    
    .pointer {
      position: absolute;
      top: -280rpx;
      left: 50%;
      transform: translateX(-50%);
      
      .pointer-arrow {
        width: 0;
        height: 0;
        border-left: 30rpx solid transparent;
        border-right: 30rpx solid transparent;
        border-top: 60rpx solid #ff6b6b;
        filter: drop-shadow(0 4rpx 8rpx rgba(255, 107, 107, 0.5));
      }
    }
    
    .center-button {
      width: 180rpx;
      height: 180rpx;
      background: linear-gradient(135deg, #ff6b6b, #ee5a6f);
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.5),
                  inset 0 -4rpx 8rpx rgba(0, 0, 0, 0.2);
      transition: all 0.3s;
      
      &:active {
        transform: scale(0.95);
      }
      
      &.spinning {
        animation: pulse 1s infinite;
      }
      
      .btn-text {
        font-size: 32rpx;
        font-weight: bold;
        color: #ffffff;
        text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
      }
      
      .btn-subtext {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.9);
        margin-top: 5rpx;
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.5);
  }
  50% {
    box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.8),
                0 0 40rpx rgba(255, 107, 107, 0.6);
  }
}

// 奖品列表
.prize-list {
  padding: 40rpx;
  margin-bottom: 40rpx;
  
  .list-title {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    
    .title-text {
      font-size: 32rpx;
      font-weight: bold;
      color: #ffffff;
      margin-right: 20rpx;
    }
    
    .title-line {
      flex: 1;
      height: 2rpx;
      background: linear-gradient(90deg, rgba(255, 255, 255, 0.3), transparent);
    }
  }
  
  .prize-card-item {
    background: rgba(255, 255, 255, 0.08);
    backdrop-filter: blur(10px);
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    display: flex;
    align-items: center;
    border: 1px solid rgba(255, 255, 255, 0.05);
    
    .prize-icon {
      font-size: 60rpx;
      margin-right: 24rpx;
    }
    
    .prize-info {
      flex: 1;
      
      .name {
        display: block;
        font-size: 28rpx;
        font-weight: bold;
        color: #ffffff;
        margin-bottom: 8rpx;
      }
      
      .desc {
        display: block;
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.6);
      }
    }
    
    .prize-stock {
      .stock-text {
        font-size: 24rpx;
        color: #ff6b6b;
        font-weight: bold;
        background: rgba(0,0,0,0.2);
        padding: 4rpx 12rpx;
        border-radius: 10rpx;
      }
    }
  }
}

.safe-area-bottom {
  padding-bottom: env(safe-area-inset-bottom);
  height: 40rpx;
}

// 中奖弹窗
.prize-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s;
  
  .modal-content {
    position: relative;
    width: 600rpx;
    background: linear-gradient(180deg, #ffffff, #f8f9fa);
    border-radius: 40rpx;
    padding: 80rpx 60rpx 60rpx;
    text-align: center;
    animation: scaleIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    overflow: hidden;
    
    .confetti {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;
      
      .confetti-piece {
        position: absolute;
        width: 20rpx;
        height: 20rpx;
        top: -20rpx;
        animation: confettiFall 3s ease-out forwards;
      }
    }
    
    .modal-icon {
      font-size: 120rpx;
      margin-bottom: 30rpx;
      animation: bounce 1s infinite;
    }
    
    .modal-title {
      display: block;
      font-size: 48rpx;
      font-weight: bold;
      background: linear-gradient(135deg, #667eea, #764ba2);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 20rpx;
    }
    
    .modal-prize {
      display: block;
      font-size: 36rpx;
      font-weight: bold;
      color: #333333;
      margin-bottom: 15rpx;
    }
    
    .modal-value {
      display: block;
      font-size: 28rpx;
      color: #ff6b6b;
      margin-bottom: 15rpx;
    }
    
    .modal-tip {
      display: block;
      font-size: 24rpx;
      color: #999999;
      margin-bottom: 40rpx;
    }
    
    .modal-actions {
      display: flex;
      gap: 20rpx;
      
      .modal-btn {
        flex: 1;
        height: 88rpx;
        border-radius: 44rpx;
        font-size: 28rpx;
        font-weight: bold;
        border: none;
        
        &.primary {
          background: linear-gradient(135deg, #667eea, #764ba2);
          color: #ffffff;
          box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
        }
        
        &.secondary {
          background: #f8f9fa;
          color: #667eea;
          border: 2rpx solid #667eea;
        }
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0.5);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20rpx);
  }
}

@keyframes confettiFall {
  to {
    transform: translateY(800rpx) rotate(720deg);
    opacity: 0;
  }
}

// 我的奖品弹窗
.my-prizes-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s;
  
  .modal-content {
    width: 680rpx;
    max-height: 80vh;
    background: linear-gradient(180deg, #ffffff, #f8f9fa);
    border-radius: 32rpx;
    overflow: hidden;
    animation: slideUp 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    
    .modal-header {
      padding: 40rpx 32rpx;
      background: linear-gradient(135deg, #667eea, #764ba2);
      display: flex;
      align-items: center;
      justify-content: space-between;
      
      .modal-title {
        font-size: 36rpx;
        font-weight: bold;
        color: #ffffff;
      }
      
      .close-btn {
        width: 56rpx;
        height: 56rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32rpx;
        color: #ffffff;
        transition: all 0.3s;
        
        &:active {
          background: rgba(255, 255, 255, 0.3);
          transform: scale(0.9);
        }
      }
    }
    
    .prizes-scroll {
      max-height: 60vh;
      padding: 24rpx;
      
      .prize-card {
        background: #ffffff;
        border-radius: 24rpx;
        padding: 32rpx;
        margin-bottom: 20rpx;
        display: flex;
        align-items: center;
        box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
        transition: all 0.3s;
        
        &:active {
          transform: scale(0.98);
        }
        
        .prize-icon {
          font-size: 72rpx;
          margin-right: 24rpx;
        }
        
        .prize-details {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 8rpx;
          
          .prize-name {
            font-size: 32rpx;
            font-weight: bold;
            color: #333333;
          }
          
          .prize-type {
            font-size: 24rpx;
            color: #667eea;
            background: rgba(102, 126, 234, 0.1);
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
            align-self: flex-start;
          }
          
          .prize-time {
            font-size: 22rpx;
            color: #999999;
          }
        }
        
        .prize-value {
          display: flex;
          flex-direction: column;
          align-items: center;
          padding: 16rpx 24rpx;
          background: linear-gradient(135deg, #667eea, #764ba2);
          border-radius: 16rpx;
          
          .value {
            font-size: 40rpx;
            font-weight: bold;
            color: #ffffff;
            line-height: 1;
          }
          
          .unit {
            font-size: 20rpx;
            color: rgba(255, 255, 255, 0.8);
            margin-top: 4rpx;
          }
        }
      }
    }
    
    .load-more, .no-more {
      padding: 32rpx;
      text-align: center;
      
      .loading-text, text {
        font-size: 24rpx;
        color: #999999;
      }
    }
    
    .empty-state {
      padding: 120rpx 40rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .empty-icon {
        font-size: 120rpx;
        margin-bottom: 24rpx;
        opacity: 0.5;
      }
      
      .empty-text {
        font-size: 32rpx;
        color: #666666;
        margin-bottom: 12rpx;
      }
      
      .empty-tip {
        font-size: 24rpx;
        color: #999999;
      }
    }
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
