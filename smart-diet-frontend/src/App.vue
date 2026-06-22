<template>
  <div class="app-layout" :class="{ 'full-width-layout': isNavHidden }">
    <!-- 左侧常驻侧边栏 (PC 端全局 aside)，登录页与移动端 H5 隐藏 -->
    <aside v-if="!isNavHidden" class="app-sidebar">
      <div class="sidebar-brand-logo" @click="$router.push('/')">
        SmartDiet
        <span class="logo-badge caption">Alpha</span>
      </div>

      <!-- 垂直导航菜单 (分组二级树状结构) -->
      <nav class="sidebar-nav-menu">
        <!-- 分组 1: 健康分析 -->
        <div class="menu-group">
          <div class="group-header" @click="toggleGroup('analysis')">
            <span class="group-icon">⬡</span>
            <span class="group-title">健康分析</span>
            <span class="group-arrow" :class="{ 'collapsed': !expandedGroups.analysis }">▾</span>
          </div>
          <div v-show="expandedGroups.analysis" class="sub-menu-list">
            <router-link
                to="/dashboard"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                计划进度
              </button>
            </router-link>

            <router-link
                to="/weight"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                体重追踪
              </button>
            </router-link>
          </div>
        </div>

        <!-- 分组 2: 膳食计划 -->
        <div class="menu-group">
          <div class="group-header" @click="toggleGroup('diet')">
            <span class="group-icon">✦</span>
            <span class="group-title">膳食管理</span>
            <span class="group-arrow" :class="{ 'collapsed': !expandedGroups.diet }">▾</span>
          </div>
          <div v-show="expandedGroups.diet" class="sub-menu-list">
            <router-link
                to="/meal-planner"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                联合配餐
              </button>
            </router-link>

            <router-link
                to="/dishes"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                菜谱广场
              </button>
            </router-link>
          </div>
        </div>

        <!-- 分组 2.5: 菜谱库管理 -->
        <div class="menu-group">
          <div class="group-header" @click="toggleGroup('recipeLibrary')">
            <span class="group-icon">📖</span>
            <span class="group-title">菜谱库管理</span>
            <span class="group-arrow" :class="{ 'collapsed': !expandedGroups.recipeLibrary }">▾</span>
          </div>
          <div v-show="expandedGroups.recipeLibrary" class="sub-menu-list">
            <router-link
                to="/dish-manage"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                菜谱管理
              </button>
            </router-link>

            <router-link
                to="/ingredients"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                原材料管理
              </button>
            </router-link>

            <router-link
                to="/cooking-steps"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                操作步骤
              </button>
            </router-link>
          </div>
        </div>

        <!-- 分组 3: 档案管理 -->
        <div class="menu-group">
          <div class="group-header" @click="toggleGroup('profile')">
            <span class="group-icon">🗂</span>
            <span class="group-title">档案管理</span>
            <span class="group-arrow" :class="{ 'collapsed': !expandedGroups.profile }">▾</span>
          </div>
          <div v-show="expandedGroups.profile" class="sub-menu-list">
            <router-link
                to="/profile/personal"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                个人档案
              </button>
            </router-link>

            <router-link
                to="/profile/family"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                家庭档案
              </button>
            </router-link>
          </div>
        </div>

        <!-- 分组 4: 系统设置 -->
        <div class="menu-group">
          <div class="group-header" @click="toggleGroup('system')">
            <span class="group-icon">⚙</span>
            <span class="group-title">系统管理</span>
            <span class="group-arrow" :class="{ 'collapsed': !expandedGroups.system }">▾</span>
          </div>
          <div v-show="expandedGroups.system" class="sub-menu-list">

            <router-link
                to="/system-setting/user"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                用户管理
              </button>
            </router-link>

            <router-link
                to="/system-setting/role"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                角色管理
              </button>
            </router-link>

            <router-link
                to="/system-setting/menu"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                菜单管理
              </button>
            </router-link>

            <router-link
                to="/system-setting/dict"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                字典管理
              </button>
            </router-link>

            <router-link
                to="/system-setting/file"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                存储管理
              </button>
            </router-link>

            <router-link
                to="/system-setting/login-log"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                登录日志
              </button>
            </router-link>

            <router-link
                to="/system-setting/op-log"
                custom
                v-slot="{ navigate, isActive }"
            >
              <button
                  :class="['sub-menu-btn', isActive ? 'active' : '']"
                  @click="navigate"
              >
                操作日志
              </button>
            </router-link>
          </div>
        </div>
      </nav>

      <!-- 侧边栏底部用户信息和退出 -->
      <div class="sidebar-footer">
        <div class="user-profile">
          <div class="online-indicator"></div>
          <span class="eyebrow profile-name">COOKER: {{ currentRealName }}</span>
        </div>
        <button class="btn-logout-sidebar" @click="handleLogout">
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 右侧容器 -->
    <div class="app-main-container">
      <!-- 跑马灯 logo 条 (marquee-strip)，导航隐藏时隐藏 -->
      <template v-if="!isNavHidden">
        <div class="marquee-strip">
          <div class="marquee-content">
            <span class="eyebrow">◆ BMI MANAGEMENT</span>
            <span class="eyebrow">◆ BMR测评</span>
            <span class="eyebrow">◆ TDEE等比分餐</span>
            <span class="eyebrow">◆ MINIO 存储</span>
            <span class="eyebrow">◆ 菜系配置避重</span>
            <span class="eyebrow">◆ 拿手菜培养</span>
            <span class="eyebrow">◆ BMI MANAGEMENT</span>
            <span class="eyebrow">◆ BMR测评</span>
            <span class="eyebrow">◆ TDEE等比分餐</span>
            <span class="eyebrow">◆ MINIO 存储</span>
            <span class="eyebrow">◆ 菜系配置避重</span>
            <span class="eyebrow">◆ 拿手菜培养</span>
          </div>
        </div>
      </template>

      <!-- 主页面渲染区域 -->
      <main class="main-content">
        <router-view :key="route.path + '_' + currentGroupId"/>
      </main>

      <!-- 页脚 (footer) -->
      <footer class="bottom-footer hairline-border">
        <div class="footer-container">
          <div class="footer-brand">
            <h2 class="display-lg">SmartDiet</h2>
            <p class="caption">PERSONALIZED FAMILY HEALTH ASSISTANT</p>
          </div>
          <div class="footer-links">
            <div class="link-column">
              <span class="caption column-title">PROJECT TECH</span>
              <span class="body-sm">Spring Boot 3</span>
              <span class="body-sm">Vue 3 + Vite</span>
              <span class="body-sm">PostgreSQL</span>
            </div>
            <div class="link-column">
              <span class="caption column-title">DEVICES</span>
              <span class="body-sm">MinIO (S3 SDK)</span>
              <span class="body-sm">Quartz Job</span>
              <span class="body-sm">Flyway DB</span>
            </div>
          </div>
        </div>
        <div class="footer-bottom content-container">
          <p class="caption">© 2026 SMARTDIET. DESIGN INSPIRED BY FIGMA EDITORIAL SYSTEM.</p>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, provide, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const router = useRouter()
