<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
import {useRoleStore} from '../../store/role'
import request from '../../utils/request'
import {showToast} from 'vant'

const roleStore = useRoleStore()
const loading = ref(false)

const meals = ref<any[]>([])
const dailyTargetCalories = ref(1500)
const plannedCalories = ref(0)

// 打卡相关状态
const reasonOptions = ['外出就餐', '工作加班', '身体不适', '胃口欠佳', '其他原因']
const showReasonSheet = ref(false)
const selectedReason = ref('')
const skipNote = ref('')
const currentMealPlanId = ref<number | null>(null)
const feedbackMap = ref<Record<number, { eatStatus: number; skipReason?: string; skipNote?: string }>>({})

const getFeedback = (mealPlanId: number) => {
  return feedbackMap.value[mealPlanId]
}

const fetchData = async () => {
  loading.value = true
  try {
    const todayStr = new Date().toISOString().split('T')[0]
    const res: any = await request.post('/api/diet/family-meal-plan/portions', {
      groupId: roleStore.groupId,
      profileId: roleStore.profileId,
      date: todayStr
    })

    if (res && res.length > 0) {
      meals.value = res
      let totalCal = 0
      res.forEach((meal: any) => {
        meal.dishes?.forEach((d: any) => {
          totalCal += Number(d.calories || 0)
        })
      })
      plannedCalories.value = Math.round(totalCal)
    } else {
      meals.value = []
      plannedCalories.value = 0
    }

    // 拉取打卡状态
    try {
      const feedbackList: any = await request.get(`/api/diet/meal-feedback/list-today?profileId=${roleStore.profileId}`)
      if (feedbackList && feedbackList.length > 0) {
        feedbackList.forEach((fb: any) => {
          feedbackMap.value[fb.mealPlanId] = {
            eatStatus: fb.eatStatus,
            skipReason: fb.skipReason,
            skipNote: fb.skipNote
          }
        })
      }
    } catch (err) {
      console.error('获取打卡反馈历史失败', err)
    }

    if (roleStore.profileId) {
      const profileRes: any = await request.get(`/api/diet/user-health-profile/${roleStore.profileId}`)
      const profile = profileRes && profileRes.data ? profileRes.data : profileRes
      if (profile && profile.dailyTargetCalories) {
        dailyTargetCalories.value = Math.round(profile.dailyTargetCalories)
      }
    }
  } catch (error) {
    console.error('获取分餐数据失败', error)
    meals.value = []
    plannedCalories.value = 0
  } finally {
    loading.value = false
  }
}

// 打卡点击
const handleCheckIn = async (mealPlanId: number, eatStatus: number) => {
  if (eatStatus === 1) {
    try {
      if (!roleStore.token?.startsWith('mock-')) {
        await request.post('/api/diet/meal-feedback/save', {
          mealPlanId,
          profileId: roleStore.profileId,
          eatStatus: 1
        })
      }
      feedbackMap.value[mealPlanId] = {eatStatus: 1}
      showToast('已餐打卡成功！')
    } catch (err) {
      console.error(err)
      feedbackMap.value[mealPlanId] = {eatStatus: 1}
      showToast('已餐打卡成功')
    }
  } else {
    currentMealPlanId.value = mealPlanId
    selectedReason.value = ''
    skipNote.value = ''
    showReasonSheet.value = true
  }
}

// 提交未餐原因
const submitSkipFeedback = async () => {
  if (!selectedReason.value) {
    showToast('请选择未就餐原因')
    return
  }
  const mealPlanId = currentMealPlanId.value
  if (!mealPlanId) return

  try {
    if (!roleStore.token?.startsWith('mock-')) {
      await request.post('/api/diet/meal-feedback/save', {
        mealPlanId,
        profileId: roleStore.profileId,
        eatStatus: 2,
        skipReason: selectedReason.value,
        skipNote: skipNote.value
      })
    }
    feedbackMap.value[mealPlanId] = {
      eatStatus: 2,
      skipReason: selectedReason.value,
      skipNote: skipNote.value
    }
    showReasonSheet.value = false
    showToast('未餐打卡提交成功！')
  } catch (err) {
    console.error(err)
    feedbackMap.value[mealPlanId] = {
      eatStatus: 2,
      skipReason: selectedReason.value,
      skipNote: skipNote.value
    }
    showReasonSheet.value = false
    showToast('未餐反馈提交成功')
  }
}

