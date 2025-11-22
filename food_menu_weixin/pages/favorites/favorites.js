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
  async loadFavorites() {
    const token = wx.getStorageSync('fm_token');
    if (!token) {
      // 未登录，从本地存储加载
      this.loadFavoritesFromLocal();
      return;
    }

    this.setData({ loading: true });
    
    try {
      // 调用后端API获取收藏列表
      const res = await request({ url: '/favorite/list' });
      
      if (res.code === 1) {
        const dishes = res.data || [];
        const favorites = dishes.map(dish => ({
          id: dish.id,
          name: dish.name,
          price: dish.price,
          description: dish.description || '家人共创菜谱，敬请期待更多故事',
          badge: dish.categoryName || '家庭菜',
          image: dish.image || 'https://dummyimage.com/400x400/1e293b/ffffff&text=family',
          status: dish.status,
          isFavorite: true
        }));

        // 同步到本地存储
        const favoritesMap = {};
        favorites.forEach(dish => {
          favoritesMap[dish.id] = true;
        });
        wx.setStorageSync('dish_favorites', favoritesMap);

        this.setData({ favorites, loading: false });
      } else {
        throw new Error(res.msg || '加载失败');
      }
    } catch (error) {
      console.error('Load favorites failed:', error);
      // 如果接口失败，尝试从本地存储加载
      this.loadFavoritesFromLocal();
      this.setData({ loading: false });
    }
  },

  // 从本地存储加载收藏（备用方案）
  loadFavoritesFromLocal() {
    try {
      const favoritesMap = wx.getStorageSync('dish_favorites') || {};
      const favoriteIds = Object.keys(favoritesMap).filter(id => favoritesMap[id]);

      if (favoriteIds.length === 0) {
        this.setData({ favorites: [] });
        return;
      }

      // 尝试从后端获取菜品详情
      this.loadFavoriteDishesFromServer(favoriteIds);
    } catch (error) {
      console.error('Load favorites from local failed:', error);
      this.setData({ favorites: [] });
    }
  },

  // 从服务器加载收藏菜品详情（本地存储有ID但缺少详情时）
  async loadFavoriteDishesFromServer(favoriteIds) {
    try {
      const res = await request({ url: '/dish/list' });
      const allDishes = res.data || [];
      
      const favorites = allDishes
        .filter(dish => favoriteIds.includes(String(dish.id)))
        .map(dish => ({
          id: dish.id,
          name: dish.name,
          price: dish.price,
          description: dish.description || '家人共创菜谱，敬请期待更多故事',
          badge: dish.categoryName || '家庭菜',
          image: dish.image || 'https://dummyimage.com/400x400/1e293b/ffffff&text=family',
          status: dish.status,
          isFavorite: true
        }));

      this.setData({ favorites });
    } catch (error) {
      console.error('Load favorite dishes from server failed:', error);
      this.setData({ favorites: [] });
    }
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
  async doRemoveFavorite(dishId) {
    const token = wx.getStorageSync('fm_token');
    
    try {
      // 如果有登录token，调用后端API
      if (token) {
        const res = await request({
          url: `/favorite/remove/${dishId}`,
          method: 'DELETE'
        });
        
        if (res.code !== 1) {
          wx.showToast({
            title: res.msg || '取消收藏失败',
            icon: 'none'
          });
          return;
        }
      }

      // 更新本地存储
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
      wx.showToast({ 
        title: '操作失败，请稍后重试', 
        icon: 'none' 
      });
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

