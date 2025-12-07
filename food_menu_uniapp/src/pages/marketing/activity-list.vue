<template>
  <view class="activity-list">
    <!-- 头部 -->
    <view class="header">
      <text class="title">营销活动</text>
    </view>

    <!-- 活动列表 -->
    <view class="activities">
      <view 
        v-for="activity in activities" 
        :key="activity.id" 
        class="activity-card"
        @click="goToActivity(activity)"
      >
        <!-- 活动横幅 -->
        <image 
          v-if="activity.bannerImage" 
          :src="activity.bannerImage" 
          class="banner"
          mode="aspectFill"
        />
        
        <!-- 活动信息 -->
        <view class="info">
          <view class="name">{{ activity.activityName }}</view>
          <view class="desc">{{ activity.activityDesc || '参与活动,赢取丰厚奖品!' }}</view>
          
          <view class="meta">
            <view class="type-tag">{{ getActivityTypeLabel(activity.activityType) }}</view>
            <view class="time">{{ formatTime(activity.endTime) }}截止</view>
          </view>
        </view>

        <!-- 参与按钮 -->
        <view class="action">
          <button class="btn-participate" @click.stop="participate(activity)">
            立即参与
          </button>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && activities.length === 0" class="empty">
        <text class="empty-text">暂无活动</text>
      </view>

      <!-- 加载中 -->
      <view v-if="loading" class="loading">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request';

const activities = ref([]);
const loading = ref(false);

// 活动类型标签
const getActivityTypeLabel = (type) => {
  const typeMap = {
    LOTTERY: '抽奖',
    WHEEL: '大转盘',
    COUPON: '优惠券',
    POINTS_EXCHANGE: '积分兑换',
    SIGN_IN: '签到',
    GROUP_BUY: '拼团'
  };
  return typeMap[type] || type;
};

// 格式化时间
const formatTime = (timeStr) => {
  const date = new Date(timeStr);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${month}月${day}日`;
};

// 加载活动列表
const loadActivities = async () => {
  loading.value = true;
  try {
    const res = await request({
      url: '/uniapp/marketing/activity/list',
      method: 'GET'
    });
    
    if (res.code === 1) {
      activities.value = res.data || [];
    } else {
      uni.showToast({
        title: res.msg || '加载失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('加载活动失败:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 跳转到活动详情
const goToActivity = (activity) => {
  // 根据活动类型跳转到不同页面
  const typePageMap = {
    LOTTERY: '/pages/marketing/lottery',
    WHEEL: '/pages/marketing/wheel',
    COUPON: '/pages/marketing/coupon'
  };
  
  const page = typePageMap[activity.activityType];
  if (page) {
    uni.navigateTo({
      url: `${page}?id=${activity.id}`
    });
  } else {
    uni.showToast({
      title: '该活动类型暂未开放',
      icon: 'none'
    });
  }
};

// 参与活动
const participate = async (activity) => {
  // 显示加载提示
  uni.showLoading({
    title: '参与中...',
    mask: true
  });
  
  try {
    const res = await request({
      url: `/uniapp/marketing/activity/${activity.id}/participate`,
      method: 'POST'
    });
    
    uni.hideLoading();
    
    if (res.code === 1) {
      const result = res.data;
      
      // 显示中奖结果
      if (result.isWin) {
        uni.showModal({
          title: '🎉 恭喜中奖!',
          content: `您获得了: ${result.prize?.prizeName || '神秘奖品'}\n\n${result.remainTimes !== null && result.remainTimes !== undefined ? `剩余参与次数: ${result.remainTimes}` : ''}`,
          showCancel: false,
          confirmText: '太棒了'
        });
      } else {
        uni.showModal({
          title: '提示',
          content: `${result.message || '谢谢参与,再接再厉!'}\n\n${result.remainTimes !== null && result.remainTimes !== undefined ? `剩余参与次数: ${result.remainTimes}` : ''}`,
          showCancel: false,
          confirmText: '知道了'
        });
      }
    } else {
      // 显示错误信息
      uni.showModal({
        title: '提示',
        content: res.msg || '参与失败,请稍后重试',
        showCancel: false,
        confirmText: '知道了'
      });
    }
  } catch (error) {
    uni.hideLoading();
    console.error('参与活动失败:', error);
    uni.showModal({
      title: '提示',
      content: error.message || '参与失败,请检查网络连接',
      showCancel: false,
      confirmText: '知道了'
    });
  }
};

onMounted(() => {
  loadActivities();
});
</script>

<style lang="scss" scoped>
.activity-list {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
  padding-bottom: 40rpx;
}

.header {
  padding: 40rpx 30rpx 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .title {
    font-size: 48rpx;
    font-weight: bold;
    color: #ffffff;
  }
}

.activities {
  padding: 20rpx 30rpx;
}

.activity-card {
  background: #ffffff;
  border-radius: 24rpx;
  margin-bottom: 30rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
}

.banner {
  width: 100%;
  height: 320rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.info {
  padding: 30rpx;
}

.name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 16rpx;
}

.desc {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 20rpx;
  line-height: 1.6;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.type-tag {
  display: inline-block;
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-size: 24rpx;
  border-radius: 20rpx;
}

.time {
  font-size: 24rpx;
  color: #999999;
}

.action {
  padding: 0 30rpx 30rpx;
}

.btn-participate {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  border: none;
  box-shadow: 0 8rpx 16rpx rgba(102, 126, 234, 0.4);
  
  &:active {
    opacity: 0.8;
  }
}

.empty {
  text-align: center;
  padding: 120rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
}

.loading {
  text-align: center;
  padding: 60rpx 0;
  font-size: 28rpx;
  color: #999999;
}
</style>
