<template>
  <div class="marketing-view">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <h2>营销活动管理</h2>
        <div class="table-actions">
          <NSelect
            v-model:value="queryParams.activityType"
            :options="activityTypeOptions"
            placeholder="活动类型"
            clearable
            style="width: 150px"
            @update:value="loadActivities"
          />
          <NSelect
            v-model:value="queryParams.status"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="width: 120px"
            @update:value="loadActivities"
          />
          <NButton secondary @click="loadActivities">刷新</NButton>
          <NButton class="primary-soft" type="primary" @click="handleCreate">新增活动</NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="activities"
        :loading="loading"
        :pagination="pagination"
        size="large"
      />
    </section>

    <!-- 创建/编辑活动对话框 -->
    <NModal v-model:show="activityModal.show" preset="card" :mask-closable="false" style="max-width: 900px">
      <template #header>
        {{ activityModal.isEdit ? '编辑活动' : '新增活动' }}
      </template>
      <NTabs v-model:value="activityModal.activeTab" type="line">
        <!-- 基本信息 -->
        <NTabPane name="basic" tab="基本信息">
          <NForm :model="activityModal.form" label-placement="left" label-width="120">
            <NFormItem label="活动名称" required>
              <NInput v-model:value="activityModal.form.activityName" placeholder="请输入活动名称" />
            </NFormItem>
            <NFormItem label="活动类型" required>
              <NSelect
                v-model:value="activityModal.form.activityType"
                :options="activityTypeOptions.filter(o => o.value)"
                placeholder="请选择活动类型"
              />
            </NFormItem>
            <NFormItem label="活动描述">
              <NInput
                v-model:value="activityModal.form.activityDesc"
                type="textarea"
                :rows="3"
                placeholder="请输入活动描述"
              />
            </NFormItem>
            <NFormItem label="横幅图片">
              <div class="image-upload">
                <NImage
                  v-if="activityModal.imagePreviewUrl || activityModal.form.bannerImage"
                  class="image-preview"
                  :src="activityModal.imagePreviewUrl || activityModal.form.bannerImage"
                  width="300"
                  height="150"
                  object-fit="cover"
                />
                <NUpload
                  :custom-request="handleImageUpload"
                  :show-file-list="false"
                  accept="image/*"
                  :disabled="imageUploading"
                >
                  <NButton :loading="imageUploading">
                    {{ imageUploading ? '上传中...' : '上传图片' }}
                  </NButton>
                </NUpload>
              </div>
            </NFormItem>
            <NFormItem label="开始时间" required>
              <NDatePicker
                v-model:value="activityModal.form.startTime"
                type="datetime"
                clearable
                style="width: 100%"
              />
            </NFormItem>
            <NFormItem label="结束时间" required>
              <NDatePicker
                v-model:value="activityModal.form.endTime"
                type="datetime"
                clearable
                style="width: 100%"
              />
            </NFormItem>
            <NFormItem label="参与限制">
              <NInputNumber v-model:value="activityModal.form.participateLimit" :min="0" placeholder="0表示不限制" style="width: 100%" />
            </NFormItem>
            <NFormItem label="限制类型">
              <NSelect
                v-model:value="activityModal.form.limitType"
                :options="limitTypeOptions"
                placeholder="请选择限制类型"
              />
            </NFormItem>
            <NFormItem label="排序权重">
              <NInputNumber v-model:value="activityModal.form.sortOrder" :min="0" placeholder="数字越大越靠前" style="width: 100%" />
            </NFormItem>
          </NForm>
        </NTabPane>

        <!-- 奖品设置 -->
        <NTabPane name="prizes" tab="奖品设置" :disabled="!activityModal.form.id">
          <div class="prizes-section">
            <div class="prizes-header">
              <NButton type="primary" @click="handleAddPrize">添加奖品</NButton>
              <NText depth="3">已添加 {{ prizes.length }} 个奖品</NText>
            </div>
            <NDataTable
              :columns="prizeColumns"
              :data="prizes"
              :loading="prizesLoading"
              size="small"
            />
          </div>
        </NTabPane>
      </NTabs>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="activityModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="activityModal.loading" @click="saveActivity">保存</NButton>
        </div>
      </template>
    </NModal>

    <!-- 奖品编辑对话框 -->
    <NModal v-model:show="prizeModal.show" preset="card" :mask-closable="false" style="max-width: 600px">
      <template #header>
        {{ prizeModal.isEdit ? '编辑奖品' : '添加奖品' }}
      </template>
      <NForm :model="prizeModal.form" label-placement="left" label-width="100">
        <NFormItem label="奖品名称" required>
          <NInput v-model:value="prizeModal.form.prizeName" placeholder="例如: 10元代金券" />
        </NFormItem>
        <NFormItem label="奖品类型" required>
          <NSelect
            v-model:value="prizeModal.form.prizeType"
            :options="prizeTypeOptions"
            placeholder="请选择奖品类型"
          />
        </NFormItem>
        <NFormItem label="奖品价值">
          <NInputNumber v-model:value="prizeModal.form.prizeValue" :min="0" placeholder="奖品价值(元)" style="width: 100%" />
        </NFormItem>
        <NFormItem label="总数量" required>
          <NInputNumber v-model:value="prizeModal.form.totalQuantity" :min="-1" placeholder="-1表示不限量" style="width: 100%" />
        </NFormItem>
        <NFormItem label="中奖概率" required>
          <NInputNumber v-model:value="prizeModal.form.winProbability" :min="0" :max="1" :step="0.01" placeholder="0-1之间,例如0.1表示10%" style="width: 100%" />
        </NFormItem>
        <NFormItem label="奖品等级">
          <NInputNumber v-model:value="prizeModal.form.prizeLevel" :min="1" placeholder="数字越小等级越高" style="width: 100%" />
        </NFormItem>
      </NForm>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="prizeModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="prizeModal.loading" @click="savePrize">保存</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue';
