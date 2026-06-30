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
            // 排除我们为心愿和忌口定制的真实的 api/diet 接口，使其直达后端
            '^/api/diet/(wish-dish|dislike-dish)': {
                target: 'http://localhost:8000',
                changeOrigin: true
            },
            // 体重记录接口重写映射
            '/api/diet/weight-record/list': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/weight-record\/list/, '/api/plan/weight/history')
            },
            '/api/diet/weight-record/add': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/weight-record\/add/, '/api/plan/weight')
            },
            // 菜谱与做法分支重写映射
            '/api/diet/dish': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/dish/, '/api/dish')
            },
            // 成员健康档案重写映射
            '/api/diet/user-health-profile': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/user-health-profile/, '/api/profile')
            },
            // 联合配餐计划重写映射
            '/api/diet/family-meal-plan': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/family-meal-plan/, '/api/meal')
            },
            // 采购清单重写映射
            '/api/diet/family-meal-grocery': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/family-meal-grocery/, '/api/meal')
            },
            // 就餐反馈打卡接口重写映射
            '/api/diet/meal-feedback/save': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/meal-feedback\/save/, '/api/meal/complete')
            },
            '/api/diet/meal-feedback': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/diet\/meal-feedback/, '/api/meal')
            },
            // 基础通用 api 直接路由到 8000 端口
            '/api': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                secure: false
            }
        }
    }
})
