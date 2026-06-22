<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">PERSONAL HEALTH ARCHIVE</span>
      <h1 class="display-lg">个人健康档案</h1>
    </div>

    <!-- 骨架屏 / 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p class="body-sm text-muted">正在加载个人健康指标...</p>
    </div>

    <!-- 主容器 -->
    <div v-else class="personal-layout-grid">

      <!-- 左半部分：核心体测评估大盘 -->
      <div class="dashboard-panel">
        <div v-if="profile" class="profile-dashboard-card color-block color-block-lilac">
          <div class="card-header-row">
            <div>
              <span class="eyebrow">HEALTH REPORT</span>
              <h2 class="card-title">{{ profile.memberName }} 的健康报告</h2>
            </div>
            <span class="tag-relation">本人 | 账号已绑定</span>
          </div>

          <!-- 四大核心指标大字报 -->
          <div class="profile-metrics-grid">
            <div class="metric-card-item">
              <span class="caption">BMI 体质指数</span>
              <div class="metric-value font-mono">{{ bmiVal }}</div>
              <span :class="['bmi-status-tag', bmiClass]">{{ bmiText }}</span>
            </div>

            <div class="metric-card-item">
              <span class="caption">BMR 基础代谢</span>
              <div class="metric-value font-mono">{{ profile.bmrCalories || 0 }} <span class="unit">kcal</span></div>
              <span class="caption-desc">维持生命所需的最低热量</span>
            </div>

            <div class="metric-card-item">
              <span class="caption">TDEE 每日总消耗</span>
              <div class="metric-value font-mono">{{ profile.tdeeCalories || 0 }} <span class="unit">kcal</span></div>
              <span class="caption-desc">结合日常活动强度的实际消耗</span>
            </div>

            <div class="metric-card-item">
              <span class="caption">单日推荐摄入目标</span>
              <div class="metric-value font-mono highlight-color">{{ profile.dailyTargetCalories || 0 }} <span class="unit">kcal</span></div>
              <span class="caption-desc">根据减重速度计算的预算热量</span>
            </div>
          </div>

          <!-- 身体指标概览 -->
          <div class="physical-params-summary hairline-border">
            <div class="summary-item">
              <span class="label">性别</span>
              <strong class="val">{{ profile.memberGender === 1 ? '男' : '女' }}</strong>
            </div>
            <div class="summary-item">
              <span class="label">年龄</span>
              <strong class="val">{{ profile.memberAge }} 岁</strong>
            </div>
            <div class="summary-item">
              <span class="label">当前身高</span>
              <strong class="val">{{ profile.memberHeight }} cm</strong>
            </div>
            <div class="summary-item">
              <span class="label">当前体重</span>
              <strong class="val">{{ profile.memberWeight }} kg</strong>
            </div>
            <div class="summary-item">
              <span class="label">目标体重</span>
              <strong class="val">{{ profile.targetWeight }} kg</strong>
            </div>
          </div>
        </div>

        <!-- 暂无个人档案警告 -->
        <div v-else class="empty-profile-card color-block color-block-pink">
          <h2 class="headline">未检测到您的个人档案</h2>
          <p class="body-text text-muted" style="margin-bottom: 20px;">
            您当前还没有在当前家庭组下录入您的个人健康档案。请点击右侧表单录入您的身高、体重等核心身体指标，以便系统为您进行科学的膳食摄入评估。
          </p>
        </div>

        <!-- 科学指南贴纸 -->
        <div class="science-guide-card color-block color-block-cream">
          <span class="eyebrow">DIET SCIENCE</span>
          <h3 class="card-title">💡 科学健康减重贴士</h3>
          <ul class="guide-list body-sm">
            <li><strong>合理热量赤字：</strong>推荐周减重 0.5kg（对应每日热量赤字约 550 kcal），这是最温和且不易反弹的减脂速度。</li>
            <li><strong>最低热量防线：</strong>系统已自动开启安全保护机制，您的每日目标摄入量绝不低于 BMR 的 90% 或 1000 kcal，以此避免基础代谢受损。</li>
            <li><strong>精准运动加成：</strong>如果您的运动量有所增加，建议在右侧表单调整“日常活动强度”，系统将自动为您重算 TDEE 并更新配餐比例。</li>
          </ul>
        </div>
      </div>

      <!-- 右半部分：档案信息配置与修改 -->
      <div class="edit-form-panel color-block">
        <div class="panel-header">
          <span class="eyebrow">UPDATE ARCHIVE</span>
          <h2 class="panel-title">修改身体参数</h2>
          <p class="caption-desc">请保持数据的真实性，以便智能配餐算法准确计算每日热量比例。</p>
        </div>

        <div class="archive-edit-form">
          <!-- 姓名与关系 -->
          <div class="form-row-2">
            <div class="form-item">
              <label class="caption">姓名/称呼</label>
              <input type="text" v-model="form.memberName" class="input-text" placeholder="例如: 张大厨"/>
            </div>
            <div class="form-item">
              <label class="caption">与做饭人关系</label>
              <input type="text" value="本人 (主账号)" class="input-text disabled-input" disabled/>
            </div>
          </div>

          <!-- 性别与年龄 -->
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
              <input type="number" v-model="form.memberAge" class="input-text" placeholder="输入年龄"/>
            </div>
          </div>

          <!-- 身高与体重 -->
          <div class="form-row-2">
            <div class="form-item">
              <label class="caption">身高 (cm)</label>
              <input type="number" v-model="form.memberHeight" class="input-text" step="0.1" placeholder="输入身高"/>
            </div>
            <div class="form-item">
              <label class="caption">体重 (kg)</label>
              <input type="number" v-model="form.memberWeight" class="input-text" step="0.1" placeholder="输入当前实际体重"/>
            </div>
          </div>

          <!-- 目标体重与日常活动强度 -->
          <div class="form-row-2">
            <div class="form-item">
              <label class="caption">目标体重 (kg)</label>
              <input type="number" v-model="form.targetWeight" class="input-text" step="0.1" placeholder="输入期望减重目标"/>
            </div>
            <div class="form-item">
              <label class="caption">日常活动强度</label>
              <el-select v-model="form.activityLevel" placeholder="选择日常活动量" style="width: 100%">
                <el-option :value="1" label="久坐（办公室工作、少运动）"/>
                <el-option :value="2" label="轻度（日常零星散步或轻度活动）"/>
                <el-option :value="3" label="中度（每周规律运动3-5次）"/>
                <el-option :value="4" label="重度（高强度体力劳动或重度健身）"/>
              </el-select>
            </div>
          </div>

          <!-- 减重速度选择 -->
          <div class="form-item">
            <label class="caption">周科学减重速度 (kg/周)</label>
            <el-select v-model="form.dietSpeed" placeholder="选择每日减脂赤字速度" style="width: 100%">
              <el-option :value="0.00" label="0.00 kg/周 (维持当前体重 / 纯保养食谱)"/>
              <el-option :value="0.25" label="0.25 kg/周 (温和减脂 - 每日赤字 275 kcal)"/>
              <el-option :value="0.50" label="0.50 kg/周 (推荐速度 - 每日赤字 550 kcal)"/>
              <el-option :value="0.75" label="0.75 kg/周 (强效减脂 - 每日赤字 825 kcal)"/>
              <el-option :value="1.00" label="1.00 kg/周 (极限挑战 - 每日赤字 1100 kcal)"/>
            </el-select>
          </div>

          <!-- 保存按钮 -->
          <div class="form-actions" style="margin-top: 10px;">
            <button class="btn-primary btn-save" @click="handleSaveProfile">
              保存并自动测评
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, inject, onMounted, ref, unref} from 'vue'
import {ElMessage} from 'element-plus'
import request from '../utils/request'

