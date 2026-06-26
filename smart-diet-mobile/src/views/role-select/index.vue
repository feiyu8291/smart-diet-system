<script setup lang="ts">
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {useRoleStore} from '../../store/role'
import {showLoadingToast, showSuccessToast, showToast} from 'vant'
import request from '../../utils/request'

const router = useRouter()
const roleStore = useRoleStore()

const activeTab = ref<'tourist' | 'real'>('tourist')
const idCardNum = ref('')
const loading = ref(false)

// 一键体验模式的数据定义（Mock 数据，可保证手机预览顺畅工作）
const handleGuestLogin = (role: 'chef' | 'diner') => {
  roleStore.clear()
  roleStore.setAuth({
    token: 'mock-jwt-token-for-preview-' + role,
    userId: role === 'chef' ? '1001' : '1002',
    userName: role === 'chef' ? '张大厨' : '李四(家庭成员)'
  })

  if (role === 'chef') {
    // 模拟厨师身兼双职（既是厨师又是用餐人）
    roleStore.setRolesAndGroups(['chef', 'diner'], [
      {id: 1, name: '健康快乐一家人'},
      {id: 2, name: '活力四射健身组'}
    ])
    roleStore.setRole('chef')
    roleStore.setGroupAndProfile(1, 1) // 张大厨档案 ID 是 1
    showSuccessToast('已进入：双视角体验模式')
    router.push('/chef/home')
  } else {
    // 模拟仅普通用餐成员
    roleStore.setRolesAndGroups(['diner'], [
      {id: 1, name: '健康快乐一家人'}
    ])
    roleStore.setRole('diner')
    roleStore.setGroupAndProfile(1, 2) // 李四档案 ID 是 2
    showSuccessToast('已进入：就餐成员体验模式')
    router.push('/diner/home')
  }
}

