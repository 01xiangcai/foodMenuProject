import { request } from '../utils/request'

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
        url: '/order',
        method: 'POST',
        data
    })
}

/**
 * 获取订单列表
 * @param {Object} params 查询参数
 */
export const getOrderList = (params) => {
    return request({
        url: '/order/page',
        method: 'GET',
        data: params
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
 * 用户登录
 * @param {Object} data 登录数据
 */
export const login = (data) => {
    return request({
        url: '/user/login',
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
        url: '/user/sendcode',
        method: 'POST',
        data: { phone }
    })
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
    return request({
        url: '/user/info',
        method: 'GET'
    })
}
