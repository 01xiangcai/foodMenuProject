const { request } = require('../../utils/request');

Page({
  data: {
    categories: [],
    activeCategoryId: null,
    dishes: []
  },

  onLoad() {
    this.loadCategories();
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
      const dishes = (res.data || []).map((dish) => ({
        id: dish.id,
        name: dish.name,
        price: dish.price,
        description: dish.description || '家人共创菜谱，敬请期待更多故事',
        badge: '家庭菜',
        image: dish.image || 'https://dummyimage.com/400x400/1e293b/ffffff&text=family',
        status: dish.status
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
  }
});

