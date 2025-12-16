<template>
  <div class="p-6">
    <n-card title="今日菜单统计" class="mb-4">
      <n-space vertical :size="16">
        <!-- 搜索筛选 -->
        <n-space>
          <n-date-picker v-model:value="searchForm.dateRange" type="daterange" clearable />
          <n-select
            v-model:value="searchForm.mealPeriod"
            :options="mealPeriodOptions"
            placeholder="选择餐次"
            clearable
            style="width: 150px"
          />
          <n-select
            v-model:value="searchForm.status"
            :options="statusOptions"
            placeholder="选择状态"
            clearable
            style="width: 150px"
          />
          <n-button type="primary" @click="handleSearch">查询</n-button>
          <n-button @click="handleReset">重置</n-button>
        </n-space>

        <!-- 数据表格 -->
        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          @update:page="handlePageChange"
        />
      </n-space>
    </n-card>

    <!-- 详情弹窗 -->
    <n-modal v-model:show="showDetail" preset="card" title="大订单详情" style="width: 800px">
      <n-descriptions v-if="currentDetail" :column="2" bordered>
        <n-descriptions-item label="日期">{{ currentDetail.orderDate }}</n-descriptions-item>
        <n-descriptions-item label="餐次">{{ getMealPeriodLabel(currentDetail.mealPeriod) }}</n-descriptions-item>
        <n-descriptions-item label="参与人数">{{ currentDetail.memberCount }}</n-descriptions-item>
        <n-descriptions-item label="菜品数量">{{ currentDetail.dishCount }}</n-descriptions-item>
        <n-descriptions-item label="总金额">¥{{ currentDetail.totalAmount }}</n-descriptions-item>
        <n-descriptions-item label="状态">
          <n-tag :type="getStatusType(currentDetail.status)">
            {{ getStatusLabel(currentDetail.status) }}
          </n-tag>
        </n-descriptions-item>
      </n-descriptions>

      <n-divider />

      <div v-if="currentDetail.memberOrders && currentDetail.memberOrders.length > 0">
        <h4 class="mb-4">成员订单</h4>
        <n-list bordered>
          <n-list-item v-for="member in currentDetail.memberOrders" :key="member.orderId">
            <template #prefix>
              <n-avatar :src="member.avatar" round />
            </template>
            <n-thing :title="member.nickname" :description="`订单号: ${member.orderNumber}`">
              <template #footer>
                <n-space>
                  <n-tag v-for="item in member.items" :key="item.id" size="small">
                    {{ item.dishName }} x{{ item.quantity }}
                  </n-tag>
                </n-space>
              </template>
            </n-thing>
            <template #suffix>
              <n-text strong>¥{{ member.totalAmount }}</n-text>
            </template>
          </n-list-item>
        </n-list>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue';
import {
  NCard,
  NSpace,
  NDatePicker,
  NSelect,
  NButton,
  NDataTable,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NDivider,
  NList,
  NListItem,
  NAvatar,
  NThing,
  NTag,
  NText,
  useMessage,
  type DataTableColumns
} from 'naive-ui';
import http from '@/api/http';

const message = useMessage();

// 搜索表单
const searchForm = reactive({
  dateRange: null as [number, number] | null,
  mealPeriod: null,
  status: null
});

// 选项
const mealPeriodOptions = [
  { label: '早餐', value: 'BREAKFAST' },
  { label: '中餐', value: 'LUNCH' },
  { label: '晚餐', value: 'DINNER' }
];

const statusOptions = [
  { label: '收集中', value: 0 },
  { label: '已确认', value: 1 },
  { label: '已截止', value: 2 }
];

// 表格数据
const loading = ref(false);
const tableData = ref([]);
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
});

// 表格列
const columns: DataTableColumns = [
  { title: '日期', key: 'orderDate' },
  {
    title: '餐次',
    key: 'mealPeriod',
    render: (row: any) => getMealPeriodLabel(row.mealPeriod)
  },
  { title: '参与人数', key: 'memberCount' },
  { title: '菜品数量', key: 'dishCount' },
  {
    title: '总金额',
    key: 'totalAmount',
    render: (row: any) => `¥${row.totalAmount}`
  },
  {
    title: '状态',
    key: 'status',
    render: (row: any) =>
      h(
        NTag,
        { type: getStatusType(row.status) },
        { default: () => getStatusLabel(row.status) }
      )
  },
  {
    title: '操作',
    key: 'actions',
    render: (row: any) =>
      h(
        NButton,
        {
          size: 'small',
          onClick: () => handleViewDetail(row.id)
        },
        { default: () => '查看详情' }
      )
  }
];

// 详情
const showDetail = ref(false);
const currentDetail = ref<any>(null);

// 获取餐次标签
const getMealPeriodLabel = (period: string) => {
  const map: Record<string, string> = {
    BREAKFAST: '早餐',
    LUNCH: '中餐',
    DINNER: '晚餐'
  };
  return map[period] || period;
};

// 获取状态标签
const getStatusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: '收集中',
    1: '已确认',
    2: '已截止'
  };
  return map[status] || '未知';
};

// 获取状态类型
const getStatusType = (status: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'warning'
  };
  return map[status] || 'default';
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    };

    if (searchForm.dateRange) {
      params.startDate = new Date(searchForm.dateRange[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchForm.dateRange[1]).toISOString().split('T')[0];
    }
    if (searchForm.mealPeriod) params.mealPeriod = searchForm.mealPeriod;
    if (searchForm.status !== null) params.status = searchForm.status;

    const res = await http.get('/admin/daily-meal-order', { params });
    tableData.value = res.data.records || [];
    pagination.itemCount = res.data.total || 0;
  } catch (error) {
    console.error('加载数据失败:', error);
    message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 查看详情
const handleViewDetail = async (id: number) => {
  try {
    const res = await http.get(`/admin/daily-meal-order/${id}`);
    currentDetail.value = res.data;
    showDetail.value = true;
  } catch (error) {
    console.error('加载详情失败:', error);
    message.error('加载详情失败');
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadData();
};

// 重置
const handleReset = () => {
  searchForm.dateRange = null;
  searchForm.mealPeriod = null;
  searchForm.status = null;
  handleSearch();
};

// 分页
const handlePageChange = (page: number) => {
  pagination.page = page;
  loadData();
};

onMounted(() => {
  loadData();
});
</script>
