<template>
  <div class="menu-grid">
    <section class="glass-card hover-rise categories-card">
      <div class="section-header">
        <div>
          <h2>菜品分类</h2>
        </div>
        <NButton class="primary-soft" size="small" type="primary" @click="openCategoryModal()">新增分类</NButton>
      </div>
      <div class="category-list" v-if="categories.length">
        <!-- 全部选项 -->
        <button
          class="category-item"
          :class="{ active: selectedCategoryId === null }"
          @click="selectCategory(null)"
        >
          <div>
            <strong>全部</strong>
            <p>查看所有菜品</p>
          </div>
        </button>
        
        <!-- 分类列表 -->
        <button
          v-for="category in categories"
          :key="category.id"
          class="category-item"
          :class="{ active: category.id === selectedCategoryId }"
          @click="selectCategory(category.id)"
        >
          <div>
            <strong>{{ category.name }} ({{ category.dishCount || 0 }})</strong>
            <p>排序 {{ category.sort ?? '-' }}</p>
          </div>
          <div class="category-actions">
            <NButton size="tiny" tertiary @click.stop="openCategoryModal(category)">编辑</NButton>
            <NButton size="tiny" tertiary type="error" @click.stop="handleDeleteCategory(category.id)">删除</NButton>
          </div>
        </button>
      </div>
      <div v-else class="empty-hint">还没有分类，先添加一个吧～</div>
    </section>

    <section class="glass-card hover-rise dishes-card">
      <div class="table-header">
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
          <NInput
            v-model:value="keyword"
            clearable
            placeholder="搜索菜名"
            style="width: 200px"
            @keydown.enter.prevent="refreshDishes"
          />
          <NButton secondary @click="refreshDishes">刷新</NButton>
          <NButton class="primary-soft" type="primary" @click="openDishModal()">新增菜品</NButton>
        </div>
      </div>
      <NDataTable
        :columns="columns"
        :data="dishes"
        :loading="dishLoading"
        :pagination="pagination"
        :remote="true"
        size="large"
      />
    </section>

    <NModal v-model:show="categoryModal.show" preset="card" style="max-width: 420px">
      <template #header>
        {{ categoryModal.form.id ? '编辑分类' : '新增分类' }}
      </template>
      <NForm :model="categoryModal.form" label-placement="left" label-width="80" @submit.prevent>
        <NFormItem label="分类名称">
          <NInput v-model:value="categoryModal.form.name" placeholder="家常菜" />
        </NFormItem>
        <NFormItem label="排序号">
          <NInputNumber v-model:value="categoryModal.form.sort" :min="1" />
        </NFormItem>
      </NForm>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="categoryModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="categoryModal.loading" @click="saveCategory">保存</NButton>
        </div>
      </template>
    </NModal>

    <NModal v-model:show="dishModal.show" preset="card" :mask-closable="false" style="max-width: 520px">
      <template #header>
        {{ dishModal.form.id ? '编辑菜品' : '新增菜品' }}
      </template>
      <NSpin :show="dishModal.fetching">
        <NForm ref="dishFormRef" :model="dishModal.form" :rules="dishFormRules" label-placement="left" label-width="80" @submit.prevent="saveDish">
          <NFormItem label="菜品名称" path="name" required>
            <NInput v-model:value="dishModal.form.name" placeholder="番茄炒蛋" />
          </NFormItem>
          <NFormItem label="所属分类" path="categoryIds" required>
            <NSelect
              v-model:value="dishModal.form.categoryIds"
              :options="categoryOptions"
              placeholder="请选择分类"
              multiple
              filterable
              max-tag-count="responsive"
            />
          </NFormItem>
          <NFormItem label="所属家庭" v-if="isSuperAdmin" path="familyId" required>
            <NSelect
              v-model:value="dishModal.form.familyId"
              :options="familyOptions"
              placeholder="请选择家庭"
            />
          </NFormItem>
          <NFormItem label="价格 (元)" path="price" required>
            <NInputNumber v-model:value="dishModal.form.price" :min="0" :precision="2" placeholder="例如 28" />
          </NFormItem>
          <NFormItem label="菜品图片" path="localImagesArray" required>
            <div class="multi-image-upload">
              <div class="image-list">
                <div 
                  v-for="(img, realIndex) in validImageList" 
                  :key="`img-${realIndex}-${img.path}`"
                  class="image-item"
                  :class="{ 'is-main': img.path === dishModal.form.localImage }"
                >
                  <NImage :src="img.url" width="120" height="120" object-fit="cover" />
                  <div class="image-actions">
                    <NButton 
                      size="tiny" 
                      type="primary" 
                      v-if="img.path !== dishModal.form.localImage"
                      @click="setMainImage(img.path)"
                    >
                      设为主图
                    </NButton>
                    <NButton 
                      size="tiny" 
                      type="error" 
                      @click="removeImage(realIndex)"
                    >
                      删除
                    </NButton>
                  </div>
                  <div v-if="img.path === dishModal.form.localImage" class="main-badge">主图</div>
                  <div v-if="img.isPending" class="pending-badge">待上传</div>
                </div>
                
                <NUpload
                  v-if="validImageCount < imageLimit"
                  :custom-request="handleImageUpload"
                  :show-file-list="false"
                  accept="image/*"
                  :disabled="imageUploading"
                >
                  <div class="upload-placeholder">
                    <div class="upload-icon">+</div>
                    <div class="upload-text">上传图片</div>
                    <div class="upload-tip">
                      {{ validImageCount }}/{{ imageLimit }}
                    </div>
                  </div>
                </NUpload>
              </div>
              <div class="image-limit-tip" v-if="validImageCount >= imageLimit">
                已达到最大上传数量（{{ imageLimit }}张）
              </div>
            </div>
          </NFormItem>
          <NFormItem label="上架状态">
            <NSwitch v-model:value="dishModal.form.status" :checked-value="'on'" :unchecked-value="'off'">
              <template #checked>在用</template>
              <template #unchecked>下架</template>
            </NSwitch>
          </NFormItem>
          <NFormItem label="口味标签">
            <NInput
              v-model:value="dishModal.form.flavorText"
              placeholder="用逗号或空格分隔，如 微辣 少油"
            />
          </NFormItem>
          <NFormItem label="卡路里">
            <NInput
              v-model:value="dishModal.form.calories"
              placeholder="例如: 482 kcal"
            />
          </NFormItem>
          <NFormItem label="菜品标签">
            <NSelect
              v-model:value="dishModal.form.tagsArray"
              :options="tagOptions"
              multiple
              filterable
              placeholder="选择菜品标签"
              :loading="tagsLoading"
            />
          </NFormItem>
          <NFormItem label="家庭备注">
            <NInput
              v-model:value="dishModal.form.description"
              placeholder="喜欢的配菜、特别提醒..."
              type="textarea"
              :rows="3"
            />
          </NFormItem>
        </NForm>
      </NSpin>
      <template #action>
        <div class="modal-actions">
          <NButton quaternary @click="dishModal.show = false">取消</NButton>
          <NButton class="primary-soft" type="primary" :loading="dishModal.loading" @click="saveDish">保存</NButton>
        </div>
      </template>
    </NModal>

    <!-- 详情弹窗 -->
    <NModal v-model:show="detailModal.show" preset="card" style="max-width: 400px; max-height: 100vh" :title="detailModal.data?.name">
      <div v-if="detailModal.data" class="dish-detail">
        <div class="detail-image-wrapper">
          <NCarousel 
            v-if="getDetailImages().length > 0"
            :show-dots="getDetailImages().length > 1"
            :show-arrow="getDetailImages().length > 1"
            :autoplay="getDetailImages().length > 1"
            :interval="3000"
            :duration="500"
            :loop="getDetailImages().length > 1"
            style="width: 100%; height: 200px;"
          >
            <img
              v-for="(img, index) in getDetailImages()"
              :key="index"
              :src="img"
              class="detail-image"
            />
          </NCarousel>
          <div class="detail-price">¥{{ Number(detailModal.data.price).toFixed(2) }}</div>
        </div>
        
        <div class="detail-content">
          <div class="detail-section">
            <div class="section-label">标签</div>
            <div class="detail-tags" v-if="detailModal.data.tags">
              <NTag size="small" type="primary" v-for="tag in detailModal.data.tags.split(/[,，]/)" :key="tag">
                {{ tag }}
              </NTag>
            </div>
            <div class="section-text" v-else>暂无</div>
          </div>
          
          <div class="detail-section">
            <div class="section-label">家庭备注</div>
            <div class="section-text">{{ detailModal.data.description || '暂无' }}</div>
          </div>
          
          <div class="detail-section">
            <div class="section-label">能量</div>
            <div class="section-text">{{ detailModal.data.calories || '暂无' }}</div>
          </div>

          <div class="detail-section">
            <div class="section-label">口味</div>
            <div class="section-text">{{ parseFlavorText(detailModal.data.flavors) || '暂无' }}</div>
          </div>

          <div class="detail-stats">
            <div class="stat-box">
              <div class="stat-value">
                {{ 
                  detailModal.data.categoryNames && detailModal.data.categoryNames.length > 0
                    ? detailModal.data.categoryNames.join(', ')
                    : (detailModal.data.categoryName || lookupCategoryName(detailModal.data.categoryId))
                }}
              </div>
              <div class="stat-label">所属分类</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ detailModal.data.status === 1 ? '在售' : '下架' }}</div>
              <div class="stat-label">当前状态</div>
            </div>
          </div>
        </div>
      </div>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, watch } from 'vue';
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
  NCarousel,
  useMessage,
  type DataTableColumns,
  type PaginationProps,
  type UploadCustomRequestOptions
} from 'naive-ui';
import {
  createCategory,
  createDish,
  fetchCategories,
  fetchDishDetail,
  fetchDishes,
  removeCategory,
  removeDish,
  updateCategory,
  updateDish,
  uploadImage,
  fetchAllDishTags,
  fetchSystemConfig,
  fetchAllFamilies,
  fetchProfile
} from '@/api/modules';