const activeGroupIdRef = inject<any>('groupId')
const groupId = unref(activeGroupIdRef) || 1
const cookUserId = inject<number>('cookUserId', 1)

const loading = ref(true)
const profile = ref<any>(null)

const defaultForm = {
  profileId: null as number | null,
  userId: null as number | null,
  groupId: groupId,
  groupRole: 1, // 1-做饭人/管理员
  memberName: '',
  memberRelation: '本人',
  memberGender: 1,
  memberHeight: 170.0,
  memberWeight: 65.0,
  memberAge: 30,
  activityLevel: 2,
  targetWeight: 60.0,
  dietSpeed: 0.50
}

const form = ref({...defaultForm})

// BMI 核心指标辅助计算
const bmiVal = computed(() => {
  if (!profile.value || !profile.value.memberWeight || !profile.value.memberHeight) return '0.0'
  const heightM = profile.value.memberHeight / 100.0
  const bmi = profile.value.memberWeight / (heightM * heightM)
  return (Math.round(bmi * 10) / 10).toFixed(1)
})

const bmiText = computed(() => {
  const val = parseFloat(bmiVal.value)
  if (val === 0) return '无数据'
  if (val < 18.5) return '偏瘦'
  if (val >= 18.5 && val < 24.0) return '正常'
  if (val >= 24.0 && val < 28.0) return '超重'
  return '肥胖'
})

const bmiClass = computed(() => {
  const val = parseFloat(bmiVal.value)
  if (val < 18.5) return 'bmi-under'
  if (val >= 18.5 && val < 24.0) return 'bmi-normal'
  if (val >= 24.0 && val < 28.0) return 'bmi-over'
  return 'bmi-obese'
})