const route = useRoute()

// 全局共享家庭组 ID
const currentGroupId = ref(1)

const changeGroupId = (id: number) => {
  currentGroupId.value = id
  localStorage.setItem('activeGroupId', String(id))
}

provide('groupId', currentGroupId)
provide('changeGroupId', changeGroupId)
provide('cookUserId', 1)

// 是否隐藏导航和跑马灯（登录页及移动端页面）
const isNavHidden = computed(() => {
  return route.path === '/login' || route.path.startsWith('/m/')
})

// 折叠导航菜单控制
const expandedGroups = ref<Record<string, boolean>>({
  analysis: true,
  diet: true,
  recipeLibrary: true,
  profile: true,
  system: true
})

const toggleGroup = (group: 'analysis' | 'diet' | 'recipeLibrary' | 'profile' | 'system') => {
  expandedGroups.value[group] = !expandedGroups.value[group]
}

// 动态读取当前登录用户的真实姓名
const currentRealName = ref('张大厨')

onMounted(() => {
  const savedGroupId = localStorage.getItem('activeGroupId')
  if (savedGroupId) {
    currentGroupId.value = Number(savedGroupId)
  }

  const realName = localStorage.getItem('realName')
  if (realName && realName.trim()) {
    currentRealName.value = realName.trim()
  }
})

// 退出登录：清除本地存储并跳转到登录页
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('realName')
  localStorage.removeItem('userId')
  localStorage.removeItem('menuList')
  localStorage.removeItem('permissions')
  currentRealName.value = '张大厨'
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: row;
  min-height: 100vh;
  background-color: var(--canvas);
}

.app-layout.full-width-layout {
  flex-direction: column;
}

