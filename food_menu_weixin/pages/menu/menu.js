const { request } = require('../../utils/request');

Page({
  data: {
    categories: [],
    activeCategoryId: null,
    dishes: [],
    theme: 'tech',
    cartList: {},
    totalCount: 0,
    totalPrice: '0.00',
    favorites: {} // 收藏状态 {dishId: true/false}
  },

  onShow() {
    this.setData({
      theme: getApp().globalData.theme
    });
  },

  onLoad() {
    this.loadFavorites();
    this.loadCategories();
  },

  // 加载收藏列表
  loadFavorites() {
    try {
      const favorites = wx.getStorageSync('dish_favorites') || {};
      this.setData({ favorites });
      return favorites;
    } catch (error) {
      console.error('Load favorites failed:', error);
      return {};
    }
  },

  // 保存收藏列表
  saveFavorites(favorites) {
    try {
      wx.setStorageSync('dish_favorites', favorites);
      this.setData({ favorites });
    } catch (error) {
      console.error('Save favorites failed:', error);
    }
  },

  async loadCategories() {
    try {
      const res = await request({ url: '/category/list', data: { type: 1 } });
      const categories = (res.data || []).map((item, index) => ({
        id: item.id,
        name: item.name,
        icon: this.pickIcon(index)
      }));
      const firstId = categories.length ? categories[0].id : null;
      this.setData({ categories, activeCategoryId: firstId });
      if (firstId) {
        this.loadDishes(firstId);
      }
    } catch (error) {
      wx.showToast({ title: '加载分类失败', icon: 'none' });
    }
  },

  async loadDishes(categoryId) {
    try {
      const res = await request({ url: '/dish/list', data: { categoryId } });
      const favorites = this.data.favorites || {};
      const dishes = (res.data || []).map((dish) => ({
        id: dish.id,
        name: dish.name,
        price: dish.price,
        description: dish.description || '家人共创菜谱，敬请期待更多故事',
        badge: '家庭菜',
        image: dish.image || 'https://dummyimage.com/400x400/1e293b/ffffff&text=family',
        status: dish.status,
        isFavorite: favorites[dish.id] || false
      }));
      this.setData({ dishes });
    } catch (error) {
      wx.showToast({ title: '加载菜品失败', icon: 'none' });
    }
  },

  pickIcon(index) {
    const icons = ['👩‍🍳', '👨‍🔬', '🪐', '🍲', '🍱', '🥢'];
    return icons[index % icons.length];
  },

  handleParentTap(e) {
    const id = e.currentTarget.dataset.id;
    if (id === this.data.activeCategoryId) return;
    this.setData({ activeCategoryId: id });
    this.loadDishes(id);
  },

  // 添加到购物车
  addToCart(e) {
    const dish = e.currentTarget.dataset.item; // 需要在wxml中传递整个item
    const cart = this.data.cartList || {}; // 使用对象存储 {id: {count, price, name, image}}

    if (!cart[dish.id]) {
      cart[dish.id] = { ...dish, count: 1 };
    } else {
      cart[dish.id].count++;
    }

    this.calculateTotals(cart);
  },

  // 从购物车移除
  removeFromCart(e) {
    const dish = e.currentTarget.dataset.item;
    const cart = this.data.cartList || {};

    if (cart[dish.id]) {
      cart[dish.id].count--;
      if (cart[dish.id].count <= 0) {
        delete cart[dish.id];
      }
    }

    this.calculateTotals(cart);
  },

  // 计算总计
  calculateTotals(cart) {
    let totalCount = 0;
    let totalPrice = 0;

    Object.values(cart).forEach(item => {
      totalCount += item.count;
      totalPrice += item.count * item.price;
    });

    this.setData({
      cartList: cart,
      totalCount,
      totalPrice: totalPrice.toFixed(2)
    });
  },

  // 去结算
  goToCheckout() {
    if (this.data.totalCount === 0) return;

    // 将购物车数据存入本地存储，供确认页使用
    wx.setStorageSync('cart_data', this.data.cartList);
    wx.setStorageSync('cart_total', this.data.totalPrice);

    wx.navigateTo({
      url: '/pages/order-confirm/order-confirm'
    });
  },

  // 切换收藏状态
  toggleFavorite(e) {
    const dish = e.currentTarget.dataset.item;
    if (!dish || !dish.id) return;

    const favorites = { ...this.data.favorites };
    const isFavorite = !favorites[dish.id];

    // 更新收藏状态
    if (isFavorite) {
      favorites[dish.id] = true;
      wx.showToast({
        title: '已收藏',
        icon: 'success',
        duration: 1500
      });
    } else {
      delete favorites[dish.id];
      wx.showToast({
        title: '已取消收藏',
        icon: 'none',
        duration: 1500
      });
    }

    // 保存收藏状态
    this.saveFavorites(favorites);

    // 更新当前菜品列表中的收藏状态
    const dishes = this.data.dishes.map(item => {
      if (item.id === dish.id) {
        return { ...item, isFavorite };
      }
      return item;
    });
    this.setData({ dishes });
  }
});
