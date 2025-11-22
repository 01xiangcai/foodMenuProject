const { request } = require('../../utils/request');

Page({
    data: {
        username: '',
        password: '',
        confirmPassword: '',
        nickname: '',
        theme: 'tech',
        isRegister: false
    },

    onShow() {
        this.setData({
            theme: getApp().globalData.theme
        });
    },

    toggleMode() {
        this.setData({
            isRegister: !this.data.isRegister,
            username: '',
            password: '',
            confirmPassword: '',
            nickname: ''
        });
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
    },

    async handleRegister() {
        const { username, password, confirmPassword, nickname } = this.data;

        if (!username || !password) {
            wx.showToast({
                title: '请输入用户名和密码',
                icon: 'none'
            });
            return;
        }

        if (password !== confirmPassword) {
            wx.showToast({
                title: '两次密码不一致',
                icon: 'none'
            });
            return;
        }

        wx.showLoading({
            title: '注册中...',
        });

        try {
            await request({
                url: '/wx/user/register',
                method: 'POST',
                data: {
                    username,
                    password,
                    nickname,
                    phone: '' // Optional
                }
            });

            wx.showToast({
                title: '注册成功',
                icon: 'success'
            });

            // Auto login or switch to login mode
            setTimeout(() => {
                this.toggleMode();
                // Pre-fill username
                this.setData({ username });
            }, 1500);

        } catch (error) {
            wx.showToast({
                title: error.message || '注册失败',
                icon: 'none'
            });
        } finally {
            wx.hideLoading();
        }
    }
});
