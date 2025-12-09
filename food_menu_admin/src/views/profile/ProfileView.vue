<template>
  <div class="profile-page min-h-screen relative overflow-hidden p-4 md:p-8">
    <!-- 背景装饰 (仅在特定区域显示，增加氛围) -->
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden -z-10 pointer-events-none">
      <div class="absolute top-[-10%] left-[-10%] w-[500px] h-[500px] rounded-full bg-primary/20 blur-[100px] animate-blob"></div>
      <div class="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] rounded-full bg-secondary/20 blur-[100px] animate-blob animation-delay-2000"></div>
    </div>

    <div class="max-w-7xl mx-auto h-full flex flex-col gap-6">
      
      <!-- 页面标题区 -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 fade-in-down">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-3">
             <i class="i-tabler-user-circle text-primary text-4xl" />
             个人中心
          </h1>
          <p class="text-gray-500 dark:text-gray-400 mt-2">管理您的个人资料和账户安全设置</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 mt-4">
        <!-- 左侧个人概览卡片 -->
        <div class="lg:col-span-4 xl:col-span-3 flex flex-col gap-6 fade-in-left">
          <section class="glass-card relative overflow-hidden group">
            <div class="relative z-10 flex flex-col items-center pt-8 pb-8 px-6 text-center">
              <!-- 头像区域 -->
              <div class="relative mb-6 group cursor-pointer">
                <div class="w-32 h-32 rounded-full p-1 bg-gradient-to-br from-primary to-secondary/50 shadow-xl shadow-primary/20 transition-transform duration-300 group-hover:scale-105">
                  <div class="w-full h-full rounded-full overflow-hidden bg-white dark:bg-gray-800 flex items-center justify-center relative">
                     <span v-if="!userInfo?.avatar" class="text-5xl font-bold bg-gradient-to-br from-primary to-secondary bg-clip-text text-transparent">
                       {{ avatarText }}
                     </span>
                     <img v-else :src="userInfo.avatar" class="w-full h-full object-cover" />
                     
                     <!-- 上传遮罩 -->
                     <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 backdrop-blur-sm">
                       <i class="i-tabler-camera text-white text-3xl"></i>
                     </div>
                  </div>
                </div>
                <div class="absolute bottom-1 right-2 w-8 h-8 bg-green-500 rounded-full border-4 border-white dark:border-gray-800 flex items-center justify-center shadow-md">
                   <i class="i-tabler-check text-white text-xs"></i>
                </div>
              </div>

              <h2 class="text-2xl font-bold text-gray-800 dark:text-white mb-1">{{ userInfo?.nickname || userInfo?.name || '用户' }}</h2>
              <p class="text-gray-500 dark:text-gray-400 text-sm mb-4">@{{ userInfo?.username }}</p>

              <div class="flex items-center gap-2 mb-6">
                <n-tag :type="roleTagType" round size="small" class="px-3 font-medium">
                  {{ roleText }}
                </n-tag>
                <n-tag :type="userInfo?.status === 1 ? 'success' : 'error'" round size="small" class="px-3" :bordered="false">
                  <template #icon>
                    <i :class="userInfo?.status === 1 ? 'i-tabler-circle-check' : 'i-tabler-circle-x'" />
                  </template>
                  {{ userInfo?.status === 1 ? '状态正常' : '已禁用' }}
                </n-tag>
              </div>

              <!-- 分割线 -->
              <div class="w-full h-[1px] bg-gradient-to-r from-transparent via-gray-200 dark:via-gray-700 to-transparent mb-6"></div>

              <!-- 统计或额外信息 -->
              <div class="w-full grid grid-cols-1 gap-3">
                 <div class="flex flex-col gap-1 p-3 rounded-xl bg-gray-50 dark:bg-white/5 hover:bg-white hover:shadow-md transition-all">
                    <span class="text-xs text-gray-500 dark:text-gray-400">所属家庭</span>
                    <span class="font-semibold text-gray-800 dark:text-gray-200 truncate profile-stat-value">{{ familyName }}</span>
                 </div>
                 <div class="flex flex-col gap-1 p-3 rounded-xl bg-gray-50 dark:bg-white/5 hover:bg-white hover:shadow-md transition-all">
                    <span class="text-xs text-gray-500 dark:text-gray-400">注册时间</span>
                    <span class="font-semibold text-gray-800 dark:text-gray-200 text-sm profile-stat-value">{{ joinDate }}</span>
                 </div>
              </div>
            </div>
            
            <!-- 装饰背景 -->
            <div class="absolute top-0 left-0 w-full h-32 bg-gradient-to-b from-primary/10 to-transparent pointer-events-none"></div>
          </section>
        </div>

        <!-- 右侧详细设置 -->
        <div class="lg:col-span-8 xl:col-span-9 fade-in-right">
          <div class="glass-card h-full flex flex-col relative overflow-hidden">
             <n-tabs type="segment" animated size="large" class="h-full flex flex-col p-6">
               <!-- Tab 1: 基本资料 -->
               <n-tab-pane name="basic" tab="基本资料">
                 <div class="max-w-2xl py-6">
                   <div class="mb-8">
                     <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">个人档案</h3>
                     <p class="text-gray-500 dark:text-gray-400">查看并更新您的基本个人信息。</p>
                   </div>

                   <NForm ref="formRef" :model="formData" :rules="rules" label-placement="top" size="large">
                      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <NFormItem label="账号" path="username">
                           <n-input :value="userInfo?.username" disabled placeholder="系统生成的唯一账号">
                              <template #prefix>
                                <i class="i-tabler-id text-gray-400" />
                              </template>
                           </n-input>
                        </NFormItem>
                        <NFormItem label="姓名/昵称" path="name">
                           <n-input v-model:value="formData.name" placeholder="设置您的对外显示名称">
                              <template #prefix>
                                <i class="i-tabler-user text-gray-400" />
                              </template>
                           </n-input>
                        </NFormItem>
                        <NFormItem label="手机号码" path="phone">
                           <n-input v-model:value="formData.phone" placeholder="绑定的手机号码">
                              <template #prefix>
                                <i class="i-tabler-phone text-gray-400" />
                              </template>
                           </n-input>
                        </NFormItem>
                        <NFormItem label="家庭组" path="family">
                           <n-input :value="familyName" disabled>
                              <template #prefix>
                                <i class="i-tabler-home text-gray-400" />
                              </template>
                           </n-input>
                        </NFormItem>
                      </div>

                      <div class="flex justify-end items-center mt-8 pt-6 border-t border-gray-100 dark:border-gray-700/50">
                        <n-space align="center" :size="20">
                           <n-button @click="handleReset" size="medium" class="px-6">
                              重置更改
                           </n-button>
                           <n-button type="primary" @click="handleSave" :loading="saving" size="medium" class="px-8 shadow-lg shadow-primary/30">
                              <template #icon><i class="i-tabler-device-floppy"></i></template>
                              保存个人信息
                           </n-button>
                        </n-space>
                      </div>
                   </NForm>
                 </div>
               </n-tab-pane>

               <!-- Tab 2: 安全设置 -->
               <n-tab-pane name="security" tab="安全中心">
                 <div class="max-w-2xl py-6">
                   <div class="mb-8">
                     <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">密码与安全</h3>
                     <p class="text-gray-500 dark:text-gray-400">定期修改密码可以保护您的账户安全。</p>
                   </div>

                   <NForm ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-placement="top" size="large">
                      <NFormItem label="当前密码" path="oldPassword">
                         <n-input
                            v-model:value="passwordForm.oldPassword"
                            type="password"
                            show-password-on="click"
                            placeholder="请输入当前使用的密码"
                         >
                            <template #prefix><i class="i-tabler-lock text-gray-400" /></template>
                         </n-input>
                      </NFormItem>

                      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                         <NFormItem label="新密码" path="newPassword">
                            <n-input
                                v-model:value="passwordForm.newPassword"
                                type="password"
                                show-password-on="click"
                                placeholder="设置新密码 (至少6位)"
                            >
                                <template #prefix><i class="i-tabler-key text-gray-400" /></template>
                            </n-input>
                         </NFormItem>

                         <NFormItem label="确认新密码" path="confirmPassword">
                            <n-input
                                v-model:value="passwordForm.confirmPassword"
                                type="password"
                                show-password-on="click"
                                placeholder="再次输入新密码"
                            >
                                <template #prefix><i class="i-tabler-key text-gray-400" /></template>
                            </n-input>
                         </NFormItem>
                      </div>

                      <div class="bg-yellow-50 dark:bg-yellow-500/10 rounded-xl p-4 mb-6 flex gap-3 text-sm text-yellow-700 dark:text-yellow-400">
                         <i class="i-tabler-alert-triangle text-xl shrink-0 mt-0.5" />
                         <div>
                            <p class="font-bold mb-1">安全提示</p>
                            <p>修改密码成功后，系统将自动退出登录，请使用新密码重新进入系统。</p>
                         </div>
                      </div>

                      <div class="flex justify-end mt-4">
                           <n-button type="primary" @click="handlePasswordChange" :loading="passwordChanging" size="medium" class="px-8 shadow-lg shadow-primary/30">
                              <template #icon><i class="i-tabler-shield-check"></i></template>
                              确认修改密码
                           </n-button>
                      </div>
                   </NForm>
                 </div>
               </n-tab-pane>
             </n-tabs>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NForm, NFormItem, NInput, NButton, NSpace, NTag, NTabs, NTabPane, useMessage, type FormInst, type FormRules } from 'naive-ui'