type Category = {
  id: number;
  name: string;
  sort?: number;
  dishCount?: number;
};

type DishFlavor = {
  name: string;
  value: string;
};

type DishRecord = {
  id: number;
  name: string;
  price: number | string;
  status: number;
  categoryId: number;
  categoryName?: string;
  description?: string;
  flavors?: DishFlavor[];
  image?: string;
  calories?: string;
  tags?: string;
  localImage?: string;
  localImages?: string;
  localImagesArray?: string[];
  familyId?: number;
};

type DishForm = {
  id?: number;
  name: string;
  categoryIds: number[]; // 改为数组支持多分类
  price: number | null;
  status: 'on' | 'off';
  description: string;
  flavorText: string;
  image: string;
  calories: string;
  tags: string;
};

const message = useMessage();

// 表单引用
const dishFormRef = ref<InstanceType<typeof NForm> | null>(null);

// 表单校验规则（动态计算，根据是否为超级管理员）
const dishFormRules = computed(() => ({
  name: {
    required: true,
    message: '请输入菜品名称',
    trigger: ['blur', 'input']
  },
  categoryIds: {
    required: true,
    type: 'array',
    message: '请至少选择一个分类',
    trigger: ['blur', 'change']
  },
  ...(isSuperAdmin.value ? {
    familyId: {
      required: true,
      type: 'number',
      message: '请选择所属家庭',
      trigger: ['blur', 'change']
    }
  } : {}),
  price: {
    required: true,
    type: 'number',
    message: '请输入价格',
    trigger: ['blur', 'input'],
    validator: (rule: any, value: number | null) => {
      if (value === null || value === undefined) {
        return new Error('请输入价格');
      }
      if (value < 0) {
        return new Error('价格不能小于0');
      }
      return true;
    }
  },
  localImagesArray: {
    required: true,
    type: 'array',
    message: '请至少上传一张图片',
    trigger: ['blur'],
    validator: (rule: any, value: string[]) => {
      // 检查已上传的图片
      const validImages = value?.filter(path => path && path.trim()) || [];
      // 检查待上传的图片
      const pendingCount = dishModal.pendingFiles.length;
      // 总图片数量
      const totalCount = validImages.length + pendingCount;
      if (totalCount === 0) {
        return new Error('请至少上传一张图片');
      }
      return true;
    }
  }
}));

