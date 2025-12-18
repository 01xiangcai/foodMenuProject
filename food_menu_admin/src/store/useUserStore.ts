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
      token.value = result.data;
      localStorage.setItem('fm_token', result.data);
      await loadProfile();
      return result;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    token.value = null;
    localStorage.removeItem('fm_token');
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

