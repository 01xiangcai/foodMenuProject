// 微信小程序生产环境配置示例
// 使用方法：将此文件重命名为 request.js 并替换 utils/request.js

// ⚠️ 重要：生产环境必须使用 HTTPS 域名
const BASE_URL = 'https://your-domain.com/api';

const request = (options = {}) => {
    const { url, method = 'GET', data = {}, header = {} } = options;
    const token = wx.getStorageSync('fm_token');
    const finalHeader = {
        'content-type': 'application/json',
        ...header
    };
    if (token) {
        finalHeader.Authorization = `Bearer ${token}`;
    }

    return new Promise((resolve, reject) => {
        wx.request({
            url: `${BASE_URL}${url}`,
            method,
            data,
            header: finalHeader,
            success: (res) => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    if (res.data && res.data.code === 1) {
                        resolve(res.data);
                    } else if (res.data && res.data.code === 0) {
                        reject(new Error(res.data.msg || '请求失败'));
                    } else {
                        resolve(res.data);
                    }
                } else {
                    reject(new Error(res.errMsg || `请求失败(${res.statusCode})`));
                }
            },
            fail: (err) => {
                console.error('Request failed:', err);
                reject(new Error(err.errMsg || '网络请求失败，请检查网络连接'));
            }
        });
    });
};

module.exports = {
    request,
    BASE_URL
};
