/**
 * 图片处理工具函数
 */

/**
 * 默认菜品图片（Base64 SVG）
 * 一个简单的灰色背景带餐盘图标的占位图
 */
const DEFAULT_DISH_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgZmlsbD0iI2YzZjRmNiIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNDUlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iODAiIGZpbGw9IiM5Y2EzYWYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPvCfjaU8L3RleHQ+CiAgPHRleHQgeD0iNTAlIiB5PSI2NSUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxOCIgZmlsbD0iIzljYTNhZiIgdGV4dC1hbmNob3I9Im1pZGRsZSI+5pqC5peg5Zu+54mHPC90ZXh0Pgo8L3N2Zz4=';

/**
 * 获取菜品图片URL
 * 优先级：localImage > image > 默认图片
 * 
 * @param {Object} dish - 菜品对象
 * @param {boolean} useThumb - 是否使用缩略图
 * @returns {string} 图片URL
 */
export function getDishImage(dish, useThumb = false) {
  if (!dish) {
    return DEFAULT_DISH_IMAGE;
  }

  // 支持驼峰和下划线两种命名方式
  let imagePath = dish.localImage || dish.local_image || dish.image;

  // 都不存在则返回默认图片
  if (!imagePath || imagePath.trim() === '') {
    return DEFAULT_DISH_IMAGE;
  }

  // 如果需要缩略图，且不是 Base64，且路径中还不包含 _thumb
  // 且必须是本地路径或本站路径，外部域名路径不尝试转缩略图（因为外部服务如 dummyimage 不支持此后缀）
  const isRemote = imagePath.startsWith('http') && !imagePath.includes('/uploads/');
  
  if (useThumb && imagePath && !imagePath.startsWith('data:') && !imagePath.includes('_thumb.') && !isRemote) {
    const lastDotIndex = imagePath.lastIndexOf('.');
    // 确保点是在路径的最后一部分（文件名部分）
    const lastSlashIndex = imagePath.lastIndexOf('/');
    if (lastDotIndex !== -1 && lastDotIndex > lastSlashIndex) {
      imagePath = imagePath.substring(0, lastDotIndex) + '_thumb' + imagePath.substring(lastDotIndex);
    }
  }

  // 处理可能的中文路径问题（对非完整URL进行处理或对非Base64且包含中文的URL进行编码提示）
  // 注意：这里建议在后端 processImageUrl 处理好编码，前端仅做兜底
  // 确保返回完整URL
  return getImageUrl(imagePath);
}

/**
 * 获取菜品图片URL（别名，为了兼容性）
 * @param {Object} dish - 菜品对象
 * @returns {string} 图片URL
 */
export function getDishImageUrl(dish) {
  return getDishImage(dish);
}

/**
 * 检查图片URL是否有效
 * @param {string} url - 图片URL
 * @returns {boolean} 是否有效
 */
export function isValidImageUrl(url) {
  if (!url || typeof url !== 'string') {
    return false;
  }
  const trimmedUrl = url.trim();
  return trimmedUrl !== '' && trimmedUrl !== 'null' && trimmedUrl !== 'undefined';
}

/**
 * 获取完整图片URL
 * 如果是相对路径,则拼接baseURL
 * @param {string} path - 图片路径
 * @returns {string} 完整URL
 */
export function getImageUrl(path) {
  if (!path || path.trim() === '') {
    return '';
  }

  // 如果已经是完整URL,直接返回
  if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('data:')) {
    return path;
  }

  // 使用与request.js相同的BASE_URL配置
  const ENV_API_URL = import.meta.env.VITE_API_URL;

  let BASE_URL = '';
  // #ifdef H5
  BASE_URL = '/api';
  // #endif

  // #ifdef MP-WEIXIN
  BASE_URL = ENV_API_URL || '';
  // #endif

  // 如果BASE_URL为空,返回空字符串
  if (!BASE_URL) {
    console.warn('BASE_URL is not configured, image may not display correctly');
    return path;
  }

  // 拼接完整URL
  // 核心逻辑: 本项目的本地上传路径在数据库中是相对的(不带/uploads),需要补齐
  let cleanPath = path;
  if (!path.startsWith('/')) {
    // 如果是 food-menu/ banners/ avatars/ 等目录开头的相对路径，补齐 /uploads/
    if (path.startsWith('food-menu') || path.startsWith('banners') || path.startsWith('avatars')) {
      cleanPath = `/uploads/${path}`;
    } else {
      cleanPath = `/${path}`;
    }
  } else if (!path.startsWith('/uploads/')) {
     // 如果是以 / 开头但不是 /uploads/ 开头，补齐 /uploads/
     cleanPath = `/uploads${path}`;
  }
  
  return `${BASE_URL}${cleanPath}`;
}
