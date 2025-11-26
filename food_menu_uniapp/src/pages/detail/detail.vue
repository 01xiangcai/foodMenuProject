<template>
  <view class="page">
    <scroll-view class="detail-scroll" scroll-y>
      <!-- 1. 图片卡片 -->
      <view class="card image-card">
        <image class="dish-image" :src="dish.image" mode="aspectFill" />
      </view>

      <!-- 2. 信息卡片 -->
      <view class="card info-card">
        <view class="info-header">
          <text class="dish-name">{{ dish.name }}</text>
          <view class="price-row">
            <text class="price">¥{{ dish.price }}</text>
            <text class="category-tag">· {{ dish.categoryName || '家庭菜谱' }}</text>
          </view>
        </view>
        <text class="dish-desc">{{ dish.description }}</text>
      </view>

      <!-- 3. 评论卡片 -->
      <view class="card comment-card">
        <view class="comment-header">
          <text class="comment-title">家庭评论 {{ totalCommentCount }} 条留言</text>
          <view class="add-comment-btn" @tap="showCommentInput">
            <text>✍️ 发表评论</text>
          </view>
        </view>
        
        <!-- 评论列表 -->
        <CommentList 
          :comments="comments" 
          @reply="handleReply"
        />
      </view>
      
      <!-- 底部占位 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 购物车弹窗组件 -->
    <CartPopup 
      v-model:visible="cartPopupVisible" 
      @close="cartPopupVisible = false"
    />

    <!-- 评论输入组件 -->
    <CommentInput
      v-model:visible="commentInputVisible"
      :reply-to="replyToComment"
      @submit="handleSubmitComment"
      @close="commentInputVisible = false; replyToComment = null"
    />

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="cart-icon-wrapper" @tap="toggleCartPopup">
        <image src="/static/tab-cart.png" mode="aspectFit" class="cart-icon" />
        <view class="badge" v-if="cartStore.totalCount > 0">
          <text>{{ cartStore.totalCount }}</text>
        </view>
      </view>

      <view class="btn-add-cart" @tap="addToCart">
        <text>加入购物车</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDishDetail } from '@/api/index'
import { getCommentList, addComment } from '@/api/comment'
import { useTheme } from '@/stores/theme'
import { useCartStore } from '@/stores/cart'
import CartPopup from '@/components/CartPopup.vue'
import CommentList from '@/components/CommentList.vue'
import CommentInput from '@/components/CommentInput.vue'

const { themeConfig, loadTheme } = useTheme()
const cartStore = useCartStore()
const cartPopupVisible = ref(false)

const dish = ref({
  id: 0,
  name: '加载中...',
  description: '',
  price: 0,
  image: 'https://dummyimage.com/800x600/6366f1/ffffff&text=Loading',
  categoryName: '家庭菜谱'
})

// 评论相关
const comments = ref([])
const commentInputVisible = ref(false)
const replyToComment = ref(null)

// 计算评论总数（包括回复）
const totalCommentCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach(comment => {
    if (comment.replies) {
      count += comment.replies.length
    }
  })
  return count
})

const loadDishDetail = async (id) => {
  try {
    const res = await getDishDetail(id)
    if (res.data) {
      dish.value = res.data
    }
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    // 显示默认数据
    dish.value = {
      id: id,
      name: '宫保鸡丁',
      description: '经典川菜，选用优质鸡肉，配以花生米、干辣椒等食材，口感香辣酥脆，色泽红亮，是一道深受欢迎的传统名菜。',
      price: 38,
      image: 'https://dummyimage.com/800x600/ff6b6b/ffffff&text=宫保鸡丁',
      categoryName: '川菜经典'
    }
  }
}

