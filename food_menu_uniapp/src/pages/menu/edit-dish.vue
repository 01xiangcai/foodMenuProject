<template>
  <view class="page" :style="{ background: themeConfig.bgPrimary }">
    <popup-selector 
      v-model:show="showCategoryPopup" 
      title="选择分类" 
      :list="categories" 
      :selected="selectedCategories"
      :themeConfig="themeConfig"
      @confirm="onCategoryConfirm"
    />
    <popup-selector 
      v-model:show="showTagPopup" 
      title="选择标签" 
      :list="cuisineOptions" 
      :selected="selectedTags"
      :multiple="true"
      labelKey="label"
      valueKey="value"
      :themeConfig="themeConfig"
      @confirm="onTagConfirm"
    />
    <!-- 顶部导航栏 (透明/滚动变色) -->
    <view class="navbar" :style="{ background: scrollTop > 50 ? themeConfig.bgSecondary : 'transparent' }">
      <view class="navbar-content">
        <view class="back-btn" @tap="goBack" :style="{ background: scrollTop > 50 ? themeConfig.bgTertiary : 'rgba(255,255,255,0.9)', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }">
          <text class="icon" :style="{ color: scrollTop > 50 ? themeConfig.textPrimary : '#333' }">‹</text>
        </view>
        <text class="navbar-title" :style="{ opacity: scrollTop > 50 ? 1 : 0, color: themeConfig.textPrimary }">编辑菜品</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <scroll-view class="content" scroll-y @scroll="onScroll">
      <!-- 封面图区域 -->
      <view class="cover-section">
        <swiper 
          class="image-swiper" 
          :indicator-dots="imageList.length > 1"
          indicator-active-color="#FF7D58"
          indicator-color="rgba(255,255,255,0.5)"
        >
          <swiper-item v-for="(img, index) in imageList" :key="index">
            <view class="swiper-image-wrap">
              <image :src="img.url" mode="aspectFill" class="cover-image" @tap="previewImage(index)" />
              <view class="image-overlay">
                <view v-if="index === 0" class="main-badge">封面</view>
                <view class="delete-btn" @tap.stop="deleteImage(index)">×</view>
              </view>
            </view>
          </swiper-item>
          
          <swiper-item v-if="imageList.length < 5">
            <view class="upload-placeholder" @tap="chooseImage" :style="{ background: themeConfig.bgTertiary }">
              <view class="upload-content">
                <text class="upload-icon" :style="{ color: themeConfig.textSecondary }">📷</text>
                <text class="upload-text" :style="{ color: themeConfig.textSecondary }">上传菜品图片 ({{ imageList.length }}/5)</text>
              </view>
            </view>
          </swiper-item>
        </swiper>
      </view>

      <view class="form-container" :style="{ background: themeConfig.bgSecondary }">
        
        <!-- 选项列表 -->
        <view class="cell-group">
          
          <!-- 菜品名称 (标准表单样式) -->
          <view class="cell-item">
            <view class="cell-left">
              <text class="cell-icon">🍲</text>
              <text class="cell-label" :style="{ color: themeConfig.textPrimary }">菜品名称 <text class="required">*</text></text>
            </view>
            <view class="cell-right">
              <input 
                class="cell-input-normal" 
                v-model="formData.name" 
                placeholder="请输入菜品名称"
                placeholder-style="color: #999"
                :style="{ color: themeConfig.textPrimary }"
              />
            </view>
          </view>
          
          <view class="divider-inset" :style="{ background: themeConfig.borderColor }"></view>

          <!-- 所属分类 (多选) -->
          <!-- 所属分类 (多选) -->
          <view class="cell-item" hover-class="cell-hover" @tap="showCategoryPopup = true">
            <view class="cell-left">
              <text class="cell-icon">📂</text>
              <text class="cell-label" :style="{ color: themeConfig.textPrimary }">所属分类 <text class="required">*</text></text>
            </view>
            <view class="cell-right">
              <text class="cell-value" :class="{ empty: selectedCategories.length === 0 }" :style="{ color: selectedCategories.length > 0 ? themeConfig.textSecondary : themeConfig.textTertiary }">
                {{ selectedCategories.length > 0 ? selectedCategories.map(c => c.name).join('、') : '请选择分类' }}
              </text>
              <text class="cell-arrow" :style="{ color: themeConfig.textTertiary }">></text>
            </view>
          </view>

          <!-- 已选分类列表 -->
          <view class="selected-tags" v-if="selectedCategories.length > 0">
            <view 
              class="selected-tag-chip" 
              v-for="(category, index) in selectedCategories" 
              :key="category.id"
              :style="{ background: themeConfig.bgTertiary, color: themeConfig.textSecondary }"
            >
              <text>{{ category.name }}</text>
              <text class="tag-remove" @tap.stop="removeCategory(index)">×</text>
            </view>
          </view>

          <view class="divider-inset" :style="{ background: themeConfig.borderColor }"></view>
          
          <!-- 菜品标签 (多选) -->
          <!-- 菜品标签 (多选) -->
          <view class="cell-item" hover-class="cell-hover" @tap="showTagPopup = true">
            <view class="cell-left">
              <text class="cell-icon">🏷️</text>
              <text class="cell-label" :style="{ color: themeConfig.textPrimary }">菜品标签</text>
            </view>
            <view class="cell-right">
              <text class="cell-value" :class="{ empty: selectedTags.length === 0 }" :style="{ color: selectedTags.length > 0 ? themeConfig.textSecondary : themeConfig.textTertiary }">
                {{ selectedTags.length > 0 ? selectedTags.join('、') : '请选择标签' }}
              </text>
              <text class="cell-arrow" :style="{ color: themeConfig.textTertiary }">></text>
            </view>
          </view>
          
          <!-- 已选标签列表 -->
          <view class="selected-tags" v-if="selectedTags.length > 0">
            <view 
              class="selected-tag-chip" 
              v-for="(tag, index) in selectedTags" 
              :key="tag"
              :style="{ background: themeConfig.bgTertiary, color: themeConfig.textSecondary }"
            >
              <text>{{ tag }}</text>
              <text class="tag-remove" @tap.stop="removeTag(index)">×</text>
            </view>
          </view>

          <view class="divider-inset" :style="{ background: themeConfig.borderColor }"></view>

          <!-- 价格 -->
          <view class="cell-item">
            <view class="cell-left">
              <text class="cell-icon">💰</text>
              <text class="cell-label" :style="{ color: themeConfig.textPrimary }">价格 (元) <text class="required">*</text></text>
            </view>
            <view class="cell-right">
              <text class="price-symbol" :style="{ color: themeConfig.primaryColor }">¥</text>
              <input 
                class="cell-input" 
                v-model="formData.price" 
                type="digit" 
                placeholder="0.00"
                placeholder-style="color: #999"
                :style="{ color: themeConfig.primaryColor }"
              />
            </view>
          </view>
          
          <view class="divider-inset" :style="{ background: themeConfig.borderColor }"></view>

          <!-- 上架状态 -->
          <view class="cell-item">
            <view class="cell-left">
              <text class="cell-icon">🟢</text>
              <text class="cell-label" :style="{ color: themeConfig.textPrimary }">上架状态</text>
            </view>
            <view class="cell-right">
              <switch 
                :checked="formData.status === 1" 
                @change="onStatusChange" 
                color="#FF7D58" 
                style="transform:scale(0.8)"
              />
              <text class="switch-label" :style="{ color: themeConfig.textSecondary }">{{ formData.status === 1 ? '启用' : '停用' }}</text>
            </view>
          </view>
          
          <view class="divider-inset" :style="{ background: themeConfig.borderColor }"></view>

          <!-- 卡路里 -->
          <view class="cell-item">
            <view class="cell-left">
              <text class="cell-icon">🔥</text>
              <text class="cell-label" :style="{ color: themeConfig.textPrimary }">卡路里</text>
            </view>
            <view class="cell-right">
              <input 
                class="cell-input-normal" 
                v-model="formData.calories" 
                placeholder="例如: 482 kcal"
                placeholder-style="color: #999"
                :style="{ color: themeConfig.textSecondary }"
              />
            </view>
          </view>
          <view class="divider-inset" :style="{ background: themeConfig.borderColor }"></view>

          <!-- 家庭备注 -->
        </view>

        <!-- 家庭备注 (富文本样式) -->
        <view class="rich-input-group">
          <view class="rich-header">
            <text class="cell-icon">📝</text>
            <text class="cell-label" :style="{ color: themeConfig.textPrimary }">家庭备注 (简介)</text>
          </view>
          <view class="rich-textarea-wrapper" :style="{ background: themeConfig.bgTertiary }">
            <textarea 
              class="rich-textarea" 
              v-model="formData.description" 
              placeholder="选填，可填写口味偏好或制作要求..."
              placeholder-style="color: #999"
              :maxlength="200"
              :style="{ color: themeConfig.textPrimary }"
            />
            <view class="word-count" :style="{ color: themeConfig.textTertiary }">
              {{ formData.description.length }}/200
            </view>
          </view>
        </view>



        <!-- 口味标签 (多个输入) -->
        <view class="tags-section">
          <view class="tags-header">
            <text class="cell-icon">🌶️</text>
            <text class="cell-label" :style="{ color: themeConfig.textPrimary }">口味标签</text>
          </view>
          <view class="tags-desc" :style="{ color: themeConfig.textTertiary }">如：微辣、少油、全熟</view>
          
          <view class="tags-list">
            <view 
              class="tag-chip" 
              v-for="(flavor, index) in flavorList" 
              :key="index"
              :style="{ background: themeConfig.bgTertiary, color: themeConfig.textSecondary }"
            >
              <text>{{ flavor }}</text>
              <text class="tag-del" @tap="removeFlavor(index)">×</text>
            </view>
            <view 
              v-if="flavorList.length < 5" 
              class="tag-add-btn" 
              @tap="showFlavorInput"
              :style="{ border: `1px dashed ${themeConfig.borderColor}`, color: themeConfig.textTertiary }"
            >
              + 添加口味
            </view>
          </view>
        </view>

        <!-- 占位 -->
        <view class="bottom-spacer"></view>
      </view>
    </scroll-view>

    <!-- 底部悬浮按钮 -->
    <view class="bottom-bar" :style="{ background: themeConfig.bgSecondary, borderTop: `1px solid ${themeConfig.borderColor}` }">
      <view 
        class="publish-btn" 
        @tap="submitForm"
        :style="{ background: themeConfig.primaryGradient, boxShadow: themeConfig.shadowLight }"
      >
        保存修改
      </view>
    </view>
    
    <!-- 口味输入弹窗 -->
    <view v-if="isFlavorInputVisible" class="modal-mask" @tap="hideFlavorInput">
      <view class="modal-content" @tap.stop :style="{ background: themeConfig.bgSecondary }">
        <text class="modal-title" :style="{ color: themeConfig.textPrimary }">添加口味</text>
        <input 
          class="modal-input" 
          v-model="flavorInputValue" 
          placeholder="例如: 微辣" 
          focus
          @confirm="confirmFlavor"
          :style="{ background: themeConfig.bgTertiary, color: themeConfig.textPrimary }"
        />
        <view class="modal-actions">
          <text class="modal-btn cancel" @tap="hideFlavorInput" :style="{ color: themeConfig.textSecondary }">取消</text>
          <text class="modal-btn confirm" @tap="confirmFlavor" :style="{ color: themeConfig.primaryColor }">确定</text>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useTheme } from '@/stores/theme'
