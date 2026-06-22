<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">DIET RECIPE MANAGEMENT</span>
      <h1 class="display-lg">菜谱管理列表</h1>
    </div>

    <div class="dish-manage-card color-block">
      <!-- 搜索与新增 -->
      <div class="toolbar-section">
        <div class="search-form">
          <input
              type="text"
              v-model="searchName"
              class="input-text"
              placeholder="搜索菜谱名称..."
              @keyup.enter="loadDishes"
              style="width: 200px;"
          />
          <el-select v-model="searchCuisine" placeholder="全部菜系" style="width: 140px" clearable @change="loadDishes">
            <el-option
                v-for="item in allCuisineTypes"
                :key="item.dataCode"
                :label="item.dataValue"
                :value="item.dataValue"
            />
          </el-select>
          <el-select v-model="filterMode" placeholder="膳食模式" style="width: 130px" @change="loadDishes">
            <el-option :value="null" label="全部模式"/>
            <el-option :value="0" label="正常饮食"/>
            <el-option :value="1" label="轻食减脂"/>
            <el-option :value="2" label="放纵餐"/>
          </el-select>
          <button class="btn-primary" @click="loadDishes">搜索</button>
          <button class="btn-secondary" @click="resetSearch">重置</button>
        </div>
        <button class="btn-primary" @click="openCreateModal">
          + 新建健康菜谱
        </button>
      </div>

      <!-- 菜谱列表 -->
      <el-table
          v-loading="loading"
          :data="dishes"
          style="width: 100%; margin-top: 20px"
          class="custom-table"
      >
        <el-table-column prop="dishId" label="ID" width="70" align="center"/>
        <el-table-column label="预览图" width="90" align="center">
          <template #default="scope">
            <img
                :src="scope.row.previewUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500'"
                class="dish-preview-thumb"
                alt="dish cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="dishName" label="菜名" width="160"/>
        <el-table-column prop="cuisineType" label="菜系" width="100" align="center"/>
        <el-table-column prop="dietMode" label="就餐建议" width="110" align="center">
          <template #default="scope">
            <span :class="['mode-badge', getModeClass(scope.row.dietMode)]">
              {{ getModeText(scope.row.dietMode) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="热量" width="120" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.calories || 0 }}</span> <span class="unit">kcal</span>
          </template>
        </el-table-column>
        <el-table-column label="蛋白质" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.protein || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="脂肪" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.fat || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="碳水" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.carbs || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openEditModal(scope.row.dishId)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.dishId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 菜谱配置全屏或宽 Dialog -->
    <el-dialog
        v-model="modalVisible"
        :title="form.dishId ? '编辑健康菜谱' : '新建健康菜谱'"
        width="860px"
    >
      <div class="dish-editor-scrollable">
        <!-- Part 1: 菜谱基础配置 -->
        <div class="section-title">
          <span class="eyebrow">BASIC INFORMATION</span>
          <h3>1. 菜谱属性</h3>
        </div>
        <div class="editor-grid">
          <div class="form-row-2">
            <div class="form-item">
              <label class="caption">菜品名称</label>
              <input type="text" v-model="form.dishName" class="input-text" placeholder="例如: 番茄炒蛋、西芹腰果"/>
            </div>
            <div class="form-item">
              <label class="caption">菜系类型</label>
              <el-select v-model="form.cuisineType" placeholder="选择菜系" style="width: 100%">
                <el-option
                    v-for="item in allCuisineTypes"
                    :key="item.dataCode"
                    :label="item.dataValue"
                    :value="item.dataValue"
                />
              </el-select>
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-item">
              <label class="caption">建议就餐膳食模式</label>
              <el-select v-model="form.dietMode" placeholder="选择营养建议模式" style="width: 100%">
                <el-option :value="0" label="正常饮食 (常规餐，老少皆宜)"/>
                <el-option :value="1" label="轻食减脂 (少油低碳水，利于刷脂)"/>
                <el-option :value="2" label="放纵餐 (高油脂高热量，欺骗餐)"/>
              </el-select>
            </div>
            <div class="form-item" style="grid-column: span 2;">
              <label class="caption" style="margin-bottom: 6px;">自制成品图上传 (最多 3 张，单张不超 10MB)</label>
              <el-upload
                  v-model:file-list="fileList"
                  :action="`${BASE_URL}/api/s3Storage/upload`"
                  :headers="uploadHeaders"
                  :data="{ bucketName: 'file' }"
                  list-type="picture-card"
                  :limit="3"
                  :on-exceed="handleExceed"
                  :before-upload="beforeUpload"
                  :on-success="handleUploadSuccess"
                  :on-remove="handleRemove"
              >
                <el-icon>
                  <Plus/>
                </el-icon>
              </el-upload>
            </div>
          </div>

          <!-- 营养指标：可自定义输入或自动折算 -->
          <div class="form-item" style="margin-top: 10px;">
            <el-checkbox v-model="autoCalculateNutrients">
              自动评估成品每 100g 营养比例（根据下表配料及克数折算，免去手动估算）
            </el-checkbox>
          </div>

          <div class="form-row-4" v-if="!autoCalculateNutrients">
            <div class="form-item">
              <label class="caption">热量 (kcal/100g)</label>
              <input type="number" v-model="form.calories" class="input-text" step="0.1"/>
            </div>
            <div class="form-item">
              <label class="caption">蛋白质 (g/100g)</label>
              <input type="number" v-model="form.protein" class="input-text" step="0.1"/>
            </div>
            <div class="form-item">
              <label class="caption">脂肪 (g/100g)</label>
              <input type="number" v-model="form.fat" class="input-text" step="0.1"/>
            </div>
            <div class="form-item">
              <label class="caption">碳水 (g/100g)</label>
              <input type="number" v-model="form.carbs" class="input-text" step="0.1"/>
            </div>
          </div>
        </div>

        <!-- Part 2: 配方原材料克数表 -->
        <div class="section-title" style="margin-top: 24px;">
          <span class="eyebrow">INGREDIENT FORMULA</span>
          <div class="sub-header-row">
            <h3>2. 包含食材及用量配比</h3>
            <button class="btn-secondary btn-xs" @click="addIngredientRow">+ 添加原材料行</button>
          </div>
        </div>

        <div class="ingredients-list-editor">
          <div v-if="form.ingredients.length === 0" class="empty-dynamic-placeholder body-sm text-muted">
            尚未关联原材料，请点击右上角新增，这不仅是菜谱核心成分，更是全天自动生成采购单的基础。
          </div>
          <div v-else class="dynamic-rows-container">
            <div class="row-header-labels">
              <span class="caption label-col">原材料名称</span>
              <span class="caption label-col">预计用量</span>
              <span class="caption label-col">主/配辅料</span>
              <span class="caption label-col" style="flex: 0 0 60px;">操作</span>
            </div>
            <div v-for="(row, index) in form.ingredients" :key="index" class="dynamic-row">
              <div class="row-col">
                <el-select v-model="row.ingredientId" filterable placeholder="检索原料..." style="width: 100%">
                  <el-option
                      v-for="ing in allIngredients"
                      :key="ing.ingredientId"
                      :label="ing.ingredientName + (ing.condimentFlag === 1 ? ' (调料)' : '')"
                      :value="ing.ingredientId"
                  />
                </el-select>
              </div>
              <div class="row-col flex-row-align">
                <input type="number" v-model="row.useAmount" class="input-text" placeholder="克数/毫升" style="width: 100%;"/>
                <span class="unit" style="margin-left: 6px;">{{ getUnit(row.ingredientId) }}</span>
              </div>
              <div class="row-col">
                <el-select v-model="row.mainMaterialFlag" style="width: 100%">
                  <el-option :value="1" label="核心主料"/>
                  <el-option :value="0" label="辅料调味"/>
                </el-select>
              </div>
              <div class="row-col align-center" style="flex: 0 0 60px;">
                <button class="btn-delete-row" @click="removeIngredientRow(index)">✕</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Part 3: 做法步骤描述 -->
        <div class="section-title" style="margin-top: 24px;">
          <span class="eyebrow">COOKING STEPS DIAL</span>
          <div class="sub-header-row">
            <h3>3. 烹饪加工步骤</h3>
            <button class="btn-secondary btn-xs" @click="addStepRow">+ 新增工序步骤</button>
          </div>
        </div>

        <div class="steps-list-editor">
          <div v-if="form.steps.length === 0" class="empty-dynamic-placeholder body-sm text-muted">
            尚未定义步骤。步骤可以使用步骤库的标准模块一键引入，也可以在卡片内直接输入您的个性描述。
          </div>
          <div v-else class="steps-rows-container">
            <div v-for="(step, index) in form.steps" :key="index" class="step-card-editor hairline-border">
              <div class="step-card-header">
                <span class="step-num font-mono">步骤 {{ index + 1 }}</span>
                <div class="step-template-select-col">
                  <span class="caption" style="margin-right: 8px;">引入模板:</span>
                  <el-select
                      v-model="step.stepPoolId"
                      placeholder="标准步骤..."
                      style="width: 160px"
                      clearable
                      @change="(val: any) => handleStepTemplateChange(val, index)"
                  >
                    <el-option
                        v-for="pool in allStandardSteps"
                        :key="pool.stepPoolId"
                        :label="pool.stepName"
                        :value="pool.stepPoolId"
                    />
                  </el-select>
                </div>
                <div class="step-actions-col">
                  <button class="btn-step-act" :disabled="index === 0" @click="moveStepUp(index)">▲</button>
                  <button class="btn-step-act" :disabled="index === form.steps.length - 1" @click="moveStepDown(index)">▼</button>
                  <button class="btn-step-act text-danger" @click="removeStepRow(index)">✕</button>
                </div>
              </div>
              <div class="step-card-body">
                <textarea
                    v-model="step.customDetail"
                    class="input-text"
                    rows="3"
                    placeholder="输入本步骤详细烹饪方法..."
                    style="resize: none; font-size: 13px;"
                ></textarea>
              </div>
            </div>
          </div>
        </div>

      </div>
      <template #footer>
        <button class="btn-secondary" style="margin-right: 10px" @click="modalVisible = false">取消</button>
        <button class="btn-primary" @click="handleSave">保存菜谱并自动核算</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import request from '../utils/request'

