<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">MEAL PLANNING CENTER</span>
      <h1 class="display-lg">家庭联合配餐</h1>
    </div>

    <!-- 1. 配置项：冷却天数与做饭人擅长 (并排两个 Color-Blocks) -->
    <div class="config-grid">
      <!-- 1.1 冷却避重天数配置 (Block-Lime) -->
      <div class="color-block color-block-lime config-card">
        <span class="caption">COOLDOWN SETTING</span>
        <h3 class="card-title">菜系冷却避重天数</h3>
        <p class="body-sm" style="margin: var(--spacing-sm) 0">
          设置在生成推荐时，相同菜系在多少天内不重复出现。强力防止家人们“连续吃辣”或“连着吃某菜系”产生厌倦。
        </p>
        <div class="input-inline">
          <input
              type="number"
              v-model="cooldownDays"
              class="input-text"
              style="width: 100px"
              min="0"
              max="15"
          />
          <button class="btn-primary" @click="handleSaveCooldown">
            保存配置
          </button>
        </div>
      </div>

      <!-- 1.2 做饭人擅长具体菜品管理 (Block-Cream) -->
      <div class="color-block color-block-cream config-card">
        <span class="caption">COOK SKILLS</span>
        <h3 class="card-title">我的擅长菜品维护</h3>
        <p class="body-sm" style="margin: var(--spacing-sm) 0">
          标记您自己拿手的特色家常菜。系统在配餐推荐时，会自动增加这几道菜的出现概率，
          且会动态推算您的“擅长菜系”并加成对应菜品。
        </p>
        <button class="btn-primary" @click="openSkilledModal">
          维护擅长菜品
        </button>
      </div>
    </div>

    <!-- 2. 联合配餐台 -->
    <div class="color-block color-block-navy planner-main-box">
      <span class="caption" style="color: var(--primary-hover)">DIET GENERATOR</span>
      <h2 class="display-lg" style="margin-bottom: var(--spacing-md)">智能膳食发生器</h2>

      <div class="planner-form">
        <!-- 用餐日期 -->
        <div class="form-group">
          <label class="caption">用餐日期</label>
          <el-date-picker
              v-model="targetDate"
              type="date"
              placeholder="选择配餐日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledPastDates"
              style="width: 180px"
          />
        </div>

        <!-- 用餐时段 -->
        <div class="form-group">
          <label class="caption">用餐餐次</label>
          <el-select v-model="mealPeriod" style="width: 120px">
            <el-option :value="1" label="早餐"/>
            <el-option :value="2" label="午餐"/>
            <el-option :value="3" label="晚餐"/>
          </el-select>
        </div>

        <!-- 膳食建议模式 -->
        <div class="form-group">
          <label class="caption">就餐建议模式</label>
          <el-select v-model="dietMode" style="width: 140px">
            <el-option :value="0" label="正常饮食"/>
            <el-option :value="1" label="轻食减脂"/>
            <el-option :value="2" label="放纵餐"/>
          </el-select>
        </div>

        <!-- 生成大按钮 -->
        <button class="btn-primary" @click="handleGenerateRecommend"
                style="align-self: flex-end;">
          智能推荐
        </button>
      </div>
    </div>

    <!-- 3. 生成预览展现区域 -->
    <div v-if="previewDishes.length > 0" class="preview-results section-gap">
      <h3 class="headline" style="margin-bottom: var(--spacing-md)">联合配餐预览（尚未确定）</h3>

      <!-- 推荐的菜品列表 -->
      <div class="preview-dishes-grid">
        <div
            v-for="dish in previewDishes"
            :key="dish.dishId"
            class="dish-preview-card hairline-border"
        >
          <!-- 想吃日期高亮标识 -->
          <div v-if="dish.wishMatch" class="wish-banner caption">
            ★ 家人今日想吃
          </div>

          <img :src="dish.previewUrl || 'https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=500'"
               class="dish-preview-img" alt="dish cover"/>
          <div class="dish-preview-info">
            <div style="display: flex; justify-content: space-between; align-items: baseline">
              <span class="caption">{{ dish.cuisineType }}</span>
              <!-- 拿手菜勋章 -->
              <span v-if="dish.signatureFlag === 1" class="eyebrow sig-badge">★ 拿手菜</span>
            </div>
            <h4 class="card-title">{{ dish.dishName }}</h4>
            <p class="body-sm">热量：{{ dish.calories }} kcal/100g | 蛋白质：{{ dish.protein }}g</p>
          </div>
        </div>
      </div>

      <!-- 分餐与采购预览 -->
      <div class="grocery-portion-grid" style="margin-top: var(--spacing-lg)">
        <!-- 预计分餐建议 (Block-Mint) -->
        <div class="color-block color-block-mint info-preview-box">
          <span class="caption">PORTION ESTIMATION</span>
          <h4 class="card-title">预计家人单人分餐克数</h4>
          <div class="portion-list">
            <div v-for="item in calculatedPortions" :key="item.profileId + '_' + item.dishId" class="portion-item">
              <div class="portion-user">
                <span class="body-text text-bold">{{ item.memberName }}</span>
                <span class="body-sm" style="color: var(--ink-subtle)">吃【{{ item.dishName }}】</span>
              </div>
              <div class="portion-gram eyebrow">
                {{ item.recommendWeight }} g
              </div>
            </div>
          </div>
        </div>

        <!-- 预计采购清单 (Block-Cream) -->
        <div class="color-block color-block-cream info-preview-box">
          <span class="caption">GROCERY PREVIEW</span>
          <h4 class="card-title">本餐累计食材原料采购单</h4>
          <ul class="grocery-ul">
            <li v-for="item in calculatedGroceries" :key="item.ingredientId">
              <span class="body-text">{{ item.ingredientName }}</span>
              <span class="eyebrow text-bold">{{ item.useAmount }} {{ item.measureUnit }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- 确认发布大药丸 -->
      <div class="publish-action">
        <button class="btn-primary" @click="handlePublishMealPlan" style="font-size: 22px; padding: 12px 36px">
          确认发布并锁定今日膳食安排
        </button>
      </div>
    </div>

    <!-- 4. 擅长菜品维护模态框 (Element Plus Dialog) -->
    <el-dialog
        v-model="skilledModalVisible"
        title="维护我的擅长菜品"
        width="500px"
        :show-close="false"
        class="custom-dialog"
    >
      <div class="skilled-dishes-list">
        <div
            v-for="dish in allDishes"
            :key="dish.dishId"
            class="skilled-dish-checkbox-row"
        >
          <span class="body-text text-bold">{{ dish.dishName }}</span>
          <el-checkbox
              v-model="dish.isSkilled"
              :true-value="1"
              :false-value="0"
              @change="handleToggleSkilled(dish)"
          >
            擅长
          </el-checkbox>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <button class="btn-primary" @click="skilledModalVisible = false">
            关闭
          </button>
        </span>
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
const cookUserId = inject<number>('cookUserId', 1)

// 冷却天数
const cooldownDays = ref(3)

// 擅长菜品维护
const skilledModalVisible = ref(false)
const allDishes = ref<any[]>([])

// 配餐表单
const targetDate = ref(new Date().toISOString().split('T')[0])
const mealPeriod = ref(2) // 默认午餐
const dietMode = ref(1)   // 默认轻食

// 生成预览
const previewDishes = ref<any[]>([])
const calculatedPortions = ref<any[]>([])
const calculatedGroceries = ref<any[]>([])

// 禁止选择过去日期
const disabledPastDates = (date: Date) => {
  return date.getTime() < Date.now() - 8.64e7
}

// 载入冷却天数
const loadCooldownDays = async () => {
  try {
    const res: any = await request.get(`/api/meal/cooldown?groupId=${groupId}`)
    if (res !== undefined && res !== null) {
      cooldownDays.value = Number(res)
    }
  } catch (e) {
  }
}

// 保存冷却天数配置
const handleSaveCooldown = async () => {
  try {
    await request.post(`/api/meal/cooldown?groupId=${groupId}&cooldownDays=${cooldownDays.value}`)
    ElMessage.success('避重天数保存成功，下次智能配餐将严格遵循！')
  } catch (e) {
    ElMessage.error('避重天数保存失败')
  }
}

// 载入组配置并推断当前就餐模式
const loadGroupConfig = async () => {
  try {
    const res: any = await request.get(`/api/plan/current?groupId=${groupId}`)
    if (res && res.progress) {
      const currentDay = res.progress.currentDay
      const cycle = currentDay % 5
      if (cycle === 1 || cycle === 2 || cycle === 3) dietMode.value = 1 // 轻食
      else if (cycle === 4) dietMode.value = 0 // 正常
      else dietMode.value = 2 // 放纵
    }
  } catch (e) {
  }
}

// 载入广场菜品（以勾选擅长）
const loadDishes = async () => {
  try {
    allDishes.value = await request.get(`/api/dish/list?userId=${cookUserId}&groupId=${groupId}`)
  } catch (e) {
  }
}

const openSkilledModal = () => {
  loadDishes()
  skilledModalVisible.value = true
}

const handleToggleSkilled = async (dish: any) => {
  try {
    await request.post('/api/dish/skilled', {
      userId: cookUserId,
      dishId: dish.dishId,
      isSkilled: dish.isSkilled
    })
    ElMessage.success(dish.isSkilled === 1 ? '添加擅长成功！' : '取消擅长成功！')
  } catch (e) {
  }
}

// 智能生成预览推荐
const handleGenerateRecommend = async () => {
  try {
    const recommendedList: any[] = await request.get(
        `/api/meal/recommend?groupId=${groupId}&targetDate=${targetDate.value}&mealPeriod=${mealPeriod.value}&dietMode=${dietMode.value}&limit=2`
    )

    if (recommendedList.length === 0) {
      ElMessage.warning('候选池中无匹配菜谱，请调整就餐建议模式！')
      return;
    }

    // 设定想吃高亮
    recommendedList.forEach(dish => {
      dish.wishMatch = dish.dishId === 3 || dish.dishId === 4;
    })

    previewDishes.value = recommendedList

    await loadCalculatedDetails(recommendedList)

    ElMessage.success('推荐配餐生成成功！您可查看下方预计的分餐量及采购单。')
  } catch (e) {
  }
}

const loadCalculatedDetails = async (dishes: any[]) => {
  const members: any[] = await request.get(`/api/profile/list?groupId=${groupId}`)
  const dishCount = dishes.length
  const periodRatio = (mealPeriod.value === 1 || mealPeriod.value === 3) ? 0.3 : 0.4

  const portions: any[] = []
  const dishTotalWeights: Record<number, number> = {}
  dishes.forEach(d => {
    dishTotalWeights[d.dishId] = 0
  })

  members.forEach(p => {
    if (!p.dailyTargetCalories) return
    const budget = p.dailyTargetCalories * periodRatio
    dishes.forEach(d => {
      const w = (budget * 100) / (d.calories * dishCount)
      const weight = Math.round(w * 100) / 100
      portions.push({
        profileId: p.profileId,
        memberName: p.memberName,
        dishId: d.dishId,
        dishName: d.dishName,
        recommendWeight: weight
      })
      dishTotalWeights[d.dishId] += weight
    })
  })
  calculatedPortions.value = portions

  const groceryMap: Record<string, { name: string, amount: number, unit: string }> = {}

  for (const d of dishes) {
    const totalW = dishTotalWeights[d.dishId]
    let recipeSum = 300
    let mockIngredients: any[] = []
    if (d.dishId === 1) {
      mockIngredients = [
        {name: '鸡胸肉', amount: 200, unit: 'g'},
        {name: '西兰花', amount: 50, unit: 'g'}
      ]
      recipeSum = 250
    } else if (d.dishId === 3) {
      mockIngredients = [
        {name: '西红柿', amount: 200, unit: 'g'},
        {name: '鸡蛋', amount: 120, unit: 'g'}
      ]
      recipeSum = 320
    } else if (d.dishId === 4) {
      mockIngredients = [
        {name: '豆腐', amount: 300, unit: 'g'},
        {name: '猪里脊肉', amount: 50, unit: 'g'}
      ]
      recipeSum = 350
    } else {
      mockIngredients = [
        {name: '牛里脊', amount: 150, unit: 'g'},
        {name: '西兰花', amount: 150, unit: 'g'}
      ]
      recipeSum = 300
    }

    const scale = totalW / recipeSum
    mockIngredients.forEach(ing => {
      const needed = Math.round(ing.amount * scale * 100) / 100
      if (groceryMap[ing.name]) {
        groceryMap[ing.name].amount += needed
      } else {
        groceryMap[ing.name] = {name: ing.name, amount: needed, unit: ing.unit}
      }
    })
  }

  calculatedGroceries.value = Object.values(groceryMap).map((item: any) => ({
    ingredientId: item.name,
    ingredientName: item.name,
    useAmount: Math.round(item.amount * 100) / 100,
    measureUnit: item.unit
  }))
}

const handlePublishMealPlan = async () => {
  if (previewDishes.value.length === 0) return

  const dishIds = previewDishes.value.map(d => d.dishId)
  try {
    const res = await request.post('/api/meal/save', {
      groupId: groupId,
      targetDate: targetDate.value,
      mealPeriod: mealPeriod.value,
      dietMode: dietMode.value,
      dishIds: dishIds
    })
    if (res) {
      ElMessage.success('膳食计划已成功发布，分餐卡片与采购单已同步生成并锁定！')
      previewDishes.value = []
    }
  } catch (e) {
  }
}

onMounted(() => {
  loadCooldownDays()
  loadGroupConfig()
  loadDishes()
})
</script>

<style scoped>
.header-section {
  margin-top: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
}

.config-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.config-card {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.input-inline {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xs);
}

.planner-main-box {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.planner-form {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xl);
  margin-top: var(--spacing-md);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.form-group label {
  color: var(--ink-subtle);
}

.preview-results {
  border-top: 1px solid var(--hairline);
  padding-top: var(--spacing-xl);
}

.preview-dishes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.dish-preview-card {
  border-radius: var(--rounded-lg);
  overflow: hidden;
  position: relative;
  background-color: var(--surface-1);
  border: 1px solid var(--hairline);
}

.wish-banner {
  position: absolute;
  top: 12px;
  left: 12px;
  background-color: var(--primary);
  color: var(--on-primary);
  padding: 4px 10px;
  border-radius: var(--rounded-sm);
  font-size: 11px;
  font-weight: 600;
  z-index: 10;
}

.sig-badge {
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  color: var(--primary-hover);
  font-size: 10px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: var(--rounded-xs);
}

.dish-preview-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.dish-preview-info {
  padding: var(--spacing-md);
}

.info-preview-box {
  min-height: 240px;
}

.grocery-portion-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
}

.grocery-ul {
  list-style: none;
  margin-top: var(--spacing-md);
}

.grocery-ul li {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-xs) 0;
  border-bottom: 1px dashed var(--hairline);
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
  border-bottom: 1px dashed var(--hairline);
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

.publish-action {
  text-align: right;
  margin-top: var(--spacing-xl);
}

.skilled-dishes-list {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.skilled-dish-checkbox-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-xs) 0;
  border-bottom: 1px solid var(--hairline);
}
</style>
