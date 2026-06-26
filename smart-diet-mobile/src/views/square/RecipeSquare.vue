<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRoleStore} from '../../store/role'
import request from '../../utils/request'
import {showSuccessToast, showToast} from 'vant'

const roleStore = useRoleStore()
const loading = ref(false)
const searchQuery = ref('')
const selectedCuisine = ref('全部')

// 菜系过滤选项
const cuisines = ['全部', '粤菜', '湘菜', '川菜', '鲁菜', '闽菜']

// 菜谱详情 Popup 状态
const showDetail = ref(false)
const activeBranchId = ref<number | null>(null)
const currentDish = ref<any>(null)

// 模拟的高颜值菜谱及做法分支数据
const mockDishes = [
  {
    dishId: 2,
    dishName: '西兰花炒牛肉',
    cuisineType: '湘菜',
    dietModeName: '轻食减脂',
    totalLikes: 144,
    coverEmoji: '🥦🥩',
    branches: [
      {
        branchId: 1,
        branchName: '无油低卡健身版西兰花牛肉',
        creatorName: '张大厨',
        calories: 85,
        protein: 16.2,
        fat: 1.8,
        carbs: 2.5,
        likes: 88,
        liked: false,
        collected: false,
        ingredients: [
          {name: '牛里脊', amount: '150g', isMain: true},
          {name: '西兰花', amount: '150g', isMain: true},
          {name: '生抽', amount: '2ml', isMain: false},
          {name: '食盐', amount: '1g', isMain: false}
        ],
        steps: [
          '将牛里脊横纹切成薄片，西兰花切成大小均匀的小朵。',
          '锅中注入清水烧开，将西兰花与牛肉片一同下锅用沸水焯熟，捞出沥干。',
          '淋入2ml生抽，撒入1g食盐，快速凉拌均匀即可出盘。'
        ]
      },
      {
        branchId: 2,
        branchName: '经典传统水淀粉勾芡西兰花牛肉',
        creatorName: '系统预置',
        calories: 130,
        protein: 14.0,
        fat: 5.5,
        carbs: 6.2,
        likes: 56,
        liked: false,
        collected: false,
        ingredients: [
          {name: '牛里脊', amount: '150g', isMain: true},
          {name: '西兰花', amount: '150g', isMain: true},
          {name: '食用油', amount: '15g', isMain: false},
          {name: '生抽', amount: '10ml', isMain: false},
          {name: '食盐', amount: '2g', isMain: false}
        ],
        steps: [
          '牛肉片加入少许淀粉、生抽、料酒抓匀腌制10分钟。',
          '西兰花焯水30秒捞出沥干备用。',
          '热锅下油，下姜蒜末爆香，倒入牛肉片大火快速滑炒至变色。',
          '倒入西兰花及调味料翻炒，最后淋入少许水淀粉勾芡收汁装盘。'
        ]
      }
    ]
  },
  {
    dishId: 3,
    dishName: '西红柿炒鸡蛋',
    cuisineType: '闽菜',
    dietModeName: '正常膳食',
    totalLikes: 162,
    coverEmoji: '🍅🍳',
    branches: [
      {
        branchId: 3,
        branchName: '零卡糖无油版西红柿炒蛋',
        creatorName: '张大厨',
        calories: 75,
        protein: 6.8,
        fat: 2.5,
        carbs: 3.2,
        likes: 120,
        liked: false,
        collected: true,
        ingredients: [
          {name: '西红柿', amount: '200g', isMain: true},
          {name: '鸡蛋', amount: '120g (2个)', isMain: true},
          {name: '食用油', amount: '2g', isMain: false},
          {name: '零卡糖', amount: '2g', isMain: false},
          {name: '食盐', amount: '1.5g', isMain: false}
        ],
        steps: [
          '西红柿切小块，鸡蛋打散。',
          '不粘锅刷薄薄2g油，倒入蛋液快速划散至半熟盛出。',
          '下西红柿炒出沙，倒入蛋碎，撒入零卡糖与盐炒匀出锅。'
        ]
      },
      {
        branchId: 4,
        branchName: '传统多油多甜本味西红柿炒蛋',
        creatorName: '家庭私房',
        calories: 155,
        protein: 6.2,
        fat: 11.2,
        carbs: 8.5,
        likes: 42,
        liked: false,
        collected: false,
        ingredients: [
          {name: '西红柿', amount: '200g', isMain: true},
          {name: '鸡蛋', amount: '120g (2个)', isMain: true},
          {name: '食用油', amount: '12g', isMain: false},
          {name: '白砂糖', amount: '8g', isMain: false},
          {name: '食盐', amount: '3g', isMain: false}
        ],
        steps: [
          '鸡蛋液打入碗中，加少许盐打散。西红柿滚刀切块。',
          '热锅多油，倒入蛋液大火炒出大蓬松状态后捞出。',
          '留底油下西红柿，加糖加盐，炒出大量酸甜汤汁。',
          '倒入捞出的鸡蛋，大火翻炒10秒让蛋液吸饱汤汁即可。'
        ]
      }
    ]
  },
  {
    dishId: 1,
    dishName: '清蒸水煮鸡胸肉',
    cuisineType: '粤菜',
    dietModeName: '轻食减脂',
    totalLikes: 30,
    coverEmoji: '🥬🍗',
    branches: [
      {
        branchId: 5,
        branchName: '主厨秘制无油蒸鸡胸',
        creatorName: '张大厨',
        calories: 95,
        protein: 18.0,
        fat: 1.5,
        carbs: 2.0,
        likes: 30,
        liked: false,
        collected: false,
        ingredients: [
          {name: '鸡胸肉', amount: '200g', isMain: true},
          {name: '西兰花', amount: '50g', isMain: true},
          {name: '生抽', amount: '3ml', isMain: false},
          {name: '黑胡椒', amount: '1g', isMain: false}
        ],
        steps: [
          '鸡胸肉横切薄片，加入少许料酒腌制去腥。',
          '冷水上锅，将鸡肉与西兰花大火清蒸8分钟。',
          '取出后，撒上黑胡椒粉，蘸少许生抽食用。'
        ]
      }
    ]
  }
]

