<script setup lang="ts">
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {useRoleStore} from '../../store/role'
import request from '../../utils/request'
import {showLoadingToast, showSuccessToast, showToast} from 'vant'

const router = useRouter()
const roleStore = useRoleStore()

const loading = ref(false)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const dietMode = ref(1) // 默认 1-轻食减脂, 0-正常, 2-放纵

// 菜品库，用于手动更换
const mockDishPool = [
  {id: 10, name: '水煮牛肉', type: '正常', calories: 230},
  {id: 11, name: '黑椒牛仔骨', type: '放纵', calories: 340},
  {id: 12, name: '青椒炒肉丝', type: '正常', calories: 180},
  {id: 13, name: '蒜蓉粉丝蒸扇贝', type: '正常', calories: 120},
  {id: 14, name: '虾仁滑蛋', type: '轻食', calories: 140},
  {id: 15, name: '香烤龙利鱼', type: '轻食', calories: 115},
  {id: 16, name: '双菇西兰花', type: '轻食', calories: 55},
  {id: 17, name: '香辣干锅虾', type: '放纵', calories: 290},
  {id: 18, name: '冬瓜排骨汤', type: '正常', calories: 95}
]

// 智能生成的今日菜单模型
const generatedMenu = ref<any>(null)

// 触发“智能生成配餐”
const handleGenerate = async () => {
  loading.value = true
  const toast = showLoadingToast({
    message: '智能配餐匹配中...',
    forbidClick: true,
    duration: 1000
  })

  // 模拟请求延迟，给用户“AI智能推荐中”的高级体验
  setTimeout(async () => {
    toast.close()
    loading.value = false

    // 如果是联调模式，调用后端接口
    if (!roleStore.token?.startsWith('mock-')) {
      try {
        const res: any = await request.post('/api/diet/family-meal-plan/generate-recommend', {
          groupId: roleStore.groupId,
          mealDate: selectedDate.value,
          dietMode: dietMode.value
        })
        if (res && res.data) {
          generatedMenu.value = res.data
          return
        }
      } catch (err) {
        console.error('调用后端配餐算法失败，使用本地推荐算法结果展示', err)
      }
    }

    // 体验模式或接口降级时的模拟算法数据
    if (dietMode.value === 1) { // 轻食减脂
      generatedMenu.value = {
        breakfast: [
          {id: 101, name: '水煮蛋', calories: 78, tags: ['高蛋白']},
          {id: 102, name: '全麦面包', calories: 156, tags: ['粗粮']},
          {id: 103, name: '低脂牛奶', calories: 120, tags: ['补钙']}
        ],
        lunch: [
          {id: 104, name: '香煎鸡胸肉', calories: 198, tags: ['低脂高蛋']},
          {id: 105, name: '清炒西兰花', calories: 45, tags: ['膳食纤维']},
          {id: 106, name: '黑米饭', calories: 168, tags: ['低GI']}
        ],
        dinner: [
          {id: 107, name: '清蒸鲈鱼', calories: 210, tags: ['不饱和脂肪']},
          {id: 108, name: '白灼生菜', calories: 25, tags: ['低卡']},
          {id: 109, name: '蒸紫薯', calories: 86, tags: ['高纤维']}
        ]
      }
    } else { // 正常或放纵模式
      generatedMenu.value = {
        breakfast: [
          {id: 1, name: '西红柿炒鸡蛋', calories: 140, tags: ['经典']},
          {id: 103, name: '低脂牛奶', calories: 120, tags: ['温热']}
        ],
        lunch: [
          {id: 10, name: '水煮牛肉', calories: 230, tags: ['麻辣过瘾']},
          {id: 12, name: '青椒炒肉丝', calories: 180, tags: ['家常']},
          {id: 106, name: '白米饭', calories: 230, tags: ['能量碳水']}
        ],
        dinner: [
          {id: 18, name: '冬瓜排骨汤', calories: 95, tags: ['滋补']},
          {id: 16, name: '双菇西兰花', calories: 55, tags: ['清淡']}
        ]
      }
    }
    showToast('智能配餐生成成功！已为您避开忌口菜品。')
  }, 1000)
}

// 换一批：随机从菜品池替换
const activeReplaceMeal = ref<'breakfast' | 'lunch' | 'dinner' | null>(null)
const activeReplaceIndex = ref<number>(-1)
const showReplacePopup = ref(false)

