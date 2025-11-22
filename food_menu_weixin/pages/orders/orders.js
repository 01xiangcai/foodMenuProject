Page({
  data: {
    timeline: [
      { time: '18:05', title: '妈妈提交了备餐清单', status: '准备食材', glow: '#38bdf8' },
      { time: '18:15', title: '爸爸确认烤箱预热', status: '协同中', glow: '#f472b6' },
      { time: '18:32', title: '可可发起甜品投票', status: '待确认', glow: '#fcd34d' }
    ],
    history: [
      { number: 'FM20241101', amount: '¥128', dish: '番茄牛腩+炒时蔬', status: '已完成' },
      { number: 'FM20241031', amount: '¥96', dish: '柠檬烤鱼', status: '已完成' }
    ],
    theme: 'tech'
  },

  onShow() {
    this.setData({
      theme: getApp().globalData.theme
    });
    this.loadOrders();
  },

  loadOrders() {
    // 读取模拟订单
    const mockOrders = wx.getStorageSync('mock_orders') || [];

    if (mockOrders.length > 0) {
      // 将最新的订单作为"进行中"展示在时间线
      const latestOrder = mockOrders[0];

      // 更新时间线数据
      const timeline = [
        { time: latestOrder.time, title: '订单已提交', status: '待接单', glow: '#38bdf8' },
        { time: '预计 10:00', title: '预计送达', status: '准备中', glow: '#f472b6' }
      ];

      // 更新历史订单列表
      // 这里简单地把所有模拟订单都加到历史列表里演示
      const history = [
        ...mockOrders,
        { number: 'FM20241101', amount: '¥128', dish: '番茄牛腩+炒时蔬', status: '已完成' },
        { number: 'FM20241031', amount: '¥96', dish: '柠檬烤鱼', status: '已完成' }
      ];

      this.setData({
        timeline,
        history
      });
    }
  }
});

