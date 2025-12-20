<template>
  <div class="notification-types-view">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <div>
          <h2>通知类型配置</h2>
          <p>管理系统通知类型和消息模板</p>
        </div>
        <div class="table-actions">
          <NButton secondary @click="loadNotificationTypes">刷新</NButton>
          <NButton class="primary-soft" type="primary" @click="openBroadcastModal">发送公告</NButton>
          <NButton class="primary-soft" type="primary" @click="openTypeModal()">新增类型</NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="notificationTypes"
        :loading="loading"
        size="large"
      />
    </section>

    <!-- 编辑/新增通知类型Modal -->
    <NModal v-model:show="typeModal.show" preset="card" :mask-closable="false" style="max-width: 600px">
      <template #header>
        {{ typeModal.form.id ? '编辑通知类型' : '新增通知类型' }}
      </template>
      <NForm :model="typeModal.form" label-placement="left" label-width="100" @submit.prevent>
        <NFormItem label="类型编码" required>
          <NInput 
            v-model:value="typeModal.form.code" 
            placeholder="例如：MEAL_PUBLISHED"
            :disabled="!!typeModal.form.id"
          />
        </NFormItem>
        <NFormItem label="类型名称" required>
          <NInput v-model:value="typeModal.form.name" placeholder="例如：餐次发布" />
        </NFormItem>
        <NFormItem label="标题模板" required>
          <NInput v-model:value="typeModal.form.titleTemplate" placeholder="支持变量如{mealPeriod}" />
          <template #feedback>
            <span class="form-tip">可用变量: {mealPeriod}, {dishName}, {dishCount}, {amount}</span>
          </template>
        </NFormItem>
        <NFormItem label="内容模板" required>
          <NInput 
            v-model:value="typeModal.form.contentTemplate" 
            type="textarea" 
            :rows="3"
            placeholder="支持变量如{dishName}"
          />
        </NFormItem>
        <NFormItem label="图标">
          <NInput v-model:value="typeModal.form.icon" placeholder="图标URL（可选）" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="typeModal.form.sortOrder" :min="0" placeholder="数字越小越靠前" style="width: 100%" />
        </NFormItem>
      </NForm>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="typeModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="typeModal.loading" @click="saveType">保存</NButton>
        </div>
      </template>
    </NModal>

    <!-- 发送公告Modal -->
    <NModal v-model:show="broadcastModal.show" preset="card" :mask-closable="false" style="max-width: 550px">
      <template #header>发送系统公告</template>
      <NForm :model="broadcastModal.form" label-placement="left" label-width="80" @submit.prevent>
        <NFormItem label="公告标题" required>
          <NInput v-model:value="broadcastModal.form.title" placeholder="请输入公告标题" />
        </NFormItem>
        <NFormItem label="公告内容" required>
          <NInput 
            v-model:value="broadcastModal.form.content" 
            type="textarea" 
            :rows="4"
            placeholder="请输入公告内容"
          />
        </NFormItem>
        <NFormItem label="推送范围">
          <NSelect 
            v-model:value="broadcastModal.form.familyId"
            :options="familyOptions"
            placeholder="全平台推送"
            clearable
          />
        </NFormItem>
      </NForm>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="broadcastModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="broadcastModal.loading" @click="sendBroadcastClick">发送</NButton>
        </div>
      </template>
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
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  useMessage,
  type DataTableColumns
} from 'naive-ui';
import { 
  fetchNotificationTypes, 
  createNotificationType, 
  updateNotificationType, 
  toggleNotificationType, 
  deleteNotificationType,
  sendBroadcast,
  fetchAllFamilies,
  type NotificationTypeConfig 
} from '@/api/modules';

interface NotificationType {
  id?: number;
  code: string;
  name: string;
  titleTemplate: string;
  contentTemplate: string;
  icon?: string;
  isEnabled?: number;
  isSystem?: number;
  sortOrder?: number;
  createTime?: string;
  updateTime?: string;
}

const message = useMessage();
const notificationTypes = ref<NotificationType[]>([]);
const loading = ref(false);
const familyOptions = ref<{ label: string; value: number }[]>([]);

const typeModal = reactive({
  show: false,
  loading: false,
  form: {
    id: undefined as number | undefined,
    code: '',
    name: '',
    titleTemplate: '',
    contentTemplate: '',
    icon: '',
    sortOrder: 0
  }
});

const broadcastModal = reactive({
  show: false,
  loading: false,
  form: {
    title: '',
    content: '',
    familyId: null as number | null
  }
});

