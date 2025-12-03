<template>
  <div class="login-stage">
    <div class="theme-toggle-wrapper">
      <ThemeToggle />
    </div>
    
    <!-- Animated background elements -->
    <div class="orbit-shape shape-1" />
    <div class="orbit-shape shape-2" />
    
    <div class="login-card glass-card hover-rise fade-in-up">
      <div class="login-header">
        <div class="logo-icon">
          <i class="i-tabler-chef-hat" />
        </div>
        <h2 class="gradient-text">家宴菜单</h2>
        <p class="subtitle">一起准备下一顿饭</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-item">
          <label>用户名</label>
          <div class="input-wrapper">
            <i class="i-tabler-user input-icon" />
            <input v-model="username" type="text" placeholder="请输入用户名" required />
          </div>
        </div>
        
        <div class="form-item">
          <label>密码</label>
          <div class="input-wrapper">
            <i class="i-tabler-lock input-icon" />
            <input 
              v-model="password" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder="请输入密码" 
              required 
            />
            <i 
              class="password-toggle"
              :class="showPassword ? 'i-tabler-eye' : 'i-tabler-eye-off'"
              @click="showPassword = !showPassword"
            />
          </div>
        </div>
        
        <button :disabled="userStore.loading" class="primary-button" type="submit">
          <i v-if="!userStore.loading" class="i-tabler-login" />
          <i v-else class="i-tabler-loader spin" />
          <span>{{ userStore.loading ? '登录中...' : '进入家庭工作台' }}</span>
        </button>
      </form>
      
      <div class="tips">
        <div class="tip-item">
          <i class="i-tabler-info-circle" />
          <span>默认账号: admin / 123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/useUserStore';
import ThemeToggle from '@/components/ThemeToggle.vue';
import { useMessage } from 'naive-ui';

const userStore = useUserStore();
const username = ref('admin');
const password = ref('123456');
const showPassword = ref(false);
const router = useRouter();
const message = useMessage();

const handleLogin = async () => {
  if (!username.value || !password.value) {
    message.warning('请输入用户名和密码');
    return;
  }
  
  try {
    await userStore.loginWithPassword(username.value, password.value);
    message.success('欢迎回来');
    router.push('/');
  } catch (error) {
    message.error((error as Error).message || '登录失败');
  }
};
</script>

<style scoped>
.login-stage {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--gradient-bg);
}

/* Animated Background Shapes */
.orbit-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  z-index: 0;
  animation: float 10s ease-in-out infinite;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: var(--primary-color);
  top: -100px;
  left: -100px;
  opacity: 0.2;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: var(--secondary-color, #7c3aed);
  bottom: -50px;
  right: -50px;
  opacity: 0.2;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}

.theme-toggle-wrapper {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
}

.login-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  z-index: 1;
  margin: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 24px;
  border-radius: 16px;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  box-shadow: var(--shadow-glow);
}

.login-header h2 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-left: 4px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: var(--text-tertiary);
  font-size: 18px;
  transition: color 0.3s;
  pointer-events: none;
}

.password-toggle {
  position: absolute;
  right: 16px;
  color: var(--text-tertiary);
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
  z-index: 2;
}

.password-toggle:hover {
  color: var(--text-primary);
}

input {
  width: 100%;
  padding: 12px 44px 12px 44px; /* 右侧留出空间给眼睛图标 */
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  background: var(--bg-body);
  color: var(--text-primary);
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

input:hover {
  border-color: var(--text-tertiary);
}

input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--border-focus);
  background: var(--bg-card);
}

.input-wrapper:focus-within .input-icon {
  color: var(--primary-color);
}

.primary-button {
  margin-top: 8px;
  padding: 14px;
  border-radius: 12px;
  border: none;
  background: var(--gradient-primary);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: var(--shadow-glow);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.primary-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  filter: brightness(1.1);
}

.primary-button:active:not(:disabled) {
  transform: translateY(0);
}

.primary-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.tips {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-secondary);
  display: flex;
  justify-content: center;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-tertiary);
  background: var(--bg-body);
  padding: 6px 12px;
  border-radius: 20px;
}
</style>


