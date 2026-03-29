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

      <!-- 消息撤回时限 -->
      <div class="setting-card">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
            <path d="M3 3v5h5"/>
            <path d="M12 7v5l4 2"/>
          </svg>
        </div>
        <div class="card-content">
          <div class="card-header">
            <h3>消息撤回时限</h3>
            <span class="status-badge" :class="{ 'saving': savingStates.chatRevokeTimeLimit, 'changed': chatRevokeTimeLimitChanged }">
              {{ savingStates.chatRevokeTimeLimit ? '保存中...' : (chatRevokeTimeLimitChanged ? '未保存' : '已保存') }}
            </span>
          </div>
          <p class="card-description">设置聊天消息可撤回的时间限制</p>
          <div class="card-control">
            <div class="control-group">
              <NInputNumber
                v-model:value="settings.chatRevokeTimeLimit"
                :min="1"
                :max="60"
                :show-button="true"
                size="large"
                @update:value="onChatRevokeTimeLimitChange"
              >
                <template #suffix>分钟</template>
              </NInputNumber>
              <NButton
                type="primary"
                size="large"
                :loading="savingStates.chatRevokeTimeLimit"
                :disabled="!chatRevokeTimeLimitChanged"
                @click="saveChatRevokeTimeLimit"
                class="confirm-btn"
              >
                {{ savingStates.chatRevokeTimeLimit ? '保存中' : '确认' }}
              </NButton>
            </div>
            <span class="control-hint">范围: 1-60分钟</span>
          </div>
        </div>
      </div>

      <!-- 聊天消息保留天数 -->
      <div class="setting-card">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <div class="card-content">
          <div class="card-header">
            <h3>聊天消息保留天数</h3>
            <span class="status-badge" :class="{ 'saving': savingStates.chatMessageRetention, 'changed': chatMessageRetentionChanged }">
              {{ savingStates.chatMessageRetention ? '保存中...' : (chatMessageRetentionChanged ? '未保存' : '已保存') }}
            </span>
          </div>
          <p class="card-description">设置聊天消息的保留时长，超期将自动清理</p>
          <div class="card-control">
            <div class="control-group">
              <NInputNumber
                v-model:value="settings.chatMessageRetention"
                :min="1"
                :max="365"
                :show-button="true"
                size="large"
                @update:value="onChatMessageRetentionChange"
              >
                <template #suffix>天</template>
              </NInputNumber>
              <NButton
                type="primary"
                size="large"
                :loading="savingStates.chatMessageRetention"
                :disabled="!chatMessageRetentionChanged"
                @click="saveChatMessageRetention"
                class="confirm-btn"
              >
                {{ savingStates.chatMessageRetention ? '保存中' : '确认' }}
              </NButton>
            </div>
            <span class="control-hint">范围: 1-365天</span>
          </div>
        </div>
      </div>

      <!-- 历史数据保留天数 -->
      <div class="setting-card">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
        </div>
        <div class="card-content">
          <div class="card-header">
            <h3>历史数据保留天数</h3>
            <span class="status-badge" :class="{ 'saving': savingStates.mealHistoryRetention, 'changed': mealHistoryRetentionChanged }">
              {{ savingStates.mealHistoryRetention ? '保存中...' : (mealHistoryRetentionChanged ? '未保存' : '已保存') }}
            </span>
          </div>
          <p class="card-description">设置历史记录的保留时长，超期将自动清理</p>
          <div class="card-control">
            <div class="control-group">
              <NInputNumber
                v-model:value="settings.mealHistoryRetention"
                :min="1"
                :max="365"
                :show-button="true"
                size="large"
                @update:value="onMealHistoryRetentionChange"
              >
                <template #suffix>天</template>
              </NInputNumber>
              <NButton
                type="primary"
                size="large"
                :loading="savingStates.mealHistoryRetention"
                :disabled="!mealHistoryRetentionChanged"
                @click="saveMealHistoryRetention"
                class="confirm-btn"
              >
                {{ savingStates.mealHistoryRetention ? '保存中' : '确认' }}
              </NButton>
            </div>
            <span class="control-hint">范围: 1-365天</span>
          </div>
        </div>
      </div>

      <!-- AI 外部服务配置 -->
      <div class="setting-card">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8" />
            <path d="M21 3v5h-5" />
            <path d="M10 13a2 2 0 1 0 4 0a2 2 0 0 0 -4 0" />
            <path d="M8 21v-1a2 2 0 0 1 2 -2h4a2 2 0 0 1 2 2v1" />
          </svg>
        </div>
        <div class="card-content">
          <div class="card-header">
            <h3>AI 外部服务配置</h3>
            <div class="status-group">
              <span class="status-badge" :class="{ 'saving': savingStates.aiExternalBaseUrl, 'changed': aiExternalBaseUrlChanged }">
                Url: {{ savingStates.aiExternalBaseUrl ? '保存中...' : (aiExternalBaseUrlChanged ? '未保存' : '已保存') }}
              </span>
              <span class="status-badge" :class="{ 'saving': savingStates.aiExternalAppKey, 'changed': aiExternalAppKeyChanged }">
                Key: {{ savingStates.aiExternalAppKey ? '保存中...' : (aiExternalAppKeyChanged ? '未保存' : '已保存') }}
              </span>
            </div>
          </div>
          <p class="card-description">设置外部 AI 客服的服务地址与密钥，建议使用公网/局域网 IP 以同步小程序端</p>
          <div class="card-control ai-config-group">
            <div class="control-item">
              <label>服务地址 (BaseURL)</label>
              <div class="control-group">
                <NInput
                  v-model:value="settings.aiExternalBaseUrl"
                  placeholder="服务地址, 如 http://192.168.1.100:9900"
                  size="large"
                  @input="onAiExternalBaseUrlChange"
                />
                <NButton
                  type="primary"
                  size="large"
                  :loading="savingStates.aiExternalBaseUrl"
                  :disabled="!aiExternalBaseUrlChanged"
                  @click="saveAiExternalBaseUrl"
                  class="confirm-btn"
                >
                  确认
                </NButton>
              </div>
            </div>
            <div class="control-item">
              <label>应用密钥 (AppKey)</label>
              <div class="control-group">
                <NInput
                  v-model:value="settings.aiExternalAppKey"
                  type="password"
                  show-password-on="click"
                  placeholder="应用密钥 AppKey"
                  size="large"
                  @input="onAiExternalAppKeyChange"
                />
                <NButton
                  type="primary"
                  size="large"
                  :loading="savingStates.aiExternalAppKey"
                  :disabled="!aiExternalAppKeyChanged"
                  @click="saveAiExternalAppKey"
                  class="confirm-btn"
                >
                  确认
                </NButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { NInput, NInputNumber, NSwitch, NButton, useMessage, useDialog } from 'naive-ui';
