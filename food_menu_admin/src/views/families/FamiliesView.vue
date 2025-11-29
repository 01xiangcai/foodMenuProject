<template>
  <div class="families-container">
    <!-- 页面头部 -->
    <div class="page-header glass-card hover-rise">
      <div class="header-content">
        <div class="header-left">
          <div class="icon-wrapper">
            <div class="icon-bg">🏠</div>
          </div>
          <div>
            <h1>家庭管理</h1>
            <p class="subtitle">管理系统中的所有家庭，分配邀请码</p>
          </div>
        </div>
        <div class="header-actions">
          <NButton type="primary" size="large" class="primary-soft" @click="openFamilyModal()">
            <template #icon>
              <span class="btn-icon">✨</span>
            </template>
            创建家庭
          </NButton>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card glass-card hover-rise">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <span>👨‍👩‍👧‍👦</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ pagination.itemCount || 0 }}</div>
          <div class="stat-label">总家庭数</div>
        </div>
      </div>
      
      <div class="stat-card glass-card hover-rise">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <span>✅</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ activeCount }}</div>
          <div class="stat-label">活跃家庭</div>
        </div>
      </div>
      
      <div class="stat-card glass-card hover-rise">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <span>🎫</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ families.length }}</div>
          <div class="stat-label">当前页数量</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="glass-card hover-rise search-section">
      <div class="search-content">
        <NInput
          v-model:value="searchQuery.name"
          clearable
          placeholder="🔍 搜索家庭名称..."
          size="large"
          class="search-input"
          @keydown.enter="refreshFamilies"
        />
        <NSelect
          v-model:value="searchQuery.status"
          :options="statusOptions"
          placeholder="筛选状态"
          clearable
          size="large"
          style="width: 160px"
        />
        <NButton secondary size="large" @click="refreshFamilies">
          <template #icon><span>🔄</span></template>
          刷新
        </NButton>
      </div>
    </div>

    <!-- 家庭列表 -->
    <div class="glass-card families-table">
      <NDataTable
        :columns="columns"
        :data="families"
        :loading="loading"
        :pagination="pagination"
        :remote="true"
        size="large"
        :row-class-name="() => 'table-row'"
      />
    </div>

    <!-- 创建/编辑弹窗 -->
    <NModal
      v-model:show="familyModal.show"
      preset="card"
      :title="familyModal.form.id ? '编辑家庭' : '创建新家庭'"
      style="max-width: 520px"
      :mask-closable="false"
      class="family-modal"
    >
      <NForm :model="familyModal.form" label-placement="top" @submit.prevent>
        <NFormItem label="家庭名称" required>
          <NInput
            v-model:value="familyModal.form.name"
            placeholder="例如：姚家大院"
            size="large"
            maxlength="50"
            show-count
          />
        </NFormItem>
        
        <NFormItem label="家庭描述">
          <NInput
            v-model:value="familyModal.form.description"
            type="textarea"
            placeholder="简单描述这个家庭..."
            :rows="3"
            maxlength="200"
            show-count
          />
        </NFormItem>
        
        <NFormItem label="状态">
          <NSwitch
            v-model:value="familyModal.form.statusBool"
            size="large"
          >
            <template #checked>✅ 启用</template>
            <template #unchecked>⛔ 禁用</template>
          </NSwitch>
        </NFormItem>
        
        <div v-if="familyModal.form.inviteCode" class="invite-code-section">
          <div class="invite-code-label">邀请码</div>
          <div class="invite-code-display">
            <div class="code-box">{{ familyModal.form.inviteCode }}</div>
            <NButton text @click="() => copyInviteCode()">
              <template #icon><span>📋</span></template>
              复制
            </NButton>
          </div>
        </div>
      </NForm>
      
      <template #action>
        <div class="modal-actions">
          <NButton quaternary size="large" @click="familyModal.show = false">取消</NButton>
          <NButton
            type="primary"
            size="large"
            class="primary-soft"
            :loading="familyModal.loading"
            @click="saveFamily"
          >
            <template #icon><span>💾</span></template>
            保存
          </NButton>
        </div>
      </template>
    </NModal>

    <!-- 详情弹窗 -->
    <NModal
      v-model:show="detailModal.show"
      preset="card"
      title="家庭详情"
      style="max-width: 480px"
      class="detail-modal"
    >
      <div v-if="detailModal.data" class="family-detail">
        <div class="detail-header">
          <div class="family-avatar">🏠</div>
          <div class="family-info">
            <h2>{{ detailModal.data.name }}</h2>
            <NTag :type="detailModal.data.status === 1 ? 'success' : 'error'" size="small">
              {{ detailModal.data.status === 1 ? '✅ 启用' : '⛔ 禁用' }}
            </NTag>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-label">📝 家庭描述</div>
          <div class="section-text">{{ detailModal.data.description || '暂无描述' }}</div>
        </div>
        
        <div class="detail-section">
          <div class="section-label">🎫 邀请码</div>
          <div class="invite-code-display">
            <div class="code-box">{{ detailModal.data.inviteCode }}</div>
            <NButton text @click="copyInviteCode(detailModal.data.inviteCode)">
              <template #icon><span>📋</span></template>
              复制
            </NButton>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-label">⏰ 创建时间</div>
          <div class="section-text">{{ formatDate(detailModal.data.createTime) }}</div>
        </div>
        
        <div class="detail-section">
          <div class="section-label">🔄 更新时间</div>
          <div class="section-text">{{ formatDate(detailModal.data.updateTime) }}</div>
        </div>
      </div>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  useMessage,
  type DataTableColumns,
  type PaginationProps
} from 'naive-ui';
import {
  createFamily,
  deleteFamily,
  fetchFamilies,
  fetchFamilyDetail,
  updateFamily,
  type FamilyPayload,
  type FamilyQuery
} from '@/api/modules';

