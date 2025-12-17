<template>
  <div class="p-6">
    <n-card title="餐次配置管理" class="mb-4">
      <template #header-extra>
        <n-space>
          <!-- 超级管理员家庭选择器 -->
          <n-select
            v-if="isSuperAdmin"
            v-model:value="selectedFamilyId"
            :options="familyOptions"
            placeholder="选择家庭"
            style="width: 200px"
            @update:value="handleFamilyChange"
          />
          <n-button type="primary" @click="handleSave">保存配置</n-button>
        </n-space>
      </template>

      <n-form :model="form" label-placement="left" label-width="120">
        <n-divider title-placement="left">早餐配置</n-divider>
        <n-grid :cols="3" :x-gap="20">
          <n-form-item-gi label="开始时间">
            <n-time-picker v-model:value="form.breakfast.startTime" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
          <n-form-item-gi label="结束时间">
            <n-time-picker v-model:value="form.breakfast.endTime" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
          <n-form-item-gi label="下单截止">
            <n-time-picker v-model:value="form.breakfast.orderDeadline" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
        </n-grid>

        <n-divider title-placement="left">中餐配置</n-divider>
        <n-grid :cols="3" :x-gap="20">
          <n-form-item-gi label="开始时间">
            <n-time-picker v-model:value="form.lunch.startTime" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
          <n-form-item-gi label="结束时间">
            <n-time-picker v-model:value="form.lunch.endTime" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
          <n-form-item-gi label="下单截止">
            <n-time-picker v-model:value="form.lunch.orderDeadline" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
        </n-grid>

        <n-divider title-placement="left">晚餐配置</n-divider>
        <n-grid :cols="3" :x-gap="20">
          <n-form-item-gi label="开始时间">
            <n-time-picker v-model:value="form.dinner.startTime" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
          <n-form-item-gi label="结束时间">
            <n-time-picker v-model:value="form.dinner.endTime" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
          <n-form-item-gi label="下单截止">
            <n-time-picker v-model:value="form.dinner.orderDeadline" value-format="HH:mm:ss" format="HH:mm" />
          </n-form-item-gi>
        </n-grid>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { NCard, NForm, NFormItemGi, NGrid, NTimePicker, NDivider, NButton, NSelect, NSpace, useMessage } from 'naive-ui';
import http from '@/api/http';
import { useUserStore } from '@/store/useUserStore';

const message = useMessage();
const userStore = useUserStore();

// 是否是超级管理员
const isSuperAdmin = computed(() => userStore.profile?.role === 2);

// 家庭列表
const families = ref<any[]>([]);
const selectedFamilyId = ref<number | null>(null);
const allConfigs = ref<any[]>([]); // 存储所有配置

// 家庭选项
const familyOptions = computed(() => {
  return families.value.map(f => ({
    label: f.name,
    value: f.id
  }));
});

const form = ref({
  breakfast: {
    id: null,
    familyId: null,
    startTime: null,
    endTime: null,
    orderDeadline: null
  },
  lunch: {
    id: null,
    familyId: null,
    startTime: null,
    endTime: null,
    orderDeadline: null
  },
  dinner: {
    id: null,
    familyId: null,
    startTime: null,
    endTime: null,
    orderDeadline: null
  }
});

// 加载配置
const loadConfig = async () => {
  try {
    const res = await http.get('/admin/meal-config');
    if (res.data) {
      allConfigs.value = res.data;
      
      // 如果是超级管理员且没有选中家庭,选中第一个家庭
      if (isSuperAdmin.value && !selectedFamilyId.value && families.value.length > 0) {
        selectedFamilyId.value = families.value[0].id;
      }
      
      // 过滤并显示当前家庭的配置
      filterConfigsByFamily();
    }
  } catch (error) {
    console.error('加载配置失败:', error);
    message.error('加载配置失败');
  }
};