/* 左侧常驻侧边栏 */
.app-sidebar {
  width: 240px;
  min-width: 240px;
  height: 100vh;
  position: sticky;
  top: 0;
  background-color: var(--surface-1);
  border-right: 1px solid var(--hairline);
  display: flex;
  flex-direction: column;
  padding: var(--spacing-xl) var(--spacing-md);
  box-sizing: border-box;
  z-index: 1000;
}

.sidebar-brand-logo {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: var(--ink);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xl);
  padding: 0 var(--spacing-xs);
}

.logo-badge {
  font-size: 9px;
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  color: var(--primary-hover);
  padding: 2px 6px;
  border-radius: var(--rounded-xs);
  text-transform: uppercase;
}

.sidebar-nav-menu {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  flex-grow: 1;
  overflow-y: auto;
}

/* 树状二级菜单样式 */
.menu-group {
  display: flex;
  flex-direction: column;
}

.group-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 8px var(--spacing-xs);
  cursor: pointer;
  user-select: none;
}

.group-icon {
  font-size: 14px;
  color: var(--primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
}

.group-title {
  flex-grow: 1;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-subtle);
  letter-spacing: -0.01em;
  transition: color 0.15s ease;
}

.group-header:hover .group-title {
  color: var(--ink);
}

.group-arrow {
  font-size: 9px;
  color: var(--ink-tertiary);
  transition: transform 0.2s ease;
}

.group-arrow.collapsed {
  transform: rotate(-90deg);
}

.sub-menu-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-left: 26px;
  margin-top: 4px;
}

.sub-menu-btn {
  display: block;
  width: 100%;
  padding: 8px 12px;
  background: transparent;
  border: none;
  color: var(--ink-subtle);
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--rounded-sm);
  cursor: pointer;
  text-align: left;
  transition: all 0.15s ease;
}

.sub-menu-btn:hover {
  background-color: var(--surface-2);
  color: var(--ink);
}

.sub-menu-btn.active {
  background-color: var(--surface-3);
  color: var(--primary);
  font-weight: 600;
}

/* 侧边栏底部 */
.sidebar-footer {
  border-top: 1px solid var(--hairline);
  padding-top: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.sidebar-footer .user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 0 var(--spacing-xs);
}

.sidebar-footer .online-indicator {
  width: 6px;
  height: 6px;
  background-color: var(--semantic-success);
  border-radius: var(--rounded-full);
}

.sidebar-footer .profile-name {
  font-size: 11px;
  font-weight: 500;
  color: var(--ink-muted);
  text-transform: uppercase;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 侧栏退出登录按钮 */
.btn-logout-sidebar {
  font-size: 12px;
  font-weight: 500;
  width: 100%;
  padding: 8px var(--spacing-md);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-sm);
  background-color: var(--surface-2);
  color: var(--ink-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  transition: all 0.15s ease;
}

.btn-logout-sidebar:hover {
  background-color: var(--surface-2);
  border-color: var(--primary);
  color: var(--primary);
}

/* 右侧主容器 */
.app-main-container {
  flex-grow: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 跑马灯条 */
.marquee-strip {
  height: 32px;
  background-color: var(--surface-2);
  color: var(--ink-subtle);
  display: flex;
  align-items: center;
  overflow: hidden;
  white-space: nowrap;
  border-bottom: 1px solid var(--hairline);
}

.marquee-content {
  display: inline-block;
  animation: marquee 25s linear infinite;
  padding-left: 100%;
}

.marquee-content span {
  font-size: 11px;
  margin-right: var(--spacing-xl);
  display: inline-block;
}

@keyframes marquee {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(-100%, 0, 0);
  }
}

.main-content {
  flex-grow: 1;
  background-color: var(--canvas);
}

/* Footer 页脚 */
.bottom-footer {
  background-color: var(--canvas);
  color: var(--ink-subtle);
  margin-top: var(--spacing-section);
  border-top: 1px solid var(--hairline);
}

.footer-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: var(--spacing-xxl) var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--spacing-xl);
}

.footer-brand {
  max-width: 400px;
}

.footer-brand h2 {
  font-size: 26px;
  margin-bottom: var(--spacing-xs);
  color: var(--ink);
  font-weight: 700;
}

.footer-links {
  display: flex;
  gap: var(--spacing-xxl);
}

.link-column {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.column-title {
  margin-bottom: var(--spacing-xs);
  color: var(--ink-tertiary);
}

.footer-bottom {
  border-top: 1px solid var(--hairline);
  padding: var(--spacing-md) 0 var(--spacing-xl) 0;
  text-align: center;
  color: var(--ink-tertiary);
}
</style>
