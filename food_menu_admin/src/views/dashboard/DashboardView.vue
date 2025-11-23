<template>
  <div class="dashboard">
    <div class="grid-auto">
      <StatCard
        v-for="card in statCards"
        :key="card.label"
        class="hover-rise"
        :label="card.label"
        :value="card.value"
        :trend="card.trend"
        :chip="card.chip"
      />
    </div>

    <section class="grid-2">
      <div class="glass-card glow-border hover-rise">
        <h3>实时订单流</h3>
        <OrderStream />
      </div>
      <div class="glass-card hover-rise">
        <h3>菜品热力</h3>
        <EChart :option="dishOption" />
      </div>
    </section>

    <section class="grid-2">
      <div class="glass-card hover-rise nebula-grid">
        <h3>运营脉冲</h3>
        <PulseTimeline />
      </div>
      <div class="glass-card hover-rise">
        <h3>Flavor DNA</h3>
        <FlavorNetwork />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import { useMessage } from 'naive-ui';
import StatCard from '@/components/StatCard.vue';
import OrderStream from '@/components/OrderStream.vue';
import EChart from '@/components/base/EChart.vue';
import PulseTimeline from '@/components/PulseTimeline.vue';
import FlavorNetwork from '@/components/FlavorNetwork.vue';
import { fetchDashboardStats } from '@/api/modules';

type DishRecord = {
  categoryId: number;
  categoryName?: string;
};

type CategoryRecord = {
  id: number;
  name: string;
};

const message = useMessage();

const stats = ref({
  orders: 0,
  dishes: 0,
  categories: 0,
  latestAmount: 0
});

const categorySeries = ref<{ label: string; count: number }[]>([]);
const loading = ref(false);

const formatNumber = (value: number) => new Intl.NumberFormat('zh-CN').format(value);

const favoriteCategory = computed(() => {
  if (!categorySeries.value.length) return '暂无';
  return [...categorySeries.value].sort((a, b) => b.count - a.count)[0]?.label || '暂无';
});

const statCards = computed(() => [
  {
    label: '家庭订单',
    value: formatNumber(stats.value.orders),
    trend: `最新一单 ¥${stats.value.latestAmount.toFixed(2)}`,
    chip: 'Orders'
  },
  {
    label: '上线菜品',
    value: formatNumber(stats.value.dishes),
    trend: `覆盖 ${stats.value.categories} 个分类`,
    chip: 'Dishes'
  },
  {
    label: '分类数量',
    value: formatNumber(stats.value.categories),
    trend: `最受欢迎：${favoriteCategory.value}`,
    chip: 'Categories'
  },
  {
    label: '家庭活跃',
    value: stats.value.orders ? `${Math.min(stats.value.orders, 99)}%` : '0%',
    trend: stats.value.orders ? '大家都在参与' : '等待第一单',
    chip: 'Family'
  }
]);

const dishOption = computed(() => {
  const labels = categorySeries.value.length
    ? categorySeries.value.map((item) => item.label)
    : ['暂无数据'];
  const data = categorySeries.value.length
    ? categorySeries.value.map((item) => item.count)
    : [0];
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis' as const
    },
    xAxis: {
      type: 'category' as const,
      data: labels,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.4)' } }
    },
    yAxis: {
      type: 'value' as const,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    series: [
      {
        type: 'line' as const,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#14b8ff'
        },
        areaStyle: {
          color: 'rgba(20, 184, 255, 0.2)'
        },
        data
      },
      {
        type: 'bar' as const,
        barWidth: 14,
        data,
        itemStyle: {
          borderRadius: 8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#a855f7' },
            { offset: 1, color: 'rgba(168, 85, 247, 0.2)' }
          ])
        }
      }
    ]
  };
});

const buildCategorySeries = (dishes: DishRecord[], categories: CategoryRecord[]) => {
  const counts = new Map<number, number>();
  dishes.forEach((dish) => {
    counts.set(dish.categoryId, (counts.get(dish.categoryId) ?? 0) + 1);
  });
  return categories.map((category) => ({
    label: category.name,
    count: counts.get(category.id) ?? 0
  }));
};

const loadDashboard = async () => {
  loading.value = true;
  try {
    const result = await fetchDashboardStats();
    const ordersData = result.orders?.data;
    const dishesData = result.dishes?.data;
    const categoriesData = result.categories?.data || [];

    stats.value.orders = ordersData?.total ?? 0;
    stats.value.dishes = dishesData?.total ?? 0;
    stats.value.categories = categoriesData.length;
    stats.value.latestAmount = Number(ordersData?.records?.[0]?.amount || 0);

    categorySeries.value = buildCategorySeries(
      (dishesData?.records as DishRecord[]) || [],
      categoriesData as CategoryRecord[]
    );
  } catch (error) {
    message.error((error as Error).message);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadDashboard();
});
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.grid-auto {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}

.grid-2 {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
}

h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

h3::before {
  content: '';
  width: 4px;
  height: 20px;
  background: var(--gradient-primary);
  border-radius: 2px;
}

/* Card Enhancements */
:deep(.glass-card) {
  animation: slideUp 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 24px;
}

:deep(.glass-card:nth-child(1)) {
  animation-delay: 0.1s;
}

:deep(.glass-card:nth-child(2)) {
  animation-delay: 0.2s;
}

.nebula-grid {
  position: relative;
  overflow: hidden;
}

.nebula-grid::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, var(--primary-color) 0%, transparent 70%);
  opacity: 0.1;
  filter: blur(40px);
  pointer-events: none;
}

/* ECharts Container */
:deep(.echarts-container) {
  min-height: 300px;
}

/* Responsive */
@media (max-width: 768px) {
  .dashboard {
    gap: 20px;
  }

  .grid-2 {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  h3 {
    font-size: 18px;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>


