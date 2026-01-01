<template>
  <div class="tags-view">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <div>
          <h2>标签管理</h2>
          <p>管理菜品标签和图标配置</p>
        </div>
        <div class="table-actions">
          <NSelect
            v-if="isSuperAdmin"
            v-model:value="selectedFamilyId"
            :options="familyFilterOptions"
            placeholder="选择家庭"
            clearable
            style="width: 180px"
            @update:value="handleFamilyChange"
          />
          <NInput
            v-model:value="searchName"
            clearable
            placeholder="搜索标签名称"
            style="width: 200px"
            @input="handleSearch"
          />
          <NButton secondary @click="loadTags">刷新</NButton>
          <NButton class="primary-soft" type="primary" @click="openTagModal()">新增标签</NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="tags"
        :loading="loading"
        :pagination="pagination"
        :remote="true"
        size="large"
      />
    </section>

    <NModal v-model:show="tagModal.show" preset="card" :mask-closable="false" style="max-width: 550px">
      <template #header>
        {{ tagModal.form.id ? '编辑标签' : '新增标签' }}
      </template>
      <NForm :model="tagModal.form" label-placement="left" label-width="80" @submit.prevent>
        <NFormItem label="标签名称" required>
          <NInput v-model:value="tagModal.form.name" placeholder="例如：川菜、辣、下饭" />
        </NFormItem>
        <NFormItem v-if="isSuperAdmin" label="所属家庭" required>
          <NSelect
            v-model:value="tagModal.form.familyId"
            :options="familyOptions"
            placeholder="请选择家庭"
            clearable
          />
        </NFormItem>
        <NFormItem label="标签图标" required>
          <div class="icon-selector">
            <div class="icon-preview">
              <span class="preview-icon">{{ tagModal.form.icon || '🔸' }}</span>
              <span class="preview-text">{{ tagModal.form.icon ? '当前图标' : '请选择图标' }}</span>
            </div>
            <div class="emoji-grid">
              <button
                v-for="emoji in commonEmojis"
                :key="emoji"
                class="emoji-btn"
                :class="{ active: tagModal.form.icon === emoji }"
                @click="tagModal.form.icon = emoji"
                type="button"
              >
                {{ emoji }}
              </button>
            </div>
            <NInput
              v-model:value="tagModal.form.icon"
              placeholder="或直接输入 Emoji"
              style="margin-top: 12px"
            />
          </div>
        </NFormItem>
        <NFormItem label="排序号">
          <NInputNumber v-model:value="tagModal.form.sort" :min="0" placeholder="数字越小越靠前" style="width: 100%" />
        </NFormItem>
      </NForm>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="tagModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="tagModal.loading" @click="saveTag">保存</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, computed } from 'vue';
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NSelect,
  NSpace,
  NTag,
  useMessage,
  type DataTableColumns,
  type PaginationProps
} from 'naive-ui';
import { fetchDishTags, createDishTag, updateDishTag, removeDishTag, fetchAllFamilies, fetchProfile, type DishTagPayload } from '@/api/modules';

interface Tag {
  id?: number;
  name: string;
  icon: string;
  sort: number;
  familyId?: number;
  familyName?: string;
  createTime?: string;
  updateTime?: string;
}

interface Family {
  id: number;
  name: string;
}

const message = useMessage();
const tags = ref<Tag[]>([]);
const loading = ref(false);
const searchName = ref('');

// 家庭相关
const families = ref<Family[]>([]);
const currentUserRole = ref<number | null>(null);
const selectedFamilyId = ref<number | null>(null);
const isSuperAdmin = computed(() => currentUserRole.value === 2);

// 家庭选项（用于添加/编辑标签）
const familyOptions = computed(() => {
  return families.value.map(f => ({
    label: f.name,
    value: f.id
  }));
});

// 家庭筛选选项（用于筛选标签列表）
const familyFilterOptions = computed(() => {
  return [
    { label: '全部家庭', value: null },
    ...families.value.map(f => ({
      label: f.name,
      value: f.id
    }))
  ];
});

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showQuickJumper: true,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => `共 ${itemCount ?? 0} 条`,
  onChange: (page: number) => {
    pagination.page = page;
    loadTags();
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    loadTags();
  }
});

