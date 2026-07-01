<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useRoleStore} from '../../store/role'
import request from '../../utils/request'
import {showSuccessToast} from 'vant'

const router = useRouter()
const roleStore = useRoleStore()
const loading = ref(false)

// 当前选中的就餐餐次 (1-早餐, 2-午餐, 3-晚餐)
const activePeriod = ref(2) // 默认选中午餐

// 菜品大类数据与烹饪步骤
const mockChefDishes = {
  1: [ // 早餐
    {
      id: 101,
      name: '水煮蛋',
      steps: [
        {num: 1, detail: '将鸡蛋表面清洗干净，放入小锅中。'},
        {num: 2, detail: '加入冷水没过鸡蛋，大火烧开转中火煮8分钟。'},
        {num: 3, detail: '捞出后立即投入凉水中浸泡，剥壳切半。'}
      ],
      completed: false
    },
    {
      id: 102,
      name: '全麦面包',
      steps: [
        {num: 1, detail: '多士炉预热，放入全麦切片面包。'},
        {num: 2, detail: '中低档烘烤1-2分钟至表面微微焦黄酥脆。'}
      ],
      completed: false
    }
  ],
  2: [ // 午餐
    {
      id: 104,
      name: '香煎鸡胸肉',
      steps: [
        {num: 1, detail: '鸡胸肉横向片成两半，用刀背轻轻拍松。'},
        {num: 2, detail: '加少许盐、黑胡椒粉、料酒、生抽及淀粉腌制15分钟。'},
        {num: 3, detail: '平底锅刷薄油烧热，下入鸡胸肉大火双面各煎1分钟锁住水分。'},
        {num: 4, detail: '转小火盖盖焖煎2分钟，出锅切条即可。'}
      ],
      completed: false
    },
    {
      id: 105,
      name: '清炒西兰花',
      steps: [
        {num: 1, detail: '西兰花掰成小朵，用盐水浸泡洗净。'},
        {num: 2, detail: '锅中烧水加少许盐，下西兰花焯水40秒捞出过凉水。'},
        {num: 3, detail: '热锅冷油，下蒜片炒香，倒入西兰花大火翻炒1分钟。'},
        {num: 4, detail: '加适量盐、鸡精快速翻匀出锅。'}
      ],
      completed: false
    }
  ],
  3: [ // 晚餐
    {
      id: 107,
      name: '清蒸鲈鱼',
      steps: [
        {num: 1, detail: '鲈鱼去鳞去内脏洗净，鱼身两面各划三刀。'},
        {num: 2, detail: '用少许盐抹匀鱼身，盘底垫葱段姜片，鱼肚塞姜丝。'},
        {num: 3, detail: '蒸锅水开后，大火入锅蒸8分钟，熄火再焖2分钟出锅。'},
        {num: 4, detail: '倒掉盘中蒸鱼水，铺上新鲜葱丝红椒丝，淋上蒸鱼豉油，泼一勺热油。'}
      ],
      completed: false
    }
  ]
}

const mealDishes = ref<any[]>([])
const selectedDishIndex = ref(0)
const hasPublishedToday = ref(true)
const currentMealPlanId = ref<number | null>(null)

