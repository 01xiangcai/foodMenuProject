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
  familyId?: number; // 家庭ID（超级管理员可以设置）
};

export type DishQuery = {
  page: number;
  pageSize: number;
  name?: string;
  categoryId?: number | null;
  familyId?: number | null; // 家庭ID（超级管理员可以筛选特定家庭）
};

export type OrdersQuery = {
  page: number;
  pageSize: number;
  familyId?: number | null; // 家庭ID（超级管理员可以筛选特定家庭）
};

export type BannerPayload = {
  id?: number;
  image: string;
  title?: string;
  description?: string;
  linkUrl?: string;
  sort?: number;
  status: number;
  familyId?: number; // 家庭ID（超级管理员可以设置）
};

export type BannerQuery = {
  page: number;
  pageSize: number;
  familyId?: number | null; // 家庭ID（超级管理员可以筛选特定家庭）
};

export type UserQuery = {
  page: number;
  pageSize: number;
  username?: string;
  phone?: string;
  name?: string;
  type?: number;
  status?: number;
  familyId?: number | null; // 家庭ID（超级管理员可以筛选特定家庭）
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
  familyId?: number; // 家庭ID（超级管理员可以设置）
  role?: number; // 角色: 0-普通管理员, 1-家庭管理员, 2-超级管理员
};

export type WxUserQuery = {
  page: number;
  pageSize: number;
  username?: string;
  phone?: string;
  nickname?: string;
  status?: number;
  familyId?: number | null; // 家庭ID（超级管理员可以筛选特定家庭）
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

export const fetchCategories = (params?: any) => http.get('/category/list', { params: { type: 1, ...params } });
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

// 家庭管理
export type FamilyQuery = {
  page: number;
  pageSize: number;
  name?: string;
  status?: number;
};

export type FamilyPayload = {
  id?: number;
  name: string;
  description?: string;
  status: number;
};

export const fetchFamilies = (params: FamilyQuery) => http.get('/admin/family/page', { params });
export const fetchAllFamilies = () => http.get('/admin/family/list'); // 获取所有家庭列表（不分页）
export const fetchFamilyDetail = (id: number) => http.get(`/admin/family/${id}`);
export const fetchFamilyByInviteCode = (code: string) => http.get(`/admin/family/invite/${code}`);
export const createFamily = (data: FamilyPayload) => http.post('/admin/family', data);
export const updateFamily = (data: FamilyPayload) => http.put('/admin/family', data);
export const deleteFamily = (id: number) => http.delete(`/admin/family/${id}`);

// 操作日志
export type OperationLogQuery = {
  page: number;
  pageSize: number;
  operationType?: string;
  operationModule?: string;
  operatorId?: number;
  operatorName?: string;
  operatorType?: string;
  status?: number;
  familyId?: number | null; // 家庭ID（仅超级管理员可用，用于筛选特定家庭）
  startTime?: string;
  endTime?: string;
  ipAddress?: string;
};

export type OperationLog = {
  id: number;
  operationType: string;
  operationModule: string;
  operationDesc: string;
  methodName: string;
  requestMethod: string;
  requestUrl: string;
  requestParams?: string;
  responseResult?: string;
  operatorId: number;
  operatorName: string;
  operatorType: string;
  ipAddress: string;
  userAgent?: string;
  executionTime: number;
  status: number;
  errorMsg?: string;
  familyId?: number;
  createTime: string;
};

export const fetchOperationLogs = (params: OperationLogQuery) => http.get('/admin/operationLog/page', { params });
export const fetchOperationLogDetail = (id: number) => http.get(`/admin/operationLog/${id}`);
export const deleteOperationLog = (id: number) => http.delete(`/admin/operationLog/${id}`);
export const batchDeleteOperationLogs = (endTime: string) => http.delete('/admin/operationLog/batch', { params: { endTime } });

// 钱包管理
export type WalletQuery = {
  page: number;
  pageSize: number;
  wxUserId?: string;
  nickname?: string;
};

export type WalletInfo = {
  id: number;
  wxUserId: string;
  balance: number;
  frozenAmount: number;
  version: number;
  nickname?: string;
  phone?: string;
  createTime: string;
  updateTime: string;
};

export type RechargePayload = {
  wxUserId: string;
  amount: number;
  remark?: string;
};

export type WalletTransaction = {
  id: number;
  wxUserId: string;
  transType: number;
  amount: number;
  balanceAfter: number;
  relatedOrderNo?: string;
  remark?: string;
  createTime: string;
};

export const fetchWallets = (params: WalletQuery) => http.get('/admin/wallet/list', { params });
export const rechargeWallet = (data: RechargePayload) => http.post('/admin/wallet/recharge', data);
export const fetchWalletDetail = (wxUserId: string) => http.get(`/admin/wallet/detail/${wxUserId}`);
export const fetchWalletTransactions = (params: { wxUserId: string; page: number; pageSize: number }) =>
  http.get('/admin/wallet/transactions', { params });
export const resetWalletPassword = (wxUserId: string) =>
  http.post('/admin/wallet/password/reset', null, { params: { wxUserId } });