// 修改打卡
const handleCancelCheckIn = (mealPlanId: number) => {
  delete feedbackMap.value[mealPlanId]
  showToast('已重置打卡，请重新选择')
}

// 侦听家庭组与个人档案切换
watch(
    () => [roleStore.groupId, roleStore.profileId],
    () => {
      fetchData()
    }
)

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="diner-home">
    <!-- 头部健康进度卡片 -->
    <div class="health-summary-card gradient-header card-shadow">
      <div class="summary-top">
        <div class="left">
          <p class="summary-label">今日推荐摄入</p>
          <h2 class="summary-value">{{ dailyTargetCalories }} <span>kcal</span></h2>
        </div>
        <div class="right">
          <p class="summary-label">已规划餐盘</p>
          <h2 class="summary-value">{{ plannedCalories }} <span>kcal</span></h2>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="progress-bar-wrapper">
        <van-progress
            :percentage="Math.min(100, Math.round((plannedCalories / dailyTargetCalories) * 100))"
            stroke-width="8"
            color="#ffffff"
            track-color="rgba(255,255,255,0.3)"
            :show-pivot="false"
        />
        <div class="progress-labels">
          <span>0%</span>
          <span>规划占比: {{ Math.round((plannedCalories / dailyTargetCalories) * 100) }}%</span>
          <span>100%</span>
        </div>
      </div>
    </div>

    <!-- 食谱推荐列表 -->
    <div class="meal-list">
      <div class="section-title">今日定制食谱</div>

      <van-skeleton :row="10" :loading="loading">
        <div v-for="(meal, index) in meals" :key="index" class="meal-group card-shadow">
          <div class="meal-header">
            <span class="meal-title">{{ meal.periodName }}</span>
            <van-tag type="success" plain>{{ meal.dietModeName || '轻食减脂' }}</van-tag>
          </div>

          <div class="dish-list">
            <div v-for="dish in meal.dishes" :key="dish.id" class="dish-item">
              <div class="dish-main">
                <div class="dish-meta">
                  <span class="dish-name">{{ dish.name }}</span>
                  <span class="dish-portion">推荐分量: {{ dish.portion }}</span>
                </div>
                <span class="dish-calories">+{{ dish.calories }} kcal</span>
              </div>

              <div class="dish-sub" v-if="dish.note || dish.protein">
                <span class="dish-note">{{ dish.note || '精心搭配，健康饮食' }}</span>
                <span class="dish-nutrients">蛋:{{ dish.protein }}g | 脂:{{ dish.fat }}g | 碳:{{ dish.carbs }}g</span>
              </div>
            </div>
          </div>

          <!-- 就餐打卡反馈区 -->
          <div class="meal-feedback-bar">
            <div v-if="!getFeedback(meal.mealPlanId)" class="feedback-actions">
              <span class="feedback-label">就餐反馈：</span>
              <van-button
                  type="success"
                  size="small"
                  icon="success"
                  class="feed-btn"
                  @click="handleCheckIn(meal.mealPlanId, 1)"
              >
                已餐
              </van-button>
              <van-button
                  type="danger"
                  size="small"
                  icon="cross"
                  class="feed-btn"
                  @click="handleCheckIn(meal.mealPlanId, 2)"
              >
                未餐
              </van-button>
            </div>

            <div v-else class="feedback-result">
              <div class="result-info">
                <van-icon
                    :name="getFeedback(meal.mealPlanId).eatStatus === 1 ? 'checked' : 'clear'"
                    :color="getFeedback(meal.mealPlanId).eatStatus === 1 ? '#2ecc71' : '#e74c3c'"
                    size="18"
                />
                <span class="result-text">
                  打卡结果：{{ getFeedback(meal.mealPlanId).eatStatus === 1 ? '已按计划就餐' : '未就餐' }}
                  <span v-if="getFeedback(meal.mealPlanId).eatStatus === 2" class="reason-tag">
                    ({{ getFeedback(meal.mealPlanId).skipReason }})
                  </span>
                </span>
              </div>
              <van-button
                  plain
                  type="primary"
                  size="mini"
                  class="re-feed-btn"
                  @click="handleCancelCheckIn(meal.mealPlanId)"
              >
                修改
              </van-button>
            </div>
          </div>
        </div>
      </van-skeleton>
    </div>

    <!-- 未餐原因强制收集 ActionSheet -->
    <van-action-sheet
        v-model:show="showReasonSheet"
        title="反馈未就餐原因"
        safe-area-inset-bottom
    >
      <div class="reason-sheet-content">
        <p class="reason-tip">统计家庭饮食的依从度需要，请勾选并提交未就餐的真实原因：</p>

        <van-radio-group v-model="selectedReason" class="reason-group">
          <van-cell-group inset>
            <van-cell
                v-for="r in reasonOptions"
                :key="r"
                :title="r"
                clickable
                @click="selectedReason = r"
            >
              <template #right-icon>
                <van-radio :name="r" checked-color="#2ecc71"/>
              </template>
            </van-cell>
          </van-cell-group>
        </van-radio-group>

        <!-- 详细备注输入 -->
        <div class="reason-note-input">
          <van-field
              v-model="skipNote"
              rows="2"
              autosize
              label="备注说明"
              type="textarea"
              maxlength="100"
              placeholder="请填写更具体的未用餐备注（选填）"
              show-word-limit
          />
        </div>

        <div class="submit-reason-box">
          <van-button
              type="success"
              block
              class="submit-reason-btn"
              @click="submitSkipFeedback"
          >
            确认提交反馈
          </van-button>
        </div>
      </div>
    </van-action-sheet>
  </div>
