import { request } from '@/utils/request'

/**
 * 获取轮播图列表
 */
export const getBannerList = () => {
    return request({
        url: '/banner/list',
        method: 'GET'
    })
}

/**
 * 获取菜品分类列表
 */
export const getCategoryList = () => {
    return request({
        url: '/category/list',
        method: 'GET',
        data: { type: 1 }
    })
}

/**
 * 获取菜品列表
 * @param {Object} params 查询参数
 */
export const getDishList = (params) => {
    return request({
        url: '/dish/list',
        method: 'GET',
        data: params
    })
}

/**
 * 更新订单状态
 * @param {Number} id 订单ID
 * @param {Number} status 订单状态:0-待接单,1-准备中,2-配送中,3-已完成,4-已取消
 */
export const updateOrderStatus = (id, status) => {
    return request({
        url: `/order/status?id=${id}&status=${status}`,
        method: 'PUT'
    })
}

/**
 * 更新订单备注
 * @param {Number} id 订单ID
 * @param {String} remark 备注内容
 */
export const updateOrderRemark = (id, remark) => {
    return request({
        url: `/order/remark?id=${id}&remark=${encodeURIComponent(remark)}`,
        method: 'PUT'
    })
}

/**
 * 获取菜品详情
 * @param {Number} id 菜品ID
 */
export const getDishDetail = (id) => {
    return request({
        url: `/dish/${id}`,
        method: 'GET'
    })
}

/**
 * 创建订单
 * @param {Object} data 订单数据
 */
export const createOrder = (data) => {
    return request({
        url: '/order/submit',
        method: 'POST',
        data
    })
}

/**
 * 订单支付
 * @param {Object} data { orderNo, payMethod, payPassword, remark }
 */
export const payOrder = (data) => {
    return request({
        url: '/order/pay',
        method: 'POST',
        data
    })
}

/**
 * 获取菜品标签列表 (菜系/口味等)
 * @param {Number} type 标签类型 (可选)
 */
/**
 * 获取菜品标签列表 (菜系/口味等)
 * @param {Number} type 标签类型 (可选)
 */
export const getDishTagList = (type) => {
    const data = {}
    if (type !== undefined && type !== null) {
        data.type = type
    }
    return request({
        url: '/dish-tag/list',
        method: 'GET',
        data
    })
}

/**
 * 管理员获取全部订单列表
 * @param {Object} params 查询参数
 */
export const getAllOrders = (params) => {
    let url = '/order/admin'
    let query = []
    if (params) {
        if (params.page) query.push(`page=${params.page}`)
        if (params.pageSize) query.push(`pageSize=${params.pageSize}`)
        if (params.status !== undefined && params.status !== -1) query.push(`status=${params.status}`)
        if (params.familyId) query.push(`familyId=${params.familyId}`)
    }
    const queryString = query.join('&')
    return request({
        url: queryString ? `${url}?${queryString}` : url,
        method: 'GET'
    })
}

/**
 * 管理员获取订单统计
 * @param {Object} params { familyId }
 */
export const getAdminOrderCounts = (params) => {
    let url = '/order/admin/count'
    let query = []
    if (params) {
        if (params.familyId) query.push(`familyId=${params.familyId}`)
    }
    const queryString = query.join('&')
    return request({
        url: queryString ? `${url}?${queryString}` : url,
        method: 'GET'
    })
}

/**
 * 获取订单列表
 * @param {Object} params 查询参数
 */
export const getOrderList = (params) => {
    return request({
        url: '/order/my',  // 使用小程序专用接口
        method: 'GET',
        data: params
    })
}

/**
 * 获取订单统计
 */
export const getOrderCounts = () => {
    return request({
        url: '/order/count',
        method: 'GET'
    })
}

/**
 * 删除订单
 * @param {Number} id 订单ID
 */
export const deleteOrder = (id) => {
    return request({
        url: `/order/${id}`,
        method: 'DELETE'
    })
}

/**
 * 获取订单详情
 * @param {Number} id 订单ID
 */
export const getOrderDetail = (id) => {
    return request({
        url: `/order/${id}`,
        method: 'GET'
    })
}

/**
 * 添加收藏
 * @param {Number} dishId 菜品ID
 */
export const addFavorite = (dishId) => {
    return request({
        url: '/favorite/add',
        method: 'POST',
        data: { dishId }
    })
}

/**
 * 取消收藏
 * @param {Number} dishId 菜品ID
 */
export const removeFavorite = (dishId) => {
    return request({
        url: `/favorite/remove/${dishId}`,
        method: 'DELETE'
    })
}

