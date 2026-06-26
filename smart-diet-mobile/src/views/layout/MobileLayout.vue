<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useRoleStore} from '../../store/role'
import {showConfirmDialog, showToast} from 'vant'

const router = useRouter()
const route = useRoute()
const roleStore = useRoleStore()

const activeTab = ref(0)
const showGroupSheet = ref(false)

// 动态计算当前的顶部导航标题
const title = computed(() => {
  return (route.meta.title as string) || '智慧饮食系统'
})

// 计算当前关联的家庭组名称
const currentGroupName = computed(() => {
  const g = roleStore.userGroups.find(x => x.id === roleStore.groupId)
  return g ? g.name : '选择组'
})

// 格式化家庭组选择器的 Action 列表
const groupActions = computed(() => {
  return roleStore.userGroups.map(g => ({
    name: g.name,
    id: g.id,
    color: g.id === roleStore.groupId ? '#2ecc71' : ''
  }))
})

// 选择家庭组事件
const onSelectGroup = (action: any) => {
  roleStore.changeGroup(action.id)
  showGroupSheet.value = false
  showToast(`已切换至：${action.name}`)
}

// 检查是否拥有多个身份
const isMultiRole = computed(() => roleStore.userRoles.length > 1)

// 双视角穿梭一键切换
const handleSwitchRole = () => {
  const nextRole = roleStore.switchRole()
  showToast({
    message: `已穿梭至：${nextRole === 'chef' ? '👨‍🍳 掌勺主厨' : '🍲 就餐成员'}`,
    icon: nextRole === 'chef' ? 'manager-o' : 'friends-o'
  })
  if (nextRole === 'chef') {
    router.push('/chef/home')
  } else {
    router.push('/diner/home')
  }
}

// 根据用户的角色动态提供不同的 Tab 菜单项 (均包含菜谱做法广场)
const tabs = computed(() => {
  if (roleStore.role === 'chef') {
    return [
      {text: '烹饪指南', icon: 'description-o', path: '/chef/home'},
      {text: '智能配餐', icon: 'records-o', path: '/chef/recipe-generator'},
      {text: '做法广场', icon: 'apps-o', path: '/square'},
      {text: '买菜清单', icon: 'shopping-cart-o', path: '/chef/grocery'}
    ]
  } else {
    return [
      {text: '今日餐盘', icon: 'smile-o', path: '/diner/home'},
      {text: '我想吃', icon: 'like-o', path: '/diner/wish'},
      {text: '做法广场', icon: 'apps-o', path: '/square'},
      {text: '健康中心', icon: 'chart-trending-o', path: '/diner/health'}
    ]
  }
})

// 监听路由变化，同步高亮底部对应的 Tab 项
watch(
    () => route.path,
    (newPath) => {
      const idx = tabs.value.findIndex(tab => tab.path === newPath)
      if (idx !== -1) {
        activeTab.value = idx
      }
    },
    {immediate: true}
)

// 底部 Tab 切换事件
const onTabChange = (index: number) => {
  const targetPath = tabs.value[index].path
  router.push(targetPath)
}

// 退出登录
const handleLogout = () => {
  showConfirmDialog({
    title: '退出登录',
    message: '确定要安全退出系统吗？'
  }).then(() => {
    roleStore.clear()
    router.push('/role-select')
  }).catch(() => {
  })
}
</script>

<template>
  <div class="mobile-layout">
    <!-- 顶部状态导航条 -->
    <van-nav-bar :title="title">
      <template #left>
        <div class="group-select-pill" @click="showGroupSheet = true">
          <van-icon name="friends-o" size="14" color="#2ecc71"/>
          <span class="group-name">{{ currentGroupName }}</span>
          <van-icon name="arrow-down" size="9" color="#bdc3c7"/>
        </div>
      </template>

      <template #right>
        <div class="navbar-right-actions">
          <van-tag
              type="primary"
              size="medium"
              v-if="isMultiRole"
              class="switch-role-tag"
              @click="handleSwitchRole"
          >
            🔄 切至{{ roleStore.role === 'chef' ? '就餐' : '大厨' }}
          </van-tag>
          <van-icon name="power-setting" size="18" class="logout-btn" @click="handleLogout"/>
        </div>
      </template>
    </van-nav-bar>

    <!-- 主页面内容区域 (带过渡动画) -->
    <div class="layout-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component"/>
        </transition>
      </router-view>
    </div>

    <!-- 底部 Tab 导航 -->
    <van-tabbar
        v-model="activeTab"
        @change="onTabChange"
        active-color="#2ecc71"
        inactive-color="#7f8c8d"
        placeholder
        safe-area-inset-bottom
    >
      <van-tabbar-item
          v-for="(tab, index) in tabs"
          :key="index"
          :icon="tab.icon"
      >
        {{ tab.text }}
      </van-tabbar-item>
    </van-tabbar>

    <!-- 家庭组选择底面板 -->
    <van-action-sheet
        v-model:show="showGroupSheet"
        :actions="groupActions"
        cancel-text="取消"
        close-on-click-action
        @select="onSelectGroup"
        title="切换所属家庭组"
    />
  </div>
</template>

<style scoped>
.mobile-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
}

.layout-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 12px 0 12px;
  background-color: #f7f8fa;
  -webkit-overflow-scrolling: touch;
}

:deep(.van-nav-bar) {
  background-color: #ffffff;
  border-bottom: 1px solid #ebedf0;
}

.group-select-pill {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: #f1f2f6;
  padding: 4px 8px;
  border-radius: 20px;
  max-width: 140px;
  border: 1px solid #e8e8e8;
}

.group-select-pill:active {
  opacity: 0.8;
}

.group-name {
  font-size: 11px;
  font-weight: 700;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.navbar-right-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.switch-role-tag {
  padding: 3px 6px;
  border-radius: 20px;
  cursor: pointer;
  background: linear-gradient(135deg, #2ecc71, #27ae60);
  font-weight: 600;
  color: white;
  border: none;
  font-size: 10px;
}

.switch-role-tag:active {
  opacity: 0.8;
}

.logout-btn {
  color: #e74c3c;
  padding: 4px;
}

.logout-btn:active {
  transform: scale(0.9);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
