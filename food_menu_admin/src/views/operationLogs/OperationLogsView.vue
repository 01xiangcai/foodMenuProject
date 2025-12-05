<template>
  <div class="operation-logs-container">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <h2>📋 操作日志</h2>
        <div class="table-actions">
          <!-- 超级管理员显示家庭筛选器 -->
          <NSelect
            v-if="isSuperAdmin"
            v-model:value="filters.familyId"
            :options="familyFilterOptions"
            placeholder="选择家庭"
            clearable
            style="width: 180px"
            @update:value="refreshLogs"
          />
          <!-- 操作类型筛选 -->
          <NSelect
            v-model:value="filters.operationType"
            :options="operationTypeOptions"
            placeholder="操作类型"
            clearable
            style="width: 150px"
            @update:value="refreshLogs"
          />
          <!-- 操作模块筛选 -->
          <NSelect
            v-model:value="filters.operationModule"
            :options="operationModuleOptions"
            placeholder="操作模块"
            clearable
            style="width: 150px"
            @update:value="refreshLogs"
          />
          <!-- 状态筛选 -->
          <NSelect
            v-model:value="filters.status"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="width: 120px"
            @update:value="refreshLogs"
          />
          <!-- 时间范围 -->
          <NDatePicker
            v-model:value="dateRange"
            type="datetimerange"
            clearable
            placeholder="时间范围"
            style="width: 300px"
            @update:value="handleDateRangeChange"
          />
          <!-- 搜索 -->
          <NInput
            v-model:value="filters.operatorName"
            clearable
            placeholder="操作人姓名"
            style="width: 150px"
            @keydown.enter.prevent="refreshLogs"
          />
          <NButton secondary @click="refreshLogs">刷新</NButton>
          <NButton
            v-if="isSuperAdmin"
            type="error"
            secondary
            @click="showBatchDeleteModal = true"
          >
            批量删除
          </NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="logs"
        :loading="loading"
        :pagination="pagination"
        size="large"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </section>

    <!-- 日志详情 Modal -->
    <NModal v-model:show="detailModal.show" preset="card" style="max-width: 900px">
      <template #header>
        <span>操作日志详情</span>
      </template>
      <div v-if="detailModal.data" class="log-detail">
        <NDescriptions :column="2" bordered>
          <NDescriptionsItem label="操作类型">
            <NTag :type="getOperationTypeTagType(detailModal.data.operationType)">
              {{ detailModal.data.operationType }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="操作模块">
            {{ detailModal.data.operationModule }}
          </NDescriptionsItem>
          <NDescriptionsItem label="操作描述">
            {{ detailModal.data.operationDesc }}
          </NDescriptionsItem>
          <NDescriptionsItem label="状态">
            <NTag :type="detailModal.data.status === 1 ? 'success' : 'error'">
              {{ detailModal.data.status === 1 ? '成功' : '失败' }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="操作人">
            {{ detailModal.data.operatorName }} ({{ detailModal.data.operatorType }})
          </NDescriptionsItem>
          <NDescriptionsItem label="操作人ID">
            {{ detailModal.data.operatorId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="IP地址">
            {{ detailModal.data.ipAddress }}
          </NDescriptionsItem>
          <NDescriptionsItem label="执行时长">
            {{ detailModal.data.executionTime }}ms
          </NDescriptionsItem>
          <NDescriptionsItem label="请求方式">
            {{ detailModal.data.requestMethod }}
          </NDescriptionsItem>
          <NDescriptionsItem label="请求URL">
            {{ detailModal.data.requestUrl }}
          </NDescriptionsItem>
          <NDescriptionsItem label="方法名" :span="2">
            <code class="text-xs">{{ detailModal.data.methodName }}</code>
          </NDescriptionsItem>
          <NDescriptionsItem label="创建时间" :span="2">
            {{ formatDateTime(detailModal.data.createTime) }}
          </NDescriptionsItem>
          <NDescriptionsItem v-if="detailModal.data.requestParams" label="请求参数" :span="2">
            <NCode :code="formatJson(detailModal.data.requestParams)" language="json" />
          </NDescriptionsItem>
          <NDescriptionsItem v-if="detailModal.data.responseResult" label="响应结果" :span="2">
            <NCode :code="formatJson(detailModal.data.responseResult)" language="json" />
          </NDescriptionsItem>
          <NDescriptionsItem v-if="detailModal.data.errorMsg" label="错误信息" :span="2">
            <NAlert type="error" :title="detailModal.data.errorMsg" />
          </NDescriptionsItem>
        </NDescriptions>
      </div>
    </NModal>

    <!-- 批量删除 Modal -->
    <NModal v-model:show="showBatchDeleteModal" preset="card" style="max-width: 500px">
      <template #header>
        <span>批量删除操作日志</span>
      </template>
      <NForm :model="batchDeleteForm" label-placement="left" label-width="120">
        <NFormItem label="删除时间之前" required>
          <NDatePicker
            v-model:value="batchDeleteForm.endTime"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </NFormItem>
        <NAlert type="warning" style="margin-top: 16px">
          此操作将删除指定时间之前的所有操作日志（根据您的权限自动过滤），删除后无法恢复，请谨慎操作！
        </NAlert>
      </NForm>
      <template #action>
        <div class="flex justify-end gap-2">
          <NButton @click="showBatchDeleteModal = false">取消</NButton>
          <NButton type="error" @click="handleBatchDelete">确认删除</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue';
import {
  NDataTable,
  NButton,
  NSelect,
  NInput,
  NDatePicker,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NTag,
  NCode,
  NAlert,
  NForm,
  NFormItem,
  useMessage,
  type DataTableColumns,
  type SelectOption
} from 'naive-ui';
import { fetchOperationLogs, fetchOperationLogDetail, deleteOperationLog, batchDeleteOperationLogs, fetchAllFamilies } from '@/api/modules';
import type { OperationLog, OperationLogQuery } from '@/api/modules';
import { useUserStore } from '@/store/useUserStore';
import { storeToRefs } from 'pinia';

const message = useMessage();
const userStore = useUserStore();
const { profile } = storeToRefs(userStore);

// 判断是否为超级管理员
const isSuperAdmin = computed(() => profile.value?.role === 2);

// 数据
const loading = ref(false);
const logs = ref<OperationLog[]>([]);
const families = ref<SelectOption[]>([]);

// 筛选条件
const filters = ref<OperationLogQuery>({
  page: 1,
  pageSize: 10,
  operationType: undefined,
  operationModule: undefined,
  status: undefined,
  familyId: null,
  operatorName: undefined
});

const dateRange = ref<[number, number] | null>(null);

// 分页
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    filters.value.page = page;
    refreshLogs();
  },
  onUpdatePageSize: (pageSize: number) => {
    filters.value.pageSize = pageSize;
    filters.value.page = 1;
    refreshLogs();
  }
});

// 详情 Modal
const detailModal = ref({
  show: false,
  data: null as OperationLog | null
});

// 批量删除 Modal
const showBatchDeleteModal = ref(false);
const batchDeleteForm = ref({
  endTime: null as number | null
});

// 选项
const operationTypeOptions: SelectOption[] = [
  { label: '新增', value: 'INSERT' },
  { label: '修改', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
  { label: '查询', value: 'QUERY' },
  { label: '登录', value: 'LOGIN' },
  { label: '登出', value: 'LOGOUT' },
  { label: '导出', value: 'EXPORT' },
  { label: '导入', value: 'IMPORT' },
  { label: '审核', value: 'AUDIT' },
  { label: '授权', value: 'GRANT' },
  { label: '其他', value: 'OTHER' }
];

const operationModuleOptions: SelectOption[] = [
  { label: '订单', value: '订单' },
  { label: '菜品', value: '菜品' },
  { label: '用户', value: '用户' },
  { label: '分类', value: '分类' },
  { label: '轮播图', value: '轮播图' },
  { label: '标签', value: '标签' },
  { label: '家庭', value: '家庭' }
];

const statusOptions: SelectOption[] = [
  { label: '成功', value: 1 },
  { label: '失败', value: 0 }
];

const familyFilterOptions = computed(() => [
  { label: '全部家庭', value: null },
  ...families.value
]);

// 表格列
const columns: DataTableColumns<OperationLog> = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '操作类型',
    key: 'operationType',
    width: 100,
    render: (row) => {
      return h(NTag, { type: getOperationTypeTagType(row.operationType) }, { default: () => row.operationType });
    }
  },
  {
    title: '操作模块',
    key: 'operationModule',
    width: 100
  },
  {
    title: '操作描述',
    key: 'operationDesc',
    width: 150,
    ellipsis: { tooltip: true }
  },
  {
    title: '操作人',
    key: 'operatorName',
    width: 120,
    render: (row) => `${row.operatorName} (${row.operatorType})`
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error' }, {
        default: () => row.status === 1 ? '成功' : '失败'
      });
    }
  },
  {
    title: 'IP地址',
    key: 'ipAddress',
    width: 130
  },
  {
    title: '执行时长',
    key: 'executionTime',
    width: 100,
    render: (row) => `${row.executionTime}ms`
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: (row) => formatDateTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => {
      return h('div', { class: 'flex gap-2' }, [
        h(NButton, {
          size: 'small',
          secondary: true,
          onClick: () => viewDetail(row.id)
        }, { default: () => '详情' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          secondary: true,
          onClick: () => handleDelete(row.id)
        }, { default: () => '删除' })
      ]);
    }
  }
];

