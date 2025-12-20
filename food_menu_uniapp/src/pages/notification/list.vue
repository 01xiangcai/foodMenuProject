<template>
  <view class="notification-list">
    <!-- 顶部操作栏 -->
    <view class="header-bar">
      <view class="title">系统通知</view>
      <text class="read-all" @click="handleReadAll" v-if="hasUnread">全部已读</text>
    </view>

    <!-- 通知列表 -->
    <scroll-view 
      scroll-y 
      class="notification-scroll"
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-if="notifications.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无通知</text>
      </view>

      <view 
        v-for="item in notifications" 
        :key="item.id"
        class="notification-item"
        :class="{ unread: !item.isRead }"
        @click="handleItemClick(item)"
      >
        <view class="item-icon">
          <image v-if="item.icon" :src="item.icon" mode="aspectFit" class="icon-img" />
          <view v-else class="icon-default">📢</view>
        </view>
        <view class="item-content">
          <view class="item-header">
            <text class="item-title">{{ item.title }}</text>
            <view v-if="!item.isRead" class="unread-dot"></view>
          </view>
          <text class="item-desc">{{ item.content }}</text>
          <text class="item-time">{{ formatTime(item.createTime) }}</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="loading" class="loading-more">
        <text>加载中...</text>
      </view>
      <view v-if="!hasMore && notifications.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getNotificationList, markAsRead, markAllAsRead, getUnreadCount } from '@/api/notification'

const notifications = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const hasMore = computed(() => notifications.value.length < total.value)
const hasUnread = computed(() => notifications.value.some(n => !n.isRead))

// 加载通知列表
const loadNotifications = async (refresh = false) => {
  if (loading.value) return
  
  if (refresh) {
    pageNum.value = 1
    refreshing.value = true
  }
  
  loading.value = true
  try {
    console.log('开始加载通知列表, pageNum:', pageNum.value, 'pageSize:', pageSize.value)
    const res = await getNotificationList(pageNum.value, pageSize.value)
    console.log('通知列表响应:', res)
    if (res.code === 1 && res.data) {
      const records = res.data.records || []
      if (refresh) {
        notifications.value = records
      } else {
        notifications.value = [...notifications.value, ...records]
      }
      total.value = res.data.total || 0
    } else {
      console.error('接口返回失败:', res)
      if (res.msg) {
        uni.showToast({ title: res.msg, icon: 'none' })
      }
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    const errMsg = error?.msg || error?.message || '加载失败'
    uni.showToast({ title: errMsg, icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  loadNotifications(true)
}

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    pageNum.value++
    loadNotifications()
  }
}

// 点击通知项
const handleItemClick = async (item) => {
  // 先标记已读
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 根据通知类型跳转
  navigateByNotificationType(item)
}

// 根据通知类型跳转到对应页面
const navigateByNotificationType = (item) => {
  const typeCode = item.typeCode
  let extra = {}
  
  console.log('点击通知:', item)
  console.log('typeCode:', typeCode)
  console.log('extra原始值:', item.extra)
  
  // 解析extra字段
  try {
    if (item.extra) {
      extra = typeof item.extra === 'string' ? JSON.parse(item.extra) : item.extra
    }
  } catch (e) {
    console.error('解析extra失败:', e)
  }
  
  console.log('解析后extra:', extra)
  
  // 餐次相关通知 -> 跳转到餐次订单详情
  const mealRelatedTypes = ['MEAL_PUBLISHED', 'MEAL_UPDATED', 'DISH_ACCEPTED', 'LATE_ORDER_ACCEPTED', 'DISH_REJECTED', 'MEAL_SERVED']
  if (mealRelatedTypes.includes(typeCode)) {
    if (extra.dailyMealOrderId) {
      // 有订单ID，跳转到订单详情
      uni.navigateTo({
        url: `/pages/order/daily-meal-order-detail?id=${extra.dailyMealOrderId}`
      })
    } else {
      // 没有订单ID，跳转到今日菜单
      uni.switchTab({
        url: '/pages/menu/menu'
      })
    }
    return
  }
  
  // 退款/消费通知 -> 跳转到钱包页面
  if (typeCode === 'REFUND_SUCCESS' || typeCode === 'PAYMENT_SUCCESS') {
    uni.navigateTo({
      url: '/pages/wallet/index'
    })
    return
  }
  
  // 其他通知不跳转
  console.log('未知通知类型，不跳转')
}

// 全部已读
const handleReadAll = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (error) {
    console.error('标记全部已读失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 1分钟内
  if (diff < 60 * 1000) return '刚刚'
  // 1小时内
  if (diff < 60 * 60 * 1000) return Math.floor(diff / 60 / 1000) + '分钟前'
  // 24小时内
  if (diff < 24 * 60 * 60 * 1000) return Math.floor(diff / 60 / 60 / 1000) + '小时前'
  // 7天内
  if (diff < 7 * 24 * 60 * 60 * 1000) return Math.floor(diff / 24 / 60 / 60 / 1000) + '天前'
  // 更久
  return date.toLocaleDateString('zh-CN')
}

// 监听下拉刷新
onMounted(() => {
  loadNotifications(true)
})

// 页面下拉刷新
uni.$on('onPullDownRefresh', () => {
  loadNotifications(true).finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.notification-list {
  min-height: 100vh;
  background: #f8f8f8;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
  
  .title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .read-all {
    font-size: 28rpx;
    color: #FF7D58;
  }
}

.notification-scroll {
  height: calc(100vh - 100rpx);
}

.notification-item {
  display: flex;
  padding: 28rpx 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  
  &.unread {
    background: #fff9f5;
  }
  
  .item-icon {
    flex-shrink: 0;
    width: 80rpx;
    height: 80rpx;
    margin-right: 24rpx;
    
    .icon-img {
      width: 100%;
      height: 100%;
      border-radius: 16rpx;
    }
    
    .icon-default {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40rpx;
      background: linear-gradient(135deg, #FFE0D0, #FFB591);
      border-radius: 16rpx;
    }
  }
  
  .item-content {
    flex: 1;
    min-width: 0;
    
    .item-header {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;
      
      .item-title {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .unread-dot {
        width: 14rpx;
        height: 14rpx;
        background: #FF4D4F;
        border-radius: 50%;
        margin-left: 12rpx;
        flex-shrink: 0;
      }
    }
    
    .item-desc {
      font-size: 26rpx;
      color: #666;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin-bottom: 8rpx;
    }
    
    .item-time {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  
  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 24rpx;
    opacity: 0.6;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.loading-more, .no-more {
  text-align: center;
  padding: 24rpx;
  font-size: 24rpx;
  color: #999;
}
</style>