// 加载家庭列表(仅超级管理员)
const loadFamilies = async () => {
  console.log('loadFamilies called, isSuperAdmin:', isSuperAdmin.value);
  if (!isSuperAdmin.value) return;
  
  try {
    console.log('请求家庭列表...');
    const res = await http.get('/admin/meal-config/families');
    console.log('家庭列表响应:', res);
    if (res.data) {
      families.value = res.data;
      console.log('家庭列表:', families.value);
      if (families.value.length > 0) {
        selectedFamilyId.value = families.value[0].id;
        console.log('选中家庭 ID:', selectedFamilyId.value);
      }
    }
  } catch (error) {
    console.error('加载家庭列表失败:', error);
  }
};

// 根据家庭过滤配置
const filterConfigsByFamily = () => {
  const targetFamilyId = isSuperAdmin.value ? selectedFamilyId.value : userStore.profile?.familyId;
  
  const configMap: any = {};
  allConfigs.value
    .filter((item: any) => item.familyId === targetFamilyId)
    .forEach((item: any) => {
      const period = item.mealPeriod.toLowerCase();
      configMap[period] = {
        id: item.id,
        familyId: item.familyId,
        startTime: timeToTimestamp(item.startTime),
        endTime: timeToTimestamp(item.endTime),
        orderDeadline: timeToTimestamp(item.orderDeadline)
      };
    });
  
  if (configMap.breakfast) form.value.breakfast = configMap.breakfast;
  if (configMap.lunch) form.value.lunch = configMap.lunch;
  if (configMap.dinner) form.value.dinner = configMap.dinner;
};

// 家庭切换
const handleFamilyChange = () => {
  filterConfigsByFamily();
};

// 时间字符串转时间戳 (Naive UI需要)
const timeToTimestamp = (timeStr: string) => {
  if (!timeStr) return null;
  const [hours, minutes, seconds] = timeStr.split(':');
  const date = new Date();
  date.setHours(parseInt(hours), parseInt(minutes), parseInt(seconds || '0'));
  return date.getTime();
};

// 时间戳转时间字符串 (发送给后端需要)
const timestampToTime = (timestamp: number | null) => {
  if (!timestamp) return null;
  const date = new Date(timestamp);
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${hours}:${minutes}:${seconds}`;
};

// 保存配置
const handleSave = async () => {
  try {
    // 使用选中的家庭 ID
    const targetFamilyId = isSuperAdmin.value ? selectedFamilyId.value : userStore.profile?.familyId;
    
    const configs = [
      {
        id: form.value.breakfast.id,
        familyId: targetFamilyId,
        mealPeriod: 'BREAKFAST',
        startTime: timestampToTime(form.value.breakfast.startTime),
        endTime: timestampToTime(form.value.breakfast.endTime),
        orderDeadline: timestampToTime(form.value.breakfast.orderDeadline)
      },
      {
        id: form.value.lunch.id,
        familyId: targetFamilyId,
        mealPeriod: 'LUNCH',
        startTime: timestampToTime(form.value.lunch.startTime),
        endTime: timestampToTime(form.value.lunch.endTime),
        orderDeadline: timestampToTime(form.value.lunch.orderDeadline)
      },
      {
        id: form.value.dinner.id,
        familyId: targetFamilyId,
        mealPeriod: 'DINNER',
        startTime: timestampToTime(form.value.dinner.startTime),
        endTime: timestampToTime(form.value.dinner.endTime),
        orderDeadline: timestampToTime(form.value.dinner.orderDeadline)
      }
    ];
    
    await http.post('/admin/meal-config', configs);
    message.success('保存成功');
    loadConfig();
  } catch (error) {
    console.error('保存失败:', error);
    message.error('保存失败');
  }
};

onMounted(async () => {
  console.log('onMounted 开始');
  // 加载用户信息
  await userStore.loadProfile();
  console.log('userStore.profile:', userStore.profile);
  console.log('isSuperAdmin:', isSuperAdmin.value);
  
  if (isSuperAdmin.value) {
    console.log('开始加载家庭列表...');
    await loadFamilies();
  } else {
    console.log('不是超级管理员,跳过加载家庭列表');
  }
  await loadConfig();
  console.log('onMounted 完成');
});
</script>
