<template>
  <view class="page">
    <!-- 订单状态 -->
    <view class="status-card" :class="getStatusClass(order.status)">
      <text class="status-icon">{{ getStatusIcon(order.status) }}</text>
      <view class="status-content">
        <text class="status-text">{{ getStatusText(order.status) }}</text>
        <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
      </view>
    </view>

    <!-- 订单商品 -->
    <view class="section-card">
      <view class="card-header">
        <view class="header-line"></view>
        <text class="card-title">订单商品</text>
      </view>
      <view class="order-items">
        <view 
          class="order-item" 
          :class="{ 'not-published': item.isPublished === 0 }"
          v-for="item in order.items" 
          :key="item.id" 
          @tap="navigateToDishDetail(item.dishId, item.dishStatus)"
        >
          <image class="item-image" :src="getDishImage(item)" mode="aspectFill" />
          <view class="item-info">
            <view class="item-name-row">
              <text class="item-name">{{ item.name }}</text>
              <view class="publish-badge" v-if="item.isPublished === 0">
                <text>未采纳</text>
              </view>
              <view class="publish-badge published" v-else-if="item.isPublished === 1">
                <text>✓ 已采纳</text>
              </view>
            </view>
            <text class="item-spec">x{{ item.quantity }}</text>
          </view>
          <text class="item-price">¥{{ item.price }}</text>
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="section-card">
      <view class="card-header">
        <view class="header-line"></view>
        <text class="card-title">备注</text>
        <view class="btn-edit" v-if="order.status === 0" @tap="showRemarkModal">
          <text>修改</text>
        </view>
      </view>
      <view class="remark-content">
        <text class="remark-text" v-if="order.remark">{{ order.remark }}</text>
        <text class="remark-text empty" v-else>暂无备注</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="section-card">
      <view class="card-header">
        <view class="header-line"></view>
        <text class="card-title">订单信息</text>
      </view>
      <view class="info-list">
        <view class="info-row" v-if="order.mealPeriod">
          <text class="label">餐次</text>
          <view class="meal-period-value">
            <text class="period-icon">{{ getMealPeriodIcon(order.mealPeriod) }}</text>
            <text class="period-text">{{ getMealPeriodName(order.mealPeriod) }}</text>
          </view>
        </view>
        <view class="info-row">
          <text class="label">订单号</text>
          <text class="value">{{ order.orderNumber }}</text>
        </view>
        <view class="info-row">
          <text class="label">下单时间</text>
          <text class="value">{{ order.createTime || '2024-11-24 16:00:00' }}</text>
        </view>
        <view class="info-row">
          <text class="label">支付方式</text>
          <text class="value">{{ getPayMethodText(order.payMethod) }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-row total">
          <text class="label">订单金额</text>
          <text class="value price">¥{{ order.totalAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar" v-if="order.status === 0 || order.status === 5">
      <view class="btn-cancel" @tap="cancelOrder">
        <text>取消订单</text>
      </view>
      <view class="btn-pay" v-if="order.status === 5" @tap="handlePay">
        <text>立即支付</text>
      </view>
    </view>

    <!-- 自定义备注编辑弹窗 -->
    <view class="remark-modal" v-if="showModal" @tap="closeModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">✏️ 修改备注</text>
          <view class="close-btn" @tap="closeModal">
            <text>✕</text>
          </view>
        </view>
        <view class="modal-body">
          <textarea 
            class="remark-input" 
            v-model="tempRemark" 
            placeholder="请输入备注内容,如:不要辣椒、多加香菜等..."
            placeholder-class="input-placeholder"
            maxlength="200"
            :auto-height="true"
            :show-confirm-bar="false"
          />
          <view class="char-count">
            <text>{{ tempRemark.length }}/200</text>
          </view>
        </view>
        <view class="modal-footer">
          <view class="btn btn-cancel-modal" @tap="closeModal">
            <text>取消</text>
          </view>
          <view class="btn btn-confirm" @tap="confirmRemark">
            <text>确定</text>
          </view>
        </view>
      </view>
    </view>
    <!-- 支付方式选择弹窗 -->
    <view class="pay-method-popup" :class="{ visible: showPayMethodSelect }">
      <view class="popup-mask" @tap="closePayMethodSelect"></view>
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">选择支付方式</text>
          <view class="popup-close" @tap="closePayMethodSelect">×</view>
        </view>
        
        <view class="method-list">
          <!-- 余额支付 -->
          <view 
            class="method-item" 
            :class="{ active: selectedPayMethod === 1, disabled: walletBalance < order.totalAmount }"
            @tap="selectPayMethod(1)"
          >
            <view class="method-left">
              <text class="method-icon">💰</text>
              <view class="method-info">
                <text class="method-name">余额支付</text>
                <text 
                  class="method-desc"
                  :class="{ 
                    'text-error': walletBalance < order.totalAmount,
                    'text-gray': walletBalance >= order.totalAmount
                  }"
                >
                  余额: ¥{{ walletBalance.toFixed(2) }}
                  {{ walletBalance < order.totalAmount ? '(余额不足)' : '' }}
                </text>
              </view>
            </view>
            <view class="radio-circle"></view>
          </view>
          
          <!-- 模拟支付 -->
          <!-- <view 
            class="method-item" 
            :class="{ active: selectedPayMethod === 2 }"
            @tap="selectPayMethod(2)"
          >
            <view class="method-left">
              <text class="method-icon">🎮</text>
              <view class="method-info">
                <text class="method-name">模拟支付</text>
                <text class="method-desc text-gray">开发测试使用</text>
              </view>
            </view>
            <view class="radio-circle"></view>
          </view> -->
        </view>
        
        <view class="popup-footer">
          <view class="btn-confirm-pay" @tap="confirmPayMethod">
            <text>确认支付 ¥{{ order.totalAmount }}</text>
          </view>
        </view>
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
import { ref, computed } from 'vue' // Added computed
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getOrderDetail, updateOrderStatus, updateOrderRemark, payOrder, getWalletInfo } from '@/api/index'
import { useTheme } from '@/stores/theme'
import { getDishImage } from '@/utils/image'
import PayPasswordPopup from '@/components/PayPasswordPopup.vue'

const order = ref({
  id: 0,
  orderNumber: '',
  status: 0,
  payStatus: 0,
  totalAmount: 0,
  remark: '',
  items: []
})

const showModal = ref(false)
const tempRemark = ref('')
const showPayPassword = ref(false)
const isPaying = ref(false)
const walletBalance = ref(0)
const hasPayPassword = ref(false)
const autoPay = ref(false)

// 支付方式选择相关
const showPayMethodSelect = ref(false)
const selectedPayMethod = ref(1) // 默认余额支付

const getStatusIcon = (status) => {
  const iconMap = {
    0: '⏳', // 待接单
    1: '👨‍🍳', // 准备中
    2: '🛵', // 配送中
    3: '🎉', // 已完成
    4: '❌', // 已取消
    5: '💳'  // 待支付
  }
  return iconMap[status] || '📋'
}

const getStatusText = (status) => {
  if (status === 5) {
    return '待支付'
  }
  const textMap = {
    0: '待接单',
    1: '准备中',
    2: '配送中',
    3: '已完成',
    4: '已取消',
    5: '待支付'
  }
  return textMap[status] || '未知状态'
}

const getStatusDesc = (status) => {
  const descMap = {
    0: '等待商家接单中',
    1: '商家正在准备餐品',
    2: '骑手正在配送中',
    3: '订单已完成，期待下次光临',
    4: '订单已取消',
    5: '请尽快完成支付'
  }
  return descMap[status] || ''
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'status-waiting',
    1: 'status-preparing',
    2: 'status-delivering',
    3: 'status-completed',
    4: 'status-cancelled',
    5: 'status-unpaid'
  }
  return classMap[status] || ''
}

