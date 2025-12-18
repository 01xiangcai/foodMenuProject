<template>
  <div class="meal-orders-page p-6 scroll-smooth">
    <!-- 统计卡片区 - 采用订单记录同款渐变色 -->
    <div class="stats-grid mb-6 fade-in-up">
      <div class="stat-card gradient-purple">
        <div class="stat-icon"><i class="i-tabler-clipboard-list" /></div>
        <div class="stat-content">
          <div class="stat-value font-bold tracking-tight">{{ totalCount }}</div>
          <div class="stat-label">累计餐单</div>
          <div class="stat-hint">历史总餐次订单数</div>
        </div>
      </div>

      <div class="stat-card gradient-pink">
        <div class="stat-icon"><i class="i-tabler-loader" /></div>
        <div class="stat-content">
          <div class="stat-value font-bold tracking-tight">{{ pendingCount }}</div>
          <div class="stat-label">待处理</div>
          <div class="stat-hint">收集中订单数量</div>
        </div>
      </div>

      <div class="stat-card gradient-blue">
        <div class="stat-icon"><i class="i-tabler-users" /></div>
        <div class="stat-content">
          <div class="stat-value font-bold tracking-tight">{{ todayPeople }}</div>
          <div class="stat-label">参与规模</div>
          <div class="stat-hint">参与用户总人次</div>
        </div>
      </div>

      <div class="stat-card gradient-green">
        <div class="stat-icon"><i class="i-tabler-currency-yuan" /></div>
        <div class="stat-content">
          <div class="stat-value font-bold tracking-tight">¥{{ totalRevenue }}</div>
          <div class="stat-label">预估流水</div>
          <div class="stat-hint">所有餐次订单总额</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 - 参考订单记录的 Glass Card 风格 -->
    <div class="glass-card filter-section mb-6 fade-in-up" style="animation-delay: 0.1s">
      <!-- 状态页签 -->
      <n-tabs
        v-model:value="searchForm.status"
        type="line"
        animated
        class="status-tabs"
        @update:value="handleSearch"
      >
        <n-tab-pane
          v-for="option in statusOptionsWithTotal"
          :key="option.value"
          :name="option.value"
        >
          <template #tab>
            <div class="tab-with-badge">
              <span>{{ option.label }}</span>
              <n-badge
                v-if="statusCounts[option.value] > 0"
                :value="statusCounts[option.value]"
                :max="99"
                :type="getBadgeType(option.value)"
                class="tab-badge"
              />
            </div>
          </template>
        </n-tab-pane>
      </n-tabs>

      <!-- 次级过滤条 -->
      <div class="filter-bar mt-4 flex flex-wrap gap-4 items-center">
        <div class="flex items-center gap-2 group">
          <span class="text-sm font-bold text-text-secondary uppercase tracking-widest text-[10px]">时间范围</span>
          <n-date-picker 
            v-model:value="searchForm.dateRange" 
            type="daterange" 
            clearable 
            class="filter-input-date"
          />
        </div>
        
        <div class="flex items-center gap-2">
          <span class="text-sm font-bold text-text-secondary uppercase tracking-widest text-[10px]">餐次</span>
          <n-select
            v-model:value="searchForm.mealPeriod"
            :options="mealPeriodOptions"
            placeholder="全部餐次"
            clearable
            class="w-32"
          />
        </div>

        <div class="flex gap-2 ml-auto">
          <n-button strong secondary @click="handleReset" circle>
             <template #icon><i class="i-tabler-refresh" /></template>
          </n-button>
          <n-button type="primary" @click="handleSearch" class="px-6 rounded-xl" style="color: #000">
            <template #icon><i class="i-tabler-search" /></template>
            <span style="color: #000">筛选</span>
          </n-button>
        </div>
      </div>
    </div>

    <!-- 卡片网格列表 - 替换原表格 -->
    <div v-if="loading && tableData.length === 0" class="flex justify-center py-20">
      <n-spin size="large" />
    </div>

    <div v-else-if="tableData.length > 0">
      <div class="meal-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 fade-in-up" style="animation-delay: 0.2s">
        <div 
          v-for="row in tableData" 
          :key="row.id" 
          class="meal-card glass-card hover-rise-lg p-5"
          :class="`status-accent-${row.status}`"
        >
          <!-- 卡片头部: 日期与状态 -->
          <div class="flex items-start justify-between mb-4">
            <div>
              <div class="text-xs font-bold text-text-secondary uppercase tracking-tighter mb-1">{{ row.orderDate }}</div>
              <h3 class="text-lg font-black text-text-primary flex items-center gap-2">
                {{ getMealPeriodLabel(row.mealPeriod) }}
                <n-tag :type="getMealPeriodColor(row.mealPeriod)" size="small" round quaternary>
                   {{ row.mealPeriod }}
                </n-tag>
              </h3>
            </div>
            <n-tag :type="getStatusType(row.status)" round :bordered="false" class="status-indicator">
               <template #icon><i :class="getStatusIcon(row.status)" /></template>
               {{ getStatusLabel(row.status) }}
            </n-tag>
          </div>

          <!-- 核心统计展示 -->
          <div class="stats-overview grid grid-cols-2 gap-3 mb-5">
            <div class="overview-item">
              <span class="label">参与规模</span>
              <div class="value text-primary font-black text-xl">
                 {{ row.memberCount }} <span class="unit">人</span>
              </div>
            </div>
            <div class="overview-item">
              <span class="label">预估流水</span>
              <div class="value text-green-600 font-black text-xl">
                 <span class="currency">¥</span>{{ row.totalAmount }}
              </div>
            </div>
            <div class="overview-item col-span-2 flex items-center gap-2 mt-1">
               <span class="label">菜品配置:</span>
               <span class="text-orange-500 font-bold">{{ row.dishCount }} 道菜品</span>
            </div>
          </div>

          <!-- 底部操作区 -->
          <div class="flex items-center justify-between border-t border-gray-100 dark:border-gray-800 pt-4">
            <!-- 参与者头像预览 (简单展示前3个) -->
            <div class="avatar-group flex -space-x-2">
              <template v-if="row.memberOrders">
                <n-avatar 
                  v-for="(member, idx) in row.memberOrders.slice(0, 3)" 
                  :key="idx"
                  :src="member.avatar" 
                  round 
                  size="small"
                  class="border-2 border-white dark:border-gray-900"
                />
                <div v-if="row.memberCount > 3" class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center text-[10px] text-gray-500 border-2 border-white dark:border-gray-900">
                  +{{ row.memberCount - 3 }}
                </div>
              </template>
            </div>
            
            <n-button 
              type="primary" 
              quaternary 
              @click="handleViewDetail(row.id)"
              class="font-bold rounded-lg"
            >
              详情 & 管理 <i class="i-tabler-chevron-right ml-1" />
            </n-button>
          </div>
        </div>
      </div>

      <!-- 分页控制 -->
      <div class="mt-8 flex justify-center pb-10">
        <n-pagination
          v-model:page="pagination.page"
          :item-count="pagination.itemCount"
          :page-size="pagination.pageSize"
          @update:page="handlePageChange"
        />
      </div>
    </div>

    <n-empty v-else description="暂无匹配的餐次订单" style="margin-top: 100px" />

    <!-- 订单明细 Modal -->
    <n-modal 
      v-model:show="showDetail" 
      preset="card" 
      class="meal-detail-modal"
      style="width: 1100px; border-radius: 20px" 
      title="餐次订单详情"
    >
      <div v-if="currentDetail" class="space-y-6">
        <!-- 头部摘要 -->
        <div class="detail-hero p-6 rounded-2xl bg-gradient-to-r from-primary/5 to-secondary/5 border border-primary/10 flex items-center justify-between">
          <div class="flex items-center gap-5">
            <div class="icon-circle bg-primary/20 text-primary w-14 h-14 rounded-full flex items-center justify-center text-2xl">
              <i class="i-tabler-calendar-event" />
            </div>
            <div>
              <div class="text-sm font-bold text-text-secondary opacity-60">{{ currentDetail.orderDate }}</div>
              <div class="text-2xl font-black text-text-primary">{{ getMealPeriodLabel(currentDetail.mealPeriod) }} · 订单明细</div>
            </div>
          </div>
          <n-tag :type="getStatusType(currentDetail.status)" size="large" round>
            {{ getStatusLabel(currentDetail.status) }}
          </n-tag>
        </div>

        <!-- 详细数据列 -->
        <div class="grid grid-cols-4 gap-4">
          <div v-for="s in [
            { l: '参与人数', v: currentDetail.memberCount, u: '人', c: 'blue' },
            { l: '菜品总数', v: currentDetail.dishCount, u: '道', c: 'orange' },
            { l: '订单流水', v: currentDetail.totalAmount, u: '元', p: '¥', c: 'green' },
            { l: '操作员', v: currentDetail.confirmedBy || '系统', u: '', c: 'purple' }
          ]" :key="s.l" class="p-4 rounded-xl border border-gray-100 dark:border-gray-800 text-center">
            <div class="text-[10px] uppercase tracking-tighter text-text-secondary mb-1">{{ s.l }}</div>
            <div :class="`text-xl font-black text-${s.c}-600 dark:text-${s.c}-400`">
              <span v-if="s.p" class="text-sm mr-0.5">{{ s.p }}</span>{{ s.v }}<span v-if="s.u" class="text-xs ml-1 opacity-60">{{ s.u }}</span>
            </div>
          </div>
        </div>

        <!-- 成员订单列表 -->
        <div class="detail-section">
          <h3 class="section-title">👥 成员个单明细 ({{ currentDetail.memberOrders?.length || 0 }})</h3>
          <n-scrollbar style="max-height: 600px">
            <div class="members-list">
              <div 
                v-for="member in currentDetail.memberOrders" 
                :key="member.orderId"
                class="member-order-card"
              >
                <!-- 成员信息头 -->
                <div class="member-header">
                  <div class="member-user">
                    <img 
                      :src="getImgUrl(member.avatar)" 
                      class="member-avatar"
                      @error="handleImageError"
                    />
                    <div class="member-info">
                      <div class="member-name">{{ member.nickname || '神秘参与者' }}</div>
                      <div class="member-order-number">订单号: {{ member.orderNumber ? member.orderNumber.slice(-8) : '---' }}</div>
                    </div>
                  </div>
                  <div class="member-total">¥{{ member.totalAmount }}</div>
                </div>
                
                <!-- 菜品列表 -->
                <div class="dishes-list">
                  <div v-for="item in member.items" :key="item.id" class="dish-detail-item">
                    <img
                      :src="getImgUrl(item.dishImage)"
                      :alt="item.dishName"
                      class="dish-detail-image"
                      @error="handleImageError"
                    />
                    <div class="dish-detail-info">
                      <div class="dish-detail-name">{{ item.dishName }}</div>
                      <div class="dish-detail-price">¥{{ item.price }} × {{ item.quantity }}</div>
                    </div>
                    <div class="dish-detail-subtotal">¥{{ item.subtotal }}</div>
                  </div>
                </div>
              </div>
            </div>
          </n-scrollbar>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted, computed, watch } from 'vue';
