const { request } = require('../../utils/request');

Page({
    data: {
        theme: 'tech',
        orders: [], // 订单列表
        page: 1, // 当前页码
        pageSize: 10, // 每页数量
        hasMore: true, // 是否还有更多数据
        loading: false, // 是否正在加载
        refreshing: false, // 是否正在刷新
        statusMap: {
            0: { text: '待接单', color: '#FF9F43' },
            1: { text: '制作中', color: '#14b8ff' },
            2: { text: '配送中', color: '#a855f7' },
            3: { text: '已完成', color: '#10b981' },
            4: { text: '已取消', color: '#ef4444' }
        }
    },

    onLoad() {
        this.setData({
            theme: getApp().globalData.theme || 'tech'
        });
        this.loadOrders(true);
    },

    onShow() {
        // 每次显示页面时刷新数据（可能从订单详情页返回）
        this.setData({
            theme: getApp().globalData.theme || 'tech'
        });
        getApp().applyTheme(this.data.theme);
        this.refreshOrders();
    },

    /**
     * 加载订单列表
     * @param {Boolean} reset - 是否重置列表
     */
    async loadOrders(reset = false) {
        if (this.data.loading || (!this.data.hasMore && !reset)) {
            return;
        }

        this.setData({
            loading: true
        });

        try {
            const page = reset ? 1 : this.data.page;
            const response = await request({
                url: `/order/page?page=${page}&pageSize=${this.data.pageSize}`,
                method: 'GET'
            });

            if (response && response.data) {
                const pageInfo = response.data;
                const newOrders = pageInfo.records || [];

                // 格式化订单数据
                const formattedOrders = newOrders.map(order => {
                    return {
                        ...order,
                        statusText: this.data.statusMap[order.status]?.text || '未知',
                        statusColor: this.data.statusMap[order.status]?.color || '#666',
                        createTimeText: this.formatTime(order.createTime),
                        canCancel: order.status === 0 || order.status === 1 // 待接单和制作中可以取消
                    };
                });

                this.setData({
                    orders: reset ? formattedOrders : [...this.data.orders, ...formattedOrders],
                    page: pageInfo.current || page,
                    hasMore: pageInfo.current < pageInfo.pages,
                    loading: false
                });
            } else {
                this.setData({
                    loading: false,
                    hasMore: false
                });
            }
        } catch (error) {
            console.error('Load orders failed:', error);
            wx.showToast({
                title: error.message || '加载失败',
                icon: 'none'
            });
            this.setData({
                loading: false
            });
        }
    },

    /**
     * 下拉刷新
     */
    async onPullDownRefresh() {
        this.setData({
            refreshing: true
        });
        await this.loadOrders(true);
        this.setData({
            refreshing: false
        });
        wx.stopPullDownRefresh();
    },

    /**
     * 上拉加载更多
     */
    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.setData({
                page: this.data.page + 1
            });
            this.loadOrders(false);
        }
    },

    /**
     * 刷新订单列表
     */
    async refreshOrders() {
        this.setData({
            page: 1,
            hasMore: true
        });
        await this.loadOrders(true);
    },

    /**
     * 查看订单详情
     */
    async viewOrderDetail(e) {
        const orderId = e.currentTarget.dataset.id;
        if (!orderId) return;

        wx.navigateTo({
            url: `/pages/order-detail/order-detail?id=${orderId}`
        });
    },

    /**
     * 取消订单
     */
    cancelOrder(e) {
        const orderId = e.currentTarget.dataset.id;
        const orderNumber = e.currentTarget.dataset.number;

        if (!orderId) return;

        wx.showModal({
            title: '确认取消',
            content: `确定要取消订单 ${orderNumber} 吗？`,
            confirmText: '确定',
            cancelText: '取消',
            success: async (res) => {
                if (res.confirm) {
                    await this.handleCancelOrder(orderId);
                }
            }
        });
    },

    /**
     * 处理取消订单
     */
    async handleCancelOrder(orderId) {
        wx.showLoading({ title: '处理中...' });

        try {
            await request({
                url: `/order/status?id=${orderId}&status=4`,
                method: 'PUT'
            });

            wx.hideLoading();
            wx.showToast({
                title: '订单已取消',
                icon: 'success'
            });

            // 刷新订单列表
            this.refreshOrders();
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
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) {
            return '刚刚';
        } else if (minutes < 60) {
            return `${minutes}分钟前`;
        } else if (hours < 24) {
            return `${hours}小时前`;
        } else if (days < 7) {
            return `${days}天前`;
        } else {
            const month = date.getMonth() + 1;
            const day = date.getDate();
            const hour = date.getHours();
            const minute = date.getMinutes();
            return `${month}-${day} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
        }
    }
});

