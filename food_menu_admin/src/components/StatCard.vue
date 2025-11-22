<template>
  <div class="stat-card glass-card">
    <div class="chip">{{ chip }}</div>
    <p class="label">{{ label }}</p>
    <p class="value">{{ value }}</p>
    <p class="trend">{{ trend }}</p>
    <div class="orbit-gradient" />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
  value: string;
  trend: string;
  chip: string;
}>();
</script>

<style scoped>
.stat-card {
  position: relative;
  overflow: hidden;
  padding: 24px;
}

.chip {
  display: inline-flex;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  font-size: 12px;
  margin-bottom: 12px;
}

.label {
  opacity: 0.7;
  letter-spacing: 3px;
  text-transform: uppercase;
}

.value {
  font-size: 36px;
  font-weight: 700;
  margin: 8px 0;
}

.trend {
  color: #00ffb3;
  letter-spacing: 2px;
  text-transform: uppercase;
}
</style>

