<template>
  <div class="orders-page">
    <!-- 统计卡片区 -->
    <div class="stats-grid">
      <div class="stat-card gradient-purple">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <div class="stat-value">{{ todayOrders }}</div>
          <div class="stat-label">今日订单</div>
        </div>
      </div>

      <div class="stat-card gradient-pink">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <div class="stat-value">{{ pendingOrders }}</div>
          <div class="stat-label">待处理</div>
          <div class="stat-hint">待接单 + 准备中</div>
        </div>
      </div>

      <div class="stat-card gradient-blue">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <div class="stat-value">¥{{ todayRevenue }}</div>
          <div class="stat-label">今日营收</div>
        </div>
      </div>

      <div class="stat-card gradient-green">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">¥{{ monthRevenue }}</div>
          <div class="stat-label">本月营收</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="glass-card filter-section">
      <div class="filter-bar">
        <NInput
          v-model:value="filters.keyword"
          placeholder="搜索订单号"
          clearable
          style="width: 200px"
        >
          <template #prefix>
            <span>🔍</span>
          </template>
        </NInput>

        <NDatePicker
          v-model:value="filters.dateRange"
          type="daterange"
          clearable
          style="width: 280px"
        />

        <NSelect
          v-model:value="filters.status"
          :options="statusOptions"
          placeholder="订单状态"
          clearable
          style="width: 140px"
        />

        <NButton type="primary" @click="handleSearch">搜索</NButton>
        <NButton @click="handleReset">重置</NButton>
        <NButton secondary @click="() => loadOrders(true)">
          <template #icon>
            <span>🔄</span>
          </template>
          刷新
        </NButton>
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-if="ordersLoading" class="loading-container">
      <NSpin size="large" />
    </div>



    <div v-else-if="filteredOrders.length > 0">
      <div class="orders-grid">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-card glass-card hover-rise"
          :class="`status-${order.status}`"
        >
          <!-- 订单头部 -->
          <div class="order-header">
            <div class="order-number">
              <span class="label">订单号</span>
              <span class="value">{{ order.orderNumber }}</span>
            </div>
            <NTag :type="getStatusType(order.status)" :bordered="false">
              {{ getStatusText(order.status) }}
            </NTag>
          </div>

          <!-- 菜品预览 -->
          <div class="order-dishes">
            <div
              v-for="(item, index) in order.orderItems.slice(0, 3)"
              :key="item.id"
              class="dish-item"
            >
              <img
                :src="item.dishImage || '/default-dish.png'"
                :alt="item.dishName"
                class="dish-image"
                @error="handleImageError"
              />
              <div class="dish-info">
                <div class="dish-name">{{ item.dishName }}</div>
                <div class="dish-detail">¥{{ item.price }} × {{ item.quantity }}</div>
              </div>
              <div class="dish-subtotal">¥{{ item.subtotal }}</div>
            </div>

            <div v-if="order.orderItems.length > 3" class="more-dishes">
              +{{ order.orderItems.length - 3 }} 道菜品
            </div>
          </div>

          <!-- 订单底部 -->
          <div class="order-footer">
            <div class="order-info">
              <div class="order-user" v-if="order.userNickname || order.userPhone">
                <img 
                  v-if="order.userAvatar" 
                  :src="order.userAvatar" 
                  class="user-avatar"
                  @error="handleImageError"
                />
                <div class="user-info">
                  <div class="user-nickname">{{ order.userNickname || '未知用户' }}</div>
                  <div class="user-phone">{{ order.userPhone || '—' }}</div>
                </div>
              </div>
              <div class="order-time">{{ formatTime(order.createTime) }}</div>
              <div class="order-amount">
                总计：<span class="amount-value">¥{{ order.totalAmount }}</span>
              </div>
            </div>

            <div class="order-actions">
              <NButton size="small" tertiary @click="openDetail(order.id)"> 详情 </NButton>

              <NButton v-if="order.status === 0" size="small" type="primary" class="action-btn" @click="updateStatus(order.id, 1)">
                接单
              </NButton>

              <NButton v-if="order.status === 1" size="small" type="warning" class="action-btn-warning" @click="updateStatus(order.id, 2)">
                配送
              </NButton>

              <NButton v-if="order.status === 2" size="small" type="success" class="action-btn-success" @click="updateStatus(order.id, 3)">
                完成
              </NButton>

              <NButton
                v-if="order.status < 3"
                size="small"
                type="error"
                tertiary
                @click="cancelOrder(order.id)"
              >
                取消
              </NButton>
            </div>
          </div>

          <!-- 备注 -->
          <div v-if="order.remark" class="order-remark">
            <span class="remark-icon">💬</span>
            <span class="remark-text">{{ order.remark }}</span>
          </div>
        </div>
      </div>

      <!-- 加载更多按钮 -->
      <div v-if="hasMore" class="load-more-container">
        <NButton 
          :loading="loadingMore" 
          @click="loadMore"
          secondary
          size="large"
          block
        >
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </NButton>
      </div>
      <div v-else-if="orders.length > 0" class="no-more-hint">
        已加载全部订单
      </div>
    </div>

    <NEmpty v-else description="暂无订单数据" style="margin: 60px 0" />

    <!-- 订单详情Modal -->
    <NModal v-model:show="detailModal.show" preset="card" title="订单详情" style="max-width: 680px">
      <NSpin :show="detailModal.loading">
        <div v-if="detailModal.data" class="order-detail">
          <!-- 订单信息区 -->
          <div class="detail-section">
            <h3 class="section-title">📋 订单信息</h3>
            
            <!-- 用户信息 -->
            <div v-if="detailModal.data.userNickname || detailModal.data.userPhone" class="user-detail-box">
              <img 
                v-if="detailModal.data.userAvatar" 
                :src="detailModal.data.userAvatar" 
                class="user-detail-avatar"
                @error="handleImageError"
              />
              <div class="user-detail-info">
                <div class="user-detail-name">{{ detailModal.data.userNickname || '未知用户' }}</div>
                <div class="user-detail-phone">📱 {{ detailModal.data.userPhone || '—' }}</div>
              </div>
            </div>
            
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">订单号</span>
                <span class="info-value">{{ detailModal.data.orderNumber }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">订单状态</span>
                <NTag :type="getStatusType(detailModal.data.status)">
                  {{ getStatusText(detailModal.data.status) }}
                </NTag>
              </div>
              <div class="info-item">
                <span class="info-label">下单时间</span>
                <span class="info-value">{{ formatTime(detailModal.data.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">订单金额</span>
                <span class="info-value amount">¥{{ detailModal.data.totalAmount }}</span>
              </div>
            </div>

            <div v-if="detailModal.data.remark" class="remark-box">
              <strong>备注：</strong>{{ detailModal.data.remark }}
            </div>
          </div>

          <!-- 菜品明细区 -->
          <div class="detail-section">
            <h3 class="section-title">🍽️ 菜品明细</h3>
            <div class="dishes-list">
              <div v-for="item in detailModal.data.orderItems" :key="item.id" class="dish-detail-item">
                <img
                  :src="item.dishImage || '/default-dish.png'"
                  :alt="item.dishName"
                  class="dish-detail-image"
                  @error="handleImageError"
                />
                <div class="dish-detail-info">
                  <div class="dish-detail-name">{{ item.dishName }}</div>
                  <div class="dish-detail-price">单价：¥{{ item.price }} × {{ item.quantity }}</div>
                </div>
                <div class="dish-detail-subtotal">¥{{ item.subtotal }}</div>
              </div>
            </div>

            <div class="total-row">
              <span>总计</span>
              <span class="total-amount">¥{{ detailModal.data.totalAmount }}</span>
            </div>
          </div>

          <!-- 操作记录 -->
          <div class="detail-section">
            <h3 class="section-title">⏱️ 操作记录</h3>
            <NTimeline>
              <NTimelineItem type="success" title="订单创建" :time="formatTime(detailModal.data.createTime)" />
              <NTimelineItem v-if="detailModal.data.status >= 1" type="info" title="商家接单" />
              <NTimelineItem v-if="detailModal.data.status >= 2" type="warning" title="开始配送" />
              <NTimelineItem v-if="detailModal.data.status === 3" type="success" title="订单完成" />
              <NTimelineItem v-if="detailModal.data.status === 4" type="error" title="订单取消" />
            </NTimeline>
          </div>
        </div>
      </NSpin>

      <template #action>
        <div class="modal-actions">
          <NButton @click="detailModal.show = false">关闭</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import {
  NButton,
  NDatePicker,
  NEmpty,
  NInput,
  NModal,
  NSelect,
  NSpin,
  NTag,
  NTimeline,
  NTimelineItem,
  useDialog,
  useMessage
} from 'naive-ui';
import { fetchOrders, updateOrderStatus } from '@/api/modules';

type OrderItemRecord = {
  id: number;
  orderId: number;
  dishId: number;
  dishName: string;
  dishImage?: string;
  price: string | number;
  quantity: number;
  subtotal: string | number;
  createTime: string;
};

type OrderRecord = {
  id: number;
  orderNumber: string;
  userId: number;
  totalAmount: string | number;
  status: number;
  remark?: string;
  createTime: string;
  updateTime: string;
  orderItems: OrderItemRecord[];
  // User information
  userNickname?: string;
  userPhone?: string;
  userAvatar?: string;
};

type OrderFilters = {
  keyword: string;
  dateRange: [number, number] | null;
  status: number | null;
};

const message = useMessage();
const dialog = useDialog();

const orders = ref<OrderRecord[]>([]);
const ordersLoading = ref(false);
const loadingMore = ref(false);
const currentPage = ref(1);
const pageSize = 6;
const hasMore = ref(true);

const filters = reactive<OrderFilters>({
  keyword: '',
  dateRange: null,
  status: null
});

const detailModal = reactive({
  show: false,
  loading: false,
  data: null as OrderRecord | null
});

const statusOptions = [
  { label: '全部状态', value: -1 },
  { label: '待接单', value: 0 },
  { label: '准备中', value: 1 },
  { label: '配送中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
];

const statusMap: Record<number, { text: string; type: 'warning' | 'info' | 'default' | 'success' | 'error' }> = {
  0: { text: '待接单', type: 'warning' },
  1: { text: '准备中', type: 'info' },
  2: { text: '配送中', type: 'default' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'error' }
};

// 统计数据
const todayOrders = computed(() => orders.value.filter((o) => isToday(o.createTime)).length);

const pendingOrders = computed(() => orders.value.filter((o) => o.status === 0 || o.status === 1).length);

const todayRevenue = computed(() =>
  orders.value
    .filter((o) => isToday(o.createTime) && o.status !== 4)
    .reduce((sum, o) => sum + Number(o.totalAmount), 0)
    .toFixed(2)
);

const monthRevenue = computed(() =>
  orders.value
    .filter((o) => isThisMonth(o.createTime) && o.status !== 4)
    .reduce((sum, o) => sum + Number(o.totalAmount), 0)
    .toFixed(2)
);

// 筛选后的订单
const filteredOrders = computed(() => {
  let result = orders.value;

  if (filters.keyword) {
    result = result.filter((o) => o.orderNumber.toLowerCase().includes(filters.keyword.toLowerCase()));
  }

  if (filters.dateRange && filters.dateRange.length === 2) {
    const [start, end] = filters.dateRange;
    result = result.filter((o) => {
      const time = new Date(o.createTime).getTime();
      return time >= start && time <= end + 86400000; // +1天
    });
  }

  if (filters.status !== null && filters.status !== -1) {
    result = result.filter((o) => o.status === filters.status);
  }

  return result;
});

const getStatusText = (status: number) => statusMap[status]?.text || '未知';
const getStatusType = (status: number) => statusMap[status]?.type || 'default';

const isToday = (dateStr: string) => {
  const date = new Date(dateStr);
  const today = new Date();
  return (
    date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate()
  );
};

const isThisMonth = (dateStr: string) => {
  const date = new Date(dateStr);
  const today = new Date();
  return date.getFullYear() === today.getFullYear() && date.getMonth() === today.getMonth();
};

const formatTime = (value?: string) => {
  if (!value) return '—';
  try {
    return new Intl.DateTimeFormat('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect fill="%23ddd" width="60" height="60"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="14" fill="%23999"%3E暂无图片%3C/text%3E%3C/svg%3E';
};

const handleSearch = () => {
  // 筛选逻辑已在 computed 中实现
};

const handleReset = () => {
  filters.keyword = '';
  filters.dateRange = null;
  filters.status = null;
};

const loadOrders = async (reset = true) => {
  if (reset) {
    ordersLoading.value = true;
    currentPage.value = 1;
    orders.value = [];
  } else {
    loadingMore.value = true;
  }
  
  try {
    const result = await fetchOrders({ page: currentPage.value, pageSize });
    const newOrders = result.data?.records || [];
    
    if (reset) {
      orders.value = newOrders;
    } else {
      orders.value = [...orders.value, ...newOrders];
    }
    
    // 检查是否还有更多数据
    hasMore.value = newOrders.length === pageSize;
  } catch (error) {
    message.error((error as Error).message || '加载订单失败');
  } finally {
    ordersLoading.value = false;
    loadingMore.value = false;
  }
};

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return;
  currentPage.value++;
  await loadOrders(false);
};

const updateStatus = async (id: number, status: number) => {
  try {
    await updateOrderStatus(id, status);
    message.success('订单状态已更新');
    await loadOrders();
  } catch (error) {
    message.error((error as Error).message || '更新失败');
  }
};

const cancelOrder = (id: number) => {
  dialog.warning({
    title: '确认取消',
    content: '确定要取消这个订单吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updateStatus(id, 4);
    }
  });
};

const openDetail = async (id: number) => {
  const order = orders.value.find((o) => o.id === id);
  if (order) {
    detailModal.data = order;
    detailModal.show = true;
  }
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.orders-page {
  padding: 0;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 24px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: var(--shadow-lg);
  transition: transform 0.3s;
  color: white;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-glow);
}

.gradient-purple {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.gradient-pink {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.gradient-blue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.gradient-green {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon {
  font-size: 48px;
  line-height: 1;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 4px;
}

.stat-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 2px;
}

/* 筛选栏 */
.filter-section {
  padding: 20px;
  margin-bottom: 24px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

/* 订单列表 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
}

.order-card {
  padding: 20px;
  border-radius: 16px;
  border-left: 4px solid transparent;
  transition: all 0.3s;
  background: var(--bg-card);
}

.order-card.status-0 {
  border-left-color: var(--accent-warning);
}

.order-card.status-1 {
  border-left-color: var(--primary-color);
}

.order-card.status-2 {
  border-left-color: var(--secondary-color);
}

.order-card.status-3 {
  border-left-color: var(--accent-success);
}

.order-card.status-4 {
  border-left-color: var(--accent-error);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-secondary);
}

.order-number .label {
  font-size: 12px;
  opacity: 0.7;
  margin-right: 8px;
  color: var(--text-secondary);
}

.order-number .value {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.order-dishes {
  margin-bottom: 16px;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--border-secondary);
}

.dish-item:last-child {
  border-bottom: none;
}

.dish-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  background: var(--bg-body);
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.dish-name {
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}

.dish-detail {
  font-size: 12px;
  opacity: 0.7;
  color: var(--text-secondary);
}

.dish-subtotal {
  font-weight: 600;
  color: var(--primary-color);
}

.more-dishes {
  text-align: center;
  padding: 8px;
  font-size: 12px;
  opacity: 0.7;
  background: var(--bg-body);
  border-radius: 8px;
  margin-top: 8px;
  color: var(--text-secondary);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  padding: 8px;
  background: var(--bg-body);
  border-radius: 8px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--bg-card);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-nickname {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}

.user-phone {
  font-size: 12px;
  opacity: 0.7;
  color: var(--text-secondary);
}

.order-time {
  font-size: 12px;
  opacity: 0.7;
  margin-bottom: 4px;
  color: var(--text-secondary);
}

.order-amount {
  font-size: 14px;
  color: var(--text-primary);
}

.amount-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  margin-left: 4px;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.order-remark {
  margin-top: 12px;
  padding: 10px;
  background: rgba(var(--primary-h), var(--primary-s), var(--primary-l), 0.1);
  border-radius: 8px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
}

.action-btn {
  background-color: var(--primary-color);
  color: white;
  border: none;
}

.action-btn:hover {
  background-color: var(--primary-color-hover);
}

.action-btn-warning {
  background-color: var(--accent-warning);
  color: white;
  border: none;
}

.action-btn-warning:hover {
  filter: brightness(1.1);
}

.action-btn-success {
  background-color: var(--accent-success);
  color: white;
  border: none;
}

.action-btn-success:hover {
  filter: brightness(1.1);
}

.remark-icon {
  font-size: 16px;
}

.remark-text {
  flex: 1;
  opacity: 0.9;
}

/* 详情Modal */
.order-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.detail-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #14b8ff;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 12px;
  opacity: 0.7;
}

.info-value {
  font-weight: 500;
}

.info-value.amount {
  font-size: 18px;
  color: #14b8ff;
  font-weight: 700;
}

.remark-box {
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  font-size: 13px;
}

.user-detail-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(20, 184, 255, 0.1), rgba(168, 85, 247, 0.1));
  border-radius: 12px;
  margin-bottom: 16px;
}

.user-detail-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(20, 184, 255, 0.3);
}

.user-detail-info {
  flex: 1;
  min-width: 0;
}

.user-detail-name {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 6px;
  color: #14b8ff;
}

.user-detail-phone {
  font-size: 14px;
  opacity: 0.9;
}

.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.dish-detail-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.05);
}

.dish-detail-info {
  flex: 1;
  min-width: 0;
}

.dish-detail-name {
  font-weight: 600;
  margin-bottom: 6px;
}

.dish-detail-price {
  font-size: 13px;
  opacity: 0.7;
}

.dish-detail-subtotal {
  font-size: 16px;
  font-weight: 700;
  color: #14b8ff;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(20, 184, 255, 0.1);
  border-radius: 8px;
  margin-top: 12px;
  font-weight: 600;
}

.total-amount {
  font-size: 24px;
  color: #14b8ff;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.load-more-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.no-more-hint {
  text-align: center;
  margin-top: 24px;
  color: var(--text-secondary);
  font-size: 14px;
  opacity: 0.7;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .orders-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
