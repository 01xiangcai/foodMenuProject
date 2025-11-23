import axios from 'axios';

const http = axios.create({
  baseURL: process.env.NODE_ENV === 'production'
    ? '/api'  // 生产环境通过 Nginx 代理
    : '/api', // 开发环境通过 Vite 代理
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('fm_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const res = response.data;
    // If the custom code is not 1, it is judged as an error.
    if (res.code === 0) {
      return Promise.reject(new Error(res.msg || 'Error'));
    }
    return res;
  },
  (error) => {
    const message = error.response?.data?.message || error.message;
    return Promise.reject(new Error(message));
  }
);

export default http;