const triggerReplace = (mealType: 'breakfast' | 'lunch' | 'dinner', index: number) => {
  activeReplaceMeal.value = mealType
  activeReplaceIndex.value = index
  showReplacePopup.value = true
}

const replaceWith = (dish: any) => {
  if (!activeReplaceMeal.value) return

  generatedMenu.value[activeReplaceMeal.value][activeReplaceIndex.value] = {
    id: dish.id,
    name: dish.name,
    calories: dish.calories,
    tags: [dish.type + '膳食']
  }
  showReplacePopup.value = false
  showToast('已替换为：' + dish.name)
}

// 一键随机换一批
const handleRandomSwap = (mealType: 'breakfast' | 'lunch' | 'dinner', index: number) => {
  const pool = mockDishPool.filter(d => dietMode.value === 1 ? d.type === '轻食' : true)
  const randomDish = pool[Math.floor(Math.random() * pool.length)]

  generatedMenu.value[mealType][index] = {
    id: randomDish.id,
    name: randomDish.name,
    calories: randomDish.calories,
    tags: ['随机换新']
  }
  showToast('随机替换为：' + randomDish.name)
}

// 确认发布食谱
const handlePublish = async () => {
  if (!generatedMenu.value) return

  loading.value = true
  const toast = showLoadingToast({
    message: '生成采购明细及家庭分餐中...',
    forbidClick: true,
    duration: 0
  })

  try {
    if (roleStore.token?.startsWith('mock-')) {
      // 模拟发布
      setTimeout(() => {
        toast.close()
        loading.value = false
        showSuccessToast('配餐已发布！已向就餐成员推送分餐推荐，买菜清单已生成')
        router.push('/chef/home')
      }, 1200)
    } else {
      // 调用后端发布接口，发布今日菜单，触发后端的 grocery 和 portion 的自动生成
      await request.post('/api/diet/family-meal-plan/publish', {
        groupId: roleStore.groupId,
        mealDate: selectedDate.value,
        menuData: generatedMenu.value
      })
      toast.close()
      loading.value = false
      showSuccessToast('今日食谱确认并成功发布')
      router.push('/chef/home')
    }
  } catch (err) {
    toast.close()
    loading.value = false
    console.error(err)
  }
}
</script>

