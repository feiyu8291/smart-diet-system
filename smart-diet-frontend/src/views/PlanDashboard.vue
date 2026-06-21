<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">DIET DASHBOARD</span>
      <h1 class="display-lg">计划进度大盘</h1>
    </div>

    <!-- 1. 未开启计划状态 (展示 Block-Lilac 紫色便签卡片) -->
    <div v-if="!hasActivePlan" class="color-block color-block-lilac intro-card">
      <div class="intro-left">
        <h2 class="headline">开启您的家庭健康饮食周期</h2>
        <p class="body-text">
          健康减脂是一场科学规律的周期循环。本系统支持“轻食、正常、放纵餐”交替推荐模式，
          自动为每位家人提供精确至克的单餐摄入量。立即选择一个减脂模板开始吧！
        </p>
        <div class="start-form">
          <el-select v-model="selectedTemplateId" placeholder="选择膳食周期计划模板" style="width: 280px">
            <el-option
                v-for="tpl in templates"
                :key="tpl.planId"
                :label="tpl.planName + ' (' + tpl.totalDays + '天)'"
                :value="tpl.planId"
            />
          </el-select>
          <button class="btn-primary" @click="handleStartPlan" :disabled="!selectedTemplateId">
            开启健康周期计划
          </button>
        </div>
      </div>
    </div>

    <!-- 2. 已开启计划状态 -->
    <div v-else class="dashboard-active-grid">
      <!-- 2.1 计划大看板 (Block-Lime 绿色背景) -->
      <div class="color-block color-block-lime progress-card">
        <span class="caption">ACTIVE PLAN</span>
        <h2 class="card-title">{{ activeTemplate.planName }}</h2>
        <div class="progress-big-number">
          <span class="display-xl">{{ currentProgress.currentDay }}</span>
          <span class="body-lg">/ {{ activeTemplate.totalDays }} 天</span>
        </div>
        <p class="body-sm">{{ activeTemplate.planDescription }}</p>
      </div>

      <!-- 2.2 日历周期指示 (展现轻/正常/放纵交替) -->
      <div class="calendar-panel hairline-border">
        <span class="caption">DIET SCHEDULE DIAL</span>
        <h3 class="headline">周期循环盘</h3>
        <div class="dial-grid">
          <!-- 模拟渲染 21 天，按 3天轻食(Lime) + 1天正常(Cream) + 放纵餐(Pink) 轮换 -->
          <div
              v-for="day in activeTemplate.totalDays"
              :key="day"
              :class="[
              'dial-day-cell',
              getDayModeClass(day),
              currentProgress.currentDay === day ? 'active-day-pulse' : ''
            ]"
              @click="handleDayClick(day)"
          >
            <span class="day-num">{{ day }}</span>
            <span class="day-mode-label">{{ getDayModeText(day) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 用餐食谱详情 (选中某一天展现) -->
    <div v-if="hasActivePlan" class="meal-detail-section content-container">
      <div class="meal-detail-header">
        <h3 class="headline">第 {{ selectedDay }} 天 膳食安排</h3>
        <span class="caption tag-mode" :class="getDayModeClass(selectedDay)">
          {{ getDayModeText(selectedDay) }} 模式
        </span>
      </div>

      <!-- 一日三餐 Tab 药丸样式 -->
      <div class="period-tabs">
        <button
            v-for="p in periods"
            :key="p.id"
            :class="activePeriod === p.id ? 'btn-primary' : 'btn-secondary'"
            @click="activePeriod = p.id; loadMealDetail()"
        >
          {{ p.name }}
        </button>
      </div>

      <!-- 今日食谱展现 -->
      <div v-if="mealPlanDetail.hasMeal" class="meal-plan-box">
        <div class="dishes-row">
          <div v-for="dish in mealPlanDetail.dishes" :key="dish.dishId" class="dish-card hairline-border">
            <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500'"
                 class="dish-img" alt="dish cover"/>
            <div class="dish-info">
              <span class="caption">{{ dish.cuisineType }}</span>
              <h4 class="card-title">{{ dish.dishName }}</h4>
              <p class="body-sm">单餐热量：{{ dish.calories }} kcal/100g</p>
            </div>
          </div>
        </div>

        <div class="grocery-portion-grid">
          <!-- 采购清单 (左侧) -->
          <div class="color-block color-block-cream grocery-box">
            <span class="caption">GROCERY LIST</span>
            <h4 class="card-title">本餐食材采购量</h4>
            <ul class="grocery-ul">
              <li v-for="item in mealPlanDetail.groceries" :key="item.ingredientId">
                <span class="body-text">{{ item.ingredientName }}</span>
                <span class="eyebrow text-bold">{{ item.useAmount }} {{ item.measureUnit }}</span>
              </li>
            </ul>
          </div>

          <!-- 分餐卡片 (右侧) -->
          <div class="color-block color-block-mint portion-box">
            <span class="caption">PORTION DISPENSING</span>
            <h4 class="card-title">家庭成员分餐建议 (等比例)</h4>
            <div class="portion-list">
              <div v-for="item in mealPlanDetail.portions" :key="item.portionId" class="portion-item">
                <div class="portion-user">
                  <span class="body-text text-bold">{{ item.memberName }}</span>
                  <span class="caption">({{ item.memberRelation }})</span>
                </div>
                <div class="portion-gram eyebrow">
                  {{ item.recommendWeight }} g
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 打卡按钮 -->
        <div class="action-footer" v-if="selectedDay === currentProgress.currentDay">
          <button class="btn-primary" @click="dialogVisible = true">
            用膳完毕，打卡记录
          </button>
        </div>
      </div>

      <!-- 今日未配餐 -->
      <div v-else class="color-block color-block-pink empty-meal-box">
        <h4 class="headline">本餐尚未生成膳食推荐</h4>
        <p class="body-text">请立刻前往联合配餐页面，生成今天家人们最期望且不重复的营养菜系吧！</p>
        <button class="btn-primary" @click="$router.push('/meal-planner')">
          立即配餐
        </button>
      </div>
    </div>

    <!-- 打卡不喜欢标记对话框 ( Element Plus ) -->
    <el-dialog
        v-model="dialogVisible"
        title="用餐反馈记录"
        width="480px"
    >
      <p class="body-sm" style="margin-bottom: var(--spacing-sm)">
        吃饭人如果觉得这餐饭有不喜欢的菜，可以进行标记。
        连续3次标记不喜欢的菜，后续将彻底在菜单推荐中拉黑！
      </p>

      <div v-for="member in familyMembers" :key="member.profileId" class="feedback-member-row">
        <span class="body-sm font-bold">{{ member.memberName }}:</span>
        <el-checkbox-group v-model="feedbackDislikes[member.profileId]">
          <el-checkbox
              v-for="dish in mealPlanDetail.dishes"
              :key="dish.dishId"
              :value="dish.dishId"
          >
            不喜欢【{{ dish.dishName }}】
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <button class="btn-secondary" style="margin-right: 10px" @click="dialogVisible = false">取消</button>
          <button class="btn-primary" @click="handleCompleteMeal">确认打卡</button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {inject, onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import request from '../utils/request'

const groupId = inject<number>('groupId', 1)

// 状态控制
const hasActivePlan = ref(false)
const selectedTemplateId = ref<number | null>(null)
const templates = ref<any[]>([])
const activeTemplate = ref<any>({})
const currentProgress = ref<any>({})
const selectedDay = ref(1)

const periods = [
  {id: 1, name: '早餐'},
  {id: 2, name: '午餐'},
  {id: 3, name: '晚餐'}
]
const activePeriod = ref(2) // 默认午餐

const mealPlanDetail = ref<any>({hasMeal: false})
const dialogVisible = ref(false)

const familyMembers = ref<any[]>([])
const feedbackDislikes = ref<Record<number, number[]>>({}) // profileId -> [dishId]

// 载入大盘数据
const loadDashboard = async () => {
  try {
    const res: any = await request.get(`/api/plan/current?groupId=${groupId}`)
    if (res.hasActivePlan) {
      hasActivePlan.value = true
      currentProgress.value = res.progress
      activeTemplate.value = res.template
      selectedDay.value = res.progress.currentDay
      loadMealDetail()
    } else {
      hasActivePlan.value = false
      loadTemplates()
    }
  } catch (e) {
  }
}

const loadTemplates = async () => {
  try {
    templates.value = await request.get('/api/plan/templates')
  } catch (e) {
  }
}

const loadMealDetail = async () => {
  if (!hasActivePlan.value) return
  // 计算选中天对应的公历日期
  const offset = selectedDay.value - currentProgress.value.currentDay
  const baseDate = new Date()
  baseDate.setDate(baseDate.getDate() + offset)
  const targetDateStr = baseDate.toISOString().split('T')[0]

  try {
    mealPlanDetail.value = await request.get(`/api/meal/detail?groupId=${groupId}&targetDate=${targetDateStr}&mealPeriod=${activePeriod.value}`)
  } catch (e) {
  }
}

const handleStartPlan = async () => {
  if (!selectedTemplateId.value) return
  try {
    const res = await request.post('/api/plan/start', {
      groupId: groupId,
      planId: selectedTemplateId.value
    })
    if (res) {
      ElMessage.success('计划启动成功！')
      loadDashboard()
    }
  } catch (e) {
  }
}

// 获取家庭成员
const loadMembers = async () => {
  try {
    familyMembers.value = await request.get(`/api/profile/list?groupId=${groupId}`)
    familyMembers.value.forEach(m => {
      feedbackDislikes.value[m.profileId] = []
    })
  } catch (e) {
  }
}

// 自动判断天数对应模式：轻食 / 正常 / 放纵
const getDayMode = (day: number): number => {
  // 3天轻食 (1) + 1天正常 (0) + 1天放纵 (2) 轮流循环
  const cycle = day % 5
  if (cycle === 1 || cycle === 2 || cycle === 3) return 1 // 轻食
  if (cycle === 4) return 0 // 正常
  return 2 // 放纵
}

const getDayModeText = (day: number): string => {
  const mode = getDayMode(day)
  if (mode === 1) return '轻食'
  if (mode === 0) return '正常'
  return '放纵'
}

const getDayModeClass = (day: number): string => {
  const mode = getDayMode(day)
  if (mode === 1) return 'color-block-lime'
  if (mode === 0) return 'color-block-cream'
  return 'color-block-pink'
}

const handleDayClick = (day: number) => {
  selectedDay.value = day
  loadMealDetail()
}

// 打卡确认
const handleCompleteMeal = async () => {
  const dislikesList: any[] = []
  Object.keys(feedbackDislikes.value).forEach(pIdStr => {
    const pId = Number(pIdStr)
    feedbackDislikes.value[pId].forEach(dishId => {
      dislikesList.push({profileId: pId, dishId: dishId})
    })
  })

  try {
    const res = await request.post('/api/meal/complete', {
      mealPlanId: mealPlanDetail.value.mealPlan.mealPlanId,
      dislikes: dislikesList
    })
    if (res) {
      ElMessage.success('用餐完毕，打卡成功！')
      dialogVisible.value = false
      loadDashboard()
    }
  } catch (e) {
  }
}

onMounted(() => {
  loadDashboard()
  loadMembers()
})
</script>

<style scoped>
.header-section {
  margin-top: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
}

.intro-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 280px;
}

.intro-left {
  max-width: 650px;
}

.intro-left h2 {
  margin-bottom: var(--spacing-md);
}

.intro-left p {
  margin-bottom: var(--spacing-xl);
}

.start-form {
  display: flex;
  gap: var(--spacing-md);
}

.dashboard-active-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.progress-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 260px;
}

.progress-big-number {
  margin: var(--spacing-md) 0;
  display: flex;
  align-items: baseline;
}

.calendar-panel {
  border-radius: var(--rounded-lg);
  padding: var(--spacing-lg);
}

.dial-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: var(--spacing-xs);
  margin-top: var(--spacing-md);
}

