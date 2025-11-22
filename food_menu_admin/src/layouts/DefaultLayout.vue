<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <span class="glow-dot" />
        <div>
          <p class="logo-title">家宴菜单</p>
          <p class="logo-sub">一起准备下一顿饭</p>
        </div>
      </div>
      <nav>
        <RouterLink v-for="link in links" :key="link.path" :to="link.path" class="nav-item">
          <i :class="link.icon" />
          <span>{{ link.label }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-footer">
        <p>家庭联机</p>
        <div class="pulse-dot" />
      </div>
    </aside>
    <main class="content">
      <header>
        <div>
          <h1>欢迎回来，{{ welcomeName }}</h1>
          <p>现在是 {{ now }} · 一起决定餐桌上的味道</p>
        </div>
        <div class="header-actions">
          <RouterLink class="ghost-button" to="/dishes">管理菜品</RouterLink>
          <button class="primary-button" @click="syncFamily">
            同步家庭信息
          </button>
        </div>
      </header>
      <section class="view-container">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { useMessage } from 'naive-ui';
import { useUserStore } from '@/store/useUserStore';

const links = [
  { path: '/', label: '家庭看板', icon: 'i-tabler-dashboard' },
  { path: '/dishes', label: '家庭菜单', icon: 'i-tabler-notebook' },
  { path: '/orders', label: '订单记录', icon: 'i-tabler-activity-heartbeat' },
  { path: '/banners', label: '轮播图管理', icon: 'i-tabler-photo' }
];

const now = ref('');
const updateClock = () => {
  now.value = new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(new Date());
};
updateClock();
let clockTimer: number | null = null;

const userStore = useUserStore();
const { profile } = storeToRefs(userStore);
const message = useMessage();

const welcomeName = computed(() => profile.value?.name || profile.value?.username || '家人');

const syncFamily = async () => {
  try {
    await userStore.loadProfile();
    message.success('家庭信息已更新');
  } catch (error) {
    message.error((error as Error).message);
  }
};

onMounted(() => {
  clockTimer = window.setInterval(updateClock, 1000);
  if (userStore.token && !profile.value) {
    userStore.loadProfile().catch(() => undefined);
  }
});

onBeforeUnmount(() => {
  if (clockTimer) {
    clearInterval(clockTimer);
    clockTimer = null;
  }
});
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
  backdrop-filter: blur(20px);
}

.sidebar {
  background: linear-gradient(180deg, rgba(10, 17, 40, 0.9), rgba(6, 8, 20, 0.95));
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
}

.logo-sub {
  font-size: 12px;
  opacity: 0.8;
}

.glow-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: radial-gradient(circle, #4cfff7 0%, transparent 70%);
  box-shadow: 0 0 15px #4cfff7;
}

nav {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  background: transparent;
  color: #cbd5f5;
  text-decoration: none;
  transition: 0.3s;
  font-weight: 500;
}

.nav-item.router-link-active {
  background: linear-gradient(90deg, rgba(20, 184, 255, 0.25), rgba(124, 58, 237, 0.15));
  color: #fff;
  box-shadow: 0 10px 40px rgba(20, 184, 255, 0.1);
}

.nav-item:hover {
  transform: translateX(6px);
  background: rgba(255, 255, 255, 0.04);
}

.sidebar-footer {
  margin-top: auto;
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.pulse-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #00ffb3;
  box-shadow: 0 0 16px #00ffb3;
  animation: pulse 1.8s ease-in-out infinite;
}

.content {
  padding: 32px;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.ghost-button,
.primary-button {
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #fff;
  cursor: pointer;
  transition: 0.3s;
}

.primary-button {
  background: linear-gradient(120deg, #3b82f6, #a855f7, #14b8ff);
  border: none;
  box-shadow: 0 10px 40px rgba(59, 130, 246, 0.35);
}

.view-container {
  min-height: calc(100vh - 140px);
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.4);
    opacity: 0.4;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>

