<template>
  <div class="system-setting-layout">
    <!-- 右侧主内容区域 (物理移除 settings-sidebar 后占据 100% 满宽) -->
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
import {useRoute} from 'vue-router'

const route = useRoute()

// 7个系统管理菜单描述字典（用于给右侧标题描述栏读取，不在页面中渲染为侧边栏）
const tabList = [
  {
    value: 'user',
    label: '用户管理',
    desc: '系统平台操作员管理，包含密码重置和角色绑定',
    path: '/system-setting/user'
  },
  {
    value: 'role',
    label: '角色管理',
    desc: '角色设定及页面菜单权限的管理与绑定',
    path: '/system-setting/role'
  },
  {
    value: 'menu',
    label: '菜单树管理',
    desc: '系统左侧导航栏、页面按钮控制树形数据',
    path: '/system-setting/menu'
  },
  {
    value: 'dict',
    label: '数据字典',
    desc: '用于全局下拉选单、各类枚举的静态参数配置',
    path: '/system-setting/dict'
  },
  {
    value: 'file',
    label: '云存储管理',
    desc: 'S3/MinIO 对象存储管理及文件上传预览',
    path: '/system-setting/file'
  },
  {
    value: 'loginLog',
    label: '登录日志',
    desc: '审计管理员及所有做饭人用户的登录记录',
    path: '/system-setting/login-log'
  },
  {
    value: 'opLog',
    label: '操作日志',
    desc: '审计所有敏感数据的变更、上传和特权操作记录',
    path: '/system-setting/op-log'
  }
]

// 联动路由获取当前在哪个管理子视图
const activeTab = computed(() => {
  const currentPath = route.path
  const matchedTab = tabList.find(tab => currentPath.startsWith(tab.path))
  return matchedTab ? matchedTab.value : 'user'
})

const currentTabLabel = computed(() => tabList.find(t => t.value === activeTab.value)?.label || '系统控制台')
const currentTabDesc = computed(() => tabList.find(t => t.value === activeTab.value)?.desc || '管理系统基础配置与运行日志')
</script>

<style scoped>
.system-setting-layout {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 32px); /* 减去跑马灯高度 */
  background-color: var(--canvas);
  box-sizing: border-box;
}

/* 右侧主内容区域 */
.settings-content-wrapper {
  flex: 1;
  padding: 40px;
  background-color: var(--canvas);
  display: flex;
  flex-direction: column;
  gap: 30px;
  box-sizing: border-box;
  overflow-x: hidden;
}

.content-header h1 {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.5px;
  margin: 0 0 6px 0;
  color: var(--ink);
}

.content-desc {
  font-size: 14px;
  color: var(--ink-subtle);
  margin: 0;
}

/* 极简卡片容器 */
.brutalist-card {
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  padding: 30px;
  background-color: var(--surface-1);
}

.brutalist-card :deep(.panel-header-section) {
  display: none !important;
}
</style>