// 读取个人健康档案信息
const loadPersonalProfile = async () => {
  loading.value = true
  try {
    const list: any[] = await request.get(`/api/profile/list?groupId=${groupId}`)
    const currentUserId = Number(localStorage.getItem('userId')) || cookUserId

    // 优先匹配 userId 等于当前用户的档案，其次匹配 memberRelation === '本人'
    const found = list.find((m: any) => m.userId === currentUserId) || list.find((m: any) => m.memberRelation === '本人')

    if (found) {
      profile.value = found
      form.value = {...found}
    } else {
      profile.value = null
      // 初始化空表单数据
      form.value = {
        ...defaultForm,
        groupId: groupId,
        userId: currentUserId,
        memberName: localStorage.getItem('realName') || localStorage.getItem('username') || '本人'
      }
    }
  } catch (e) {
    console.error('获取个人健康档案失败', e)
  } finally {
    loading.value = false
  }
}

// 保存个人档案并重算体测
const handleSaveProfile = async () => {
  if (!form.value.memberName) {
    ElMessage.warning('请输入姓名或称呼！')
    return
  }
  if (!form.value.memberAge || form.value.memberAge <= 0) {
    ElMessage.warning('请输入合法的年龄！')
    return
  }
  if (!form.value.memberHeight || form.value.memberHeight <= 0) {
    ElMessage.warning('请输入合法的身高！')
    return
  }
  if (!form.value.memberWeight || form.value.memberWeight <= 0) {
    ElMessage.warning('请输入合法的体重！')
    return
  }
  if (!form.value.targetWeight || form.value.targetWeight <= 0) {
    ElMessage.warning('请输入合理的目标体重！')
    return
  }

  try {
    const res = await request.post('/api/profile/save', form.value)
    if (res) {
      ElMessage.success('保存成功，个人健康体测已重新计算！')
      await loadPersonalProfile()
    }
  } catch (e) {
    console.error('保存个人档案失败', e)
  }
}

onMounted(() => {
  loadPersonalProfile()
})
</script>

<style scoped>
.personal-layout-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: var(--spacing-lg);
  margin-top: var(--spacing-xl);
}

.dashboard-panel {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.profile-dashboard-card {
  min-height: 380px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.tag-relation {
  display: inline-block;
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  color: var(--primary);
  padding: 4px 10px;
  border-radius: var(--rounded-xs);
  font-size: 11px;
  font-weight: 600;
}

/* 核心四大指标 */
.profile-metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
  margin: var(--spacing-md) 0 var(--spacing-lg) 0;
}

.metric-card-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-md);
  min-height: 90px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin: var(--spacing-xxs) 0;
}

.metric-value.highlight-color {
  color: var(--primary);
}

.metric-value .unit {
  font-size: 12px;
  font-weight: 400;
  color: var(--ink-subtle);
}

.caption-desc {
  font-size: 11px;
  color: var(--ink-tertiary);
  line-height: 1.3;
}

/* BMI 标签颜色分类 */
.bmi-status-tag {
  display: inline-block;
  align-self: flex-start;
  padding: 2px 8px;
  border-radius: var(--rounded-xs);
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
}

.bmi-under {
  background-color: #e0f2fe;
  color: #0369a1;
}

.bmi-normal {
  background-color: #dcfce7;
  color: #15803d;
}

.bmi-over {
  background-color: #fef3c7;
  color: #b45309;
}

.bmi-obese {
  background-color: #fee2e2;
  color: #b91c1c;
}

/* 身体概览排列表 */
.physical-params-summary {
  display: flex;
  justify-content: space-between;
  background-color: var(--surface-1);
  padding: var(--spacing-md);
  border-radius: var(--rounded-md);
  margin-top: auto;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.summary-item:not(:last-child) {
  border-right: 1px dashed var(--hairline);
}

.summary-item .label {
  font-size: 11px;
  color: var(--ink-tertiary);
  margin-bottom: 2px;
}

.summary-item .val {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
}

/* 科学指南列表 */
.science-guide-card .guide-list {
  margin-top: var(--spacing-sm);
  padding-left: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  color: var(--ink-muted);
}

.science-guide-card .guide-list li {
  line-height: 1.6;
}

/* 右半部分：表单配置 */
.edit-form-panel {
  display: flex;
  flex-direction: column;
  height: fit-content;
}

.edit-form-panel .panel-header {
  margin-bottom: var(--spacing-lg);
}

.edit-form-panel .panel-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink);
  margin-top: 4px;
}

.archive-edit-form {
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

.disabled-input {
  background-color: var(--surface-3) !important;
  color: var(--ink-subtle) !important;
  cursor: not-allowed;
}

.btn-save {
  width: 100%;
  padding: 12px;
  font-size: 15px;
  font-weight: 600;
}

/* 空白提示与加载状态 */
.empty-profile-card {
  min-height: 200px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xxl) 0;
  gap: var(--spacing-md);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--hairline);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
