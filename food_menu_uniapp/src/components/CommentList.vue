<template>
  <view class="comment-list">
    <!-- 空状态 -->
    <view v-if="comments.length === 0" class="empty-state">
      <text>{{ emptyText }}</text>
    </view>

    <!-- 评论列表 -->
    <view v-else class="comments">
      <view v-for="comment in comments" :key="comment.id" class="comment-item">
        <!-- 评论主体 -->
        <view class="comment-main">
          <!-- 头像 -->
          <image 
            class="avatar" 
            :src="comment.avatarUrl || defaultAvatar" 
            mode="aspectFill"
            @tap.stop="goToProfile(comment.wxUserId)"
          />
          
          <!-- 评论内容 -->
          <view class="comment-content">
            <!-- 用户名和时间 -->
            <view class="user-info">
              <text class="author-name">{{ comment.authorName || '家庭成员' }}</text>
              <text class="comment-time">{{ formatTime(comment.createTime) }}</text>
            </view>
            
            <!-- 评论文本 -->
            <text class="comment-text">{{ comment.content }}</text>
            
            <!-- 回复按钮 -->
            <view class="action-btn" @tap="handleReply(comment)">
              <text>回复</text>
            </view>
          </view>
        </view>

        <!-- 回复列表 -->
        <view v-if="comment.replies && comment.replies.length > 0" class="replies-container">
          <view class="replies">
            <!-- 显示回复 -->
            <view 
              v-for="reply in getVisibleReplies(comment)" 
              :key="reply.id" 
              class="reply-item"
            >
              <!-- 子评论头像 -->
              <image 
                class="reply-avatar" 
                :src="reply.avatarUrl || defaultAvatar" 
                mode="aspectFill"
                @tap.stop="goToProfile(reply.wxUserId)"
              />

              <view class="reply-content">
                <!-- 回复者和被回复者 -->
                <text class="reply-text">
                  <text class="reply-author">{{ reply.authorName || '家庭成员' }}</text>
                  <text class="reply-to">
                    ▶    {{ getReplyToName(comment, reply) }}
                  </text>
                  <text class="reply-message"> ：{{ reply.content }}</text>
                </text>
                
                <!-- 时间和回复按钮 -->
                <view class="reply-footer">
                  <text class="reply-time">{{ formatTime(reply.createTime) }}</text>
                  <text class="reply-btn" @tap="handleReply(reply, comment)">回复</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 展开/收起按钮 -->
          <view 
            v-if="comment.replies.length > 3" 
            class="toggle-btn"
            @tap="toggleReplies(comment.id)"
          >
            <text v-if="expandedComments.includes(comment.id)">
              收起 ^
            </text>
            <text v-else>
              展开更多 {{ comment.replies.length - 3 }} 条回复 ▽
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useTheme } from '@/stores/theme'

const { themeConfig } = useTheme()

const props = defineProps({
  comments: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '第一条味觉灵感等你来写~'
  }
})

const emit = defineEmits(['reply'])

// 默认头像
const defaultAvatar = 'https://dummyimage.com/80x80/6366f1/ffffff&text=U'

// 展开的评论ID列表
const expandedComments = ref([])

// 切换回复展开/收起
const toggleReplies = (commentId) => {
  const index = expandedComments.value.indexOf(commentId)
  if (index > -1) {
    expandedComments.value.splice(index, 1)
  } else {
    expandedComments.value.push(commentId)
  }
}

// 获取可见的回复（展开时显示全部，否则只显示前3条）
const getVisibleReplies = (comment) => {
  if (!comment.replies) return []
  const isExpanded = expandedComments.value.includes(comment.id)
  return isExpanded ? comment.replies : comment.replies.slice(0, 3)
}

// 获取被回复人的名字
const getReplyToName = (parentComment, reply) => {
  // 优先使用后端返回的replyToName
  if (reply.replyToName) {
    return reply.replyToName
  }

  // 兼容旧数据逻辑
  // 先在子评论中查找被回复的评论
  const replyTo = parentComment.replies?.find(r => r.id === reply.parentId)
  
  if (replyTo) {
    // 找到了被回复的子评论，返回其作者名
    return replyTo.authorName || '家庭成员'
  }
  
  // 如果在子评论中没找到，说明是回复主评论
  if (reply.parentId === parentComment.id) {
    return parentComment.authorName || '家庭成员'
  }
  
  // 兜底：返回主评论作者
  return parentComment.authorName || '家庭成员'
}

// 处理回复 - 支持回复主评论和子评论
const handleReply = (targetComment, parentComment = null) => {
  // 如果是回复子评论，传递父评论信息
  emit('reply', targetComment, parentComment)
}

// 跳转到个人主页
const goToProfile = (userId) => {
  if (!userId) {
    console.warn('Cannot navigate to profile: userId is missing')
    return
  }
  
  uni.navigateTo({
    url: `/pages/profile/personal-info?id=${userId}`,
    fail: (err) => {
      console.error('Navigate to profile failed:', err)
      uni.showToast({
        title: '无法查看用户主页',
        icon: 'none'
      })
    }
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    const year = time.getFullYear()
    const month = String(time.getMonth() + 1).padStart(2, '0')
    const date = String(time.getDate()).padStart(2, '0')
    return `${year}-${month}-${date}`
  }
}
</script>

<style lang="scss" scoped>
.comment-list {
  width: 100%;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60rpx 0;
  
  text {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
  }
}

.comments {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.comment-item {
  display: flex;
  flex-direction: column;
}

.comment-main {
  display: flex;
  gap: 20rpx;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background-color: v-bind('themeConfig.bgSecondary');
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-info {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.author-name {
  font-size: 28rpx;
  font-weight: 500;
  color: v-bind('themeConfig.textPrimary');
}

.comment-time {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
}

.comment-text {
  font-size: 30rpx;
  color: v-bind('themeConfig.textPrimary');
  line-height: 1.5;
  word-break: break-all;
  margin-top: 4rpx;
}

.action-btn {
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
  padding: 8rpx 0;
  display: inline-block;
  width: fit-content;
  
  &:active {
    opacity: 0.6;
  }
}

/* 回复区域容器 */
.replies-container {
  margin-left: 92rpx;
  margin-top: 16rpx;
}

.replies {
  background: v-bind('themeConfig.bgSecondary');
  border-radius: 12rpx;
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.reply-item {
  display: flex;
  flex-direction: row; /* 改为水平排列以支持左侧头像 */
  gap: 16rpx;
}

.reply-avatar {
  width: 48rpx; /* 24px */
  height: 48rpx; /* 24px */
  border-radius: 50%;
  background-color: v-bind('themeConfig.bgPrimary');
  flex-shrink: 0;
  margin-top: 4rpx; /* 微调对齐 */
  margin-right: 16rpx;
}

.reply-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.reply-text {
  font-size: 28rpx;
  line-height: 1.5;
  word-break: break-all;
}

.reply-author {
  color: v-bind('themeConfig.textPrimary');
  font-weight: 500;
}

.reply-to {
  color: v-bind('themeConfig.primaryColor');
  margin: 0 4rpx;
}

.reply-message {
  color: v-bind('themeConfig.textPrimary');
}

.reply-footer {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 4rpx;
}

.reply-time {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
}

.reply-btn {
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
  
  &:active {
    opacity: 0.6;
  }
}

/* 展开/收起按钮 */
.toggle-btn {
  padding: 12rpx 16rpx;
  text-align: left;
  margin-top: 8rpx;
  
  text {
    font-size: 26rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  &:active {
    opacity: 0.6;
  }
}
</style>
