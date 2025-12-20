import { request } from '@/utils/request'

/**
 * 获取通知列表（分页）
 * @param {Number} pageNum 页码
 * @param {Number} pageSize 每页数量
 */
export const getNotificationList = (pageNum = 1, pageSize = 20) => {
    return request({
        url: `/uniapp/notification/list?pageNum=${pageNum}&pageSize=${pageSize}`,
        method: 'GET'
    })
}

/**
 * 获取未读通知数量
 */
export const getUnreadCount = () => {
    return request({
        url: '/uniapp/notification/unread-count',
        method: 'GET'
    })
}

/**
 * 标记单条通知为已读
 * @param {Number} id 通知ID
 */
export const markAsRead = (id) => {
    return request({
        url: `/uniapp/notification/${id}/read`,
        method: 'PUT'
    })
}

/**
 * 标记所有通知为已读
 */
export const markAllAsRead = () => {
    return request({
        url: '/uniapp/notification/read-all',
        method: 'PUT'
    })
}
