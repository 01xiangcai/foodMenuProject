<template>
  <div class="users-container">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <div>
          <h2>👥 用户管理</h2>
          <p>管理员和小程序用户的统一管理</p>
        </div>
        <div class="table-actions">
          <NSelect
            v-model:value="filters.userType"
            placeholder="用户类型"
            :options="userTypeOptions"
            style="width: 150px"
            @update:value="handleUserTypeChange"
          />
          <NInput
            v-model:value="filters.search"
            clearable
            placeholder="用户名/昵称/手机号"
            style="width: 200px"
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
    <NModal v-model:show="userModal.show" preset="card" style="max-width: 600px">
      <template #header>
        {{ userModal.form.id ? '编辑用户' : '新增用户' }}
      </template>
      <NForm :model="userModal.form" label-placement="left" label-width="100" @submit.prevent>
        <NFormItem label="用户类型" required>
          <NRadioGroup v-model:value="userModal.form.userType" :disabled="!!userModal.form.id">
            <NRadio value="admin">管理员</NRadio>
            <NRadio value="wxuser">小程序用户</NRadio>
          </NRadioGroup>
        </NFormItem>
        
        <NFormItem label="用户名" required v-if="!userModal.form.id">
          <NInput v-model:value="userModal.form.username" placeholder="用户名" :disabled="!!userModal.form.id" />
        </NFormItem>
        
        <NFormItem label="密码" :required="!userModal.form.id" v-if="!userModal.form.id">
          <NInput
            v-model:value="userModal.form.password"
            type="password"
            placeholder="至少6位"
          />
        </NFormItem>

        <!-- Admin-specific fields -->
        <template v-if="userModal.form.userType === 'admin'">
          <NFormItem label="姓名">
            <NInput v-model:value="userModal.form.name" placeholder="张三" />
          </NFormItem>
        </template>

        <!-- WxUser-specific fields -->
        <template v-if="userModal.form.userType === 'wxuser'">
          <NFormItem label="昵称">
            <NInput v-model:value="userModal.form.nickname" placeholder="用户昵称" />
          </NFormItem>
          <NFormItem label="性别">
            <NRadioGroup v-model:value="userModal.form.gender">
              <NRadio :value="0">未知</NRadio>
              <NRadio :value="1">男</NRadio>
              <NRadio :value="2">女</NRadio>
            </NRadioGroup>
          </NFormItem>
        </template>

        <NFormItem label="手机号" required>
          <NInput v-model:value="userModal.form.phone" placeholder="13800138000" />
        </NFormItem>
        
        <NFormItem v-if="userModal.form.userType === 'wxuser'" label="小程序角色">
          <NRadioGroup v-model:value="userModal.form.role">
            <NRadio :value="0">普通用户</NRadio>
            <NRadio :value="1">小程序管理员</NRadio>
          </NRadioGroup>
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
  NAvatar,
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NRadio,
  NRadioGroup,
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
  createWxUser,
  deleteUser,
  deleteWxUser,
  fetchUsers,
  fetchWxUsers,
  resetUserPassword,
  resetWxUserPassword,
  updateUser,
  updateUserStatus,
  updateWxUser,
  updateWxUserStatus,
  type UserPayload,
  type WxUserPayload
} from '@/api/modules';

type UnifiedUserRecord = {
  id: number;
  userType: 'admin' | 'wxuser';
  username?: string;
  nickname?: string;
  name?: string;
  phone?: string;
  avatar?: string;
  gender?: number;
  status: number;
  createTime: string;
  role?: number;
};

const message = useMessage();
const dialog = useDialog();

const users = ref<UnifiedUserRecord[]>([]);
const loading = ref(false);

const filters = reactive({
  userType: 'all' as 'all' | 'admin' | 'wxuser',
  search: '',
  status: 0 as number | null
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
    userType: 'admin' as 'admin' | 'wxuser',
    username: '',
    password: '',
    name: '',
    nickname: '',
    phone: '',
    gender: 0,
    status: 1,
    role: 0
  }
});

const passwordModal = reactive({
  show: false,
  loading: false,
  userId: null as number | null,
  userType: 'admin' as 'admin' | 'wxuser',
  newPassword: ''
});

const userTypeOptions = [
  { label: '全部用户', value: 'all' },
  { label: '管理员', value: 'admin' },
  { label: '小程序用户', value: 'wxuser' }
];