import { fetchSystemConfig, updateSystemConfig } from '@/api/modules';

const message = useMessage();
const dialog = useDialog();

// 设置数据
const settings = reactive({
  dishImageLimit: 5,
  userRegisterEnabled: true,
  chatRevokeTimeLimit: 2,
  chatMessageRetention: 30,
  mealHistoryRetention: 30,
  aiExternalAppKey: '',
  aiExternalBaseUrl: ''
});

// 原始值(用于检测变更)
const originalValues = reactive({
  dishImageLimit: 5,
  chatRevokeTimeLimit: 2,
  chatMessageRetention: 30,
  mealHistoryRetention: 30,
  aiExternalAppKey: '',
  aiExternalBaseUrl: ''
});

// 变更状态
const dishImageLimitChanged = ref(false);
const chatRevokeTimeLimitChanged = ref(false);
const chatMessageRetentionChanged = ref(false);
const mealHistoryRetentionChanged = ref(false);
const aiExternalAppKeyChanged = ref(false);
const aiExternalBaseUrlChanged = ref(false);

// 保存状态
const savingStates = reactive({
  dishImageLimit: false,
  userRegisterEnabled: false,
  chatRevokeTimeLimit: false,
  chatMessageRetention: false,
  mealHistoryRetention: false,
  aiExternalAppKey: false,
  aiExternalBaseUrl: false
});

