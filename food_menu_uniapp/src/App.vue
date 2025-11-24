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
</style>
