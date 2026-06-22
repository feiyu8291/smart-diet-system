<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">DIET RECIPE SQUARE</span>
      <h1 class="display-lg">菜谱广场</h1>
    </div>

    <!-- 饮食分类胶囊 (Airbnb 药丸风格) -->
    <div class="category-strip">
      <button
          v-for="tab in categoryTabs"
          :key="tab.value"
          :class="['category-tab-btn', { active: activeCategory === tab.value }]"
          @click="selectCategory(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 1. 菜谱网格展示 -->
    <div class="dishes-grid">
      <div
          v-for="dish in filteredDishes"
          :key="dish.dishId"
          class="dish-card-item hairline-border"
      >
        <!-- 拿手菜勋章 -->
        <div v-if="dish.signatureFlag === 1" class="signature-tag caption">
          ★ 拿手菜
        </div>

        <div class="card-img-box">
          <img
              :src="dish.previewUrl || 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=500'"
              class="card-img"
              alt="dish image"
          />
          <!-- 上传成品图小操作面板 -->
          <div class="upload-overlay">
            <el-upload
                action=""
                :http-request="(options: any) => handleUploadImage(options, dish)"
                :show-file-list="false"
                accept="image/*"
            >
              <button class="btn-primary upload-btn" style="font-size: 12px; padding: 4px 10px">
                上传我的成品图
              </button>
            </el-upload>
          </div>
        </div>

        <div class="card-body">
          <div class="card-meta">
            <span class="caption">{{ dish.cuisineType }}</span>
            <span class="eyebrow count-badge">已烹饪 {{ dish.cookCount }} 次</span>
          </div>

          <h3 class="card-title dish-name" @click="handleOpenSteps(dish)">
            {{ dish.dishName }}
          </h3>

          <p class="body-sm" style="color: var(--ink-subtle); margin-bottom: var(--spacing-sm)">
            热量：{{ dish.calories }} kcal/100g | 饮食模式: {{ getDietModeName(dish.dietMode) }}
          </p>

          <!-- 底部想吃与不喜欢登记按钮组 -->
          <div class="action-btn-row">
            <button class="btn-secondary active-wish" @click="openWishModal(dish)">
              我想吃
            </button>
            <button class="btn-secondary active-dislike" @click="openDislikeModal(dish)">
              不喜欢
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. 做法步骤和原料大抽屉 -->
    <el-drawer
        v-model="drawerVisible"
        title="菜品烹饪说明书"
        size="480px"
        direction="rtl"
    >
      <div v-if="selectedDishDetail" class="drawer-detail-box">
        <span class="caption">RECIPE STEPS</span>
        <h2 class="display-lg" style="font-size: 40px; margin-bottom: var(--spacing-md)">
          {{ selectedDishDetail.dish.dishName }}
        </h2>

        <!-- 配方原料单 -->
        <div class="color-block color-block-cream info-block">
          <span class="caption">INGREDIENTS FORMULA</span>
          <h4 class="card-title" style="margin-bottom: var(--spacing-xs)">配方主料与调料</h4>
          <ul class="ingred-ul">
            <li v-for="ing in selectedDishDetail.ingredients" :key="ing.relationId">
              <span class="body-sm text-bold">{{ ing.ingredientName }}</span>
              <span class="eyebrow" style="font-size: 13px">{{ ing.useAmount }} {{ ing.measureUnit }}</span>
            </li>
          </ul>
        </div>

        <!-- 详细制作步骤 -->
        <div class="color-block color-block-lime info-block" style="margin-top: var(--spacing-md)">
          <span class="caption">COOKING STEPS</span>
          <h4 class="card-title" style="margin-bottom: var(--spacing-xs)">详细制作步骤</h4>
          <div class="steps-list">
            <div
                v-for="step in selectedDishDetail.steps"
                :key="step.stepNum"
                class="step-item"
            >
              <div class="step-badge eyebrow">{{ step.stepNum }}</div>
              <p class="body-sm">{{ step.stepDetail }}</p>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 3. 想吃计划登记对话框 -->
    <el-dialog
        v-model="wishModalVisible"
        title="登记想吃偏好"
        width="440px"
    >
      <div class="modal-form">
        <div class="form-item">
          <label class="caption">就餐成员</label>
          <el-select v-model="wishForm.profileId" placeholder="选择吃饭人" style="width: 100%">
            <el-option
                v-for="m in members"
                :key="m.profileId"
                :label="m.memberName"
                :value="m.profileId"
            />
          </el-select>
        </div>
        <div class="form-item">
          <label class="caption">期望就餐的特定日期 (可选)</label>
          <el-date-picker
              v-model="wishForm.wishDate"
              type="date"
              placeholder="不指定日期则为随机想吃"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
          />
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" style="margin-right: 10px" @click="wishModalVisible = false">取消</button>
        <button class="btn-primary" @click="handleSaveWish">提交想吃意向</button>
      </template>
    </el-dialog>

    <!-- 4. 不喜欢登记对话框 -->
    <el-dialog
        v-model="dislikeModalVisible"
        title="登记不喜欢偏好"
        width="440px"
    >
      <div class="modal-form">
        <div class="form-item">
          <label class="caption">就餐成员</label>
          <el-select v-model="dislikeForm.profileId" placeholder="谁觉得这道菜不好吃？" style="width: 100%">
            <el-option
                v-for="m in members"
                :key="m.profileId"
                :label="m.memberName"
                :value="m.profileId"
            />
          </el-select>
        </div>
        <p class="body-sm" style="color: var(--accent-magenta); margin-top: var(--spacing-xs)">
          提醒：登记不喜欢此菜后，此人在吃此菜时推荐概率直接折半，累积 3 次后该菜系将从此就餐人面前被终身屏蔽。
        </p>
      </div>
      <template #footer>
        <button class="btn-secondary" style="margin-right: 10px" @click="dislikeModalVisible = false">取消</button>
        <button class="btn-primary" @click="handleSaveDislike">登记不喜欢</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, inject, onMounted, ref, unref} from 'vue'