.dial-day-cell {
  aspect-ratio: 1;
  border-radius: var(--rounded-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid transparent;
  transition: transform 0.1s ease;
}

.dial-day-cell:hover {
  transform: scale(1.05);
}

.active-day-pulse {
  border: 2px solid var(--primary) !important;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
}

.day-num {
  font-size: 20px;
  font-weight: 700;
}

.day-mode-label {
  font-size: 10px;
  font-weight: 500;
}

.meal-detail-section {
  border-top: 1px solid var(--hairline);
  padding-top: var(--spacing-xl);
}

.meal-detail-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.tag-mode {
  padding: 4px 12px;
  border-radius: var(--rounded-pill);
  font-size: 12px;
  font-weight: 700;
}

.period-tabs {
  display: flex;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xl);
}

.period-tabs button {
  padding: 8px 24px;
}

.dishes-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.dish-card {
  border-radius: var(--rounded-lg);
  overflow: hidden;
  display: flex;
  background-color: var(--canvas);
}

.dish-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
}

.dish-info {
  padding: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.grocery-portion-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.grocery-box, .portion-box {
  min-height: 280px;
}

.grocery-ul {
  list-style: none;
  margin-top: var(--spacing-md);
}

.grocery-ul li {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-xs) 0;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.1);
}

.portion-list {
  margin-top: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.portion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-xs) 0;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.1);
}

.portion-user {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-xs);
}

.portion-gram {
  font-size: 18px;
  font-weight: 700;
}

.action-footer {
  text-align: right;
  margin-top: var(--spacing-lg);
}

.empty-meal-box {
  text-align: center;
  padding: var(--spacing-xl);
}

.empty-meal-box button {
  margin-top: var(--spacing-lg);
}

.feedback-member-row {
  margin-bottom: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xxs);
}
</style>
