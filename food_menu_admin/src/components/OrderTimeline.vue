<template>
  <div class="timeline">
    <div v-for="item in items" :key="item.id" class="timeline-item">
      <div class="line" />
      <div>
        <p class="title">{{ item.title }}</p>
        <p class="desc">{{ item.desc }}</p>
      </div>
      <span>{{ item.time }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const items = [
  { id: 1, title: '#FM-1031', desc: 'AI 套餐 · 上海', time: '10:24' },
  { id: 2, title: '#FM-1032', desc: '川湘双拼 · 成都', time: '10:37' },
  { id: 3, title: '#FM-1033', desc: '植物能量碗 · 北京', time: '10:45' }
];
</script>

<style scoped>
.timeline {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.timeline-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
}

.line {
  width: 2px;
  height: 40px;
  background: linear-gradient(180deg, #14b8ff, #7c3aed);
  border-radius: 999px;
}

.title {
  font-weight: 600;
}

.desc {
  font-size: 13px;
  opacity: 0.7;
}
</style>

