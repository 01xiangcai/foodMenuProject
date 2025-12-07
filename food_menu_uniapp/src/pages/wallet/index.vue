<template>
  <view class="page">
    <!-- 资产卡片 -->
    <view class="wallet-card glass-card">
      <view class="wallet-header">
        <text class="title">我的钱包</text>
        <view class="password-status" @tap="handlePasswordClick">
          <text class="icon">{{ hasPayPassword ? '🔒' : '🔓' }}</text>
          <text class="text">{{ hasPayPassword ? '已设置密码' : '设置密码' }}</text>
        </view>
      </view>
      
      <view class="balance-section">
        <view class="balance-item main">
          <text class="label">可用余额</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="amount">{{ formatMoney(walletInfo.balance) }}</text>
          </view>
        </view>
        <view class="divider"></view>
        <view class="balance-item">
          <text class="label">冻结金额</text>
          <view class="amount-row">
            <text class="currency small">¥</text>
            <text class="amount small">{{ formatMoney(walletInfo.frozenAmount) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 流水列表 -->
    <view class="transaction-section glass-card">
      <view class="section-header">
        <text class="title">交易记录</text>
      </view>
      
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="transactions.length === 0" class="empty-state">
        <text class="icon">📝</text>
        <text class="text">暂无交易记录</text>
      </view>
      
      <view v-else class="transaction-list">
        <view 
          v-for="item in transactions" 
          :key="item.id" 
          class="transaction-item"
        >
          <view class="item-left">
            <view class="type-icon" :class="getTypeClass(item.transType)">
              {{ getTypeIcon(item.transType) }}
            </view>
            <view class="item-info">
              <text class="type-name">{{ getTypeName(item.transType) }}</text>
              <text class="time">{{ item.createTime }}</text>
            </view>
          </view>
          <view class="item-right">
            <text class="amount" :class="item.amount >= 0 ? 'income' : 'expense'">
              {{ item.amount >= 0 ? '+' : '' }}{{ item.amount.toFixed(2) }}
            </text>
            <text class="balance-after">余额: ¥{{ item.balanceAfter?.toFixed(2) || '0.00' }}</text>
          </view>
        </view>
      </view>
      
      <!-- 加载更多 -->
      <view v-if="hasMore && !loading" class="load-more" @tap="loadMore">
        <text>加载更多</text>
      </view>
    </view>

    <!-- 设置/修改密码弹窗 -->
    <view class="modal-mask" v-if="showPasswordModal" @tap="showPasswordModal = false"></view>
    <view class="password-modal" :class="{ show: showPasswordModal }">
      <view class="modal-content glass-card">
        <view class="modal-header">
          <text class="title">{{ getModalTitle() }}</text>
          <view class="close-btn" @tap="showPasswordModal = false">
            <text>✕</text>
          </view>
        </view>
        <view class="modal-body">
          <text class="tip">{{ getModalTip() }}</text>
          <view class="password-input-display">
            <view 
              v-for="i in 6" 
              :key="i" 
              class="password-dot"
              :class="{ filled: getCurrentPassword().length >= i }"
            ></view>
          </view>
          <!-- 数字键盘 -->
          <view class="number-keyboard">
            <view 
              v-for="num in [1,2,3,4,5,6,7,8,9,'',0,'del']" 
              :key="num" 
              class="key"
              :class="{ empty: num === '', del: num === 'del' }"
              @tap="handleKeyPress(num)"
            >
              <text v-if="num === 'del'">⌫</text>
              <text v-else>{{ num }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getWalletInfo, getWalletTransactions, setPayPassword, checkPayPassword, updatePayPassword, verifyPayPassword } from '@/api/index'
import { useTheme } from '@/stores/theme'

const { themeConfig } = useTheme()

const walletInfo = ref({
  balance: 0,
  frozenAmount: 0
})
const hasPayPassword = ref(false)
const transactions = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)

// 密码弹窗
const showPasswordModal = ref(false)
const newPassword = ref('')
const passwordMode = ref('set') // 'set' | 'change'
const oldPassword = ref('')
const changeStep = ref(1) // 1=输入旧密码, 2=输入新密码

// 格式化金额
const formatMoney = (value) => {
  return (value || 0).toFixed(2)
}

// 获取交易类型名称
const getTypeName = (type) => {
  const map = {
    1: '充值',
    2: '消费',
    3: '冻结',
    4: '退款'
  }
  return map[type] || '未知'
}

// 获取交易类型图标
const getTypeIcon = (type) => {
  const map = {
    1: '💰',
    2: '🛒',
    3: '❄️',
    4: '↩️'
  }
  return map[type] || '📝'
}

// 获取交易类型样式类
const getTypeClass = (type) => {
  const map = {
    1: 'recharge',
    2: 'consume',
    3: 'freeze',
    4: 'refund'
  }
  return map[type] || ''
}

// 加载钱包信息
const loadWalletInfo = async () => {
  try {
    const res = await getWalletInfo()
    if (res.data) {
      walletInfo.value = res.data
      hasPayPassword.value = res.data.hasPayPassword || false
    }
  } catch (error) {
    console.error('获取钱包信息失败:', error)
  }
}

// 加载交易记录
const loadTransactions = async (refresh = false) => {
  if (loading.value) return
  
  if (refresh) {
    page.value = 1
    transactions.value = []
    hasMore.value = true
  }
  
  loading.value = true
  try {
    const res = await getWalletTransactions(page.value, pageSize)
    if (res.data?.records) {
      if (refresh) {
        transactions.value = res.data.records
      } else {
        transactions.value = [...transactions.value, ...res.data.records]
      }
      hasMore.value = transactions.value.length < res.data.total
    }
  } catch (error) {
    console.error('获取交易记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    page.value++
    loadTransactions()
  }
}

// 处理密码点击
const handlePasswordClick = () => {
  if (hasPayPassword.value) {
    // 已设置密码，弹出操作菜单
    uni.showActionSheet({
      itemList: ['修改密码', '忘记密码'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 修改密码
          passwordMode.value = 'change'
          changeStep.value = 1
          oldPassword.value = ''
          newPassword.value = ''
          showPasswordModal.value = true
        } else if (res.tapIndex === 1) {
          // 忘记密码
          uni.showModal({
            title: '忘记密码',
            content: '请联系管理员重置支付密码',
            showCancel: false,
            confirmText: '知道了'
          })
        }
      }
    })
  } else {
    // 未设置密码，开始设置
    passwordMode.value = 'set'
    newPassword.value = ''
    showPasswordModal.value = true
  }
}

// 获取当前输入的密码（根据模式和步骤）
const getCurrentPassword = () => {
  if (passwordMode.value === 'change' && changeStep.value === 1) {
    return oldPassword.value
  }
  return newPassword.value
}

// 设置当前输入的密码
const setCurrentPassword = (val) => {
  if (passwordMode.value === 'change' && changeStep.value === 1) {
    oldPassword.value = val
  } else {
    newPassword.value = val
  }
}

// 获取弹窗标题
const getModalTitle = () => {
  if (passwordMode.value === 'set') return '设置支付密码'
  if (changeStep.value === 1) return '验证原密码'
  return '设置新密码'
}

// 获取提示文字
const getModalTip = () => {
  if (passwordMode.value === 'set') return '请设置6位数字支付密码'
  if (changeStep.value === 1) return '请输入当前支付密码'
  return '请输入新的6位数字密码'
}

// 处理数字键盘按键
const handleKeyPress = async (key) => {
  if (key === '') return
  
  const current = getCurrentPassword()
  
  if (key === 'del') {
    setCurrentPassword(current.slice(0, -1))
    return
  }
  
  if (current.length >= 6) return
  
  const updated = current + key
  setCurrentPassword(updated)
  
  // 输入完成6位后处理
  if (updated.length === 6) {
    if (passwordMode.value === 'set') {
      // 设置密码
      try {
        await setPayPassword(updated)
        uni.showToast({
          title: '设置成功',
          icon: 'success'
        })
        hasPayPassword.value = true
        showPasswordModal.value = false
      } catch (error) {
        uni.showToast({
          title: error.message || '设置失败',
          icon: 'none'
        })
        newPassword.value = ''
      }
    } else if (passwordMode.value === 'change') {
      if (changeStep.value === 1) {
        // 验证旧密码
        try {
          const res = await verifyPayPassword(updated)
          if (res.data === true) {
            // 验证通过，进入第二步
            changeStep.value = 2
            newPassword.value = ''
          } else {
            uni.showToast({
              title: '原密码错误',
              icon: 'none'
            })
            oldPassword.value = ''
          }
        } catch (error) {
          uni.showToast({
            title: error.message || '原密码错误',
            icon: 'none'
          })
          oldPassword.value = ''
        }
      } else {
        // 修改密码
        try {
          await updatePayPassword(oldPassword.value, updated)
          uni.showToast({
            title: '修改成功',
            icon: 'success'
          })
          showPasswordModal.value = false
        } catch (error) {
          uni.showToast({
            title: error.message || '修改失败',
            icon: 'none'
          })
          // 重新开始
          changeStep.value = 1
          oldPassword.value = ''
          newPassword.value = ''
        }
      }
    }
  }
}

onShow(() => {
  loadWalletInfo()
  loadTransactions(true)
})

onMounted(() => {
  loadWalletInfo()
  loadTransactions(true)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding: 20rpx;
}

.glass-card {
  background: v-bind('themeConfig.cardBg');
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('themeConfig.cardBorder');
  border-radius: 24rpx;
  margin-bottom: 20rpx;
}

.wallet-card {
  padding: 40rpx;
  
  .wallet-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .title {
      font-size: 32rpx;
      font-weight: 700;
      color: v-bind('themeConfig.textPrimary');
    }
    
    .password-status {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 8rpx 16rpx;
      background: v-bind('themeConfig.inputBg');
      border-radius: 20rpx;
      
      .icon {
        font-size: 24rpx;
      }
      
      .text {
        font-size: 22rpx;
        color: v-bind('themeConfig.textSecondary');
      }
    }
  }
  
  .balance-section {
    display: flex;
    align-items: center;
    
    .balance-item {
      flex: 1;
      
      &.main {
        flex: 2;
      }
      
      .label {
        display: block;
        font-size: 24rpx;
        color: v-bind('themeConfig.textSecondary');
        margin-bottom: 10rpx;
      }
      
      .amount-row {
        display: flex;
        align-items: baseline;
        
        .currency {
          font-size: 32rpx;
          font-weight: 600;
          color: v-bind('themeConfig.primaryColor');
          
          &.small {
            font-size: 24rpx;
            color: v-bind('themeConfig.textSecondary');
          }
        }
        
        .amount {
          font-size: 56rpx;
          font-weight: 700;
          color: v-bind('themeConfig.primaryColor');
          margin-left: 4rpx;
          
          &.small {
            font-size: 36rpx;
            color: v-bind('themeConfig.textPrimary');
          }
        }
      }
    }
    
    .divider {
      width: 1px;
      height: 80rpx;
      background: v-bind('themeConfig.borderColor');
      margin: 0 30rpx;
    }
  }
}

.transaction-section {
  padding: 30rpx;
  
  .section-header {
    margin-bottom: 20rpx;
    
    .title {
      font-size: 28rpx;
      font-weight: 600;
      color: v-bind('themeConfig.textPrimary');
    }
  }
  
  .loading-state,
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60rpx 0;
    
    .icon {
      font-size: 64rpx;
      margin-bottom: 20rpx;
    }
    
    .text {
      font-size: 28rpx;
      color: v-bind('themeConfig.textSecondary');
    }
  }
  
  .transaction-list {
    .transaction-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 1px solid v-bind('themeConfig.borderColor');
      
      &:last-child {
        border-bottom: none;
      }
      
      .item-left {
        display: flex;
        align-items: center;
        gap: 16rpx;
        
        .type-icon {
          width: 64rpx;
          height: 64rpx;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 28rpx;
          background: v-bind('themeConfig.inputBg');
          
          &.recharge {
            background: rgba(52, 211, 153, 0.2);
          }
          
          &.consume {
            background: rgba(251, 146, 60, 0.2);
          }
          
          &.freeze {
            background: rgba(96, 165, 250, 0.2);
          }
          
          &.refund {
            background: rgba(52, 211, 153, 0.2);
          }
        }
        
        .item-info {
          display: flex;
          flex-direction: column;
          gap: 4rpx;
          
          .type-name {
            font-size: 28rpx;
            font-weight: 500;
            color: v-bind('themeConfig.textPrimary');
          }
          
          .time {
            font-size: 22rpx;
            color: v-bind('themeConfig.textTertiary');
          }
        }
      }
      
      .item-right {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 4rpx;
        
        .amount {
          font-size: 32rpx;
          font-weight: 600;
          
          &.income {
            color: #34d399;
          }
          
          &.expense {
            color: #fb7185;
          }
        }
        
        .balance-after {
          font-size: 22rpx;
          color: v-bind('themeConfig.textTertiary');
        }
      }
    }
  }
  
  .load-more {
    text-align: center;
    padding: 20rpx;
    
    text {
      font-size: 26rpx;
      color: v-bind('themeConfig.primaryColor');
    }
  }
}

