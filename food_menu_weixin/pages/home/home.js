const { request } = require('../../utils/request');

Page({
  data: {
    now: '',
    banners: [],
    quickActions: [
      { label: '一键叫饭', desc: '告诉家人开饭啦', link: '/pages/orders/orders' },
      { label: '今日菜单', desc: '挑选 3 道拿手菜', link: '/pages/menu/menu' },
      { label: '口味心愿', desc: '记录家人偏好', link: '/pages/menu/menu' }
    ],
    featuredDishes: [
      { title: '妈妈的招牌番茄牛腩', tag: '暖胃', energy: '482 kcal' },
      { title: '爸爸的柠檬烤鱼', tag: '低油', energy: '328 kcal' },
      { title: '可可的芝士焗南瓜', tag: '甜蜜', energy: '266 kcal' }
    ],
    theme: 'tech'
  },

  onShow() {
    this.setData({
      theme: getApp().globalData.theme
    });
  },

  onLoad() {
    this.updateClock();
    this.timer = setInterval(() => this.updateClock(), 1000);
    this.loadBanners();
  },

  onUnload() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },

  updateClock() {
    const now = new Date();
    const formatted = now.toLocaleTimeString('zh-CN', {
      hour12: false
    });
    this.setData({ now: formatted });
  },

  loadBanners() {
    request({ url: '/banner/list' })
      .then((res) => {
        console.log('轮播图接口返回:', JSON.stringify(res, null, 2));
        const banners = (res.data || []).map((item) => {
          // 直接使用后端返回的URL，OSS签名URL已经是正确格式，不要重新编码
          const imageUrl = item.image || 'https://dummyimage.com/800x400/6366f1/ffffff&text=Banner';
          console.log(`轮播图${item.id} - 标题: ${item.title}, 图片URL: ${imageUrl.substring(0, 100)}...`);
          return {
            id: item.id,
            image: imageUrl,
            title: item.title,
            description: item.description,
            linkUrl: item.linkUrl
          };
        });
        // 如果后端返回为空，使用默认轮播图
        if (banners.length === 0) {
          banners.push({
            id: 1,
            image: 'https://dummyimage.com/800x400/6366f1/ffffff&text=家庭美食',
            title: '家庭美食精选',
            description: '每日新鲜食材，用心烹饪每一道菜'
          });
        }
        console.log('设置轮播图数据，数量:', banners.length);
        this.setData({ banners }, () => {
          console.log('轮播图数据已设置，当前banners:', this.data.banners);
        });
      })
      .catch((error) => {
        console.error('加载轮播图失败:', error);
        // 使用默认轮播图
        this.setData({
          banners: [
            {
              id: 1,
              image: 'https://dummyimage.com/800x400/6366f1/ffffff&text=家庭美食',
              title: '家庭美食精选',
              description: '每日新鲜食材，用心烹饪每一道菜'
            }
          ]
        });
      });
  },

  onBannerImageLoad(e) {
    console.log('轮播图加载成功:', e);
    const index = e.currentTarget.dataset.index || 0;
    console.log(`第${index + 1}张轮播图加载成功`);
  },

  onBannerImageError(e) {
    console.error('轮播图加载失败:', e);
    console.error('错误详情:', e.detail);
    const index = e.currentTarget.dataset.index || 0;
    const banners = this.data.banners;
    if (banners && banners[index]) {
      console.log('当前图片URL:', banners[index].image);
      // 尝试使用默认图片
      banners[index].image = 'https://dummyimage.com/800x400/6366f1/ffffff&text=图片加载失败';
      this.setData({ banners });
    }
  }
});
