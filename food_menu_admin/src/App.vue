<script setup lang="ts">
import { computed } from 'vue';
import { NConfigProvider, NMessageProvider, NDialogProvider, darkTheme, lightTheme, GlobalThemeOverrides } from 'naive-ui';
import { useThemeStore } from '@/store/theme';

const themeStore = useThemeStore();

// Compute Naive UI theme based on store
const naiveTheme = computed(() => {
  return themeStore.isDark ? darkTheme : lightTheme;
});

// Custom theme overrides for Naive UI
const themeOverrides = computed<GlobalThemeOverrides>(() => {
  const primary = themeStore.primaryColor;
  const common = {
    primaryColor: `hsl(${primary.h}, ${primary.s}%, ${primary.l}%)`,
    primaryColorHover: `hsl(${primary.h}, ${primary.s}%, ${primary.l - 10}%)`,
    primaryColorPressed: `hsl(${primary.h}, ${primary.s}%, ${primary.l - 20}%)`,
    primaryColorSuppl: `hsl(${primary.h}, ${primary.s}%, ${primary.l + 10}%)`,
    borderRadius: '12px',
    fontFamily: "'Inter', sans-serif"
  };

  const lightOverrides: GlobalThemeOverrides = {
    common,
    Card: {
      color: 'rgba(255, 255, 255, 0.8)',
      borderColor: 'rgba(255, 255, 255, 0.5)',
      borderRadius: '16px'
    },
    Button: {
      borderRadiusMedium: '10px',
      fontWeight: '600'
    },
    Input: {
      borderRadius: '10px',
      color: 'rgba(255, 255, 255, 0.5)',
      colorFocus: 'rgba(255, 255, 255, 0.9)'
    }
  };

  const darkOverrides: GlobalThemeOverrides = {
    common,
    Card: {
      color: 'rgba(30, 41, 59, 0.6)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      borderRadius: '16px'
    },
    Button: {
      borderRadiusMedium: '10px',
      fontWeight: '600'
    },
    Input: {
      borderRadius: '10px',
      color: 'rgba(30, 41, 59, 0.5)',
      colorFocus: 'rgba(30, 41, 59, 0.8)'
    }
  };

  return themeStore.isDark ? darkOverrides : lightOverrides;
});
</script>

<template>
  <n-config-provider :theme="naiveTheme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <RouterView />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<style>
/* Global transitions are handled in animations.css and theme.css */
</style>