</template>

<style scoped>
.diner-home {
  padding-bottom: 24px;
}

/* 顶部汇总卡片 */
.health-summary-card {
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 20px;
}

.summary-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.summary-label {
  font-size: 11px;
  opacity: 0.85;
  margin-bottom: 4px;
  font-weight: 500;
}

.summary-value {
  font-size: 24px;
  font-weight: 800;
  margin: 0;
}

.summary-value span {
  font-size: 13px;
  font-weight: 400;
}

.progress-bar-wrapper {
  margin-top: 10px;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  opacity: 0.8;
  margin-top: 6px;
}

/* 餐食列表 */
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 15px 0 10px 4px;
}

.meal-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.meal-group {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  padding: 16px;
}

.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 10px;
  margin-bottom: 12px;
}

.meal-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color-primary);
}

/* 菜品单项 */
.dish-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dish-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dish-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dish-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.dish-portion {
  font-size: 11px;
  color: #2ecc71;
  font-weight: 500;
}

.dish-calories {
  font-size: 13px;
  font-weight: 700;
  color: #e67e22;
}

.dish-sub {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-color-secondary);
}

.dish-note {
  font-style: italic;
}

/* 打卡样式 */
.meal-feedback-bar {
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-color);
}

.feedback-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feedback-label {
  font-size: 11px;
  font-weight: bold;
  color: #7f8c8d;
}

.feed-btn {
  padding: 0 12px;
  height: 26px !important;
  border-radius: 6px !important;
  font-size: 10px !important;
  font-weight: bold;
}

.feedback-result {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
  padding: 6px 10px;
  border-radius: 8px;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-text {
  font-size: 11px;
  font-weight: 600;
  color: #2c3e50;
}

.reason-tag {
  color: #e74c3c;
  font-size: 10px;
}

.re-feed-btn {
  border-radius: 4px !important;
  font-size: 9px !important;
  padding: 0 6px !important;
  height: 20px !important;
}

.reason-sheet-content {
  padding: 16px;
}

.reason-tip {
  font-size: 11px;
  color: #7f8c8d;
  line-height: 1.5;
  margin-bottom: 12px;
  padding: 0 4px;
}

.reason-group {
  margin-bottom: 12px;
}

.reason-note-input {
  margin-bottom: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
}

.submit-reason-box {
  padding: 0 4px 12px 4px;
}

.submit-reason-btn {
  height: 40px !important;
  border-radius: 8px !important;
  font-weight: bold !important;
}
</style>
