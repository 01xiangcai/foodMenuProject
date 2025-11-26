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
            color: isDark ? '#8b8fa3' : '#9ca3af',
            selectedColor: isDark ? '#14b8ff' : '#ff6b6b',
            backgroundColor: isDark ? '#0a1120' : '#ffffff',
            borderStyle: isDark ? 'black' : 'white'
        })
    } catch (e) {
        console.log('设置TabBar样式失败:', e)
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
        // 科技主题 - 深色炫酷
        return {
            name: '科技',
            // 背景色
            bgPrimary: '#050a1f',
            bgSecondary: '#0a1128',
            bgTertiary: '#0f1729',

            // 卡片背景（毛玻璃效果）
            cardBg: 'rgba(15, 23, 41, 0.6)',
            cardBorder: 'rgba(255, 255, 255, 0.1)',

            // 文字颜色
            textPrimary: '#ffffff',
            textSecondary: '#8b8fa3',
            textTertiary: '#5a5e73',

            // 主题色（渐变）
            primaryGradient: 'linear-gradient(135deg, #14b8ff 0%, #a855f7 100%)',
            primaryColor: '#14b8ff',
            secondaryColor: '#a855f7',

            // 强调色
            accentColor: '#14b8ff',
            successColor: '#10b981',
            warningColor: '#f59e0b',
            errorColor: '#ef4444',

            // 阴影
            shadowLight: '0 4px 20px rgba(20, 184, 255, 0.1)',
            shadowMedium: '0 8px 30px rgba(20, 184, 255, 0.15)',
            shadowHeavy: '0 12px 40px rgba(20, 184, 255, 0.2)',

            // 边框
            borderColor: 'rgba(255, 255, 255, 0.1)',

            // 输入框
            inputBg: 'rgba(255, 255, 255, 0.05)',
            inputBorder: 'rgba(255, 255, 255, 0.1)',

            // 状态颜色 (柔和渐变)
            statusGradients: {
                waiting: 'linear-gradient(135deg, rgba(255, 149, 0, 0.15) 0%, rgba(255, 149, 0, 0.05) 100%)',
                preparing: 'linear-gradient(135deg, rgba(20, 184, 255, 0.15) 0%, rgba(20, 184, 255, 0.05) 100%)',
                delivering: 'linear-gradient(135deg, rgba(168, 85, 247, 0.15) 0%, rgba(168, 85, 247, 0.05) 100%)',
                completed: 'linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(16, 185, 129, 0.05) 100%)',
                cancelled: 'linear-gradient(135deg, rgba(139, 143, 163, 0.15) 0%, rgba(139, 143, 163, 0.05) 100%)'
            },
            statusTextColors: {
                waiting: '#ff9500',
                preparing: '#14b8ff',
                delivering: '#a855f7',
                completed: '#10b981',
                cancelled: '#8b8fa3'
            }
        }
    } else {
        // 家庭主题 - 暖色温馨（重新设计）
        return {
            name: '温馨',
            // 背景色 - 使用暖米色/奶油色代替冷白
            bgPrimary: '#fff9f0', // 暖米色背景
            bgSecondary: '#ffffff',
            bgTertiary: '#fff5e6',

            // 卡片背景 - 纯白卡片，增加不透明度以提升对比
            cardBg: '#ffffff',
            cardBorder: 'rgba(0, 0, 0, 0.02)', // 极淡的黑色边框，更自然

            // 文字颜色 - 使用深暖灰/褐色
            textPrimary: '#2d3436', // 深灰
            textSecondary: '#636e72', // 中灰
            textTertiary: '#b2bec3',

            // 主题色（暖橙渐变）
            primaryGradient: 'linear-gradient(135deg, #ff9a9e 0%, #fad0c4 99%, #fad0c4 100%)', // 桃粉色渐变
            primaryColor: '#ff6b6b', // 珊瑚红
            secondaryColor: '#ff9f43', // 暖橙

            // 强调色
            accentColor: '#ff6b6b',
            successColor: '#00b894',
            warningColor: '#fdcb6e',
            errorColor: '#d63031',

            // 阴影 - 使用更自然的暖灰色阴影，而非红色阴影
            shadowLight: '0 4px 16px rgba(12, 12, 12, 0.03)',
            shadowMedium: '0 8px 24px rgba(12, 12, 12, 0.06)',
            shadowHeavy: '0 16px 48px rgba(12, 12, 12, 0.1)',

            // 边框
            borderColor: 'rgba(0, 0, 0, 0.05)',

            // 输入框
            inputBg: '#ffffff',
            inputBorder: 'rgba(0, 0, 0, 0.1)',

            // 图标
            iconColor: '#636e72',

            // 状态颜色 (柔和莫兰迪色系)
            statusGradients: {
                waiting: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)', // 柔和杏色
                preparing: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)', // 柔和天蓝
                delivering: 'linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)', // 柔和淡紫
                completed: 'linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%)', // 柔和薄荷
                cancelled: 'linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%)'  // 柔和灰
            },
            statusTextColors: {
                waiting: '#f57c00',
                preparing: '#1976d2',
                delivering: '#7b1fa2',
                completed: '#388e3c',
                cancelled: '#757575'
            }
        }
    }
})

// 导出主题相关功能
export const useTheme = () => {
    return {
        currentTheme,
        themeConfig,
        toggleTheme,
        loadTheme
    }
}
