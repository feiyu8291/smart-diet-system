<template>
  <div class="app-layout">
    <!-- 顶部导航栏 (Monochrome top-nav)，登录页时隐藏 -->
    <header v-if="!isLoginPage" class="top-header hairline-bottom">
      <div class="header-container">
        <!-- Logo -->
        <div class="brand-logo" @click="$router.push('/')">
          SmartDiet
          <span class="logo-badge caption">Alpha</span>
        </div>

        <!-- 导航项 (Pill-Tabs) -->
        <nav class="nav-pills">
          <router-link
              to="/dashboard"
              custom
              v-slot="{ navigate, isActive }"
          >
            <button
                :class="isActive ? 'btn-primary' : 'btn-secondary'"
                @click="navigate"
            >
              计划大盘
            </button>
          </router-link>

          <router-link
              to="/meal-planner"
              custom
              v-slot="{ navigate, isActive }"
          >
            <button
                :class="isActive ? 'btn-primary' : 'btn-secondary'"
                @click="navigate"
            >
              联合配餐
            </button>
          </router-link>

          <router-link
              to="/family"
              custom
              v-slot="{ navigate, isActive }"
          >
            <button
                :class="isActive ? 'btn-primary' : 'btn-secondary'"
                @click="navigate"
            >
              家庭档案
            </button>
          </router-link>

          <router-link
              to="/dishes"
              custom
              v-slot="{ navigate, isActive }"
          >
            <button
                :class="isActive ? 'btn-primary' : 'btn-secondary'"
                @click="navigate"
            >
              菜谱广场
            </button>
          </router-link>

          <router-link
              to="/weight"
              custom
              v-slot="{ navigate, isActive }"
          >
            <button
                :class="isActive ? 'btn-primary' : 'btn-secondary'"
                @click="navigate"
            >
              体重追踪
            </button>
          </router-link>

          <router-link
              to="/system-setting"
              custom
              v-slot="{ navigate, isActive }"
          >
            <button
                :class="isActive ? 'btn-primary' : 'btn-secondary'"
                @click="navigate"
            >
              系统设置
            </button>
          </router-link>
        </nav>

        <!-- 当前做饭人账号显示 (在线状态) -->
        <div class="user-profile">
          <div class="online-indicator"></div>
          <span class="eyebrow profile-name">COOKER: {{ currentRealName }}</span>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <!-- 跑马灯 logo 条 (marquee-strip)，登录页时隐藏 -->
    <template v-if="!isLoginPage">
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
      <router-view/>
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
</template>

<script setup lang="ts">
import {computed, onMounted, provide, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const router = useRouter()
const route = useRoute()

// 全局共享家庭组 ID 与做饭人 ID
provide('groupId', 1)
provide('cookUserId', 1)

// 是否处于登录页（隐藏导航、跑马灯）
const isLoginPage = computed(() => route.path === '/login')

// 动态读取当前登录用户的真实姓名
const currentRealName = ref('张大厨')

onMounted(() => {
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
  flex-direction: column;
  min-height: 100vh;
}

.top-header {
  height: 56px;
  background-color: var(--canvas);
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
}

.header-container {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand-logo {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: var(--ink);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.logo-badge {
  font-size: 9px;
  background-color: var(--block-lime);
  color: var(--ink);
  padding: 2px 6px;
  border-radius: var(--rounded-pill);
}

.nav-pills {
  display: flex;
  gap: var(--spacing-xs);
}

.nav-pills button {
  font-size: 14px;
  font-weight: 500;
  padding: 6px 16px;
  cursor: pointer;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.online-indicator {
  width: 8px;
  height: 8px;
  background-color: var(--semantic-success);
  border-radius: var(--rounded-full);
}

.profile-name {
  font-size: 11px;
  font-weight: 700;
}

/* 退出登录按钮 */
.btn-logout {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border: 1.5px solid var(--ink);
  border-radius: var(--rounded-pill);
  background-color: transparent;
  color: var(--ink);
  cursor: pointer;
  letter-spacing: 0.03em;
  transition: background-color 0.12s, color 0.12s;
}

.btn-logout:hover {
  background-color: var(--ink);
  color: var(--canvas);
}

/* 跑马灯条 (marquee-strip) */
.marquee-strip {
  height: 36px;
  background-color: var(--inverse-canvas);
  color: var(--inverse-ink);
  display: flex;
  align-items: center;
  overflow: hidden;
  white-space: nowrap;
}

.marquee-content {
  display: inline-block;
  animation: marquee 25s linear infinite;
  padding-left: 100%;
}

.marquee-content span {
  font-size: 12px;
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
  color: var(--ink);
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
  font-size: 32px;
  margin-bottom: var(--spacing-xs);
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
  color: #888888;
}

.footer-bottom {
  border-top: 1px solid var(--hairline-soft);
  padding: var(--spacing-md) 0 var(--spacing-xl) 0;
  text-align: center;
  color: #888888;
}
</style>