import { getCategoryList, uploadDishImage, updateDishUniapp, getDishDetail, getDishTagList } from '@/api/index'
import PopupSelector from '@/components/common/popup-selector.vue'

const { themeConfig, loadTheme } = useTheme()

// 获取菜品ID
const dishId = ref(null)
const isLoading = ref(true)

// 滚动
const scrollTop = ref(0)
const onScroll = (e) => { scrollTop.value = e.detail.scrollTop }

// 表单数据
const formData = ref({
  id: null,
  name: '',
  categoryIds: [], // 改为数组以支持多分类
  price: '',
  description: '', // 映射为"家庭备注"
  tags: '', // 映射为"菜品标签"(菜系)
  status: 1, // 上架状态
  calories: '',
  localImage: '',
  localImages: '[]',
  flavors: [] // 映射为"口味标签"
})

// 图片
const imageList = ref([])

// 分类 (多选)
const categories = ref([])
const selectedCategories = ref([]) // 已选分类对象数组
const showCategoryPopup = ref(false)

// 菜系选项 (从后台获取)
const cuisineOptions = ref([])
const selectedTags = ref([]) // 已选标签数组
const showTagPopup = ref(false)

// 口味列表 (临时存储字符串列表，提交时转换结构)
const flavorList = ref([])
const isFlavorInputVisible = ref(false)
const flavorInputValue = ref('')

