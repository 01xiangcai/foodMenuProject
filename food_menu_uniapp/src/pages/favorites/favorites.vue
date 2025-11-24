<template>
  <view class="page">
    <scroll-view class="favorites-scroll" scroll-y>
      <view class="favorites-grid" v-if="favorites.length > 0">
        <view 
          class="dish-card glass-card"
          v-for="dish in favorites" 
          :key="dish.id"
          @tap="goToDetail(dish.id)"
        >
          <image class="dish-image" :src="dish.image" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-desc">{{ dish.description }}</text>
            <view class="dish-footer">
              <text class="dish-price">¥{{ dish.price }}</text>
              <view class="btn-unfavorite" @tap.stop="unfavorite(dish.id)">
                <text>❤️</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty" v-else>
        <text class="icon">❤️</text>
        <text class="text">还没有收藏的菜品</text>
        <view class="btn-go-shop" @tap="goToMenu">
          <text>去点餐</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const favorites = ref([
  {
    id: 1,
    name: '宫保鸡丁',
    description: '经典川菜，香辣可口',
    price: 38,
    image: 'https://dummyimage.com/400x300/ff6b6b/ffffff&text=宫保鸡丁'
  },
  {
    id: 2,
    name: '红烧肉',
    description: '肥而不腻，入口即化',
    price: 48,
    image: 'https://dummyimage.com/400x300/4ecdc4/ffffff&text=红烧肉'
  }
])

// 跳转详情
const goToDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${dishId}`
  })
}

// 取消收藏
const unfavorite = (dishId) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏吗？',
    success: (res) => {
      if (res.confirm) {
        const index = favorites.value.findIndex(item => item.id === dishId)
        if (index > -1) {
          favorites.value.splice(index, 1)
          uni.showToast({
            title: '已取消收藏',
            icon: 'success'
          })
        }
      }
    }
  })
}

// 去点餐
const goToMenu = () => {
  uni.switchTab({
    url: '/pages/menu/menu'
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #050a1f;
}

.favorites-scroll {
  height: 100vh;
  padding: 20rpx;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.dish-card {
  overflow: hidden;
  transition: transform 0.3s;
  
  &:active {
    transform: scale(0.95);
  }
}

.dish-image {
  width: 100%;
  height: 300rpx;
  border-radius: 12rpx;
}

.dish-info {
  padding: 20rpx;
}

.dish-name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-desc {
  display: block;
  font-size: 24rpx;
  color: #8b8fa3;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 36rpx;
  font-weight: 700;
  color: #14b8ff;
}

.btn-unfavorite {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 40rpx;
  }
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  
  .icon {
    font-size: 120rpx;
    margin-bottom: 30rpx;
  }
  
  .text {
    font-size: 32rpx;
    color: #8b8fa3;
    margin-bottom: 40rpx;
  }
}

.btn-go-shop {
  padding: 24rpx 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40rpx;
  color: #fff;
  font-size: 28rpx;
}
</style>
