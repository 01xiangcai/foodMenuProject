<template>
  <div class="login-stage">
    <div class="hologram" />
    <div class="login-card glass-card hover-rise">
      <div class="login-header">
        <p class="badge">FAMILY · ACCESS</p>
        <h2>家庭管理员登录</h2>
        <p>登录后即可管理全家的口味与订单</p>
      </div>
      <form @submit.prevent="handleLogin">
        <label>
          <span>用户名</span>
          <input v-model="username" type="text" placeholder="admin" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="password" type="password" placeholder="••••••" required />
        </label>
        <button :disabled="userStore.loading" class="primary-button" type="submit">
          {{ userStore.loading ? '登录中...' : '进入家庭工作台' }}
        </button>
      </form>
      <p class="tips">默认账号 admin/123456 · 登录信息仅在家庭网络中使用</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/useUserStore';

const userStore = useUserStore();
const username = ref('admin');
const password = ref('123456');
const router = useRouter();

const handleLogin = async () => {
  try {
    await userStore.loginWithPassword(username.value, password.value);
    router.push('/');
  } catch (error) {
    window.alert((error as Error).message);
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
  background: radial-gradient(circle at 20% 20%, rgba(20, 184, 255, 0.3), transparent 40%),
    radial-gradient(circle at 80% 0%, rgba(124, 58, 237, 0.3), transparent 40%),
    #020617;
}

.hologram {
  width: 360px;
  height: 360px;
  border: 1px solid rgba(20, 184, 255, 0.5);
  border-radius: 40%;
  position: absolute;
  animation: rotate 10s linear infinite;
  opacity: 0.3;
}

.login-card {
  position: relative;
  width: min(400px, 90vw);
  z-index: 1;
}

.badge {
  font-size: 12px;
  letter-spacing: 0.4em;
  color: rgba(255, 255, 255, 0.6);
}

.login-header h2 {
  font-size: 28px;
  margin: 8px 0;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

input {
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
}

.tips {
  text-align: center;
  font-size: 12px;
  opacity: 0.6;
  margin-top: 18px;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