const categories = ref<Category[]>([]);
const selectedCategoryId = ref<number | null>(null);
const keyword = ref('');

// 家庭相关
const families = ref<any[]>([]);
const currentUserRole = ref<number | null>(null);
const currentUserFamilyId = ref<number | null>(null);
const selectedFamilyId = ref<number | null>(null); // 家庭筛选器选中的家庭ID
const isSuperAdmin = computed(() => currentUserRole.value === 2);

const dishes = ref<DishRecord[]>([]);
const dishLoading = ref(false);

// 标签相关
const dishTags = ref<Array<{ id: number; name: string; icon: string }>>([]);
const tagsLoading = ref(false);
const tagOptions = computed(() => {
  return dishTags.value.map(tag => ({
    label: `${tag.icon} ${tag.name}`,
    value: tag.name
  }));
});

// 图片数量限制
const imageLimit = ref(5);
const imageUploading = ref(false);

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showQuickJumper: true,
  showSizePicker: true,
  pageSizes: [5, 10, 20],
  prefix: ({ itemCount }) => `共 ${itemCount ?? 0} 道菜`,
  onChange: (page: number) => {
    pagination.page = page;
    loadDishes();
  },
  onUpdatePageSize: (size: number) => {
    pagination.pageSize = size;
    pagination.page = 1;
    loadDishes();
  }
});

const categoryModal = reactive({
  show: false,
  loading: false,
  form: {
    id: undefined as number | undefined,
    name: '',
    sort: 1
  }
});

const dishModal = reactive({
  show: false,
  loading: false,
  fetching: false,
  form: {
    id: undefined as number | undefined,
    name: '',
    categoryIds: [] as number[], // 改为数组支持多分类
    price: null as number | null,
    status: 'on' as 'on' | 'off',
    description: '',
    flavorText: '',
    image: '', // Store object key (relative path) for database (OSS)
    localImage: '', // Store local image path (主图)
    localImages: '', // Store JSON string of all images
    localImagesArray: [] as string[], // 图片数组，用于前端操作
    calories: '',
    tags: '',
    tagsArray: [] as string[], // 标签数组，用于多选
    familyId: null as number | null // 家庭ID（超级管理员可以设置）
  },
  imagePreviewUrls: [] as string[], // Store presigned URLs for preview
  pendingFiles: [] as Array<{ file: File; previewUrl: string; index: number }> // 待上传的文件列表
});

// 家庭选项（用于添加/编辑菜品）
const familyOptions = computed(() => {
  return families.value.map(f => ({
    label: f.name,
    value: f.id
  }));
});

// 家庭筛选选项（用于筛选菜品列表）
const familyFilterOptions = computed(() => {
  return [
    { label: '全部家庭', value: null },
    ...families.value.map(f => ({
      label: f.name,
      value: f.id
    }))
  ];
});

const detailModal = reactive({
  show: false,
  data: null as DishRecord | null
});

const columns: DataTableColumns<DishRecord> = [
  {
    title: '图片',
    key: 'image',
    width: 80,
    render: (row) => {
      // 后端已经将完整URL设置到localImage和image字段中（本地存储模式下）
      // 优先使用localImage字段（后端已拼接好URL），如果没有则使用image字段
      const imageUrl = row.localImage || row.image || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgZmlsbD0iI2YzZjRmNiIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNDUlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iODAiIGZpbGw9IiM5Y2EzYWYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPvCfjaU8L3RleHQ+CiAgPHRleHQgeD0iNTAlIiB5PSI2NSUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxOCIgZmlsbD0iIzljYTNhZiIgdGV4dC1hbmNob3I9Im1pZGRsZSI+5pqC5peg5Zu+54mHPC90ZXh0Pgo8L3N2Zz4=';
      return h(
        NImage,
        {
          width: 48,
          height: 48,
          src: imageUrl,
          objectFit: 'cover',
          style: {
            borderRadius: '8px',
            boxShadow: 'var(--shadow-sm)',
            border: '1px solid var(--border-secondary)'
          },
          fallbackSrc: 'https://dummyimage.com/100x100/e2e8f0/94a3b8&text=No+Image'
        }
      );
    }
  },
  { title: '菜品', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '分类',
    key: 'categoryNames',
    ellipsis: { tooltip: true },
    render: (row) => {
      // 优先使用 categoryNames 数组,如果没有则使用单个 categoryName
      if (row.categoryNames && Array.isArray(row.categoryNames) && row.categoryNames.length > 0) {
        return row.categoryNames.join(', ');
      }
      return row.categoryName || lookupCategoryName(row.categoryId) || '—';
    }
  },
  {
    title: '价格',
    key: 'price',
    render: (row) => `¥${Number(row.price || 0).toFixed(2)}`
  },
  {
    title: '状态',
    key: 'status',
    render: (row) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'warning', bordered: false },
        { default: () => (row.status === 1 ? '在用' : '下架') }
      )
  },
  {
    title: '卡路里',
    key: 'calories',
    render: (row: any) => row.calories || '—'
  },
  {
    title: '标签',
    key: 'tags',
    ellipsis: { tooltip: true },
    render: (row: any) => row.tags || '—'
  },
  {
    title: '家庭备注',
    key: 'description',
    ellipsis: { tooltip: true },
    render: (row) => row.description || '—'
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
              { size: 'small', tertiary: true, type: 'info', onClick: () => openDetailModal(row) },
              { default: () => '详情' }
            ),
            h(
              NButton,
              { size: 'small', tertiary: true, onClick: () => openDishModal(row.id) },
              { default: () => '编辑' }
            ),
            h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                type: row.status === 1 ? 'warning' : 'success',
                onClick: () => toggleDishStatus(row)
              },
              { default: () => (row.status === 1 ? '下架' : '上架') }
            ),
            h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                type: 'error',
                onClick: () => handleDeleteDish(row.id)
              },
              { default: () => '删除' }
            )
          ]
        }
      )
  }
];

