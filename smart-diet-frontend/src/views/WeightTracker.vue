<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">WEIGHT TRACKING DIAL</span>
      <h1 class="display-lg">体重追踪管理</h1>
    </div>

    <!-- 成员切换栏 -->
    <div class="member-selector-row hairline-bottom">
      <span class="body-text text-bold">当前追踪就餐人:</span>
      <el-select v-model="selectedProfileId" @change="handleProfileChange" style="width: 180px">
        <el-option
            v-for="m in members"
            :key="m.profileId"
            :label="m.memberName"
            :value="m.profileId"
        />
      </el-select>
    </div>

    <div class="tracker-layout-grid" v-if="selectedProfile">
      <!-- 1. 左侧：体重记录与目标达成面板 (并排两个 Color-Blocks) -->
      <div class="tracker-left-pane">
        <!-- 今日体重打卡 (Block-Lime) -->
        <div class="color-block color-block-lime checkin-card">
          <span class="caption">WEIGHT CHECK-IN</span>
          <h3 class="card-title">记录今日体重</h3>
          <p class="body-sm" style="margin: var(--spacing-sm) 0">
            录入今日最新体重，系统会自动将其更新为该成员的健康测评基准，并重新折算单餐等比例热量建议。
          </p>
          <div class="input-inline">
            <input
                type="number"
                v-model="newWeight"
                class="input-text"
                style="width: 140px"
                step="0.1"
                placeholder="体重 (kg)"
            />
            <button class="btn-primary" @click="handleRecordWeight" :disabled="!newWeight">
              确认打卡
            </button>
          </div>
        </div>

        <!-- 达成度大面板 (Block-Cream) -->
        <div class="color-block color-block-cream target-card">
          <span class="caption">TARGET PROGRESS</span>
          <h3 class="card-title">健康目标达成度</h3>

          <div class="stats-grid">
            <div class="stat-item">
              <span class="caption">初始体重</span>
              <div class="stat-val font-mono">{{ initialWeight }} <span class="unit">kg</span></div>
            </div>
            <div class="stat-item">
              <span class="caption">目标体重</span>
              <div class="stat-val font-mono">{{ selectedProfile.targetWeight }} <span class="unit">kg</span></div>
            </div>
            <div class="stat-item">
              <span class="caption">累计减重</span>
              <div class="stat-val font-mono"
                   :style="{ color: cumulativeLoss >= 0 ? 'var(--semantic-success)' : 'var(--accent-magenta)' }">
                {{ cumulativeLoss >= 0 ? '-' : '+' }}{{ Math.abs(cumulativeLoss) }} <span class="unit">kg</span>
              </div>
            </div>
            <div class="stat-item">
              <span class="caption">目标完成率</span>
              <div class="stat-val font-mono" style="color: var(--primary)">
                {{ completionRate }} <span class="unit">%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 右侧：SVG 自制体重趋势折线图 (Block-Mint) -->
      <div class="color-block color-block-mint trend-panel">
        <span class="caption">WEIGHT TREND CURVE</span>
        <h3 class="card-title" style="margin-bottom: var(--spacing-md)">体重变化曲线</h3>

        <div class="chart-container" v-if="historyRecords.length > 0">
          <!-- 自制 SVG 折线图，契合 Figma 编辑器极简黑白像素感，避免引入臃肿第三方库 -->
          <svg class="svg-chart" viewBox="0 0 500 240">
            <!-- 趋势填充区域 -->
            <polygon :points="chartPolygonPoints" fill="rgba(94, 106, 210, 0.1)"/>

            <!-- 折线 -->
            <polyline :points="chartPolylinePoints" fill="none" stroke="var(--primary)" stroke-width="3"/>

            <!-- 数据圆点与标注 -->
            <g v-for="(pt, idx) in chartPoints" :key="idx">
              <!-- 圆点 -->
              <circle :cx="pt.x" :cy="pt.y" r="5" fill="var(--primary)"/>
              <!-- 体重数值文本 -->
              <text :x="pt.x" :y="pt.y - 12" class="chart-text-value" text-anchor="middle">
                {{ pt.weight }}kg
              </text>
              <!-- 日期文本 -->
              <text :x="pt.x" :y="225" class="chart-text-date" text-anchor="middle">
                {{ formatShortDate(pt.date) }}
              </text>
            </g>

            <!-- 底部时间轴辅助线 -->
            <line x1="20" y1="210" x2="480" y2="210" stroke="var(--hairline)" stroke-width="1"/>
          </svg>
        </div>

        <div v-else class="empty-chart-box">
          <p class="body-sm" style="color: var(--ink-subtle)">暂无历史体重数据，请在左侧录入今日体重开启第一次打卡！</p>
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

const members = ref<any[]>([])
const selectedProfileId = ref<number | null>(null)
const selectedProfile = ref<any>(null)

const newWeight = ref<number | null>(null)
const historyRecords = ref<any[]>([])

