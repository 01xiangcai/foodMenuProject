<template>
  <div class="timeline">
    <div v-for="pulse in pulses" :key="pulse.id" class="pulse">
      <div class="pulse-line" />
      <div>
        <p class="pulse-title">{{ pulse.title }}</p>
        <p class="pulse-desc">{{ pulse.desc }}</p>
      </div>
      <span>{{ pulse.time }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const pulses = [
  { id: 1, title: 'AI 推荐刷新', desc: '菜品曝光 +23%', time: '09:24' },
  { id: 2, title: '供应链补货', desc: '新鲜食材抵达', time: '10:02' },
  { id: 3, title: '高峰预警', desc: '预计订单 180+', time: '11:10' }
];
</script>

<style scoped>
.timeline {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.pulse {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 14px;
  align-items: center;
  padding: 12px 0;
}

.pulse-line {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(120deg, #14b8ff, #7c3aed);
  box-shadow: 0 0 12px rgba(20, 184, 255, 0.6);
}

.pulse-title {
  font-weight: 600;
}

.pulse-desc {
  font-size: 13px;
  opacity: 0.7;
}
</style>

