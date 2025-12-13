import { ref, computed } from 'vue'

// 主题类型：'dark' (科技) 或 'light' (家庭)
const currentTheme = ref('dark')

// 从本地存储加载主题
const loadTheme = () => {
    const savedTheme = uni.getStorageSync('app_theme')
    if (savedTheme) {
        currentTheme.value = savedTheme
        // 延迟一点执行以确保页面加载完成
        setTimeout(() => {
            applyTheme(savedTheme)
        }, 100)
    }
}

// 保存主题到本地存储
const saveTheme = (theme) => {
    uni.setStorageSync('app_theme', theme)
}

// 应用主题到导航栏和TabBar
const applyTheme = (theme) => {
    const isDark = theme === 'dark'

    // 更新导航栏颜色
    try {
        uni.setNavigationBarColor({
            frontColor: isDark ? '#ffffff' : '#000000', // 浅色主题使用黑色文字/图标
            backgroundColor: isDark ? '#050a1f' : '#fff9f0', // 浅色主题使用暖色背景
            animation: {
                duration: 300,
                timingFunc: 'easeInOut'
            }
        })
    } catch (e) {
        console.log('设置导航栏颜色失败:', e)
    }

    // 更新TabBar样式
    try {
        uni.setTabBarStyle({
            color: isDark ? '#94A3B8' : '#999999',
            selectedColor: '#FF7D58',
            backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
            borderStyle: isDark ? 'black' : 'white'
        })
    } catch (e) {
        console.log('设置TabBar样式失败:', e)
    }

    // 更新窗口背景色 (修复底部或者下拉露底的颜色)
    try {
        const bgColor = isDark ? '#0F172A' : '#FDFBF7'
        uni.setBackgroundColor({
            backgroundColor: bgColor,
            backgroundColorTop: bgColor,
            backgroundColorBottom: bgColor
        })
    } catch (e) {
        console.log('设置窗口背景色失败:', e)
    }
}

// 切换主题
const toggleTheme = () => {
    currentTheme.value = currentTheme.value === 'dark' ? 'light' : 'dark'
    saveTheme(currentTheme.value)
    applyTheme(currentTheme.value)
}

// 主题配置
const themeConfig = computed(() => {
    if (currentTheme.value === 'dark') {
        // 深色模式 (Dark Mode)
        return {
            name: '深邃',
            bgPrimary: '#0F172A', // 深邃蓝
            bgSecondary: '#1E293B',
            bgTertiary: '#334155',

            cardBg: 'rgba(30, 41, 59, 0.7)', // 磨砂玻璃质感
            cardBorder: 'rgba(255, 255, 255, 0.1)',

            textPrimary: '#E2E8F0',
            textSecondary: '#94A3B8',
            textTertiary: '#64748B',

            primaryGradient: 'linear-gradient(135deg, #FF9F43 0%, #FF7D58 100%)',
            primaryColor: '#FF7D58', // 活力橙
            secondaryColor: '#FF9F43',

            shadowLight: '0 8px 20px rgba(0, 0, 0, 0.3)',
        }
    } else {
        // 浅色模式 (Light Mode)
        return {
            name: '温馨',
            bgPrimary: '#FDFBF7', // 暖米色背景
            bgSecondary: '#FFFFFF',
            bgTertiary: '#F3F4F6',

            cardBg: '#FFFFFF', // 纯白卡片
            cardBorder: 'rgba(0, 0, 0, 0.02)',

            textPrimary: '#333333',
            textSecondary: '#666666',
            textTertiary: '#999999',

            primaryGradient: 'linear-gradient(135deg, #FF9F43 0%, #FF7D58 100%)',
            primaryColor: '#FF7D58', // 活力橙
            secondaryColor: '#FF9F43',

            shadowLight: '0 8px 20px rgba(0, 0, 0, 0.06)',
        }
    }
})

// 应用当前主题
const applyCurrentTheme = () => {
    applyTheme(currentTheme.value)
}

// 导出主题相关功能
export const useTheme = () => {
    return {
        currentTheme,
        themeConfig,
        toggleTheme,
        loadTheme,
        applyCurrentTheme
    }
}
