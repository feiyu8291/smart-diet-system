<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from 'vue'
import {useRoleStore} from '../../store/role'
import request from '../../utils/request'
import {showSuccessToast, showToast} from 'vant'

const roleStore = useRoleStore()
const loading = ref(false)

// 用餐人健康状态
const profile = ref({
  memberName: '就餐成员',
  memberGender: 1,
  memberHeight: 172,
  memberWeight: 68.5,
  memberBirthday: '2001-05-18',
  activityLevel: 2,
  targetWeight: 62.0,
  bmrCalories: 1540,
  tdeeCalories: 1850,
  dailyTargetCalories: 1500,
  idCardNum: ''
})

// 体重记录列表
const weightHistory = ref<any[]>([
  {date: '06-19', weight: 70.2},
  {date: '06-20', weight: 69.8},
  {date: '06-21', weight: 69.5},
  {date: '06-22', weight: 69.0},
  {date: '06-23', weight: 68.8},
  {date: '06-24', weight: 68.5}
])

// 新增体重打卡控制
const showAddWeight = ref(false)
const newWeight = ref(68.5)

// 编辑健康档案 Popup 控制
const showEditProfile = ref(false)
const showDatePicker = ref(false)
const birthDateValue = ref<string[]>([])
const minDate = new Date(1940, 0, 1)
const maxDate = new Date()

const editForm = ref({
  memberName: '',
  idCardNum: '',
  memberBirthday: '',
  memberGender: 1,
  memberHeight: 172,
  memberWeight: 68.5,
  targetWeight: 62.0,
  activityLevel: 2
})

const activityOptions = [
  {text: '久坐不动', value: 1},
  {text: '轻度活动', value: 2},
  {text: '中度活动', value: 3},
  {text: '重度活动', value: 4}
]

// 计算 BMI
const bmi = ref(23.2)
const bmiStatus = ref('正常')
const bmiColor = ref('#2ecc71')

const calculateBmiInfo = () => {
  const h = profile.value.memberHeight / 100
  const w = profile.value.memberWeight
  const b = Number((w / (h * h)).toFixed(1))
  bmi.value = b
  if (b < 18.5) {
    bmiStatus.value = '偏瘦'
    bmiColor.value = '#3498db'
  } else if (b < 24) {
    bmiStatus.value = '正常'
    bmiColor.value = '#2ecc71'
  } else if (b < 28) {
    bmiStatus.value = '偏胖'
    bmiColor.value = '#f39c12'
  } else {
    bmiStatus.value = '肥胖'
    bmiColor.value = '#e74c3c'
  }
}

// 身份证输入侦听自动提取生日与性别
const handleIdCardInput = (val: string) => {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
  if (val.length === 18 && reg.test(val)) {
    const year = val.substring(6, 10)
    const month = val.substring(10, 12)
    const day = val.substring(12, 14)
    editForm.value.memberBirthday = `${year}-${month}-${day}`

    // 性别：第 17 位奇数男，偶数女
    const genderDigit = Number(val.substring(16, 17))
    editForm.value.memberGender = (genderDigit % 2 === 1) ? 1 : 2
    showToast('已根据身份证识别并反填生日与性别')
  }
}

// 日期确认
const onConfirmDate = ({selectedValues}: any) => {
  editForm.value.memberBirthday = selectedValues.join('-')
  showDatePicker.value = false
}

// 计算年龄（周岁）
const calculateAge = (birthdayStr: string) => {
  if (!birthdayStr) return 0
  const birthDate = new Date(birthdayStr)
  if (isNaN(birthDate.getTime())) return 0
  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const m = today.getMonth() - birthDate.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age >= 0 ? age : 0
}

const currentAge = computed(() => {
  return calculateAge(profile.value.memberBirthday)
})

// 卡路里重新核算 (Mock状态下重新合算BMR/TDEE)
const calculateCaloriesMock = (p: any) => {
  const age = calculateAge(p.memberBirthday)
  let bmr = 0
  if (p.memberGender === 1) {
    bmr = 66.47 + 13.75 * p.memberWeight + 5.0 * p.memberHeight - 6.75 * age
  } else {
    bmr = 655.1 + 9.56 * p.memberWeight + 1.85 * p.memberHeight - 4.68 * age
  }

  const multipliers = [1.2, 1.375, 1.55, 1.725]
  const mult = multipliers[p.activityLevel - 1] || 1.375
  const tdee = bmr * mult
  const target = tdee - 500

  p.bmrCalories = Math.round(bmr)
  p.tdeeCalories = Math.round(tdee)
  p.dailyTargetCalories = Math.round(target)
}

