import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {fileURLToPath, URL} from 'node:url'

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        host: '0.0.0.0', // 允许局域网内其他设备（如手机）访问
        port: 5178,
        strictPort: true, // 设为 true 时，若端口已被占用则直接退出，不会自动寻找下一个可用端口
        proxy: {
            '/api': {
                target: 'http://localhost:8080', // 指向本地 Spring Boot 后端
                changeOrigin: true,
                secure: false
            }
        }
    }
})
