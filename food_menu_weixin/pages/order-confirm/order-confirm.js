const { request } = require('../../utils/request');

Page({
    data: {
        cartList: [],
        totalPrice: 0,
        remark: '',
        theme: 'tech',
        submitting: false
    },

    onLoad() {
        // 从本地存储获取购物车数据
        const cartData = wx.getStorageSync('cart_data') || {};
        const totalPrice = wx.getStorageSync('cart_total') || 0;

        // 转换为数组方便展示
        const cartList = Object.values(cartData);

        this.setData({
            cartList,
            totalPrice
        });
    },

    onShow() {
        this.setData({
            theme: getApp().globalData.theme
        });
    },

    onRemarkInput(e) {
        this.setData({
            remark: e.detail.value
        });
    },

    async handleSubmitOrder() {
        if (this.data.submitting) return;

        this.setData({ submitting: true });
        wx.showLoading({ title: '提交中...' });

        try {
            // 模拟提交订单
            // 实际项目中这里会调用后端API

            // 构建模拟订单数据
            const newOrder = {
                number: 'FM' + Date.now().toString().slice(-8),
                amount: '¥' + this.data.totalPrice,
                dish: this.data.cartList.map(item => item.name).join('+'),
                status: '准备食材', // 初始状态
                time: new Date().toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit' }),
                title: '订单已提交',
                glow: '#38bdf8'
            };

            // 保存到本地存储的模拟订单列表
            const mockOrders = wx.getStorageSync('mock_orders') || [];
            mockOrders.unshift(newOrder);
            wx.setStorageSync('mock_orders', mockOrders);

            // 清空购物车
            wx.removeStorageSync('cart_data');
            wx.removeStorageSync('cart_total');

            wx.showToast({
                title: '下单成功',
                icon: 'success'
            });

            setTimeout(() => {
                wx.switchTab({
                    url: '/pages/orders/orders'
                });
            }, 1500);

        } catch (error) {
            wx.showToast({
                title: '下单失败',
                icon: 'none'
            });
        } finally {
            this.setData({ submitting: false });
            wx.hideLoading();
        }
    }
});