// 页面加载
import { onLoad, onShow } from '@dcloudio/uni-app'

onLoad((options) => {
  if (options.id) {
    dishId.value = options.id
  } else {
    uni.showToast({ title: '菜品ID缺失', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
  }
})

onShow(async () => {
  loadTheme()
  await loadCategories()
  await loadTags()
  if (dishId.value) {
    await loadDishDetail()
  }
})

const loadTags = async () => {
  try {
    const res = await getDishTagList()
    if (res.data) {
      // 简单的 Emoji 映射，如果后台名称匹配则使用，否则随机或使用默认
      const emojiMap = {
        '川菜': '🌶️', '湘菜': '🔥', '粤菜': '🥯', '鲁菜': '🥘', 
        '浙菜': '🐟', '东北菜': '🥟', '本帮菜': '�', '赣菜': '�', 
        '徽菜': '🏔️', '西餐': '🥩', '日料': '�', '家常菜': '🏠', '轻食': '🥗'
      }
      
      cuisineOptions.value = res.data.map(tag => {
        const emoji = emojiMap[tag.name] || '🏷️'
        return {
          label: `${emoji} ${tag.name}`,
          value: tag.name 
        }
      })
    }
  } catch(e) { console.error('获取标签失败', e) }
}


const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.data) categories.value = res.data
  } catch(e) { console.error(e) }
}