const openEditProfile = () => {
  editForm.value = {
    memberName: profile.value.memberName,
    idCardNum: profile.value.idCardNum || '',
    memberBirthday: profile.value.memberBirthday,
    memberGender: profile.value.memberGender,
    memberHeight: profile.value.memberHeight,
    memberWeight: profile.value.memberWeight,
    targetWeight: profile.value.targetWeight,
    activityLevel: profile.value.activityLevel
  }
  if (profile.value.memberBirthday) {
    birthDateValue.value = profile.value.memberBirthday.split('-')
  }
  showEditProfile.value = true
}

const saveProfile = async () => {
  if (!editForm.value.memberName) {
    showToast('就餐人姓名不能为空')
    return
  }
  if (!editForm.value.memberBirthday) {
    showToast('出生日期不能为空')
    return
  }

  try {
    if (!roleStore.token?.startsWith('mock-')) {
      await request.post('/api/diet/user-health-profile/save', {
        profileId: roleStore.profileId,
        groupId: roleStore.groupId,
        memberName: editForm.value.memberName,
        idCardNum: editForm.value.idCardNum,
        memberBirthday: editForm.value.memberBirthday,
        memberGender: editForm.value.memberGender,
        memberHeight: editForm.value.memberHeight,
        memberWeight: editForm.value.memberWeight,
        targetWeight: editForm.value.targetWeight,
        activityLevel: editForm.value.activityLevel
      })
    }

    Object.assign(profile.value, editForm.value)
    calculateCaloriesMock(profile.value)
    calculateBmiInfo()
    drawWeightChart()
    showEditProfile.value = false
    showSuccessToast('健康档案已更新！')
  } catch (err: any) {
    console.error(err)
    showToast(err.message || '保存档案失败')
  }
}

