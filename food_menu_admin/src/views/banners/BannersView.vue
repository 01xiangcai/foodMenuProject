<template>
  <div class="banners-view">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <h2>轮播图管理</h2>
        <div class="table-actions">
          <NSelect
            v-if="isSuperAdmin"
            v-model:value="selectedFamilyId"
            :options="familyFilterOptions"
            placeholder="选择家庭"
            clearable
            style="width: 180px"
            @update:value="handleFamilyFilterChange"
          />
          <NButton secondary @click="loadBanners">刷新</NButton>
          <NButton class="primary-soft" type="primary" @click="openBannerModal()">新增轮播图</NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="banners"
        :loading="loading"
        :pagination="pagination"
        size="large"
      />
    </section>

    <NModal v-model:show="bannerModal.show" preset="card" :mask-closable="false" style="max-width: 600px">
      <template #header>
        {{ bannerModal.form.id ? '编辑轮播图' : '新增轮播图' }}
      </template>
      <NSpin :show="bannerModal.fetching">
        <NForm :model="bannerModal.form" label-placement="left" label-width="100" @submit.prevent>
          <NFormItem label="轮播图图片" required>
            <div class="image-upload">
              <NImage
                v-if="bannerModal.imagePreviewUrl || bannerModal.form.image"
                class="image-preview"
                :src="bannerModal.imagePreviewUrl || bannerModal.form.image"
                width="200"
                height="100"
                object-fit="cover"
              />
              <NUpload
                :custom-request="handleImageUpload"
                :show-file-list="false"
                accept="image/*"
                :disabled="imageUploading"
              >
                <NUploadDragger>
                  <div class="upload-inner">
                    <p>{{ imageUploading ? '上传中...' : '点击或拖拽上传' }}</p>
                    <p class="upload-tip">支持 jpg/png，建议尺寸 800x400</p>
                  </div>
                </NUploadDragger>
              </NUpload>
            </div>
          </NFormItem>
          <NFormItem label="标题">
            <NInput v-model:value="bannerModal.form.title" placeholder="例如：家庭美食精选" />
          </NFormItem>
          <NFormItem label="描述">
            <NInput
              v-model:value="bannerModal.form.description"
              type="textarea"
              :rows="2"
              placeholder="例如：每日新鲜食材，用心烹饪每一道菜"
            />
          </NFormItem>
          <NFormItem label="链接地址">
            <NInput v-model:value="bannerModal.form.linkUrl" placeholder="例如：/pages/menu/menu（可选）" />
          </NFormItem>
          <NFormItem label="排序号">
            <NInputNumber v-model:value="bannerModal.form.sort" :min="1" placeholder="数字越小越靠前" />
          </NFormItem>
          <NFormItem label="所属家庭" v-if="isSuperAdmin" required>
            <NSelect
              v-model:value="bannerModal.form.familyId"
              :options="familyOptions"
              placeholder="请选择家庭"
            />
          </NFormItem>
          <NFormItem label="状态">
            <NSwitch v-model:value="bannerModal.form.status" :checked-value="1" :unchecked-value="0">
              <template #checked>启用</template>
              <template #unchecked>禁用</template>
            </NSwitch>
          </NFormItem>
        </NForm>
      </NSpin>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="bannerModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="bannerModal.loading" @click="saveBanner">保存</NButton>
        </div>
      </template>
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
  NImage,
  NInput,
  NInputNumber,
  NModal,
  NSelect,
  NSpace,
  NSpin,
  NSwitch,
  NTag,
  NUpload,
  NUploadDragger,
  useMessage,
  type DataTableColumns,
  type PaginationProps,
  type UploadCustomRequestOptions
} from 'naive-ui';
import {
  createBanner,
  fetchBannerDetail,
  fetchBanners,
  removeBanner,
  updateBanner,
  uploadImage,
  fetchAllFamilies,
  fetchProfile,
  type BannerPayload
} from '@/api/modules';

type BannerRecord = {
  id: number;
  image: string;
  title?: string;
  description?: string;
  linkUrl?: string;
  sort: number;
  status: number;
};

const message = useMessage();

// 家庭相关
const families = ref<any[]>([]);
const currentUserRole = ref<number | null>(null);
const currentUserFamilyId = ref<number | null>(null);
const selectedFamilyId = ref<number | null>(null); // 家庭筛选器选中的家庭ID
const isSuperAdmin = computed(() => currentUserRole.value === 2);

const banners = ref<BannerRecord[]>([]);
const loading = ref(false);
const imageUploading = ref(false);

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
    loadBanners();
  },
  onUpdatePageSize: (size: number) => {
    pagination.pageSize = size;
    pagination.page = 1;
    loadBanners();
  }
});

const bannerModal = reactive({
  show: false,
  loading: false,
  fetching: false,
  imagePreviewUrl: '',
  form: {
    id: undefined as number | undefined,
    image: '',
    title: '',
    description: '',
    linkUrl: '',
    sort: 1,
    status: 1,
    familyId: null as number | null // 家庭ID（超级管理员可以设置）
  }
});

const columns: DataTableColumns<BannerRecord> = [
  {
    title: '图片',
    key: 'image',
    width: 120,
    render: (row) =>
      h(NImage, {
        src: row.image,
        width: 100,
        height: 50,
        objectFit: 'cover',
        style: { borderRadius: '8px' }
      })
  },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'warning', bordered: false },
        { default: () => (row.status === 1 ? '启用' : '禁用') }
      )
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) =>
      h(
        NSpace,
        { size: 8 },
        {
          default: () => [
            h(
              NButton,
              { size: 'small', tertiary: true, onClick: () => openBannerModal(row.id) },
              { default: () => '编辑' }
            ),
            h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                type: 'error',
                onClick: () => handleDeleteBanner(row.id)
              },
              { default: () => '删除' }
            )
          ]
        }
      )
  }
];