import {
  NCard, NTabs, NTabPane, NBadge, NDatePicker, NSelect, NButton, 
  NTag, NPagination, NModal, NAvatar, NSpin, NEmpty, NScrollbar,
  useMessage
} from 'naive-ui';
import http from '@/api/http';
import { fetchDailyMealOrderStats } from '@/api/modules';

const message = useMessage();

// 核心统计数据
const totalCount = ref(0);
const pendingCount = ref(0);
const todayPeople = ref(0);
const totalRevenue = ref('0.00');
const statusCounts = reactive<Record<number, number>>({
  [-1]: 0, 0: 0, 1: 0, 2: 0
});

// 搜索表单
const searchForm = reactive({
  dateRange: null as [number, number] | null,
  mealPeriod: null,
  status: -1 // 默认全部
});

// 配置项
const mealPeriodOptions = [
  { label: '早餐', value: 'BREAKFAST' },
  { label: '中餐', value: 'LUNCH' },
  { label: '晚餐', value: 'DINNER' }
];

const statusOptionsWithTotal = [
  { label: '全部餐次', value: -1 },
  { label: '收集中', value: 0 },
  { label: '正常供应', value: 1 },
  { label: '已收官', value: 2 }
];

// 数据列表
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({
  page: 1,
  pageSize: 12, // 卡片布局 3x4，填补底部空白
  itemCount: 0
});

