<template>
  <div class="thumbnail-container">
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold gradient-text">缩略图管理</h1>
      <p class="text-gray-500 mt-2">扫描本地存储并批量补齐缺失的缩略图，优化小程序加载体验</p>
    </div>

    <n-grid :x-gap="24" :y-gap="24" cols="1 m:2" responsive="screen">
      <!-- 状态检查卡片 -->
      <n-grid-item>
        <div class="glass-card hover-rise p-6 h-full flex flex-col">
          <div class="flex items-center gap-3 mb-6">
            <div class="p-3 rounded-xl bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400">
              <i class="i-tabler-search text-2xl" />
            </div>
            <div>
              <h3 class="text-lg font-bold">状态检查</h3>
              <p class="text-xs text-gray-400">Status Check</p>
            </div>
          </div>

          <div class="flex-1 flex flex-col justify-center items-center py-8">
            <div v-if="missingCount !== null" class="text-center animate-fade-in">
              <div class="text-5xl font-black mb-2" :class="missingCount > 0 ? 'text-orange-500' : 'text-green-500'">
                {{ missingCount }}
              </div>
              <div class="text-gray-500 text-sm">张图片缺失缩略图</div>
            </div>
            <div v-else class="text-gray-400 text-sm italic py-4">
              请点击下方按钮扫描系统磁盘
            </div>
          </div>

          <div class="mt-6">
            <n-button 
              block 
              secondary 
              strong
              type="primary" 
              :loading="checking" 
              @click="handleCheck"
            >
              <template #icon><i class="i-tabler-scan" /></template>
              立即检查缺失情况
            </n-button>
          </div>
        </div>
      </n-grid-item>

      <!-- 批量生成卡片 -->
      <n-grid-item>
        <div class="glass-card hover-rise p-6 h-full flex flex-col">
          <div class="flex items-center gap-3 mb-6">
            <div class="p-3 rounded-xl bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
              <i class="i-tabler-wand text-2xl" />
            </div>
            <div>
              <h3 class="text-lg font-bold">批量补全</h3>
              <p class="text-xs text-gray-400">Batch Generation</p>
            </div>
          </div>

          <div class="flex-1 text-sm text-gray-500 leading-relaxed">
            <p class="mb-4">批量生成程序将执行以下操作：</p>
            <ul class="space-y-3">
              <li class="flex items-start gap-2">
                <i class="i-tabler-check text-green-500 mt-1" />
                <span>递归扫描本地存储的所有子目录</span>
              </li>
              <li class="flex items-start gap-2">
                <i class="i-tabler-check text-green-500 mt-1" />
                <span>自动识别未生成缩略图的原图文件</span>
              </li>
              <li class="flex items-start gap-2">
                <i class="i-tabler-check text-green-500 mt-1" />
                <span>按 400x400 分辨率自动补齐缩略图</span>
              </li>
              <li class="flex items-start gap-2">
                <i class="i-tabler-check text-green-500 mt-1" />
                <span>处理过程高度安全，不覆盖已有文件</span>
              </li>
            </ul>
          </div>

          <div class="mt-6">
            <n-button 
              block 
              type="primary" 
              strong
              :loading="processing" 
              :disabled="missingCount === 0 || missingCount === null || !!(thumbnailStatus && thumbnailStatus.running)"
              @click="handleGenerate"
            >
              <template #icon><i class="i-tabler-player-play" /></template>
              {{ (thumbnailStatus && thumbnailStatus.running) ? '任务执行中...' : '开始批量补全' }}
            </n-button>
          </div>

          <!-- 进度条区域 -->
          <div v-if="thumbnailStatus && (thumbnailStatus.running || thumbnailStatus.progress > 0)" class="mt-6 animate-fade-in">
            <div class="flex justify-between text-xs mb-2">
              <span class="text-gray-400">{{ thumbnailStatus.message }}</span>
              <span class="font-mono font-bold text-primary">{{ thumbnailStatus.progress.toFixed(1) }}%</span>
            </div>
            <n-progress
              type="line"
              :percentage="thumbnailStatus.progress"
              :indicator-placement="'inside'"
              processing
              :status="thumbnailStatus.failed > 0 ? 'error' : 'success'"
              :height="18"
              border-radius="9"
            />
            <div class="flex justify-between text-[10px] mt-2 text-gray-400">
              <span>已处理: {{ thumbnailStatus.migrated }}</span>
              <span>失败: {{ thumbnailStatus.failed }}</span>
              <span>总数: {{ thumbnailStatus.total }}</span>
            </div>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 说明卡片 -->
    <n-card title="优化建议" class="mt-6 glass-card shadow-sm">
      <div class="text-sm text-gray-500">
        <p>本功能旨在解决历史存量图片的加载缓慢问题。缩略图生成后，小程序列表页将自动切换至轻量化预览图，首屏加载速度可提升 70% 以上。</p>
        <p class="mt-2 text-primary font-medium">提示：新上传的图片在上传瞬间已自动完成缩略图处理，无需手动在此补全。</p>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useMessage, useDialog, NGrid, NGridItem, NButton, NCard, NProgress } from 'naive-ui'
import http from '@/api/http'

interface ThumbnailStatus {
  total: number
  migrated: number
  failed: number
  progress: number
  running: boolean
  message: string
}

const message = useMessage()
const dialog = useDialog()

const checking = ref(false)
const processing = ref(false)
const missingCount = ref<number | null>(null)
const thumbnailStatus = ref<ThumbnailStatus | null>(null)
let pollTimer: number | null = null

// 检查缺失情况
const handleCheck = async () => {
  checking.value = true
  try {
    const res = await http.get('/admin/migration/thumbnails/check')
    missingCount.value = res.data
    if (res.data === 0) {
      message.success('检查完毕：磁盘所有图片均已拥有缩略图')
    } else {
      message.warning(`检查完毕：发现 ${res.data} 张图片需要补齐缩略图`)
    }
  } catch (error: any) {
    message.error('检查统计失败：' + (error.message || '网络异常'))
  } finally {
    checking.value = false
  }
}

// 轮询状态
const startPolling = () => {
  if (pollTimer) return
  pollTimer = window.setInterval(async () => {
    try {
      const res = await http.get('/admin/migration/thumbnails/status')
      thumbnailStatus.value = res.data
      if (res.data && !res.data.running) {
        stopPolling()
        processing.value = false
        // 任务完成后刷新一次缺失统计
        handleCheck()
      }
    } catch (e) {
      console.error('轮询失败', e)
    }
  }, 2000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 执行批量补全
const handleGenerate = () => {
  if (missingCount.value === 0 || missingCount.value === null) return

  dialog.info({
    title: '确认执行任务',
    content: `即将为发现的 ${missingCount.value} 张原始图片补齐缩略图。该操作将在后台异步执行，您可以随时离开此页面。确认开启？`,
    positiveText: '确认开启',
    negativeText: '取消',
    onPositiveClick: async () => {
      processing.value = true
      try {
        await http.post('/admin/migration/thumbnails/batch')
        message.success('后台任务已启动')
        startPolling()
      } catch (error: any) {
        message.error('启动失败：' + (error.message || '服务响应异常'))
        processing.value = false
      }
    }
  })
}

onMounted(() => {
  // 进来先查一遍状态，看是否有正在运行的任务
  http.get('/admin/migration/thumbnails/status').then(res => {
    if (res.data && res.data.running) {
      thumbnailStatus.value = res.data
      startPolling()
    }
  })
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.thumbnail-container {
  max-width: 1000px;
  margin: 0 auto;
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}
</style>
