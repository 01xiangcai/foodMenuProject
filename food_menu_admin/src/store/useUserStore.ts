import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { fetchProfile, login } from '@/api/modules';

type UserProfile = {
  id: number;
  name?: string;
  username?: string;
  phone?: string;
  role?: number; // 角色: 0-普通管理员, 1-家庭管理员, 2-超级管理员
  familyId?: number; // 家庭ID
};

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('fm_token'));
  const loading = ref(false);
  const profile = ref<UserProfile | null>(null);

  const loadProfile = async () => {
    if (!token.value) {
      profile.value = null;
      return null;
    }
    try {
      const result = await fetchProfile();
      profile.value = result.data;
      return result.data;
    } catch (error) {
      profile.value = null;
      localStorage.removeItem('fm_token');
      token.value = null;
      throw error;
    }
  };

  const loginWithPassword = async (username: string, password: string) => {
    loading.value = true;
    try {
      const result = await login({ username, password, type: 1 });

      // 兼容新旧格式
      if (typeof result.data === 'object' && result.data.token) {
        // 新格式: { token, userId }
        token.value = result.data.token;
        localStorage.setItem('fm_token', result.data.token);
        if (result.data.userId) {
          localStorage.setItem('user_id', result.data.userId.toString());
        }
      } else if (typeof result.data === 'string') {
        // 旧格式: 只有token字符串
        token.value = result.data;
        localStorage.setItem('fm_token', result.data);
      }

      await loadProfile();
      return result;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    token.value = null;
    localStorage.removeItem('fm_token');
    localStorage.removeItem('user_id');
    profile.value = null;
  };

  const isAdmin = computed(() => {
    return profile.value?.role === 2;
  });

  return {
    token,
    loading,
    profile,
    isAdmin,
    loginWithPassword,
    loadProfile,
    logout
  };
});

