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
  background: rgba(var(--primary-h), var(--primary-s), var(--primary-l), 0.1);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
}

.label {
  opacity: 0.7;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.value {
  font-size: 36px;
  font-weight: 700;
  margin: 8px 0;
  color: var(--text-primary);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.trend {
  color: var(--accent-success);
  letter-spacing: 1px;
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 600;
}
</style>