const fetchChefMenu = async () => {
  if (roleStore.token?.startsWith('mock-')) {
    mealDishes.value = mockChefDishes[activePeriod.value as keyof typeof mockChefDishes] || []
    selectedDishIndex.value = 0
    return
  }

  loading.value = true
  try {
    const todayStr = new Date().toISOString().split('T')[0]
    // 获取已发布食谱
    const res: any = await request.get(`/api/diet/family-meal-plan/current-day?groupId=${roleStore.groupId}&date=${todayStr}`)

    if (res && res.hasMeal) {
      hasPublishedToday.value = true

      // 根据餐次选择对应的配餐数据 (1-早餐, 2-午餐, 3-晚餐)
      let periodMeal: any = null
      if (activePeriod.value === 1) periodMeal = res.breakfast
      else if (activePeriod.value === 2) periodMeal = res.lunch
      else if (activePeriod.value === 3) periodMeal = res.dinner

      if (periodMeal && periodMeal.hasMeal) {
        currentMealPlanId.value = periodMeal.mealPlan?.mealPlanId || null
        const dishesForPeriod = periodMeal.dishes || []

        mealDishes.value = dishesForPeriod.map((d: any) => {
          return {
            id: d.branchId, // 关联做法分支 ID 作为打卡的主键 ID
            name: d.branchName,
            steps: d.steps && d.steps.length > 0
                ? d.steps.map((s: any) => ({num: s.stepNum, detail: s.stepDetail}))
                : (mockChefDishes[activePeriod.value as keyof typeof mockChefDishes]?.find(m => m.id === d.dishId)?.steps || [
                  {num: 1, detail: '洗净主料，热锅冷油进行烹饪。'},
                  {num: 2, detail: '加入辅料及少许食盐、酱油进行调味。'},
                  {num: 3, detail: '熟透后盛出装盘。'}
                ]),
            completed: d.cookFlag === 1
          }
        })
      } else {
        mealDishes.value = []
        currentMealPlanId.value = null
      }
    } else {
      hasPublishedToday.value = false
      mealDishes.value = []
      currentMealPlanId.value = null
    }
  } catch (err) {
    console.error('拉取已发布配餐失败', err)
    mealDishes.value = mockChefDishes[activePeriod.value as keyof typeof mockChefDishes] || []
  } finally {
    loading.value = false
    selectedDishIndex.value = 0
  }
}

// 更改餐次 Tab
const handlePeriodChange = () => {
  fetchChefMenu()
}

// 完成一道菜的烹饪
const completeDish = async (idx: number) => {
  const dish = mealDishes.value[idx]
  if (currentMealPlanId.value && !roleStore.token?.startsWith('mock-')) {
    try {
      await request.post(`/api/diet/family-meal-plan/cook-dish?mealPlanId=${currentMealPlanId.value}&branchId=${dish.id}&cookFlag=1`)
    } catch (err) {
      console.error('保存做菜完成状态失败', err)
    }
  }
  dish.completed = true
  showSuccessToast(`${dish.name} 烹饪完毕！`)

  // 检查是否这顿饭所有的菜都做完了
  const allDone = mealDishes.value.every(d => d.completed)
  if (allDone) {
    showSuccessToast({
      message: '这顿饭已全部做完，开饭啦！',
      duration: 3000
    })
  }
}

onMounted(() => {
  fetchChefMenu()
})
</script>