// 加载设置
const loadSettings = async () => {
  try {
    const dishImageRes = await fetchSystemConfig('dish_image_limit');
    if (dishImageRes.data?.configValue) {
      const value = Number(dishImageRes.data.configValue) || 5;
      settings.dishImageLimit = value;
      originalValues.dishImageLimit = value;
    }

    const registerRes = await fetchSystemConfig('user_register_enabled');
    if (registerRes.data?.configValue) {
      settings.userRegisterEnabled = registerRes.data.configValue === 'true' || registerRes.data.configValue === '1';
    }

    const revokeRes = await fetchSystemConfig('chat_revoke_time_limit');
    if (revokeRes.data?.configValue) {
      const value = Number(revokeRes.data.configValue) || 2;
      settings.chatRevokeTimeLimit = value;
      originalValues.chatRevokeTimeLimit = value;
    }

    const chatRetentionRes = await fetchSystemConfig('chat_message_retention_days');
    if (chatRetentionRes.data?.configValue) {
      const value = Number(chatRetentionRes.data.configValue) || 30;
      settings.chatMessageRetention = value;
      originalValues.chatMessageRetention = value;
    }

    const mealRetentionRes = await fetchSystemConfig('meal.history.retention.days');
    if (mealRetentionRes.data?.configValue) {
      const value = Number(mealRetentionRes.data.configValue) || 30;
      settings.mealHistoryRetention = value;
      originalValues.mealHistoryRetention = value;
    }
    
    const aiKeyRes = await fetchSystemConfig('ai_external_app_key');
    if (aiKeyRes.data?.configValue) {
      settings.aiExternalAppKey = aiKeyRes.data.configValue;
      originalValues.aiExternalAppKey = aiKeyRes.data.configValue;
    }

    const aiUrlRes = await fetchSystemConfig('ai_external_base_url');
    if (aiUrlRes.data?.configValue) {
      settings.aiExternalBaseUrl = aiUrlRes.data.configValue;
      originalValues.aiExternalBaseUrl = aiUrlRes.data.configValue;
    }
  } catch (error) {
    console.error('加载设置失败:', error);
    message.error('加载设置失败');
  }
};

const onDishImageLimitChange = (value: number | null) => {
  if (value !== null) dishImageLimitChanged.value = value !== originalValues.dishImageLimit;
};

const saveDishImageLimit = async () => {
  savingStates.dishImageLimit = true;
  try {
    await updateSystemConfig({ configKey: 'dish_image_limit', configValue: String(settings.dishImageLimit) });
    originalValues.dishImageLimit = settings.dishImageLimit;
    dishImageLimitChanged.value = false;
    message.success('菜品图片数量限制已更新');
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    savingStates.dishImageLimit = false;
  }
};

const handleUserRegisterChange = (newValue: boolean) => {
  dialog.warning({
    title: `确认${newValue ? '开启' : '关闭'}用户注册功能`,
    content: newValue ? '开启后,微信端用户可以自主注册账号。是否确认开启?' : '关闭后,新用户将无法自主注册,只能由管理员创建账号。是否确认关闭?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      savingStates.userRegisterEnabled = true;
      try {
        await updateSystemConfig({ configKey: 'user_register_enabled', configValue: String(newValue) });
        settings.userRegisterEnabled = newValue;
        message.success(`用户注册功能已${newValue ? '开启' : '关闭'}`);
      } catch (error: any) {
        message.error(error.message || '保存失败');
      } finally {
        savingStates.userRegisterEnabled = false;
      }
    }
  });
};