// Canvas 平滑折线图绘制
const trendCanvas = ref<HTMLCanvasElement | null>(null)
const drawWeightChart = () => {
  const canvas = trendCanvas.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  const width = canvas.clientWidth
  const height = canvas.clientHeight
  canvas.width = width * dpr
  canvas.height = height * dpr
  ctx.scale(dpr, dpr)

  ctx.clearRect(0, 0, width, height)
  const data = weightHistory.value
  if (data.length === 0) return

  const weights = data.map(d => d.weight)
  const maxWeight = Math.max(...weights) + 1
  const minWeight = Math.min(...weights) - 1
  const range = maxWeight - minWeight

  const paddingLeft = 30
  const paddingRight = 20
  const paddingTop = 30
  const paddingBottom = 25
  const chartWidth = width - paddingLeft - paddingRight
  const chartHeight = height - paddingTop - paddingBottom

  const points = data.map((item, idx) => {
    const x = paddingLeft + (idx / (data.length - 1)) * chartWidth
    const y = paddingTop + chartHeight - ((item.weight - minWeight) / (range || 1)) * chartHeight
    return {x, y, weight: item.weight, date: item.date}
  })

  // 1. 绘制网格
  ctx.strokeStyle = '#f1f2f6'
  ctx.lineWidth = 1
  for (let i = 0; i <= 3; i++) {
    const y = paddingTop + (i / 3) * chartHeight
    ctx.beginPath()
    ctx.moveTo(paddingLeft, y)
    ctx.lineTo(width - paddingRight, y)
    ctx.stroke()
  }

  // 2. 绘制平滑贝塞尔曲线
  ctx.beginPath()
  ctx.strokeStyle = '#2ecc71'
  ctx.lineWidth = 3
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'

  if (points.length > 0) {
    ctx.moveTo(points[0].x, points[0].y)
    for (let i = 0; i < points.length - 1; i++) {
      const xc = (points[i].x + points[i + 1].x) / 2
      const yc = (points[i].y + points[i + 1].y) / 2
      ctx.quadraticCurveTo(points[i].x, points[i].y, xc, yc)
    }
    ctx.lineTo(points[points.length - 1].x, points[points.length - 1].y)
    ctx.stroke()
  }

  // 3. 填充渐变
  if (points.length > 0) {
    ctx.beginPath()
    ctx.moveTo(points[0].x, paddingTop + chartHeight)
    ctx.lineTo(points[0].x, points[0].y)
    for (let i = 0; i < points.length - 1; i++) {
      const xc = (points[i].x + points[i + 1].x) / 2
      const yc = (points[i].y + points[i + 1].y) / 2
      ctx.quadraticCurveTo(points[i].x, points[i].y, xc, yc)
    }
    ctx.lineTo(points[points.length - 1].x, points[points.length - 1].y)
    ctx.lineTo(points[points.length - 1].x, paddingTop + chartHeight)
    ctx.closePath()

    const fillGradient = ctx.createLinearGradient(0, paddingTop, 0, paddingTop + chartHeight)
    fillGradient.addColorStop(0, 'rgba(46, 204, 113, 0.2)')
    fillGradient.addColorStop(1, 'rgba(46, 204, 113, 0.0)')
    ctx.fillStyle = fillGradient
    ctx.fill()
  }

  // 4. 节点圈与文字
  points.forEach((pt) => {
    ctx.beginPath()
    ctx.arc(pt.x, pt.y, 6, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(46, 204, 113, 0.3)'
    ctx.fill()

    ctx.beginPath()
    ctx.arc(pt.x, pt.y, 4, 0, Math.PI * 2)
    ctx.fillStyle = '#ffffff'
    ctx.fill()
    ctx.strokeStyle = '#2ecc71'
    ctx.lineWidth = 2
    ctx.stroke()

    ctx.fillStyle = '#2c3e50'
    ctx.font = '10px -apple-system'
    ctx.textAlign = 'center'
    ctx.fillText(`${pt.weight}kg`, pt.x, pt.y - 10)

    ctx.fillStyle = '#95a5a6'
    ctx.fillText(pt.date, pt.x, paddingTop + chartHeight + 15)
  })
}

// 模拟加载真实数据
const fetchHealthData = async () => {
  if (roleStore.token?.startsWith('mock-')) {
    calculateBmiInfo()
    nextTick(() => {
      drawWeightChart()
    })
    return
  }

  loading.value = true
  try {
    const profileRes: any = await request.get(`/api/diet/user-health-profile/${roleStore.profileId}`)
    const p = profileRes && profileRes.data ? profileRes.data : profileRes
    if (p) {
      profile.value = p
      newWeight.value = Number(p.memberWeight)
    }

    const weightRes: any = await request.get(`/api/diet/weight-record/list?profileId=${roleStore.profileId}`)
    const list = weightRes && weightRes.data ? weightRes.data : weightRes
    if (list && list.length > 0) {
      const formatted = list.slice(-7).map((w: any) => {
        const datePart = w.recordDate ? w.recordDate.substring(5) : '未知'
        return {date: datePart, weight: Number(w.recordWeight)}
      })
      weightHistory.value = formatted
    }

    calculateBmiInfo()
    nextTick(() => {
      drawWeightChart()
    })
  } catch (err) {
    console.error('获取健康数据失败，使用内置 mock 预览', err)
    calculateBmiInfo()
    nextTick(() => {
      drawWeightChart()
    })
  } finally {
    loading.value = false
  }
}

// 提交体重
const submitWeight = async () => {
  try {
    const todayStr = new Date().toISOString().split('T')[0]

    if (roleStore.token?.startsWith('mock-')) {
      const todayShort = todayStr.substring(5)
      const existing = weightHistory.value.find(w => w.date === todayShort)
      if (existing) {
        existing.weight = newWeight.value
      } else {
        weightHistory.value.push({date: todayShort, weight: newWeight.value})
      }
      profile.value.memberWeight = newWeight.value
      showSuccessToast('打卡成功')
    } else {
      await request.post('/api/diet/weight-record/add', {
        profileId: roleStore.profileId,
        recordWeight: newWeight.value,
        recordDate: todayStr
      })
      showSuccessToast('体重打卡成功')
      fetchHealthData()
    }

    calculateBmiInfo()
    drawWeightChart()
    showAddWeight.value = false
  } catch (err) {
    console.error(err)
  }
}

// 监听家庭组/档案切换以拉取新健康数据
watch(
    () => [roleStore.groupId, roleStore.profileId],
    () => {
      fetchHealthData()
    }
)

onMounted(() => {
  fetchHealthData()
})
</script>

<template>
  <div class="diner-health">
    <!-- BMI健康评定卡片 -->
    <div class="health-card card-shadow">
      <div class="header-info">
        <div>
          <div class="name-edit-row">
            <h2>{{ profile.memberName }}</h2>
            <van-icon name="edit" size="14" class="edit-prof-icon" @click="openEditProfile"/>
          </div>
          <p>生日: {{ profile.memberBirthday }} ({{ currentAge }} 岁) | 身高: {{ profile.memberHeight }} cm</p>
        </div>
        <div class="bmi-badge" :style="{ backgroundColor: bmiColor }">
          <span class="bmi-title">BMI {{ bmi }}</span>
          <span class="bmi-desc">{{ bmiStatus }}</span>
        </div>
      </div>

      <div class="divider"></div>

      <!-- 健康指标网格 -->
      <div class="metrics-grid">
        <div class="metric-item">
          <span class="m-label">当前实际体重</span>
          <span class="m-value">{{ profile.memberWeight }} <small>kg</small></span>
        </div>
        <div class="metric-item border-l">
          <span class="m-label">目标体重</span>
          <span class="m-value target">{{ profile.targetWeight }} <small>kg</small></span>
        </div>
        <div class="metric-item border-l">
          <span class="m-label">基础代谢 (BMR)</span>
          <span class="m-value">{{ Math.round(profile.bmrCalories) }} <small>kcal</small></span>
        </div>
      </div>
    </div>

    <!-- 体重趋势折线图卡片 -->
    <div class="chart-card card-shadow mt-16">
      <div class="card-header">
        <h3>📈 体重变化趋势</h3>
        <van-button
            type="primary"
            size="small"
            round
            plain
            icon="edit"
            @click="showAddWeight = true"
        >
          体重打卡
        </van-button>
      </div>

      <div class="canvas-wrapper">
        <canvas ref="trendCanvas" class="trend-canvas"></canvas>
      </div>
    </div>

    <!-- 每日配额 -->
    <div class="diet-plan-card card-shadow mt-16">
      <h3>🥗 每日减重热量配额</h3>
      <div class="plan-details">
        <div class="row">
          <span>总消耗能 (TDEE)</span>
          <span class="highlight-val">{{ Math.round(profile.tdeeCalories) }} kcal</span>
        </div>
        <div class="row">
          <span>推荐缺口配额</span>
          <span class="highlight-val color-green">-{{ Math.round(profile.tdeeCalories - profile.dailyTargetCalories) }} kcal</span>
        </div>
        <div class="row border-t">
          <span>每日饮食限额</span>
          <span class="target-val">{{ Math.round(profile.dailyTargetCalories) }} kcal</span>
        </div>
      </div>
    </div>

    <!-- 体重打卡弹窗 -->
    <van-dialog
        v-model:show="showAddWeight"
        title="记录今日体重"
        show-cancel-button
        @confirm="submitWeight"
    >
      <div class="dialog-body">
        <div class="slider-val">{{ newWeight }} kg</div>
        <div class="slider-container">
          <van-slider
              v-model="newWeight"
              :min="40"
              :max="120"
              :step="0.1"
              active-color="#2ecc71"
          />
        </div>
        <div class="slider-range">
          <span>40kg</span>
          <span>80kg</span>
          <span>120kg</span>
        </div>
      </div>
    </van-dialog>

    <!-- 档案修改弹窗 -->
    <van-popup
        v-model:show="showEditProfile"
        position="bottom"
        round
        :style="{ height: '80%' }"
        safe-area-inset-bottom
    >
      <div class="edit-profile-form">
        <h3 class="form-title">更新个人健康档案</h3>

        <van-cell-group inset>
          <van-field
              v-model="editForm.memberName"
              label="成员姓名"
              placeholder="请输入姓名"
              required
          />

          <van-field
              v-model="editForm.idCardNum"
              label="身份证号"
              placeholder="输入18位身份证号自动识别"
              maxlength="18"
              @input="(e: any) => handleIdCardInput(e.target.value)"
          />

          <van-cell title="出生日期" required>
            <template #value>
              <span @click="showDatePicker = true" class="date-select-text">
                {{ editForm.memberBirthday || '选择出生日期' }}
              </span>
            </template>
          </van-cell>

          <van-cell title="性别" required>
            <template #value>
              <van-radio-group v-model="editForm.memberGender" direction="horizontal">
                <van-radio :name="1" checked-color="#2ecc71">男</van-radio>
                <van-radio :name="2" checked-color="#2ecc71">女</van-radio>
              </van-radio-group>
            </template>
          </van-cell>

          <van-field
              v-model.number="editForm.memberHeight"
              label="身高 (cm)"
              type="number"
              placeholder="请输入身高"
              required
          />

          <van-field
              v-model.number="editForm.memberWeight"
              label="实际体重 (kg)"
              type="number"
              placeholder="当前实际体重"
              required
          />

          <van-field
              v-model.number="editForm.targetWeight"
              label="目标体重 (kg)"
              type="number"
              placeholder="期望目标体重"
              required
          />

          <van-cell title="日常活动强度">
            <template #value>
              <van-dropdown-menu active-color="#2ecc71" class="custom-dropdown">
                <van-dropdown-item v-model="editForm.activityLevel" :options="activityOptions"/>
              </van-dropdown-menu>
            </template>
          </van-cell>
        </van-cell-group>

        <div class="form-submit-box">
          <van-button type="success" block round class="submit-profile-btn" @click="saveProfile">
            保存修改并重新核算
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 生日DatePicker -->
    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker
          v-model="birthDateValue"
          title="选择出生日期"
          :min-date="minDate"
          :max-date="maxDate"
          @confirm="onConfirmDate"
          @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.diner-health {
  padding-bottom: 24px;
}

.mt-16 {
  margin-top: 16px;
}

.health-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
}