import { fetchProfile, updateProfile, updateOwnPassword } from '@/api/modules'

const message = useMessage()

// 用户信息
const userInfo = ref<any>(null)
const familyName = ref('加载中...')

// 表单数据
const formData = ref({
  name: '',
  phone: ''
})

// 表单引用
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

// 表单验证规则
const rules: FormRules = {
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ]
}

// 头像文字
const avatarText = computed(() => {
   const name = userInfo.value?.name || userInfo.value?.username || 'U';
   return name.substring(0, 1).toUpperCase();
});


const roleTagType = computed(() => {
   const r = userInfo.value?.role;
   if (r === 2) return 'warning';
   if (r === 1) return 'primary';
   return 'default';
});

// 角色文本
const roleText = computed(() => {
  if (!userInfo.value) return '—'
  const roleMap: Record<number, string> = {
    0: '普通管理员',
    1: '家庭管理员',
    2: '超级管理员'
  }
  return roleMap[userInfo.value.role] || '—'
})

// 密码修改
const passwordFormRef = ref<FormInst | null>(null)
const passwordChanging = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        return value === passwordForm.value.newPassword
      },
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const result = await fetchProfile()
    userInfo.value = result.data
    formData.value.name = result.data.name || ''
    formData.value.phone = result.data.phone || ''
    
    // 显示真实的家庭名称
    if (result.data.familyName) {
      familyName.value = result.data.familyName
    } else if (result.data.familyId) {
       // 兜底显示ID
       familyName.value = `我的家庭 #${result.data.familyId}`
    } else {
       familyName.value = '暂未加入家庭';
    }
  } catch (error) {
    message.error((error as Error).message || '加载用户信息失败')
  }
}

