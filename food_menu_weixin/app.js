App({
  globalData: {
    familyName: '周家·食光',
    themeColors: ['#14b8ff', '#a855f7'],
    // baseUrl: 'http://localhost:8080',
    baseUrl: 'http://a9284666.natappfree.cc',
    theme: 'family' // 默认主题: tech | family
  },

  // 切换主题
  toggleTheme() {
    const currentTheme = this.globalData.theme;
    const newTheme = currentTheme === 'tech' ? 'family' : 'tech';
    this.globalData.theme = newTheme;

    // 保存到本地存储
    wx.setStorageSync('theme', newTheme);

    // 应用主题样式
    this.applyTheme(newTheme);

    return newTheme;
  },

  onLaunch() {
    // 读取本地存储的主题
    const savedTheme = wx.getStorageSync('theme');
    if (savedTheme) {
      this.globalData.theme = savedTheme;
    }
    // 应用主题
    this.applyTheme(this.globalData.theme);
  },

  // 应用主题颜色到导航栏和TabBar
  applyTheme(theme) {
    if (theme === 'family') {
      // 温馨主题 (浅色)
      wx.setNavigationBarColor({
        frontColor: '#000000',
        backgroundColor: '#ffffff',
        animation: { duration: 300, timingFunc: 'easeIn' }
      });
      wx.setTabBarStyle({
        backgroundColor: '#ffffff',
        borderStyle: 'black',
        color: '#999999',
        selectedColor: '#FF9F43'
      });
    } else {
      // 科技主题 (深色)
      wx.setNavigationBarColor({
        frontColor: '#ffffff',
        backgroundColor: '#050a1f',
        animation: { duration: 300, timingFunc: 'easeIn' }
      });
      wx.setTabBarStyle({
        backgroundColor: '#0a1120',
        borderStyle: 'white',
        color: '#8b8fa3',
        selectedColor: '#14b8ff'
      });
    }
  }
});

