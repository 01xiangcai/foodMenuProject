<template>
  <div class="ai-menu-page">
    <section class="glass-card hover-rise">
      <div class="section-header">
        <div>
          <h2>AI菜单生成器</h2>
          <p class="section-desc">让AI帮你规划一周的美食菜单</p>
        </div>
      </div>

      <!-- 配置区域 -->
      <div class="config-section">
        <NForm :model="formData" label-placement="left" label-width="120">
          <NFormItem label="偏好设置">
            <NInput
              v-model:value="formData.preferences"
              type="textarea"
              :rows="3"
              placeholder="例如: 营养均衡、荤素搭配、预算控制在每天50元、少油少盐"
            />
          </NFormItem>
        </NForm>

        <div class="action-buttons">
          <NButton
            class="primary-soft"
            type="primary"
            size="large"
            :loading="generating"
            @click="generateMenu"
          >
            <template #icon>
              <span>🤖</span>
            </template>
            生成一周菜单
          </NButton>
        </div>
      </div>

      <!-- 生成结果 -->
      <div v-if="menuData" class="menu-result">
        <div class="result-header">
          <h3>生成的菜单</h3>
          <NButton secondary @click="exportMenu">导出菜单</NButton>
        </div>

        <div class="week-menu">
          <div
            v-for="(day, index) in weekDays"
            :key="day.key"
            class="day-card"
          >
            <div class="day-header">
              <span class="day-icon">{{ day.icon }}</span>
              <span class="day-name">{{ day.name }}</span>
            </div>

            <div class="meals">
              <div class="meal-section">
                <div class="meal-label">🍳 早餐</div>
                <div class="dish-list">
                  <NTag
                    v-for="(dish, idx) in getDayMeals(day.key, 'breakfast')"
                    :key="idx"
                    type="success"
                    size="small"
                  >
                    {{ dish }}
                  </NTag>
                  <span v-if="!getDayMeals(day.key, 'breakfast').length" class="empty-text">暂无</span>
                </div>
              </div>

              <div class="meal-section">
                <div class="meal-label">🍱 午餐</div>
                <div class="dish-list">
                  <NTag
                    v-for="(dish, idx) in getDayMeals(day.key, 'lunch')"
                    :key="idx"
                    type="info"
                    size="small"
                  >
                    {{ dish }}
                  </NTag>
                  <span v-if="!getDayMeals(day.key, 'lunch').length" class="empty-text">暂无</span>
                </div>
              </div>

              <div class="meal-section">
                <div class="meal-label">🍷 晚餐</div>
                <div class="dish-list">
                  <NTag
                    v-for="(dish, idx) in getDayMeals(day.key, 'dinner')"
                    :key="idx"
                    type="warning"
                    size="small"
                  >
                    {{ dish }}
                  </NTag>
                  <span v-if="!getDayMeals(day.key, 'dinner').length" class="empty-text">暂无</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">🍽️</div>
        <p>还没有生成菜单</p>
        <p class="empty-hint">点击上方按钮,让AI为你规划一周美食</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { NButton, NForm, NFormItem, NInput, NTag, useMessage } from 'naive-ui';
import http from '@/api/http';

const message = useMessage();

// 表单数据
const formData = ref({
  preferences: '营养均衡、荤素搭配、预算控制在每天50元'
});

// 生成状态
const generating = ref(false);

// 菜单数据
const menuData = ref<any>(null);

// 星期数据
const weekDays = [
  { key: 'monday', name: '星期一', icon: '📅' },
  { key: 'tuesday', name: '星期二', icon: '📅' },
  { key: 'wednesday', name: '星期三', icon: '📅' },
  { key: 'thursday', name: '星期四', icon: '📅' },
  { key: 'friday', name: '星期五', icon: '📅' },
  { key: 'saturday', name: '星期六', icon: '🎉' },
  { key: 'sunday', name: '星期日', icon: '🎉' }
];

// 生成菜单
const generateMenu = async () => {
  generating.value = true;
  try {
    const res = await http.post('/admin/ai/generate-weekly-menu', {
      preferences: formData.value.preferences
    });

    if (res.data) {
      menuData.value = res.data;
      message.success('菜单生成成功!');
    } else {
      message.error('生成失败');
    }
  } catch (error: any) {
    console.error('生成菜单失败:', error);
    message.error(error.message || '生成失败,请稍后重试');
  } finally {
    generating.value = false;
  }
};

// 获取某天某餐的菜品
const getDayMeals = (day: string, meal: string): string[] => {
  if (!menuData.value || !menuData.value[day]) {
    return [];
  }
  const meals = menuData.value[day][meal];
  return Array.isArray(meals) ? meals : [];
};

// 导出菜单
const exportMenu = () => {
  if (!menuData.value) {
    message.warning('暂无菜单数据');
    return;
  }

  // 生成文本格式的菜单
  let text = '一周菜单\n\n';
  weekDays.forEach(day => {
    text += `${day.name}\n`;
    text += `  早餐: ${getDayMeals(day.key, 'breakfast').join('、') || '暂无'}\n`;
    text += `  午餐: ${getDayMeals(day.key, 'lunch').join('、') || '暂无'}\n`;
    text += `  晚餐: ${getDayMeals(day.key, 'dinner').join('、') || '暂无'}\n\n`;
  });

  // 创建下载
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `一周菜单_${new Date().toLocaleDateString()}.txt`;
  a.click();
  URL.revokeObjectURL(url);

  message.success('菜单已导出');
};
</script>

<style lang="scss" scoped>
.ai-menu-page {
  padding: 24px;
}

.section-header {
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  .section-desc {
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.config-section {
  margin-bottom: 32px;

  .action-buttons {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}

.menu-result {
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h3 {
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }
}

.week-menu {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.day-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .day-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid var(--border-primary);

    .day-icon {
      font-size: 20px;
    }

    .day-name {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }

  .meals {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .meal-section {
    .meal-label {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-secondary);
      margin-bottom: 8px;
    }

    .dish-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .empty-text {
        font-size: 12px;
        color: var(--text-tertiary);
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 80px 20px;

  .empty-icon {
    font-size: 80px;
    margin-bottom: 16px;
  }

  p {
    font-size: 16px;
    color: var(--text-secondary);
    margin-bottom: 8px;
  }

  .empty-hint {
    font-size: 14px;
    color: var(--text-tertiary);
  }
}

// 深色模式适配
:global(.dark) {
  .day-card {
    background: rgba(255, 255, 255, 0.05);
  }
}
</style>