const resetBannerForm = () => {
  bannerModal.form.id = undefined;
  bannerModal.form.image = '';
  bannerModal.form.title = '';
  bannerModal.form.description = '';
  bannerModal.form.linkUrl = '';
  bannerModal.form.sort = 1;
  bannerModal.form.status = 1;
  // 非超级管理员默认使用自己的家庭ID
  bannerModal.form.familyId = isSuperAdmin.value ? null : currentUserFamilyId.value;
  bannerModal.imagePreviewUrl = '';
};

const openBannerModal = async (id?: number) => {
  bannerModal.show = true;
  bannerModal.fetching = Boolean(id);
  resetBannerForm();
  if (!id) {
    bannerModal.fetching = false;
    return;
  }
  try {
    const result = await fetchBannerDetail(id);
    const banner = result.data;
    bannerModal.form.id = banner.id;
    bannerModal.form.image = banner.image || '';
    bannerModal.form.title = banner.title || '';
    bannerModal.form.description = banner.description || '';
    bannerModal.form.linkUrl = banner.linkUrl || '';
    bannerModal.form.sort = banner.sort || 1;
    bannerModal.form.status = banner.status ?? 1;
    bannerModal.form.familyId = (banner as any).familyId || null;
    // If image is a presigned URL, extract object key and set preview
    if (banner.image && banner.image.includes('?Expires=')) {
      const urlObj = new URL(banner.image);
      const objectKey = urlObj.pathname.substring(1);
      bannerModal.form.image = objectKey;
      bannerModal.imagePreviewUrl = banner.image;
    } else if (banner.image && (banner.image.startsWith('http://') || banner.image.startsWith('https://'))) {
      bannerModal.form.image = banner.image;
      bannerModal.imagePreviewUrl = banner.image;
    } else {
      bannerModal.form.image = banner.image || '';
    }
  } finally {
    bannerModal.fetching = false;
  }
};

const saveBanner = async () => {
  if (!bannerModal.form.image) {
    message.warning('请上传轮播图图片');
    return;
  }
  // 超级管理员必须选择家庭
  if (isSuperAdmin.value && !bannerModal.form.familyId) {
    message.warning('请选择所属家庭');
    return;
  }
  bannerModal.loading = true;
  try {
    const payload: BannerPayload = {
      id: bannerModal.form.id,
      image: bannerModal.form.image,
      title: bannerModal.form.title || undefined,
      description: bannerModal.form.description || undefined,
      linkUrl: bannerModal.form.linkUrl || undefined,
      sort: bannerModal.form.sort || 1,
      status: bannerModal.form.status,
      // 超级管理员可以选择家庭，非超级管理员使用自己的家庭ID
      familyId: isSuperAdmin.value 
        ? (bannerModal.form.familyId ?? undefined)
        : (currentUserFamilyId.value ?? undefined)
    };
    if (payload.id) {
      await updateBanner(payload);
      message.success('轮播图已更新');
    } else {
      await createBanner(payload);
      message.success('轮播图已创建');
    }
    bannerModal.show = false;
    await loadBanners();
  } catch (error: any) {
    message.error(error.message || '操作失败');
  } finally {
    bannerModal.loading = false;
  }
};

const handleDeleteBanner = async (id: number) => {
  if (!window.confirm('确定要删除这个轮播图吗？')) {
    return;
  }
  try {
    await removeBanner(id);
    message.success('轮播图已删除');
    await loadBanners();
  } catch (error: any) {
    message.error(error.message || '删除失败');
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
    bannerModal.form.image = uploadResult.objectKey;
    bannerModal.imagePreviewUrl = uploadResult.presignedUrl;
    message.success('图片上传成功');
    onFinish?.();
  } catch (error) {
    message.error((error as Error).message);
    onError?.();
  } finally {
    imageUploading.value = false;
  }
};

const loadBanners = async () => {
  loading.value = true;
  try {
    const result = await fetchBanners({
      page: pagination.page,
      pageSize: pagination.pageSize,
      familyId: isSuperAdmin.value ? (selectedFamilyId.value ?? undefined) : undefined
    });
    banners.value = result.data?.records || [];
    pagination.itemCount = result.data?.total || 0;
  } finally {
    loading.value = false;
  }
};

// 家庭筛选器变化处理
const handleFamilyFilterChange = () => {
  pagination.page = 1;
  loadBanners();
};

// 家庭筛选选项（用于筛选轮播图列表）
const familyFilterOptions = computed(() => {
  return [
    { label: '全部家庭', value: null },
    ...families.value.map(f => ({
      label: f.name,
      value: f.id
    }))
  ];
});

// 家庭选项（用于添加/编辑轮播图）
const familyOptions = computed(() => {
  return families.value.map(f => ({
    label: f.name,
    value: f.id
  }));
});

// 加载家庭列表
const loadFamilies = async () => {
  try {
    const result = await fetchAllFamilies();
    families.value = result.data || [];
  } catch (error) {
    console.error('加载家庭列表失败:', error);
  }
};

// 加载当前用户信息
const loadCurrentUser = async () => {
  try {
    const result = await fetchProfile();
    currentUserRole.value = result.data?.role ?? null;
    currentUserFamilyId.value = result.data?.familyId ?? null;
  } catch (error) {
    console.error('加载用户信息失败:', error);
  }
};

onMounted(async () => {
  await loadCurrentUser();
  if (isSuperAdmin.value) {
    await loadFamilies();
  }
  await loadBanners();
});
</script>

<style scoped>
.banners-view {
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
}

.table-header p {
  margin: 0;
  opacity: 0.7;
  font-size: 14px;
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
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}

.upload-inner {
  color: #cbd5f5;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
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