.header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name-edit-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.name-edit-row h2 {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.edit-prof-icon {
  color: #2ecc71;
  padding: 4px;
}

.edit-prof-icon:active {
  transform: scale(0.9);
}

.header-info p {
  font-size: 11px;
  color: #7f8c8d;
  margin: 0;
}

.bmi-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 6px 10px;
  border-radius: 12px;
  min-width: 60px;
}

.bmi-title {
  font-size: 12px;
  font-weight: 700;
}

.bmi-desc {
  font-size: 9px;
  opacity: 0.9;
}

.divider {
  height: 1px;
  background-color: #ebedf0;
  margin: 16px 0;
}

.metrics-grid {
  display: flex;
  justify-content: space-between;
}

.metric-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.border-l {
  border-left: 1px solid #ebedf0;
}

.m-label {
  font-size: 10px;
  color: #7f8c8d;
  margin-bottom: 4px;
}

.m-value {
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
}

.m-value.target {
  color: #2ecc71;
}

.m-value small {
  font-size: 9px;
  font-weight: 400;
}

/* 折线图 */
.chart-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
}

.chart-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chart-card h3 {
  font-size: 14px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.canvas-wrapper {
  width: 100%;
  height: 180px;
}

.trend-canvas {
  width: 100%;
  height: 100%;
}

/* 缺口详情 */
.diet-plan-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
}