const dishesList = ref<any[]>([])

// 联调及Mock载入
const fetchDishes = async () => {
  loading.value = true
  try {
    if (roleStore.token?.startsWith('mock-')) {
      dishesList.value = JSON.parse(JSON.stringify(mockDishes))
      return
    }

    // 后端接口联调：获取所有菜品及其烹饪分支
    const res: any = await request.get('/api/diet/dish/list-branches')
    if (res && res.length > 0) {
      dishesList.value = res
    } else {
      dishesList.value = JSON.parse(JSON.stringify(mockDishes))
    }
  } catch (err) {
    console.error('获取做法广场数据失败，采用Mock兜底', err)
    dishesList.value = JSON.parse(JSON.stringify(mockDishes))
  } finally {
    loading.value = false
  }
}

// 过滤后的菜谱列表
const filteredDishes = computed(() => {
  return dishesList.value.filter(dish => {
    const matchesSearch = dish.dishName.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesCuisine = selectedCuisine.value === '全部' || dish.cuisineType === selectedCuisine.value
    return matchesSearch && matchesCuisine
  })
})

// 查看菜谱详情
const openDishDetail = (dish: any) => {
  currentDish.value = dish
  // 默认激活点赞数最高的分支
  if (dish.branches && dish.branches.length > 0) {
    // 按照点赞数降序排行
    const sorted = [...dish.branches].sort((a, b) => b.likes - a.likes)
    activeBranchId.value = sorted[0].branchId
  } else {
    activeBranchId.value = null
  }
  showDetail.value = true
}

// 获取当前激活的分支详情
const activeBranch = computed(() => {
  if (!currentDish.value || !activeBranchId.value) return null
  return currentDish.value.branches.find((b: any) => b.branchId === activeBranchId.value)
})

// 分支按点赞热度排序输出
const sortedBranches = computed(() => {
  if (!currentDish.value || !currentDish.value.branches) return []
  return [...currentDish.value.branches].sort((a, b) => b.likes - a.likes)
})

// 红心点赞（防重）
const handleLike = async (branch: any) => {
  if (branch.liked) {
    // 取消赞
    try {
      if (!roleStore.token?.startsWith('mock-')) {
        await request.post(`/api/diet/dish/like/cancel?branchId=${branch.branchId}`)
      }
      branch.likes--
      branch.liked = false
      showToast('已取消点赞')
    } catch (err) {
      branch.likes--
      branch.liked = false
    }
  } else {
    // 点赞
    try {
      if (!roleStore.token?.startsWith('mock-')) {
        await request.post(`/api/diet/dish/like/add?branchId=${branch.branchId}`)
      }
      branch.likes++
      branch.liked = true
      showSuccessToast('点赞成功！❤️')
    } catch (err) {
      branch.likes++
      branch.liked = true
      showSuccessToast('已点赞 ❤️')
    }
  }
  // 重新累加主菜品的总赞数
  if (currentDish.value) {
    currentDish.value.totalLikes = currentDish.value.branches.reduce((sum: number, b: any) => sum + b.likes, 0)
  }
}

