<template>
  <div class="mobile-layout">
    <!-- 1. 顶部 Airbnb 胶囊搜索条装饰 -->
    <div class="airbnb-search-bar">
      <div class="search-inner">
        <span class="search-icon">🔍</span>
        <div class="search-text">
          <span class="main-text">健康膳食大盘</span>
          <span class="sub-text">第 {{ hasActivePlan ? selectedDay : '?' }} 天 · 全家就餐</span>
        </div>
      </div>
    </div>

    <!-- 页面核心滑动区 -->
    <div class="mobile-content">
      <!-- 2. 未开启计划状态 (Airbnb 风格软圆角引导卡) -->
      <div v-if="!hasActivePlan" class="airbnb-card intro-card">
        <h2 class="display-lg">开启健康膳食周期</h2>
        <p class="body-text">
          智能膳食系统采用 3天轻食 + 1天正常饮食 的科学循环，自动计算每位家庭成员的卡路里和分餐量。
        </p>
        <div class="start-form-mobile">
          <el-select v-model="selectedTemplateId" placeholder="选择膳食周期计划模板" style="width: 100%; margin-bottom: 12px;">
            <el-option
                v-for="tpl in templates"
                :key="tpl.planId"
                :label="tpl.planName + ' (' + tpl.totalDays + '天)'"
                :value="tpl.planId"
            />
          </el-select>
          <button class="btn-airbnb-primary full-width" @click="handleStartPlan" :disabled="!selectedTemplateId">
            启动健康周期
          </button>
        </div>
      </div>

      <!-- 3. 已开启计划状态 -->
      <div v-else>
        <!-- 3.1 当前活跃计划大卡片 -->
        <div class="airbnb-card active-plan-card">
          <span class="caption">当前正在执行的周期</span>
          <h2 class="display-lg">{{ activeTemplate.planName }}</h2>
          <div class="progress-section">
            <span class="progress-num">{{ currentProgress.currentDay }}</span>
            <span class="progress-total">/ {{ activeTemplate.totalDays }} 天</span>
          </div>
          <p class="body-sm">{{ activeTemplate.planDescription }}</p>
        </div>

        <!-- 3.2 周期循环盘 (适配移动端横向滑动的日历圈) -->
        <div class="airbnb-card calendar-card">
          <span class="caption">周期日历 (左右滑动切换)</span>
          <div class="dial-scroll-container">
            <div
                v-for="day in activeTemplate.totalDays"
                :key="day"
                :class="[
                'scroll-day-cell',
                currentProgress.currentDay === day ? 'today-pulse' : '',
                selectedDay === day ? 'selected-cell' : ''
              ]"
                @click="handleDayClick(day)"
            >
              <span class="cell-day-num">{{ day }}</span>
              <span class="cell-day-mode" :class="getDayModeTextClass(day)">
                {{ getDayModeText(day) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 4. 今日就餐食谱明细 -->
        <div class="meal-detail-section">
          <div class="meal-header">
            <h3 class="display-md">第 {{ selectedDay }} 天 膳食方案</h3>
            <span class="badge-mode" :class="getDayModeBadgeClass(selectedDay)">
              {{ getDayModeText(selectedDay) }} 模式
            </span>
          </div>

          <!-- 一日三餐流式展示 -->
          <div v-if="mealDayDetail.hasMeal" class="meal-day-container-mobile">

            <!-- 早餐段 -->
            <div class="meal-period-section-mobile">
              <div class="period-title-row">
                <span class="period-emoji">🍳</span>
                <span class="period-name">早餐 Breakfast</span>
              </div>
              <div v-if="mealDayDetail.breakfast && mealDayDetail.breakfast.hasMeal" class="period-content-mobile">
                <!-- 菜品 -->
                <div class="dish-list-mobile">
                  <div v-for="dish in mealDayDetail.breakfast.dishes" :key="dish.dishId" class="airbnb-dish-card">
                    <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600'"
                         class="dish-img-large" alt="dish cover"/>
                    <div class="dish-meta">
                      <span class="dish-cuisine">{{ dish.cuisineType }}</span>
                      <h4 class="dish-title">{{ dish.dishName }}</h4>
                      <span class="dish-kcal">{{ dish.calories }} kcal/100g</span>
                    </div>
                  </div>
                </div>
                <!-- 分餐 -->
                <div class="airbnb-card portion-card-compact">
                  <span class="caption">分餐建议 (等比例)</span>
                  <div class="portion-list-mobile">
                    <div v-for="item in mealDayDetail.breakfast.portions" :key="item.portionId" class="portion-row">
                      <div class="user-desc">
                        <span class="user-name">{{ item.memberName }}</span>
                        <span class="user-relation">({{ item.memberRelation }})</span>
                      </div>
                      <span class="user-gram">{{ item.recommendWeight }} g</span>
                    </div>
                  </div>
                </div>
                <!-- 打卡 -->
                <div class="period-action-mobile" v-if="selectedDay === currentProgress.currentDay">
                  <button class="btn-airbnb-primary full-width" @click="openFeedbackDialog(mealDayDetail.breakfast)">
                    早餐打卡反馈
                  </button>
                </div>
              </div>
              <div v-else class="airbnb-card empty-period-mobile">
                <p class="body-sm">今日未安排早餐</p>
              </div>
            </div>

            <!-- 午餐段 -->
            <div class="meal-period-section-mobile">
              <div class="period-title-row">
                <span class="period-emoji">🍱</span>
                <span class="period-name">午餐 Lunch</span>
              </div>
              <div v-if="mealDayDetail.lunch && mealDayDetail.lunch.hasMeal" class="period-content-mobile">
                <!-- 菜品 -->
                <div class="dish-list-mobile">
                  <div v-for="dish in mealDayDetail.lunch.dishes" :key="dish.dishId" class="airbnb-dish-card">
                    <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600'"
                         class="dish-img-large" alt="dish cover"/>
                    <div class="dish-meta">
                      <span class="dish-cuisine">{{ dish.cuisineType }}</span>
                      <h4 class="dish-title">{{ dish.dishName }}</h4>
                      <span class="dish-kcal">{{ dish.calories }} kcal/100g</span>
                    </div>
                  </div>
                </div>
                <!-- 分餐 -->
                <div class="airbnb-card portion-card-compact">
                  <span class="caption">分餐建议 (等比例)</span>
                  <div class="portion-list-mobile">
                    <div v-for="item in mealDayDetail.lunch.portions" :key="item.portionId" class="portion-row">
                      <div class="user-desc">
                        <span class="user-name">{{ item.memberName }}</span>
                        <span class="user-relation">({{ item.memberRelation }})</span>
                      </div>
                      <span class="user-gram">{{ item.recommendWeight }} g</span>
                    </div>
                  </div>
                </div>
                <!-- 打卡 -->
                <div class="period-action-mobile" v-if="selectedDay === currentProgress.currentDay">
                  <button class="btn-airbnb-primary full-width" @click="openFeedbackDialog(mealDayDetail.lunch)">
                    午餐打卡反馈
                  </button>
                </div>
              </div>
              <div v-else class="airbnb-card empty-period-mobile">
                <p class="body-sm">今日未安排午餐</p>
              </div>
            </div>

            <!-- 晚餐段 -->
            <div class="meal-period-section-mobile">
              <div class="period-title-row">
                <span class="period-emoji">🥗</span>
                <span class="period-name">晚餐 Dinner</span>
              </div>
              <div v-if="mealDayDetail.dinner && mealDayDetail.dinner.hasMeal" class="period-content-mobile">
                <!-- 菜品 -->
                <div class="dish-list-mobile">
                  <div v-for="dish in mealDayDetail.dinner.dishes" :key="dish.dishId" class="airbnb-dish-card">
                    <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600'"
                         class="dish-img-large" alt="dish cover"/>
                    <div class="dish-meta">
                      <span class="dish-cuisine">{{ dish.cuisineType }}</span>
                      <h4 class="dish-title">{{ dish.dishName }}</h4>
                      <span class="dish-kcal">{{ dish.calories }} kcal/100g</span>
                    </div>
                  </div>
                </div>
                <!-- 分餐 -->
                <div class="airbnb-card portion-card-compact">
                  <span class="caption">分餐建议 (等比例)</span>
                  <div class="portion-list-mobile">
                    <div v-for="item in mealDayDetail.dinner.portions" :key="item.portionId" class="portion-row">
                      <div class="user-desc">
                        <span class="user-name">{{ item.memberName }}</span>
                        <span class="user-relation">({{ item.memberRelation }})</span>
                      </div>
                      <span class="user-gram">{{ item.recommendWeight }} g</span>
                    </div>
                  </div>
                </div>
                <!-- 打卡 -->
                <div class="period-action-mobile" v-if="selectedDay === currentProgress.currentDay">
                  <button class="btn-airbnb-primary full-width" @click="openFeedbackDialog(mealDayDetail.dinner)">
                    晚餐打卡反馈
                  </button>
                </div>
              </div>
              <div v-else class="airbnb-card empty-period-mobile">
                <p class="body-sm">今日未安排晚餐</p>
              </div>
            </div>

            <!-- 食材采购清单 (全天合并) -->
            <div class="airbnb-card daily-grocery-card-mobile">
              <span class="caption">全天合并采购清单</span>
              <p class="body-sm" style="color: #717171; margin-bottom: 12px;">已自动合并加总早、中、晚三餐食材用量。</p>
              <ul v-if="mealDayDetail.dailyGroceries && mealDayDetail.dailyGroceries.length > 0" class="grocery-list-mobile">
                <li v-for="item in mealDayDetail.dailyGroceries" :key="item.ingredientId">
                  <span class="item-name">{{ item.ingredientName }}</span>
                  <span class="item-weight text-bold">{{ item.useAmount }} {{ item.measureUnit }}</span>
                </li>
              </ul>
              <div v-else class="empty-grocery-placeholder-mobile">
                <span class="body-sm">今日无采购数据</span>
              </div>
            </div>

          </div>

          <!-- 未配餐引导状态 -->
          <div v-else class="airbnb-card empty-card">
            <div class="empty-icon">🍳</div>
            <h4 class="display-sm">今日尚未生成食谱</h4>
            <p class="body-text">请在后台生成今天的全家配餐，或者联系做饭人进行饮食调度。</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 移动端打卡弹窗 (Element Plus) -->
    <el-dialog
        v-model="dialogVisible"
        title="全家用餐打卡反馈"
        width="90%"
        class="airbnb-dialog"
        :show-close="false"
    >
      <p class="body-sm" style="color: var(--airbnb-body); line-height: 1.5; margin-bottom: 16px;">
        如果某位成员不喜欢今天这餐的某个菜品，可以在下方进行勾选，系统会在后续自动调整配餐概率。
      </p>

      <div class="feedback-container">
        <div v-for="member in familyMembers" :key="member.profileId" class="feedback-member-block">
          <span class="member-title">{{ member.memberName }}:</span>
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
      </div>

      <template #footer>
        <div class="airbnb-dialog-footer">
          <button class="btn-airbnb-secondary" style="margin-right: 12px; width: 45%;" @click="dialogVisible = false">取消</button>
          <button class="btn-airbnb-primary" style="width: 45%;" @click="handleCompleteMeal">确认打卡</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {inject, onMounted, ref, unref} from 'vue'
import {ElMessage} from 'element-plus'
import request from '../../utils/request'

const activeGroupIdRef = inject<any>('groupId')
const groupId = unref(activeGroupIdRef) || 1

// 页面基础数据定义
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

const getDayModeTextClass = (day: number): string => {
  const mode = getDayMode(day)
  if (mode === 1) return 'text-lime'
  if (mode === 0) return 'text-orange'
  return 'text-red'
}

const getDayModeBadgeClass = (day: number): string => {
  const mode = getDayMode(day)
  if (mode === 1) return 'badge-lime'
  if (mode === 0) return 'badge-orange'
  return 'badge-red'
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
      ElMessage.success('用膳完毕，打卡成功！')
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
   Airbnb 移动端 H5 视觉规范定义
   ========================================== */
.mobile-layout {
  max-width: 500px;
  margin: 0 auto;
  background-color: #fafafa;
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
  color: #222222;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  padding-bottom: 96px; /* 为底部悬浮按钮留出空间 */
}

/* 1. Airbnb 经典顶部胶囊导航 */
.airbnb-search-bar {
  background-color: #ffffff;
  padding: 14px 20px;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #ebebeb;
}

.search-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 999px;
  box-shadow: 0px 3px 10px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebebeb;
}

.search-icon {
  font-size: 18px;
}

.search-text {
  display: flex;
  flex-direction: column;
}

.main-text {
  font-size: 14px;
  font-weight: 600;
  color: #222222;
}

.sub-text {
  font-size: 11px;
  color: #717171;
  margin-top: 1px;
}

.mobile-content {
  padding: 20px;
  flex: 1;
}

/* 2. Airbnb 风格圆角卡片 */
.airbnb-card {
  background-color: #ffffff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0px 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #ebebeb;
  margin-bottom: 20px;
  position: relative;
}

.caption {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #717171;
  text-transform: uppercase;
  display: block;
  margin-bottom: 6px;
}

.display-lg {
  font-size: 22px;
  font-weight: 600;
  color: #222222;
  margin: 0 0 12px 0;
  letter-spacing: -0.02em;
}

.display-md {
  font-size: 18px;
  font-weight: 600;
  color: #222222;
  margin: 0;
  letter-spacing: -0.01em;
}

.display-sm {
  font-size: 16px;
  font-weight: 600;
  color: #222222;
  margin: 8px 0;
}

.body-text {
  font-size: 14px;
  color: #3f3f3f;
  line-height: 1.5;
}

.body-sm {
  font-size: 12px;
  color: #717171;
  line-height: 1.4;
}

/* Airbnb Rausch 经典红色按钮 */
.btn-airbnb-primary {
  background-color: #ff385c;
  color: #ffffff;
  border: none;
  border-radius: 999px; /* 胶囊药丸状 */
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.btn-airbnb-primary:hover {
  background-color: #e00b41;
}

.btn-airbnb-primary:disabled {
  background-color: #ffd1da;
  cursor: not-allowed;
}

.btn-airbnb-secondary {
  background-color: #ffffff;
  color: #222222;
  border: 1px solid #222222;
  border-radius: 999px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.full-width {
  width: 100%;
}

.progress-section {
  display: flex;
  align-items: baseline;
  margin: 12px 0;
}

.progress-num {
  font-size: 40px;
  font-weight: 700;
  color: #222222;
  line-height: 1;
}

.progress-total {
  font-size: 14px;
  color: #717171;
  margin-left: 4px;
}

/* 3.2 左右滑动指示日历 */
.dial-scroll-container {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px 0;
  scroll-behavior: smooth;
}

/* 隐藏横向滚动条 */
.dial-scroll-container::-webkit-scrollbar {
  display: none;
}

.scroll-day-cell {
  flex: 0 0 54px;
  aspect-ratio: 1;
  border-radius: 50%; /* 极具亲和力的圆形 cell */
  border: 1px solid #ebebeb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.15s ease;
}

.selected-cell {
  border-color: #222222 !important;
  background-color: #222222 !important;
}

.selected-cell .cell-day-num {
  color: #ffffff !important;
}

.selected-cell .cell-day-mode {
  color: #ffffff !important;
}

.today-pulse {
  box-shadow: 0 0 0 2px #ff385c;
}

.cell-day-num {
  font-size: 15px;
  font-weight: 600;
  color: #222222;
}

.cell-day-mode {
  font-size: 9px;
  margin-top: 1px;
}

.text-lime {
  color: #29bc9b;
}

.text-orange {
  color: #f5a623;
}

.text-red {
  color: #ff385c;
}

/* 4. 今日食谱段 */
.meal-detail-section {
  margin-top: 24px;
}

.meal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.badge-mode {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.badge-lime {
  background-color: rgba(41, 188, 155, 0.08);
  color: #29bc9b;
}

.badge-orange {
  background-color: rgba(245, 166, 35, 0.08);
  color: #ab570a;
}

.badge-red {
  background-color: rgba(255, 56, 92, 0.08);
  color: #ff385c;
}

/* Tab 切换药丸 */
.pill-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.pill-tab-btn {
  flex: 1;
  background-color: #ffffff;
  color: #484848;
  border: 1px solid #dddddd;
  border-radius: 999px; /* 药丸 */
  padding: 8px 0;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.active-pill {
  background-color: #ff385c; /* Rausch 激活色 */
  color: #ffffff;
  border-color: #ff385c;
  box-shadow: 0px 2px 6px rgba(255, 56, 92, 0.2);
}

/* 菜品大卡片排盘 */
.dish-list-mobile {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.airbnb-dish-card {
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0px 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #ebebeb;
}

.dish-img-large {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.dish-meta {
  padding: 12px 16px;
}

.dish-cuisine {
  font-size: 11px;
  font-weight: 700;
  color: #717171;
  text-transform: uppercase;
}

.dish-title {
  font-size: 16px;
  font-weight: 600;
  color: #222222;
  margin: 2px 0 4px 0;
}

.dish-kcal {
  font-size: 12px;
  color: #717171;
}

/* 采购列表与分餐 */
.grocery-list-mobile {
  list-style: none;
  padding: 0;
  margin: 12px 0 0 0;
}

.grocery-list-mobile li {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #ebebeb;
  font-size: 14px;
}

.grocery-list-mobile li:last-child {
  border-bottom: none;
}

.item-name {
  color: #222222;
}

.item-weight {
  font-weight: 600;
}

.portion-list-mobile {
  margin-top: 12px;
}

.portion-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebebeb;
}

.portion-row:last-child {
  border-bottom: none;
}

.user-desc {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #222222;
}

.user-relation {
  font-size: 11px;
  color: #717171;
}

.user-gram {
  font-size: 15px;
  font-weight: 700;
}

.empty-card {
  text-align: center;
  padding: 32px 16px;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

/* 5. 底部固定打卡大栏 */
.fixed-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  border-top: 1px solid #ebebeb;
  padding: 12px 20px 20px 20px;
  z-index: 110;
  max-width: 500px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.fixed-bottom-bar button {
  width: 100%;
  height: 48px;
}

.shadow-glow {
  box-shadow: 0px 4px 14px rgba(255, 56, 92, 0.35);
}

.feedback-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 350px;
  overflow-y: auto;
}

/* 流式排盘追加样式 */
.meal-period-section-mobile {
  margin-bottom: 24px;
}

.period-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-left: 4px;
}

.period-emoji {
  font-size: 18px;
}

.period-name {
  font-size: 15px;
  font-weight: 700;
  color: #222222;
}

.portion-card-compact {
  padding: 14px 16px;
  margin-top: 12px;
  margin-bottom: 12px;
  border-radius: 10px;
}

.period-action-mobile {
  margin-top: 8px;
  margin-bottom: 16px;
}

.empty-period-mobile {
  text-align: center;
  padding: 16px;
  color: #717171;
  border: 1px dashed #dddddd;
  border-radius: 10px;
}

.daily-grocery-card-mobile {
  border-left: 4px solid #ff385c;
  padding: 16px;
  margin-top: 16px;
}

.empty-grocery-placeholder-mobile {
  text-align: center;
  padding: 16px;
  color: #717171;
}

.feedback-member-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.member-title {
  font-size: 14px;
  font-weight: 600;
  color: #222222;
}
</style>
