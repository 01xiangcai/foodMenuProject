<template>
  <div class="p-6">
    <n-card title="餐次配置管理" class="mb-4">
      <template #header-extra>
        <n-button type="primary" @click="handleSave">保存配置</n-button>
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
import { ref, onMounted } from 'vue';
import { NCard, NForm, NFormItemGi, NGrid, NTimePicker, NDivider, NButton, useMessage } from 'naive-ui';
import http from '@/api/http';

const message = useMessage();

const form = ref({
  breakfast: {
    startTime: null,
    endTime: null,
    orderDeadline: null
  },
  lunch: {
    startTime: null,
    endTime: null,
    orderDeadline: null
  },
  dinner: {
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
      const configMap: any = {};
      res.data.forEach((item: any) => {
        const period = item.mealPeriod.toLowerCase();
        configMap[period] = {
          id: item.id,
          startTime: timeToTimestamp(item.startTime),
          endTime: timeToTimestamp(item.endTime),
          orderDeadline: timeToTimestamp(item.orderDeadline)
        };
      });
      
      if (configMap.breakfast) form.value.breakfast = configMap.breakfast;
      if (configMap.lunch) form.value.lunch = configMap.lunch;
      if (configMap.dinner) form.value.dinner = configMap.dinner;
    }
  } catch (error) {
    console.error('加载配置失败:', error);
    message.error('加载配置失败');
  }
};

// 时间字符串转时间戳 (Naive UI需要)
const timeToTimestamp = (timeStr: string) => {
  if (!timeStr) return null;
  const [hours, minutes, seconds] = timeStr.split(':');
  const date = new Date();
  date.setHours(parseInt(hours), parseInt(minutes), parseInt(seconds || '0'));
  return date.getTime();
};

// 保存配置
const handleSave = async () => {
  try {
    const configs = [
      {
        ...form.value.breakfast,
        mealPeriod: 'BREAKFAST'
      },
      {
        ...form.value.lunch,
        mealPeriod: 'LUNCH'
      },
      {
        ...form.value.dinner,
        mealPeriod: 'DINNER'
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

onMounted(() => {
  loadConfig();
});
</script>
