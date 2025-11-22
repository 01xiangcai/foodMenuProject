import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import path from 'path';
import UnoCSS from 'unocss/vite';
import Pages from 'vite-plugin-pages';
import tsconfigPaths from 'vite-tsconfig-paths';

export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    tsconfigPaths(),
    Pages({
      dirs: 'src/views',
      extensions: ['vue'],
      routeBlockLang: 'yaml'
    }),
    UnoCSS()
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/mixins.scss" as *;`
      }
    }
  }
});

