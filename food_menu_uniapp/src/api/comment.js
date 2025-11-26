import { request } from '@/utils/request'

/**
 * 获取菜品评论列表
 * @param {Number} dishId 菜品ID
 */
export const getCommentList = (dishId) => {
    return request({
        url: `/dish/comments/${dishId}`,
        method: 'GET'
    })
}

/**
 * 发表评论或回复
 * @param {Object} data 评论数据
 * @param {Number} data.dishId 菜品ID
 * @param {Number} data.parentId 父评论ID（回复时需要）
 * @param {String} data.content 评论内容
 */
export const addComment = (data) => {
    return request({
        url: '/dish/comments',
        method: 'POST',
        data
    })
}