const columns: DataTableColumns<NotificationType> = [
  {
    title: 'ID',
    key: 'id',
    width: 60,
    align: 'center'
  },
  {
    title: '类型编码',
    key: 'code',
    width: 160,
    render: (row) => h(NTag, { type: 'info', size: 'small' }, { default: () => row.code })
  },
  {
    title: '类型名称',
    key: 'name',
    width: 120
  },
  {
    title: '标题模板',
    key: 'titleTemplate',
    width: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '内容模板',
    key: 'contentTemplate',
    width: 250,
    ellipsis: { tooltip: true }
  },
  {
    title: '状态',
    key: 'isEnabled',
    width: 100,
    align: 'center',
    render: (row) => {
      return h(NSwitch, {
        value: row.isEnabled === 1,
        onUpdateValue: () => handleToggle(row)
      });
    }
  },
  {
    title: '系统预置',
    key: 'isSystem',
    width: 90,
    align: 'center',
    render: (row) => {
      return h(NTag, 
        { type: row.isSystem === 1 ? 'warning' : 'default', size: 'small' }, 
        { default: () => row.isSystem === 1 ? '是' : '否' }
      );
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
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
                onClick: () => openTypeModal(row)
              },
              { default: () => '编辑' }
            ),
            row.isSystem !== 1 ? h(
              NButton,
              {
                size: 'small',
                type: 'error',
                secondary: true,
                onClick: () => handleDelete(row)
              },
              { default: () => '删除' }
            ) : null
          ].filter(Boolean)
        }
      );
    }
  }
];

const loadNotificationTypes = async () => {
  loading.value = true;
  try {
    const res = await fetchNotificationTypes();
    if (res.data) {
      notificationTypes.value = res.data || [];
    }
  } catch (error: any) {
    console.error('加载通知类型失败:', error);
    message.error(error.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const loadFamilies = async () => {
  try {
    const res = await fetchAllFamilies();
    if (res.data) {
      familyOptions.value = res.data.map((f: any) => ({
        label: f.name,
        value: f.id
      }));
    }
  } catch (error) {
    console.error('加载家庭列表失败:', error);
  }
};

const openTypeModal = (type?: NotificationType) => {
  if (type) {
    typeModal.form.id = type.id;
    typeModal.form.code = type.code;
    typeModal.form.name = type.name;
    typeModal.form.titleTemplate = type.titleTemplate;
    typeModal.form.contentTemplate = type.contentTemplate;
    typeModal.form.icon = type.icon || '';
    typeModal.form.sortOrder = type.sortOrder || 0;
  } else {
    typeModal.form.id = undefined;
    typeModal.form.code = '';
    typeModal.form.name = '';
    typeModal.form.titleTemplate = '';
    typeModal.form.contentTemplate = '';
    typeModal.form.icon = '';
    typeModal.form.sortOrder = 0;
  }
  typeModal.show = true;
};

const openBroadcastModal = () => {
  broadcastModal.form.title = '';
  broadcastModal.form.content = '';
  broadcastModal.form.familyId = null;
  broadcastModal.show = true;
};

const saveType = async () => {
  if (!typeModal.form.code || !typeModal.form.name) {
    message.warning('请填写类型编码和名称');
    return;
  }
  if (!typeModal.form.titleTemplate || !typeModal.form.contentTemplate) {
    message.warning('请填写标题和内容模板');
    return;
  }

  typeModal.loading = true;
  try {
    const payload: NotificationTypeConfig = {
      code: typeModal.form.code,
      name: typeModal.form.name,
      titleTemplate: typeModal.form.titleTemplate,
      contentTemplate: typeModal.form.contentTemplate,
      icon: typeModal.form.icon || undefined,
      sortOrder: typeModal.form.sortOrder
    };

    if (typeModal.form.id) {
      await updateNotificationType(typeModal.form.id, payload);
      message.success('更新成功');
    } else {
      await createNotificationType(payload);
      message.success('添加成功');
    }

    typeModal.show = false;
    loadNotificationTypes();
  } catch (error: any) {
    console.error('保存失败:', error);
    message.error(error.message || '保存失败');
  } finally {
    typeModal.loading = false;
  }
};

const handleToggle = async (type: NotificationType) => {
  if (!type.id) return;
  try {
    await toggleNotificationType(type.id);
    message.success(type.isEnabled === 1 ? '已禁用' : '已启用');
    loadNotificationTypes();
  } catch (error: any) {
    console.error('切换状态失败:', error);
    message.error(error.message || '操作失败');
  }
};

const handleDelete = async (type: NotificationType) => {
  if (!type.id) return;
  if (type.isSystem === 1) {
    message.warning('系统预置类型不可删除');
    return;
  }

  const confirmed = window.confirm(`确定要删除通知类型"${type.name}"吗？`);
  if (!confirmed) return;

  try {
    await deleteNotificationType(type.id);
    message.success('删除成功');
    loadNotificationTypes();
  } catch (error: any) {
    console.error('删除失败:', error);
    message.error(error.message || '删除失败');
  }
};

const sendBroadcastClick = async () => {
  if (!broadcastModal.form.title || !broadcastModal.form.content) {
    message.warning('请填写公告标题和内容');
    return;
  }

  broadcastModal.loading = true;
  try {
    await sendBroadcast({
      title: broadcastModal.form.title,
      content: broadcastModal.form.content,
      familyId: broadcastModal.form.familyId
    });
    message.success('公告发送成功');
    broadcastModal.show = false;
  } catch (error: any) {
    console.error('发送失败:', error);
    message.error(error.message || '发送失败');
  } finally {
    broadcastModal.loading = false;
  }
};

onMounted(() => {
  loadNotificationTypes();
  loadFamilies();
});
</script>

<style scoped lang="scss">
.notification-types-view {
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

.form-tip {
  font-size: 12px;
  color: var(--text-tertiary);
}

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