const categoryOptions = computed(() =>
  categories.value.map((category) => ({
    label: category.name,
    value: category.id
  }))
);

// 计算有效图片列表（过滤空值，包括待上传的图片）
const validImageList = computed(() => {
  const valid: Array<{ path: string; url: string; index: number; isPending?: boolean }> = [];
  
  // 先添加已上传的图片
  for (let i = 0; i < dishModal.form.localImagesArray.length; i++) {
    const path = dishModal.form.localImagesArray[i];
    const url = dishModal.imagePreviewUrls[i] || path;
    if (path && path.trim()) {
      valid.push({ path, url, index: i, isPending: false });
    }
  }
  
  // 再添加待上传的图片（使用临时路径标识）
  dishModal.pendingFiles.forEach((pending, idx) => {
    const tempPath = `pending_${pending.index}`;
    valid.push({ 
      path: tempPath, 
      url: pending.previewUrl, 
      index: dishModal.form.localImagesArray.length + idx,
      isPending: true 
    });
  });
  
  return valid;
});

// 计算有效图片数量（包括待上传的）
const validImageCount = computed(() => {
  const uploadedCount = dishModal.form.localImagesArray.filter(path => path && path.trim()).length;
  return uploadedCount + dishModal.pendingFiles.length;
});

function lookupCategoryName(id: number) {
  return categories.value.find((item) => item.id === id)?.name || '未分类';
}

const selectCategory = (id: number | null) => {
  selectedCategoryId.value = id;
};

const resetCategoryForm = (category?: Category) => {
  categoryModal.form.id = category?.id;
  categoryModal.form.name = category?.name ?? '';
  categoryModal.form.sort = category?.sort ?? categories.value.length + 1;
};

const openCategoryModal = (category?: Category) => {
  resetCategoryForm(category);
  categoryModal.show = true;
};

const saveCategory = async () => {
  if (!categoryModal.form.name.trim()) {
    message.warning('请输入分类名称');
    return;
  }
  categoryModal.loading = true;
  try {
    const payload = {
      id: categoryModal.form.id,
      name: categoryModal.form.name.trim(),
      sort: categoryModal.form.sort ?? 1,
      type: 1
    };
    if (payload.id) {
      await updateCategory(payload);
      message.success('分类已更新');
    } else {
      await createCategory(payload);
      message.success('分类已创建');
    }
    categoryModal.show = false;
    await loadCategories();
  } finally {
    categoryModal.loading = false;
  }
};

const handleDeleteCategory = async (id: number) => {
  if (!window.confirm('删除后分类下的菜品仍会保留，确定要删除吗？')) {
    return;
  }
  await removeCategory(id);
  message.success('分类已删除');
  if (selectedCategoryId.value === id) {
    selectedCategoryId.value = null;
  }
  await loadCategories();
};

const refreshDishes = () => {
  pagination.page = 1;
  loadDishes();
};

// 家庭筛选器变化处理
const handleFamilyFilterChange = () => {
  pagination.page = 1;
  loadDishes();
  loadCategories();
};

const resetDishForm = () => {
  dishModal.form.id = undefined;
  dishModal.form.name = '';
  dishModal.form.categoryIds = selectedCategoryId.value ? [selectedCategoryId.value] : []; // 改为数组
  dishModal.form.price = null;
  dishModal.form.status = 'on';
  dishModal.form.description = '';
  dishModal.form.flavorText = '';
  dishModal.form.image = '';
  dishModal.form.localImage = '';
  dishModal.form.localImages = '';
  dishModal.form.localImagesArray = [];
  dishModal.form.calories = '';
  dishModal.form.tags = '';
  dishModal.form.tagsArray = [];
  // 非超级管理员默认使用自己的家庭ID
  dishModal.form.familyId = isSuperAdmin.value ? null : currentUserFamilyId.value;
  dishModal.imagePreviewUrls = [];
  dishModal.pendingFiles = []; // 清空待上传文件
};

