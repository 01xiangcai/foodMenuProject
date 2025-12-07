<template>
  <div class="wallet-container">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <h2>💰 钱包管理</h2>
        <div class="table-actions">
          <NInput
            v-model:value="filters.wxUserId"
            placeholder="搜索用户ID"
            clearable
            style="width: 200px"
            @keyup.enter="loadWallets"
          />
          <NButton class="primary-soft" type="primary" @click="loadWallets">
            搜索
          </NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="wallets"
        :loading="loading"
        :pagination="pagination"
        size="large"
      />
    </section>

    <!-- 充值弹窗 -->
    <NModal v-model:show="rechargeModal.show" preset="card" style="max-width: 450px">
      <template #header>💳 充值</template>
      <NForm :model="rechargeModal.form" label-placement="left" label-width="80" @submit.prevent>
        <NFormItem label="用户ID">
          <NInput :value="rechargeModal.form.wxUserId" disabled />
        </NFormItem>
        <NFormItem label="当前余额">
          <NText type="success" strong>¥{{ rechargeModal.form.currentBalance?.toFixed(2) || '0.00' }}</NText>
        </NFormItem>
        <NFormItem label="充值金额" required>
          <NInputNumber
            v-model:value="rechargeModal.form.amount"
            :min="0.01"
            :precision="2"
            placeholder="请输入充值金额"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="备注">
          <NInput
            v-model:value="rechargeModal.form.remark"
            type="textarea"
            placeholder="可选，充值备注说明"
            :rows="2"
          />
        </NFormItem>
      </NForm>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="rechargeModal.show = false">取消</NButton>
          <NButton
            class="primary-soft"
            type="primary"
            :loading="rechargeModal.loading"
            @click="handleRecharge"
          >
            确认充值
          </NButton>
        </div>
      </template>
    </NModal>

    <!-- 流水弹窗 -->
    <NModal v-model:show="transactionModal.show" preset="card" style="max-width: 950px; width: 95%">
      <template #header>📜 交易流水</template>
      <NDataTable
        :columns="transactionColumns"
        :data="transactionModal.list"
        :loading="transactionModal.loading"
        :pagination="transactionModal.pagination"
        :scroll-x="800"
        size="small"
      />
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NSpace,
  NTag,
  NText,
  type DataTableColumns,
  type PaginationProps,
  useMessage,
  useDialog,
} from 'naive-ui';
import {
  fetchWallets,
  rechargeWallet,
  fetchWalletTransactions,
  resetWalletPassword,
  type WalletInfo,
  type WalletTransaction,
} from '@/api/modules';

const message = useMessage();
const dialog = useDialog();

const wallets = ref<WalletInfo[]>([]);
const loading = ref(false);

const filters = reactive({
  wxUserId: '',
});

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showQuickJumper: true,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  onChange(page: number) {
    pagination.page = page;
    loadWallets();
  },
  onUpdatePageSize(size: number) {
    pagination.pageSize = size;
    pagination.page = 1;
    loadWallets();
  },
});

const rechargeModal = reactive({
  show: false,
  loading: false,
  form: {
    wxUserId: '',
    currentBalance: 0,
    amount: null as number | null,
    remark: '',
  },
});

const transactionModal = reactive({
  show: false,
  loading: false,
  wxUserId: '',
  list: [] as WalletTransaction[],
  pagination: {
    page: 1,
    pageSize: 10,
    itemCount: 0,
    onChange(page: number) {
      transactionModal.pagination.page = page;
      loadTransactions();
    },
  },
});

const columns: DataTableColumns<WalletInfo> = [
  {
    title: '用户ID',
    key: 'wxUserId',
    width: 100,
  },
  {
    title: '用户昵称',
    key: 'nickname',
    width: 120,
    render: (row) => row.nickname || '-',
  },
  {
    title: '手机号',
    key: 'phone',
    width: 120,
    render: (row) => row.phone || '-',
  },
  {
    title: '可用余额',
    key: 'balance',
    width: 120,
    render: (row) =>
      h(NText, { type: 'success', strong: true }, () => `¥${row.balance?.toFixed(2) || '0.00'}`),
  },
  {
    title: '冻结金额',
    key: 'frozenAmount',
    width: 120,
    render: (row) =>
      h(NText, { type: 'warning' }, () => `¥${row.frozenAmount?.toFixed(2) || '0.00'}`),
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            secondary: true,
            onClick: () => openRechargeModal(row),
          },
          () => '充值'
        ),
        h(
          NButton,
          {
            size: 'small',
            secondary: true,
            onClick: () => openTransactionModal(row),
          },
          () => '流水'
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'warning',
            secondary: true,
            onClick: () => handleResetPassword(row),
          },
          () => '重置支付密码'
        ),
      ]),
  },
];