// 加载评论列表
const loadComments = async () => {
  if (!dish.value.id) return
  
  try {
    const res = await getCommentList(dish.value.id)
    if (res.data) {
      comments.value = res.data
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 显示评论输入框
const showCommentInput = () => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  replyToComment.value = null
  commentInputVisible.value = true
}

// 处理回复 - 支持回复主评论和子评论
const handleReply = (targetComment, parentComment = null) => {
  const token = uni.getStorageSync('fm_token')
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  // 如果是回复子评论，parentComment会有值
  // 如果是回复主评论，parentComment为null
  replyToComment.value = {
    ...targetComment,
    // 保存被回复人的名字用于显示
    replyToName: targetComment.authorName,
    // 如果是回复子评论，parentId应该是主评论的ID
    actualParentId: parentComment ? parentComment.id : targetComment.id
  }
  commentInputVisible.value = true
}

// 提交评论
const handleSubmitComment = async (data) => {
  try {
    const commentData = {
      dishId: dish.value.id,
      content: data.content,
      parentId: data.parentId,
      replyToName: data.replyToName
    }
    
    await addComment(commentData)
    
    uni.showToast({
      title: '评论发表成功',
      icon: 'success'
    })
    
    // 关闭输入框
    commentInputVisible.value = false
    replyToComment.value = null
    
    // 重新加载评论
    await loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
    uni.showToast({
      title: '发表失败，请重试',
      icon: 'none'
    })
  }
}

const addToCart = () => {
  cartStore.addToCart(dish.value)
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}

const toggleCartPopup = () => {
  if (cartStore.totalCount > 0) {
    cartPopupVisible.value = !cartPopupVisible.value
  } else {
    uni.showToast({
      title: '购物车是空的，快去选购吧~',
      icon: 'none',
      duration: 2000
    })
  }
}

onLoad((options) => {
  if (options.id) {
    loadDishDetail(options.id)
    // 延迟加载评论，确保dish.value.id已设置
    setTimeout(() => {
      loadComments()
    }, 100)
  }
})

onMounted(() => {
  loadTheme()
})
</script>

<style lang="scss" scoped>
.page {
  height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
}

.detail-scroll {
  flex: 1;
  padding: 0 24rpx;
}

/* 通用卡片样式 */
.card {
  background: v-bind('themeConfig.cardBg');
  border-radius: 24rpx;
  border: 1px solid v-bind('themeConfig.cardBorder');
  box-shadow: v-bind('themeConfig.shadowLight');
  margin-bottom: 24rpx;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 1. 图片卡片 */
.image-card {
  height: 460rpx;
  padding: 12rpx; /* 内边距效果 */
  
  .dish-image {
    width: 100%;
    height: 100%;
    border-radius: 16rpx;
    background-color: v-bind('themeConfig.bgSecondary');
  }
}

/* 2. 信息卡片 */
.info-card {
  padding: 32rpx;
}

.info-header {
  margin-bottom: 24rpx;
}

.dish-name {
  font-size: 44rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  margin-bottom: 16rpx;
  display: block;
  line-height: 1.2;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.price {
  font-size: 40rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary'); /* 使用主色而非红色，更显高级 */
}

.category-tag {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
}

.dish-desc {
  font-size: 28rpx;
  color: v-bind('themeConfig.textSecondary');
  line-height: 1.6;
  display: block;
}

/* 3. 评论卡片 */
.comment-card {
  padding: 32rpx;
  min-height: 200rpx;
}

.comment-header {
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-title {
  font-size: 30rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
}

.add-comment-btn {
  padding: 12rpx 24rpx;
  background: v-bind('themeConfig.primaryGradient');
  border-radius: 32rpx;
  box-shadow: v-bind('themeConfig.shadowLight');
  transition: all 0.3s ease;
  
  text {
    font-size: 24rpx;
    color: #fff;
    font-weight: 600;
  }
  
  &:active {
    transform: scale(0.95);
    opacity: 0.9;
  }
}

.bottom-spacer {
  height: 180rpx;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(20px);
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1px solid v-bind('themeConfig.borderColor');
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  z-index: 100;
}

.cart-icon-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: v-bind('themeConfig.inputBg');
  border-radius: 50%;
  border: 1px solid v-bind('themeConfig.borderColor');
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
    background: v-bind('themeConfig.bgSecondary');
  }
  
  .cart-icon {
    width: 50rpx;
    height: 50rpx;
  }
  
  .badge {
    position: absolute;
    top: 0;
    right: 0;
    background: #ef4444;
    color: #fff;
    font-size: 20rpx;
    padding: 4rpx 10rpx;
    border-radius: 20rpx;
    border: 2rpx solid v-bind('themeConfig.cardBg');
    min-width: 32rpx;
    text-align: center;
  }
}

.btn-add-cart {
  flex: 1;
  background: v-bind('themeConfig.primaryGradient');
  color: #fff;
  padding: 24rpx 40rpx;
  border-radius: 999rpx;
  font-size: 30rpx;
  font-weight: 600;
  text-align: center;
  box-shadow: v-bind('themeConfig.shadowMedium');
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}
</style>


