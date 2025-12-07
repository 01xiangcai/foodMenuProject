<template>
  <n-layout has-sider class="h-screen bg-bg-body transition-colors duration-300">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="themeStore.sidebarCollapsed"
      @collapse="themeStore.sidebarCollapsed = true"
      @expand="themeStore.sidebarCollapsed = false"
      class="glass-heavy z-50"
      :native-scrollbar="false"
    >
      <div class="flex flex-col h-full">
        <!-- Logo -->
        <div class="h-16 flex items-center justify-center relative overflow-hidden">
          <div class="flex items-center gap-3 transition-all duration-300" :class="{ 'scale-0 opacity-0': themeStore.sidebarCollapsed }">
            <img src="/assets/logo.png" alt="Logo" class="w-10 h-10 rounded-xl shadow-lg object-cover" />
            <span class="font-bold text-xl tracking-wide text-gray-900 dark:text-white">
              家宴菜单
            </span>
          </div>
          <!-- Collapsed Logo -->
          <div class="absolute inset-0 flex items-center justify-center transition-all duration-300" :class="{ 'scale-0 opacity-0': !themeStore.sidebarCollapsed }">
             <img src="/assets/logo.png" alt="Logo" class="w-10 h-10 rounded-xl shadow-lg object-cover" />
          </div>
        </div>

        <!-- Menu -->
        <n-menu
          :collapsed="themeStore.sidebarCollapsed"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          :options="menuOptions"
          :value="activeKey"
          class="flex-1"
        />

        <!-- Footer -->
        <div class="p-4 border-t border-gray-200 dark:border-gray-700/50">
           <div class="flex items-center justify-center gap-2" :class="{ 'flex-col': themeStore.sidebarCollapsed }">

             <button 
               class="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors text-text-secondary"
               @click="themeStore.toggleSidebar"
             >
               <i :class="themeStore.sidebarCollapsed ? 'i-tabler-layout-sidebar-right' : 'i-tabler-layout-sidebar-left'" class="text-xl" />
             </button>
           </div>
        </div>
      </div>
    </n-layout-sider>

    <n-layout class="bg-transparent h-full flex flex-col">
      <n-layout-header class="glass z-40 h-16 px-6 flex items-center justify-between sticky top-0">
        <!-- Left: Breadcrumb or Title -->
        <div class="flex items-center gap-4">
           <div class="text-xl font-bold text-text-primary fade-in-down">
             {{ currentRouteTitle }}
           </div>
        </div>

        <!-- Right: Actions -->
        <div class="flex items-center gap-4">
          <ThemeToggle />
          <div class="hidden md:flex items-center gap-2 text-sm text-text-secondary bg-gray-100 dark:bg-gray-800/50 px-3 py-1 rounded-full">
             <i class="i-tabler-clock" />
             <span>{{ now }}</span>
          </div>
          
          <n-dropdown :options="userOptions" @select="handleUserSelect">
            <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 px-2 py-1 rounded-lg transition-colors">
              <n-avatar
                round
                size="small"
                class="bg-gradient-to-br from-primary to-secondary text-white"
              >
                {{ userAvatar }}
              </n-avatar>
              <span class="text-sm font-medium text-text-primary hidden sm:block">{{ userName }}</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>

      <n-layout-content 
        class="bg-transparent flex-1 p-6 relative overflow-hidden" 
        :native-scrollbar="false"
        content-style="min-height: 100%; display: flex; flex-direction: column;"
      >
         <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, h } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NMenu, NDropdown, NAvatar, useMessage } from 'naive-ui';
import type { MenuOption } from 'naive-ui';
import { useThemeStore } from '@/store/theme';
import { useUserStore } from '@/store/useUserStore';
import ThemeToggle from '@/components/ThemeToggle.vue';
import { storeToRefs } from 'pinia';

const themeStore = useThemeStore();
const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const message = useMessage();
const { profile } = storeToRefs(userStore);

