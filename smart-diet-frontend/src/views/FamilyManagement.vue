<template>
  <div class="content-container section-gap">
    <div class="header-section" style="display: flex; justify-content: space-between; align-items: flex-end">
      <div>
        <span class="eyebrow">FAMILY HEALTH ARCHIVE</span>
        <h1 class="display-lg">家庭健康档案</h1>
      </div>
      <button class="btn-primary" @click="handleOpenCreateModal">
        + 新增就餐人
      </button>
    </div>

    <!-- 1. 成员大便签纸卡片列表 -->
    <div class="members-grid">
      <div
          v-for="(member, idx) in members"
          :key="member.profileId"
          :class="['color-block', 'member-card', getMemberCardColor(idx)]"
      >
        <div class="member-header">
          <div>
            <h3 class="card-title">{{ member.memberName }}</h3>
            <!-- 关系与在线/离线角色标识 -->
            <span class="caption tag-relation">
              {{ member.memberRelation }} | {{ member.userId ? '在线用户' : '离线成员' }}
            </span>
          </div>
          <div class="member-actions">
            <!-- 圆形编辑和删除按钮 -->
            <button class="btn-icon-clear" @click="handleEdit(member)">
              <el-icon>
                <Edit/>
              </el-icon>
            </button>
            <button class="btn-icon-clear" @click="handleDelete(member.profileId)">
              <el-icon>
                <Delete/>
              </el-icon>
            </button>
          </div>
        </div>

        <!-- 测评核心指标数据面板 -->
        <div class="metrics-grid">
          <div class="metric-item">
            <span class="caption">BMI 指标</span>
            <div class="metric-value">{{ calculateBMI(member) }}</div>
          </div>
          <div class="metric-item">
            <span class="caption">BMR 基础代谢</span>
            <div class="metric-value font-mono">{{ member.bmrCalories || 0 }} <span class="unit">kcal</span></div>
          </div>
          <div class="metric-item">
            <span class="caption">TDEE 日消耗</span>
            <div class="metric-value font-mono">{{ member.tdeeCalories || 0 }} <span class="unit">kcal</span></div>
          </div>
          <div class="metric-item">
            <span class="caption">单日热量目标</span>
            <div class="metric-value font-mono" style="color: var(--primary)">
              {{ member.dailyTargetCalories || 0 }} <span class="unit">kcal</span>
            </div>
          </div>
        </div>

        <!-- 身体测量基本参数 -->
        <div class="params-row body-sm">
          <span>身高: <strong>{{ member.memberHeight }} cm</strong></span>
          <span>体重: <strong>{{ member.memberWeight }} kg</strong></span>
          <span>年龄: <strong>{{ member.memberAge }} 岁</strong></span>
          <span>目标: <strong>{{ member.targetWeight }} kg</strong></span>
        </div>
      </div>
    </div>

    <!-- 2. 档案编辑/新增测评弹窗 (抽屉/Dialog，圆角与Monochrome输入框) -->
    <el-dialog
        v-model="modalVisible"
        :title="form.profileId ? '编辑健康档案测评' : '新增就餐成员测评'"
        width="560px"
    >
      <div class="member-form">
        <!-- 成员称呼 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">姓名/称呼</label>
            <input type="text" v-model="form.memberName" class="input-text" placeholder="例如: 爸爸、张三"/>
          </div>
          <div class="form-item">
            <label class="caption">与做饭人关系</label>
            <el-select v-model="form.memberRelation" placeholder="选择亲属关系" style="width: 100%">
              <el-option value="本人" label="本人"/>
              <el-option value="配偶" label="配偶"/>
              <el-option value="子女" label="子女"/>
              <el-option value="父母" label="父母"/>
              <el-option value="亲友" label="亲友"/>
            </el-select>
          </div>
        </div>

        <!-- 绑定系统账号 vs 离线无账号 -->
        <div class="form-item">
          <label class="caption">账号绑定状态</label>
          <el-select v-model="bindStatus" placeholder="选择就餐人是否为在线账号" style="width: 100%">
            <el-option :value="0" label="离线成员（免注册，仅做饭人维护其健康指标）"/>
            <el-option :value="1" label="在线用户（关联本人注册的系统账号）"/>
          </el-select>
        </div>

        <!-- 只有绑定账号时输入关联 ID -->
        <div class="form-item" v-if="bindStatus === 1">
          <label class="caption">系统注册用户 ID</label>
          <input type="number" v-model="form.userId" class="input-text" placeholder="输入关联的 sys_user.user_id"/>
        </div>

        <!-- 性别、年龄 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">成员性别</label>
            <el-select v-model="form.memberGender" placeholder="选择性别" style="width: 100%">
              <el-option :value="1" label="男"/>
              <el-option :value="2" label="女"/>
            </el-select>
          </div>
          <div class="form-item">
            <label class="caption">年龄 (周岁)</label>
            <input type="number" v-model="form.memberAge" class="input-text" placeholder="年龄"/>
          </div>
        </div>

        <!-- 身高、体重 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">身高 (cm)</label>
            <input type="number" v-model="form.memberHeight" class="input-text" step="0.1" placeholder="身高"/>
          </div>
          <div class="form-item">
            <label class="caption">体重 (kg)</label>
            <input type="number" v-model="form.memberWeight" class="input-text" step="0.1" placeholder="当前体重"/>
          </div>
        </div>

        <!-- 活动强度 & 减重参数 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">日常活动强度</label>
            <el-select v-model="form.activityLevel" placeholder="选择日常活动量" style="width: 100%">
              <el-option :value="1" label="久坐（办公室工作、少运动）"/>
              <el-option :value="2" label="轻度（日常零星散步或轻度活动）"/>
              <el-option :value="3" label="中度（每周规律运动3-5次）"/>
              <el-option :value="4" label="重度（高强度体力劳动或重度健身）"/>
            </el-select>
          </div>
          <div class="form-item">
            <label class="caption">目标体重 (kg)</label>
            <input type="number" v-model="form.targetWeight" class="input-text" step="0.1" placeholder="目标体重"/>
          </div>
        </div>

        <div class="form-item">
          <label class="caption">周科学减重速度 (kg/周)</label>
          <el-select v-model="form.dietSpeed" placeholder="选择建议减脂赤字" style="width: 100%">
            <el-option :value="0.00" label="0.00 kg/周 (维持当前体重 / 纯保养食谱)"/>
            <el-option :value="0.25" label="0.25 kg/周 (温和减脂 - 每日赤字 275 kcal)"/>
            <el-option :value="0.50" label="0.50 kg/周 (推荐速度 - 每日赤字 550 kcal)"/>
            <el-option :value="0.75" label="0.75 kg/周 (强效减脂 - 每日赤字 825 kcal)"/>
            <el-option :value="1.00" label="1.00 kg/周 (极限挑战 - 每日赤字 1100 kcal)"/>
          </el-select>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <button class="btn-secondary" style="margin-right: 10px" @click="modalVisible = false">取消</button>
          <button class="btn-primary" @click="handleSaveProfile">保存并自动测评</button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {inject, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import request from '../utils/request'

const groupId = inject<number>('groupId', 1)

const members = ref<any[]>([])
const modalVisible = ref(false)
const bindStatus = ref(0) // 默认离线

const defaultForm = {
  profileId: null as number | null,
  userId: null as number | null,
  groupId: groupId,
  groupRole: 2,
  memberName: '',
  memberRelation: '配偶',
  memberGender: 1,
  memberHeight: 170.0,
  memberWeight: 65.0,
  memberAge: 30,
  activityLevel: 2,
  targetWeight: 60.0,
  dietSpeed: 0.50
}
const form = ref({...defaultForm})

const loadMembers = async () => {
  try {
    members.value = await request.get(`/api/profile/list?groupId=${groupId}`)
  } catch (e) {
  }
}

const getMemberCardColor = (idx: number): string => {
  // 按顺序循环使用 lime, lilac, cream, mint, pink, coral 六种大贴纸色
  const colors = ['color-block-lime', 'color-block-lilac', 'color-block-cream', 'color-block-mint', 'color-block-pink', 'color-block-coral']
  return colors[idx % colors.length]
}

// 计算 BMI 辅助函数
const calculateBMI = (member: any): string => {
  if (!member.memberWeight || !member.memberHeight) return '0.0'
  const heightM = member.memberHeight / 100.0
  const bmi = member.memberWeight / (heightM * heightM)

  // 四舍五入保留一位小数
  const val = Math.round(bmi * 10) / 10

  // 加上分类字样
  if (val < 18.5) return `${val} (偏瘦)`
  if (val >= 18.5 && val < 24.0) return `${val} (正常)`
  if (val >= 24.0 && val < 28.0) return `${val} (超重)`
  return `${val} (肥胖)`
}

const handleOpenCreateModal = () => {
  form.value = {...defaultForm}
  bindStatus.value = 0
  modalVisible.value = true
}

const handleEdit = (member: any) => {
  form.value = {...member}
  bindStatus.value = member.userId ? 1 : 0
  modalVisible.value = true
}

const handleSaveProfile = async () => {
  if (!form.value.memberName) {
    ElMessage.warning('请输入就餐人姓名或称呼！')
    return
  }

  // 根据绑定状态决定 userId 值
  if (bindStatus.value === 0) {
    form.value.userId = null
  }

  try {
    const res = await request.post('/api/profile/save', form.value)
    if (res) {
      ElMessage.success('保存成功，体测评估指标已重算！')
      modalVisible.value = false
      loadMembers()
    }
  } catch (e) {
  }
}

const handleDelete = (profileId: number) => {
  ElMessageBox.confirm(
      '确认将该就餐人移出家庭组？其健康指标和体重历史档案将被假删除。',
      '警告',
      {
        confirmButtonText: '确认移出',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/profile/delete/${profileId}`)
      if (res) {
        ElMessage.success('就餐人移出家庭组成功！')
        loadMembers()
      }
    } catch (e) {
    }
  }).catch(() => {
  })
}

onMounted(() => {
  loadMembers()
})
</script>

<style scoped>
.header-section {
  margin-top: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: var(--spacing-lg);
}

.member-card {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.tag-relation {
  display: inline-block;
  background-color: var(--primary);
  color: var(--on-primary);
  padding: 2px 8px;
  border-radius: var(--rounded-pill);
  font-size: 10px;
  font-weight: 700;
  margin-top: var(--spacing-xxs);
}

.btn-icon-clear {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 6px;
  font-size: 18px;
  color: var(--ink);
  transition: opacity 0.1s ease;
}

.btn-icon-clear:hover {
  opacity: 0.6;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-sm);
  margin: var(--spacing-md) 0;
}

.metric-item {
  display: flex;
  flex-direction: column;
  border-left: 2px solid var(--primary);
  padding-left: var(--spacing-xs);
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.metric-value .unit {
  font-size: 11px;
  font-weight: 400;
}

.params-row {
  display: flex;
  justify-content: space-between;
  border-top: 1px dashed rgba(0, 0, 0, 0.15);
  padding-top: var(--spacing-sm);
}

.member-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xxs);
}
</style>