const onChatRevokeTimeLimitChange = (value: number | null) => {
  if (value !== null) chatRevokeTimeLimitChanged.value = value !== originalValues.chatRevokeTimeLimit;
};

const saveChatRevokeTimeLimit = async () => {
  savingStates.chatRevokeTimeLimit = true;
  try {
    await updateSystemConfig({ configKey: 'chat_revoke_time_limit', configValue: String(settings.chatRevokeTimeLimit) });
    originalValues.chatRevokeTimeLimit = settings.chatRevokeTimeLimit;
    chatRevokeTimeLimitChanged.value = false;
    message.success('消息撤回时限已更新');
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    savingStates.chatRevokeTimeLimit = false;
  }
};

const onChatMessageRetentionChange = (value: number | null) => {
  if (value !== null) chatMessageRetentionChanged.value = value !== originalValues.chatMessageRetention;
};

const saveChatMessageRetention = async () => {
  savingStates.chatMessageRetention = true;
  try {
    await updateSystemConfig({ configKey: 'chat_message_retention_days', configValue: String(settings.chatMessageRetention) });
    originalValues.chatMessageRetention = settings.chatMessageRetention;
    chatMessageRetentionChanged.value = false;
    message.success('聊天消息保留天数已更新');
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    savingStates.chatMessageRetention = false;
  }
};

const onMealHistoryRetentionChange = (value: number | null) => {
  if (value !== null) mealHistoryRetentionChanged.value = value !== originalValues.mealHistoryRetention;
};

const saveMealHistoryRetention = async () => {
  savingStates.mealHistoryRetention = true;
  try {
    await updateSystemConfig({ configKey: 'meal.history.retention.days', configValue: String(settings.mealHistoryRetention) });
    originalValues.mealHistoryRetention = settings.mealHistoryRetention;
    mealHistoryRetentionChanged.value = false;
    message.success('历史数据保留天数已更新');
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    savingStates.mealHistoryRetention = false;
  }
};

const onAiExternalBaseUrlChange = (value: string) => {
  aiExternalBaseUrlChanged.value = value !== originalValues.aiExternalBaseUrl;
};

const saveAiExternalBaseUrl = async () => {
  savingStates.aiExternalBaseUrl = true;
  try {
    await updateSystemConfig({ configKey: 'ai_external_base_url', configValue: settings.aiExternalBaseUrl });
    originalValues.aiExternalBaseUrl = settings.aiExternalBaseUrl;
    aiExternalBaseUrlChanged.value = false;
    message.success('AI 服务地址已更新');
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    savingStates.aiExternalBaseUrl = false;
  }
};

const onAiExternalAppKeyChange = (value: string) => {
  aiExternalAppKeyChanged.value = value !== originalValues.aiExternalAppKey;
};

const saveAiExternalAppKey = async () => {
  savingStates.aiExternalAppKey = true;
  try {
    await updateSystemConfig({ configKey: 'ai_external_app_key', configValue: settings.aiExternalAppKey });
    originalValues.aiExternalAppKey = settings.aiExternalAppKey;
    aiExternalAppKeyChanged.value = false;
    message.success('AI AppKey 已更新');
  } catch (error: any) {
    message.error(error.message || '保存失败');
  } finally {
    savingStates.aiExternalAppKey = false;
  }
};

</script>

<style scoped lang="scss">
.settings-view {
  min-height: 100vh;
  padding: 40px;
  background: var(--bg-secondary, #f5f7fa);

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

  .ai-config-group {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .control-item {
      display: flex;
      flex-direction: column;
      gap: 6px;

      label {
        font-size: 13px;
        font-weight: 500;
        color: var(--text-tertiary, #888);
      }
    }
  }

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

  .status-group {
    display: flex;
    gap: 8px;
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
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .settings-view {
    background: var(--bg-secondary, #1a1a1a);

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
}

// 响应式设计
@media (max-width: 768px) {
  .settings-view {
    padding: 20px;

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
}
</style>
