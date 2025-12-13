<script>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useTheme } from '@/stores/theme'

export default {
  onLaunch() {
    console.log('App Launch')
    // 加载主题
    const { loadTheme } = useTheme()
    loadTheme()
  },
  onShow() {
    console.log('App Show')
    // 每次显示应用时重新应用当前主题，确保TabBar和导航栏样式正确
    const { applyCurrentTheme } = useTheme()
    applyCurrentTheme()
  },
  onHide() {
    console.log('App Hide')
  },
  // 全局转发配置
  onShareAppMessage(res) {
    return {
      title: '美食菜单 - 发现美味生活',
      path: '/pages/index/index',
      imageUrl: '' // 可选:使用自定义分享图片
    }
  }
}
</script>

<style lang="scss">
/* 导入全局样式 */
@import '@/styles/global.scss';

/* Theme CSS Variables */
:root, page {
  /* 默认浅色模式 (Light Mode) */
  --bg-page: #FDFBF7; /* 暖米色背景 */
  --bg-gradient-top: #fff7ea; /* 浅色模式顶部光晕 */
  --bg-card: #FFFFFF; /* 纯白卡片 */
  --bg-input: #F3F4F6;
  --text-primary: #333333;
  --text-secondary: #666666;
  --text-tertiary: #999999;
  --accent-orange: #FF7D58; /* 活力橙 */
  --accent-gradient: linear-gradient(135deg, #FF9F43 0%, #FF7D58 100%);
  --shadow-soft: 0 8px 20px rgba(0, 0, 0, 0.06);
  --border-color: rgba(0, 0, 0, 0.05);
  --card-radius: 16px;
}

/* 深色模式 (Dark Mode) - 通过类名控制 */
.theme-dark {
  --bg-page: #0F172A; /* 深邃蓝 */
  --bg-gradient-top: #1a1c29; /* 深色模式顶部光晕 */
  --bg-card: rgba(30, 41, 59, 0.7); /* 磨砂玻璃质感 */
  --bg-input: rgba(30, 41, 59, 0.5);
  --text-primary: #E2E8F0;
  --text-secondary: #94A3B8;
  --text-tertiary: #64748B;
  --shadow-soft: 0 8px 20px rgba(0, 0, 0, 0.3);
  --border-color: rgba(255, 255, 255, 0.1);
}

/* 全局页面背景 - 使用 CSS 变量 */
page {
  background-color: var(--bg-page);
  /* 全局增加微妙的顶部渐变，打破单调 */
  background-image: linear-gradient(to bottom, var(--bg-gradient-top) 0%, var(--bg-page) 35%);
  background-attachment: fixed;
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 底部导航栏圆角样式 - 更圆润 */
uni-tabbar,
uni-tabbar__bd,
.uni-tabbar,
.uni-tabbar__bd {
  border-radius: 48rpx 48rpx 0 0 !important;
  overflow: hidden !important;
}

/* H5 平台的 tabBar 圆角 */
/* #ifdef H5 */
.uni-tabbar,
.uni-tabbar__border,
uni-tabbar {
  border-radius: 48rpx 48rpx 0 0 !important;
  overflow: hidden !important;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1) !important;
}
/* #endif */

/* 小程序和 APP 平台的 tabBar 圆角 */
/* #ifndef H5 */
.uni-tabbar,
.uni-tabbar-border,
uni-tabbar {
  border-radius: 48rpx 48rpx 0 0 !important;
  overflow: hidden !important;
}
/* #endif */

/* 微信小程序特殊处理 */
/* #ifdef MP-WEIXIN */
page {
  padding-bottom: env(safe-area-inset-bottom);
}
/* #endif */
</style>