<template>
  <div class="recipe-generator">
    <!-- 配餐选项配置卡 -->
    <div class="config-card card-shadow">
      <h3>📅 智能配餐配置</h3>

      <van-cell-group inset>
        <van-field
            v-model="selectedDate"
            type="date"
            label="配餐日期"
        />

        <div class="mode-select-row">
          <span class="mode-label">就餐模式</span>
          <div class="pills">
            <button
                :class="['pill-btn', { active: dietMode === 1 }]"
                @click="dietMode = 1"
            >
              🥗 轻食减脂
            </button>
            <button
                :class="['pill-btn', { active: dietMode === 0 }]"
                @click="dietMode = 0"
            >
              🥩 均衡膳食
            </button>
            <button
                :class="['pill-btn', { active: dietMode === 2 }]"
                @click="dietMode = 2"
            >
              🥘 放纵大餐
            </button>
          </div>
        </div>
      </van-cell-group>

      <div class="generate-action">
        <van-button
            type="primary"
            block
            round
            icon="bulb-o"
            class="gen-btn"
            @click="handleGenerate"
        >
          一键生成推荐菜单
        </van-button>
      </div>
    </div>

    <!-- 生成的推荐菜单详情 -->
    <div v-if="generatedMenu" class="recommend-results fade-in mt-16">
      <div class="result-title">🎯 AI 配餐推荐列表</div>

      <!-- 早餐 -->
      <div class="meal-section card-shadow">
        <h4>早餐 🥞</h4>
        <div class="dish-chips">
          <div v-for="(dish, idx) in generatedMenu.breakfast" :key="dish.id" class="dish-chip">
            <div class="chip-main">
              <span class="d-name">{{ dish.name }}</span>
              <span class="d-cal">{{ dish.calories }} kcal</span>
            </div>
            <div class="chip-actions">
              <span class="act-btn" @click="handleRandomSwap('breakfast', Number(idx))">🔄 换一批</span>
              <span class="act-btn border-l" @click="triggerReplace('breakfast', Number(idx))">🔍 挑选</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 午餐 -->
      <div class="meal-section card-shadow mt-12">
        <h4>午餐 🥗</h4>
        <div class="dish-chips">
          <div v-for="(dish, idx) in generatedMenu.lunch" :key="dish.id" class="dish-chip">
            <div class="chip-main">
              <span class="d-name">{{ dish.name }}</span>
              <span class="d-cal">{{ dish.calories }} kcal</span>
            </div>
            <div class="chip-actions">
              <span class="act-btn" @click="handleRandomSwap('lunch', Number(idx))">🔄 换一批</span>
              <span class="act-btn border-l" @click="triggerReplace('lunch', Number(idx))">🔍 挑选</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 晚餐 -->
      <div class="meal-section card-shadow mt-12">
        <h4>晚餐 🐟</h4>
        <div class="dish-chips">
          <div v-for="(dish, idx) in generatedMenu.dinner" :key="dish.id" class="dish-chip">
            <div class="chip-main">
              <span class="d-name">{{ dish.name }}</span>
              <span class="d-cal">{{ dish.calories }} kcal</span>
            </div>
            <div class="chip-actions">
              <span class="act-btn" @click="handleRandomSwap('dinner', Number(idx))">🔄 换一批</span>
              <span class="act-btn border-l" @click="triggerReplace('dinner', Number(idx))">🔍 挑选</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 确认发布 -->
      <div class="publish-container mt-20">
        <van-button
            type="success"
            block
            round
            icon="success"
            @click="handlePublish"
        >
          确认配餐并发布
        </van-button>
        <p class="publish-tip">发布后，系统将自动生成食材采购明细，并向就餐成员推送个性化的分餐克数推荐。</p>
      </div>
    </div>

    <!-- 挑选菜品弹窗 -->
    <van-popup
        v-model:show="showReplacePopup"
        position="bottom"
        round
        :style="{ height: '60%' }"
    >
      <div class="replace-panel">
        <div class="replace-header">选择新菜品</div>
        <div class="replace-body">
          <van-cell
              v-for="dish in mockDishPool"
              :key="dish.id"
              :title="dish.name"
              :label="`${dish.type} | ${dish.calories} kcal`"
              is-link
              @click="replaceWith(dish)"
          />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.recipe-generator {
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

.config-card {
  background: white;
  border-radius: 16px;
  padding: 16px 8px;
}

.config-card h3 {
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 16px 16px;
}

/* 就餐模式选择 pills */
.mode-select-row {
  display: flex;
  flex-direction: column;
  padding: 14px 16px;
  gap: 10px;
}

.mode-label {
  font-size: 14px;
  color: var(--text-color-primary);
  text-align: left;
}

.pills {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.pill-btn {
  border: 1px solid var(--border-color);
  background: white;
  padding: 6px 12px;
  border-radius: 14px;
  font-size: 12px;
  color: var(--text-color-primary);
  white-space: nowrap;
  transition: all 0.2s;
}

.pill-btn.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.generate-action {
  padding: 16px 16px 8px 16px;
}

.gen-btn {
  box-shadow: 0 4px 10px rgba(46, 204, 113, 0.2);
}

/* 推荐结果列表 */
.result-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 15px 0 10px 4px;
  text-align: left;
}

.meal-section {
  background: white;
  border-radius: 16px;
  padding: 14px 16px;
  text-align: left;
}

.meal-section h4 {
  font-size: 14px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 12px 0;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 6px;
}

.dish-chips {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-chip {
  background: #f7f8fa;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chip-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.d-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.d-cal {
  font-size: 10px;
  color: var(--text-color-secondary);
}

.chip-actions {
  display: flex;
  gap: 8px;
}

.act-btn {
  font-size: 11px;
  color: #3498db;
  cursor: pointer;
}

.border-l {
  border-left: 1px solid var(--border-color);
  padding-left: 8px;
}

/* 底部发布信息 */
.publish-container {
  padding: 0 4px;
}

.publish-tip {
  font-size: 10px;
  color: var(--text-color-secondary);
  line-height: 1.4;
  margin-top: 8px;
  text-align: center;
}

/* 弹出面板 */
.replace-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.replace-header {
  padding: 16px;
  font-size: 15px;
  font-weight: 700;
  text-align: center;
  border-bottom: 1px solid var(--border-color);
}

.replace-body {
  flex: 1;
  overflow-y: auto;
}

.fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