// Menu Options
const renderIcon = (icon: string) => {
  return () => h('i', { class: `${icon} text-lg` });
};

const renderLabel = (label: string, to: string) => {
  return () => h(RouterLink, { to }, { default: () => label });
};

const menuOptions: MenuOption[] = [
  { label: renderLabel('家庭看板', '/'), key: 'dashboard', icon: renderIcon('i-tabler-dashboard') },
  { label: renderLabel('菜品管理', '/dishes'), key: 'dishes', icon: renderIcon('i-tabler-notebook') },
  { label: renderLabel('订单记录', '/orders'), key: 'orders', icon: renderIcon('i-tabler-activity-heartbeat') },
  { label: renderLabel('轮播图管理', '/banners'), key: 'banners', icon: renderIcon('i-tabler-photo') },
  { label: renderLabel('用户管理', '/users'), key: 'users', icon: renderIcon('i-tabler-users') },
  { label: renderLabel('家庭管理', '/families'), key: 'families', icon: renderIcon('i-tabler-home-2') },
  { label: renderLabel('标签管理', '/tags'), key: 'tags', icon: renderIcon('i-tabler-tags') },
  { label: renderLabel('钱包管理', '/wallet'), key: 'wallet', icon: renderIcon('i-tabler-wallet') },
  { label: renderLabel('营销活动', '/marketing'), key: 'marketing', icon: renderIcon('i-tabler-gift') },
  { label: renderLabel('操作日志', '/operationLogs'), key: 'operationLogs', icon: renderIcon('i-tabler-file-text') },
  // { label: renderLabel('图片迁移', '/migration'), key: 'migration', icon: renderIcon('i-tabler-cloud-upload') },
  { label: renderLabel('系统设置', '/settings'), key: 'settings', icon: renderIcon('i-tabler-settings') }
];

const activeKey = computed(() => (route.name as string) || 'dashboard');
const currentRouteTitle = computed(() => (route.meta.title as string) || '未来食堂');

// User Info
const userName = computed(() => profile.value?.name || profile.value?.username || '家人');
const userAvatar = computed(() => userName.value.charAt(0).toUpperCase());

const userOptions = [
  { label: '同步家庭信息', key: 'sync', icon: renderIcon('i-tabler-refresh') },
  { label: '退出登录', key: 'logout', icon: renderIcon('i-tabler-logout') }
];

const handleUserSelect = async (key: string) => {
  if (key === 'logout') {
    userStore.logout();
    router.push('/login');
    message.success('已退出登录');
  } else if (key === 'sync') {
    try {
      await userStore.loadProfile();
      message.success('家庭信息已更新');
    } catch (error) {
      message.error('同步失败');
    }
  }
};

// Clock
const now = ref('');
let clockTimer: number | null = null;
const updateClock = () => {
  now.value = new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    weekday: 'short'
  }).format(new Date());
};

onMounted(() => {
  updateClock();
  clockTimer = window.setInterval(updateClock, 1000);
  if (userStore.token && !profile.value) {
    userStore.loadProfile().catch(() => undefined);
  }
});

onBeforeUnmount(() => {
  if (clockTimer) {
    clearInterval(clockTimer);
  }
});
</script>

<style scoped>
/* Custom override for Naive UI Menu to match glassmorphism */
:deep(.n-menu .n-menu-item-content) {
  border-radius: 12px;
  margin: 4px 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.n-menu .n-menu-item-content:hover) {
  background-color: rgba(var(--primary-h), var(--primary-s), var(--primary-l), 0.1);
}

:deep(.n-menu .n-menu-item-content--selected) {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-color-pressed));
  color: white !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.n-menu .n-menu-item-content--selected .n-menu-item-content-header a) {
  color: white !important;
}

:deep(.n-menu .n-menu-item-content--selected .n-menu-item-content__icon) {
  color: white !important;
}
</style>
