import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd())

    console.log('Vite Config - Mode:', mode)
    console.log('Vite Config - VITE_PROXY_TARGET:', env.VITE_PROXY_TARGET)

    return {
        plugins: [uni()],
        server: {
            port: 5174,
            proxy: {
                '/api': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                    secure: false,
                    rewrite: (path) => path.replace(/^\/api/, '')
                },
                // WebSocket代理
                '/ws': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                    ws: true,  // 启用WebSocket代理
                    secure: false
                }
            }
        }
    }
})