const statusOptions = [
  { label: '全部', value: 0 }, // Changed from null to 0 to avoid type error, need to handle in fetch
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 } // Changed 0 to 2 to avoid conflict if 0 is 'all'
];

const columns: DataTableColumns<UnifiedUserRecord> = [
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '用户类型',
    key: 'userType',
    width: 120,
    render: (row) =>
      h(
        NTag,
        { type: row.userType === 'admin' ? 'info' : 'success', bordered: false },
        { default: () => (row.userType === 'admin' ? '管理员' : '小程序用户') }
      )
  },
  {
    title: '头像',
    key: 'avatar',
    width: 80,
    render: (row) =>
      row.avatar
        ? h(NAvatar, { src: row.avatar, size: 40, round: true })
        : h(NAvatar, { size: 40, round: true }, { default: () => row.nickname?.[0] || row.username?.[0] || '?' })
  },
  {
    title: '用户名',
    key: 'username',
    ellipsis: { tooltip: true },
    render: (row) => row.username || '—'
  },
  {
    title: '姓名/昵称',
    key: 'displayName',
    render: (row) => row.name || row.nickname || '—'
  },
  {
    title: '手机号',
    key: 'phone',
    render: (row) => row.phone || '—'
  },
  {
    title: '角色',
    key: 'role',
    width: 100,
    render: (row) =>
      row.userType === 'wxuser'
        ? (row.role === 1 ? '小程序管理员' : '普通用户')
        : '—'
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
    width: 320,
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
                onClick: () => openEditModal(row)
              },
              { default: () => '编辑' }
            ),
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
              { size: 'small', tertiary: true, onClick: () => openPasswordModal(row.id, row.userType) },
              { default: () => '重置密码' }
            ),
            h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                type: 'error',
                onClick: () => handleDeleteUser(row.id, row.userType)
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

const handleUserTypeChange = () => {
  pagination.page = 1;
  loadUsers();
};

const refreshUsers = () => {
  pagination.page = 1;
  loadUsers();
};

const resetUserForm = () => {
  userModal.form.id = undefined;
  userModal.form.userType = 'admin';
  userModal.form.username = '';
  userModal.form.password = '';
  userModal.form.name = '';
  userModal.form.nickname = '';
  userModal.form.phone = '';
  userModal.form.gender = 0;
  userModal.form.status = 1;
  userModal.form.role = 0;
};

const openUserModal = () => {
  resetUserForm();
  userModal.show = true;
};

const openEditModal = (user: UnifiedUserRecord) => {
  userModal.form.id = user.id;
  userModal.form.userType = user.userType;
  userModal.form.username = user.username || '';
  userModal.form.password = '';
  userModal.form.name = user.name || '';
  userModal.form.nickname = user.nickname || '';
  userModal.form.phone = user.phone || '';
  userModal.form.gender = user.gender || 0;
  userModal.form.status = user.status;
  userModal.form.role = user.role ?? 0;
  userModal.show = true;
};

const saveUser = async () => {
  // Validation for new users
  if (!userModal.form.id) {
    if (!userModal.form.username.trim()) {
      message.warning('请输入用户名');
      return;
    }
    if (!userModal.form.password) {
      message.warning('请输入密码');
      return;
    }
    if (userModal.form.password.length < 6) {
      message.warning('密码至少6位');
      return;
    }
    if (!userModal.form.phone) {
      message.warning('请输入手机号');
      return;
    }
  }

  userModal.loading = true;
  try {
    if (userModal.form.userType === 'admin') {
      const payload: UserPayload = {
        id: userModal.form.id,
        username: userModal.form.username.trim(),
        password: userModal.form.password || undefined,
        name: userModal.form.name || undefined,
        phone: userModal.form.phone || undefined,
        status: userModal.form.status
      };
      
      if (userModal.form.id) {
        await updateUser(payload);
      } else {
        await createUser(payload);
      }
    } else {
      const payload: WxUserPayload = {
        id: userModal.form.id,
        username: userModal.form.username.trim(),
        password: userModal.form.password || undefined,
        nickname: userModal.form.nickname || undefined,
        phone: userModal.form.phone || undefined,
        gender: userModal.form.gender,
        status: userModal.form.status,
        role: userModal.form.role
      };
      
      if (userModal.form.id) {
        await updateWxUser(payload);
      } else {
        await createWxUser(payload);
      }
    }
    
    message.success(userModal.form.id ? '用户已更新' : '用户已创建');
    userModal.show = false;
    await loadUsers();
  } catch (error) {
    message.error((error as Error).message || '操作失败');
  } finally {
    userModal.loading = false;
  }
};