// 密码弹窗
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 500;
}

.password-modal {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 501;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  
  &.show {
    transform: translateY(0);
  }
  
  .modal-content {
    border-radius: 32rpx 32rpx 0 0;
    padding: 30rpx;
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30rpx;
      
      .title {
        font-size: 32rpx;
        font-weight: 600;
        color: v-bind('themeConfig.textPrimary');
      }
      
      .close-btn {
        width: 48rpx;
        height: 48rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        text {
          font-size: 28rpx;
          color: v-bind('themeConfig.textSecondary');
        }
      }
    }
    
    .modal-body {
      .tip {
        display: block;
        text-align: center;
        font-size: 26rpx;
        color: v-bind('themeConfig.textSecondary');
        margin-bottom: 40rpx;
      }
      
      .password-input-display {
        display: flex;
        justify-content: center;
        gap: 20rpx;
        margin-bottom: 40rpx;
        
        .password-dot {
          width: 48rpx;
          height: 48rpx;
          border: 2px solid v-bind('themeConfig.borderColor');
          border-radius: 12rpx;
          background: v-bind('themeConfig.inputBg');
          
          &.filled {
            background: v-bind('themeConfig.primaryColor');
            border-color: v-bind('themeConfig.primaryColor');
          }
        }
      }
      
      .number-keyboard {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16rpx;
        
        .key {
          height: 100rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background: v-bind('themeConfig.inputBg');
          border-radius: 16rpx;
          
          &:active {
            background: v-bind('themeConfig.borderColor');
          }
          
          &.empty {
            background: transparent;
          }
          
          &.del {
            background: v-bind('themeConfig.inputBg');
          }
          
          text {
            font-size: 40rpx;
            font-weight: 500;
            color: v-bind('themeConfig.textPrimary');
          }
        }
      }
    }
  }
}
</style>
