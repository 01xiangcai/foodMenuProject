const { request } = require('../../utils/request');

Page({
    data: {
        theme: 'tech',
        orderId: null,
        order: null,
        loading: true,
        statusMap: {
            0: { text: '待接单', color: '#FF9F43', icon: '⏳' },
            1: { text: '制作中', color: '#14b8ff', icon: '👨‍🍳' },
            2: { text: '配送中', color: '#a855f7', icon: '🚚' },
            3: { text: '已完成', color: '#10b981', icon: '✅' },
            4: { text: '已取消', color: '#ef4444', icon: '❌' }
        }
    },

    onLoad(options) {
        const orderId = options.id;
        if (!orderId) {
            wx.showToast({
                title: '订单ID不存在',
                icon: 'none'
            });
            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
            return;
        }

        this.setData({
            theme: getApp().globalData.theme || 'tech',
            orderId: orderId
        });

        this.loadOrderDetail();
    },

    onShow() {
        this.setData({
            theme: getApp().globalData.theme || 'tech'
        });
    },

    /**
     * 加载订单详情
     */
    async loadOrderDetail() {
        this.setData({ loading: true });

        try {
            const response = await request({
                url: `/order/${this.data.orderId}`,
                method: 'GET'
            });

            if (response && response.data) {
                const order = response.data;
                const statusInfo = this.data.statusMap[order.status] || { text: '未知', color: '#666', icon: '❓' };

                this.setData({
                    order: {
                        ...order,
                        statusText: statusInfo.text,
                        statusColor: statusInfo.color,
                        statusIcon: statusInfo.icon,
                        createTimeText: this.formatTime(order.createTime),
                        canCancel: order.status === 0 || order.status === 1
                    },
                    loading: false
                });
            } else {
                throw new Error('订单不存在');
            }
        } catch (error) {
            console.error('Load order detail failed:', error);
            wx.showToast({
                title: error.message || '加载失败',
                icon: 'none'
            });
            this.setData({ loading: false });
            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
        }
    },

    /**
     * 取消订单
     */
    cancelOrder() {
        if (!this.data.order || !this.data.order.canCancel) {
            return;
        }

        wx.showModal({
            title: '确认取消',
            content: `确定要取消订单 ${this.data.order.orderNumber} 吗？`,
            confirmText: '确定',
            cancelText: '取消',
            success: async (res) => {
                if (res.confirm) {
                    await this.handleCancelOrder();
                }
            }
        });
    },

    /**
     * 处理取消订单
     */
    async handleCancelOrder() {
        wx.showLoading({ title: '处理中...' });

        try {
            await request({
                url: `/order/status?id=${this.data.orderId}&status=4`,
                method: 'PUT'
            });

            wx.hideLoading();
            wx.showToast({
                title: '订单已取消',
                icon: 'success'
            });

            // 重新加载订单详情
            setTimeout(() => {
                this.loadOrderDetail();
            }, 1000);
        } catch (error) {
            wx.hideLoading();
            console.error('Cancel order failed:', error);
            wx.showToast({
                title: error.message || '取消失败',
                icon: 'none'
            });
        }
    },

    /**
     * 格式化时间
     */
    formatTime(timeStr) {
        if (!timeStr) return '';
        
        const date = new Date(timeStr);
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const hour = date.getHours().toString().padStart(2, '0');
        const minute = date.getMinutes().toString().padStart(2, '0');
        
        return `${year}-${month}-${day} ${hour}:${minute}`;
    }
});

