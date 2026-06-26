import axios from 'axios'
import {showFailToast} from 'vant'
import 'vant/es/toast/style'

// 在本地开发环境下，我们已经通过 Vite 代理了 /api，所以 baseURL 设为 / 即可自动触发代理
// 在其他生产环境下，可配置为统一后端地址
const request = axios.create({
    baseURL: '/',
    timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res && typeof res.code === 'number') {
            if (res.code === 200) {
                const url = response.config.url || ''
                // 兼容管理系统的特殊前缀返回
                if (url.startsWith('/sys') || url.startsWith('/api/s3Storage') || url.startsWith('/api/auth')) {
                    return res
                }
                return res.data
            } else {
                if (res.code === 401) {
                    localStorage.removeItem('token')
                    localStorage.removeItem('role')
                    // 派发自定义事件，通知页面跳转到登录页
                    window.dispatchEvent(new CustomEvent('unauthorized'))
                }
                showFailToast(res.message || '操作失败')
                return Promise.reject(new Error(res.message || '操作失败'))
            }
        }
        return res
    },
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('role')
            window.dispatchEvent(new CustomEvent('unauthorized'))
            showFailToast('登录失效，请重新登录')
        } else {
            const msg = error.response?.data?.message || '网络请求错误，请稍后重试'
            showFailToast(msg)
        }
        return Promise.reject(error)
    }
)

export default request as any