const getPayMethodText = (method) => {
  const map = {
    1: '余额支付',
    2: '模拟支付'
  }
  return map[method] || (method ? '未知支付' : '微信支付')
}

// 获取餐次图标
const getMealPeriodIcon = (period) => {
  const map = {
    'BREAKFAST': '🍳',
    'LUNCH': '🍱',
    'DINNER': '🍷'
  }
  return map[period] || '🍽️'
}

// 获取餐次名称
const getMealPeriodName = (period) => {
  const map = {
    'BREAKFAST': '早餐',
    'LUNCH': '中餐',
    'DINNER': '晚餐'
  }
  return map[period] || ''
}

const { themeConfig, loadTheme } = useTheme()

const loadOrderDetail = async (id) => {
  try {
    const res = await getOrderDetail(id)
    if (res.data) {
      const data = res.data
      order.value = {
        id: data.id,
        orderNumber: data.orderNumber,
        status: data.status,
        totalAmount: data.totalAmount,
        consignee: data.consignee,
        phone: data.phone,
        address: data.address,
        createTime: data.createTime,
        payMethod: data.payMethod,
        payStatus: data.payStatus,
        remark: data.remark || '',
        mealPeriod: data.mealPeriod || '', // 餐次
        items: (data.orderItems || []).map(item => ({
          id: item.id,
          dishId: item.dishId,
          name: item.dishName,
          quantity: item.quantity,
          price: item.price,
          image: item.dishImage,
          dishStatus: item.dishStatus !== undefined && item.dishStatus !== null ? item.dishStatus : 0 // 如果后端没有返回状态，默认认为已下架（更安全）
        }))
      }
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    // 显示示例数据
    order.value = {
      id: id,
      orderNumber: '202311240001',
      status: 1,
      totalAmount: 86,
      consignee: '张三',
      phone: '138****8888',
      address: '广东省深圳市南山区科技园',
      createTime: '2024-11-24 16:00:00',
      payMethod: '微信支付',
      items: [
        {
          id: 1,
          dishId: 1,
          name: '宫保鸡丁',
          quantity: 1,
          price: 38,
          image: 'https://dummyimage.com/200x200/ff6b6b/ffffff&text=宫保鸡丁'
        },
        {
          id: 2,
          dishId: 2,
          name: '红烧肉',
          quantity: 1,
          price: 48,
          image: 'https://dummyimage.com/200x200/4ecdc4/ffffff&text=红烧肉'
        }
      ]
    }
  }
}

const cancelOrder = () => {
  if (!order.value.id) return
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateOrderStatus(order.value.id, 4)
          // 本地立即更新状态，确保当前页面即时回显
          order.value.status = 4
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          console.error('取消订单失败:', error)
        }
      }
    }
  })
}

