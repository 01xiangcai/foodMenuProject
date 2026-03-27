<template>
  <div class="migration-container">
    <!-- 顶部标题区 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold gradient-text">图片迁移服务</h1>
      <p class="text-gray-500 mt-2">将阿里云OSS上的图片迁移到本地存储，支持双重备份与快速访问</p>
    </div>

    <n-grid :x-gap="24" :y-gap="24" cols="1 m:2 l:3" responsive="screen">
      <!-- 菜品迁移卡片 -->
      <n-grid-item>
        <div class="glass-card hover-rise p-6 h-full flex flex-col">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="p-3 rounded-xl bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
                <i class="i-tabler-tools-kitchen-2 text-2xl" />
              </div>
              <div>
                <h3 class="text-lg font-bold">菜品与轮播图</h3>
                <p class="text-xs text-gray-400">Dishes & Banners</p>
              </div>
            </div>
            <n-tag :type="getStatusType(dishStatus)" round size="small">
              {{ getStatusText(dishStatus) }}
            </n-tag>
          </div>

          <div class="flex-1">
            <div class="mb-6">
              <div class="flex justify-between text-sm mb-2">
                <span class="text-gray-500">迁移进度</span>
                <span class="font-mono font-bold">{{ dishStatus.progress }}%</span>
              </div>
              <n-progress
                type="line"
                :percentage="dishStatus.progress"
                :status="getProgressStatus(dishStatus)"
                :height="12"
                border-radius="6"
                processing
              />
            </div>

            <n-grid :cols="2" :x-gap="12" :y-gap="12" class="mb-6">
              <div class="stat-item bg-gray-50 dark:bg-gray-800/50 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">总数量</div>
                <div class="text-lg font-bold">{{ dishStatus.total }}</div>
              </div>
              <div class="stat-item bg-green-50 dark:bg-green-900/20 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">已完成</div>
                <div class="text-lg font-bold text-green-600">{{ dishStatus.migrated }}</div>
              </div>
              <div class="stat-item bg-red-50 dark:bg-red-900/20 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">失败</div>
                <div class="text-lg font-bold text-red-600">{{ dishStatus.failed }}</div>
              </div>
              <div class="stat-item bg-gray-50 dark:bg-gray-800/50 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">跳过</div>
                <div class="text-lg font-bold text-gray-600">{{ dishStatus.skipped }}</div>
              </div>
            </n-grid>

            <div v-if="dishStatus.message" class="bg-gray-50 dark:bg-gray-800/50 p-3 rounded-lg text-xs text-gray-500 mb-4 font-mono break-all">
              > {{ dishStatus.message }}
            </div>
          </div>

          <div class="mt-auto pt-4 border-t border-gray-100 dark:border-gray-700/50 flex gap-3">
            <n-button
              type="primary"
              class="flex-1"
              :loading="dishMigrating"
              :disabled="dishMigrating || avatarMigrating"
              @click="startDishMigration"
            >
              {{ dishMigrating ? '迁移中...' : '开始迁移' }}
            </n-button>
          </div>
        </div>
      </n-grid-item>

      <!-- 头像迁移卡片 -->
      <n-grid-item>
        <div class="glass-card hover-rise p-6 h-full flex flex-col">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="p-3 rounded-xl bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">
                <i class="i-tabler-user-circle text-2xl" />
              </div>
              <div>
                <h3 class="text-lg font-bold">用户头像</h3>
                <p class="text-xs text-gray-400">User Avatars</p>
              </div>
            </div>
            <n-tag :type="getStatusType(avatarStatus)" round size="small">
              {{ getStatusText(avatarStatus) }}
            </n-tag>
          </div>

          <div class="flex-1">
            <div class="mb-6">
              <div class="flex justify-between text-sm mb-2">
                <span class="text-gray-500">迁移进度</span>
                <span class="font-mono font-bold">{{ avatarStatus.progress }}%</span>
              </div>
              <n-progress
                type="line"
                :percentage="avatarStatus.progress"
                :status="getProgressStatus(avatarStatus)"
                :height="12"
                border-radius="6"
                processing
                color="#7c3aed"
              />
            </div>

            <n-grid :cols="2" :x-gap="12" :y-gap="12" class="mb-6">
              <div class="stat-item bg-gray-50 dark:bg-gray-800/50 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">总数量</div>
                <div class="text-lg font-bold">{{ avatarStatus.total }}</div>
              </div>
              <div class="stat-item bg-green-50 dark:bg-green-900/20 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">已完成</div>
                <div class="text-lg font-bold text-green-600">{{ avatarStatus.migrated }}</div>
              </div>
              <div class="stat-item bg-red-50 dark:bg-red-900/20 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">失败</div>
                <div class="text-lg font-bold text-red-600">{{ avatarStatus.failed }}</div>
              </div>
              <div class="stat-item bg-gray-50 dark:bg-gray-800/50 p-3 rounded-lg">
                <div class="text-xs text-gray-400 mb-1">跳过</div>
                <div class="text-lg font-bold text-gray-600">{{ avatarStatus.skipped }}</div>
              </div>
            </n-grid>

            <div v-if="avatarStatus.message" class="bg-gray-50 dark:bg-gray-800/50 p-3 rounded-lg text-xs text-gray-500 mb-4 font-mono break-all">
              > {{ avatarStatus.message }}
            </div>
          </div>

          <div class="mt-auto pt-4 border-t border-gray-100 dark:border-gray-700/50 flex gap-3">
            <n-button
              color="#7c3aed"
              class="flex-1"
              :loading="avatarMigrating"
              :disabled="dishMigrating || avatarMigrating"
              @click="startAvatarMigration"
            >
              {{ avatarMigrating ? '迁移中...' : '开始迁移' }}
            </n-button>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 底部操作栏 -->
    <div class="mt-6 flex justify-center gap-4">
      <n-button quaternary circle size="large" @click="refreshStatus" title="刷新状态">
        <template #icon>
          <i class="i-tabler-refresh text-xl" :class="{ 'animate-spin': isRefreshing }" />
        </template>
      </n-button>
      
      <n-button 
        v-if="isAnyMigrating" 
        type="error" 
        secondary 
        round 
        size="large" 
        @click="stopMigration"
        class="shadow-lg shadow-red-500/20"
      >
        <template #icon>
          <i class="i-tabler-player-stop" />
        </template>
        停止所有任务
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import http from '@/api/http'

