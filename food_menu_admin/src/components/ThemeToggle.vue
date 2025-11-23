<template>
  <n-tooltip trigger="hover">
    <template #trigger>
      <button 
        class="theme-toggle" 
        :class="{ 'is-light': !themeStore.isDark }"
        @click="themeStore.toggleDark()"
      >
        <div class="icon-wrapper">
          <i class="i-tabler-sun sun-icon" />
          <i class="i-tabler-moon moon-icon" />
        </div>
      </button>
    </template>
    {{ themeStore.isDark ? '切换到浅色模式' : '切换到深色模式' }}
  </n-tooltip>
</template>

<script setup lang="ts">
import { useThemeStore } from '@/store/theme';
import { NTooltip } from 'naive-ui';

const themeStore = useThemeStore();
</script>

<style scoped>
.theme-toggle {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid var(--border-primary);
  background: var(--bg-glass);
  backdrop-filter: blur(10px);
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-toggle:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-glow);
}

.theme-toggle:active {
  transform: scale(0.95);
}

.icon-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sun-icon,
.moon-icon {
  position: absolute;
  font-size: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Dark mode - show moon */
.moon-icon {
  opacity: 1;
  transform: rotate(0deg) scale(1);
  color: var(--accent-primary);
}

.sun-icon {
  opacity: 0;
  transform: rotate(180deg) scale(0);
  color: var(--accent-warning);
}

/* Light mode - show sun */
.theme-toggle.is-light .moon-icon {
  opacity: 0;
  transform: rotate(-180deg) scale(0);
}

.theme-toggle.is-light .sun-icon {
  opacity: 1;
  transform: rotate(0deg) scale(1);
}

/* Glow effect */
.theme-toggle::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity 0.3s;
  z-index: -1;
}

.theme-toggle:hover::before {
  opacity: 0.3;
}
</style>
