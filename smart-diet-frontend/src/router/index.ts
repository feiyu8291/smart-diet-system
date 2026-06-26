import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/profile/personal'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: {title: '登录 - 智能膳食系统'}
    },
    {
        path: '/profile/personal',
        name: 'ProfileList',
        component: () => import('../views/ProfileList.vue'),
        meta: {title: '健康档案管理 - 智能膳食系统'}
    },
    {
        path: '/profile/personal-detail',
        name: 'PersonalProfile',
        component: () => import('../views/PersonalProfile.vue'),
        meta: {title: '成员健康评估详情 - 智能膳食系统'}
    },
    {
        path: '/profile/family',
        name: 'FamilyProfile',
        component: () => import('../views/FamilyManagement.vue'),
        meta: {title: '家庭档案 - 智能膳食系统'}
    },
    {
        path: '/dish-manage',
        name: 'DishManage',
        component: () => import('../views/DishManage.vue'),
        meta: {title: '菜谱管理 - 智能膳食系统'}
    },
    {
        path: '/ingredients',
        name: 'IngredientManage',
        component: () => import('../views/IngredientManage.vue'),
        meta: {title: '原材料管理 - 智能膳食系统'}
    },
    {
        path: '/cooking-steps',
        name: 'CookingStepManage',
        component: () => import('../views/CookingStepManage.vue'),
        meta: {title: '烹饪步骤 - 智能膳食系统'}
    },
    {
        path: '/system-setting',
        name: 'SystemSetting',
        component: () => import('../views/SystemSetting.vue'),
        redirect: '/system-setting/user',
        meta: {title: '系统设置 - 智能膳食系统'},
        children: [
            {
                path: 'user',
                name: 'UserManage',
                component: () => import('../views/system/UserManage.vue'),
                meta: {title: '用户管理 - 智能膳食系统'}
            },
            {
                path: 'role',
                name: 'RoleManage',
                component: () => import('../views/system/RoleManage.vue'),
                meta: {title: '角色管理 - 智能膳食系统'}
            },
            {
                path: 'menu',
                name: 'MenuManage',
                component: () => import('../views/system/MenuManage.vue'),
                meta: {title: '菜单管理 - 智能膳食系统'}
            },
            {
                path: 'dict',
                name: 'DictManage',
                component: () => import('../views/system/DictManage.vue'),
                meta: {title: '字典管理 - 智能膳食系统'}
            },
            {
                path: 'file',
                name: 'FileStorage',
                component: () => import('../views/system/FileStorage.vue'),
                meta: {title: '文件存储 - 智能膳食系统'}
            },
            {
                path: 'login-log',
                name: 'LoginLog',
                component: () => import('../views/system/LoginLog.vue'),
                meta: {title: '登录日志 - 智能膳食系统'}
            },
            {
                path: 'op-log',
                name: 'OperationLog',
                component: () => import('../views/system/OperationLog.vue'),
                meta: {title: '操作日志 - 智能膳食系统'}
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局路由守卫：Title 修改及登录拦截
router.beforeEach((to, _from, next) => {
    if (to.meta.title) {
        document.title = to.meta.title as string
    }

    const token = localStorage.getItem('token')
    if (to.path !== '/login' && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router