/**
 * 批量检查收藏状态
 * @param {Array<Number>} dishIds 菜品ID集合
 */
export const checkFavoriteBatch = (dishIds = []) => {
    return request({
        url: '/favorite/check/batch',
        method: 'POST',
        data: { dishIds }
    })
}

/**
 * 获取收藏列表
 */
export const getFavoriteList = () => {
    return request({
        url: '/favorite/list',
        method: 'GET'
    })
}

/**
 * 分页获取收藏列表
 * @param {Number} page 页码，从1开始
 * @param {Number} pageSize 每页数量
 */
export const getFavoritePage = (page = 1, pageSize = 10) => {
    return request({
        url: '/favorite/page',
        method: 'GET',
        data: { page, pageSize }
    })
}

/**
 * 获取明星菜品
 */
export const getTopDishes = () => {
    return request({
        url: '/dish/top',
        method: 'GET'
    })
}

/**
 * 用户登录
 * @param {Object} data 登录数据
 */
export const wxLogin = (data) => {
    return request({
        url: '/wx/user/login',
        method: 'POST',
        data
    })
}

/**
 * 用户注册
 * @param {Object} data 注册数据 { username, password, nickname?, phone? }
 */
export const wxRegister = (data) => {
    return request({
        url: '/wx/user/register',
        method: 'POST',
        data
    })
}

/**
 * 发送验证码
 * @param {String} phone 手机号
 */
export const sendCode = (phone) => {
    return request({
        url: '/wx/user/sendcode',
        method: 'POST',
        data: { phone }
    })
}

/**
 * 获取用户信息
 */
export const getWxUserInfo = () => {
    return request({
        url: '/wx/user/info',
        method: 'GET'
    })
}

/**
 * 获取指定用户信息（公开信息）
 * @param {Number} id 用户ID
 */
export const getWxOtherUserInfo = (id) => {
    return request({
        url: `/wx/user/info/${id}`,
        method: 'GET'
    })
}

/**
 * 更新用户信息
 * @param {Object} data 用户信息
 */
export const updateWxUserInfo = (data) => {
    return request({
        url: '/wx/user',
        method: 'PUT',
        data
    })
}

/**
 * 上传文件
 * @param {String} filePath 文件路径
 */
export const uploadFile = (filePath) => {
    const token = uni.getStorageSync('fm_token')

    // 获取环境变量
    const ENV_API_URL = import.meta.env.VITE_API_URL

    // 根据平台设置 BASE_URL
    let BASE_URL = ''
    // #ifdef H5
    BASE_URL = '/api'
    // #endif

    // #ifdef MP-WEIXIN
    BASE_URL = ENV_API_URL
    // #endif

    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: `${BASE_URL}/wx/user/avatar`,
            filePath: filePath,
            name: 'file',
            header: {
                'Authorization': token ? `Bearer ${token}` : ''
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    const data = JSON.parse(res.data)
                    if (data.code === 1) {
                        resolve(data)
                    } else {
                        reject(new Error(data.msg || '上传失败'))
                    }
                } else {
                    reject(new Error(`上传失败(${res.statusCode})`))
                }
            },
            fail: (err) => {
                reject(err)
            }
        })
    })
}

/**
 * 获取标签图标映射
 */
export const getTagIconMap = () => {
    return request({
        url: '/dish-tag/icon-map',
        method: 'GET'
    })
}

/**
 * 获取标签列表
 * @param {Number} type 标签类型
 */
export const getTagList = (type) => {
    return request({
        url: '/dish-tag/list',
        method: 'GET',
        data: type ? { type } : {}
    })
}

/**
 * 通过邀请码加入家庭
 * @param {String} inviteCode 邀请码
 */
export const joinFamily = (inviteCode) => {
    return request({
        url: `/uniapp/family/join/${inviteCode}`,
        method: 'POST'
    })
}


/**
 * 获取当前用户的家庭信息
 */
export const getCurrentFamily = () => {
    return request({
        url: '/uniapp/family/current',
        method: 'GET'
    })
}

/**
 * 快速随机推荐菜品
 */
export const getRandomDish = () => {
    return request({
        url: '/uniapp/random-dish/quick',
        method: 'GET'
    })
}

/**
 * 条件筛选随机推荐
 * @param {Object} filter 筛选条件 { priceMin, priceMax, categoryIds, tags, excludeIds }
 */
export const getRandomDishWithFilter = (filter) => {
    return request({
        url: '/uniapp/random-dish/filter',
        method: 'POST',
        data: filter
    })
}

