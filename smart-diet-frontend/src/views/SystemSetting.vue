<template>
  <div class="system-setting-layout">
    <!-- 左侧 Neo-brutalism 导航 -->
    <div class="settings-sidebar">
      <div class="sidebar-header">
        <h2>系统控制台</h2>
        <span class="mono-badge">v1.0.0</span>
      </div>
      <div class="sidebar-menu">
        <button
            v-for="tab in tabList"
            :key="tab.value"
            :class="['menu-item', { active: activeTab === tab.value }]"
            @click="changeTab(tab)"
        >
          <span class="menu-icon">{{ tab.icon }}</span>
          <span class="menu-text">{{ tab.label }}</span>
        </button>
      </div>
    </div>

    <!-- 右侧主内容区域 -->
    <div class="settings-content-wrapper">
      <div class="content-header">
        <h1>{{ currentTabLabel }}</h1>
        <p class="content-desc">{{ currentTabDesc }}</p>
      </div>

      <div class="content-body brutalist-card">
        <router-view/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const router = useRouter()
const route = useRoute()

// 7个系统管理菜单项（去除了Quartz定时任务）
const tabList = [
  {
    value: 'user',
    label: '用户管理',
    icon: '👥',
    desc: '系统平台操作员管理，包含密码重置和角色绑定',
    path: '/system-setting/user'
  },
  {
    value: 'role',
    label: '角色管理',
    icon: '🛡️',
    desc: '角色设定及页面菜单权限的管理与绑定',
    path: '/system-setting/role'
  },
  {
    value: 'menu',
    label: '菜单树管理',
    icon: '📂',
    desc: '系统左侧导航栏、页面按钮控制树形数据',
    path: '/system-setting/menu'
  },
  {
    value: 'dict',
    label: '数据字典',
    icon: '📖',
    desc: '用于全局下拉选单、各类枚举的静态参数配置',
    path: '/system-setting/dict'
  },
  {
    value: 'file',
    label: '云存储管理',
    icon: '☁️',
    desc: 'S3/MinIO 对象存储管理及文件上传预览',
    path: '/system-setting/file'
  },
  {
    value: 'loginLog',
    label: '登录日志',
    icon: '🔑',
    desc: '审计管理员及所有做饭人用户的登录记录',
    path: '/system-setting/login-log'
  },
  {
    value: 'opLog',
    label: '操作日志',
    icon: '📝',
    desc: '审计所有敏感数据的变更、上传和特权操作记录',
    path: '/system-setting/op-log'
  }
]

// 联动高亮路由选中状态
const activeTab = computed(() => {
  const currentPath = route.path
  const matchedTab = tabList.find(tab => currentPath.startsWith(tab.path))
  return matchedTab ? matchedTab.value : 'user'
})

const currentTabLabel = computed(() => tabList.find(t => t.value === activeTab.value)?.label)
const currentTabDesc = computed(() => tabList.find(t => t.value === activeTab.value)?.desc)

const changeTab = (tab: typeof tabList[0]) => {
  router.push(tab.path)
}
</script>

<style scoped>
.system-setting-layout {
  display: flex;
  min-height: calc(100vh - 56px);
  background-color: #ffffff;
  font-family: 'figmaSans', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  box-sizing: border-box;
}

/* 左侧 Neo-brutalism 导航栏 */
.settings-sidebar {
  width: 260px;
  background-color: #f7f7f5; /* surface-soft */
  border-right: 3px solid #000000;
  padding: 30px 16px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  box-sizing: border-box;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 12px;
}

.sidebar-header h2 {
  font-size: 20px;
  font-weight: 800;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: -0.5px;
}

.mono-badge {
  font-family: 'figmaMono', monospace;
  font-size: 11px;
  background: #000000;
  color: #ffffff;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 700;
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 2px solid transparent;
  background: transparent;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item:hover {
  background-color: #ffffff;
  border-color: #000000;
}

.menu-item.active {
  background-color: #000000;
  color: #ffffff;
  border-color: #000000;
}

.menu-icon {
  font-size: 18px;
}

.menu-text {
  font-size: 15px;
  font-weight: 600;
}

/* 右侧主内容区域 */
.settings-content-wrapper {
  flex: 1;
  padding: 40px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 30px;
  box-sizing: border-box;
  overflow-x: hidden;
}

.content-header h1 {
  font-size: 32px;
  font-weight: 900;
  letter-spacing: -1px;
  margin: 0 0 6px 0;
}

.content-desc {
  font-size: 15px;
  color: #555555;
  margin: 0;
}

/* Figma brutalist 表格容器 */
.brutalist-card {
  border: 3px solid #000000;
  border-radius: 20px;
  padding: 30px;
  background-color: #ffffff;
  box-shadow: 6px 6px 0px #000000;
}
</style>
