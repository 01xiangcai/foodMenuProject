<script setup lang="ts">
import { NConfigProvider, NMessageProvider, darkTheme } from 'naive-ui';
</script>

<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <RouterView />
    </n-message-provider>
  </n-config-provider>
</template>

<style scoped>
:global(body) {
  font-family:
    'Inter',
    'HarmonyOS Sans',
    'Helvetica Neue',
    Arial,
    sans-serif;
  background: radial-gradient(circle at top, #0f172a 0%, #020617 60%, #000 100%);
  color: #f8fafc;
  min-height: 100vh;
}
</style>

