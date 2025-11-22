<template>
  <div ref="chartRef" class="chart-shell" />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps<{
  option: echarts.EChartsOption;
}>();

const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;

const initChart = () => {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);
  chartInstance.setOption(props.option);
};

watch(
  () => props.option,
  (val) => {
    if (chartInstance) {
      chartInstance.setOption(val, true);
    }
  },
  { deep: true }
);

onMounted(() => {
  initChart();
  window.addEventListener('resize', () => chartInstance?.resize());
  // @ts-expect-error attach to window for inline option
  window.echarts = echarts;
});

onUnmounted(() => {
  chartInstance?.dispose();
});
</script>

<style scoped>
.chart-shell {
  width: 100%;
  height: 280px;
}
</style>