const tagModal = reactive({
  show: false,
  loading: false,
  form: {
    id: undefined as number | undefined,
    name: '',
    icon: '',
    sort: 0,
    familyId: null as number | null
  }
});

const commonEmojis = [
  '🌶️', '🔥', '🥘', '🍖', '🦐', '🥟', '🍲',
  '🍋', '🍯', '🍬', '🌿', '🧄', '✨', '⚡',
  '🍚', '🏠', '⭐', '👑', '💎', '🥬', '💚',
  '🥗', '☘️', '🦞', '🍢', '🍜', '🍰',
  '👶', '😋', '🫖', '🍵', '🔸', '🥕', '🍅'
];

// 动态生成列配置
const columns = computed<DataTableColumns<Tag>>(() => {
  const baseColumns: DataTableColumns<Tag> = [
    {
      title: 'ID',
      key: 'id',
      width: 80,
      align: 'center'
    },
    {
      title: '标签名称',
      key: 'name',
      width: 150,
      render: (row) => {
        return h(NTag, { type: 'info', size: 'medium' }, { default: () => row.name });
      }
    },
    {
      title: '图标',
      key: 'icon',
      width: 100,
      align: 'center',
      render: (row) => {
        return h('span', { style: 'font-size: 28px' }, row.icon);
      }
    },
    {
      title: '排序',
      key: 'sort',
      width: 100,
      align: 'center'
    }
  ];

  // 超级管理员显示家庭列
  if (isSuperAdmin.value) {
    baseColumns.push({
      title: '所属家庭',
      key: 'familyName',
      width: 150,
      render: (row) => row.familyName || '—'
    });
  }

  baseColumns.push(
    {
      title: '创建时间',
      key: 'createTime',
      width: 180,
      render: (row) => {
        return row.createTime ? new Date(row.createTime).toLocaleString('zh-CN') : '-';
      }
    },
    {
      title: '操作',
      key: 'actions',
      width: 180,
      align: 'center',
      render: (row) => {
        return h(
          NSpace,
          { justify: 'center' },
          {
            default: () => [
              h(
                NButton,
                {
                  size: 'small',
                  secondary: true,
                  onClick: () => openTagModal(row)
                },
                { default: () => '编辑' }
              ),
              h(
                NButton,
                {
                  size: 'small',
                  type: 'error',
                  secondary: true,
                  onClick: () => handleDelete(row)
                },
                { default: () => '删除' }
              )
            ]
          }
        );
      }
    }
  );

  return baseColumns;
});

// 加载家庭列表
const loadFamilies = async () => {
  try {
    const res = await fetchAllFamilies();
    if (res.data) {
      families.value = res.data || [];
    }
  } catch (error) {
    console.error('加载家庭列表失败:', error);
  }
};

// 加载当前用户信息
const loadCurrentUser = async () => {
  try {
    const res = await fetchProfile();
    if (res.data) {
      currentUserRole.value = res.data.role ?? null;
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
  }
};

const loadTags = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    };
    if (searchName.value) {
      params.name = searchName.value;
    }
    // 超级管理员可以按家庭筛选
    if (isSuperAdmin.value && selectedFamilyId.value) {
      params.familyId = selectedFamilyId.value;
    }

    const res = await fetchDishTags(params);
    if (res.data) {
      const records = res.data.records || [];
      // 为每条记录添加家庭名称
      tags.value = records.map((tag: any) => {
        const family = families.value.find(f => f.id === tag.familyId);
        return {
          ...tag,
          familyName: family?.name || '—'
        };
      });
      pagination.itemCount = res.data.total || 0;
    }
  } catch (error: any) {
    console.error('加载标签失败:', error);
    message.error(error.message || '加载标签失败');
  } finally {
    loading.value = false;
  }
};

