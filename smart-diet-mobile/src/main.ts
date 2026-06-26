import {createApp} from 'vue'
import {createPinia} from 'pinia'
import Vant from 'vant'
import App from './App.vue'
import router from './router'

// 引入 Vant 移动端组件库的所有基本样式
import 'vant/lib/index.css'
// 引入本地自定义全局样式
import './style.css'

// 启动移动端调试工具 VConsole (开发与预览状态下非常实用)
import VConsole from 'vconsole'

if (import.meta.env.DEV || window.location.href.includes('debug=true')) {
    new VConsole()
}

const app = createApp(App)
app.use(createPinia())
app.use(Vant)
app.use(router)

// 监听网络请求层抛出的“登录失效”事件
window.addEventListener('unauthorized', () => {
    router.push('/role-select')
})

app.mount('#app')

