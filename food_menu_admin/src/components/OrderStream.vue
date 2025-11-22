<template>
  <div class="stream">
    <div v-for="order in orders" :key="order.id" class="stream-item">
      <div>
        <p class="id">#{{ order.id }}</p>
        <p class="meta">{{ order.category }} · {{ order.city }}</p>
      </div>
      <div class="status">{{ order.status }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const orders = computed(() => [
  { id: 'FM-1024', category: '川菜', city: '北京朝阳', status: '制作中' },
  { id: 'FM-1025', category: 'Fusion', city: '上海徐汇', status: '待出餐' },
  { id: 'FM-1026', category: '粤菜', city: '深圳南山', status: '派送中' },
  { id: 'FM-1027', category: '湘菜', city: '杭州滨江', status: '完成' }
]);
</script>

<style scoped>
.stream {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.stream-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.id {
  font-weight: 600;
}

.meta {
  font-size: 12px;
  opacity: 0.6;
}

.status {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(20, 184, 255, 0.15);
  color: #14b8ff;
  font-size: 12px;
}
</style>