import {ElMessage} from 'element-plus'
import request from '../utils/request'

const activeGroupIdRef = inject<any>('groupId')
const cookUserId = inject<number>('cookUserId', 1)

const groupId = unref(activeGroupIdRef) || 1

const dishes = ref<any[]>([])
const members = ref<any[]>([])

// 饮食分类胶囊
const activeCategory = ref<any>('all')
const categoryTabs = [
  {label: '全部菜谱', value: 'all'},
  {label: '轻食减脂', value: 1},
  {label: '正常饮食', value: 0},
  {label: '放纵餐', value: 2}
]

const filteredDishes = computed(() => {
  if (activeCategory.value === 'all') {
    return dishes.value
  }
  return dishes.value.filter(dish => dish.dietMode === activeCategory.value)
})

const selectCategory = (val: any) => {
  activeCategory.value = val
}

// 抽屉详情
const drawerVisible = ref(false)
const selectedDishDetail = ref<any>(null)

// 想吃对话框
const wishModalVisible = ref(false)
const wishForm = ref({
  profileId: null as number | null,
  groupId: groupId,
  dishId: null as number | null,
  wishDate: ''
})

// 不喜欢对话框
const dislikeModalVisible = ref(false)
const dislikeForm = ref({
  profileId: null as number | null,
  groupId: groupId,
  dishId: null as number | null
})

const loadDishes = async () => {
  try {
    dishes.value = await request.get(`/api/dish/list?userId=${cookUserId}&groupId=${groupId}`)
  } catch (e) {
  }
}

const loadMembers = async () => {
  try {
    members.value = await request.get(`/api/profile/list?groupId=${groupId}`)
  } catch (e) {
  }
}

const getDietModeName = (mode: number): string => {
  if (mode === 1) return '轻食减脂'
  if (mode === 2) return '放纵餐'
  return '正常饮食'
}

// 展开制作步骤做法
const handleOpenSteps = async (dish: any) => {
  try {
    const res = await request.get(`/api/dish/detail/${dish.dishId}`)
    selectedDishDetail.value = res
    drawerVisible.value = true
  } catch (e) {
  }
}

