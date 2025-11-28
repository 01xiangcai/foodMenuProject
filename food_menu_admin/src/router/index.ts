import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import DefaultLayout from '@/layouts/DefaultLayout.vue';
import LoginView from '@/views/auth/LoginView.vue';
import DashboardView from '@/views/dashboard/DashboardView.vue';
import DishesView from '@/views/dishes/DishesView.vue';
import OrdersView from '@/views/orders/OrdersView.vue';
import BannersView from '@/views/banners/BannersView.vue';
import UsersView from '@/views/users/UsersView.vue';
import MigrationView from '@/views/migration/MigrationView.vue';
import TagsView from '@/views/tags/TagsView.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      public: true,
      title: '家庭菜单登录'
    }
  },
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        path: '',
        name: 'dashboard',
        component: DashboardView,
        meta: { title: '家庭看板' }
      },
      {
        path: 'dishes',
        name: 'dishes',
        component: DishesView,
        meta: { title: '家庭菜单' }
      },
      {
        path: 'orders',
        name: 'orders',
        component: OrdersView,
        meta: { title: '订单记录' }
      },
      {
        path: 'banners',
        name: 'banners',
        component: BannersView,
        meta: { title: '轮播图管理' }
      },
      {
        path: 'users',
        name: 'users',
        component: UsersView,
        meta: { title: '用户管理' }
      },
      {
        path: 'migration',
        name: 'migration',
        component: MigrationView,
        meta: { title: '图片迁移' }
      },
      {
        path: 'tags',
        name: 'tags',
        component: TagsView,
        meta: { title: '标签管理' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '未来食堂控制台';

  const token = localStorage.getItem('fm_token');
  if (!to.meta.public && !token) {
    next('/login');
    return;
  }

  next();
});

export default router;

