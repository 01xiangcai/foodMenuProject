<template>
  <div class="settings-view">
    <section class="glass-card hover-rise">
      <div class="table-header">
        <div>
          <h2>系统设置</h2>
          <p>管理系统全局配置</p>
        </div>
      </div>

      <div class="settings-content">
        <NForm :model="settingsForm" label-placement="left" label-width="150">
          <NFormItem label="菜品图片数量限制">
            <NInputNumber
              v-model:value="settingsForm.dishImageLimit"
              :min="1"
              :max="10"
              :show-button="false"
              style="width: 200px"
            />
            <span class="form-tip">设置每个菜品最多可上传的图片数量（1-10张）</span>
          </NFormItem>

          <NFormItem>
            <NButton class="primary-soft" type="primary" :loading="saving" @click="saveSettings">保存设置</NButton>
          </NFormItem>
        </NForm>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { NButton, NForm, NFormItem, NInputNumber, useMessage } from 'naive-ui';
import { fetchSystemConfig, updateSystemConfig } from '@/api/modules';

const message = useMessage();
const saving = ref(false);

const settingsForm = ref({
  dishImageLimit: 5
});

const loadSettings = async () => {
  try {
    const res = await fetchSystemConfig('dish_image_limit');
    if (res.data && res.data.configValue) {
      settingsForm.value.dishImageLimit = Number(res.data.configValue) || 5;
    }
  } catch (error) {
    console.error('加载设置失败:', error);
  }
};

const saveSettings = async () => {
  saving.value = true;
  try {
    await updateSystemConfig({
      configKey: 'dish_image_limit',
      configValue: String(settingsForm.value.dishImageLimit)
    });
    message.success('设置保存成功');
  } catch (error: any) {
    console.error('保存设置失败:', error);
    message.error(error.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadSettings();
});
</script>

<style scoped lang="scss">
.settings-view {
  padding: 24px;
}

.glass-card {
  background: var(--gradient-card);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;

  &.hover-rise:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }
}

.table-header {
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 4px 0;
    color: var(--text-primary);
  }

  p {
    margin: 0;
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.settings-content {
  max-width: 800px;
}

.form-tip {
  margin-left: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

:deep(.primary-soft) {
  background: var(--gradient-primary);
  border: none;

  &:hover {
    opacity: 0.9;
  }
}
</style>

