<template>
  <div class="settings-view">
    <div class="page-header">
      <h1>系统设置</h1>
      <p>管理系统全局配置</p>
    </div>

    <div class="settings-grid">
      <!-- 菜品图片数量限制 -->
      <div class="setting-card">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
        </div>
        <div class="card-content">
          <div class="card-header">
            <h3>菜品图片数量限制</h3>
            <span class="status-badge" :class="{ 'saving': savingStates.dishImageLimit, 'changed': dishImageLimitChanged }">
              {{ savingStates.dishImageLimit ? '保存中...' : (dishImageLimitChanged ? '未保存' : '已保存') }}
            </span>
          </div>
          <p class="card-description">设置每个菜品最多可上传的图片数量</p>
          <div class="card-control">
            <div class="control-group">
              <NInputNumber
                v-model:value="settings.dishImageLimit"
                :min="1"
                :max="10"
                :show-button="true"
                size="large"
                @update:value="onDishImageLimitChange"
              >
                <template #suffix>张</template>
              </NInputNumber>
              <NButton
                type="primary"
                size="large"
                :loading="savingStates.dishImageLimit"
                :disabled="!dishImageLimitChanged"
                @click="saveDishImageLimit"
                class="confirm-btn"
              >
                {{ savingStates.dishImageLimit ? '保存中' : '确认' }}
              </NButton>
            </div>
            <span class="control-hint">范围: 1-10张</span>
          </div>
        </div>
      </div>

      <!-- 用户自主注册开关 -->
      <div class="setting-card">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="8.5" cy="7" r="4"/>
            <line x1="20" y1="8" x2="20" y2="14"/>
            <line x1="23" y1="11" x2="17" y2="11"/>
          </svg>
        </div>
        <div class="card-content">
          <div class="card-header">
            <h3>用户自主注册</h3>
            <span class="status-badge" :class="{ 'saving': savingStates.userRegisterEnabled }">
              {{ savingStates.userRegisterEnabled ? '保存中...' : '已保存' }}
            </span>
          </div>
          <p class="card-description">控制微信端用户是否可以自主注册账号</p>
          <div class="card-control">
            <NSwitch
              :value="settings.userRegisterEnabled"
              size="large"
              :disabled="savingStates.userRegisterEnabled"
              @update:value="handleUserRegisterChange"
            >
              <template #checked>
                <span class="switch-label">开启</span>
              </template>
              <template #unchecked>
                <span class="switch-label">关闭</span>
              </template>
            </NSwitch>
            <span class="control-hint">
              {{ settings.userRegisterEnabled ? '用户可以自主注册' : '仅管理员可创建账号' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { NInputNumber, NSwitch, NButton, NModal, useMessage, useDialog } from 'naive-ui';
import { fetchSystemConfig, updateSystemConfig } from '@/api/modules';

const message = useMessage();
const dialog = useDialog();

// 设置数据
const settings = reactive({
  dishImageLimit: 5,
  userRegisterEnabled: true
});

// 原始值(用于检测变更)
const originalValues = reactive({
  dishImageLimit: 5
});

// 变更状态
const dishImageLimitChanged = ref(false);

// 保存状态
const savingStates = reactive({
  dishImageLimit: false,
  userRegisterEnabled: false
});

// 加载设置
const loadSettings = async () => {
  try {
    // 加载菜品图片数量限制
    const dishImageRes = await fetchSystemConfig('dish_image_limit');
    if (dishImageRes.data && dishImageRes.data.configValue) {
      const value = Number(dishImageRes.data.configValue) || 5;
      settings.dishImageLimit = value;
      originalValues.dishImageLimit = value;
    }

    // 加载用户注册开关
    const registerRes = await fetchSystemConfig('user_register_enabled');
    if (registerRes.data && registerRes.data.configValue) {
      settings.userRegisterEnabled = registerRes.data.configValue === 'true' || registerRes.data.configValue === '1';
    }
  } catch (error) {
    console.error('加载设置失败:', error);
    message.error('加载设置失败');
  }
};

// 检测菜品图片数量限制变更
const onDishImageLimitChange = (value: number | null) => {
  if (value !== null) {
    dishImageLimitChanged.value = value !== originalValues.dishImageLimit;
  }
};

// 保存菜品图片数量限制
const saveDishImageLimit = async () => {
  savingStates.dishImageLimit = true;
  try {
    await updateSystemConfig({
      configKey: 'dish_image_limit',
      configValue: String(settings.dishImageLimit)
    });
    originalValues.dishImageLimit = settings.dishImageLimit;
    dishImageLimitChanged.value = false;
    message.success('菜品图片数量限制已更新');
  } catch (error: any) {
    console.error('保存失败:', error);
    message.error(error.message || '保存失败');
  } finally {
    savingStates.dishImageLimit = false;
  }
};

// 处理用户注册开关变更
const handleUserRegisterChange = (newValue: boolean) => {
  const action = newValue ? '开启' : '关闭';
  const content = newValue 
    ? '开启后,微信端用户可以自主注册账号。是否确认开启?'
    : '关闭后,新用户将无法自主注册,只能由管理员创建账号。是否确认关闭?';

  dialog.warning({
    title: `确认${action}用户注册功能`,
    content: content,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      savingStates.userRegisterEnabled = true;
      try {
        await updateSystemConfig({
          configKey: 'user_register_enabled',
          configValue: String(newValue)
        });
        // 保存成功后,手动更新本地状态
        settings.userRegisterEnabled = newValue;
        message.success(`用户注册功能已${action}`);
      } catch (error: any) {
        console.error('保存失败:', error);
        message.error(error.message || '保存失败');
      } finally {
        savingStates.userRegisterEnabled = false;
      }
    }
    // 取消时不需要做任何事,因为:value还没有变,UI会自动保持原样
  });
};

onMounted(() => {
  loadSettings();
});
</script>

<style scoped lang="scss">
.settings-view {
  padding: 32px;
  min-height: 100vh;
  background: var(--bg-secondary, #f5f7fa);
}

.page-header {
  margin-bottom: 32px;

  h1 {
    font-size: 32px;
    font-weight: 700;
    margin: 0 0 8px 0;
    color: var(--text-primary, #1a1a1a);
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    margin: 0;
    font-size: 16px;
    color: var(--text-secondary, #666);
  }
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.setting-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 20px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
    border-color: rgba(102, 126, 234, 0.3);
  }
}

.card-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  svg {
    width: 28px;
    height: 28px;
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary, #1a1a1a);
  }
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #e8f5e9;
  color: #2e7d32;
  transition: all 0.3s ease;

  &.saving {
    background: #fff3e0;
    color: #f57c00;
    animation: pulse 1.5s ease-in-out infinite;
  }

  &.changed {
    background: #fff3e0;
    color: #f57c00;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.card-description {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary, #666);
  line-height: 1.6;
}

.card-control {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 8px;

  .control-group {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  :deep(.n-input-number) {
    width: 160px;
  }

  :deep(.n-switch) {
    --n-rail-height: 28px;
    --n-rail-width: 56px;
  }
}

.confirm-btn {
  min-width: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.control-hint {
  font-size: 13px;
  color: var(--text-tertiary, #999);
  font-style: italic;
}

.switch-label {
  font-size: 12px;
  font-weight: 500;
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .settings-view {
    background: var(--bg-secondary, #1a1a1a);
  }

  .page-header h1 {
    color: var(--text-primary, #fff);
  }

  .setting-card {
    background: var(--card-bg, #2a2a2a);
    border-color: rgba(255, 255, 255, 0.1);

    &:hover {
      border-color: rgba(102, 126, 234, 0.5);
    }
  }

  .card-header h3 {
    color: var(--text-primary, #fff);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .settings-view {
    padding: 20px;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }

  .setting-card {
    padding: 20px;
  }

  .card-icon {
    width: 48px;
    height: 48px;

    svg {
      width: 24px;
      height: 24px;
    }
  }
}
</style>
