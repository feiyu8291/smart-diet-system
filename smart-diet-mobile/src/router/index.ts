import type {RouteRecordRaw} from 'vue-router'
import {createRouter, createWebHashHistory} from 'vue-router'
import {useRoleStore} from '../store/role'

const routes: RouteRecordRaw[] = [
    {
        path: '/role-select',
        name: 'RoleSelect',
        component: () => import('../views/role-select/index.vue'),
        meta: {title: '选择角色登录'}
    },
    {
        path: '/',
        component: () => import('../views/layout/MobileLayout.vue'),
        children: [
            // 厨师（Chef）路由
            {
                path: 'chef/home',
                name: 'ChefHome',
                component: () => import('../views/chef/ChefHome.vue'),
                meta: {title: '每日烹饪指南', role: 'chef'}
            },
            {
                path: 'chef/recipe-generator',
                name: 'ChefRecipeGenerator',
                component: () => import('../views/chef/ChefRecipeGenerator.vue'),
                meta: {title: '智能配餐生成', role: 'chef'}
            },
            {
                path: 'chef/grocery',
                name: 'ChefGrocery',
                component: () => import('../views/chef/ChefGrocery.vue'),
                meta: {title: '菜市场买菜清单', role: 'chef'}
            },
            // 用餐人（Diner）路由
            {
                path: 'diner/home',
                name: 'DinerHome',
                component: () => import('../views/diner/DinerHome.vue'),
                meta: {title: '今日分餐膳食', role: 'diner'}
            },
            {
                path: 'diner/wish',
                name: 'DinerWish',
                component: () => import('../views/diner/DinerWish.vue'),
                meta: {title: '想吃与避坑偏好', role: 'diner'}
            },
            {
                path: 'diner/health',
                name: 'DinerHealth',
                component: () => import('../views/diner/DinerHealth.vue'),
                meta: {title: '健康看板与体重打卡', role: 'diner'}
            },
            // 菜谱广场路由 (全平台通用)
            {
                path: 'square',
                name: 'RecipeSquare',
                component: () => import('../views/square/RecipeSquare.vue'),
                meta: {title: '菜谱做法广场'}
            }
        ]
    },
    // 兜底重定向
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

// 路由守卫：拦截未登录/未选定角色
router.beforeEach((to) => {
    const roleStore = useRoleStore()

    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title as string
    }

    // 如果是去角色登录选择页，直接通过
    if (to.path === '/role-select') {
        return true
    }

    // 未登录或未选角色，强行跳转到角色选择页面
    if (!roleStore.token || !roleStore.role) {
        return '/role-select'
    }

    // 如果访问根路径 "/"，根据角色跳转到对应的首页面
    if (to.path === '/') {
        return roleStore.role === 'chef' ? '/chef/home' : '/diner/home'
    }

    // 角色权限防护：如果去往跟角色不匹配的路由，重定向到其匹配的首页面
    if (to.meta.role && to.meta.role !== roleStore.role) {
        return roleStore.role === 'chef' ? '/chef/home' : '/diner/home'
    }

    return true
})

export default router