// 加载菜品详情
const loadDishDetail = async () => {
  try {
    uni.showLoading({ title: '加载中' })
    const res = await getDishDetail(dishId.value)
    if (res.data) {
      const dish = res.data
      
      // 回填基本信息
      formData.value.id = dish.id
      formData.value.name = dish.name
      formData.value.categoryId = dish.categoryId
      formData.value.price = dish.price.toString()
      formData.value.description = dish.description || ''
      formData.value.tags = dish.tags || ''
      formData.value.status = dish.status
      formData.value.calories = dish.calories || ''
      
      // 回填分类选择器 - 支持多分类
      if (dish.categoryIds && dish.categoryIds.length > 0 && categories.value.length > 0) {
        // 使用新的categoryIds字段
        selectedCategories.value = categories.value.filter(c => dish.categoryIds.includes(c.id))
        formData.value.categoryIds = dish.categoryIds
      } else if (dish.categoryId && categories.value.length > 0) {
        // 兼容旧版本:如果没有categoryIds但有categoryId
        const category = categories.value.find(c => c.id === dish.categoryId)
        if (category) {
          selectedCategories.value = [category]
          formData.value.categoryIds = [dish.categoryId]
        }
      }
      
      // 回填菜系选择器 - 支持多个标签
      if (dish.tags && cuisineOptions.value.length > 0) {
        // 将tags字符串按逗号分割为数组
        const tags = dish.tags.split(',').map(t => t.trim()).filter(Boolean)
        selectedTags.value = tags
      }
      
      // 回填图片列表
      if (dish.localImagesArray && dish.localImagesArray.length > 0) {
        imageList.value = dish.localImagesArray.map(url => ({
          url: url,
          path: extractPathFromUrl(url)
        }))
        formData.value.localImage = imageList.value[0].path
        formData.value.localImages = JSON.stringify(imageList.value.map(i => i.path))
      } else if (dish.localImage) {
        // 如果没有图片数组,使用主图
        const url = dish.image || dish.localImage
        imageList.value = [{
          url: url,
          path: dish.localImage
        }]
        formData.value.localImage = dish.localImage
        formData.value.localImages = JSON.stringify([dish.localImage])
      }
      
      // 回填口味标签
      if (dish.flavors && dish.flavors.length > 0) {
        const flavorItem = dish.flavors.find(f => f.name === '口味')
        if (flavorItem && flavorItem.value) {
          try {
            const flavors = JSON.parse(flavorItem.value)
            if (Array.isArray(flavors)) {
              flavorList.value = flavors
            }
          } catch (e) {
            console.error('解析口味失败', e)
          }
        }
      }
      
      isLoading.value = false
    }
    uni.hideLoading()
  } catch (e) {
    uni.hideLoading()
    console.error('加载菜品详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
  }
}

// 从完整URL提取相对路径
const extractPathFromUrl = (url) => {
  if (!url) return ''
  // 如果是完整URL,提取路径部分
  if (url.startsWith('http://') || url.startsWith('https://')) {
    try {
      const urlObj = new URL(url)
      return urlObj.pathname.substring(1) // 移除开头的 /
    } catch (e) {
      return url
    }
  }
  return url
}

// 分类弹窗确认
const onCategoryConfirm = (val) => {
  selectedCategories.value = val
  updateCategoryIds()
}

// 移除分类
const removeCategory = (index) => {
  selectedCategories.value.splice(index, 1)
  updateCategoryIds()
}

// 更新categoryIds字段
const updateCategoryIds = () => {
  formData.value.categoryIds = selectedCategories.value.map(c => c.id)
}

// 标签弹窗确认
const onTagConfirm = (val) => {
  // val 是对象数组 [{label, value}]
  selectedTags.value = val.map(v => v.value)
  updateTagsField()
}

// 移除标签
const removeTag = (index) => {
  selectedTags.value.splice(index, 1)
  updateTagsField()
}

// 更新tags字段
const updateTagsField = () => {
  formData.value.tags = selectedTags.value.join(',')
}

const onStatusChange = (e) => {
  formData.value.status = e.detail.value ? 1 : 0
}

const chooseImage = () => {
  uni.chooseImage({
    count: 5 - imageList.value.length,
    success: (res) => uploadImages(res.tempFilePaths)
  })
}

const uploadImages = async (paths) => {
  uni.showLoading({ title: '上传中' })
  for (let path of paths) {
    try {
      const res = await uploadDishImage(path)
      if (res.data) {
        imageList.value.push({ url: res.data.presignedUrl || res.data.objectKey, path: res.data.objectKey })
      }
    } catch(e) { console.error(e) }
  }
  updateImageFields()
  uni.hideLoading()
}

const updateImageFields = () => {
  if (imageList.value.length > 0) {
    formData.value.localImage = imageList.value[0].path
    formData.value.localImages = JSON.stringify(imageList.value.map(i => i.path))
  } else {
    formData.value.localImage = ''
    formData.value.localImages = '[]'
  }
}

const deleteImage = (idx) => {
  imageList.value.splice(idx, 1)
  updateImageFields()
}

const previewImage = (idx) => {
  uni.previewImage({ urls: imageList.value.map(i => i.url), current: idx })
}

// 口味标签逻辑
const showFlavorInput = () => { isFlavorInputVisible.value = true }
const hideFlavorInput = () => { isFlavorInputVisible.value = false; flavorInputValue.value = '' }
const removeFlavor = (idx) => flavorList.value.splice(idx, 1)
const confirmFlavor = () => {
  const val = flavorInputValue.value.trim()
  if (val) {
    if (!flavorList.value.includes(val)) flavorList.value.push(val)
    hideFlavorInput()
  }
}

const submitForm = async () => {
  if (!formData.value.name.trim()) return uni.showToast({ title: '请输入菜品名称', icon: 'none' })
  if (formData.value.categoryIds.length === 0) return uni.showToast({ title: '请至少选择一个分类', icon: 'none' })
  if (!formData.value.price) return uni.showToast({ title: '请输入价格', icon: 'none' })
  if (imageList.value.length === 0) return uni.showToast({ title: '请至少上传一张图片', icon: 'none' })
  
  uni.showLoading({ title: '保存中' })
  
  try {
    // 构造口味数据结构 [{name: '口味', value: '["微辣"]'}]
    const flavors = flavorList.value.length > 0 
      ? [{ name: '口味', value: JSON.stringify(flavorList.value) }]
      : []

    await updateDishUniapp({
      ...formData.value,
      price: parseFloat(formData.value.price),
      flavors: flavors
    })
    
    uni.showToast({ title: '更新成功' })
    setTimeout(() => goBack(), 1500)
    
  } catch(e) {
    uni.showToast({ title: e.message || '系统繁忙', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 返回
const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 })
  } else {
    // 如果没有上一页（例如分享进入），则跳转到菜单页
    uni.switchTab({ url: '/pages/menu/menu' })
  }
}

</script>

<style lang="scss" scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 999; /* 提高层级 */
  transition: all 0.3s;
  pointer-events: none; /* 让点击穿透透明区域 */
}
.navbar-content {
  height: 88rpx;
  padding-top: var(--status-bar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 10rpx; /* 减少左边距，让按钮更靠边 */
  padding-right: 20rpx;
  pointer-events: auto; /* 恢复点击 */
}
.back-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  
  .icon {
    font-size: 44rpx;
    font-weight: bold;
    line-height: 1;
  }
}
.navbar-title {
  font-size: 32rpx;
  font-weight: 600;
  transition: opacity 0.3s;
}
.placeholder { width: 64rpx; }