// 载入成员档案
const loadMembers = async () => {
  try {
    const list: any[] = await request.get(`/api/profile/list?groupId=${groupId}`)
    members.value = list
    if (list.length > 0) {
      selectedProfileId.value = list[0].profileId
      selectedProfile.value = list[0]
      loadHistory()
    }
  } catch (e) {
  }
}

const handleProfileChange = () => {
  selectedProfile.value = members.value.find(m => m.profileId === selectedProfileId.value)
  loadHistory()
}

// 载入体重变化历史
const loadHistory = async () => {
  if (!selectedProfileId.value) return
  try {
    historyRecords.value = await request.get(`/api/plan/weight/history?profileId=${selectedProfileId.value}`)
  } catch (e) {
  }
}

// 计算指标：初始体重
const initialWeight = computed(() => {
  if (historyRecords.value.length === 0) {
    return selectedProfile.value ? selectedProfile.value.memberWeight : 0
  }
  return historyRecords.value[0].recordWeight
})

// 计算已减去体重
const cumulativeLoss = computed(() => {
  if (!selectedProfile.value) return 0
  const current = selectedProfile.value.memberWeight
  const loss = initialWeight.value - current
  return Math.round(loss * 100) / 100
})

// 计算目标完成度 %
const completionRate = computed(() => {
  if (!selectedProfile.value) return 0
  const init = initialWeight.value
  const target = selectedProfile.value.targetWeight
  const current = selectedProfile.value.memberWeight

  const targetDiff = init - target
  if (targetDiff <= 0) return 100 // 如果本来就达标，直接 100%

  const loss = init - current
  const rate = (loss / targetDiff) * 100

  if (rate < 0) return 0
  return Math.round(Math.min(rate, 100) * 10) / 10
})

// 录入今天体重
const handleRecordWeight = async () => {
  if (!selectedProfileId.value || !newWeight.value) return

  try {
    const res = await request.post('/api/plan/weight/record', {
      profileId: selectedProfileId.value,
      recordWeight: newWeight.value
    })
    if (res) {
      ElMessage.success('今日体重录入成功！卡路里指标已自动重算！')
      newWeight.value = null

      // 重新拉取
      loadMembers().then(() => {
        if (selectedProfileId.value) {
          selectedProfile.value = members.value.find(m => m.profileId === selectedProfileId.value)
          loadHistory()
        }
      })
    }
  } catch (e) {
  }
}

// ==========================================
// 自制 SVG 折线图坐标计算
// ==========================================
const chartPoints = computed(() => {
  if (historyRecords.value.length === 0) return []

  // 最多显示最近 7 次数据
  const list = historyRecords.value.slice(-7)
  const count = list.length

  // 计算体重的最大最小值用于折算高度
  const weights = list.map(r => r.recordWeight)
  const maxW = Math.max(...weights) + 1.0
  const minW = Math.max(0, Math.min(...weights) - 1.0)
  const range = maxW - minW

  // 折算坐标 (画图区域 x 从 40 到 460，y 从 40 到 190)
  return list.map((record, index) => {
    const x = count === 1 ? 250 : 40 + (index * (420 / (count - 1)))
    const y = range === 0 ? 115 : 190 - ((record.recordWeight - minW) / range) * 150
    return {
      x,
      y,
      weight: record.recordWeight,
      date: record.recordDate
    }
  })
})

const chartPolylinePoints = computed(() => {
  return chartPoints.value.map(pt => `${pt.x},${pt.y}`).join(' ')
})

const chartPolygonPoints = computed(() => {
  if (chartPoints.value.length === 0) return ''
  const first = chartPoints.value[0]
  const last = chartPoints.value[chartPoints.value.length - 1]
  const linePoints = chartPolylinePoints.value

  // 底部封口点，形成多边形以填充渐变或淡色背景
  return `${first.x},210 ${linePoints} ${last.x},210`
})

const formatShortDate = (dateStr: string): string => {
  if (!dateStr) return ''
  // 格式化 2026-06-20 -> 06/20
  const parts = dateStr.split('-')
  if (parts.length >= 3) {
    return `${parts[1]}/${parts[2]}`
  }
  return dateStr
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

.member-selector-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding-bottom: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.tracker-layout-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
}

.tracker-left-pane {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.checkin-card {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.input-inline {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xs);
}

.target-card {
  min-height: 220px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}

.stat-item {
  display: flex;
  flex-direction: column;
  border-left: 2px solid var(--primary);
  padding-left: var(--spacing-xs);
}

.stat-val {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.stat-val .unit {
  font-size: 11px;
  font-weight: 400;
}

.trend-panel {
  min-height: 420px;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex-grow: 1;
  background-color: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: var(--spacing-md);
}

.svg-chart {
  width: 100%;
  height: 100%;
}

.chart-text-value {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  fill: var(--primary);
}

.chart-text-date {
  font-family: var(--font-sans);
  font-size: 10px;
  fill: var(--ink-subtle);
}

.empty-chart-box {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--hairline);
  border-radius: var(--rounded-md);
  margin-top: var(--spacing-md);
}
</style>