import {
  NButton,
  NDataTable,
  NSelect,
  NSpace,
  NTag,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NDatePicker,
  NTabs,
  NTabPane,
  NUpload,
  NImage,
  NText,
  useMessage,
  type DataTableColumns,
  type PaginationProps,
  type UploadCustomRequestOptions
} from 'naive-ui';
import {
  fetchActivities,
  createActivity,
  updateActivity,
  deleteActivity,
  updateActivityStatus,
  fetchActivityPrizes,
  addActivityPrize,
  updateActivityPrize,
  deleteActivityPrize,
  uploadImage,
  type ActivityDetail,
  type ActivityCreatePayload,
  type PrizeConfigPayload
} from '@/api/modules';

const message = useMessage();

const activities = ref<ActivityDetail[]>([]);
const loading = ref(false);
const imageUploading = ref(false);

const queryParams = reactive({
  activityType: null as string | null,
  status: null as number | null
});

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showQuickJumper: true,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  prefix: ({ itemCount }) => `共 ${itemCount ?? 0} 条`,
  onChange: (page: number) => {
    pagination.page = page;
    loadActivities();
  },
  onUpdatePageSize: (size: number) => {
    pagination.pageSize = size;
    pagination.page = 1;
    loadActivities();
  }
});

const activityModal = reactive({
  show: false,
  loading: false,
  isEdit: false,
  activeTab: 'basic',
  imagePreviewUrl: '',
  form: {
    id: undefined as number | undefined,
    activityName: '',
    activityType: '',
    activityDesc: '',
    bannerImage: '',
    startTime: null as number | null,
    endTime: null as number | null,
    participateLimit: 0,
    limitType: 'UNLIMITED',
    activityConfig: '{}',
    sortOrder: 0
  }
});

const prizes = ref<any[]>([]);
const prizesLoading = ref(false);

