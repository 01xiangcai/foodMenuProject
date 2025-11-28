import http from './http';

export type LoginPayload = {
  username: string;
  password: string;
  type?: number;
};

export type CategoryPayload = {
  id?: number;
  name: string;
  sort?: number;
  type?: number;
};

export type DishFlavorPayload = {
  id?: number;
  name: string;
  value: string;
};

export type DishPayload = {
  id?: number;
  name: string;
  categoryId: number;
  price: number;
  status: number;
  description?: string;
  flavors: DishFlavorPayload[];
  image?: string;
  code?: string;
  calories?: string;
  tags?: string;
};

export type DishQuery = {
  page: number;
  pageSize: number;
  name?: string;
  categoryId?: number | null;
};

export type OrdersQuery = {
  page: number;
  pageSize: number;
};

export type BannerPayload = {
  id?: number;
  image: string;
  title?: string;
  description?: string;
  linkUrl?: string;
  sort?: number;
  status: number;
};

export type BannerQuery = {
  page: number;
  pageSize: number;
};

export type UserQuery = {
  page: number;
  pageSize: number;
  username?: string;
  phone?: string;
  name?: string;
  type?: number;
  status?: number;
};

export type UserPayload = {
  id?: number;
  username: string;
  password?: string;
  phone?: string;
  name?: string;
  avatar?: string;
  type?: number;
  status?: number;
};

export type WxUserQuery = {
  page: number;
  pageSize: number;
  username?: string;
  phone?: string;
  nickname?: string;
  status?: number;
};

export type WxUserPayload = {
  id?: number;
  username: string;
  password?: string;
  phone?: string;
  nickname?: string;
  avatar?: string;
  gender?: number;
  status?: number;
  role?: number;
};

export const login = (data: LoginPayload) => http.post('/user/login', data);

export const fetchProfile = () => http.get('/user/info');

export const fetchCategories = () => http.get('/category/list', { params: { type: 1 } });
export const createCategory = (data: CategoryPayload) => http.post('/category', data);
export const updateCategory = (data: CategoryPayload) => http.put('/category', data);
export const removeCategory = (id: number) => http.delete('/category', { params: { id } });

export const fetchDishes = (params: DishQuery) => http.get('/dish/page', { params });
export const fetchDishDetail = (id: number) => http.get(`/dish/${id}`);
export const fetchTopDishes = () => http.get('/dish/top');
export const createDish = (data: DishPayload) => http.post('/dish', data);
export const updateDish = (data: DishPayload) => http.put('/dish', data);
export const removeDish = (id: number) => http.delete('/dish', { params: { id } });

export const fetchOrders = (params: OrdersQuery) => http.get('/order/page', { params });
export const fetchOrderDetail = (id: number) => http.get(`/order/${id}`);
export const updateOrderStatus = (id: number, status: number) =>
  http.put('/order/status', null, { params: { id, status } });
export const deleteOrder = (id: number) => http.delete(`/order/${id}`);

export const uploadImage = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return http.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const fetchDashboardStats = () =>
  Promise.all([
    http.get('/order/page', { params: { page: 1, pageSize: 50 } }),
    http.get('/dish/page', { params: { page: 1, pageSize: 50 } }),
    http.get('/category/list', { params: { type: 1 } })
  ]).then(([orders, dishes, categories]) => ({
    orders,
    dishes,
    categories
  }));

export const fetchBanners = (params: BannerQuery) => http.get('/banner/page', { params });
export const fetchBannerDetail = (id: number) => http.get(`/banner/${id}`);
export const createBanner = (data: BannerPayload) => http.post('/banner', data);
export const updateBanner = (data: BannerPayload) => http.put('/banner', data);
export const removeBanner = (id: number) => http.delete('/banner', { params: { id } });

export const fetchUsers = (params: UserQuery) => http.get('/user/page', { params });
export const createUser = (data: UserPayload) => http.post('/user', data);
export const updateUser = (data: UserPayload) => http.put('/user/admin/update', data);
export const deleteUser = (id: number) => http.delete(`/user/${id}`);
export const updateUserStatus = (id: number, status: number) =>
  http.put('/user/status', null, { params: { id, status } });
export const resetUserPassword = (id: number) => http.put(`/user/reset-password/${id}`);

export const fetchWxUsers = (params: WxUserQuery) => http.get('/wx/user/page', { params });
export const createWxUser = (data: WxUserPayload) => http.post('/wx/user/register', data);
export const updateWxUser = (data: WxUserPayload) => http.put('/wx/user/admin/update', data);
export const deleteWxUser = (id: number) => http.delete(`/wx/user/${id}`);
export const updateWxUserStatus = (id: number, status: number) =>
  http.put('/wx/user/status', null, { params: { id, status } });
export const resetWxUserPassword = (id: number) => http.put(`/wx/user/reset-password/${id}`);

export type DishTagQuery = {
  page: number;
  pageSize: number;
  name?: string;
};

export type DishTagPayload = {
  id?: number;
  name: string;
  icon: string;
  sort?: number;
};

export const fetchDishTags = (params: DishTagQuery) => http.get('/dish-tag/page', { params });
export const fetchAllDishTags = () => http.get('/dish-tag/list');
export const createDishTag = (data: DishTagPayload) => http.post('/dish-tag', data);
export const updateDishTag = (data: DishTagPayload) => http.put('/dish-tag', data);
export const removeDishTag = (id: number) => http.delete(`/dish-tag/${id}`);

// 系统配置
export const fetchSystemConfig = (key: string) => http.get(`/systemConfig/${key}`);
export const updateSystemConfig = (data: { configKey: string; configValue: string }) => http.put('/systemConfig', data);
export const fetchSystemConfigs = () => http.get('/systemConfig/list');