let searchTimer: any = null;
const handleSearch = () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    pagination.page = 1;
    loadTags();
  }, 500);
};

const handleFamilyChange = () => {
  pagination.page = 1;
  loadTags();
};

const openTagModal = (tag?: Tag) => {
  if (tag) {
    // 编辑
    tagModal.form.id = tag.id;
    tagModal.form.name = tag.name;
    tagModal.form.icon = tag.icon;
    tagModal.form.sort = tag.sort;
    tagModal.form.familyId = tag.familyId ?? null;
  } else {
    // 新增
    tagModal.form.id = undefined;
    tagModal.form.name = '';
    tagModal.form.icon = '';
    tagModal.form.sort = 0;
    // 新增时，如果当前有筛选的家庭，则默认选中该家庭
    tagModal.form.familyId = selectedFamilyId.value;
  }
  tagModal.show = true;
};

const saveTag = async () => {
  if (!tagModal.form.name) {
    message.warning('请输入标签名称');
    return;
  }
  if (!tagModal.form.icon) {
    message.warning('请选择标签图标');
    return;
  }

  tagModal.loading = true;
  try {
    const payload: DishTagPayload = {
      name: tagModal.form.name,
      icon: tagModal.form.icon,
      sort: tagModal.form.sort || 0,
      familyId: tagModal.form.familyId ?? undefined
    };

    if (tagModal.form.id) {
      payload.id = tagModal.form.id;
      await updateDishTag(payload);
      message.success('更新成功');
    } else {
      await createDishTag(payload);
      message.success('添加成功');
    }

    tagModal.show = false;
    loadTags();
  } catch (error: any) {
    console.error('保存标签失败:', error);
    message.error(error.message || '保存失败');
  } finally {
    tagModal.loading = false;
  }
};

const handleDelete = async (tag: Tag) => {
  if (!tag.id) return;

  const confirmed = window.confirm(`确定要删除标签"${tag.name}"吗？`);
  if (!confirmed) return;

  try {
    await removeDishTag(tag.id);
    message.success('删除成功');
    loadTags();
  } catch (error: any) {
    console.error('删除标签失败:', error);
    message.error(error.message || '删除失败');
  }
};

onMounted(async () => {
  await loadCurrentUser();
  if (isSuperAdmin.value) {
    await loadFamilies();
  }
  loadTags();
});
</script>

<style scoped lang="scss">
.tags-view {
  padding: 24px;
}

.glass-card {
  background: var(--gradient-card);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;

  &.hover-rise:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 4px 0;
    color: var(--text-primary);
  }

  p {
    margin: 0;
    font-size: 14px;
    color: var(--text-secondary);
  }

  .table-actions {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.icon-selector {
  width: 100%;
}

.icon-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--gradient-primary);
  border-radius: 12px;
  margin-bottom: 16px;

  .preview-icon {
    font-size: 48px;
    line-height: 1;
  }

  .preview-text {
    font-size: 14px;
    color: white;
    font-weight: 500;
  }
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  padding: 16px;
  background: var(--bg-elevated);
  border-radius: 12px;
  max-height: 240px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: var(--border-secondary);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--text-tertiary);
    border-radius: 3px;

    &:hover {
      background: var(--text-secondary);
    }
  }
}

.emoji-btn {
  width: 48px;
  height: 48px;
  border: 2px solid transparent;
  background: var(--bg-card);
  border-radius: 10px;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    transform: scale(1.1);
    box-shadow: var(--shadow-md);
  }

  &.active {
    border-color: var(--primary-color);
    background: var(--bg-elevated);
    box-shadow: 0 0 0 3px rgba(var(--primary-h), var(--primary-s), var(--primary-l), 0.2);
  }
}

// Naive UI 样式覆盖
:deep(.n-data-table) {
  .n-data-table-th {
    font-weight: 600;
    background: var(--bg-elevated);
  }

  .n-data-table-td {
    font-size: 14px;
  }
}

:deep(.primary-soft) {
  background: var(--gradient-primary);
  border: none;

  &:hover {
    opacity: 0.9;
  }
}
</style>
