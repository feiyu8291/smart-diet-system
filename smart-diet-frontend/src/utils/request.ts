import axios from 'axios'
import {ElMessage} from 'element-plus'
import {API_BASE_URL} from '../config'

const request = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000
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
                // 兼容迁移过来的系统管理模块，其请求前缀是 /sys、/api/s3Storage 或 /api/auth
                const url = response.config.url || ''
                if (url.startsWith('/sys') || url.startsWith('/api/s3Storage') || url.startsWith('/api/auth') || url.includes('/page')) {
                    return res
                }
                return res.data
            } else {
                // 如果是未认证状态，清除本地 token 并跳转登录页
                if (res.code === 401) {
                    localStorage.removeItem('token')
                    window.dispatchEvent(new CustomEvent('unauthorized'))
                }
                ElMessage.error(res.message || '操作失败')
                return Promise.reject(new Error(res.message || '操作失败'))
            }
        }
        return res
    },
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token')
            window.dispatchEvent(new CustomEvent('unauthorized'))
            ElMessage.error('登录失效，请重新登录')
        } else {
            const msg = error.response?.data?.message || '网络请求错误，请稍后重试'
            ElMessage.error(msg)
        }
        return Promise.reject(error)
    }
)

export default request as any