const openDishModal = async (id?: number) => {
  dishModal.show = true;
  dishModal.fetching = Boolean(id);
  resetDishForm();
  // 清除表单校验状态
  dishFormRef.value?.restoreValidation();
  if (!id) {
    dishModal.fetching = false;
    return;
  }
  try {
    const result = await fetchDishDetail(id);
    fillDishForm(result.data);
  } finally {
    dishModal.fetching = false;
  }
};

const openDetailModal = async (dish: DishRecord) => {
  detailModal.data = dish;
  detailModal.show = true;
  // Fetch full details to ensure we have flavors etc.
  try {
    const result = await fetchDishDetail(dish.id);
    if (result.data) {
      detailModal.data = { ...dish, ...result.data };
    }
  } catch (error) {
    console.error('Failed to fetch detail', error);
  }
};

// 获取详情弹窗的图片数组
const getDetailImages = () => {
  if (!detailModal.data) {
    return [];
  }
  
  // 优先使用 localImagesArray（后端已转换的 URL 数组）
  if (detailModal.data.localImagesArray && Array.isArray(detailModal.data.localImagesArray) && detailModal.data.localImagesArray.length > 0) {
    return detailModal.data.localImagesArray;
  }
  
  // 兼容处理：解析 localImages JSON 字符串
  if (detailModal.data.localImages) {
    try {
      const parsed = typeof detailModal.data.localImages === 'string'
        ? JSON.parse(detailModal.data.localImages)
        : detailModal.data.localImages;
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed;
      }
    } catch (e) {
      console.warn('解析 localImages 失败:', e);
    }
  }
  
  // 降级到主图
  if (detailModal.data.localImage) {
    return [detailModal.data.localImage];
  }
  if (detailModal.data.image) {
    return [detailModal.data.image];
  }
  
  return [];
};

