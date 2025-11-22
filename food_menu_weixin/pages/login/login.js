const { request } = require('../../utils/request');

Page({
    data: {
        username: '',
        password: ''
    },

    async handleLogin() {
        const { username, password } = this.data;

        if (!username || !password) {
            wx.showToast({
                title: '请输入用户名和密码',
                icon: 'none'
            });
            return;
        }

        wx.showLoading({
            title: '登录中...',
        });

        try {
            const res = await request({
                url: '/wx/user/login',
                method: 'POST',
                data: {
                    type: 1,
                    username: username,
                    password: password
                }
            });

            // Login success
            const token = res.data;
            wx.setStorageSync('fm_token', token);

            wx.showToast({
                title: '登录成功',
                icon: 'success'
            });

            // Navigate back or to home
            setTimeout(() => {
                wx.switchTab({
                    url: '/pages/home/home'
                });
            }, 1500);
        } catch (error) {
            wx.showToast({
                title: error.message || '登录失败',
                icon: 'none'
            });
        } finally {
            wx.hideLoading();
        }
    }
});
