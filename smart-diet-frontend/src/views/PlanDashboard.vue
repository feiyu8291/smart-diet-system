<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">DIET DASHBOARD</span>
      <h1 class="display-lg">计划进度大盘</h1>
    </div>

    <!-- 1. 未开启计划状态 (展示 Linear 极简白色卡片，配以淡紫指示边框) -->
    <div v-if="!hasActivePlan" class="linear-card linear-card-lilac intro-card">
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
      <!-- 2.1 计划大看板 (Linear 风格配以健康绿指示边框) -->
      <div class="linear-card linear-card-lime progress-card">
        <div>
          <span class="caption">ACTIVE PLAN</span>
          <h2 class="card-title">{{ activeTemplate.planName }}</h2>
        </div>
        <div class="progress-big-number">
          <span class="display-xl">{{ currentProgress.currentDay }}</span>
          <span class="body-lg">/ {{ activeTemplate.totalDays }} 天</span>
        </div>
        <p class="body-sm">{{ activeTemplate.planDescription }}</p>
      </div>

      <!-- 2.2 日历周期指示 (精密网格循环盘) -->
      <div class="linear-card calendar-panel">
        <span class="caption">DIET SCHEDULE DIAL</span>
        <h3 class="headline">周期循环盘</h3>
        <div class="dial-grid">
          <!-- 模拟渲染 21 天，通过底部的彩色点指示轻食/正常/放纵模式 -->
          <div
              v-for="day in activeTemplate.totalDays"
              :key="day"
              :class="[
              'dial-day-cell',
              currentProgress.currentDay === day ? 'active-day-pulse' : ''
            ]"
              @click="handleDayClick(day)"
          >
            <span class="day-num">{{ day }}</span>
            <div class="day-dot-indicator">
              <span :class="['dot-node', getDayModeDotClass(day)]"></span>
              <span class="day-mode-label">{{ getDayModeText(day) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 用餐食谱详情 (选中某一天展现) -->
    <div v-if="hasActivePlan" class="meal-detail-section">
      <div class="meal-detail-header">
        <h3 class="headline">第 {{ selectedDay }} 天 膳食安排</h3>
        <span class="caption tag-mode" :class="getDayModeTagClass(selectedDay)">
          {{ getDayModeText(selectedDay) }} 模式
        </span>
      </div>

      <!-- 今日食谱展现 -->
      <div v-if="mealDayDetail.hasMeal" class="meal-day-container">
        <!-- 一日三餐平铺卡片 -->
        <div class="meal-periods-grid">
          <!-- 早餐 -->
          <div class="linear-card meal-period-card">
            <div class="period-header">
              <span class="eyebrow">BREAKFAST</span>
              <h4 class="card-title">早餐</h4>
            </div>
            <div v-if="mealDayDetail.breakfast && mealDayDetail.breakfast.hasMeal" class="period-body">
              <!-- 菜品列表 -->
              <div class="dishes-list-compact">
                <div v-for="dish in mealDayDetail.breakfast.dishes" :key="dish.dishId" class="dish-item-compact">
                  <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500'" class="dish-img-compact" alt="dish cover"/>
                  <div class="dish-info-compact">
                    <span class="dish-cuisine">{{ dish.cuisineType }}</span>
                    <h5 class="dish-name">{{ dish.dishName }}</h5>
                    <span class="dish-cal">{{ dish.calories }} kcal/100g</span>
                  </div>
                </div>
              </div>

              <!-- 分餐建议 -->
              <div class="portion-list-compact">
                <span class="caption">分餐建议 (等比例)</span>
                <div v-for="item in mealDayDetail.breakfast.portions" :key="item.portionId" class="portion-item-compact">
                  <div class="portion-user-compact">
                    <span class="body-sm text-bold">{{ item.memberName }}</span>
                    <span class="caption">({{ item.memberRelation }})</span>
                  </div>
                  <span class="portion-gram-compact">{{ item.recommendWeight }}g</span>
                </div>
              </div>

              <!-- 打卡按钮 -->
              <div class="period-action" v-if="selectedDay === currentProgress.currentDay">
                <button class="btn-primary btn-full" @click="openFeedbackDialog(mealDayDetail.breakfast)">
                  早餐打卡反馈
                </button>
              </div>
            </div>
            <div v-else class="empty-period-placeholder">
              <span class="body-sm">今日未安排早餐</span>
            </div>
          </div>

          <!-- 午餐 -->
          <div class="linear-card meal-period-card">
            <div class="period-header">
              <span class="eyebrow">LUNCH</span>
              <h4 class="card-title">午餐</h4>
            </div>
            <div v-if="mealDayDetail.lunch && mealDayDetail.lunch.hasMeal" class="period-body">
              <!-- 菜品列表 -->
              <div class="dishes-list-compact">
                <div v-for="dish in mealDayDetail.lunch.dishes" :key="dish.dishId" class="dish-item-compact">
                  <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500'" class="dish-img-compact" alt="dish cover"/>
                  <div class="dish-info-compact">
                    <span class="dish-cuisine">{{ dish.cuisineType }}</span>
                    <h5 class="dish-name">{{ dish.dishName }}</h5>
                    <span class="dish-cal">{{ dish.calories }} kcal/100g</span>
                  </div>
                </div>
              </div>

              <!-- 分餐建议 -->
              <div class="portion-list-compact">
                <span class="caption">分餐建议 (等比例)</span>
                <div v-for="item in mealDayDetail.lunch.portions" :key="item.portionId" class="portion-item-compact">
                  <div class="portion-user-compact">
                    <span class="body-sm text-bold">{{ item.memberName }}</span>
                    <span class="caption">({{ item.memberRelation }})</span>
                  </div>
                  <span class="portion-gram-compact">{{ item.recommendWeight }}g</span>
                </div>
              </div>

              <!-- 打卡按钮 -->
              <div class="period-action" v-if="selectedDay === currentProgress.currentDay">
                <button class="btn-primary btn-full" @click="openFeedbackDialog(mealDayDetail.lunch)">
                  午餐打卡反馈
                </button>
              </div>
            </div>
            <div v-else class="empty-period-placeholder">
              <span class="body-sm">今日未安排午餐</span>
            </div>
          </div>

          <!-- 晚餐 -->
          <div class="linear-card meal-period-card">
            <div class="period-header">
              <span class="eyebrow">DINNER</span>
              <h4 class="card-title">晚餐</h4>
            </div>
            <div v-if="mealDayDetail.dinner && mealDayDetail.dinner.hasMeal" class="period-body">
              <!-- 菜品列表 -->
              <div class="dishes-list-compact">
                <div v-for="dish in mealDayDetail.dinner.dishes" :key="dish.dishId" class="dish-item-compact">
                  <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500'" class="dish-img-compact" alt="dish cover"/>
                  <div class="dish-info-compact">
                    <span class="dish-cuisine">{{ dish.cuisineType }}</span>
                    <h5 class="dish-name">{{ dish.dishName }}</h5>
                    <span class="dish-cal">{{ dish.calories }} kcal/100g</span>
                  </div>
                </div>
              </div>

              <!-- 分餐建议 -->
              <div class="portion-list-compact">
                <span class="caption">分餐建议 (等比例)</span>
                <div v-for="item in mealDayDetail.dinner.portions" :key="item.portionId" class="portion-item-compact">
                  <div class="portion-user-compact">
                    <span class="body-sm text-bold">{{ item.memberName }}</span>
                    <span class="caption">({{ item.memberRelation }})</span>
                  </div>
                  <span class="portion-gram-compact">{{ item.recommendWeight }}g</span>
                </div>
              </div>

              <!-- 打卡按钮 -->
              <div class="period-action" v-if="selectedDay === currentProgress.currentDay">
                <button class="btn-primary btn-full" @click="openFeedbackDialog(mealDayDetail.dinner)">
                  晚餐打卡反馈
                </button>
              </div>
            </div>
            <div v-else class="empty-period-placeholder">
              <span class="body-sm">今日未安排晚餐</span>
            </div>
          </div>
        </div>

        <!-- 采购清单 (全天合并总量) -->
        <div class="linear-card linear-card-cream daily-grocery-card">
          <span class="caption">DAILY GROCERY LIST</span>
          <h4 class="card-title">全天食材采购清单</h4>
          <p class="body-sm" style="margin-top: 4px; color: var(--linear-muted);">已将早中晚三餐所需食材的用量进行合并加总，方便一站式采购。</p>
          <div v-if="mealDayDetail.dailyGroceries && mealDayDetail.dailyGroceries.length > 0" class="grocery-grid-layout">
            <div v-for="item in mealDayDetail.dailyGroceries" :key="item.ingredientId" class="grocery-item-chip">
              <span class="body-text">{{ item.ingredientName }}</span>
              <span class="eyebrow text-bold">{{ item.useAmount }} {{ item.measureUnit }}</span>
            </div>
          </div>
          <div v-else class="empty-grocery-placeholder">
            <span class="body-sm">暂无采购数据</span>
          </div>
        </div>
      </div>

      <!-- 今日未配餐 -->
      <div v-else class="linear-card linear-card-pink empty-meal-box">
        <h4 class="headline">今日尚未生成膳食推荐</h4>
        <p class="body-text" style="margin: 12px 0 20px 0; color: var(--linear-body);">
          请立刻前往联合配餐页面，生成今天家人们最期望且不重复的营养菜系吧！
        </p>
        <button class="btn-primary" @click="$router.push('/meal-planner')">
          立即配餐
        </button>
      </div>
    </div>

    <!-- 用餐反馈记录对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="用餐反馈记录"
        width="480px"
        class="linear-dialog"
    >
      <p class="body-sm" style="margin-bottom: 16px; color: var(--linear-body); line-height: 1.5;">
        吃饭人如果觉得这餐饭有不喜欢的菜，可以进行标记。
        连续 3 次标记不喜欢的菜，后续将彻底在菜单推荐中拉黑！
      </p>

      <div v-for="member in familyMembers" :key="member.profileId" class="feedback-member-row">
        <span class="body-sm font-bold" style="color: var(--linear-ink); font-weight: 500;">{{ member.memberName }}:</span>
        <el-checkbox-group v-model="feedbackDislikes[member.profileId]">
          <el-checkbox
              v-for="dish in feedbackMeal?.dishes || []"
              :key="dish.dishId"
              :value="dish.dishId"
          >
            不喜欢【{{ dish.dishName }}】
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <button class="btn-secondary" style="margin-right: 12px" @click="dialogVisible = false">取消</button>
          <button class="btn-primary" @click="handleCompleteMeal">确认打卡</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {inject, onMounted, ref, unref} from 'vue'
import {ElMessage} from 'element-plus'
import request from '../utils/request'

const activeGroupIdRef = inject<any>('groupId')
const groupId = unref(activeGroupIdRef) || 1

// 状态控制
const hasActivePlan = ref(false)
const selectedTemplateId = ref<number | null>(null)
const templates = ref<any[]>([])
const activeTemplate = ref<any>({})
const currentProgress = ref<any>({})
const selectedDay = ref(1)

const mealDayDetail = ref<any>({hasMeal: false})
const feedbackMeal = ref<any>(null)
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
    mealDayDetail.value = await request.get(`/api/meal/day-detail?groupId=${groupId}&targetDate=${targetDateStr}`)
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

const getDayModeDotClass = (day: number): string => {
  const mode = getDayMode(day)
  if (mode === 1) return 'dot-lime'
  if (mode === 0) return 'dot-orange'
  return 'dot-red'
}

const getDayModeTagClass = (day: number): string => {
  const mode = getDayMode(day)
  if (mode === 1) return 'tag-lime'
  if (mode === 0) return 'tag-orange'
  return 'tag-red'
}

const handleDayClick = (day: number) => {
  selectedDay.value = day
  loadMealDetail()
}

const openFeedbackDialog = (mealDetail: any) => {
  feedbackMeal.value = mealDetail
  // 清空以往的不喜欢选择
  familyMembers.value.forEach(m => {
    feedbackDislikes.value[m.profileId] = []
  })
  dialogVisible.value = true
}

// 打卡确认
const handleCompleteMeal = async () => {
  if (!feedbackMeal.value || !feedbackMeal.value.mealPlan) return
  const dislikesList: any[] = []
  Object.keys(feedbackDislikes.value).forEach(pIdStr => {
    const pId = Number(pIdStr)
    feedbackDislikes.value[pId].forEach(dishId => {
      dislikesList.push({profileId: pId, dishId: dishId})
    })
  })

  try {
    const res = await request.post('/api/meal/complete', {
      mealPlanId: feedbackMeal.value.mealPlan.mealPlanId,
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
/* ==========================================
   Linear 设计令牌定义与样式规范
   ========================================== */
:root {
  --linear-canvas: #ffffff;
  --linear-canvas-soft: #fafafa;
  --linear-canvas-soft-2: #f5f5f5;
  --linear-ink: #171717;
  --linear-body: #4d4d4d;
  --linear-muted: #888888;
  --linear-hairline: #ebebeb;
  --linear-primary: #171717;

  --linear-accent-purple: #7928ca;
  --linear-accent-green: #29bc9b;
  --linear-accent-orange: #f5a623;
  --linear-accent-red: #ee0000;
  --linear-accent-blue: #0070f3;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* Eyebrow 等宽标签修饰 */
.eyebrow {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.12em;
  color: var(--linear-muted, #888888);
  text-transform: uppercase;
  display: block;
  margin-bottom: 6px;
}

.display-lg {
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.03em;
  color: var(--linear-ink, #171717);
  margin: 0 0 24px 0;
}

/* Linear 风格极简精致卡片 */
.linear-card {
  background-color: var(--linear-canvas, #ffffff);
  border: 1px solid var(--linear-hairline, #ebebeb);
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.02), 0px 4px 12px rgba(0, 0, 0, 0.03);
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
}

.linear-card:hover {
  box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.03), 0px 8px 16px rgba(0, 0, 0, 0.04);
}

/* 顶部状态色条，用于区分不同模式 */
.linear-card-lilac {
  border-top: 3px solid var(--primary);
}

.linear-card-lime {
  border-top: 3px solid var(--semantic-success);
}

.linear-card-cream {
  border-top: 3px solid var(--brand-secure);
}

.linear-card-mint {
  border-top: 3px solid var(--primary-hover);
}

.linear-card-pink {
  border-top: 3px solid var(--accent-magenta);
}

.headline {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--linear-ink, #171717);
  margin-bottom: 12px;
}

.body-text {
  font-size: 14px;
  color: var(--linear-body, #4d4d4d);
  line-height: 1.6;
}

.start-form {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

/* 按钮规范 */
.btn-primary {
  background-color: var(--linear-primary, #171717);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s ease;
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-primary:disabled {
  background-color: #e5e5e5;
  color: #a1a1a1;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #ffffff;
  color: var(--linear-ink, #171717);
  border: 1px solid var(--linear-hairline, #ebebeb);
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.02);
  transition: background-color 0.15s ease;
}

.btn-secondary:hover {
  background-color: #fafafa;
}

.dashboard-active-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 24px;
  margin-bottom: 32px;
}

.progress-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 260px;
}

.caption {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--linear-muted, #888888);
  text-transform: uppercase;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--linear-ink, #171717);
  margin-top: 4px;
  margin-bottom: 0;
}

.progress-big-number {
  margin: 16px 0;
  display: flex;
  align-items: baseline;
}

.display-xl {
  font-size: 56px;
  font-weight: 600;
  letter-spacing: -0.04em;
  color: var(--linear-ink, #171717);
  line-height: 1;
}

.body-lg {
  font-size: 16px;
  color: var(--linear-muted, #888888);
  margin-left: 6px;
}

.body-sm {
  font-size: 13px;
  color: var(--linear-muted, #888888);
  line-height: 1.5;
}

/* 周期网格 */
.dial-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
  margin-top: 16px;
}

.dial-day-cell {
  aspect-ratio: 1;
  background-color: var(--linear-canvas, #ffffff);
  border: 1px solid var(--linear-hairline, #ebebeb);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
  padding: 6px;
}

.dial-day-cell:hover {
  border-color: var(--linear-muted, #888888);
}

.active-day-pulse {
  outline: 2px solid var(--primary);
  outline-offset: -2px;
}

.day-num {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
}

/* 精致指示器圆点 */
.day-dot-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
}

.dot-node {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  display: inline-block;
}

.dot-lime {
  background-color: var(--semantic-success);
}

.dot-orange {
  background-color: var(--brand-secure);
}

.dot-red {
  background-color: var(--primary);
}

.day-mode-label {
  font-size: 9px;
  color: var(--ink-subtle);
}

/* 用餐详情 */
.meal-detail-section {
  border-top: 1px solid var(--hairline);
  padding-top: 32px;
  margin-top: 32px;
}

.meal-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

/* Tag 标识 */
.tag-mode {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
}

.tag-lime {
  background-color: rgba(39, 166, 68, 0.1);
  color: var(--semantic-success);
}

.tag-orange {
  background-color: rgba(122, 127, 173, 0.1);
  color: var(--brand-secure);
}

.tag-red {
  background-color: rgba(94, 106, 210, 0.1);
  color: var(--primary-hover);
}

/* Tab 切换 */
.period-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.btn-tab {
  background-color: var(--linear-canvas, #ffffff);
  color: var(--linear-body, #4d4d4d);
  border: 1px solid var(--linear-hairline, #ebebeb);
  border-radius: 6px;
  padding: 6px 18px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-tab:hover {
  background-color: #fafafa;
  color: var(--linear-ink, #171717);
}

.btn-tab-active {
  background-color: var(--linear-primary, #171717);
  color: #ffffff;
  border: 1px solid var(--linear-primary, #171717);
  border-radius: 6px;
  padding: 6px 18px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

/* 菜品卡片 */
.dishes-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.dish-card {
  background-color: var(--linear-canvas, #ffffff);
  border: 1px solid var(--linear-hairline, #ebebeb);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.02);
  transition: border-color 0.15s ease;
}

.dish-card:hover {
  border-color: var(--linear-muted, #888888);
}

.dish-img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-right: 1px solid var(--linear-hairline, #ebebeb);
}

.dish-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.dish-info .caption {
  margin-bottom: 2px;
}

.dish-info .card-title {
  font-size: 15px;
  margin: 0;
}

.dish-info .body-sm {
  margin-top: 4px;
  margin-bottom: 0;
}

/* 采购与分餐区 */
.grocery-portion-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.grocery-box, .portion-box {
  min-height: 280px;
}

.grocery-ul {
  list-style: none;
  margin-top: 16px;
  padding: 0;
}

.grocery-ul li {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--linear-hairline, #ebebeb);
  font-size: 14px;
  color: var(--linear-body, #4d4d4d);
}

.portion-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.portion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--linear-hairline, #ebebeb);
}

.portion-user {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.portion-user .body-text {
  font-size: 14px;
  color: var(--linear-ink, #171717);
}

.portion-gram {
  font-size: 16px;
  font-weight: 600;
  color: var(--linear-ink, #171717);
}

.action-footer {
  text-align: right;
  margin-top: 24px;
}

.empty-meal-box {
  text-align: center;
  padding: 40px 24px;
}

/* 三餐并排 */
.meal-periods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

@media (max-width: 992px) {
  .meal-periods-grid {
    grid-template-columns: 1fr;
  }
}

.meal-period-card {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 400px;
}

.period-header {
  border-bottom: 1px solid var(--linear-hairline, #ebebeb);
  padding-bottom: 12px;
  margin-bottom: 16px;
}

.period-body {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.dishes-list-compact {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.dish-item-compact {
  display: flex;
  align-items: center;
  border: 1px solid var(--linear-hairline, #ebebeb);
  border-radius: 6px;
  overflow: hidden;
  background-color: var(--linear-canvas-soft, #fafafa);
}

.dish-img-compact {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-right: 1px solid var(--linear-hairline, #ebebeb);
}

.dish-info-compact {
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.dish-info-compact .dish-cuisine {
  font-size: 9px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: var(--linear-muted, #888888);
  text-transform: uppercase;
}

.dish-info-compact .dish-name {
  font-size: 13px;
  font-weight: 600;
  margin: 2px 0;
  color: var(--linear-ink, #171717);
}

.dish-info-compact .dish-cal {
  font-size: 10px;
  color: var(--linear-muted, #888888);
}

.portion-list-compact {
  margin-top: auto;
  border-top: 1px dashed var(--linear-hairline, #ebebeb);
  padding-top: 16px;
  margin-bottom: 16px;
}

.portion-item-compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid var(--linear-hairline, #ebebeb);
}

.portion-user-compact {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.portion-gram-compact {
  font-size: 13px;
  font-weight: 600;
  color: var(--linear-ink, #171717);
}

.period-action {
  margin-top: 12px;
}

.btn-full {
  width: 100%;
}

.empty-period-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  border: 2px dashed var(--linear-hairline, #ebebeb);
  border-radius: 6px;
  color: var(--linear-muted, #888888);
  min-height: 200px;
}

/* 全天食材采购大卡片 */
.daily-grocery-card {
  margin-top: 24px;
  border-left: 4px solid var(--linear-accent-orange, #f5a623);
}

.grocery-grid-layout {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.grocery-item-chip {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border: 1px solid var(--linear-hairline, #ebebeb);
  background-color: var(--linear-canvas-soft, #fafafa);
  border-radius: 6px;
}

.empty-grocery-placeholder {
  text-align: center;
  padding: 24px;
  color: var(--linear-muted, #888888);
}

.feedback-member-row {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>