const showRemarkModal = () => {
  tempRemark.value = order.value.remark || ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const confirmRemark = async () => {
  try {
    await updateOrderRemark(order.value.id, tempRemark.value)
    order.value.remark = tempRemark.value
    showModal.value = false
    uni.showToast({
      title: '备注修改成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('修改备注失败:', error)
    uni.showToast({
      title: '修改失败',
      icon: 'error'
    })
  }
}

const navigateToDishDetail = (dishId, dishStatus) => {
  if (!dishId) return
  
  // 检查菜品状态，只有明确是在售状态（status === 1）才允许查看详情
  // 如果状态是undefined、null、0或其他值，都阻止查看
  if (dishStatus !== 1) {
    uni.showToast({
      title: '该菜品已下架',
      icon: 'none',
      duration: 2000
    })
    return
  }
  
  uni.navigateTo({ 
    url: `/pages/detail/detail?id=${dishId}` 
  })
}

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

// 支付订单（打开选择弹窗）
const handlePay = async () => {
  if (isPaying.value) return
  showPayMethodSelect.value = true
}

// 关闭支付选择
const closePayMethodSelect = () => {
  showPayMethodSelect.value = false
}

// 选择支付方式
const selectPayMethod = (method) => {
  if (method === 1 && walletBalance.value < order.value.totalAmount) {
     // 余额不足也可以选，但在支付时拦截，或者这里就不让选？
     // 用户体验：还是让选，然后显示不足提示
  }
  selectedPayMethod.value = method
}

// 确认支付方式
const confirmPayMethod = () => {
  if (selectedPayMethod.value === 1) {
     if (walletBalance.value < order.value.totalAmount) {
        uni.showToast({
          title: '余额不足，请充值',
          icon: 'none'
        })
        return
     }
     
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
     
     // 关闭选择弹窗，打开密码弹窗
     showPayMethodSelect.value = false
     // 稍微延迟一下
     setTimeout(() => {
        showPayPassword.value = true
     }, 100)
     
  } else {
    // 模拟支付
    showPayMethodSelect.value = false
    processPay(null)
  }
}

const onPayPasswordConfirm = (password) => {
  showPayPassword.value = false
  processPay(password)
}

const processPay = async (password) => {
  if (isPaying.value) return
  isPaying.value = true
  
  try {
    const payData = {
      orderNo: order.value.orderNumber,
      payMethod: selectedPayMethod.value, // 使用选择的支付方式
      payPassword: password,
      remark: '订单支付'
    }
    
    await payOrder(payData)
    
    uni.showToast({
      title: '支付成功',
      icon: 'success'
    })
    
    // 刷新订单详情
    loadOrderDetail(order.value.id)
    
  } catch (error) {
    console.error('支付失败:', error)
    uni.showToast({
      title: error.message || '支付失败',
      icon: 'none'
    })
  } finally {
    isPaying.value = false
  }
}

onLoad((options) => {
  loadTheme()
  if (options.id) {
    loadOrderDetail(options.id)
  }
  if (options.autoPay == 1) {
    autoPay.value = true
  }
})

onShow(() => {
  loadTheme()
  loadWalletInfo()
  // 如果是自动支付且数据已加载，尝试支付
  if (autoPay.value && order.value.id) {
    autoPay.value = false // 防止重复触发
    // 稍微延迟确保数据加载
    setTimeout(() => {
        if (order.value.status === 5) {
            handlePay() // 这会打开选择弹窗，符合逻辑
        }
    }, 500)
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: v-bind('themeConfig.bgPrimary');
  padding: 20rpx;
  padding-bottom: 160rpx;
  transition: background-color 0.3s ease;
}

/* 状态卡片优化 */
.status-card {
  display: flex;
  align-items: center;
  padding: 40rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  color: #fff;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &.status-waiting {
    background: linear-gradient(135deg, #FFB347 0%, #FFCC33 100%); /* 柔和橙色 */
  }
  
  &.status-preparing {
    background: linear-gradient(135deg, #56CCF2 0%, #2F80ED 100%); /* 清新蓝色 */
  }
  
  &.status-delivering {
    background: linear-gradient(135deg, #B24592 0%, #F15F79 100%); /* 活力紫红 */
  }
  
  &.status-completed {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); /* 清爽绿色 */
  }
  
  &.status-cancelled {
    background: linear-gradient(135deg, #bdc3c7 0%, #2c3e50 100%); /* 沉稳灰色 */
    color: #fff;
    
    .status-text { color: #fff; }
    .status-desc { color: #eee; }
  }
  
  &.status-unpaid {
    background: linear-gradient(135deg, #FF9966 0%, #FF5E62 100%); /* 醒目红橙色 */
  }
}

.status-icon {
  font-size: 80rpx;
  margin-right: 30rpx;
}

.status-content {
  flex: 1;
}

.status-text {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.status-desc {
  display: block;
  font-size: 26rpx;
  opacity: 0.9;
}

/* 通用卡片样式 */
.section-card {
  padding: 0;
  margin-bottom: 24rpx;
  background: v-bind('themeConfig.cardBg');
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: v-bind('themeConfig.shadowLight');
  border: 1px solid v-bind('themeConfig.cardBorder');
  transition: all 0.3s ease;
}

.card-header {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  position: relative;
}

.header-line {
  width: 8rpx;
  height: 32rpx;
  background: v-bind('themeConfig.primaryColor');
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
  flex: 1;
}

.btn-edit {
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  background: v-bind('themeConfig.primaryColor');
  color: #fff;
  transition: all 0.3s;
  
  &:active {
    opacity: 0.8;
    transform: scale(0.95);
  }
}

/* 商品列表优化 */
.order-items {
  padding: 0 30rpx;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  transition: opacity 0.3s;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.not-published {
    opacity: 0.5;
    
    .item-image {
      filter: grayscale(100%);
    }
  }
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  background: #f5f5f5;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 120rpx;
  padding: 4rpx 0;
}

.item-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.item-name {
  font-size: 30rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 500;
  line-height: 1.4;
}

.publish-badge {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 600;
  background: #f5f5f5;
  color: #999;
  
  &.published {
    background: linear-gradient(135deg, var(--accent-orange), #ff9f43);
    color: #fff;
  }
}

.item-spec {
  font-size: 26rpx;
  color: v-bind('themeConfig.textSecondary');
}

.item-price {
  font-size: 32rpx;
  color: v-bind('themeConfig.textPrimary');
  font-weight: 700;
}

/* 信息列表优化 */
.info-list {
  padding: 30rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    font-size: 28rpx;
    color: v-bind('themeConfig.textSecondary');
  }
  
  .value {
    font-size: 28rpx;
    color: v-bind('themeConfig.textPrimary');
    font-weight: 500;
  }
}

.divider {
  height: 1px;
  background: v-bind('themeConfig.borderColor');
  margin: 24rpx 0;
}

.info-row.total {
  margin-top: 10rpx;
  
  .label {
    font-size: 30rpx;
    color: v-bind('themeConfig.textPrimary');
    font-weight: 600;
  }
  
  .value.price {
    font-size: 40rpx;
    color: v-bind('themeConfig.errorColor');
    font-weight: 800;
  }
}

/* 备注内容 */
.remark-content {
  padding: 30rpx;
}

.remark-text {
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  line-height: 1.6;
  word-break: break-all;
  
  &.empty {
    color: v-bind('themeConfig.textSecondary');
    opacity: 0.6;
  }
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: v-bind('themeConfig.bgSecondary');
  backdrop-filter: blur(20px);
  border-top: 1px solid v-bind('themeConfig.borderColor');
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  z-index: 100;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.btn-cancel {
  padding: 20rpx 48rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  background: v-bind('themeConfig.inputBg');
  border: 1px solid v-bind('themeConfig.borderColor');
  color: v-bind('themeConfig.textSecondary');
  transition: all 0.3s;
  
  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
}

.btn-pay {
  padding: 20rpx 60rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(90deg, #ff6b6b 0%, #ff4757 100%);
  box-shadow: 0 8rpx 20rpx rgba(255, 71, 87, 0.3);
  transition: all 0.3s;
  display: flex; /* Ensure centering if needed */
  align-items: center;
  justify-content: center;

  &:active {
    opacity: 0.9;
    transform: translateY(2rpx);
    box-shadow: 0 4rpx 10rpx rgba(255, 71, 87, 0.2);
  }
}

/* 自定义备注弹窗样式 */
.remark-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  width: 640rpx;
  background: v-bind('themeConfig.cardBg');
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 40rpx 30rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  background: linear-gradient(135deg, v-bind('themeConfig.primaryColor') 0%, v-bind('themeConfig.primaryColor') 100%);
}

.modal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}

.close-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 32rpx;
  transition: all 0.3s;
  
  &:active {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(0.9);
  }
}

.modal-body {
  padding: 40rpx;
}

.remark-input {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  background: v-bind('themeConfig.inputBg');
  border: 2px solid v-bind('themeConfig.borderColor');
  border-radius: 16rpx;
  font-size: 28rpx;
  color: v-bind('themeConfig.textPrimary');
  line-height: 1.6;
  transition: all 0.3s;
  box-sizing: border-box;
  
  &:focus {
    border-color: v-bind('themeConfig.primaryColor');
    background: v-bind('themeConfig.cardBg');
  }
}

.input-placeholder {
  color: v-bind('themeConfig.textSecondary');
  opacity: 0.5;
}

.char-count {
  margin-top: 16rpx;
  text-align: right;
  font-size: 24rpx;
  color: v-bind('themeConfig.textSecondary');
  opacity: 0.7;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 0 40rpx 40rpx;
}

.btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.96);
  }
}

.btn-cancel-modal {
  background: v-bind('themeConfig.inputBg');
  border: 2px solid v-bind('themeConfig.borderColor');
  color: v-bind('themeConfig.textSecondary');
  
  &:active {
    background: v-bind('themeConfig.borderColor');
  }
}

.btn-confirm {
  background: linear-gradient(135deg, v-bind('themeConfig.primaryColor') 0%, v-bind('themeConfig.primaryColor') 100%);
  color: #fff;
  box-shadow: 0 8rpx 20rpx rgba(255, 107, 107, 0.3);
  
  &:active {
    box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
  }
}
</style>
<style lang="scss" scoped>
/* 支付方式弹窗 */
.pay-method-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  visibility: hidden;
  transition: visibility 0.3s;

  &.visible {
    visibility: visible;
  }
}

.popup-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  opacity: 0;
  transition: opacity 0.3s;

  .visible & {
    opacity: 1;
  }
}

.popup-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: v-bind('themeConfig.bgSecondary');
  border-radius: 32rpx 32rpx 0 0;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding-bottom: env(safe-area-inset-bottom);

  .visible & {
    transform: translateY(0);
  }
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx;
  border-bottom: 1px solid v-bind('themeConfig.borderColor');
  position: relative;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 700;
  color: v-bind('themeConfig.textPrimary');
}

.popup-close {
  position: absolute;
  right: 30rpx;
  top: 30rpx;
  font-size: 40rpx;
  color: v-bind('themeConfig.textSecondary');
  line-height: 1;
  padding: 10rpx;
}

.method-list {
  padding: 30rpx;
}

.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  margin-bottom: 20rpx;
  background: v-bind('themeConfig.bgTertiary');
  border-radius: 20rpx;
  border: 2px solid transparent;
  transition: all 0.3s;

  &.active {
    border-color: #ff6b6b;
    background: rgba(255, 107, 107, 0.05);
  }

  &.disabled {
    opacity: 0.6;
    filter: grayscale(1);
  }
}

.method-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.method-icon {
  font-size: 48rpx;
}

.method-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.method-name {
  font-size: 30rpx;
  font-weight: 600;
  color: v-bind('themeConfig.textPrimary');
}

.method-desc {
  font-size: 24rpx;
  color: #34d399;

  &.text-error {
    color: #ff6b6b;
  }

  &.text-gray {
    color: v-bind('themeConfig.textSecondary');
  }
}

.radio-circle {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2px solid v-bind('themeConfig.borderColor');
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) scale(0);
    width: 20rpx;
    height: 20rpx;
    background: #ff6b6b;
    border-radius: 50%;
    transition: transform 0.2s;
  }

  .active & {
    border-color: #ff6b6b;
    &::after {
      transform: translate(-50%, -50%) scale(1);
    }
  }
}

.popup-footer {
  padding: 20rpx 40rpx 40rpx;
}

.btn-confirm-pay {
  height: 88rpx;
  border-radius: 44rpx;
  background: linear-gradient(90deg, #ff6b6b 0%, #ff4757 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 20rpx rgba(255, 71, 87, 0.3);

  &:active {
    transform: scale(0.98);
  }
}
</style>
