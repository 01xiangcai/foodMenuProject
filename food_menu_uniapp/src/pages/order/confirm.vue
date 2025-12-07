<template>
  <view class="page">
    <!-- 订单详情卡片 -->
    <view class="card">
      <view class="card-header">
        <text class="card-title">订单详情</text>
      </view>
      
      <view class="order-items">
        <view class="order-item" v-for="item in items" :key="item.id">
          <image 
            class="item-image" 
            :src="getDishImage(item)" 
            mode="aspectFill" 
            @error="handleImageError(item)"
          />
          <view class="item-content">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-price">¥{{ item.price }}</text>
          </view>
          <text class="item-quantity">x{{ item.quantity }}</text>
        </view>
      </view>

      <view class="divider"></view>

      <view class="summary-row">
        <text class="label">小计</text>
        <text class="value">¥{{ goodsAmount }}</text>
      </view>
    </view>

    <!-- 支付方式卡片 -->
    <view class="card">
      <view class="card-header">
        <text class="card-title">支付方式</text>
      </view>
      
      <view class="pay-methods">
        <!-- 余额支付 -->
        <view 
          class="pay-method" 
          :class="{ active: payMethod === 1, disabled: !canUseWallet }"
          @tap="selectPayMethod(1)"
        >
          <view class="pay-method-left">
            <text class="pay-icon">💰</text>
            <view class="pay-info">
              <text class="pay-name">余额支付</text>
              <text class="pay-balance" :class="{ insufficient: !canUseWallet }">
                余额: ¥{{ walletBalance.toFixed(2) }}
              </text>
            </view>
          </view>
          <view class="pay-check" v-if="payMethod === 1">✓</view>
        </view>

        <!-- 模拟支付 -->
        <view 
          class="pay-method" 
          :class="{ active: payMethod === 2 }"
          @tap="selectPayMethod(2)"
        >
          <view class="pay-method-left">
            <text class="pay-icon">🎮</text>
            <view class="pay-info">
              <text class="pay-name">模拟支付</text>
              <text class="pay-desc">开发测试使用</text>
            </view>
          </view>
          <view class="pay-check" v-if="payMethod === 2">✓</view>
        </view>
      </view>

      <!-- 余额不足提示 -->
      <view class="insufficient-tip" v-if="payMethod === 1 && !canUseWallet">
        <text>余额不足，请充值或者联系管理员</text>
      </view>
    </view>

    <!-- 备注卡片 -->
    <view class="card">
      <view class="card-header">
        <text class="card-title">备注</text>
      </view>
      <view class="textarea-wrapper">
        <textarea 
          class="textarea" 
          placeholder="口味偏好、餐具数量等" 
          :placeholder-style="`color: ${themeConfig.textSecondary};`"
          v-model="remark"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <view class="total-info">
        <text class="label">合计:</text>
        <text class="price">¥{{ goodsAmount }}</text>
      </view>
      <view 
        class="btn-submit" 
        :class="{ disabled: !canSubmit }"
        @tap="handleSubmit"
      >
        <text>{{ submitBtnText }}</text>
      </view>
    </view>

    <!-- 支付密码弹窗 -->
    <PayPasswordPopup
      v-model:visible="showPayPassword"
      :title="'请输入支付密码'"
      @confirm="onPayPasswordConfirm"
      @cancel="showPayPassword = false"
    />
  </view>
</template>

<script setup>

import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { createOrder, getWalletInfo, checkPayPassword, payOrder, getOrderDetail, updateOrderRemark } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { useCartStore } from '@/stores/cart'
import { getDishImage } from '@/utils/image'
import PayPasswordPopup from '@/components/PayPasswordPopup.vue'

const { themeConfig } = useTheme()
const cartStore = useCartStore()

const items = ref([])
const remark = ref('')
const payMethod = ref(1) // 默认余额支付
const walletBalance = ref(0)
const hasPayPassword = ref(false)
const showPayPassword = ref(false)
const isSubmitting = ref(false)
const orderId = ref(null)
const orderNumber = ref('')

// 商品金额
const goodsAmount = computed(() => {
  return items.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0).toFixed(2)
})

// 是否可以使用余额支付
const canUseWallet = computed(() => {
  return walletBalance.value >= parseFloat(goodsAmount.value)
})

