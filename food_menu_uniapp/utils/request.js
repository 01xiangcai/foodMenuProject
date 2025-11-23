// API 基础地址配置
// #ifdef H5
const BASE_URL = '/api'
// #endif

// #ifdef MP-WEIXIN
const BASE_URL = 'http://139.196.146.244/api'
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

    // 合并请求头
    const finalHeader = {
        'content-type': 'application/json',
        ...header
    }

    if (token) {
        finalHeader.Authorization = `Bearer ${token}`
    }

    return new Promise((resolve, reject) => {
        uni.request({
            url: `${BASE_URL}${url}`,
            method,
            data,
            header: finalHeader,
            success: (res) => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    // 判断业务状态码
                    if (res.data && res.data.code === 1) {
                        resolve(res.data)
                    } else if (res.data && res.data.code === 0) {
                        // 业务错误
                        uni.showToast({
                            title: res.data.msg || '请求失败',
                            icon: 'none'
                        })
                        reject(new Error(res.data.msg || '请求失败'))
                    } else {
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
