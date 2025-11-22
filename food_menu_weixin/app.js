App({
  globalData: {
    familyName: '周家·食光',
    themeColors: ['#14b8ff', '#a855f7'],
    baseUrl: 'http://localhost:8080',
    theme: 'family' // 默认主题: tech | family
  },

  // 切换主题
  toggleTheme() {
    const currentTheme = this.globalData.theme;
    const newTheme = currentTheme === 'tech' ? 'family' : 'tech';
    this.globalData.theme = newTheme;

    // 保存到本地存储
    wx.setStorageSync('theme', newTheme);

    return newTheme;
  },

  onLaunch() {
    // 读取本地存储的主题
    const savedTheme = wx.getStorageSync('theme');
    if (savedTheme) {
      this.globalData.theme = savedTheme;
    }
  }
});

