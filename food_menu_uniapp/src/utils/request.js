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
                console.log('API Response:', res)
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    // 判断业务状态码
                    if (res.data && res.data.code === 1) {
                        resolve(res.data)
                    } else if (res.data && res.data.code === 0) {
                        // 业务错误
                        const errorMsg = res.data.msg || '请求失败'
                        console.error('业务错误:', errorMsg)
                        uni.showToast({
                            title: errorMsg,
                            icon: 'none'
                        })
                        reject(new Error(errorMsg))
                    } else {
                        // 没有 code 字段，直接返回数据
                        resolve(res.data)
                    }
                } else {
                    // HTTP 错误
                    const errorMsg = `请求失败(${res.statusCode})`
                    console.error('HTTP错误:', res)
                    uni.showToast({
                        title: errorMsg,
                        icon: 'none'
                    })
                    reject(new Error(errorMsg))
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