const loading = ref(false)
const dishes = ref<any[]>([])
const searchName = ref('')
const searchCuisine = ref('')
const filterMode = ref<number | null>(null)
const modalVisible = ref(false)

// 上传与字典相关变量
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const fileList = ref<any[]>([])
const allCuisineTypes = ref<any[]>([])

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? {Authorization: 'Bearer ' + token} : {}
})

// 数据库字典载入
const allIngredients = ref<any[]>([])
const allStandardSteps = ref<any[]>([])

const autoCalculateNutrients = ref(true)

const defaultForm = {
  dishId: null as number | null,
  dishName: '',
  cuisineType: '',
  dietMode: 0,
  calories: 0.0,
  protein: 0.0,
  fat: 0.0,
  carbs: 0.0,
  coverImageId: null as number | null,
  imageIds: '',
  ingredients: [] as any[],
  steps: [] as any[]
}
const form = ref({...defaultForm})

// 加载全部菜谱
const loadDishes = async () => {
  loading.value = true
  try {
    let url = '/api/dish/list?'
    if (searchName.value.trim()) {
      url += `dishName=${encodeURIComponent(searchName.value.trim())}&`
    }
    if (searchCuisine.value.trim()) {
      url += `cuisineType=${encodeURIComponent(searchCuisine.value.trim())}&`
    }
    dishes.value = await request.get(url)
    // 前端根据 dietMode 过滤
    if (filterMode.value !== null) {
      dishes.value = dishes.value.filter(d => d.dietMode === filterMode.value)
    }
  } catch (e) {
    console.error('获取菜谱列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchName.value = ''
  searchCuisine.value = ''
  filterMode.value = null
  loadDishes()
}

// 载入基础词典
const loadDicts = async () => {
  try {
    allIngredients.value = await request.get('/api/ingredient/list')
    allStandardSteps.value = await request.get('/api/step/list')
    const dictRes = await request.get('/sys/dict/list?dataType=cuisine_type')
    allCuisineTypes.value = dictRes.data || []
  } catch (e) {
    console.error('加载标准字典失败', e)
  }
}

const getUnit = (ingredientId: number) => {
  const ing = allIngredients.value.find(i => i.ingredientId === ingredientId)
  return ing ? ing.measureUnit : 'g'
}

const getModeText = (mode: number) => {
  if (mode === 1) return '轻食减脂'
  if (mode === 2) return '放纵餐'
  return '正常饮食'
}

const getModeClass = (mode: number) => {
  if (mode === 1) return 'badge-lime'
  if (mode === 2) return 'badge-red'
  return 'badge-gray'
}

// 动态行：配料
const addIngredientRow = () => {
  form.value.ingredients.push({
    ingredientId: null,
    useAmount: 100,
    mainMaterialFlag: 1
  })
}

const removeIngredientRow = (index: number) => {
  form.value.ingredients.splice(index, 1)
}

// 动态行：做法步骤
const addStepRow = () => {
  form.value.steps.push({
    stepPoolId: null,
    stepNum: form.value.steps.length + 1,
    customDetail: ''
  })
}

const removeStepRow = (index: number) => {
  form.value.steps.splice(index, 1)
  // 重算步骤序号
  form.value.steps.forEach((s, idx) => {
    s.stepNum = idx + 1
  })
}

const moveStepUp = (index: number) => {
  if (index === 0) return
  const temp = form.value.steps[index]
  form.value.steps[index] = form.value.steps[index - 1]
  form.value.steps[index - 1] = temp
  // 重置序号
  form.value.steps.forEach((s, idx) => {
    s.stepNum = idx + 1
  })
}

const moveStepDown = (index: number) => {
  if (index === form.value.steps.length - 1) return
  const temp = form.value.steps[index]
  form.value.steps[index] = form.value.steps[index + 1]
  form.value.steps[index + 1] = temp
  // 重置序号
  form.value.steps.forEach((s, idx) => {
    s.stepNum = idx + 1
  })
}

const handleStepTemplateChange = (val: any, index: number) => {
  if (!val) return
  const template = allStandardSteps.value.find(p => p.stepPoolId === val)
  if (template) {
    form.value.steps[index].customDetail = template.stepDetail
  }
}

// 打开新增
const openCreateModal = () => {
  fileList.value = []
  form.value = {
    ...defaultForm,
    ingredients: [],
    steps: []
  }
  autoCalculateNutrients.value = true
  modalVisible.value = true
}

// 打开编辑，获取菜品完整明细数据
const openEditModal = async (dishId: number) => {
  loading.value = true
  try {
    const detail: any = await request.get(`/api/dish/detail/${dishId}`)
    const dish = detail.dish

    // 处理图片回显
    if (dish.imageIds && dish.imageIds.trim()) {
      const ids = dish.imageIds.split(',')
      fileList.value = ids.map((id: string) => ({
        name: `image-${id}`,
        storageId: Number(id),
        url: `${BASE_URL}/api/s3Storage/preview/${id}`
      }))
    } else if (dish.coverImageId) {
      fileList.value = [{
        name: `image-${dish.coverImageId}`,
        storageId: dish.coverImageId,
        url: `${BASE_URL}/api/s3Storage/preview/${dish.coverImageId}`
      }]
    } else {
      fileList.value = []
    }

    form.value = {
      dishId: dish.dishId,
      dishName: dish.dishName,
      cuisineType: dish.cuisineType,
      dietMode: dish.dietMode,
      calories: dish.calories,
      protein: dish.protein,
      fat: dish.fat,
      carbs: dish.carbs,
      coverImageId: dish.coverImageId,
      imageIds: dish.imageIds || '',
      ingredients: detail.ingredients.map((i: any) => ({
        ingredientId: i.ingredientId,
        useAmount: Number(i.useAmount),
        mainMaterialFlag: i.mainMaterialFlag
      })),
      steps: detail.steps.map((s: any) => {
        // 查找可能关联的步骤ID
        const matchedPool = allStandardSteps.value.find(p => p.stepDetail === s.stepDetail)
        return {
          stepPoolId: matchedPool ? matchedPool.stepPoolId : null,
          stepNum: s.stepNum,
          customDetail: s.stepDetail
        }
      })
    }

    // 如果热量值大于 0，说明上次未开启自动重算或自定义录入了数据，默认不勾选自动折算
    autoCalculateNutrients.value = !(dish.calories && Number(dish.calories) > 0 && form.value.ingredients.length > 0)

    modalVisible.value = true
  } catch (e) {
    console.error('获取菜谱详情失败', e)
  } finally {
    loading.value = false
  }
}

// 图片上传相关逻辑方法
const beforeUpload = (file: any) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('上传图片大小不能超过 10MB!')
  }
  return isLt10M
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 3 张图片！')
}