type FamilyRecord = {
  id: number;
  name: string;
  description?: string;
  inviteCode: string;
  status: number;
  createTime: string;
  updateTime: string;
};

const message = useMessage();

const families = ref<FamilyRecord[]>([]);
const loading = ref(false);

const searchQuery = reactive<Partial<FamilyQuery>>({
  name: '',
  status: undefined
});

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showQuickJumper: true,
  showSizePicker: true,
  pageSizes: [5, 10, 20, 50],
  prefix: ({ itemCount }) => `共 ${itemCount ?? 0} 个家庭`,
  onChange: (page: number) => {
    pagination.page = page;
    loadFamilies();
  },
  onUpdatePageSize: (size: number) => {
    pagination.pageSize = size;
    pagination.page = 1;
    loadFamilies();
  }
});

const statusOptions = [
  { label: '✅ 启用', value: 1 },
  { label: '⛔ 禁用', value: 0 }
];

const activeCount = computed(() => {
  return families.value.filter(f => f.status === 1).length;
});

const familyModal = reactive({
  show: false,
  loading: false,
  form: {
    id: undefined as number | undefined,
    name: '',
    description: '',
    statusBool: true,
    inviteCode: ''
  }
});

const detailModal = reactive({
  show: false,
  data: null as FamilyRecord | null
});

const columns: DataTableColumns<FamilyRecord> = [
  {
    title: 'ID',
    key: 'id',
    width: 80,
    align: 'center'
  },
  {
    title: '家庭名称',
    key: 'name',
    ellipsis: { tooltip: true },
    render: (row) => h(
      'div',
      { class: 'family-name-cell' },
      [
        h('div', { class: 'family-avatar-small' }, '🏠'),
        h('span', row.name)
      ]
    )
  },
  {
    title: '邀请码',
    key: 'inviteCode',
    width: 140,
    render: (row) => h(
      'div',
      { class: 'invite-code-cell' },
      row.inviteCode
    )
  },
  {
    title: '描述',
    key: 'description',
    ellipsis: { tooltip: true },
    render: (row) => row.description || '—'
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    align: 'center',
    render: (row) => h(
      NTag,
      {
        type: row.status === 1 ? 'success' : 'error',
        bordered: false,
        size: 'small'
      },
      { default: () => row.status === 1 ? '✅ 启用' : '⛔ 禁用' }
    )
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: (row) => formatDate(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    align: 'center',
    render: (row) => h(
      NSpace,
      { size: 8 },
      {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              tertiary: true,
              type: 'info',
              onClick: () => openDetailModal(row)
            },
            { default: () => '详情' }
          ),
          h(
            NButton,
            {
              size: 'small',
              tertiary: true,
              onClick: () => openFamilyModal(row.id)
            },
            { default: () => '编辑' }
          ),
          h(
            NButton,
            {
              size: 'small',
              tertiary: true,
              type: 'error',
              onClick: () => handleDeleteFamily(row.id)
            },
            { default: () => '删除' }
          )
        ]
      }
    )
  }
];

