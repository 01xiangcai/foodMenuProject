const { request } = require('../../utils/request');

Page({
  data: {
    dish: null,
    comments: [],
    theme: 'tech'
  },

  onShow() {
    this.setData({
      theme: getApp().globalData.theme
    });
  },

  onLoad(options) {
    const { id } = options || {};
    if (id) {
      this.dishId = id;
      this.loadDish(id);
      this.loadComments(id);
    }
  },

  async loadDish(id) {
    try {
      const res = await request({ url: `/dish/${id}` });
      const dish = res.data;
      this.setData({ dish });
      wx.setNavigationBarTitle({ title: dish.name || '菜品详情' });
    } catch (error) {
      wx.showToast({ title: '获取菜品失败', icon: 'none' });
    }
  },

  async loadComments(id) {
    try {
      const res = await request({ url: `/dish/comments/${id}` });
      this.setData({ comments: res.data || [] });
    } catch (error) {
      this.setData({ comments: [] });
    }
  },

  toggleReplies(e) {
    const index = e.currentTarget.dataset.index;
    const key = `comments[${index}].expanded`;
    const current = this.data.comments?.[index]?.expanded;
    this.setData({ [key]: !current });
  }
});

