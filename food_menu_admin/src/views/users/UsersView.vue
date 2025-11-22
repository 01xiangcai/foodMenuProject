<template>
  <div class="users-container">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <div>
          <h2>👥 用户管理</h2>
          <p>管理员和小程序用户的统一管理</p>
        </div>
        <div class="table-actions">
          <NInput
            v-model:value="filters.username"
            clearable
            placeholder="用户名"
            style="width: 150px"
            @keydown.enter.prevent="refreshUsers"
          />
          <NInput
            v-model:value="filters.phone"
            clearable
            placeholder="手机号"
            style="width: 150px"
            @keydown.enter.prevent="refreshUsers"
          />
          <NSelect
            v-model:value="filters.status"
            clearable
            placeholder="状态"
            :options="statusOptions"
            style="width: 120px"
          />
          <NButton secondary @click="refreshUsers">刷新</NButton>
          <NButton class="primary-soft" type="primary" @click="openUserModal()">新增用户</NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="users"
        :loading="loading"
        :pagination="pagination"
        size="large"
      />
    </section>

    <!-- Create/Edit User Modal -->
    <NModal v-model:show="userModal.show" preset="card" style="max-width: 520px">
      <template #header>
        {{ userModal.form.id ? '编辑用户' : '新增用户' }}
      </template>
      <NForm :model="userModal.form" label-placement="left" label-width="80" @submit.prevent>
        <NFormItem label="用户名" required>
          <NInput v-model:value="userModal.form.username" placeholder="admin" :disabled="!!userModal.form.id" />
        </NFormItem>
        <NFormItem label="密码" :required="!userModal.form.id">
          <NInput
            v-model:value="userModal.form.password"
            type="password"
            placeholder="至少6位"
            :disabled="!!userModal.form.id"
          />
          <template #feedback v-if="userModal.form.id">
            <span style="font-size: 12px; opacity: 0.7">编辑时不可修改密码，请使用重置密码功能</span>
          </template>
        </NFormItem>
        <NFormItem label="姓名">
          <NInput v-model:value="userModal.form.name" placeholder="张三" />
        </NFormItem>
        <NFormItem label="手机号">
          <NInput v-model:value="userModal.form.phone" placeholder="13800138000" />
        </NFormItem>
        <NFormItem label="状态">
          <NSwitch v-model:value="userModal.form.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </NSwitch>
        </NFormItem>
      </NForm>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="userModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="userModal.loading" @click="saveUser">保存</NButton>
        </div>
      </template>
    </NModal>

    <!-- Reset Password Modal -->
    <NModal v-model:show="passwordModal.show" preset="card" style="max-width: 420px">
      <template #header>重置密码</template>
      <div style="padding: 20px 0">
        <p style="margin-bottom: 16px">确定要重置该用户的密码吗？</p>
        <p v-if="passwordModal.newPassword" style="padding: 12px; background: rgba(20, 184, 255, 0.1); border-radius: 8px; font-family: monospace">
          新密码：<strong style="color: #14b8ff; font-size: 18px">{{ passwordModal.newPassword }}</strong>
        </p>
        <p v-if="passwordModal.newPassword" style="margin-top: 12px; font-size: 12px; opacity: 0.7">
          请将此密码告知用户，关闭后将无法再次查看
        </p>
      </div>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="passwordModal.show = false">
            {{ passwordModal.newPassword ? '关闭' : '取消' }}
          </NButton>
          <NButton
            v-if="!passwordModal.newPassword"
            type="warning"
            :loading="passwordModal.loading"
            @click="handleResetPassword"
          >
            确认重置
          </NButton>
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
  NModal,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type PaginationProps
} from 'naive-ui';
import {
  createUser,
  deleteUser,
  fetchUsers,
  resetUserPassword,
  updateUserStatus,
  type UserPayload
} from '@/api/modules';

type UserRecord = {
  id: number;
  username: string;
  name?: string;
  phone?: string;
  avatar?: string;
  status: number;
  createTime: string;
};

const message = useMessage();
const dialog = useDialog();

const users = ref<UserRecord[]>([]);
const loading = ref(false);

const filters = reactive({
  username: '',
  phone: '',
  status: null as number | null
});

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showQuickJumper: true,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  prefix: ({ itemCount }) => `共 ${itemCount ?? 0} 个用户`,
  onChange: (page: number) => {
    pagination.page = page;
    loadUsers();
  },
  onUpdatePageSize: (size: number) => {
    pagination.pageSize = size;
    pagination.page = 1;
    loadUsers();
  }
});

const userModal = reactive({
  show: false,
  loading: false,
  form: {
    id: undefined as number | undefined,
    username: '',
    password: '',
    name: '',
    phone: '',
    status: 1
  }
});

const passwordModal = reactive({
  show: false,
  loading: false,
  userId: null as number | null,
  newPassword: ''
});