const joinDate = computed(() => {
   if (!userInfo.value?.createTime) return '未知';
   return userInfo.value.createTime.replace('T', ' ').substring(0, 16); 
});

// 保存修改
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    await updateProfile({
      name: formData.value.name,
      phone: formData.value.phone
    })
    
    message.success('保存成功')
    await loadUserInfo()
  } catch (error: any) {
    if (error.message) {
      message.error(error.message)
    }
  } finally {
    saving.value = false
  }
}

// 重置表单
const handleReset = () => {
  formData.value.name = userInfo.value?.name || ''
  formData.value.phone = userInfo.value?.phone || ''
}

// 修改密码
const handlePasswordChange = async () => {
  try {
    await passwordFormRef.value?.validate()
    passwordChanging.value = true
    
    await updateOwnPassword(
      passwordForm.value.oldPassword,
      passwordForm.value.newPassword
    )
    
    message.success('密码修改成功,请重新登录')
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    
    // 1.5秒后跳转到登录页
    setTimeout(() => {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }, 1500)
  } catch (error: any) {
    message.error(error.message || '修改密码失败')
  } finally {
    passwordChanging.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
/* 玻璃拟态卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
  border-radius: 24px;
}

:deep(.dark) .glass-card {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.2);
}

/* 强制修复 Primary 按钮样式 - 用户要求不要白色，改为深色 */
:deep(.n-button--primary-type) {
  /* 使用深色字体以确保在浅色背景上可见，并使用!important覆盖其他样式 */
  color: #333333 !important; 
  font-weight: 600;
}

/* 优化注册时间显示，防止换行 */
.profile-stat-value {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 0.8rem; /* 调小字体以确保显示完整 */
}

/* 动画 */
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.fade-in-down {
  animation: fadeInDown 0.6s ease-out;
}

.fade-in-left {
  animation: fadeInLeft 0.6s ease-out;
}

.fade-in-right {
  animation: fadeInRight 0.6s ease-out;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>
