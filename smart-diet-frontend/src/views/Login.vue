<template>
  <div class="login-container">
    <!-- 背景点阵装饰 -->
    <div class="dots-grid"></div>

    <div class="login-card">
      <div class="card-header">
        <h1 class="system-title">SMART DIET</h1>
        <p class="system-subtitle">智能减肥健康食谱管理系统</p>
      </div>

      <!-- Pill 式 Tab 切换 -->
      <div class="tabs-container">
        <button
            :class="['tab-btn', { active: activeTab === 'pwd' }]"
            @click="activeTab = 'pwd'"
        >
          密码登录
        </button>
        <button
            :class="['tab-btn', { active: activeTab === 'idcard' }]"
            @click="activeTab = 'idcard'"
        >
          身份证免密登录
        </button>
      </div>

      <!-- 表单区域 -->
      <div class="form-content">
        <form @submit.prevent="handleLogin">
          <!-- 账号密码登录 -->
          <div v-if="activeTab === 'pwd'" class="form-group-wrapper">
            <div class="form-group">
              <label for="username">用户名</label>
              <input
                  id="username"
                  v-model="loginForm.username"
                  type="text"
                  placeholder="请输入用户名"
                  required
                  class="brutalist-input"
              />
            </div>
            <div class="form-group">
              <label for="password">密码</label>
              <input
                  id="password"
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  required
                  class="brutalist-input"
              />
            </div>
          </div>

          <!-- 身份证登录 -->
          <div v-else class="form-group-wrapper">
            <div class="form-group">
              <label for="idCardNum">身份证号</label>
              <input
                  id="idCardNum"
                  v-model="loginForm.idCardNum"
                  type="text"
                  placeholder="请输入身份证号"
                  required
                  class="brutalist-input"
              />
            </div>
          </div>

          <button type="submit" :disabled="loading" class="brutalist-btn-primary">
            {{ loading ? '登录中...' : '开始管理健康' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import request from '../utils/request'
import JSEncrypt from 'jsencrypt'

const router = useRouter()
const activeTab = ref<'pwd' | 'idcard'>('pwd')
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  idCardNum: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    let data: any
    if (activeTab.value === 'pwd') {
      // 1. 获取公钥
      const res: any = await request.get('/api/auth/public-key')
      const publicKey = res && typeof res === 'object' && 'data' in res ? res.data : res
      if (!publicKey) {
        ElMessage.error('安全传输初始化失败，请稍后重试！')
        return
      }

      const encrypt = new JSEncrypt()
      encrypt.setPublicKey(publicKey)
      const encrypted = encrypt.encrypt(loginForm.password)
      if (!encrypted) {
        ElMessage.error('密码加密失败，请重试！')
        return
      }

      // 密码登录，对应后端 /api/auth/login
      const response = await request.post('/api/auth/login', {
        username: loginForm.username,
        userPassword: encrypted
      })
      data = response && response.data ? response.data : response
    } else {
      // 身份证登录，对应后端 /api/auth/loginByIdCard (使用 query 参数的形式)
      const response = await request.post(`/api/auth/loginByIdCard?idCardNum=${loginForm.idCardNum}`)
      data = response && response.data ? response.data : response
    }

    if (data && data.token) {
      localStorage.setItem('token', data.token)
      // 缓存当前登录用户信息
      if (data.sysUser) {
        localStorage.setItem('username', data.sysUser.username || '')
        localStorage.setItem('realName', data.sysUser.realName || '')
        localStorage.setItem('userId', data.sysUser.userId || '')
      }
      ElMessage.success('登录成功')

      // 登录成功后获取用户权限菜单
      try {
        const res: any = await request.get('/api/auth/menu')
        const permData = res && res.data ? res.data : null
        if (permData && permData.menuTreeList) {
          localStorage.setItem('menuList', JSON.stringify(permData.menuTreeList))
          localStorage.setItem('permissions', JSON.stringify(permData.menuCodes || []))
        }
      } catch (e) {
        console.error('获取权限菜单失败', e)
      }

      router.push('/dashboard')
    } else {
      ElMessage.error('登录失败，未获取到有效 Token')
    }
  } catch (error: any) {
    console.error('登录异常', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--canvas);
  position: relative;
  overflow: hidden;
}

/* 点阵背景 */
.dots-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(rgba(247, 248, 248, 0.06) 1px, transparent 1px);
  background-size: 24px 24px;
  z-index: 1;
}

/* Linear 风格卡片 */
.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background-color: var(--surface-1);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  box-shadow: 0px 8px 30px rgba(0, 0, 0, 0.5);
  z-index: 2;
  position: relative;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.system-title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -1.0px;
  color: var(--ink);
  margin: 0 0 6px 0;
  text-transform: uppercase;
}

.system-subtitle {
  font-size: 13px;
  font-weight: 400;
  color: var(--ink-subtle);
  margin: 0;
}

/* 选项卡 */
.tabs-container {
  display: flex;
  background-color: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  padding: 3px;
  margin-bottom: 24px;
}

.tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--rounded-sm);
  cursor: pointer;
  color: var(--ink-subtle);
  transition: all 0.15s ease;
}

.tab-btn.active {
  background-color: var(--surface-2);
  color: var(--ink);
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.2);
}

/* 表单组件 */
.form-group-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: left;
}

.form-group label {
  font-size: 11px;
  font-weight: 500;
  color: var(--ink-subtle);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 输入框 */
.brutalist-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 14px;
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  background-color: var(--canvas);
  font-size: 14px;
  font-weight: 400;
  color: var(--ink);
  outline: none;
  transition: all 0.15s ease;
}

.brutalist-input:focus {
  border-color: var(--hairline-strong);
  box-shadow: 0 0 0 2px rgba(94, 105, 210, 0.4);
}

/* 按钮 */
.brutalist-btn-primary {
  width: 100%;
  padding: 12px;
  background-color: var(--primary);
  color: var(--on-primary);
  border: none;
  border-radius: var(--rounded-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.brutalist-btn-primary:hover {
  background-color: var(--primary-hover);
}

.brutalist-btn-primary:active {
  background-color: var(--primary-focus);
}

.brutalist-btn-primary:disabled {
  background-color: var(--ink-tertiary);
  color: var(--ink-subtle);
  cursor: not-allowed;
}
</style>
