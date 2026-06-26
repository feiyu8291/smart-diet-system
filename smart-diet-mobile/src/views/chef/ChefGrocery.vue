<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRoleStore} from '../../store/role'
import request from '../../utils/request'
import {showSuccessToast, showToast} from 'vant'

const roleStore = useRoleStore()
const loading = ref(false)

// 购买项类型
interface GroceryItem {
  id: number
  name: string
  amount: string
  category: 'main' | 'sub' | 'condiment' // 主料、配料、调味品
  checked: boolean
}

// 模拟买菜清单（用于体验模式或后端无数据兜底）
const mockGroceryList: GroceryItem[] = [
  {id: 401, name: '鸡胸肉', amount: '300 克', category: 'main', checked: false},
  {id: 402, name: '新鲜鲈鱼', amount: '1 条 (约380g)', category: 'main', checked: false},
  {id: 403, name: '土鸡蛋', amount: '2 个', category: 'main', checked: true},
  {id: 404, name: '西兰花', amount: '240 克', category: 'sub', checked: false},
  {id: 405, name: '有机生菜', amount: '300 克', category: 'sub', checked: false},
  {id: 406, name: '红蜜薯', amount: '200 克', category: 'sub', checked: false},
  {id: 407, name: '独头大蒜', amount: '20 克', category: 'condiment', checked: true},
  {id: 408, name: '特级初榨橄榄油', amount: '30 毫升', category: 'condiment', checked: true},
  {id: 409, name: '蒸鱼豉油', amount: '15 毫升', category: 'condiment', checked: false}
]

const groceryList = ref<GroceryItem[]>([])

const fetchGrocery = async () => {
  if (roleStore.token?.startsWith('mock-')) {
    groceryList.value = [...mockGroceryList]
    return
  }

  loading.value = true
  try {
    const todayStr = new Date().toISOString().split('T')[0]
    // 从后端接口拉取累计菜场食材需求清单
    const res: any = await request.get(`/api/diet/family-meal-grocery/list?groupId=${roleStore.groupId}&date=${todayStr}`)

    const rawList = res.data || res || []
    if (rawList.length > 0) {
      groceryList.value = rawList.map((item: any, idx: number) => {
        return {
          id: item.groceryId || idx,
          name: item.ingredientName,
          amount: `${item.useAmount} ${item.measureUnit || 'g'}`,
          category: item.condimentFlag === 1 ? 'condiment' : (item.mainMaterialFlag === 1 ? 'main' : 'sub'),
          checked: false
        }
      })
    } else {
      // 后端未配置或为空时也展示 mock
      groceryList.value = [...mockGroceryList]
    }
  } catch (err) {
    console.error('拉取买菜清单失败', err)
    groceryList.value = [...mockGroceryList]
  } finally {
    loading.value = false
  }
}

// 筛选不同种类的食材
const mainMaterials = computed(() => groceryList.value.filter(g => g.category === 'main'))
const subMaterials = computed(() => groceryList.value.filter(g => g.category === 'sub'))
const condiments = computed(() => groceryList.value.filter(g => g.category === 'condiment'))

// 统计采买进度
const checkedCount = computed(() => groceryList.value.filter(g => g.checked).length)
const totalCount = computed(() => groceryList.value.length)
const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((checkedCount.value / totalCount.value) * 100)
})

// 一键重置/清除勾选
const handleReset = () => {
  groceryList.value.forEach(g => g.checked = false)
  showToast('采购清单已重置')
}

// 一键全选
const handleCheckAll = () => {
  groceryList.value.forEach(g => g.checked = true)
  showSuccessToast('食材采买全选成功')
}

onMounted(() => {
  fetchGrocery()
})
</script>

