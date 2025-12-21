// 获取环境变量
const ENV_API_URL = import.meta.env.VITE_API_URL

let BASE_URL = ''

// #ifdef H5
BASE_URL = '/api'
// #endif

// #ifdef MP-WEIXIN
BASE_URL = ENV_API_URL
// #endif

/**
 * 封装 uni.request
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
export const request = (options = {}) => {
    const { url, method = 'GET', data = {}, header = {} } = options

    // 获取 token
    const token = uni.getStorageSync('fm_token')

    // 获取微信用户ID
    const wxUserId = uni.getStorageSync('wx_user_id')

    // 合并请求头
    const finalHeader = {
        'content-type': 'application/json',
        ...header
    }

    if (token) {
        finalHeader.Authorization = `Bearer ${token}`
    }

    // 添加微信用户ID到请求头
    if (wxUserId) {
        finalHeader['wx-user-id'] = wxUserId
    }

    return new Promise((resolve, reject) => {
        uni.request({
            url: `${BASE_URL}${url}`,
            method,
            data,
            header: finalHeader,
            success: (res) => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    // 判断业务状态码 - 支持code=1和code=200两种成功响应
                    if (res.data && (res.data.code === 1 || res.data.code === 200)) {
                        resolve(res.data)
                    } else if (res.data && res.data.code === 0) {
                        // 业务错误 - 不在这里显示toast,让调用方自己处理
                        reject(new Error(res.data.msg || '请求失败'))
                    } else {
                        // 其他情况也resolve,让调用方处理
                        resolve(res.data)
                    }
                } else {
                    // HTTP 错误
                    uni.showToast({
                        title: `请求失败(${res.statusCode})`,
                        icon: 'none'
                    })
                    reject(new Error(res.errMsg || `请求失败(${res.statusCode})`))
                }
            },
            fail: (err) => {
                console.error('Request failed:', err)
                uni.showToast({
                    title: '网络请求失败',
                    icon: 'none'
                })
                reject(new Error(err.errMsg || '网络请求失败'))
            }
        })
    })
}

export { BASE_URL }