const handleUploadSuccess = (response: any, uploadFile: any) => {
  if (response && response.code === 200) {
    uploadFile.storageId = response.data.storageId
    uploadFile.url = `${BASE_URL}/api/s3Storage/preview/${response.data.storageId}`
    syncImageIds()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleRemove = () => {
  syncImageIds()
}

const syncImageIds = () => {
  const ids = fileList.value
      .map(f => f.storageId || (f.response && f.response.code === 200 ? f.response.data.storageId : null))
      .filter(id => id !== null && id !== undefined)
  form.value.imageIds = ids.join(',')
}

// 保存
const handleSave = async () => {
  if (!form.value.dishName.trim()) {
    ElMessage.warning('请输入菜品名称！')
    return
  }
  if (!form.value.cuisineType || !form.value.cuisineType.trim()) {
    ElMessage.warning('请选择菜系类型！')
    return
  }

  // 同步成品图 ID 列表
  syncImageIds()

  // 如果自动核算，前端重算值发 0 触发后端自动核算
  const payload = {...form.value}
  if (autoCalculateNutrients.value) {
    payload.calories = 0
    payload.protein = 0
    payload.fat = 0
    payload.carbs = 0
  }

  // 基础必填验证
  for (let i = 0; i < payload.ingredients.length; i++) {
    const item = payload.ingredients[i]
    if (!item.ingredientId) {
      ElMessage.warning(`配料表第 ${i + 1} 行没有选择原材料！`)
      return
    }
    if (!item.useAmount || Number(item.useAmount) <= 0) {
      ElMessage.warning(`配料表第 ${i + 1} 行用量必须大于 0！`)
      return
    }
  }

  try {
    const res = await request.post('/api/dish/save', payload)
    if (res) {
      ElMessage.success('菜谱保存成功且指标重算！')
      modalVisible.value = false
      loadDishes()
    }
  } catch (e) {
    console.error('保存菜谱失败', e)
  }
}

// 软删除菜谱
const handleDelete = (dishId: number) => {
  ElMessageBox.confirm(
      '确认删除该菜谱？删除后，当前膳食计划的推荐列表将不再包含它，但以往的打卡历史记录不受影响。',
      '警告',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/dish/delete/${dishId}`)
      if (res) {
        ElMessage.success('菜品删除成功！')
        loadDishes()
      }
    } catch (e) {
      console.error('删除菜谱失败', e)
    }
  }).catch(() => {
  })
}

onMounted(() => {
  loadDishes()
  loadDicts()
})
</script>

<style scoped>
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  gap: 10px;
  align-items: center;
}

.dish-preview-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--rounded-sm);
  border: 1px solid var(--hairline);
}

.mode-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--rounded-xs);
  font-size: 10px;
  font-weight: 600;
}

.badge-lime {
  background-color: #dcfce7;
  color: #15803d;
}

.badge-red {
  background-color: #fee2e2;
  color: #b91c1c;
}

.badge-gray {
  background-color: #f3f4f6;
  color: #4b5563;
}

.unit {
  font-size: 11px;
  color: var(--ink-subtle);
}

/* 弹出编辑器布局 */
.dish-editor-scrollable {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.section-title {
  border-bottom: 1px solid var(--hairline);
  padding-bottom: 6px;
  margin-bottom: 16px;
}

.section-title h3 {
  font-size: 16px;
  font-weight: 700;
  margin-top: 4px;
}

.sub-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-xs {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: var(--rounded-xs);
}

.editor-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-row-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 动态列表配料 */
.empty-dynamic-placeholder {
  text-align: center;
  padding: 24px;
  background-color: var(--surface-2);
  border-radius: var(--rounded-md);
}

.dynamic-rows-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.row-header-labels {
  display: flex;
  gap: 16px;
  padding: 0 4px;
}

.label-col {
  flex: 1;
}

.dynamic-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.row-col {
  flex: 1;
}

.flex-row-align {
  display: flex;
  align-items: center;
}

.btn-delete-row {
  background: transparent;
  border: none;
  color: var(--ink-tertiary);
  cursor: pointer;
  font-size: 16px;
  padding: 6px;
  transition: color 0.15s ease;
}

.btn-delete-row:hover {
  color: var(--primary);
}

/* 步骤工序编辑 */
.steps-rows-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-card-editor {
  border-radius: var(--rounded-md);
  background-color: var(--surface-2);
  padding: 12px;
}

.step-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.step-num {
  font-size: 12px;
  font-weight: 700;
  color: var(--primary);
}

.step-template-select-col {
  display: flex;
  align-items: center;
}

.step-actions-col {
  display: flex;
  gap: 4px;
}

.btn-step-act {
  background-color: var(--surface-1);
  border: 1px solid var(--hairline);
  color: var(--ink-muted);
  font-size: 10px;
  padding: 4px 8px;
  border-radius: var(--rounded-xs);
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-step-act:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-step-act:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.text-danger {
  color: #ee0000 !important;
}

.step-card-body {
  width: 100%;
}
</style>