interface MigrationStatus {
  type: string
  total: number
  migrated: number
  failed: number
  skipped: number
  running: boolean
  progress: number
  message: string
  startTime?: string
  endTime?: string
}

const message = useMessage()
const dialog = useDialog()

const dishMigrating = ref(false)
const avatarMigrating = ref(false)
const thumbnailProcessing = ref(false)
const isRefreshing = ref(false)

const dishStatus = ref<MigrationStatus>({
  type: 'DISH',
  total: 0,
  migrated: 0,
  failed: 0,
  skipped: 0,
  running: false,
  progress: 0,
  message: '等待开始...'
})

const avatarStatus = ref<MigrationStatus>({
  type: 'AVATAR',
  total: 0,
  migrated: 0,
  failed: 0,
  skipped: 0,
  running: false,
  progress: 0,
  message: '等待开始...'
})

const isAnyMigrating = computed(() => dishMigrating.value || avatarMigrating.value || thumbnailProcessing.value)

let statusInterval: number | null = null

// 状态辅助函数
const getStatusType = (status: MigrationStatus) => {
  if (status.running) return 'warning'
  if (status.progress === 100) return 'success'
  if (status.failed > 0) return 'error'
  return 'default'
}

const getStatusText = (status: MigrationStatus) => {
  if (status.running) return '运行中'
  if (status.progress === 100) return '已完成'
  if (status.total === 0) return '未开始'
  return '就绪'
}

const getProgressStatus = (status: MigrationStatus) => {
  if (status.running) return 'default'
  if (status.failed > 0) return 'error'
  if (status.progress === 100) return 'success'
  return 'default'
}