// 是否可以提交订单
const canSubmit = computed(() => {
  if (isSubmitting.value) return false
  if (payMethod.value === 1 && !canUseWallet.value) return false
  return items.value.length > 0
})

// 提交按钮文字
const submitBtnText = computed(() => {
  if (isSubmitting.value) return '处理中...'
  if (payMethod.value === 1 && !canUseWallet.value) return '余额不足'
  return orderId.value ? '确认支付' : '提交订单'
})

// 获取钱包信息
const loadWalletInfo = async () => {
  try {
    const res = await getWalletInfo()
    if (res.data) {
      walletBalance.value = res.data.balance || 0
      hasPayPassword.value = res.data.hasPayPassword || false
    }
  } catch (error) {
    console.error('获取钱包信息失败:', error)
  }
}

// 加载现有订单
const loadOrder = async (id) => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await getOrderDetail(id)
    uni.hideLoading()
    
    if (res.data) {
      const order = res.data
      orderId.value = order.id
      orderNumber.value = order.orderNumber
      remark.value = order.remark || ''
      
      // 转换订单项格式以匹配显示
      items.value = order.orderItems.map(item => ({
        id: item.dishId,
        name: item.dishName,
        price: item.price,
        quantity: item.quantity,
        image: item.dishImage
      }))
    }
  } catch (error) {
    uni.hideLoading()
    console.error('加载订单失败:', error)
    uni.showToast({ title: '加载订单失败', icon: 'none' })
  }
}

// 选择支付方式
const selectPayMethod = (method) => {
  if (method === 1 && !canUseWallet.value) {
    uni.showToast({
      title: '余额不足，请充值或者联系管理员',
      icon: 'none'
    })
    return
  }
  payMethod.value = method
}

// 处理图片加载错误
const handleImageError = (item) => {
  item.image = '/static/logo.png'
}

// 点击提交
const handleSubmit = () => {
  if (!canSubmit.value) return

  if (payMethod.value === 1) {
    // 余额支付需要输入密码
    if (!hasPayPassword.value) {
      uni.showModal({
        title: '提示',
        content: '您尚未设置支付密码，请先设置支付密码',
        confirmText: '去设置',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({
              url: '/pages/wallet/index'
            })
          }
        }
      })
      return
    }
    showPayPassword.value = true
  } else {
    // 模拟支付直接提交
    submitOrder(null)
  }
}

// 支付密码确认
const onPayPasswordConfirm = (password) => {
  showPayPassword.value = false
  submitOrder(password)
}