.content {
  flex: 1;
}

/* 封面 */
.cover-section {
  width: 100%;
  height: 500rpx;
  background: #eee;
}
.image-swiper { width: 100%; height: 100%; }
.swiper-image-wrap { width: 100%; height: 100%; position: relative; }
.cover-image { width: 100%; height: 100%; }
.image-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1), transparent);
  pointer-events: none;
}
.delete-btn {
  position: absolute; top: 120rpx; right: 20rpx;
  width: 48rpx; height: 48rpx; background: rgba(0,0,0,0.5);
  border-radius: 50%; color: white;
  display: flex; align-items: center; justify-content: center; pointer-events: auto;
}
.main-badge {
  position: absolute; bottom: 20rpx; right: 20rpx;
  background: #FF7D58; color: white; font-size: 20rpx;
  padding: 4rpx 12rpx; border-radius: 8rpx;
}
.upload-placeholder {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
}
.upload-content { display: flex; flex-direction: column; align-items: center; gap: 16rpx; }
.upload-icon { font-size: 64rpx; }
.upload-text { font-size: 26rpx; }

/* 表单 */
.form-container {
  border-radius: 32rpx 32rpx 0 0;
  margin-top: -32rpx;
  position: relative;
  z-index: 10;
  padding: 40rpx 30rpx;
  min-height: 800rpx;
}