// 开始菜品迁移
const startDishMigration = () => {
  dialog.warning({
    title: '确认迁移',
    content: '确定要开始迁移菜品和轮播图吗? 这将下载OSS图片到本地服务器。',
    positiveText: '立即开始',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        dishMigrating.value = true
        const res = await http.post('/admin/migration/dishes')
        message.success(res.data || '迁移任务已启动')
        startStatusPolling()
      } catch (error: any) {
        message.error(error.message || '启动迁移失败')
        dishMigrating.value = false
      }
    }
  })
}

// 开始头像迁移
const startAvatarMigration = () => {
  dialog.warning({
    title: '确认迁移',
    content: '确定要开始迁移用户头像吗? 这将下载OSS图片到本地服务器。',
    positiveText: '立即开始',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        avatarMigrating.value = true
        const res = await http.post('/admin/migration/avatars')
        message.success(res.data || '迁移任务已启动')
        startStatusPolling()
      } catch (error: any) {
        message.error(error.message || '启动迁移失败')
        avatarMigrating.value = false
      }
    }
  })
}

// 批量生成缩略图
const startThumbnailBatch = () => {
  dialog.info({
    title: '确认生成',
    content: '确定要开始批量生成缩略图吗？该操作将扫描本地存储并补齐缺失的缩略图文件。',
    positiveText: '立即生成',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        thumbnailProcessing.value = true
        const res = await http.post('/admin/migration/thumbnails/batch')
        message.success(res.data || '处理完成')
        // 缩略图生成是同步执行的，完成后刷新状态即可
      } catch (error: any) {
        message.error(error.message || '生成失败')
      } finally {
        thumbnailProcessing.value = false
      }
    }
  })
}

// 刷新状态
const refreshStatus = async () => {
  isRefreshing.value = true
  try {
    await Promise.all([fetchDishStatus(), fetchAvatarStatus()])
    message.success('状态已更新')
  } finally {
    setTimeout(() => {
      isRefreshing.value = false
    }, 500)
  }
}

// 获取菜品迁移状态
const fetchDishStatus = async () => {
  try {
    const res = await http.get('/admin/migration/status/dishes')
    if (res.data) {
      dishStatus.value = res.data
      dishMigrating.value = res.data.running
    }
  } catch (error: any) {
    console.error('获取菜品迁移状态失败:', error)
  }
}

// 获取头像迁移状态
const fetchAvatarStatus = async () => {
  try {
    const res = await http.get('/admin/migration/status/avatars')
    if (res.data) {
      avatarStatus.value = res.data
      avatarMigrating.value = res.data.running
    }
  } catch (error: any) {
    console.error('获取头像迁移状态失败:', error)
  }
}

// 停止迁移
const stopMigration = () => {
  dialog.error({
    title: '停止任务',
    content: '确定要强制停止当前正在进行的迁移任务吗?',
    positiveText: '停止',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await http.post('/admin/migration/stop')
        message.success('迁移任务已停止')
        dishMigrating.value = false
        avatarMigrating.value = false
        stopStatusPolling()
        // 延迟刷新一次状态以获取最终结果
        setTimeout(refreshStatus, 1000)
      } catch (error: any) {
        message.error(error.message || '停止迁移失败')
      }
    }
  })
}

// 开始轮询状态
const startStatusPolling = () => {
  if (statusInterval) return
  
  // 立即执行一次
  refreshStatus()
  
  statusInterval = window.setInterval(async () => {
    await Promise.all([fetchDishStatus(), fetchAvatarStatus()])
    
    // 如果都不在运行,停止轮询
    if (!dishStatus.value.running && !avatarStatus.value.running) {
      stopStatusPolling()
      dishMigrating.value = false
      avatarMigrating.value = false
    }
  }, 2000)
}

// 停止轮询状态
const stopStatusPolling = () => {
  if (statusInterval) {
    clearInterval(statusInterval)
    statusInterval = null
  }
}

onMounted(() => {
  refreshStatus()
})

onUnmounted(() => {
  stopStatusPolling()
})
</script>

<style scoped>
.migration-container {
  max-width: 1200px;
  margin: 0 auto;
}

.stat-item {
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
}
</style>