// 真实后端身份证登录联调
const handleRealLogin = async (role: 'chef' | 'diner') => {
  if (!idCardNum.value.trim()) {
    showToast('请输入身份证号进行联调')
    return
  }

  loading.value = true
  const toast = showLoadingToast({
    message: '正在联调登录...',
    forbidClick: true,
    duration: 0
  })

  try {
    // 身份证登录，对应后端接口
    const response: any = await request.post(`/api/auth/loginByIdCard?idCardNum=${idCardNum.value.trim()}`)
    const data = response && response.data ? response.data : response

    if (data && data.token) {
      toast.close()
      roleStore.clear()
      roleStore.setAuth({
        token: data.token,
        userId: data.sysUser?.userId || '1',
        userName: data.sysUser?.realName || data.sysUser?.username || '用户'
      })

      // 成功登录后，获取绑定的家庭组与健康档案
      let roles: ('chef' | 'diner')[] = []
      let groups: { id: number; name: string }[] = []
      let defaultGroupId = 1
      let defaultProfileId = 0

      try {
        const profileRes: any = await request.get('/api/diet/user-health-profile/my-profile')
        const profile = profileRes && profileRes.data ? profileRes.data : profileRes
        if (profile) {
          defaultGroupId = profile.groupId || 1
          defaultProfileId = profile.profileId || 0
          // 1为管理员/大厨，大厨同时也是就餐人
          if (profile.group_role === 1) {
            roles = ['chef', 'diner']
          } else {
            roles = ['diner']
          }
          groups = [{id: defaultGroupId, name: profile.groupName || '我的家庭组'}]
        } else {
          roles = [role]
          groups = [{id: 1, name: '默认家庭组'}]
        }
      } catch (err) {
        console.error('获取关联健康档案失败，使用默认配置', err)
        roles = ['chef', 'diner']
        groups = [{id: 1, name: '健康快乐一家人'}, {id: 2, name: '活力四射健身组'}]
        defaultGroupId = 1
        defaultProfileId = 1
      }

      if (!roles.includes(role)) {
        roles.push(role)
      }

      roleStore.setRolesAndGroups(roles, groups)
      roleStore.setRole(role)
      roleStore.setGroupAndProfile(defaultGroupId, defaultProfileId)

      showSuccessToast(`登录成功：${roleStore.userName}`)
      if (role === 'chef') {
        router.push('/chef/home')
      } else {
        router.push('/diner/home')
      }
    } else {
      toast.close()
      showToast('登录失败，未返回Token')
    }
  } catch (error: any) {
    toast.close()
    console.error(error)
    showToast(error.message || '登录失败，请核对证件号')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="bg-decoration">
      <div class="circle c1"></div>
      <div class="circle c2"></div>
    </div>

    <div class="login-wrapper">
      <div class="app-logo">🥗</div>
      <h1 class="app-title">智慧饮食系统</h1>
      <p class="app-subtitle">多角色移动端就膳助手</p>

      <!-- 模式切换 Tab -->
      <div class="tabs-nav card-shadow">
        <button
            :class="['nav-item', { active: activeTab === 'tourist' }]"
            @click="activeTab = 'tourist'"
        >
          快速体验 (推荐)
        </button>
        <button
            :class="['nav-item', { active: activeTab === 'real' }]"
            @click="activeTab = 'real'"
        >
          本地身份证登录
        </button>
      </div>

      <!-- 快速体验内容 -->
      <div v-if="activeTab === 'tourist'" class="role-cards fade-in">
        <div class="role-card chef-card card-shadow" @click="handleGuestLogin('chef')">
          <div class="role-icon">👨‍🍳</div>
          <div class="role-info">
            <h2>我是掌勺厨师</h2>
            <p>管理家庭组、智能配餐推荐、菜市场清单、步骤烹饪向导</p>
          </div>
          <div class="enter-arrow">➔</div>
        </div>

        <div class="role-card diner-card card-shadow" @click="handleGuestLogin('diner')">
          <div class="role-icon">🍲</div>
          <div class="role-info">
            <h2>我是用餐成员</h2>
            <p>查看今日餐盘、分餐推荐、体重记录折线图、表达心愿单</p>
          </div>
          <div class="enter-arrow">➔</div>
        </div>
      </div>

      <!-- 身份证登录联调 -->
      <div v-else class="real-login-form card-shadow fade-in">
        <van-cell-group inset>
          <van-field
              v-model="idCardNum"
              label="身份证号"
              placeholder="请输入身份证号进行联调"
              left-icon="idcard"
              clearable
          />
        </van-cell-group>

        <div class="form-actions">
          <p class="role-guide-text">请选择您以此账号登入的角色：</p>
          <div class="btn-group">
            <van-button
                type="primary"
                icon="shop-o"
                block
                class="action-btn chef-btn"
                :disabled="loading"
                @click="handleRealLogin('chef')"
            >
              以厨师身份登录
            </van-button>
            <van-button
                type="success"
                icon="user-o"
                block
                class="action-btn diner-btn"
                :disabled="loading"
                @click="handleRealLogin('diner')"
            >
              以就餐成员登录
            </van-button>
          </div>
        </div>
      </div>
    </div>

    <div class="copyright">Smart Diet © 2026 智慧饮食服务</div>
  </div>
</template>

<style scoped>
.login-page {
  width: 100%;
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 40px 20px;
  background-color: #f7f8fa;
  overflow: hidden;
  box-sizing: border-box;
}

/* 渐变炫彩背景装饰球 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.c1 {
  top: -10%;
  left: -20%;
  width: 300px;
  height: 300px;
  background: rgba(46, 204, 113, 0.25);
}

.c2 {
  bottom: 10%;
  right: -20%;
  width: 250px;
  height: 250px;
  background: rgba(52, 152, 219, 0.2);
}

.login-wrapper {
  width: 100%;
  max-width: 380px;
  z-index: 1;
  text-align: center;
  margin-top: 20px;
}

.app-logo {
  font-size: 50px;
  margin-bottom: 10px;
  animation: bounce 2s infinite ease-in-out;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.app-title {
  font-size: 26px;
  font-weight: 800;
  color: #2c3e50;
  margin: 5px 0;
  letter-spacing: 1px;
}

.app-subtitle {
  font-size: 13px;
  color: #7f8c8d;
  margin-bottom: 30px;
}

/* Tab 切换导航 */
.tabs-nav {
  display: flex;
  background: white;
  padding: 4px;
  border-radius: 20px;
  margin-bottom: 25px;
}

.nav-item {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px 0;
  font-size: 13px;
  font-weight: 600;
  color: #7f8c8d;
  border-radius: 16px;
  transition: all 0.3s;
}

.nav-item.active {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
  color: white;
  box-shadow: 0 4px 10px rgba(46, 204, 113, 0.3);
}

/* 角色卡片列表 */
.role-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.role-card {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  text-align: left;
}

.role-card:active {
  transform: scale(0.98);
}

.chef-card:hover {
  border-left: 5px solid #2ecc71;
}

.diner-card:hover {
  border-left: 5px solid #3498db;
}

.role-icon {
  font-size: 34px;
  margin-right: 15px;
}

.role-info {
  flex: 1;
}

.role-info h2 {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.role-info p {
  font-size: 11px;
  color: #7f8c8d;
  line-height: 1.4;
}

.enter-arrow {
  font-size: 18px;
  color: #bdc3c7;
  margin-left: 10px;
}

/* 真实登录表单 */
.real-login-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px 10px;
  text-align: left;
}

.form-actions {
  padding: 0 16px;
  margin-top: 20px;
}

.role-guide-text {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 12px;
}

.btn-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  border-radius: 10px !important;
}

.copyright {
  font-size: 11px;
  color: #bdc3c7;
  z-index: 1;
  margin-top: 40px;
}

.fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