// 成品图上传
const handleUploadImage = async (options: any, dish: any) => {
  const file = options.file
  const formData = new FormData()
  formData.append('file', file)
  formData.append('groupId', String(groupId))
  formData.append('dishId', String(dish.dishId))
  formData.append('creator', '张大厨')

  try {
    const res: any = await request.post('/api/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.success) {
      ElMessage.success('成品打卡图片上传成功，MinIO 优先覆盖展示！')
      loadDishes()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
  }
}

// 打开想吃登记
const openWishModal = (dish: any) => {
  wishForm.value.dishId = dish.dishId
  wishForm.value.profileId = members.value.length > 0 ? members.value[0].profileId : null
  wishForm.value.wishDate = ''
  wishModalVisible.value = true
}

const handleSaveWish = async () => {
  if (!wishForm.value.profileId || !wishForm.value.dishId) return
  try {
    const res = await request.post('/api/dish/wish', wishForm.value)
    if (res) {
      ElMessage.success('家人想吃偏好已成功记下！下次生成膳食将超级高亮匹配该意向！')
      wishModalVisible.value = false
    }
  } catch (e) {
  }
}

// 打开不喜欢登记
const openDislikeModal = (dish: any) => {
  dislikeForm.value.dishId = dish.dishId
  dislikeForm.value.profileId = members.value.length > 0 ? members.value[0].profileId : null
  dislikeModalVisible.value = true
}

const handleSaveDislike = async () => {
  if (!dislikeForm.value.profileId || !dislikeForm.value.dishId) return
  try {
    const res = await request.post('/api/dish/dislike', dislikeForm.value)
    if (res) {
      ElMessage.success('不喜欢记录已增加，该菜出现频率已降低。')
      dislikeModalVisible.value = false
      loadDishes()
    }
  } catch (e) {
  }
}

onMounted(() => {
  loadDishes()
  loadMembers()
})
</script>

<style scoped>
.header-section {
  margin-top: var(--spacing-xl);
  margin-bottom: var(--spacing-sm);
}

.category-strip {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
  overflow-x: auto;
  padding-bottom: 4px;
}

.category-tab-btn {
  font-size: 13px;
  font-weight: 500;
  padding: 6px 16px;
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-pill);
  background-color: var(--surface-1);
  color: var(--ink-subtle);
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
}

.category-tab-btn:hover {
  border-color: var(--hairline-strong);
  color: var(--ink);
}

.category-tab-btn.active {
  background-color: var(--primary);
  border-color: var(--primary);
  color: var(--on-primary);
  font-weight: 600;
}

.dishes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.dish-card-item {
  border-radius: var(--rounded-lg);
  overflow: hidden;
  background-color: var(--surface-1);
  border: 1px solid var(--hairline);
  position: relative;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.dish-card-item:hover {
  transform: translateY(-4px);
  border-color: var(--hairline-strong);
}

.signature-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  color: var(--primary-hover);
  padding: 4px 10px;
  border-radius: var(--rounded-xs);
  font-size: 11px;
  font-weight: 500;
  z-index: 10;
}

.card-img-box {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.card-img-box:hover .upload-overlay {
  opacity: 1;
}

.card-body {
  padding: var(--spacing-md);
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.count-badge {
  font-size: 11px;
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  padding: 2px 8px;
  border-radius: var(--rounded-xs);
}

.dish-name {
  margin-bottom: var(--spacing-xxs);
  cursor: pointer;
}

.dish-name:hover {
  text-decoration: underline;
}

.action-btn-row {
  display: flex;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-md);
}

.action-btn-row button {
  flex: 1;
  font-size: 13px;
  padding: 6px 12px;
}

.drawer-detail-box {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.info-block {
  padding: var(--spacing-md) !important;
}

.ingred-ul {
  list-style: none;
  margin-top: var(--spacing-xs);
}

.ingred-ul li {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed var(--hairline);
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-xs);
}

.step-item {
  display: flex;
  gap: var(--spacing-sm);
  align-items: flex-start;
}

.step-badge {
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  color: var(--primary-hover);
  width: 22px;
  height: 22px;
  border-radius: var(--rounded-xs);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xxs);
}
</style>
