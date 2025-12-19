<template>
  <view class="meal-period-selector" :class="'theme-' + currentTheme">
    <view class="selector-header" @tap="toggleExpand">
      <view class="header-left">
        <text class="header-title">选择餐次</text>
        <text class="current-period" v-if="!expanded">{{ getPeriodName(selectedPeriod) }}</text>
      </view>
      <view class="header-right">
        <text class="current-time">{{ currentTime }}</text>
        <text class="expand-icon" :class="{ expanded }">{{ expanded ? '▲' : '▼' }}</text>
      </view>
    </view>
    
    <view class="period-tabs" v-if="expanded" :class="{ 'slide-down': expanded }">
      <view 
        class="tab-item" 
        :class="{ active: selectedPeriod === 'BREAKFAST', disabled: isServed('BREAKFAST') }"
        @tap="selectPeriod('BREAKFAST')"
      >
        <text class="tab-icon">🍳</text>
        <text class="tab-name">早餐</text>
        <text class="tab-time" v-if="config.BREAKFAST">{{ config.BREAKFAST.time }}</text>
        <view class="status-badge" :class="'status-' + getMealStatus('BREAKFAST')" v-if="getMealStatus('BREAKFAST') !== null">
          <text>{{ getStatusText(getMealStatus('BREAKFAST')) }}</text>
        </view>
      </view>
      
      <view 
        class="tab-item" 
        :class="{ active: selectedPeriod === 'LUNCH', disabled: isServed('LUNCH') }"
        @tap="selectPeriod('LUNCH')"
      >
        <text class="tab-icon">🍱</text>
        <text class="tab-name">中餐</text>
        <text class="tab-time" v-if="config.LUNCH">{{ config.LUNCH.time }}</text>
        <view class="status-badge" :class="'status-' + getMealStatus('LUNCH')" v-if="getMealStatus('LUNCH') !== null">
          <text>{{ getStatusText(getMealStatus('LUNCH')) }}</text>
        </view>
      </view>
      
      <view 
        class="tab-item" 
        :class="{ active: selectedPeriod === 'DINNER', disabled: isServed('DINNER') }"
        @tap="selectPeriod('DINNER')"
      >
        <text class="tab-icon">🍷</text>
        <text class="tab-name">晚餐</text>
        <text class="tab-time" v-if="config.DINNER">{{ config.DINNER.time }}</text>
        <view class="status-badge" :class="'status-' + getMealStatus('DINNER')" v-if="getMealStatus('DINNER') !== null">
          <text>{{ getStatusText(getMealStatus('DINNER')) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, defineEmits, defineProps, watch } from 'vue'
import { useTheme } from '@/stores/theme'
import { request } from '@/utils/request'

const { currentTheme } = useTheme()

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedPeriod = ref(props.modelValue || '')
const currentTime = ref('')
const config = ref({})
const expanded = ref(false) // 默认折叠
const mealStatus = ref({}) // 各餐次状态: { BREAKFAST: 0, LUNCH: 1, DINNER: 3 }

// 监听外部值变化，同步更新内部状态
watch(() => props.modelValue, (newVal) => {
  if (newVal !== selectedPeriod.value) {
    selectedPeriod.value = newVal
  }
})

// 获取餐次名称
const getPeriodName = (period) => {
  const map = {
    'BREAKFAST': '早餐',
    'LUNCH': '中餐',
    'DINNER': '晚餐'
  }
  return map[period] || '未选择'
}

// 切换展开/折叠
const toggleExpand = () => {
  expanded.value = !expanded.value
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${h}:${m}`
}

// 加载餐次配置
const loadConfig = async () => {
  try {
    const res = await request({
      url: '/uniapp/meal-config',
      method: 'GET'
    })
    
    if (res.code === 1 && res.data) {
      const configMap = {}
      res.data.forEach(item => {
        configMap[item.mealPeriod] = {
          time: `${item.startTime.substring(0, 5)}-${item.endTime.substring(0, 5)}`,
          deadline: item.orderDeadline
        }
      })
      config.value = configMap
    }
  } catch (error) {
    console.error('加载餐次配置失败:', error)
  }
}

// 获取当前餐次
const getCurrentPeriod = async () => {
  try {
    const res = await request({
      url: '/uniapp/meal-config/current-period',
      method: 'GET'
    })
    
    if (res.code === 1 && res.data) {
      selectedPeriod.value = res.data
      emit('update:modelValue', res.data)
      emit('change', res.data)
    } else {
      // API返回但没有数据,使用默认值
      setDefaultPeriod()
    }
  } catch (error) {
    console.error('获取当前餐次失败:', error)
    // 使用默认值
    setDefaultPeriod()
  }
}

// 设置默认餐次
const setDefaultPeriod = () => {
  selectedPeriod.value = 'BREAKFAST'
  emit('update:modelValue', 'BREAKFAST')
  emit('change', 'BREAKFAST')
}

// 选择餐次
const selectPeriod = (period) => {
  // 已出餐的餐次不允许选择
  if (isServed(period)) {
    uni.showToast({
      title: '该餐次已出餐，无法下单',
      icon: 'none'
    })
    return
  }
  selectedPeriod.value = period
  emit('update:modelValue', period)
  emit('change', period)
  // 选择后自动折叠
  expanded.value = false
}

// 加载餐次状态
const loadMealStatus = async () => {
  try {
    const res = await request({
      url: '/uniapp/daily-meal-order/today',
      method: 'GET'
    })
    if (res.code === 1 && res.data) {
      const statusMap = {}
      res.data.forEach(item => {
        statusMap[item.mealPeriod] = item.status
      })
      mealStatus.value = statusMap
      
      // 检查当前选中的餐次是否已出餐，如果是则自动切换到第一个可用餐次
      if (selectedPeriod.value && statusMap[selectedPeriod.value] === 3) {
        const availablePeriods = ['BREAKFAST', 'LUNCH', 'DINNER']
        const firstAvailable = availablePeriods.find(p => statusMap[p] !== 3)
        if (firstAvailable) {
          selectedPeriod.value = firstAvailable
          emit('update:modelValue', firstAvailable)
          emit('change', firstAvailable)
          uni.showToast({
            title: '当前餐次已出餐，已自动切换',
            icon: 'none'
          })
        }
      }
    }
  } catch (error) {
    console.error('加载餐次状态失败:', error)
  }
}

// 获取餐次状态
const getMealStatus = (period) => {
  return mealStatus.value[period] !== undefined ? mealStatus.value[period] : null
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    0: '收集中',
    1: '已确认',
    2: '已截止',
    3: '已出餐'
  }
  return map[status] || ''
}

// 判断是否已出餐
const isServed = (period) => {
  return mealStatus.value[period] === 3
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 60000) // 每分钟更新一次时间
  loadConfig()
  loadMealStatus() // 加载餐次状态
  
  if (!props.modelValue) {
    // 没有传入值,先设置默认值,然后尝试获取当前餐次
    setDefaultPeriod()
    getCurrentPeriod()
  } else {
    selectedPeriod.value = props.modelValue
  }
})
</script>

<style lang="scss" scoped>
.meal-period-selector {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: var(--shadow-soft);
  overflow: hidden;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.current-period {
  font-size: 24rpx;
  color: var(--accent-orange);
  font-weight: 600;
  padding: 4rpx 12rpx;
  background: rgba(255, 159, 67, 0.1);
  border-radius: 12rpx;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.current-time {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.expand-icon {
  font-size: 20rpx;
  color: var(--text-secondary);
  transition: transform 0.3s;
  
  &.expanded {
    transform: rotate(180deg);
  }
}

.period-tabs {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 12rpx;
  background: var(--bg-input);
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  
  &.active {
    background: linear-gradient(135deg, var(--accent-orange), #ff9f43);
    border-color: var(--accent-orange);
    box-shadow: 0 4rpx 12rpx rgba(255, 159, 67, 0.3);
    
    .tab-icon,
    .tab-name,
    .tab-time {
      color: #fff;
    }
  }
  
  &:active {
    transform: scale(0.96);
  }
}

.tab-icon {
  font-size: 36rpx;
}

.tab-name {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.tab-time {
  font-size: 20rpx;
  color: var(--text-secondary);
}

/* 状态标签 */
.status-badge {
  margin-top: 8rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 18rpx;
  
  text {
    font-weight: 500;
  }
  
  /* 收集中 - 绿色 */
  &.status-0 {
    background: rgba(34, 197, 94, 0.15);
    text {
      color: #22c55e;
    }
  }
  
  /* 已确认 - 蓝色 */
  &.status-1 {
    background: rgba(59, 130, 246, 0.15);
    text {
      color: #3b82f6;
    }
  }
  
  /* 已截止 - 橙色 */
  &.status-2 {
    background: rgba(245, 158, 11, 0.15);
    text {
      color: #f59e0b;
    }
  }
  
  /* 已出餐 - 紫色 */
  &.status-3 {
    background: rgba(139, 92, 246, 0.15);
    text {
      color: #8b5cf6;
    }
  }
}

/* 禁用状态 - 已出餐不可选 */
.tab-item.disabled {
  opacity: 0.5;
  pointer-events: none;
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.1);
    border-radius: 16rpx;
  }
}
</style>