// 方法
const loadFamilies = async () => {
  if (!isSuperAdmin.value) return;
  try {
    const result = await fetchAllFamilies();
    families.value = result.data.map((f: any) => ({
      label: f.name,
      value: f.id
    }));
  } catch (error) {
    console.error('加载家庭列表失败:', error);
  }
};

const refreshLogs = async () => {
  loading.value = true;
  try {
    // 处理时间范围
    if (dateRange.value) {
      filters.value.startTime = formatDate(dateRange.value[0]);
      filters.value.endTime = formatDate(dateRange.value[1]);
    } else {
      filters.value.startTime = undefined;
      filters.value.endTime = undefined;
    }

    const result = await fetchOperationLogs(filters.value);
    logs.value = result.data.records || [];
    pagination.value.itemCount = result.data.total || 0;
    pagination.value.page = result.data.current || 1;
    pagination.value.pageSize = result.data.size || 10;
  } catch (error: any) {
    message.error(error.message || '加载操作日志失败');
  } finally {
    loading.value = false;
  }
};

const viewDetail = async (id: number) => {
  try {
    const result = await fetchOperationLogDetail(id);
    detailModal.value.data = result.data;
    detailModal.value.show = true;
  } catch (error: any) {
    message.error(error.message || '加载日志详情失败');
  }
};