const fillDishForm = (dish: any) => {
  dishModal.form.id = dish.id;
  dishModal.form.name = dish.name;
  // 支持多分类:优先使用categoryIds,如果没有则使用categoryId兼容旧数据
  dishModal.form.categoryIds = dish.categoryIds && dish.categoryIds.length > 0 
    ? dish.categoryIds 
    : (dish.categoryId ? [dish.categoryId] : []);
  dishModal.form.price = Number(dish.price || 0);
  dishModal.form.status = dish.status === 1 ? 'on' : 'off';
  dishModal.form.description = dish.description || '';
  dishModal.form.flavorText = parseFlavorText(dish.flavors);
  dishModal.form.calories = dish.calories || '';
  dishModal.form.tags = dish.tags || '';
  dishModal.form.familyId = dish.familyId || null;
  // 将标签字符串转换为数组
  dishModal.form.tagsArray = dish.tags ? dish.tags.split(',').map((t: string) => t.trim()).filter(Boolean) : [];
  
  // 处理图片：将完整 URL 转换为相对路径存储到 localImagesArray，完整 URL 存储到 imagePreviewUrls
  dishModal.form.localImage = dish.localImage || '';
  dishModal.form.localImages = dish.localImages || '';
  dishModal.form.image = dish.image || '';
  
  let imageUrls: string[] = []; // 完整 URL 数组（用于预览）
  let imagePaths: string[] = []; // 相对路径数组（用于保存）
  
  // 获取图片 URL 数组
  if (dish.localImagesArray && Array.isArray(dish.localImagesArray) && dish.localImagesArray.length > 0) {
    imageUrls = [...dish.localImagesArray];
  } else if (dish.localImages) {
    // 解析 localImages JSON 字符串
    try {
      const parsed = typeof dish.localImages === 'string' 
        ? JSON.parse(dish.localImages) 
        : dish.localImages;
      imageUrls = Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      console.warn('解析 localImages 失败:', e);
      imageUrls = dish.localImage ? [dish.localImage] : [];
    }
  } else if (dish.localImage) {
    imageUrls = [dish.localImage];
  }
  
  // 从完整 URL 中提取相对路径
  const extractRelativePath = (fullUrl: string): string => {
    if (!fullUrl) return '';
    // 如果已经是相对路径，直接返回
    if (!fullUrl.startsWith('http://') && !fullUrl.startsWith('https://')) {
      return fullUrl;
    }
    // 提取相对路径：查找 images 或 uploads 后的路径
    try {
      const url = new URL(fullUrl);
      const path = url.pathname;
      // 匹配 /images/ 或 /uploads/ 后的路径
      const match = path.match(/\/(?:images|uploads|food-menu)\/(.+)$/);
      if (match) {
        return match[1];
      }
      // 如果没有匹配，返回路径部分（去掉开头的斜杠）
      return path.replace(/^\//, '');
    } catch (e) {
      return fullUrl;
    }
  };
  
  // 转换为相对路径数组和完整 URL 数组
  imagePaths = imageUrls.map(extractRelativePath);
  // 过滤空值
  imagePaths = imagePaths.filter(p => p && p.trim());
  imageUrls = imageUrls.filter((url, idx) => imagePaths[idx] && imagePaths[idx].trim());
  
  dishModal.form.localImagesArray = imagePaths;
  dishModal.imagePreviewUrls = imageUrls;
  
  // 处理主图：转换为相对路径
  if (dishModal.form.localImage) {
    dishModal.form.localImage = extractRelativePath(dishModal.form.localImage);
  }
  
  // 确保主图在图片列表中，且两个数组长度一致
  if (dishModal.form.localImage && !dishModal.form.localImagesArray.includes(dishModal.form.localImage)) {
    // 主图不在列表中，添加到开头
    dishModal.form.localImagesArray.unshift(dishModal.form.localImage);
    // 生成主图的预览 URL
    if (dish.image && dish.image.startsWith('http')) {
      try {
        const url = new URL(dish.image);
        const baseUrl = url.origin;
        const basePath = url.pathname.substring(0, url.pathname.lastIndexOf('/') + 1);
        dishModal.imagePreviewUrls.unshift(baseUrl + basePath + dishModal.form.localImage);
      } catch (e) {
        dishModal.imagePreviewUrls.unshift(dishModal.form.localImage);
      }
    } else {
      dishModal.imagePreviewUrls.unshift(dishModal.form.localImage);
    }
  } else if (dishModal.form.localImage) {
    // 主图在列表中，确保它在第一个位置
    const mainIndex = dishModal.form.localImagesArray.indexOf(dishModal.form.localImage);
    if (mainIndex > 0) {
      const mainPath = dishModal.form.localImagesArray.splice(mainIndex, 1)[0];
      const mainUrl = dishModal.imagePreviewUrls[mainIndex] ? dishModal.imagePreviewUrls.splice(mainIndex, 1)[0] : '';
      dishModal.form.localImagesArray.unshift(mainPath);
      if (mainUrl) {
        dishModal.imagePreviewUrls.unshift(mainUrl);
      }
    }
  }
  
  // 最终确保两个数组长度一致，但不添加空值，只保留有效图片
  const finalPaths: string[] = [];
  const finalUrls: string[] = [];
  for (let i = 0; i < Math.max(dishModal.form.localImagesArray.length, dishModal.imagePreviewUrls.length); i++) {
    const path = dishModal.form.localImagesArray[i];
    const url = dishModal.imagePreviewUrls[i];
    if (path && path.trim()) {
      finalPaths.push(path);
      finalUrls.push(url || path);
    }
  }
  dishModal.form.localImagesArray = finalPaths;
  dishModal.imagePreviewUrls = finalUrls;
};

const parseFlavorText = (flavors?: DishFlavor[]) => {
  if (!flavors || !flavors.length) return '';
  const flavor = flavors[0];
  try {
    const values = JSON.parse(flavor.value) as string[];
    return values.join('、');
  } catch (error) {
    return flavor.value;
  }
};

const buildFlavorPayload = (text: string): DishFlavor[] => {
  const tags = text
    .split(/[,，、\s]+/)
    .map((item) => item.trim())
    .filter(Boolean);
  const value = JSON.stringify(tags.length ? tags : ['家常']);
  return [{ name: '口味', value }];
};

const saveDish = async () => {
  // 表单校验
  try {
    await dishFormRef.value?.validate();
  } catch (errors) {
    // 校验失败，显示第一个错误信息
    const firstError = Object.values(errors as any)[0] as any;
    if (firstError && firstError[0]?.message) {
      message.warning(firstError[0].message);
    } else {
      message.warning('请完整填写菜品信息');
    }
    return;
  }
  
  // 检查是否有图片（包括待上传的）
  const totalImageCount = validImageCount.value;
  if (totalImageCount === 0) {
    message.warning('请至少上传一张图片');
    return;
  }
  
  dishModal.loading = true;
  try {
    // 先上传所有待上传的图片
    if (dishModal.pendingFiles.length > 0) {
      message.info('正在上传图片...');
      const uploadPromises = dishModal.pendingFiles.map(async (pending) => {
        try {
          const result = await uploadImage(pending.file);
          const uploadResult = result.data as { objectKey: string; presignedUrl: string };
          return {
            objectKey: uploadResult.objectKey,
            presignedUrl: uploadResult.presignedUrl,
            originalPendingIndex: pending.index
          };
        } catch (error) {
          throw new Error(`图片上传失败: ${(error as Error).message}`);
        }
      });
      
      const uploadResults = await Promise.all(uploadPromises);
      
      // 将上传结果添加到已上传列表
      uploadResults.forEach((result) => {
        dishModal.form.localImagesArray.push(result.objectKey);
        dishModal.imagePreviewUrls.push(result.presignedUrl);
      });
      
      // 清空待上传列表
      dishModal.pendingFiles = [];
      
      // 如果主图是待上传的，更新为主图路径
      if (dishModal.form.localImage.startsWith('pending_')) {
        const pendingIndex = parseInt(dishModal.form.localImage.replace('pending_', ''));
        if (pendingIndex >= 0 && pendingIndex < uploadResults.length) {
          dishModal.form.localImage = uploadResults[pendingIndex].objectKey;
        } else if (dishModal.form.localImagesArray.length > 0) {
          dishModal.form.localImage = dishModal.form.localImagesArray[0];
        }
      }
    }
    
    // 过滤掉空值，只保留有效图片
    const validImagePaths = dishModal.form.localImagesArray.filter(path => path && path.trim());
    
    if (validImagePaths.length === 0) {
      message.warning('请至少上传一张图片');
      return;
    }
    
    // 确保主图在图片列表中，且主图在第一个位置
    if (!dishModal.form.localImage || !validImagePaths.includes(dishModal.form.localImage)) {
      // 如果主图不在列表中，设置第一张为主图
      dishModal.form.localImage = validImagePaths[0];
    } else {
      // 如果主图在列表中，将其移动到第一个位置
      const mainIndex = validImagePaths.indexOf(dishModal.form.localImage);
      if (mainIndex > 0) {
        validImagePaths.splice(mainIndex, 1);
        validImagePaths.unshift(dishModal.form.localImage);
        // 同时更新预览URL数组的顺序
        const mainUrl = dishModal.imagePreviewUrls[mainIndex];
        if (mainUrl) {
          dishModal.imagePreviewUrls.splice(mainIndex, 1);
          dishModal.imagePreviewUrls.unshift(mainUrl);
        }
      }
    }
    
    // 更新表单中的图片数组
    dishModal.form.localImagesArray = validImagePaths;
    
    // 将标签数组转换为字符串
    const tagsString = dishModal.form.tagsArray.length > 0 
      ? dishModal.form.tagsArray.join(',') 
      : null;
    
    // 将过滤后的有效图片数组转换为JSON字符串（只发送相对路径）
    const localImagesString = JSON.stringify(validImagePaths);
    
    const payload: any = {
      id: dishModal.form.id,
      name: dishModal.form.name.trim(),
      categoryIds: dishModal.form.categoryIds, // 改为多分类
      price: Number(dishModal.form.price),
      status: dishModal.form.status === 'on' ? 1 : 0,
      description: dishModal.form.description,
      flavors: buildFlavorPayload(dishModal.form.flavorText),
      image: dishModal.form.image,
      localImage: dishModal.form.localImage,
      localImages: localImagesString,
      calories: dishModal.form.calories.trim() || null,
      tags: tagsString,
      familyId: dishModal.form.familyId || undefined
    };
    if (payload.id) {
      await updateDish(payload);
      message.success('菜品已更新');
    } else {
      await createDish(payload);
      message.success('菜品已添加');
    }
    dishModal.show = false;
    await loadDishes();
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    dishModal.loading = false;
  }
};

const handleDeleteDish = async (id: number) => {
  if (!window.confirm('确定删除该菜品吗？')) {
    return;
  }
  await removeDish(id);
  message.success('菜品已删除');
  await loadDishes();
};

const toggleDishStatus = async (dish: DishRecord) => {
  const result = await fetchDishDetail(dish.id);
  const detail = result.data;
  if (!detail) {
    message.error('获取菜品信息失败');
    return;
  }
  const nextStatus = dish.status === 1 ? 0 : 1;
  const flavors = detail.flavors && detail.flavors.length ? detail.flavors : buildFlavorPayload('');
  await updateDish({
    id: detail.id,
    name: detail.name,
    categoryId: detail.categoryId,
    price: Number(detail.price),
    status: nextStatus,
    description: detail.description,
    flavors,
    image: detail.image,
    calories: (detail as any).calories || null,
    tags: (detail as any).tags || null
  });
  message.success(nextStatus === 1 ? '已上架' : '已下架');
  await loadDishes();
};

const handleImageUpload = async (options: UploadCustomRequestOptions) => {
  const { file, onFinish, onError } = options;
  if (!file.file) {
    onError?.();
    return;
  }

  // 检查数量限制（包括已上传和待上传的）
  if (validImageCount.value >= imageLimit.value) {
    message.warning(`最多只能上传 ${imageLimit.value} 张图片`);
    onError?.();
    return;
  }

  // 验证文件类型
  const fileType = file.file.type;
  if (!fileType.startsWith('image/')) {
    message.warning('请上传图片文件');
    onError?.();
    return;
  }

  // 验证文件大小（10MB）
  const maxSize = 10 * 1024 * 1024;
  if (file.file.size > maxSize) {
    message.warning('图片大小不能超过10MB');
    onError?.();
    return;
  }

  // 使用 FileReader 进行本地预览
  const reader = new FileReader();
  reader.onload = (e) => {
    const previewUrl = e.target?.result as string;
    const pendingIndex = dishModal.pendingFiles.length;
    
    // 添加到待上传列表
    dishModal.pendingFiles.push({
      file: file.file as File,
      previewUrl,
      index: pendingIndex
    });
    
    // 如果是第一张图片，自动设为主图（使用临时标识）
    if (validImageCount.value === 1) {
      dishModal.form.localImage = `pending_${pendingIndex}`;
    }
    
    message.success('图片已添加，保存时将上传');
    onFinish?.();
  };
  
  reader.onerror = () => {
    message.error('图片预览失败');
    onError?.();
  };
  
  reader.readAsDataURL(file.file as File);
};

// 设置主图
const setMainImage = (imagePath: string) => {
  // 检查是否是待上传的图片
  if (imagePath.startsWith('pending_')) {
    const pendingIndex = parseInt(imagePath.replace('pending_', ''));
    if (pendingIndex >= 0 && pendingIndex < dishModal.pendingFiles.length) {
      dishModal.form.localImage = imagePath;
      message.success('已设置为主图');
      return;
    }
  }
  
  // 确保主图路径在图片列表中
  if (!dishModal.form.localImagesArray.includes(imagePath)) {
    // 如果不在列表中，可能需要从预览URL中查找对应的路径
    const index = dishModal.imagePreviewUrls.findIndex((url: string, idx: number) => {
      return dishModal.form.localImagesArray[idx] === imagePath || url === imagePath;
    });
    if (index >= 0 && index < dishModal.form.localImagesArray.length) {
      imagePath = dishModal.form.localImagesArray[index];
    }
  }
  
  // 设置主图
  dishModal.form.localImage = imagePath;
  
  // 将主图移动到第一个位置
  const mainIndex = dishModal.form.localImagesArray.indexOf(imagePath);
  if (mainIndex > 0) {
    const mainPath = dishModal.form.localImagesArray.splice(mainIndex, 1)[0];
    const mainUrl = dishModal.imagePreviewUrls[mainIndex] ? dishModal.imagePreviewUrls.splice(mainIndex, 1)[0] : '';
    dishModal.form.localImagesArray.unshift(mainPath);
    if (mainUrl) {
      dishModal.imagePreviewUrls.unshift(mainUrl);
    }
  }
  
  message.success('已设置为主图');
};

// 删除图片
const removeImage = (index: number) => {
  // 检查是否是待上传的图片
  const validList = validImageList.value;
  const targetItem = validList[index];
  
  if (targetItem.isPending) {
    // 删除待上传的图片
    const pendingIndex = parseInt(targetItem.path.replace('pending_', ''));
    dishModal.pendingFiles.splice(pendingIndex, 1);
    
    // 重新索引待上传文件
    dishModal.pendingFiles.forEach((pending, idx) => {
      pending.index = idx;
    });
    
    // 如果删除的是主图，重新设置主图
    if (targetItem.path === dishModal.form.localImage) {
      if (validImageList.value.length > 0) {
        dishModal.form.localImage = validImageList.value[0].path;
      } else {
        dishModal.form.localImage = '';
      }
    }
  } else {
    // 删除已上传的图片
    const removed = dishModal.form.localImagesArray[targetItem.index];
    dishModal.form.localImagesArray.splice(targetItem.index, 1);
    dishModal.imagePreviewUrls.splice(targetItem.index, 1);
    
    // 如果删除的是主图，自动设置第一张为主图
    if (removed === dishModal.form.localImage) {
      if (validImageList.value.length > 0) {
        dishModal.form.localImage = validImageList.value[0].path;
      } else {
        dishModal.form.localImage = '';
      }
    }
  }
  
  message.success('图片已删除');
};

const loadCategories = async () => {
  const familyId = isSuperAdmin.value ? (selectedFamilyId.value ?? undefined) : undefined;
  const result = await fetchCategories({ familyId });
  categories.value = result.data || [];
  // 默认选中"全部"(null),不自动选中第一个分类
  if (selectedCategoryId.value !== null && !categories.value.some((item) => item.id === selectedCategoryId.value)) {
    selectedCategoryId.value = null;
  }
};

// 加载标签列表
const loadDishTags = async () => {
  tagsLoading.value = true;
  try {
    const result = await fetchAllDishTags();
    dishTags.value = result.data || [];
  } catch (error) {
    console.error('加载标签失败:', error);
  } finally {
    tagsLoading.value = false;
  }
};

const loadDishes = async () => {
  dishLoading.value = true;
  try {
    const result = await fetchDishes({
      page: pagination.page || 1,
      pageSize: pagination.pageSize || 10,
      name: keyword.value || undefined,
      categoryId: selectedCategoryId.value ?? undefined,
      familyId: isSuperAdmin.value ? (selectedFamilyId.value ?? undefined) : undefined
    });
    dishes.value = result.data?.records || [];
    pagination.itemCount = result.data?.total || 0;
  } finally {
    dishLoading.value = false;
  }
};

watch(selectedCategoryId, () => {
  pagination.page = 1;
  loadDishes();
});

// 加载图片数量限制
const loadImageLimit = async () => {
  try {
    const res = await fetchSystemConfig('dish_image_limit');
    if (res.data && res.data.configValue) {
      imageLimit.value = Number(res.data.configValue) || 5;
    }
  } catch (error) {
    console.error('加载图片限制失败:', error);
  }
};

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
  await loadCategories();
  await loadDishes();
  await loadDishTags();
  await loadImageLimit();
});
</script>