.title-input { font-size: 38rpx; font-weight: bold; height: 60rpx; }
.title-placeholder { font-weight: normal; color: #ccc; }

.desc-input {
  width: 100%; min-height: 80rpx; font-size: 28rpx; padding: 20rpx 0;
}
.desc-placeholder { color: #ccc; }

.divider { height: 1rpx; margin: 10rpx 0; opacity: 0.5; }
.divider-inset { height: 1rpx; margin: 0 0 0 20rpx; opacity: 0.5; }

/* 单元格 */
.cell-group { margin-top: 20rpx; }
.cell-item {
  display: flex; align-items: center; justify-content: space-between; padding: 28rpx 0;
}
.cell-left { display: flex; align-items: center; gap: 16rpx; }
.cell-icon { font-size: 34rpx; width: 40rpx; text-align: center; }
.cell-label { font-size: 28rpx; font-weight: 500; }
.required { color: #ff4757; margin-left: 4rpx; }

.cell-right { display: flex; align-items: center; gap: 10rpx; flex: 1; justify-content: flex-end; }
.cell-value { font-size: 28rpx; }
.cell-input { text-align: right; font-size: 32rpx; font-weight: 600; width: 200rpx; }
.cell-input-normal { text-align: right; font-size: 28rpx; width: 300rpx; }
.price-symbol { font-weight: bold; }
.switch-label { font-size: 24rpx; margin-left: 10rpx; }

/* 已选标签 */
.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 16rpx 0;
  padding-left: 56rpx;
}

.selected-tag-chip {
  padding: 8rpx 16rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.tag-remove {
  opacity: 0.6;
  padding-left: 8rpx;
  font-size: 28rpx;
  cursor: pointer;
}

/* 标签 */
.tags-section { margin-top: 30rpx; }
.tags-header { display: flex; align-items: center; gap: 16rpx; margin-bottom: 10rpx; }
.tags-desc { font-size: 22rpx; margin-bottom: 20rpx; padding-left: 56rpx; }
.tags-list { display: flex; flex-wrap: wrap; gap: 16rpx; padding-left: 56rpx; }
.tag-chip {
  padding: 8rpx 20rpx; border-radius: 30rpx; font-size: 24rpx;
  display: flex; align-items: center; gap: 8rpx;
}
.tag-del { opacity: 0.6; padding-left: 8rpx; }
.tag-add-btn { padding: 8rpx 24rpx; border-radius: 30rpx; font-size: 24rpx; }

/* 底部 */
.bottom-bar {
  position: fixed; bottom: 0; width: 100%; 
  padding: 20rpx 40rpx calc(20rpx + env(safe-area-inset-bottom));
  z-index: 100;
}
.publish-btn {
  height: 88rpx; border-radius: 44rpx; display: flex; 
  align-items: center; justify-content: center; color: white; font-size: 32rpx; font-weight: 600;
}
.bottom-spacer { height: 160rpx; }

/* 弹窗 */
.modal-mask {
  position: fixed; top: 0; right: 0; bottom: 0; left: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: center; justify-content: center;
}
.modal-content {
  width: 600rpx; border-radius: 24rpx; padding: 40rpx;
  display: flex; flex-direction: column; gap: 30rpx;
}
.modal-title { font-size: 32rpx; font-weight: 600; text-align: center; }
.modal-input { height: 80rpx; border-radius: 12rpx; padding: 0 20rpx; }
.modal-actions {
  display: flex; justify-content: flex-end; gap: 40rpx; margin-top: 20rpx;
}
.modal-btn { font-size: 30rpx; font-weight: 600; padding: 10rpx 20rpx; }

/* 富文本输入框样式 */
.rich-input-group {
  margin-top: 30rpx;
}
.rich-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.rich-textarea-wrapper {
  padding: 24rpx;
  border-radius: 20rpx;
  position: relative;
}
.rich-textarea {
  width: 100%;
  height: 200rpx;
  font-size: 28rpx;
  line-height: 1.5;
}
.word-count {
  text-align: right;
  font-size: 24rpx;
  margin-top: 10rpx;
}
</style>
