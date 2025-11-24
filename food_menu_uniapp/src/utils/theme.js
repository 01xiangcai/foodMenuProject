// 主题混入工具 - 用于快速添加主题支持
// 使用方法：在页面的 <script setup> 中导入并使用

import { onMounted } from 'vue'
import { useTheme } from '@/stores/theme'

export const usePageTheme = () => {
    const { themeConfig, loadTheme } = useTheme()

    onMounted(() => {
        loadTheme()
    })

    return { themeConfig }
}