const transactionColumns: DataTableColumns<WalletTransaction> = [
  {
    title: '交易类型',
    key: 'transType',
    width: 100,
    render: (row) => {
      const typeMap: Record<number, { label: string; type: 'success' | 'error' | 'warning' | 'info' }> = {
        1: { label: '充值', type: 'success' },
        2: { label: '消费', type: 'error' },
        3: { label: '冻结', type: 'warning' },
        4: { label: '退款', type: 'info' },
      };
      const info = typeMap[row.transType] || { label: '未知', type: 'warning' };
      return h(NTag, { type: info.type, size: 'small' }, () => info.label);
    },
  },
  {
    title: '变动金额',
    key: 'amount',
    width: 100,
    render: (row) => {
      const isPositive = row.amount >= 0;
      return h(
        NText,
        { type: isPositive ? 'success' : 'error', strong: true },
        () => `${isPositive ? '+' : ''}${row.amount?.toFixed(2)}`
      );
    },
  },
  {
    title: '余额',
    key: 'balanceAfter',
    width: 100,
    render: (row) => `¥${row.balanceAfter?.toFixed(2) || '0.00'}`,
  },
  {
    title: '关联单号',
    key: 'relatedOrderNo',
    width: 120,
    render: (row) => row.relatedOrderNo || '-',
  },
  {
    title: '备注',
    key: 'remark',
    width: 120,
    ellipsis: { tooltip: true },
  },
  {
    title: '时间',
    key: 'createTime',
    width: 160,
  },
];

async function loadWallets() {
  loading.value = true;
  try {
    const res = await fetchWallets({
      page: pagination.page as number,
      pageSize: pagination.pageSize as number,
      wxUserId: filters.wxUserId || undefined,
    });
    if (res.data?.records) {
      wallets.value = res.data.records;
      pagination.itemCount = res.data.total;
    }
  } catch (e) {
    message.error('加载钱包列表失败');
  } finally {
    loading.value = false;
  }
}

function openRechargeModal(wallet: WalletInfo) {
  rechargeModal.form = {
    wxUserId: wallet.wxUserId,
    currentBalance: wallet.balance || 0,
    amount: null,
    remark: '',
  };
  rechargeModal.show = true;
}

async function handleRecharge() {
  if (!rechargeModal.form.amount || rechargeModal.form.amount <= 0) {
    message.warning('请输入有效的充值金额');
    return;
  }

  rechargeModal.loading = true;
  try {
    await rechargeWallet({
      wxUserId: rechargeModal.form.wxUserId,
      amount: rechargeModal.form.amount,
      remark: rechargeModal.form.remark || undefined,
    });
    message.success('充值成功');
    rechargeModal.show = false;
    loadWallets();
  } catch (e: any) {
    message.error(e.response?.data?.msg || '充值失败');
  } finally {
    rechargeModal.loading = false;
  }
}

function openTransactionModal(wallet: WalletInfo) {
  transactionModal.wxUserId = wallet.wxUserId;
  transactionModal.pagination.page = 1;
  transactionModal.list = [];
  transactionModal.show = true;
  loadTransactions();
}

async function loadTransactions() {
  transactionModal.loading = true;
  try {
    const res = await fetchWalletTransactions({
      wxUserId: transactionModal.wxUserId,
      page: transactionModal.pagination.page,
      pageSize: transactionModal.pagination.pageSize,
    });
    if (res.data?.records) {
      transactionModal.list = res.data.records;
      transactionModal.pagination.itemCount = res.data.total;
    }
  } catch (e) {
    message.error('加载流水失败');
  } finally {
    transactionModal.loading = false;
  }
}

async function handleResetPassword(wallet: WalletInfo) {
  dialog.warning({
    title: '确认重置',
    content: `确定要重置用户 ${wallet.nickname || wallet.wxUserId} 的支付密码吗？重置后用户需要重新设置密码。`,
    positiveText: '确定重置',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await resetWalletPassword(wallet.wxUserId);
        message.success('密码重置成功');
        loadWallets();
      } catch (e: any) {
        message.error(e.response?.data?.msg || '重置失败');
      }
    },
  });
}

onMounted(() => {
  loadWallets();
});
</script>

<style scoped>
.wallet-container {
  padding: 20px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
}

.hover-rise {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-rise:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.table-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-soft {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}
</style>