const prizeModal = reactive({
  show: false,
  loading: false,
  isEdit: false,
  form: {
    id: undefined as number | undefined,
    prizeName: '',
    prizeType: 'COUPON',
    prizeValue: 0,
    totalQuantity: 100,
    winProbability: 0.1,
    prizeLevel: 1
  }
});

const activityTypeOptions = [
  { label: '全部类型', value: null },
  { label: '抽奖活动', value: 'LOTTERY' },
  { label: '大转盘', value: 'WHEEL' },
  { label: '优惠券', value: 'COUPON' },
  { label: '积分兑换', value: 'POINTS_EXCHANGE' },
  { label: '签到', value: 'SIGN_IN' },
  { label: '拼团', value: 'GROUP_BUY' }
];

const statusOptions = [
  { label: '全部状态', value: null },
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 },
  { label: '已暂停', value: 3 }
];

const limitTypeOptions = [
  { label: '不限制', value: 'UNLIMITED' },
  { label: '每日限制', value: 'DAILY' },
  { label: '总次数限制', value: 'TOTAL' }
];

const prizeTypeOptions = [
  { label: '优惠券', value: 'COUPON' },
  { label: '积分', value: 'POINTS' },
  { label: '实物', value: 'PHYSICAL' },
  { label: '谢谢参与', value: 'NONE' }
];

const getActivityTypeLabel = (type: string) => {
  const option = activityTypeOptions.find(o => o.value === type);
  return option?.label || type;
};

const getStatusTag = (status: number) => {
  const statusMap = {
    0: { type: 'default', label: '未开始' },
    1: { type: 'success', label: '进行中' },
    2: { type: 'info', label: '已结束' },
    3: { type: 'warning', label: '已暂停' }
  };
  return statusMap[status as keyof typeof statusMap] || { type: 'default', label: '未知' };
};

const columns: DataTableColumns<ActivityDetail> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '活动名称', key: 'activityName', ellipsis: { tooltip: true } },
  {
    title: '活动类型',
    key: 'activityType',
    width: 120,
    render: (row) => getActivityTypeLabel(row.activityType)
  },
  {
    title: '开始时间',
    key: 'startTime',
    width: 180,
    render: (row) => new Date(row.startTime).toLocaleString('zh-CN')
  },
  {
    title: '结束时间',
    key: 'endTime',
    width: 180,
    render: (row) => new Date(row.endTime).toLocaleString('zh-CN')
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const tag = getStatusTag(row.status);
      return h(
        NTag,
        { type: tag.type as any, bordered: false },
        { default: () => tag.label }
      );
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    fixed: 'right',
    render: (row) =>
      h(
        NSpace,
        { size: 8 },
        {
          default: () => [
            h(
              NButton,
              { size: 'small', tertiary: true, onClick: () => handleEdit(row) },
              { default: () => '编辑' }
            ),
            row.status === 1
              ? h(
                  NButton,
                  {
                    size: 'small',
                    tertiary: true,
                    type: 'warning',
                    onClick: () => handleUpdateStatus(row.id, 3)
                  },
                  { default: () => '暂停' }
                )
              : h(
                  NButton,
                  {
                    size: 'small',
                    tertiary: true,
                    type: 'success',
                    onClick: () => handleUpdateStatus(row.id, 1)
                  },
                  { default: () => '启动' }
                ),
            h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                type: 'error',
                onClick: () => handleDelete(row.id)
              },
              { default: () => '删除' }
            )
          ]
        }
      )
  }
];

const prizeColumns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '奖品名称', key: 'prizeName' },
  { title: '类型', key: 'prizeType', width: 100 },
  { title: '价值', key: 'prizeValue', width: 80 },
  { title: '总数量', key: 'totalQuantity', width: 80 },
  { title: '剩余', key: 'remainQuantity', width: 80 },
  { title: '概率', key: 'winProbability', width: 80, render: (row) => `${(row.winProbability * 100).toFixed(1)}%` },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row) =>
      h(
        NSpace,
        { size: 8 },
        {
          default: () => [
            h(
              NButton,
              { size: 'small', tertiary: true, onClick: () => handleEditPrize(row) },
              { default: () => '编辑' }
            ),
            h(
              NButton,
              { size: 'small', tertiary: true, type: 'error', onClick: () => handleDeletePrize(row.id) },
              { default: () => '删除' }
            )
          ]
        }
      )
  }
];