const handleDelete = async (id: number) => {
  try {
    await deleteOperationLog(id);
    message.success('删除成功');
    refreshLogs();
  } catch (error: any) {
    message.error(error.message || '删除失败');
  }
};

const handleBatchDelete = async () => {
  if (!batchDeleteForm.value.endTime) {
    message.warning('请选择截止时间');
    return;
  }

  try {
    const endTimeStr = formatDate(batchDeleteForm.value.endTime);
    await batchDeleteOperationLogs(endTimeStr);
    message.success('批量删除成功');
    showBatchDeleteModal.value = false;
    batchDeleteForm.value.endTime = null;
    refreshLogs();
  } catch (error: any) {
    message.error(error.message || '批量删除失败');
  }
};

const handlePageChange = (page: number) => {
  filters.value.page = page;
  refreshLogs();
};

const handlePageSizeChange = (pageSize: number) => {
  filters.value.pageSize = pageSize;
  filters.value.page = 1;
  refreshLogs();
};

const handleDateRangeChange = () => {
  refreshLogs();
};

const getOperationTypeTagType = (type: string): 'default' | 'success' | 'warning' | 'error' | 'info' => {
  const typeMap: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
    INSERT: 'success',
    UPDATE: 'info',
    DELETE: 'error',
    QUERY: 'default',
    LOGIN: 'success',
    LOGOUT: 'warning',
    EXPORT: 'info',
    IMPORT: 'info',
    AUDIT: 'warning',
    GRANT: 'success',
    OTHER: 'default'
  };
  return typeMap[type] || 'default';
};

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

const formatDate = (timestamp: number) => {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

const formatJson = (jsonStr: string) => {
  try {
    const obj = JSON.parse(jsonStr);
    return JSON.stringify(obj, null, 2);
  } catch {
    return jsonStr;
  }
};

// 初始化
onMounted(() => {
  loadFamilies();
  refreshLogs();
});
</script>

<style scoped lang="scss">
.operation-logs-container {
  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
    }

    .table-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
  }

  .log-detail {
    :deep(.n-code) {
      max-height: 300px;
      overflow: auto;
    }
  }
}
</style>

