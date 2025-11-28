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
            <strong>{{ category.name }}</strong>
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
          <NInput
            v-model:value="keyword"
            clearable
            placeholder="搜索菜名"
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
        <NForm :model="dishModal.form" label-placement="left" label-width="80" @submit.prevent>
          <NFormItem label="菜品名称" required>
            <NInput v-model:value="dishModal.form.name" placeholder="番茄炒蛋" />
          </NFormItem>
          <NFormItem label="所属分类" required>
            <NSelect
              v-model:value="dishModal.form.categoryId"
              :options="categoryOptions"
              placeholder="请选择分类"
            />
          </NFormItem>
          <NFormItem label="价格 (元)" required>
            <NInputNumber v-model:value="dishModal.form.price" :min="0" :precision="2" placeholder="例如 28" />
          </NFormItem>
          <NFormItem label="菜品图片" required>
            <div class="image-upload">
              <NImage
                v-if="dishModal.imagePreviewUrl || dishModal.form.image"
                class="image-preview"
                :src="dishModal.imagePreviewUrl || dishModal.form.image"
                width="140"
                height="140"
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
                    <p class="upload-tip">支持 jpg/png，较大图片更清晰</p>
                  </div>
                </NUploadDragger>
              </NUpload>
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
          <img :src="detailModal.data.image" class="detail-image" />
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
              <div class="stat-value">{{ detailModal.data.categoryName || lookupCategoryName(detailModal.data.categoryId) }}</div>
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
  fetchAllDishTags
} from '@/api/modules';

type Category = {
  id: number;
  name: string;
  sort?: number;
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
};

type DishForm = {
  id?: number;
  name: string;
  categoryId: number | null;
  price: number | null;
  status: 'on' | 'off';
  description: string;
  flavorText: string;
  image: string;
  calories: string;
  tags: string;
};

const message = useMessage();

const categories = ref<Category[]>([]);
const selectedCategoryId = ref<number | null>(null);
const keyword = ref('');

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
    categoryId: null as number | null,
    price: null as number | null,
    status: 'on' as 'on' | 'off',
    description: '',
    flavorText: '',
    image: '', // Store object key (relative path) for database
    calories: '',
    tags: '',
    tagsArray: [] as string[] // 标签数组，用于多选
  },
  imagePreviewUrl: '' // Store presigned URL for preview
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
    render: (row) =>
      h(
        NImage,
        {
          width: 48,
          height: 48,
          src: row.image,
          objectFit: 'cover',
          style: {
            borderRadius: '8px',
            boxShadow: 'var(--shadow-sm)',
            border: '1px solid var(--border-secondary)'
          },
          fallbackSrc: 'https://dummyimage.com/100x100/e2e8f0/94a3b8&text=No+Image'
        }
      )
  },
  { title: '菜品', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '分类',
    key: 'categoryName',
    render: (row) => row.categoryName || lookupCategoryName(row.categoryId)
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

const resetDishForm = () => {
  dishModal.form.id = undefined;
  dishModal.form.name = '';
  dishModal.form.categoryId = selectedCategoryId.value;
  dishModal.form.price = null;
  dishModal.form.status = 'on';
  dishModal.form.description = '';
  dishModal.form.flavorText = '';
  dishModal.form.image = '';
  dishModal.form.calories = '';
  dishModal.form.tags = '';
  dishModal.form.tagsArray = [];
  dishModal.imagePreviewUrl = '';
};

const openDishModal = async (id?: number) => {
  dishModal.show = true;
  dishModal.fetching = Boolean(id);
  resetDishForm();
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

const fillDishForm = (dish: any) => {
  dishModal.form.id = dish.id;
  dishModal.form.name = dish.name;
  dishModal.form.categoryId = dish.categoryId;
  dishModal.form.price = Number(dish.price || 0);
  dishModal.form.status = dish.status === 1 ? 'on' : 'off';
  dishModal.form.description = dish.description || '';
  dishModal.form.flavorText = parseFlavorText(dish.flavors);
  dishModal.form.calories = dish.calories || '';
  dishModal.form.tags = dish.tags || '';
  // 将标签字符串转换为数组
  dishModal.form.tagsArray = dish.tags ? dish.tags.split(',').map((t: string) => t.trim()).filter(Boolean) : [];
  // Backend returns presigned URL when image is OSS object key
  // For existing dishes, image might be presigned URL or default image URL
  // We need to extract object key if it's a presigned URL, or keep as is
  const image = dish.image || '';
  // If image is a presigned URL (contains ?Expires=), extract object key
  // Otherwise, it might be object key or default image URL
  if (image.includes('?Expires=')) {
    // Extract object key from presigned URL (everything before ?)
    const urlObj = new URL(image);
    const objectKey = urlObj.pathname.substring(1); // Remove leading /
    dishModal.form.image = objectKey;
    dishModal.imagePreviewUrl = image; // Use presigned URL for preview
  } else if (image.startsWith('http://') || image.startsWith('https://')) {
    // Default image URL, save as is
    dishModal.form.image = image;
    dishModal.imagePreviewUrl = image;
  } else {
    // Object key, save as is (backend will convert to presigned URL when returning)
    dishModal.form.image = image;
    dishModal.imagePreviewUrl = ''; // Will be generated by backend when needed
  }
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
  if (!dishModal.form.name.trim() || !dishModal.form.categoryId || dishModal.form.price === null) {
    message.warning('请完整填写菜品信息');
    return;
  }
  if (!dishModal.form.image) {
    message.warning('请上传菜品图片');
    return;
  }
  dishModal.loading = true;
  try {
    // 将标签数组转换为字符串
    const tagsString = dishModal.form.tagsArray.length > 0 
      ? dishModal.form.tagsArray.join(',') 
      : null;
    
    const payload: any = {
      id: dishModal.form.id,
      name: dishModal.form.name.trim(),
      categoryId: dishModal.form.categoryId,
      price: Number(dishModal.form.price),
      status: dishModal.form.status === 'on' ? 1 : 0,
      description: dishModal.form.description,
      flavors: buildFlavorPayload(dishModal.form.flavorText),
      image: dishModal.form.image,
      calories: dishModal.form.calories.trim() || null,
      tags: tagsString
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
  imageUploading.value = true;
  try {
    const result = await uploadImage(file.file as File);
    // result.data is UploadResult: { objectKey, presignedUrl }
    const uploadResult = result.data as { objectKey: string; presignedUrl: string };
    // Save object key to database (not presigned URL, which expires)
    dishModal.form.image = uploadResult.objectKey;
    // Use presigned URL for preview
    dishModal.imagePreviewUrl = uploadResult.presignedUrl;
    message.success('图片上传成功');
    onFinish?.();
  } catch (error) {
    message.error((error as Error).message);
    onError?.();
  } finally {
    imageUploading.value = false;
  }
};

const loadCategories = async () => {
  const result = await fetchCategories();
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
      categoryId: selectedCategoryId.value ?? undefined
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

onMounted(async () => {
  await loadCategories();
  await loadDishes();
  await loadDishTags();
});
</script>

<style scoped>
.menu-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.table-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