.diet-plan-card h3 {
  font-size: 14px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 12px 0;
  border-bottom: 1px solid #ebedf0;
  padding-bottom: 8px;
}

.plan-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #2c3e50;
}

.border-t {
  border-top: 1px solid #ebedf0;
  padding-top: 12px;
  margin-top: 4px;
}

.highlight-val {
  font-weight: 600;
  color: #2c3e50;
}

.color-green {
  color: #c0392b;
}

.target-val {
  font-size: 15px;
  font-weight: 700;
  color: #2ecc71;
}

/* 体重录入 */
.dialog-body {
  padding: 24px 16px;
  text-align: center;
}

.slider-val {
  font-size: 24px;
  font-weight: 800;
  color: #2ecc71;
  margin-bottom: 16px;
}

.slider-container {
  padding: 10px 0;
}

.slider-range {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #7f8c8d;
  margin-top: 6px;
}

/* 表单编辑 */
.edit-profile-form {
  padding: 16px 8px;
}

.form-title {
  font-size: 16px;
  font-weight: bold;
  color: #2c3e50;
  text-align: center;
  margin-bottom: 16px;
}

.date-select-text {
  color: #2ecc71;
  font-weight: 600;
  text-decoration: underline;
}

.custom-dropdown :deep(.van-dropdown-menu__bar) {
  height: 24px;
  box-shadow: none;
}

.custom-dropdown :deep(.van-dropdown-menu__title) {
  font-size: 12px;
}

.form-submit-box {
  padding: 20px 16px;
}

.submit-profile-btn {
  height: 44px !important;
  font-weight: bold !important;
}
</style>