// 金星收藏
const handleCollect = async (branch: any) => {
  if (branch.collected) {
    try {
      if (!roleStore.token?.startsWith('mock-')) {
        await request.post(`/api/diet/dish/collect/cancel?branchId=${branch.branchId}`)
      }
      branch.collected = false
      showToast('已取消收藏')
    } catch (err) {
      branch.collected = false
    }
  } else {
    try {
      if (!roleStore.token?.startsWith('mock-')) {
        await request.post(`/api/diet/dish/collect/add?branchId=${branch.branchId}`)
      }
      branch.collected = true
      showSuccessToast('已存入收藏夹 ⭐')
    } catch (err) {
      branch.collected = true
      showSuccessToast('收藏成功 ⭐')
    }
  }
}

onMounted(() => {
  fetchDishes()
})
</script>

<template>
  <div class="recipe-square">
    <!-- 顶栏精美标题与搜索 -->
    <div class="search-section card-shadow">
      <van-search
          v-model="searchQuery"
          placeholder="搜索精美菜谱、原材料..."
          shape="round"
          background="transparent"
      />

      <!-- 菜系横滑动 Tag 组 -->
      <div class="cuisine-scroll">
        <button
            v-for="c in cuisines"
            :key="c"
            :class="['cuisine-tag', { active: selectedCuisine === c }]"
            @click="selectedCuisine = c"
        >
          {{ c }}
        </button>
      </div>
    </div>

    <!-- 菜谱大卡片瀑布流布局 -->
    <div class="dishes-container">
      <div v-if="filteredDishes.length === 0" class="empty-box">
        <van-empty image="search" description="暂无符合筛选的精美菜谱"/>
      </div>

      <div
          v-for="dish in filteredDishes"
          :key="dish.dishId"
          class="dish-card card-shadow"
          @click="openDishDetail(dish)"
      >
        <div class="card-bg-gradient"></div>
        <div class="dish-emoji">{{ dish.coverEmoji || '🥗' }}</div>

        <div class="dish-content">
          <div class="dish-title-row">
            <h3 class="dish-name">{{ dish.dishName }}</h3>
            <span class="cuisine-badge">{{ dish.cuisineType }}</span>
          </div>

          <div class="dish-detail-row">
            <span class="mode-tag">{{ dish.dietModeName || '轻食减脂' }}</span>
            <span class="branch-count-text">
              ✨ 共有 {{ dish.branches?.length || 1 }} 个做法分支
            </span>
          </div>

          <div class="dish-footer-row">
            <span class="likes-total">❤️ {{ dish.totalLikes }} 次觉得赞</span>
            <span class="arrow-text">查看分支做法 ➔</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 做法详情大 Popup 面板 (右侧抽屉式) -->
    <van-popup
        v-model:show="showDetail"
        position="right"
        :style="{ width: '100%', height: '100%' }"
    >
      <div class="popup-detail-container" v-if="currentDish">
        <!-- 头部导航与背景 -->
        <div class="popup-header">
          <van-icon name="arrow-left" class="back-icon" @click="showDetail = false"/>
          <h2 class="popup-title">{{ currentDish.dishName }}</h2>
          <span class="popup-cuisine-tag">{{ currentDish.cuisineType }}</span>
        </div>

        <div class="popup-body">
          <!-- 做法分支热度排行榜区 -->
          <div class="branch-leaderboard-section">
            <h3 class="section-title">🏆 做法热度排行榜</h3>
            <p class="section-subtitle">做法分支按点赞数实时降序排列</p>

            <div class="leaderboard-list">
              <div
                  v-for="(b, idx) in sortedBranches"
                  :key="b.branchId"
                  :class="['branch-rank-card', { active: activeBranchId === b.branchId }]"
                  @click="activeBranchId = b.branchId"
              >
                <!-- 排名奖杯 -->
                <div class="rank-badge">
                  <span v-if="idx === 0">🥇</span>
                  <span v-else-if="idx === 1">🥈</span>
                  <span v-else-if="idx === 2">🥉</span>
                  <span v-else class="normal-rank-num">#{{ idx + 1 }}</span>
                </div>

                <div class="rank-card-info">
                  <h4 class="rank-name">{{ b.branchName }}</h4>
                  <p class="rank-meta">
                    创作者: {{ b.creatorName }} | {{ b.calories }} kcal
                  </p>
                </div>

                <div class="rank-actions-status">
                  <span class="rank-likes">❤️ {{ b.likes }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 当前做法的用料与步骤展示 -->
          <div class="branch-detail-panel card-shadow" v-if="activeBranch">
            <div class="panel-header-row">
              <div class="left">
                <span class="sub-title">👨‍🍳 烹饪指南</span>
                <h3 class="active-branch-title">{{ activeBranch.branchName }}</h3>
              </div>
              <div class="right-actions">
                <van-icon
                    :name="activeBranch.liked ? 'like' : 'like-o'"
                    :color="activeBranch.liked ? '#e74c3c' : '#7f8c8d'"
                    size="22"
                    class="action-icon"
                    @click.stop="handleLike(activeBranch)"
                />
                <van-icon
                    :name="activeBranch.collected ? 'star' : 'star-o'"
                    :color="activeBranch.collected ? '#f1c40f' : '#7f8c8d'"
                    size="22"
                    class="action-icon"
                    @click.stop="handleCollect(activeBranch)"
                />
              </div>
            </div>

            <!-- 卡路里明细 -->
            <div class="nutrition-strip">
              <div class="nut-item">
                <span class="n-val">{{ activeBranch.calories }}</span>
                <span class="n-lbl">热量(kcal)</span>
              </div>
              <div class="nut-item border-l">
                <span class="n-val">{{ activeBranch.protein }}g</span>
                <span class="n-lbl">蛋白质</span>
              </div>
              <div class="nut-item border-l">
                <span class="n-val">{{ activeBranch.fat }}g</span>
                <span class="n-lbl">脂肪</span>
              </div>
              <div class="nut-item border-l">
                <span class="n-val">{{ activeBranch.carbs }}g</span>
                <span class="n-lbl">碳水</span>
              </div>
            </div>

            <!-- 食材清单 -->
            <div class="ingredients-section mt-16">
              <h4 class="part-title">🛒 做法配料表</h4>
              <div class="ingredients-grid">
                <div
                    v-for="ing in activeBranch.ingredients"
                    :key="ing.name"
                    class="ing-item-card"
                >
                  <span class="ing-name">{{ ing.name }}</span>
                  <span class="ing-amount">{{ ing.amount }}</span>
                  <van-tag type="success" plain v-if="ing.isMain">主料</van-tag>
                </div>
              </div>
            </div>

            <!-- 加工步骤 -->
            <div class="steps-section mt-16">
              <h4 class="part-title">🍳 加工及烹调步骤</h4>
              <div class="steps-timeline">
                <div
                    v-for="(step, sIdx) in activeBranch.steps"
                    :key="sIdx"
                    class="step-timeline-item"
                >
                  <div class="step-num-dot">{{ Number(sIdx) + 1 }}</div>
                  <div class="step-text-content">
                    <p class="step-desc">{{ step }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.recipe-square {
  padding-bottom: 24px;
}

.mt-16 {
  margin-top: 16px;
}

/* 顶栏与搜索 */
.search-section {
  background: white;
  border-radius: 16px;
  padding: 8px 4px 16px 4px;
  margin-bottom: 16px;
}

.cuisine-scroll {
  display: flex;
  overflow-x: auto;
  gap: 10px;
  padding: 4px 12px 0 12px;
  -webkit-overflow-scrolling: touch;
}

.cuisine-scroll::-webkit-scrollbar {
  display: none;
}

.cuisine-tag {
  flex-shrink: 0;
  border: none;
  background: #f1f2f6;
  color: #7f8c8d;
  font-size: 11px;
  font-weight: 700;
  padding: 6px 14px;
  border-radius: 20px;
  transition: all 0.3s;
}

.cuisine-tag.active {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
  color: white;
  box-shadow: 0 4px 8px rgba(46, 204, 113, 0.25);
}

/* 瀑布流大卡片 */
.dishes-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dish-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  padding: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.dish-card:active {
  transform: scale(0.98);
}

.card-bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 6px;
  height: 100%;
  background: linear-gradient(to bottom, #2ecc71, #3498db);
}

.dish-emoji {
  font-size: 40px;
  margin-right: 16px;
  background: #f1f2f6;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.dish-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dish-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-name {
  font-size: 15px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0;
}

.cuisine-badge {
  font-size: 9px;
  background: #e8f8f5;
  color: #2ecc71;
  padding: 2px 6px;
  border-radius: 6px;
  font-weight: bold;
}

.dish-detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mode-tag {
  font-size: 10px;
  color: #f39c12;
  font-weight: 600;
}

.branch-count-text {
  font-size: 10px;
  color: #7f8c8d;
}

.dish-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  border-top: 1px dashed #ebedf0;
  padding-top: 6px;
}

.likes-total {
  font-size: 10px;
  font-weight: bold;
  color: #e74c3c;
}

.arrow-text {
  font-size: 10px;
  color: #3498db;
  font-weight: bold;
}

/* 详情 Popup */
.popup-detail-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f7f8fa;
}