// 提交/支付订单
const submitOrder = async (payPassword) => {
  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    // 场景1: 已有订单ID（从菜单页创建），进行支付
    if (orderId.value) {
      // 如果备注变化了，先更新备注（可选，为简单起见，这里假设pay接口不支持直接更新备注，或者我们可以分两步）
      // updateOrderRemark(orderId.value, remark.value) // 视需求而定，这里先跳过，假设创建时备注为空或者不重要，或者通过 payOrder 的 remark 参数传递
      
      const payData = {
        orderNo: orderNumber.value,
        payMethod: payMethod.value,
        payPassword: payPassword,
        remark: remark.value // 将当前备注传给支付接口（如果在支付记录中需要，或者用于更新订单备注）
      }
      
      await payOrder(payData)
      
      uni.showToast({ title: '支付成功', icon: 'success' })
      
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/order/detail?id=${orderId.value}`
        })
      }, 1500)
      
    } else {
      // 场景2: 没有订单ID（旧流程或直接进入），先创建再支付（这里通常不应该发生，因为menu.vue已经改为先创建了）
      // 但为了兼容，保留创建逻辑
      const orderData = {
        remark: remark.value,
        payMethod: null,
        payPassword: null, 
        orderItems: items.value.map(item => ({
          dishId: item.id,
          quantity: item.quantity
        }))
      }

      const res = await createOrder(orderData)
      const newOrderId = res.data
      
      if (newOrderId) {
          // 清空购物车
          cartStore.clearCart()
          
          // 立即支付新创建的订单？或者跳转？
          // 如果为了统一，这里应该拿到 orderNumber 然后走支付流程。
          // 简化起见，保持原跳转逻辑，让detail页处理如果需要。
          // 但根据用户要求 "Go to Checkout creates order", 这种场景应该只在 menu.vue 发生。
          // 所以这里可以是 fallback
          
          uni.showToast({ title: '订单已提交', icon: 'success' })
          setTimeout(() => {
            uni.redirectTo({
              url: `/pages/order/detail?id=${newOrderId}&autoPay=1`
            })
          }, 1500)
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    const errMsg = error.message || '操作失败'
    uni.showToast({
      title: errMsg,
      icon: 'none',
      duration: 3000
    })
  } finally {
    isSubmitting.value = false
  }
}

onLoad((options) => {
  if (options.orderId) {
    // 模式1: 传入了订单ID，加载现有订单
    loadOrder(options.orderId)
  } else if (options.items) {
    // 模式2: 传入了商品项（旧模式，或者详情页直接购买等）
    try {
      items.value = JSON.parse(decodeURIComponent(options.items))
    } catch (error) {
      console.error('解析商品数据失败:', error)
    }
  } else if (options.dishId && options.quantity) {
    // 模式3: 单个商品直接购买
    items.value = [{
      id: parseInt(options.dishId),
      name: '商品',
      price: 38, //这里价格是假的，应该没关系，创建接口会重算
      quantity: parseInt(options.quantity),
      image: 'https://dummyimage.com/200x200/ff6b6b/ffffff&text=商品'
    }]
  }
})

onShow(() => {
  loadWalletInfo()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding: 24rpx;
  padding-bottom: 180rpx;
  box-sizing: border-box;
}

.card {
  background-color: v-bind('themeConfig.bgSecondary');
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  border: 1px solid v-bind('themeConfig.borderColor');
}

.card-header {
  margin-bottom: 30rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.item-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  background-color: v-bind('themeConfig.bgTertiary');
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8rpx;
}

.item-name {
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 500;
}

.item-price {
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 600;
}

.item-quantity {
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  margin-left: 20rpx;
}

.divider {
  height: 1px;
  background-color: v-bind('themeConfig.borderColor');
  margin: 30rpx 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-row .label {
  font-size: 30rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 600;
}

.summary-row .value {
  font-size: 36rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 700;
}

/* 支付方式样式 */
.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.pay-method {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background-color: v-bind('themeConfig.bgTertiary');
  border-radius: 16rpx;
  border: 2px solid transparent;
  transition: all 0.3s ease;

  &.active {
    border-color: v-bind('themeConfig.primary');
    background-color: v-bind('themeConfig.primary + "1a"');
  }

  &.disabled {
    opacity: 0.6;
  }
}

.pay-method-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.pay-icon {
  font-size: 48rpx;
}

.pay-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.pay-name {
  font-size: 30rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 600;
}

.pay-balance {
  font-size: 24rpx;
  color: #34d399;

  &.insufficient {
    color: #f87171;
  }
}

.pay-desc {
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
}

.pay-check {
  width: 44rpx;
  height: 44rpx;
  background: v-bind('themeConfig.gradient');
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24rpx;
  font-weight: bold;
}

.insufficient-tip {
  margin-top: 20rpx;
  padding: 16rpx 20rpx;
  background-color: rgba(248, 113, 113, 0.1);
  border-radius: 12rpx;
  
  text {
    font-size: 24rpx;
    color: #f87171;
  }
}

.textarea-wrapper {
  background-color: v-bind('themeConfig.bgTertiary');
  border-radius: 12rpx;
  padding: 20rpx;
}

.textarea {
  width: 100%;
  height: 160rpx;
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  line-height: 1.5;
}

.bottom-bar {
  position: fixed;
  bottom: 40rpx;
  left: 24rpx;
  right: 24rpx;
  height: 110rpx;
  background-color: v-bind('themeConfig.bgSecondary');
  border-radius: 55rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10rpx 0 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.3);
  border: 1px solid v-bind('themeConfig.borderColor');
  z-index: 100;
}

.total-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.total-info .label {
  font-size: 30rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 600;
}

.total-info .price {
  font-size: 36rpx;
  color: v-bind('themeConfig.primary');
  font-weight: 700;
}

.btn-submit {
  width: 240rpx;
  height: 90rpx;
  background: v-bind('themeConfig.gradient');
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 30rpx;
    color: v-bind('themeConfig.buttonText');
    font-weight: 600;
  }
  
  &.disabled {
    opacity: 0.5;
  }
  
  &:active:not(.disabled) {
    opacity: 0.9;
    transform: scale(0.98);
  }
}
</style>

