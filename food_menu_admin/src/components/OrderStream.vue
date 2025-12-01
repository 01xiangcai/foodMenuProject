<template>
  <div class="stream">
    <div v-if="loading" class="loading-hint">加载中...</div>
    <div v-else-if="orders.length === 0" class="empty-hint">暂无订单</div>
    <div
      v-else
      v-for="order in orders"
      :key="order.id"
      class="stream-item"
      :class="`status-${order.status}`"
      @click="handleOrderClick(order.id)"
    >
      <div class="order-info">
        <div class="order-header">
          <span class="order-number">{{ order.orderNumber }}</span>
          <span class="order-amount">¥{{ order.totalAmount }}</span>
        </div>
        <div class="order-meta">
          <div v-if="order.userNickname" class="user-info">
            <img
              v-if="order.userAvatar"
              :src="order.userAvatar"
              class="user-avatar"
              @error="handleImageError"
            />
            <span class="user-name">{{ order.userNickname }}</span>
          </div>
          <span class="order-time">{{ formatRelativeTime(order.createTime) }}</span>
        </div>
      </div>
      <div class="order-status" :class="`status-${order.status}`">
        {{ getStatusText(order.status) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchOrders } from '@/api/modules';

type OrderItem = {
  id: number;
  orderNumber: string;
  totalAmount: string | number;
  status: number;
  createTime: string;
  userNickname?: string;
  userAvatar?: string;
};

const router = useRouter();
const orders = ref<OrderItem[]>([]);
const loading = ref(false);

const statusMap: Record<number, string> = {
  0: '待接单',
  1: '准备中',
  2: '配送中',
  3: '已完成',
  4: '已取消'
};

const getStatusText = (status: number) => statusMap[status] || '未知';

// 格式化相对时间
const formatRelativeTime = (timeStr: string) => {
  if (!timeStr) return '';
  const now = new Date().getTime();
  const time = new Date(timeStr).getTime();
  const diff = Math.floor((now - time) / 1000); // 秒

  if (diff < 60) return `${diff}秒前`;
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`;
  return `${Math.floor(diff / 86400)}天前`;
};

// 处理图片加载错误
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="24" height="24"%3E%3Ccircle cx="12" cy="12" r="10" fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="12" fill="%23999"%3E用户%3C/text%3E%3C/svg%3E';
};

// 加载订单数据
const loadOrders = async () => {
  loading.value = true;
  try {
    const result = await fetchOrders({ page: 1, pageSize: 30 });
    const allOrders = result.data?.records || [];

    if (allOrders.length === 0) {
      orders.value = [];
      return;
    }

    // 按状态分组
    const ordersByStatus: Record<number, OrderItem[]> = {
      0: [], // 待接单
      1: [], // 准备中
      2: [], // 配送中
      3: [], // 已完成
      4: []  // 已取消
    };

    allOrders.forEach((order: OrderItem) => {
      if (ordersByStatus[order.status] !== undefined) {
        ordersByStatus[order.status].push(order);
      }
    });

    // 按时间倒序排序每个状态的订单
    Object.keys(ordersByStatus).forEach(status => {
      ordersByStatus[parseInt(status)].sort((a, b) => 
        new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      );
    });

    // 优先显示待处理订单（0,1,2），然后显示已完成的订单
    // 最终显示4条订单
    const resultOrders: OrderItem[] = [];
    
    // 收集所有待处理订单（0,1,2状态）
    const pendingOrders: OrderItem[] = [
      ...ordersByStatus[0],
      ...ordersByStatus[1],
      ...ordersByStatus[2]
    ];
    
    // 收集已完成的订单
    const completedOrders = ordersByStatus[3];
    
    // 先添加待处理订单（按时间倒序）
    pendingOrders.sort((a, b) => 
      new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
    );
    resultOrders.push(...pendingOrders);
    
    // 如果待处理订单不足4条，补充已完成的订单（按时间倒序）
    if (resultOrders.length < 4 && completedOrders.length > 0) {
      const remaining = 4 - resultOrders.length;
      resultOrders.push(...completedOrders.slice(0, remaining));
    }
    
    // 最终排序：待处理订单在前（按状态优先级0<1<2），然后按时间倒序
    resultOrders.sort((a, b) => {
      // 待处理订单（0,1,2）优先于已完成订单（3）
      if (a.status <= 2 && b.status > 2) return -1;
      if (a.status > 2 && b.status <= 2) return 1;
      
      // 如果都是待处理订单，按状态优先级排序（0 < 1 < 2）
      if (a.status <= 2 && b.status <= 2) {
        if (a.status !== b.status) {
          return a.status - b.status;
        }
      }
      
      // 相同状态或都是已完成，按时间倒序
      return new Date(b.createTime).getTime() - new Date(a.createTime).getTime();
    });

    orders.value = resultOrders.slice(0, 4);
  } catch (error) {
    console.error('加载订单失败:', error);
  } finally {
    loading.value = false;
  }
};

// 点击订单跳转
const handleOrderClick = (orderId: number) => {
  router.push({
    path: '/orders',
    query: { orderId: orderId.toString() }
  });
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.stream {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.stream-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
}

.stream-item:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(20, 184, 255, 0.3);
  transform: translateX(4px);
}

.stream-item.status-0 {
  border-left: 3px solid #f59e0b;
}

.stream-item.status-1 {
  border-left: 3px solid #3b82f6;
}

.stream-item.status-2 {
  border-left: 3px solid #8b5cf6;
}

.stream-item.status-3 {
  border-left: 3px solid #10b981;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 12px;
}

.order-number {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-amount {
  font-weight: 700;
  font-size: 14px;
  color: #14b8ff;
  flex-shrink: 0;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.1);
}

.user-name {
  color: var(--text-secondary);
  opacity: 0.8;
}

.order-time {
  color: var(--text-secondary);
  opacity: 0.6;
}

.order-status {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;
  white-space: nowrap;
}

.order-status.status-0 {
  background: rgba(245, 158, 11, 0.2);
  color: #fbbf24;
}

.order-status.status-1 {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

.order-status.status-2 {
  background: rgba(139, 92, 246, 0.2);
  color: #a78bfa;
}

.order-status.status-3 {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
}

.order-status.status-4 {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

.loading-hint,
.empty-hint {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
  opacity: 0.6;
  font-size: 14px;
}

/* 滚动条样式 */
.stream::-webkit-scrollbar {
  width: 6px;
}

.stream::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 3px;
}

.stream::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.stream::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.15);
}
</style>