.popup-header {
  background: white;
  padding: 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebedf0;
  position: relative;
}

.back-icon {
  font-size: 20px;
  color: #2c3e50;
  margin-right: 12px;
  padding: 4px;
}

.back-icon:active {
  opacity: 0.6;
}

.popup-title {
  font-size: 17px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0;
  flex: 1;
}

.popup-cuisine-tag {
  font-size: 10px;
  background: #eaf2f8;
  color: #3498db;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: bold;
}

.popup-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 做法分支排行榜 */
.branch-leaderboard-section {
  background: white;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #ebedf0;
}

.section-title {
  font-size: 13px;
  font-weight: 800;
  color: #2c3e50;
  margin: 0 0 2px 0;
}

.section-subtitle {
  font-size: 10px;
  color: #95a5a6;
  margin: 0 0 12px 0;
}

.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.branch-rank-card {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.branch-rank-card.active {
  background: #eafaf1;
  border-color: #2ecc71;
}

.rank-badge {
  font-size: 20px;
  width: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.normal-rank-num {
  font-size: 12px;
  font-weight: 800;
  color: #95a5a6;
}

.rank-card-info {
  flex: 1;
  padding-left: 6px;
}

.rank-name {
  font-size: 12px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 2px 0;
}

.rank-meta {
  font-size: 10px;
  color: #7f8c8d;
  margin: 0;
}

.rank-actions-status {
  font-size: 11px;
  font-weight: 800;
  color: #e74c3c;
}

/* 当前做法的用料与步骤展示 */
.branch-detail-panel {
  background: white;
  border-radius: 16px;
  padding: 16px;
}

.panel-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.panel-header-row .sub-title {
  font-size: 10px;
  color: #2ecc71;
  font-weight: bold;
  text-transform: uppercase;
}

.active-branch-title {
  font-size: 14px;
  font-weight: bold;
  color: #2c3e50;
  margin: 2px 0 0 0;
  line-height: 1.4;
}

.right-actions {
  display: flex;
  gap: 14px;
}

.action-icon {
  padding: 4px;
}

.action-icon:active {
  transform: scale(0.9);
}

/* 卡路里明细条 */
.nutrition-strip {
  display: flex;
  background: #f1f2f6;
  border-radius: 10px;
  padding: 10px 4px;
  text-align: center;
}

.nut-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.nut-item.border-l {
  border-left: 1px solid #dcdde1;
}

.n-val {
  font-size: 13px;
  font-weight: bold;
  color: #2c3e50;
}

.n-lbl {
  font-size: 9px;
  color: #7f8c8d;
  margin-top: 2px;
}

/* 做法食材 */
.part-title {
  font-size: 12px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.ingredients-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.ing-item-card {
  background: #f8f9fa;
  padding: 8px 10px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
}

.ing-name {
  color: #2c3e50;
  font-weight: 500;
}

.ing-amount {
  color: #7f8c8d;
}

/* 步骤时间轴 */
.steps-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-timeline-item {
  display: flex;
  gap: 10px;
}

.step-num-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #e8f8f5;
  color: #2ecc71;
  font-size: 10px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.step-text-content {
  flex: 1;
}

.step-desc {
  font-size: 11px;
  color: #2c3e50;
  line-height: 1.5;
  margin: 0;
}
</style>
