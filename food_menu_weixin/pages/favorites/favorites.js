const { request } = require('../../utils/request');

Page({
  data: {
    favorites: [],
    theme: 'tech',
    loading: false
  },

  onShow() {
    this.setData({
      theme: getApp().globalData.theme
    });
    this.loadFavorites();
  },

  onLoad() {
    this.loadFavorites();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadFavorites();
    setTimeout(() => {
      wx.stopPullDownRefresh();
    }, 500);
  },

  // 加载收藏列表
  loadFavorites() {
    try {
      // 从本地存储获取收藏的菜品ID列表
      const favoritesMap = wx.getStorageSync('dish_favorites') || {};
      const favoriteIds = Object.keys(favoritesMap).filter(id => favoritesMap[id]);

      if (favoriteIds.length === 0) {
        this.setData({ favorites: [] });
        return;
      }

      // 从本地存储获取所有菜品信息（如果之前有缓存）
      // 或者从菜单页面的数据中获取
      // 这里先使用模拟数据，后续可以优化
      this.loadFavoriteDishes(favoriteIds);
    } catch (error) {
      console.error('Load favorites failed:', error);
      wx.showToast({ title: '加载收藏失败', icon: 'none' });
    }
  },

  // 加载收藏的菜品详情
  async loadFavoriteDishes(favoriteIds) {
    this.setData({ loading: true });
    
    try {
      // 获取所有菜品列表，然后过滤出收藏的
      const res = await request({ url: '/dish/list' });
      const allDishes = res.data || [];
      
      // 过滤出收藏的菜品
      const favorites = allDishes
        .filter(dish => favoriteIds.includes(String(dish.id)))
        .map(dish => ({
          id: dish.id,
          name: dish.name,
          price: dish.price,
          description: dish.description || '家人共创菜谱，敬请期待更多故事',
          badge: '家庭菜',
          image: dish.image || 'https://dummyimage.com/400x400/1e293b/ffffff&text=family',
          status: dish.status,
          isFavorite: true
        }))
        .sort((a, b) => {
          // 按收藏时间排序（这里简化为按ID排序，后续可以优化）
          return b.id - a.id;
        });

      this.setData({ favorites, loading: false });
    } catch (error) {
      console.error('Load favorite dishes failed:', error);
      // 如果接口失败，尝试从本地存储获取基本信息
      this.loadFavoritesFromLocal(favoriteIds);
      this.setData({ loading: false });
    }
  },

  // 从本地存储加载收藏（备用方案）
  loadFavoritesFromLocal(favoriteIds) {
    // 尝试从菜单页面的缓存中获取
    // 这里简化处理，只显示ID
    const favorites = favoriteIds.map(id => ({
      id: parseInt(id),
      name: '菜品 ' + id,
      price: 0,
      description: '收藏的菜品',
      badge: '家庭菜',
      image: 'https://dummyimage.com/400x400/1e293b/ffffff&text=family',
      status: 1,
      isFavorite: true
    }));
    this.setData({ favorites });
  },

  // 取消收藏
  removeFavorite(e) {
    const dish = e.currentTarget.dataset.item;
    if (!dish || !dish.id) return;

    wx.showModal({
      title: '提示',
      content: '确定要取消收藏吗？',
      success: (res) => {
        if (res.confirm) {
          this.doRemoveFavorite(dish.id);
        }
      }
    });
  },

  // 执行取消收藏
  doRemoveFavorite(dishId) {
    try {
      const favoritesMap = wx.getStorageSync('dish_favorites') || {};
      delete favoritesMap[dishId];
      wx.setStorageSync('dish_favorites', favoritesMap);

      // 更新列表
      const favorites = this.data.favorites.filter(item => item.id !== dishId);
      this.setData({ favorites });

      wx.showToast({
        title: '已取消收藏',
        icon: 'success',
        duration: 1500
      });
    } catch (error) {
      console.error('Remove favorite failed:', error);
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  // 跳转到菜品详情
  goToDetail(e) {
    const dishId = e.currentTarget.dataset.id;
    if (dishId) {
      wx.navigateTo({
        url: `/pages/detail/detail?id=${dishId}`
      });
    }
  },

  // 跳转到菜单页面
  goToMenu() {
    wx.switchTab({
      url: '/pages/menu/menu'
    });
  }
});

