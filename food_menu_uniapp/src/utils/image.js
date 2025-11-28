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
 * @param {string} dish.localImage - 本地图片路径（驼峰命名）
 * @param {string} dish.local_image - 本地图片路径（下划线命名）
 * @param {string} dish.image - OSS图片路径
 * @returns {string} 图片URL
 */
export function getDishImage(dish) {
  if (!dish) {
    return DEFAULT_DISH_IMAGE;
  }

  // 支持驼峰和下划线两种命名方式
  const localImage = dish.localImage || dish.local_image;
  
  // 优先使用 localImage
  if (localImage && localImage.trim() !== '') {
    return localImage;
  }
  
  // 其次使用 image
  if (dish.image && dish.image.trim() !== '') {
    return dish.image;
  }
  
  // 都没有则返回默认图片
  return DEFAULT_DISH_IMAGE;
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

