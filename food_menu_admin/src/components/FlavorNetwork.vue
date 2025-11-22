<template>
  <div class="network">
    <div v-for="node in nodes" :key="node.label" class="node" :style="node.style">
      {{ node.label }}
    </div>
  </div>
</template>

<script setup lang="ts">
const nodes = [
  { label: '花椒', style: { top: '10%', left: '40%' } },
  { label: '青柠', style: { top: '30%', left: '70%' } },
  { label: '椰香', style: { top: '60%', left: '20%' } },
  { label: '味噌', style: { top: '65%', left: '60%' } },
  { label: '松露', style: { top: '35%', left: '15%' } }
];
</script>

<style scoped>
.network {
  position: relative;
  height: 220px;
  border-radius: 20px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.node {
  position: absolute;
  padding: 10px 18px;
  border-radius: 999px;
  background: rgba(20, 184, 255, 0.18);
  color: #fff;
  font-size: 14px;
  box-shadow: 0 10px 30px rgba(20, 184, 255, 0.15);
  animation: float 6s ease-in-out infinite;
}

.node:nth-child(2n) {
  background: rgba(124, 58, 237, 0.18);
  animation-delay: 2s;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-12px);
  }
  100% {
    transform: translateY(0px);
  }
}
</style>