<style scoped>
.menu-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.categories-card,
.dishes-card {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  padding-right: 4px;
}

.category-list::-webkit-scrollbar {
  width: 6px;
}

.category-list::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.category-list::-webkit-scrollbar-thumb {
  background: var(--text-tertiary);
  border-radius: 3px;
}

.category-list::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

.category-item {
  border: 1px solid var(--border-secondary);
  border-radius: 16px;
  background: var(--bg-card);
  padding: 14px 16px;
  width: 100%;
  color: var(--text-primary);
  text-align: left;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  transition: all 0.2s;
  cursor: pointer;
}

.category-item:hover {
  border-color: var(--primary-color);
  transform: translateX(4px);
}

.category-item.active {
  border-color: var(--primary-color);
  background: rgba(var(--primary-h), var(--primary-s), var(--primary-l), 0.1);
  box-shadow: var(--shadow-md);
}

.category-actions {
  display: flex;
  gap: 8px;
}

.empty-hint {
  text-align: center;
  padding: 32px 0;
  opacity: 0.7;
  color: var(--text-secondary);
}

.table-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-primary);
}

.table-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: nowrap;
  width: 100%;
  justify-content: flex-start;
  min-width: 0;
}

.table-actions > * {
  flex-shrink: 0;
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
  box-shadow: var(--shadow-md);
}