// 详情
const showDetail = ref(false);
const currentDetail = ref<any>(null);

// 辅助方法
const getMealPeriodLabel = (period: string) => ({ BREAKFAST: '早餐', LUNCH: '中餐', DINNER: '晚餐' }[period] || period);
const getMealPeriodColor = (period: string) => ({ BREAKFAST: 'warning', LUNCH: 'error', DINNER: 'info' }[period] || 'default') as 'warning' | 'error' | 'info' | 'default';
const getStatusLabel = (status: number) => ({ 0: '收集中', 1: '正常供应', 2: '已收官' }[status] || '结算中');
const getStatusType = (status: number) => ({ 0: 'info', 1: 'success', 2: 'error' }[status] || 'default') as 'info' | 'success' | 'error' | 'default';
const getStatusIcon = (status: number) => ({ 0: 'i-tabler-loader', 1: 'i-tabler-check', 2: 'i-tabler-clock-stop' }[status] || '');
const getBadgeType = (status: number) => ({ 0: 'info', 1: 'success', 2: 'warning' }[status] || 'default') as 'info' | 'success' | 'warning' | 'default';

// 图片处理
const getImgUrl = (url: string | undefined) => {
  if (!url) return '';
  if (url.startsWith('http') || url.startsWith('data:')) return url;
  if (url.startsWith('/api')) return url;
  return `/api${url.startsWith('/') ? '' : '/'}${url}`;
};

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  target.src = 'https://dummyimage.com/100x100/e2e8f0/94a3b8&text=No+Image';
};

