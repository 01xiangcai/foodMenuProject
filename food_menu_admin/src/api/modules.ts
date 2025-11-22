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

export const login = (data: LoginPayload) => http.post('/user/login', data);

export const fetchProfile = () => http.get('/user/info');

export const fetchCategories = () => http.get('/category/list', { params: { type: 1 } });
export const createCategory = (data: CategoryPayload) => http.post('/category', data);
export const updateCategory = (data: CategoryPayload) => http.put('/category', data);
export const removeCategory = (id: number) => http.delete('/category', { params: { id } });

export const fetchDishes = (params: DishQuery) => http.get('/dish/page', { params });
export const fetchDishDetail = (id: number) => http.get(`/dish/${id}`);
export const createDish = (data: DishPayload) => http.post('/dish', data);
export const updateDish = (data: DishPayload) => http.put('/dish', data);
export const removeDish = (id: number) => http.delete('/dish', { params: { id } });

export const fetchOrders = (params: OrdersQuery) => http.get('/order/page', { params });
export const fetchOrderDetail = (id: number) => http.get(`/order/${id}`);
export const updateOrderStatus = (id: number, status: number) =>
  http.put('/order/status', null, { params: { id, status } });

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