/**
 * 获取随机筛选选项
 */
export const getRandomFilterOptions = () => {
    return request({
        url: '/uniapp/random-dish/filter-options',
        method: 'GET'
    })
}

// ================== 钱包相关 API ==================

/**
 * 获取钱包信息
 */
export const getWalletInfo = () => {
    return request({
        url: '/app/wallet/info',
        method: 'GET'
    })
}

/**
 * 余额支付
 * @param {Object} data { amount, payPassword, orderNo, remark }
 */
export const walletPay = (data) => {
    return request({
        url: '/app/wallet/pay',
        method: 'POST',
        data
    })
}

/**
 * 获取交易流水
 * @param {Number} page 页码
 * @param {Number} pageSize 每页数量
 */
export const getWalletTransactions = (page = 1, pageSize = 10) => {
    return request({
        url: '/app/wallet/transactions',
        method: 'GET',
        data: { page, pageSize }
    })
}

/**
 * 设置支付密码
 * @param {String} payPassword 6位数字密码
 */
export const setPayPassword = (payPassword) => {
    return request({
        url: '/app/wallet/password',
        method: 'POST',
        data: { payPassword }
    })
}

/**
 * 验证支付密码
 * @param {String} payPassword 6位数字密码
 */
export const verifyPayPassword = (payPassword) => {
    return request({
        url: '/app/wallet/password/verify',
        method: 'POST',
        data: { payPassword }
    })
}

/**
 * 检查是否已设置支付密码
 */
export const checkPayPassword = () => {
    return request({
        url: '/app/wallet/password/check',
        method: 'GET'
    })
}

/**
 * 修改支付密码
 * @param {String} oldPassword 旧密码
 * @param {String} newPassword 新密码
 */
export const updatePayPassword = (oldPassword, newPassword) => {
    return request({
        url: '/app/wallet/password/update',
        method: 'POST',
        data: { oldPassword, newPassword }
    })
}

/**
 * 修改登录密码
 * @param {String} oldPassword 旧密码（未设置过密码的用户可不传）
 * @param {String} newPassword 新密码
 */
export const updateLoginPassword = (oldPassword, newPassword) => {
    return request({
        url: '/wx/user/password',
        method: 'PUT',
        data: { oldPassword, newPassword }
    })
}

/**
 * 获取系统配置
 * @param {String} key 配置键
 */
export const getSystemConfig = (key) => {
    return request({
        url: `/systemConfig/${key}`,
        method: 'GET'
    })
}

// ================== 小程序端菜品管理 API ==================

/**
 * 添加菜品(小程序管理员)
 * @param {Object} data 菜品数据
 */
export const addDishUniapp = (data) => {
    return request({
        url: '/uniapp/dish',
        method: 'POST',
        data
    })
}

/**
 * 更新菜品(小程序管理员)
 * @param {Object} data 菜品数据
 */
export const updateDishUniapp = (data) => {
    return request({
        url: '/uniapp/dish',
        method: 'PUT',
        data
    })
}

/**
 * 上传菜品图片
 * @param {String} filePath 文件路径
 */
export const uploadDishImage = (filePath) => {

    const token = uni.getStorageSync('fm_token')
    const ENV_API_URL = import.meta.env.VITE_API_URL

    let BASE_URL = ''
    // #ifdef H5
    BASE_URL = '/api'
    // #endif
    // #ifdef MP-WEIXIN
    BASE_URL = ENV_API_URL
    // #endif

    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: `${BASE_URL}/common/upload`,
            filePath: filePath,
            name: 'file',
            header: {
                'Authorization': token ? `Bearer ${token}` : ''
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    const data = JSON.parse(res.data)
                    if (data.code === 1) {
                        resolve(data)
                    } else {
                        reject(new Error(data.msg || '上传失败'))
                    }
                } else {
                    reject(new Error(`上传失败(${res.statusCode})`))
                }
            },
            fail: (err) => {
                reject(err)
            }
        })
    })
}
/**
 * 获取今日菜单列表(早中晚三个卡片数据)
 */
export const getTodayMeals = () => {
    return request({
        url: '/uniapp/daily-meal-order/today',
        method: 'GET'
    })
}
/**
 * 审核迟到订单(管理员)
 * @param {Number} orderId 订单ID
 * @param {Number} action 操作: 1-接受, 2-拒绝
 */
export const reviewLateOrder = (orderId, action) => {
    return request({
        url: `/admin/daily-meal-order/review-late-order/${orderId}?action=${action}`,
        method: 'POST'
    })
}
