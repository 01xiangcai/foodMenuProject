<template>
  <view 
    class="popup-mask" 
    :class="{ show: show }" 
    @tap="handleMaskClick"
    :style="{ visibility: show ? 'visible' : 'hidden' }"
  >
    <view 
      class="popup-content" 
      :class="{ show: show }" 
      @tap.stop
      :style="{ background: themeConfig.bgSecondary, paddingBottom: 'calc(20rpx + env(safe-area-inset-bottom))' }"
    >
      <!-- 标题栏 -->
      <view class="popup-header" :style="{ borderBottom: `1px solid ${themeConfig.borderColor}` }">
        <text class="popup-btn cancel" @tap="handleCancel" :style="{ color: themeConfig.textSecondary }">取消</text>
        <text class="popup-title" :style="{ color: themeConfig.textPrimary }">{{ title }}</text>
        <text class="popup-btn confirm" @tap="handleConfirm" :style="{ color: themeConfig.primaryColor }">确定</text>
      </view>

      <!-- 选项区域 -->
      <scroll-view scroll-y class="popup-body">
        <view class="tags-container">
          <view 
            v-for="(item, index) in list" 
            :key="item[valueKey] || index"
            class="tag-item"
            :class="{ active: isSelected(item) }"
            @tap="toggleSelect(item)"
            :style="isSelected(item) 
              ? { background: themeConfig.primaryColor, color: '#fff', boxShadow: themeConfig.shadowLight } 
              : { background: themeConfig.bgTertiary, color: themeConfig.textSecondary }"
          >
            {{ item[labelKey] }}
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { defineProps, defineEmits, ref, watch } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '请选择' },
  list: { type: Array, default: () => [] }, // [{ id: 1, name: 'Hot' }]
  selected: { type: Array, default: () => [] }, // 初始选中的值 (对象数组 或 简单数组)
  multiple: { type: Boolean, default: true },
  labelKey: { type: String, default: 'name' }, // 用于显示的字段名
  valueKey: { type: String, default: 'id' },   // 用于判断唯一的字段名 (对于简单数组可不传或传空)
  themeConfig: { type: Object, required: true }
})

const emit = defineEmits(['update:show', 'confirm'])

// 内部维护的选中状态 (为了在点击确定前不影响外部)
const tempSelected = ref([])

// 监听弹窗打开，初始化内部选中状态
watch(() => props.show, (newVal) => {
  if (newVal) {
    // 深拷贝，避免引用
    tempSelected.value = [...props.selected]
  }
})

// 判断是否选中
const isSelected = (item) => {
  // Case 1: Item itself is in selected
  if (tempSelected.value.includes(item)) return true
  
  // Case 2: Item is object, check by valueKey
  if (!isSimpleValue(item) && props.valueKey) {
    const itemValue = item[props.valueKey]
    return tempSelected.value.some(sel => {
      // If selected item is simple value, compare directly with itemValue
      if (isSimpleValue(sel)) return sel === itemValue
      // If selected item is object, compare valueKeys
      return sel[props.valueKey] === itemValue
    })
  }
  return false
}

// 切换选择
const toggleSelect = (item) => {
  if (props.multiple) {
    if (isSelected(item)) {
      // 移除
      if (isSimpleValue(item)) {
        tempSelected.value = tempSelected.value.filter(i => i !== item)
      } else {
        const itemValue = item[props.valueKey]
        tempSelected.value = tempSelected.value.filter(sel => {
           if (isSimpleValue(sel)) return sel !== itemValue
           return sel[props.valueKey] !== itemValue
        })
      }
    } else {
      // 添加 (总是添加 list 中的 item 对象)
      tempSelected.value.push(item)
    }
  } else {
    // 单选
    tempSelected.value = [item]
  }
}

const handleCancel = () => {
  emit('update:show', false)
}

const handleConfirm = () => {
  emit('confirm', tempSelected.value)
  emit('update:show', false)
}

const handleMaskClick = () => {
  handleCancel()
}

// 辅助：判断是否是简单值 (string/number)
const isSimpleValue = (val) => {
  return typeof val === 'string' || typeof val === 'number'
}

</script>

<style lang="scss" scoped>
.popup-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  transition: opacity 0.3s;
  opacity: 0;
  
  &.show {
    opacity: 1;
  }
}

.popup-content {
  position: absolute;
  bottom: 0; left: 0; width: 100%;
  border-radius: 32rpx 32rpx 0 0;
  transform: translateY(100%);
  transition: transform 0.3s;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  
  &.show {
    transform: translateY(0);
  }
}

.popup-header {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  flex-shrink: 0;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
}

.popup-btn {
  font-size: 28rpx;
  padding: 20rpx;
  
  &.confirm {
    font-weight: 600;
  }
}

.popup-body {
  flex: 1;
  min-height: 200rpx;
  max-height: 50vh; 
  padding: 30rpx;
  box-sizing: border-box;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.tag-item {
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  transition: all 0.2s;
  
  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
}
</style>