<template>
  <div class="chef-grocery">
    <!-- 顶部买菜进度卡 -->
    <div class="progress-card card-shadow">
      <div class="prog-info">
        <div class="info-left">
          <h2>菜市场采买助手 🛒</h2>
          <p>智能合并今日三餐所需食材，带上手机轻松采买</p>
        </div>
        <div class="info-right">
          <span class="prog-num">{{ checkedCount }}<span>/{{ totalCount }} 件</span></span>
        </div>
      </div>

      <div class="progress-bar-wrapper">
        <van-progress
            :percentage="progressPercent"
            stroke-width="6"
            color="var(--primary-color)"
            track-color="#f1f2f6"
            :show-pivot="false"
        />
        <div class="prog-bottom-actions">
          <span @click="handleReset">🔄 重新采买</span>
          <span @click="handleCheckAll">✅ 一键全选</span>
        </div>
      </div>
    </div>

    <!-- 食材清单卡分类展示 -->
    <div class="grocery-sections mt-16">

      <!-- 主料 -->
      <div class="grocery-card card-shadow" v-if="mainMaterials.length > 0">
        <h3>🥩 核心主料类 (肉禽蛋海鲜)</h3>
        <div class="list-wrapper">
          <div
              v-for="item in mainMaterials"
              :key="item.id"
              :class="['grocery-row', { done: item.checked }]"
              @click="item.checked = !item.checked"
          >
            <van-checkbox
                v-model="item.checked"
                checked-color="var(--primary-color)"
                @click.stop
            />
            <div class="g-info">
              <span class="g-name">{{ item.name }}</span>
              <span class="g-amount">{{ item.amount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 配料 -->
      <div class="grocery-card card-shadow mt-16" v-if="subMaterials.length > 0">
        <h3>🥦 辅助配料类 (新鲜蔬菜菌菇)</h3>
        <div class="list-wrapper">
          <div
              v-for="item in subMaterials"
              :key="item.id"
              :class="['grocery-row', { done: item.checked }]"
              @click="item.checked = !item.checked"
          >
            <van-checkbox
                v-model="item.checked"
                checked-color="var(--primary-color)"
                @click.stop
            />
            <div class="g-info">
              <span class="g-name">{{ item.name }}</span>
              <span class="g-amount">{{ item.amount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 调味辅料 -->
      <div class="grocery-card card-shadow mt-16" v-if="condiments.length > 0">
        <h3>🧂 调味辅料类 (油盐酱醋作料)</h3>
        <div class="list-wrapper">
          <div
              v-for="item in condiments"
              :key="item.id"
              :class="['grocery-row', { done: item.checked }]"
              @click="item.checked = !item.checked"
          >
            <van-checkbox
                v-model="item.checked"
                checked-color="var(--primary-color)"
                @click.stop
            />
            <div class="g-info">
              <span class="g-name">{{ item.name }}</span>
              <span class="g-amount">{{ item.amount }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.chef-grocery {
  padding-bottom: 24px;
}

.mt-16 {
  margin-top: 16px;
}

.progress-card {
  background: white;
  border-radius: 16px;
  padding: 18px;
}

.prog-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.info-left {
  text-align: left;
}

.info-left h2 {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.info-left p {
  font-size: 11px;
  color: var(--text-color-secondary);
  margin: 0;
}

.prog-num {
  font-size: 24px;
  font-weight: 800;
  color: var(--primary-color);
}

.prog-num span {
  font-size: 12px;
  color: var(--text-color-secondary);
  font-weight: 400;
}

.prog-bottom-actions {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #3498db;
  margin-top: 8px;
  cursor: pointer;
}

/* 列表展示卡 */
.grocery-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  text-align: left;
}

.grocery-card h3 {
  font-size: 13px;
  font-weight: 700;
  color: #2c3e50;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
  margin: 0 0 10px 0;
}

.list-wrapper {
  display: flex;
  flex-direction: column;
}

.grocery-row {
  display: flex;
  align-items: center;
  padding: 12px 4px;
  border-bottom: 1px solid #f7f8fa;
  cursor: pointer;
}

.grocery-row:last-child {
  border-bottom: none;
}

.grocery-row.done {
  opacity: 0.65;
}

.g-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 12px;
}

.g-name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.grocery-row.done .g-name {
  text-decoration: line-through;
}

.g-amount {
  font-size: 12px;
  color: var(--text-color-secondary);
  font-weight: 500;
}

.grocery-row.done .g-amount {
  color: var(--text-color-secondary);
}
</style>
