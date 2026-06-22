<template>
  <div class="content-container section-gap">
    <div class="family-layout">
      <!-- 左侧：家庭分组管理面板 -->
      <div class="group-panel hairline-border">
        <div class="panel-header">
          <span class="eyebrow">FAMILY GROUPS</span>
          <h2 class="panel-title">我的家庭组</h2>
        </div>
        <button class="btn-primary add-group-btn" @click="groupModalVisible = true">
          + 新增家庭组
        </button>

        <div class="group-list">
          <div
              v-for="g in groups"
              :key="g.groupId"
              :class="['group-item', { active: activeGroupIdRef.value === g.groupId }]"
              @click="changeGroupId(g.groupId)"
          >
            <span class="group-name">🏠 {{ g.groupName }}</span>
            <button class="btn-delete-group" @click.stop="handleDeleteGroup(g.groupId)">✕</button>
          </div>
          <div v-if="groups.length === 0" class="empty-tip">
            暂无家庭组，请先新增。
          </div>
        </div>
      </div>

      <!-- 右侧：成员档案列表面板 -->
      <div class="members-panel">
        <div class="header-section">
          <div>
            <span class="eyebrow">FAMILY HEALTH ARCHIVE</span>
            <h1 class="display-lg">家庭成员档案</h1>
          </div>
          <button class="btn-primary" @click="handleOpenCreateModal">
            + 新增就餐人
          </button>
        </div>

        <!-- 成员大便签纸卡片列表 -->
        <div class="members-grid">
          <div
              v-for="(member, idx) in members"
              :key="member.profileId"
              :class="['color-block', 'member-card', getMemberCardColor(idx)]"
          >
            <div class="member-header">
              <div>
                <h3 class="card-title">{{ member.memberName }}</h3>
                <span class="caption tag-relation">
                  {{ member.memberRelation }} | {{ member.userId ? '在线用户' : '离线成员' }}
                </span>
              </div>
              <div class="member-actions">
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
        <div v-if="members.length === 0" class="empty-tip-members color-block">
          当前家庭组下暂无就餐人档案，点击右上角“新增就餐人”开始录入。
        </div>
      </div>
    </div>

    <!-- 档案编辑/新增测评弹窗 -->
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

    <!-- 新建家庭组 Dialog -->
    <el-dialog
        v-model="groupModalVisible"
        title="新建家庭组"
        width="400px"
    >
      <div class="member-form">
        <div class="form-item">
          <label class="caption">家庭组名称</label>
          <input type="text" v-model="groupForm.groupName" class="input-text" placeholder="例如: 我的小家庭、老李的大家庭"/>
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" style="margin-right: 10px" @click="groupModalVisible = false">取消</button>
        <button class="btn-primary" @click="handleSaveGroup">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {inject, onMounted, ref, unref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import request from '../utils/request'

const activeGroupIdRef = inject<any>('groupId')
const changeGroupId = inject<any>('changeGroupId')
const cookUserId = inject<number>('cookUserId', 1)

// 解包后的 groupId 值，每次组件重新挂载时自动重新 inject 读取最新值
const groupId = unref(activeGroupIdRef) || 1

const members = ref<any[]>([])
const groups = ref<any[]>([])
const modalVisible = ref(false)
const groupModalVisible = ref(false)
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
const groupForm = ref({
  groupName: ''
})

const loadGroups = async () => {
  try {
    const list = await request.get(`/api/group/list?userId=${cookUserId}`)
    groups.value = list
    // 如果本地存储没有选中的 activeGroupId，或者当前选中的 group 不在列表里，默认选第一个
    if (list.length > 0) {
      const activeExists = list.some((g: any) => g.groupId === activeGroupIdRef.value)
      if (!activeExists) {
        changeGroupId(list[0].groupId)
      }
    }
  } catch (e) {
  }
}

const loadMembers = async () => {
  try {
    members.value = await request.get(`/api/profile/list?groupId=${groupId}`)
  } catch (e) {
  }
}

const getMemberCardColor = (idx: number): string => {
  const colors = ['color-block-lime', 'color-block-lilac', 'color-block-cream', 'color-block-mint', 'color-block-pink', 'color-block-coral']
  return colors[idx % colors.length]
}

// 计算 BMI 辅助函数
const calculateBMI = (member: any): string => {
  if (!member.memberWeight || !member.memberHeight) return '0.0'
  const heightM = member.memberHeight / 100.0
  const bmi = member.memberWeight / (heightM * heightM)
  const val = Math.round(bmi * 10) / 10

  if (val < 18.5) return `${val} (偏瘦)`
  if (val >= 18.5 && val < 24.0) return `${val} (正常)`
  if (val >= 24.0 && val < 28.0) return `${val} (超重)`
  return `${val} (肥胖)`
}

const handleOpenCreateModal = () => {
  form.value = {...defaultForm, groupId: groupId}
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

// 新增家庭组
const handleSaveGroup = async () => {
  if (!groupForm.value.groupName.trim()) {
    ElMessage.warning('请输入家庭组名称！')
    return
  }
  try {
    const payload = {
      groupName: groupForm.value.groupName.trim(),
      creatorUserId: cookUserId
    }
    const res = await request.post('/api/group/save', payload)
    if (res) {
      ElMessage.success('新建家庭组成功！')
      groupModalVisible.value = false
      groupForm.value.groupName = ''
      await loadGroups()
    }
  } catch (e) {
  }
}

// 删除家庭组
const handleDeleteGroup = (targetGroupId: number) => {
  ElMessageBox.confirm(
      '确认删除该家庭组？该操作将软删除该分组。',
      '警告',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/group/delete/${targetGroupId}`)
      if (res) {
        ElMessage.success('家庭组已删除！')
        if (activeGroupIdRef.value === targetGroupId) {
          localStorage.removeItem('activeGroupId')
        }
        await loadGroups()
      }
    } catch (e) {
    }
  }).catch(() => {
  })
}

onMounted(() => {
  loadGroups()
  loadMembers()
})
</script>

<style scoped>
.family-layout {
  display: flex;
  gap: var(--spacing-lg);
  margin-top: var(--spacing-xl);
}

.group-panel {
  width: 260px;
  min-width: 260px;
  background-color: var(--surface-1);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  height: fit-content;
}

.panel-header {
  margin-bottom: var(--spacing-sm);
}

.panel-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
  margin-top: var(--spacing-xxs);
}

.add-group-btn {
  width: 100%;
  margin-bottom: var(--spacing-md);
  padding: 8px 14px;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-radius: var(--rounded-sm);
  background-color: var(--surface-2);
  cursor: pointer;
  transition: all 0.15s ease;
  border: 1px solid transparent;
}

.group-item:hover {
  background-color: var(--surface-3);
}

.group-item.active {
  background-color: var(--surface-3);
  border-color: var(--primary);
}

.group-item.active .group-name {
  color: var(--primary);
  font-weight: 600;
}

.group-name {
  font-size: 14px;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-delete-group {
  background: transparent;
  border: none;
  color: var(--ink-tertiary);
  cursor: pointer;
  font-size: 11px;
  padding: 4px;
  line-height: 1;
}

.btn-delete-group:hover {
  color: var(--primary);
}

.empty-tip {
  font-size: 12px;
  color: var(--ink-subtle);
  text-align: center;
  padding: var(--spacing-md) 0;
}

.members-panel {
  flex-grow: 1;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--spacing-lg);
}

.empty-tip-members {
  text-align: center;
  padding: var(--spacing-xxl) !important;
  color: var(--ink-subtle);
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
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  color: var(--primary-hover);
  padding: 2px 8px;
  border-radius: var(--rounded-xs);
  font-size: 10px;
  font-weight: 500;
  margin-top: var(--spacing-xxs);
}

.btn-icon-clear {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 6px;
  font-size: 18px;
  color: var(--ink-subtle);
  transition: color 0.15s ease;
}

.btn-icon-clear:hover {
  color: var(--ink);
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
  border-top: 1px dashed var(--hairline);
  padding-top: var(--spacing-sm);
  color: var(--ink-muted);
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
