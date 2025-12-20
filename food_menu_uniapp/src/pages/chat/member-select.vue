<template>
    <view class="container">
        <!-- 简单的搜索栏 -->
        <view class="search-box">
            <text class="search-icon">🔍</text>
            <text class="search-text">搜索</text>
        </view>

        <!-- 列表 -->
        <view class="user-list">
            <view class="user-item" v-for="(user, index) in members" :key="user.id || index" @click="selectUser(user)">
                <image class="avatar" :src="user.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
                <text class="name">{{ user.nickname || 'Unknown' }}</text>
            </view>
        </view>

        <!-- Loading/Empty -->
        <view class="status-box" v-if="loading">
            <text>加载中...</text>
        </view>
        <view class="status-box" v-else-if="members.length === 0">
            <text>暂无联系人</text>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFamilyMembers } from '../../api/chat'
import { useChatStore } from '../../stores/chat'

const members = ref([])
const loading = ref(true)
const chatStore = useChatStore()

// 获取成员
const loadData = async () => {
    console.log('加载联系人列表...')
    loading.value = true
    try {
        const res = await getFamilyMembers()
        console.log('成员API返回:', res)
        if (res && res.code === 1) {
            const myId = uni.getStorageSync('userInfo')?.id
            members.value = (res.data || []).filter(u => u.id !== myId)
        }
    } catch (e) {
        console.error('API Error', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
    } finally {
        loading.value = false
    }
}

// 选择用户
const selectUser = async (user) => {
    console.log('选择用户:', user)
    uni.showLoading({ title: '启动会话' })
    try {
        const conv = await chatStore.openPrivateChat(user.id, user.nickname)
        if (conv) {
             uni.navigateTo({
                url: `/pages/chat/room?id=${conv.id}&name=${encodeURIComponent(user.nickname || '')}&type=1`
            })
        } else {
            uni.showToast({ title: '无法创建会话', icon: 'none' })
        }
    } catch (e) {
        console.error('Select User Error', e)
        uni.showToast({ title: '启动失败', icon: 'none' })
    } finally {
        uni.hideLoading()
    }
}

onMounted(() => {
    loadData()
})
</script>

<style>
/* 移除 scoped 和 scss，使用最基本CSS防止编译问题 */
.container {
    background-color: #f5f5f5;
    min-height: 100vh;
}
.search-box {
    background-color: #fff;
    padding: 20rpx;
    display: flex;
    align-items: center;
    border-bottom: 1px solid #eee;
}
.search-icon {
    font-size: 30rpx;
    margin-right: 10rpx;
}
.search-text {
    color: #999;
    font-size: 28rpx;
}
.user-list {
    background-color: #fff;
    margin-top: 20rpx;
}
.user-item {
    display: flex;
    align-items: center;
    padding: 20rpx 30rpx;
    border-bottom: 1px solid #f0f0f0;
}
.user-item:active {
    background-color: #f9f9f9;
}
.avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 10rpx;
    margin-right: 20rpx;
    background-color: #ddd;
}
.name {
    font-size: 32rpx;
    color: #333;
}
.status-box {
    padding: 50rpx;
    text-align: center;
    color: #999;
    font-size: 28rpx;
}
</style>