const toggleUserStatus = async (user: UnifiedUserRecord) => {
  const nextStatus = user.status === 1 ? 0 : 1;
  try {
    if (user.userType === 'admin') {
      await updateUserStatus(user.id, nextStatus);
    } else {
      await updateWxUserStatus(user.id, nextStatus);
    }
    message.success(nextStatus === 1 ? '已启用' : '已禁用');
    await loadUsers();
  } catch (error) {
    message.error((error as Error).message || '操作失败');
  }
};

const handleDeleteUser = (id: number, userType: 'admin' | 'wxuser') => {
  dialog.warning({
    title: '确认删除',
    content: '删除后该用户将无法登录，确定要删除吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        if (userType === 'admin') {
          await deleteUser(id);
        } else {
          await deleteWxUser(id);
        }
        message.success('用户已删除');
        await loadUsers();
      } catch (error) {
        message.error((error as Error).message || '删除失败');
      }
    }
  });
};

const openPasswordModal = (userId: number, userType: 'admin' | 'wxuser') => {
  passwordModal.userId = userId;
  passwordModal.userType = userType;
  passwordModal.newPassword = '';
  passwordModal.show = true;
};

const handleResetPassword = async () => {
  if (!passwordModal.userId) return;

  passwordModal.loading = true;
  try {
    let result;
    if (passwordModal.userType === 'admin') {
      result = await resetUserPassword(passwordModal.userId);
    } else {
      result = await resetWxUserPassword(passwordModal.userId);
    }
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
  loading.value = true;
  try {
    const pageSize = pagination.pageSize || 10;
    const page = pagination.page || 1;
    
    if (filters.userType === 'all') {
      // Load both admin and wxuser
      const [adminResult, wxUserResult] = await Promise.all([
        fetchUsers({
          page,
          pageSize: Math.ceil(pageSize / 2),
          username: filters.search || undefined,
          phone: filters.search || undefined,
          status: (filters.status === 0 || filters.status === null) ? undefined : (filters.status === 2 ? 0 : filters.status)
        }),
        fetchWxUsers({
          page,
          pageSize: Math.ceil(pageSize / 2),
          username: filters.search || undefined,
          nickname: filters.search || undefined,
          phone: filters.search || undefined,
          status: (filters.status === 0 || filters.status === null) ? undefined : (filters.status === 2 ? 0 : filters.status)
        })
      ]);

      const adminUsers: UnifiedUserRecord[] = (adminResult.data?.records || []).map((user: any) => ({
        id: user.id,
        userType: 'admin' as const,
        username: user.username,
        name: user.name,
        phone: user.phone,
        avatar: user.avatar,
        status: user.status,
        createTime: user.createTime
      }));

      const wxUsers: UnifiedUserRecord[] = (wxUserResult.data?.records || []).map((user: any) => ({
        id: user.id,
        userType: 'wxuser' as const,
        username: user.username,
        nickname: user.nickname,
        phone: user.phone,
        avatar: user.avatar,
        gender: user.gender,
        status: user.status,
        createTime: user.createTime,
        role: user.role
      }));

      users.value = [...adminUsers, ...wxUsers].sort(
        (a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      );
      pagination.itemCount = (adminResult.data?.total || 0) + (wxUserResult.data?.total || 0);
    } else if (filters.userType === 'admin') {
      // Load only admin users
      const result = await fetchUsers({
        page,
        pageSize,
        username: filters.search || undefined,
        phone: filters.search || undefined,
        status: (filters.status === 0 || filters.status === null) ? undefined : (filters.status === 2 ? 0 : filters.status)
      });

      users.value = (result.data?.records || []).map((user: any) => ({
        id: user.id,
        userType: 'admin' as const,
        username: user.username,
        name: user.name,
        phone: user.phone,
        avatar: user.avatar,
        status: user.status,
        createTime: user.createTime
      }));
      pagination.itemCount = result.data?.total || 0;
    } else {
      // Load only wxuser
      const result = await fetchWxUsers({
        page,
        pageSize,
        username: filters.search || undefined,
        nickname: filters.search || undefined,
        phone: filters.search || undefined,
        status: (filters.status === 0 || filters.status === null) ? undefined : (filters.status === 2 ? 0 : filters.status)
      });

      users.value = (result.data?.records || []).map((user: any) => ({
        id: user.id,
        userType: 'wxuser' as const,
        username: user.username,
        nickname: user.nickname,
        phone: user.phone,
        avatar: user.avatar,
        gender: user.gender,
        status: user.status,
        createTime: user.createTime,
        role: user.role
      }));
      pagination.itemCount = result.data?.total || 0;
    }
  } catch (error) {
    console.error('Load users error:', error);
    message.error((error as Error).message || '加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
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
  margin-bottom: 24px;
}

.table-header h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: var(--text-primary);
}

.table-header p {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: 0;
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

/* Enhanced Button Styles */
:deep(.n-button.primary-soft) {
  background: var(--gradient-primary);
  border: none;
  color: white;
  font-weight: 600;
  box-shadow: var(--shadow-glow);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.n-button.primary-soft:not(.n-button--disabled):hover) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  filter: brightness(1.1);
}

:deep(.n-button.primary-soft:not(.n-button--disabled):active) {
  transform: translateY(0);
}

/* Data Table Enhancements */
:deep(.n-data-table) {
  background: transparent;
}

:deep(.n-data-table-th) {
  background: rgba(var(--primary-h), var(--primary-s), var(--primary-l), 0.05);
  border-bottom: 1px solid var(--border-primary);
  color: var(--text-secondary);
  font-weight: 600;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.5px;
}

:deep(.n-data-table-td) {
  border-bottom: 1px solid var(--border-secondary);
  color: var(--text-primary);
  background: transparent;
}

:deep(.n-data-table-tr:hover .n-data-table-td) {
  background: rgba(var(--primary-h), var(--primary-s), var(--primary-l), 0.05) !important;
}

/* Input and Select Styles */
:deep(.n-input),
:deep(.n-base-selection) {
  background: var(--bg-glass) !important;
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-primary) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px !important;
}

:deep(.n-input:hover),
:deep(.n-base-selection:hover) {
  border-color: var(--text-tertiary) !important;
}

:deep(.n-input:focus-within),
:deep(.n-base-selection:focus-within) {
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 3px var(--border-focus) !important;
}

/* Modal Enhancements */
:deep(.n-card) {
  background: var(--bg-card);
  border: 1px solid var(--border-primary);
  box-shadow: var(--shadow-lg);
  border-radius: 16px;
}

:deep(.n-card-header) {
  border-bottom: 1px solid var(--border-secondary);
  color: var(--text-primary);
  font-weight: 700;
  font-size: 18px;
}

/* Tag Enhancements */
:deep(.n-tag) {
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
  padding: 2px 10px;
}

/* Avatar Enhancements */
:deep(.n-avatar) {
  border: 2px solid var(--bg-card);
  box-shadow: var(--shadow-sm);
}

/* Pagination Enhancements */
:deep(.n-pagination) {
  margin-top: 24px;
}

:deep(.n-pagination-item) {
  background: transparent;
  border: 1px solid var(--border-primary);
  color: var(--text-secondary);
  transition: all 0.3s;
  border-radius: 8px;
}

:deep(.n-pagination-item:hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

:deep(.n-pagination-item--active) {
  background: var(--gradient-primary);
  color: white !important;
  border: none;
  box-shadow: var(--shadow-glow);
}


/* Form Item Enhancements */
:deep(.n-form-item-label) {
  color: var(--text-secondary);
  font-weight: 600;
}

/* Switch Enhancements */
:deep(.n-switch) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.n-switch:hover) {
  transform: scale(1.05);
}

/* Radio Enhancements */
:deep(.n-radio) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.n-radio:hover) {
  transform: translateX(2px);
}

/* Loading State */
:deep(.n-data-table--loading) {
  opacity: 0.6;
}

/* Empty State */
:deep(.n-data-table-empty) {
  color: var(--text-tertiary);
  padding: 40px 20px;
}

/* Responsive */
@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-actions {
    width: 100%;
  }

  .table-actions > * {
    flex: 1;
    min-width: 0;
  }
}
</style>

