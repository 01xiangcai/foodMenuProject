<template>
  <div class="orders-page glass-card hover-rise">
    <div class="header">
      <div>
        <h2>家庭订单记录</h2>
        <p>最近 {{ filteredOrders.length }} 条 · 一键查看菜品明细</p>
      </div>
      <div class="controls">
        <NSelect v-model:value="statusFilter" :options="statusOptions" size="small" class="status-select" />
        <NButton secondary size="small" @click="loadOrders">刷新</NButton>
      </div>
    </div>
    <NDataTable :columns="columns" :data="filteredOrders" :loading="ordersLoading" striped :pagination="false" />
    <NDrawer v-model:show="detailDrawer.show" width="360">
      <NDrawerContent title="订单详情">
        <div v-if="detailDrawer.loading" class="drawer-loading">加载中...</div>
        <div v-else-if="detailDrawer.data" class="drawer-body">
          <p class="order-number">订单号：{{ detailDrawer.data.number }}</p>
          <p>下单时间：{{ formatTime(detailDrawer.data.createTime) }}</p>
          <p>联系人：{{ detailDrawer.data.consignee }} {{ detailDrawer.data.phone }}</p>
          <p>地址：{{ detailDrawer.data.address || '—' }}</p>
          <p>备注：{{ detailDrawer.data.remark || '无' }}</p>
          <h4>菜品明细</h4>
          <ul class="dish-list">
            <li v-for="item in detailDrawer.data.orderDetails || []" :key="item.id ?? item.name">
              <div>
                <strong>{{ item.name }}</strong>
                <span class="muted"> × {{ item.number }}</span>
              </div>
              <span>¥{{ Number(item.amount).toFixed(2) }}</span>
            </li>
          </ul>
        </div>
        <div v-else class="drawer-loading">暂无数据</div>
      </NDrawerContent>
    </NDrawer>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import {
  NButton,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NSelect,
  NTag,
  useMessage,
  type DataTableColumns
} from 'naive-ui';
import { fetchOrderDetail, fetchOrders, updateOrderStatus } from '@/api/modules';

type OrderRecord = {
  id: number;
  number: string;
  status: number;
  amount: number | string;
  consignee?: string;
  phone?: string;
  address?: string;
  remark?: string;
  createTime?: string;
};

type OrderDetailItem = {
  id: number;
  name: string;
  number: number;
  amount: number;
};

type OrderDetailDto = OrderRecord & {
  orderDetails: OrderDetailItem[];
};

const statusMap: Record<
  number,
  {
    label: string;
    type: 'default' | 'info' | 'success' | 'warning' | 'error';
  }
> = {
  1: { label: '待支付', type: 'warning' },
  2: { label: '已支付', type: 'info' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'error' }
};

const statusOptions = [
  { label: '全部状态', value: 'all' },
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
];

const orders = ref<OrderRecord[]>([]);
const ordersLoading = ref(false);
const statusFilter = ref<'all' | number>('all');
const message = useMessage();

const detailDrawer = reactive({
  show: false,
  loading: false,
  data: null as OrderDetailDto | null
});

const filteredOrders = computed(() =>
  statusFilter.value === 'all'
    ? orders.value
    : orders.value.filter((order) => order.status === statusFilter.value)
);

const columns: DataTableColumns<OrderRecord> = [
  { title: '订单号', key: 'number', ellipsis: { tooltip: true } },
  {
    title: '金额',
    key: 'amount',
    render: (row) => `¥${Number(row.amount || 0).toFixed(2)}`
  },
  {
    title: '状态',
    key: 'status',
    render: (row) => {
      const info = statusMap[row.status] || { label: '未知', type: 'default' };
      return h(
        NTag,
        { bordered: false, type: info.type },
        { default: () => info.label }
      );
    }
  },
  {
    title: '联系人',
    key: 'consignee',
    render: (row) => `${row.consignee || '—'} / ${row.phone || '--'}`
  },
  {
    title: '地址',
    key: 'address',
    ellipsis: { tooltip: true },
    render: (row) => row.address || '—'
  },
  {
    title: '下单时间',
    key: 'createTime',
    render: (row) => formatTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: (row) =>
      h(
        'div',
        { class: 'actions' },
        [
          h(
            NButton,
            { size: 'small', tertiary: true, onClick: () => openDetail(row.id) },
            { default: () => '详情' }
          ),
          h(
            NButton,
            {
              size: 'small',
              tertiary: true,
              type: 'success',
              disabled: row.status === 3,
              onClick: () => changeStatus(row, 3)
            },
            { default: () => '完结' }
          ),
          h(
            NButton,
            {
              size: 'small',
              tertiary: true,
              type: 'error',
              disabled: row.status === 4,
              onClick: () => changeStatus(row, 4, '确定取消这笔订单吗？')
            },
            { default: () => '取消' }
          )
        ]
      )
  }
];

const loadOrders = async () => {
  ordersLoading.value = true;
  try {
    const result = await fetchOrders({ page: 1, pageSize: 50 });
    orders.value = result.data?.records || [];
  } finally {
    ordersLoading.value = false;
  }
};

const changeStatus = async (order: OrderRecord, status: number, confirmText?: string) => {
  if (confirmText && !window.confirm(confirmText)) {
    return;
  }
  await updateOrderStatus(order.id, status);
  message.success(status === 3 ? '已标记为完成' : '订单已取消');
  await loadOrders();
};

const openDetail = async (id: number) => {
  detailDrawer.show = true;
  detailDrawer.loading = true;
  detailDrawer.data = null;
  try {
    const result = await fetchOrderDetail(id);
    detailDrawer.data = result.data;
  } finally {
    detailDrawer.loading = false;
  }
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

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.orders-page {
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-select {
  min-width: 140px;
}

.actions {
  display: flex;
  gap: 8px;
}

.drawer-loading {
  padding: 16px 0;
  text-align: center;
  opacity: 0.7;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-number {
  font-weight: 600;
}

.dish-list {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dish-list li {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
  padding-bottom: 4px;
}

.muted {
  opacity: 0.7;
  font-size: 13px;
  margin-left: 4px;
}
</style>