.upload-inner {
  color: var(--text-secondary);
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}

/* 多图上传样式 */
.multi-image-upload {
  width: 100%;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.image-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.image-item.is-main {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.image-item:hover .image-actions {
  opacity: 1;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.main-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.pending-badge {
  position: absolute;
  top: 4px;
  left: 4px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f9fafb;
}

.upload-placeholder:hover {
  border-color: #667eea;
  background: #f3f4f6;
}

.upload-icon {
  font-size: 32px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.image-limit-tip {
  font-size: 12px;
  color: #ef4444;
  margin-top: 8px;
}

:deep(.n-button.primary-soft) {
  background: var(--gradient-primary);
  border: none;
  color: white;
  font-weight: 600;
  box-shadow: var(--shadow-glow);
}

:deep(.n-button.primary-soft:not(.n-button--disabled):hover) {
  filter: brightness(1.1);
  transform: translateY(-2px);
}

@media (max-width: 1024px) {
  .menu-grid {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 20px;
  }

  .categories-card,
  .dishes-card {
    padding: 20px;
  }
}

/* Detail Modal Styles */
.dish-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-price {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
  font-family: 'DIN Alternate', sans-serif;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.section-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
}

.detail-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-box {
  background: rgba(255, 255, 255, 0.05);
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>

