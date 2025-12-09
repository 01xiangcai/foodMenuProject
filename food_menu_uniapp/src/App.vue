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

/* 全局页面背景 - 使用主题色 */
page {
  background-color: #050a1f;
  transition: background-color 0.3s ease;
}

/* 深色主题（科技） */
page.theme-dark {
  background-color: #050a1f;
}

/* 浅色主题（家庭） */
page.theme-light {
  background-color: #f8f9fa;
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
