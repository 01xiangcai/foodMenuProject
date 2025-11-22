const { request } = require('../../utils/request');

Page({
  data: {
    user: null,
    showLogin: false,
    showEdit: false,
    loginLoading: false,
    updateLoading: false,
    loginForm: {
      username: '',
      password: ''
    },
    editForm: {
      name: '',
      avatar: ''
    }
  },

  onLoad() {
    this.loadUserInfo();
  },

  onShow() {
    // 每次显示页面时刷新用户信息
    this.loadUserInfo();
  },

  // 加载用户信息
  async loadUserInfo() {
    const token = wx.getStorageSync('fm_token');
    if (!token) {
      this.setData({ user: null });
      return;
    }

    try {
      const res = await request({ url: '/user/info' });
      this.setData({ user: res.data });
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 如果 token 无效，清除本地存储
      if (error.message && error.message.includes('Invalid token')) {
        wx.removeStorageSync('fm_token');
        this.setData({ user: null });
      }
    }
  },

  // 显示登录弹窗
  showLoginModal() {
    this.setData({
      showLogin: true,
      loginForm: { username: '', password: '' }
    });
  },

  // 隐藏登录弹窗
  hideLoginModal() {
    this.setData({ showLogin: false });
  },

  // 阻止事件冒泡
  stopPropagation() {
    // 空函数，用于阻止事件冒泡
  },

  // 用户名输入
  onUsernameInput(e) {
    this.setData({
      'loginForm.username': e.detail.value
    });
  },

  // 密码输入
  onPasswordInput(e) {
    this.setData({
      'loginForm.password': e.detail.value
    });
  },

  // 处理登录
  async handleLogin() {
    const { username, password } = this.data.loginForm;
    if (!username || !password) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none'
      });
      return;
    }

    this.setData({ loginLoading: true });
    try {
      const res = await request({
        url: '/user/login',
        method: 'POST',
        data: {
          username,
          password,
          type: 1
        }
      });
      
      // 保存 token
      wx.setStorageSync('fm_token', res.data);
      
      // 重新加载用户信息
      await this.loadUserInfo();
      
      this.setData({ showLogin: false });
      wx.showToast({
        title: '登录成功',
        icon: 'success'
      });
    } catch (error) {
      console.error('登录失败:', error);
      wx.showToast({
        title: error.message || '登录失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loginLoading: false });
    }
  },

  // 编辑资料
  editProfile() {
    const { user } = this.data;
    this.setData({
      showEdit: true,
      editForm: {
        name: user?.name || '',
        avatar: user?.avatar || ''
      }
    });
  },

  // 隐藏编辑弹窗
  hideEditModal() {
    this.setData({ showEdit: false });
  },

  // 昵称输入
  onNameInput(e) {
    this.setData({
      'editForm.name': e.detail.value
    });
  },

  // 头像URL输入
  onAvatarInput(e) {
    this.setData({
      'editForm.avatar': e.detail.value
    });
  },

  // 更新用户信息
  async handleUpdateProfile() {
    const { name, avatar } = this.data.editForm;
    if (!name) {
      wx.showToast({
        title: '请输入昵称',
        icon: 'none'
      });
      return;
    }

    this.setData({ updateLoading: true });
    try {
      await request({
        url: '/user',
        method: 'PUT',
        data: {
          name,
          avatar
        }
      });
      
      // 重新加载用户信息
      await this.loadUserInfo();
      
      this.setData({ showEdit: false });
      wx.showToast({
        title: '更新成功',
        icon: 'success'
      });
    } catch (error) {
      console.error('更新失败:', error);
      wx.showToast({
        title: error.message || '更新失败',
        icon: 'none'
      });
    } finally {
      this.setData({ updateLoading: false });
    }
  },

  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('fm_token');
          this.setData({ user: null });
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    });
  },

  // 导航到订单页面
  navigateToOrders() {
    wx.switchTab({
      url: '/pages/orders/orders'
    });
  },

  // 导航到收藏页面（暂未实现）
  navigateToFavorites() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 导航到地址页面（暂未实现）
  navigateToAddress() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 显示关于我们
  showAbout() {
    wx.showModal({
      title: '关于我们',
      content: '家宴点餐小程序\n\n为家庭提供便捷的点餐服务',
      showCancel: false
    });
  },

  // 显示设置
  showSettings() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  }
});

