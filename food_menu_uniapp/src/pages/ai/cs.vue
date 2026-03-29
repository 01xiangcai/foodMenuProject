<template>
  <web-view v-if="csUrl" :src="csUrl"></web-view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getAiCustomerServiceUrl } from '@/api/index';

const csUrl = ref('');

onLoad(async () => {
  try {
    uni.showLoading({ title: '加载中...' });
    const res = await getAiCustomerServiceUrl();
    if (res.code === 1 && res.data) {
      csUrl.value = res.data;
      console.log('AI客服动态URL加载成功:', csUrl.value);
    } else {
      uni.showToast({
        title: res.msg || '获取配置失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('获取AI客服URL失败:', error);
    uni.showToast({
      title: '网络错误，请稍后再试',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
});
</script>