<template>
  <div class="chef-home">
    <!-- 餐次切换 Slider -->
    <div class="period-tabs card-shadow">
      <van-tabs v-model:active="activePeriod" @change="handlePeriodChange" color="var(--primary-color)">
        <van-tab :name="1" title="🥞 准备早餐"/>
        <van-tab :name="2" title="🥗 准备午餐"/>
        <van-tab :name="3" title="🐟 准备晚餐"/>
      </van-tabs>
    </div>

    <!-- 未发布配餐提示 -->
    <div v-if="!hasPublishedToday" class="unpublish-card card-shadow mt-16">
      <div class="tips-icon">📝</div>
      <h3>今日尚未发布家庭食谱</h3>
      <p>发布食谱后，即可在此查看烹饪指导和采购买菜明细。</p>
      <van-button
          type="primary"
          round
          icon="plus"
          @click="router.push('/chef/recipe-generator')"
      >
        去智能生成配餐
      </van-button>
    </div>

    <!-- 烹饪向导区域 -->
    <div v-else class="cooking-helper mt-16">
      <div class="section-title">🍳 今日掌勺菜品</div>

      <!-- 菜品选择侧边栏/滑动条 -->
      <div class="dish-tabs">
        <div
            v-for="(dish, idx) in mealDishes"
            :key="dish.id"
            :class="['dish-tab-item', { active: selectedDishIndex === idx, done: dish.completed }]"
            @click="selectedDishIndex = idx"
        >
          <span class="d-text">{{ dish.name }}</span>
          <span class="d-status" v-if="dish.completed">已做好</span>
          <span class="d-status" v-else>烹饪中</span>
        </div>
      </div>

      <!-- 步骤流程展示 -->
      <div v-if="mealDishes.length > 0" class="step-guide card-shadow mt-12">
        <div class="guide-header">
          <h3>{{ mealDishes[selectedDishIndex].name }} <span>的步骤分解</span></h3>
          <van-tag type="primary" size="medium" v-if="mealDishes[selectedDishIndex].completed">已做完</van-tag>
        </div>

        <div class="steps-flow">
          <div
              v-for="(step, sIdx) in mealDishes[selectedDishIndex].steps"
              :key="sIdx"
              class="step-node"
          >
            <div class="step-num">{{ step.num }}</div>
            <div class="step-content">
              <p>{{ step.detail }}</p>
            </div>
          </div>
        </div>

        <div class="step-finish-action mt-20" v-if="!mealDishes[selectedDishIndex].completed">
          <van-button
              type="success"
              block
              round
              icon="fire-o"
              @click="completeDish(selectedDishIndex)"
          >
            我已做好这道菜 🍲
          </van-button>
        </div>
      </div>

      <p class="empty-tip" v-else>本餐次暂无已规划的菜品</p>
    </div>
  </div>
</template>

<style scoped>
.chef-home {
  padding-bottom: 24px;
}

.mt-16 {
  margin-top: 16px;
}

.mt-12 {
  margin-top: 12px;
}

.mt-20 {
  margin-top: 20px;
}

.period-tabs {
  background: white;
  border-radius: 16px;
  overflow: hidden;
}

/* 未发布界面 */
.unpublish-card {
  background: white;
  border-radius: 16px;
  padding: 40px 20px;
  text-align: center;
}

.tips-icon {
  font-size: 50px;
  margin-bottom: 12px;
}

.unpublish-card h3 {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 6px 0;
}

.unpublish-card p {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-bottom: 20px;
}

/* 烹饪向导 */
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 15px 0 10px 4px;
  text-align: left;
}

.dish-tabs {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 2px 4px;
}

.dish-tab-item {
  flex: 1;
  background: white;
  padding: 12px 14px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  border: 1px solid transparent;
  cursor: pointer;
  min-width: 100px;
}

.dish-tab-item.active {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.15);
}

.dish-tab-item.done {
  background: #e8f8f0;
}

.d-text {
  font-size: 13px;
  font-weight: 600;
  color: #2c3e50;
}

.d-status {
  font-size: 9px;
  color: var(--text-color-secondary);
}

.dish-tab-item.active .d-status {
  color: var(--primary-color);
  font-weight: 600;
}

.dish-tab-item.done .d-status {
  color: var(--primary-active);
  font-weight: 600;
}

/* 步骤详情卡 */
.step-guide {
  background: white;
  border-radius: 16px;
  padding: 18px;
  text-align: left;
}

.guide-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 10px;
  margin-bottom: 16px;
}

.guide-header h3 {
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.guide-header h3 span {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-color-secondary);
}

.steps-flow {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-node {
  display: flex;
  gap: 12px;
}

.step-num {
  width: 20px;
  height: 20px;
  background-color: var(--primary-color);
  color: white;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  box-shadow: 0 2px 6px rgba(46, 204, 113, 0.3);
}

.step-content {
  flex: 1;
}

.step-content p {
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-color-primary);
}

.empty-tip {
  font-size: 12px;
  color: var(--text-color-secondary);
  text-align: center;
  padding: 40px 0;
}
</style>