const loadFamilies = async () => {
  loading.value = true;
  try {
    const params: FamilyQuery = {
      page: pagination.page!,
      pageSize: pagination.pageSize!,
      ...searchQuery
    };
    const result = await fetchFamilies(params);
    families.value = result.data.records || [];
    pagination.itemCount = result.data.total || 0;
  } catch (error) {
    message.error('加载家庭列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const refreshFamilies = () => {
  pagination.page = 1;
  loadFamilies();
};

const resetFamilyForm = () => {
  familyModal.form.id = undefined;
  familyModal.form.name = '';
  familyModal.form.description = '';
  familyModal.form.statusBool = true;
  familyModal.form.inviteCode = '';
};

const openFamilyModal = async (id?: number) => {
  familyModal.show = true;
  resetFamilyForm();
  
  if (id) {
    try {
      const result = await fetchFamilyDetail(id);
      const family = result.data;
      familyModal.form.id = family.id;
      familyModal.form.name = family.name;
      familyModal.form.description = family.description || '';
      familyModal.form.statusBool = family.status === 1;
      familyModal.form.inviteCode = family.inviteCode;
    } catch (error) {
      message.error('加载家庭详情失败');
      console.error(error);
    }
  }
};

const openDetailModal = (family: FamilyRecord) => {
  detailModal.data = family;
  detailModal.show = true;
};

const saveFamily = async () => {
  if (!familyModal.form.name.trim()) {
    message.warning('请输入家庭名称');
    return;
  }
  
  familyModal.loading = true;
  try {
    const payload: FamilyPayload = {
      id: familyModal.form.id,
      name: familyModal.form.name.trim(),
      description: familyModal.form.description.trim() || undefined,
      status: familyModal.form.statusBool ? 1 : 0
    };
    
    if (payload.id) {
      await updateFamily(payload);
      message.success('✅ 家庭信息已更新');
    } else {
      await createFamily(payload);
      message.success('🎉 家庭创建成功！');
    }
    
    familyModal.show = false;
    await loadFamilies();
  } catch (error: any) {
    message.error(error.message || '操作失败');
    console.error(error);
  } finally {
    familyModal.loading = false;
  }
};

const handleDeleteFamily = async (id: number) => {
  if (!window.confirm('确定要删除这个家庭吗？此操作不可恢复！')) {
    return;
  }
  
  try {
    await deleteFamily(id);
    message.success('✅ 家庭已删除');
    await loadFamilies();
  } catch (error: any) {
    message.error(error.message || '删除失败');
    console.error(error);
  }
};

const copyInviteCode = (code?: string) => {
  const textToCopy = code || familyModal.form.inviteCode;
  navigator.clipboard.writeText(textToCopy).then(() => {
    message.success('📋 邀请码已复制到剪贴板');
  }).catch(() => {
    message.error('复制失败');
  });
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '—';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  loadFamilies();
});
</script>

<style scoped lang="scss">
.families-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  padding: 32px;
  margin-bottom: 24px;
  border-radius: 20px;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-primary);
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 24px;
  }
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .icon-wrapper {
    .icon-bg {
      width: 64px;
      height: 64px;
      border-radius: 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
      animation: float 3s ease-in-out infinite;
    }
  }
  
  h1 {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 4px 0;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-hover) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .subtitle {
    margin: 0;
    opacity: 0.7;
    font-size: 14px;
  }
  
  .btn-icon {
    font-size: 18px;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 24px;
  border-radius: 16px;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-primary);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    flex-shrink: 0;
  }
  
  .stat-content {
    flex: 1;
  }
  
  .stat-value {
    font-size: 32px;
    font-weight: 700;
    line-height: 1;
    margin-bottom: 4px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-hover) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .stat-label {
    font-size: 13px;
    opacity: 0.7;
    font-weight: 500;
  }
}

.search-section {
  padding: 20px;
  margin-bottom: 24px;
  border-radius: 16px;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-primary);
  
  .search-content {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .search-input {
    flex: 1;
    max-width: 400px;
  }
}

.families-table {
  padding: 24px;
  border-radius: 16px;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-primary);
}

:deep(.family-name-cell) {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .family-avatar-small {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 16px;
    flex-shrink: 0;
  }
}

.invite-code-cell {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  padding: 6px 12px;
  background: var(--primary-color-suppl);
  border-radius: 8px;
  display: inline-block;
  letter-spacing: 1px;
}

.invite-code-section {
  margin-top: 20px;
  padding: 16px;
  background: var(--card-bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-secondary);
}

.invite-code-label {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  opacity: 0.8;
}

.invite-code-display {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .code-box {
    flex: 1;
    padding: 12px 16px;
    background: var(--primary-color-suppl);
    border-radius: 10px;
    font-family: 'Courier New', monospace;
    font-weight: 700;
    font-size: 18px;
    letter-spacing: 2px;
    text-align: center;
    border: 2px dashed var(--primary-color);
  }
}

.family-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--border-secondary);
    
    .family-avatar {
      width: 72px;
      height: 72px;
      border-radius: 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 32px;
      box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
    }
    
    .family-info {
      flex: 1;
      
      h2 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 700;
      }
    }
  }
  
  .detail-section {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .section-label {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
    opacity: 0.7;
  }
  
  .section-text {
    font-size: 15px;
    line-height: 1.6;
  }
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.glass-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.hover-rise:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

:deep(.table-row) {
  transition: all 0.2s ease;
  
  &:hover {
    background: var(--hover-bg) !important;
  }
}
</style>