// 数据加载逻辑
const loadStats = async () => {
  try {
    const res = await fetchDailyMealOrderStats();
    if (res.data) {
      const { counts, totalRevenue: rev, totalPeople: ppl, totalCount: tot, pendingCount: pend } = res.data;
      Object.assign(statusCounts, counts);
      totalRevenue.value = rev;
      todayPeople.value = ppl;
      totalCount.value = tot;
      pendingCount.value = pend;
    }
  } catch (error) {
    console.warn('统计数据加载失败', error);
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    // 异步加载统计数据，保证徽标始终正确
    loadStats();

    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    };

    if (searchForm.dateRange) {
      params.startDate = new Date(searchForm.dateRange[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchForm.dateRange[1]).toISOString().split('T')[0];
    }
    if (searchForm.mealPeriod) params.mealPeriod = searchForm.mealPeriod;
    if (searchForm.status !== -1) params.status = searchForm.status;

    const res = await http.get('/admin/daily-meal-order', { params });
    tableData.value = res.data.records || [];
    pagination.itemCount = res.data.total || 0;
  } catch (error) {
    message.error('订单数据拉取失败');
  } finally {
    loading.value = false;
  }
};

const handleViewDetail = async (id: number) => {
  try {
    const res = await http.get(`/admin/daily-meal-order/${id}`);
    if (res.data && res.data.dailyMealOrder) {
      // 合并大订单基础信息和成员订单列表，方便模板访问
      currentDetail.value = {
        ...res.data.dailyMealOrder,
        memberOrders: res.data.memberOrders || []
      };
      showDetail.value = true;
    }
  } catch (error) {
    message.error('详情加载失败');
  }
};

const handleSearch = () => {
  pagination.page = 1;
  loadData();
};

const handleReset = () => {
  searchForm.dateRange = null;
  searchForm.mealPeriod = null;
  searchForm.status = -1;
  handleSearch();
};

const handlePageChange = (page: number) => {
  pagination.page = page;
  loadData();
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.meal-orders-page {
  animation: fadeIn 0.5s ease-out;
}

/* 统计卡片样式 - 同 OrdersView */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  padding: 24px;
  border-radius: 20px;
  color: white;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px -5px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.stat-value {
  font-size: 28px;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.stat-hint {
  font-size: 10px;
  opacity: 0.6;
  margin-top: 4px;
}

/* 渐变背景 */
.gradient-purple { background: linear-gradient(135deg, #8b5cf6, #6d28d9); }
.gradient-pink { background: linear-gradient(135deg, #ec4899, #be185d); }
.gradient-blue { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
.gradient-green { background: linear-gradient(135deg, #10b981, #047857); }

/* Glass Card */
.glass-card {
  @apply bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border border-white/20 dark:border-gray-800/20 rounded-2xl;
}

.filter-section {
  padding: 12px 24px 24px;
}

.status-tabs :deep(.n-tabs-tab) {
  padding: 12px 20px;
}

.tab-with-badge {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Card Design */
.meal-card {
  border-top-width: 4px;
}

.status-accent-0 { border-top-color: #3b82f6; }
.status-accent-1 { border-top-color: #10b981; }
.status-accent-2 { border-top-color: #f59e0b; }

.hover-rise-lg:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: 0 20px 40px -10px rgba(0,0,0,0.1);
}

.overview-item .label {
  @apply text-[10px] text-text-secondary uppercase tracking-wider block mb-0.5;
}

.unit, .currency {
  @apply text-xs font-medium opacity-50 ml-0.5;
}

/* Animations */
.fade-in-up {
  animation: fadeInUp 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Modal Styling */
:deep(.n-card-header__main) { font-weight: 900 !important; }

/* 详情弹窗样式 - 参考 OrdersView */
.detail-section {
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.detail-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--primary-color, #3b82f6);
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.member-order-card {
  padding: 16px;
  border-radius: 12px;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.03));
  border: 1px solid var(--border-secondary, rgba(255, 255, 255, 0.1));
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--border-secondary, rgba(255, 255, 255, 0.1));
}

.member-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.05));
  border: 2px solid var(--primary-color, #3b82f6);
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.member-order-number {
  font-size: 12px;
  color: var(--text-secondary);
  opacity: 0.7;
}

.member-total {
  font-size: 18px;
  font-weight: 800;
  color: #10b981;
}

.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dish-detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.02));
  border-radius: 8px;
}

.dish-detail-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.05));
  flex-shrink: 0;
}

.dish-detail-info {
  flex: 1;
  min-width: 0;
}

.dish-detail-name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
  color: var(--text-primary);
}

.dish-detail-price {
  font-size: 13px;
  color: var(--text-secondary);
  opacity: 0.8;
}

.dish-detail-subtotal {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary-color, #3b82f6);
  flex-shrink: 0;
}
</style>