const loadActivities = async () => {
  loading.value = true;
  try {
    const result = await fetchActivities({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      activityType: queryParams.activityType || undefined,
      status: queryParams.status ?? undefined
    });
    activities.value = result.data?.records || [];
    pagination.itemCount = result.data?.total || 0;
  } catch (error: any) {
    message.error(error.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const loadPrizes = async (activityId: number) => {
  prizesLoading.value = true;
  try {
    const result = await fetchActivityPrizes(activityId);
    prizes.value = result.data || [];
  } catch (error: any) {
    message.error(error.message || '加载奖品失败');
  } finally {
    prizesLoading.value = false;
  }
};

const resetForm = () => {
  activityModal.form = {
    id: undefined,
    activityName: '',
    activityType: '',
    activityDesc: '',
    bannerImage: '',
    startTime: null,
    endTime: null,
    participateLimit: 0,
    limitType: 'UNLIMITED',
    activityConfig: '{}',
    sortOrder: 0
  };
  activityModal.imagePreviewUrl = '';
  activityModal.activeTab = 'basic';
  prizes.value = [];
};

const handleCreate = () => {
  resetForm();
  activityModal.isEdit = false;
  activityModal.show = true;
};

const handleEdit = async (row: ActivityDetail) => {
  activityModal.form = {
    id: row.id,
    activityName: row.activityName,
    activityType: row.activityType,
    activityDesc: row.activityDesc || '',
    bannerImage: row.bannerImage || '',
    startTime: new Date(row.startTime).getTime(),
    endTime: new Date(row.endTime).getTime(),
    participateLimit: row.participateLimit,
    limitType: row.limitType,
    activityConfig: row.activityConfig,
    sortOrder: row.sortOrder
  };
  activityModal.imagePreviewUrl = row.bannerImage || '';
  activityModal.isEdit = true;
  activityModal.activeTab = 'basic';
  activityModal.show = true;
  
  // 加载奖品
  if (row.id) {
    await loadPrizes(row.id);
  }
};

const handleImageUpload = async (options: UploadCustomRequestOptions) => {
  const { file, onFinish, onError } = options;
  if (!file.file) {
    onError?.();
    return;
  }
  imageUploading.value = true;
  try {
    const result = await uploadImage(file.file as File);
    const uploadResult = result.data as { objectKey: string; presignedUrl: string };
    activityModal.form.bannerImage = uploadResult.objectKey;
    activityModal.imagePreviewUrl = uploadResult.presignedUrl;
    message.success('图片上传成功');
    onFinish?.();
  } catch (error) {
    message.error((error as Error).message);
    onError?.();
  } finally {
    imageUploading.value = false;
  }
};

const saveActivity = async () => {
  if (!activityModal.form.activityName) {
    message.warning('请输入活动名称');
    return;
  }
  if (!activityModal.form.activityType) {
    message.warning('请选择活动类型');
    return;
  }
  if (!activityModal.form.startTime || !activityModal.form.endTime) {
    message.warning('请选择开始和结束时间');
    return;
  }

  activityModal.loading = true;
  try {
    const payload: ActivityCreatePayload = {
      activityName: activityModal.form.activityName,
      activityType: activityModal.form.activityType,
      activityDesc: activityModal.form.activityDesc,
      bannerImage: activityModal.form.bannerImage,
      startTime: new Date(activityModal.form.startTime).toISOString(),
      endTime: new Date(activityModal.form.endTime).toISOString(),
      participateLimit: activityModal.form.participateLimit,
      limitType: activityModal.form.limitType,
      activityConfig: activityModal.form.activityConfig,
      sortOrder: activityModal.form.sortOrder
    };

    if (activityModal.isEdit && activityModal.form.id) {
      await updateActivity(activityModal.form.id, payload);
      message.success('活动已更新');
    } else {
      const result = await createActivity(payload);
      message.success('活动已创建,请继续添加奖品');
      // 创建成功后,切换到编辑模式并加载奖品
      activityModal.form.id = result.data;
      activityModal.isEdit = true;
      activityModal.activeTab = 'prizes';
      await loadPrizes(result.data);
    }

    await loadActivities();
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    activityModal.loading = false;
  }
};

const handleUpdateStatus = async (id: number, status: number) => {
  try {
    await updateActivityStatus(id, status);
    message.success('状态更新成功');
    await loadActivities();
  } catch (error: any) {
    message.error(error.message || '更新失败');
  }
};

const handleDelete = async (id: number) => {
  if (!window.confirm('确定要删除这个活动吗?')) {
    return;
  }
  try {
    await deleteActivity(id);
    message.success('活动已删除');
    await loadActivities();
  } catch (error: any) {
    message.error(error.message || '删除失败');
  }
};

// 奖品管理
const resetPrizeForm = () => {
  prizeModal.form = {
    id: undefined,
    prizeName: '',
    prizeType: 'COUPON',
    prizeValue: 0,
    totalQuantity: 100,
    winProbability: 0.1,
    prizeLevel: 1
  };
};

const handleAddPrize = () => {
  resetPrizeForm();
  prizeModal.isEdit = false;
  prizeModal.show = true;
};

const handleEditPrize = (row: any) => {
  prizeModal.form = {
    id: row.id,
    prizeName: row.prizeName,
    prizeType: row.prizeType,
    prizeValue: row.prizeValue || 0,
    totalQuantity: row.totalQuantity,
    winProbability: row.winProbability,
    prizeLevel: row.prizeLevel
  };
  prizeModal.isEdit = true;
  prizeModal.show = true;
};

const savePrize = async () => {
  if (!activityModal.form.id) {
    message.warning('请先保存活动');
    return;
  }
  if (!prizeModal.form.prizeName) {
    message.warning('请输入奖品名称');
    return;
  }

  prizeModal.loading = true;
  try {
    const payload: PrizeConfigPayload = {
      prizeName: prizeModal.form.prizeName,
      prizeType: prizeModal.form.prizeType,
      prizeValue: prizeModal.form.prizeValue,
      totalQuantity: prizeModal.form.totalQuantity,
      winProbability: prizeModal.form.winProbability,
      prizeLevel: prizeModal.form.prizeLevel
    };

    if (prizeModal.isEdit && prizeModal.form.id) {
      await updateActivityPrize(prizeModal.form.id, payload);
      message.success('奖品已更新');
    } else {
      await addActivityPrize(activityModal.form.id!, payload);
      message.success('奖品已添加');
    }

    prizeModal.show = false;
    await loadPrizes(activityModal.form.id!);
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    prizeModal.loading = false;
  }
};

const handleDeletePrize = async (id: number) => {
  if (!window.confirm('确定要删除这个奖品吗?')) {
    return;
  }
  try {
    await deleteActivityPrize(id);
    message.success('奖品已删除');
    if (activityModal.form.id) {
      await loadPrizes(activityModal.form.id);
    }
  } catch (error: any) {
    message.error(error.message || '删除失败');
  }
};

// 监听活动ID变化,自动加载奖品
watch(() => activityModal.form.id, (newId) => {
  if (newId && activityModal.activeTab === 'prizes') {
    loadPrizes(newId);
  }
});

onMounted(() => {
  loadActivities();
});
</script>

<style scoped>
.marketing-view {
  padding: 0;
}

.table-header {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-top: 24px;
  margin-bottom: 24px;
}

.table-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  flex: 1;
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

.image-upload {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.image-preview {
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.prizes-section {
  padding: 16px 0;
}

.prizes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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
