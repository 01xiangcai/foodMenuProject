<template>
  <div class="star-dishes">
    <div v-if="loading" class="loading-state">
      <div class="skeleton" v-for="i in 5" :key="i"></div>
    </div>
    
    <div v-else class="dishes-list">
      <div 
        v-for="(dish, index) in dishes" 
        :key="dish.id" 
        class="dish-item"
        :style="{ '--delay': `${index * 0.1}s` }"
        @click="openDetail(dish)"
      >
        <div class="rank-badge" :class="`rank-${index + 1}`">
          {{ index + 1 }}
        </div>
        
        <div class="dish-image-wrapper">
          <img :src="dish.image" :alt="dish.name" class="dish-image" />
        </div>
        
        <div class="dish-info">
          <div class="info-header">
            <span class="dish-name">{{ dish.name }}</span>
            <span class="dish-price">¥{{ dish.price }}</span>
          </div>
          
          <div class="stats-bar">
            <div class="stat-item">
              <span class="icon">🔥</span>
              <span class="count">{{ dish.orderCount || 0 }}</span>
              <span class="label">次点单</span>
            </div>
            <div class="progress-bg">
              <div 
                class="progress-fill" 
                :style="{ width: `${Math.min((dish.orderCount || 0) / maxCount * 100, 100)}%` }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <NModal v-model:show="showModal" preset="card" style="max-width: 400px" :title="currentDish?.name">
      <div v-if="currentDish" class="dish-detail">
        <div class="detail-image-wrapper">
          <img :src="currentDish.image" class="detail-image" />
          <div class="detail-price">¥{{ currentDish.price }}</div>
        </div>
        
        <div class="detail-content">
          <div class="detail-tags" v-if="currentDish.tags">
            <NTag size="small" type="primary" v-for="tag in currentDish.tags.split(',')" :key="tag">
              {{ tag }}
            </NTag>
          </div>
          
          <div class="detail-section" v-if="currentDish.description">
            <div class="section-label">家庭备注</div>
            <div class="section-text">{{ currentDish.description }}</div>
          </div>
          
          <div class="detail-section" v-if="currentDish.calories">
            <div class="section-label">能量</div>
            <div class="section-text">{{ currentDish.calories }}</div>
          </div>

          <div class="detail-stats">
            <div class="stat-box">
              <div class="stat-value">{{ currentDish.orderCount || 0 }}</div>
              <div class="stat-label">累计点单</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ currentDish.status === 1 ? '在售' : '下架' }}</div>
              <div class="stat-label">当前状态</div>
            </div>
          </div>
        </div>
      </div>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { fetchTopDishes, fetchDishDetail } from '@/api/modules';
import { useMessage, NModal, NTag } from 'naive-ui';

type Dish = {
  id: number;
  name: string;
  image: string;
  price: number;
  orderCount?: number;
  description?: string;
  tags?: string;
  calories?: string;
  status?: number;
};

const dishes = ref<Dish[]>([]);
const loading = ref(true);
const message = useMessage();
const showModal = ref(false);
const currentDish = ref<Dish | null>(null);

const maxCount = computed(() => {
  if (!dishes.value.length) return 100;
  return Math.max(...dishes.value.map(d => d.orderCount || 0)) * 1.2;
});

const loadDishes = async () => {
  try {
    const res = await fetchTopDishes();
    if (res.data) {
      dishes.value = res.data;
    }
  } catch (error) {
    console.error('Failed to load top dishes', error);
    message.error('加载明星菜品失败');
  } finally {
    loading.value = false;
  }
};

const openDetail = async (dish: Dish) => {
  // Initialize with list data which contains orderCount
  currentDish.value = { ...dish };
  showModal.value = true;
  
  // Fetch full details
  try {
    const res = await fetchDishDetail(dish.id);
    if (res.data) {
      // Merge details but preserve orderCount from list as detail API doesn't return it
      currentDish.value = { 
        ...res.data, 
        orderCount: dish.orderCount,
        image: res.data.image || dish.image // Fallback to list image if detail image is missing
      };
    }
  } catch (error) {
    console.error('Failed to load dish detail', error);
  }
};

onMounted(() => {
  loadDishes();
});
</script>

<style scoped>
.star-dishes {
  height: 100%;
  overflow-y: auto;
  padding-right: 4px;
}

/* Scrollbar styling */
.star-dishes::-webkit-scrollbar {
  width: 4px;
}
.star-dishes::-webkit-scrollbar-track {
  background: transparent;
}
.star-dishes::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideInRight 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) backwards;
  animation-delay: var(--delay);
  cursor: pointer;
}

.dish-item:hover {
  transform: translateX(4px) scale(1.02);
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

/* Rank Badge */
.rank-badge {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 14px;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.1);
  flex-shrink: 0; /* Prevent shrinking */
}

.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #000;
  box-shadow: 0 2px 10px rgba(255, 215, 0, 0.3);
}

.rank-2 {
  background: linear-gradient(135deg, #E0E0E0, #B0B0B0);
  color: #000;
  box-shadow: 0 2px 10px rgba(192, 192, 192, 0.3);
}

.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #8B4513);
  color: #fff;
  box-shadow: 0 2px 10px rgba(205, 127, 50, 0.3);
}

/* Explicit styles for rank 4 and 5 to ensure visibility */
/* Explicit styles for rank 4 and 5 to ensure visibility on light background */
.rank-4, .rank-5 {
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #666;
  font-weight: 700;
}

/* Image */
.dish-image-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.1);
}

.dish-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.dish-item:hover .dish-image {
  transform: scale(1.1);
}

/* Info */
.dish-info {
  flex: 1;
  min-width: 0;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.dish-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dish-price {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: 'DIN Alternate', sans-serif;
}

/* Stats Bar */
.stats-bar {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.stat-item .count {
  color: #14b8ff;
  font-weight: 700;
  font-size: 14px;
}

.progress-bg {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #14b8ff, #7c3aed);
  border-radius: 2px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transform: translateX(-100%);
  animation: shimmer 2s infinite;
}

/* Detail Modal Styles */
.dish-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-price {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
  font-family: 'DIN Alternate', sans-serif;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.section-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
}

.detail-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-box {
  background: rgba(255, 255, 255, 0.05);
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

/* Loading Skeleton */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton {
  height: 72px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  animation: pulse 1.5s infinite;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 0.3; }
  100% { opacity: 0.6; }
}
</style>
