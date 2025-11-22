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
            // 构建订单数据
            const orderItems = this.data.cartList.map(item => ({
                dishId: item.id,
                dishName: item.name,
                dishImage: item.image,
                price: item.price,
                quantity: item.count
            }));

            const orderData = {
                remark: this.data.remark,
                orderItems: orderItems
            };

            console.log('Submitting order data:', orderData);

            // 调用后端API提交订单
            const response = await request({
                url: '/order/submit',
                method: 'POST',
                data: orderData
            });

            console.log('Submit response:', response);

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
            console.error('Submit order failed:', error);
            wx.showToast({
                title: error.message || '下单失败',
                icon: 'none'
            });
        } finally {
            this.setData({ submitting: false });
            wx.hideLoading();
        }
    }
});
