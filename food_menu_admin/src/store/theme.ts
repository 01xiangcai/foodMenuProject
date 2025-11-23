import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import { useDark, useToggle } from '@vueuse/core';

export const useThemeStore = defineStore('theme', () => {
    // Dark mode handling using VueUse
    const isDark = useDark({
        selector: 'html',
        attribute: 'class',
        valueDark: 'dark',
        valueLight: 'light',
    });
    const toggleDark = useToggle(isDark);

    // Primary color state (HSL values for easier manipulation)
    const primaryColor = ref({ h: 198, s: 93, l: 60 }); // Default Sky Blue

    // Layout preferences
    const sidebarCollapsed = ref(false);
    const headerBlur = ref(true);

    // Apply theme variables to document root
    const applyTheme = () => {
        const root = document.documentElement;
        root.style.setProperty('--primary-h', primaryColor.value.h.toString());
        root.style.setProperty('--primary-s', `${primaryColor.value.s}%`);
        root.style.setProperty('--primary-l', `${primaryColor.value.l}%`);
    };

    // Watch for color changes
    watch(primaryColor, applyTheme, { deep: true, immediate: true });

    const setPrimaryColor = (h: number, s: number, l: number) => {
        primaryColor.value = { h, s, l };
    };

    const toggleSidebar = () => {
        sidebarCollapsed.value = !sidebarCollapsed.value;
    };

    return {
        isDark,
        toggleDark,
        primaryColor,
        sidebarCollapsed,
        headerBlur,
        setPrimaryColor,
        toggleSidebar,
    };
});