const statusOptions = [
  { label: '全部', value: null },
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
];

const columns: DataTableColumns<UserRecord> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username', ellipsis: { tooltip: true } },
  {
    title: '姓名',
    key: 'name',
    render: (row) => row.name || '—'
  },
  {
    title: '手机号',
    key: 'phone',
    render: (row) => row.phone || '—'
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', bordered: false },
        { default: () => (row.status === 1 ? '启用' : '禁用') }
      )
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: (row) => formatTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    render: (row) =>
      h(
        NSpace,
        { size: 8 },
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                type: row.status === 1 ? 'warning' : 'success',
                onClick: () => toggleUserStatus(row)
              },
              { default: () => (row.status === 1 ? '禁用' : '启用') }
            ),
            h(
              NButton,
              { size: 'small', tertiary: true, onClick: () => openPasswordModal(row.id) },
              { default: () => '重置密码' }
            ),
            h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                type: 'error',
                onClick: () => handleDeleteUser(row.id)
              },
              { default: () => '删除' }
            )
          ]
        }
      )
  }
];

const formatTime = (timeStr: string) => {
  if (!timeStr) return '—';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const refreshUsers = () => {
  pagination.page = 1;
  loadUsers();
};

const resetUserForm = () => {
  userModal.form.id = undefined;
  userModal.form.username = '';
  userModal.form.password = '';
  userModal.form.name = '';
  userModal.form.phone = '';
  userModal.form.status = 1;
};

const openUserModal = () => {
  resetUserForm();
  userModal.show = true;
};

const saveUser = async () => {
  if (!userModal.form.username.trim()) {
    message.warning('请输入用户名');
    return;
  }
  if (!userModal.form.id && !userModal.form.password) {
    message.warning('请输入密码');
    return;
  }
  if (userModal.form.password && userModal.form.password.length < 6) {
    message.warning('密码至少6位');
    return;
  }

  userModal.loading = true;
  try {
    const payload: UserPayload = {
      id: userModal.form.id,
      username: userModal.form.username.trim(),
      password: userModal.form.password || undefined,
      name: userModal.form.name || undefined,
      phone: userModal.form.phone || undefined,
      status: userModal.form.status
    };

    await createUser(payload);
    message.success(userModal.form.id ? '用户已更新' : '用户已创建');
    userModal.show = false;
    await loadUsers();
  } catch (error) {
    message.error((error as Error).message || '操作失败');
  } finally {
    userModal.loading = false;
  }
};

const toggleUserStatus = async (user: UserRecord) => {
  const nextStatus = user.status === 1 ? 0 : 1;
  try {
    await updateUserStatus(user.id, nextStatus);
    message.success(nextStatus === 1 ? '已启用' : '已禁用');
    await loadUsers();
  } catch (error) {
    message.error((error as Error).message || '操作失败');
  }
};

const handleDeleteUser = (id: number) => {
  dialog.warning({
    title: '确认删除',
    content: '删除后该用户将无法登录，确定要删除吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser(id);
        message.success('用户已删除');
        await loadUsers();
      } catch (error) {
        message.error((error as Error).message || '删除失败');
      }
    }
  });
};

const openPasswordModal = (userId: number) => {
  passwordModal.userId = userId;
  passwordModal.newPassword = '';
  passwordModal.show = true;
};

const handleResetPassword = async () => {
  if (!passwordModal.userId) return;

  passwordModal.loading = true;
  try {
    const result = await resetUserPassword(passwordModal.userId);
    passwordModal.newPassword = result.data;
    message.success('密码已重置');
  } catch (error) {
    message.error((error as Error).message || '重置失败');
    passwordModal.show = false;
  } finally {
    passwordModal.loading = false;
  }
};

const loadUsers = async () => {
  console.log('[DEBUG] loadUsers called');
  loading.value = true;
  try {
    console.log('[DEBUG] Fetching users...');
    const result = await fetchUsers({
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: filters.username || undefined,
      phone: filters.phone || undefined,
      status: filters.status ?? undefined
    });
    console.log('[DEBUG] Result:', result);
    users.value = result.data?.records || [];
    pagination.itemCount = result.data?.total || 0;
    console.log('[DEBUG] Users loaded:', users.value.length);
  } catch (error) {
    console.error('[DEBUG] Error:', error);
    message.error((error as Error).message || '加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  console.log('[DEBUG] UsersView mounted');
  loadUsers();
});
</script>

<style scoped>
.users-container {
  padding: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.table-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.n-button.primary-soft) {
  background: linear-gradient(120deg, #14b8ff, #a855f7);
  border: none;
  color: #021221;
  font-weight: 600;
  box-shadow: 0 8px 24px rgba(20, 184, 255, 0.35);
}

:deep(.n-button.primary-soft:not(.n-button--disabled):hover) {
  filter: brightness(1.05);
}
</style>
