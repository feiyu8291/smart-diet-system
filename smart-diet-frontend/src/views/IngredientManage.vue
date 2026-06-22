<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">DIET RAW INGREDIENTS</span>
      <h1 class="display-lg">原材料库管理</h1>
    </div>

    <div class="ingredient-manage-card color-block">
      <!-- 顶部过滤与搜索 -->
      <div class="toolbar-section">
        <div class="search-form">
          <input
              type="text"
              v-model="searchName"
              class="input-text"
              placeholder="搜索原材料名称..."
              @keyup.enter="loadIngredients"
              style="width: 240px;"
          />
          <el-select v-model="filterCondiment" placeholder="类型过滤" style="width: 150px" @change="loadIngredients">
            <el-option :value="null" label="全部类型"/>
            <el-option :value="0" label="主配配料 (克/g)"/>
            <el-option :value="1" label="调味辅料 (克/毫升)"/>
          </el-select>
          <button class="btn-primary" @click="loadIngredients">搜索</button>
          <button class="btn-secondary" @click="resetSearch">重置</button>
        </div>
        <button class="btn-primary" @click="openCreateModal">
          + 新增原材料
        </button>
      </div>

      <!-- 原材料数据表格 -->
      <el-table
          v-loading="loading"
          :data="ingredients"
          style="width: 100%; margin-top: 20px"
          class="custom-table"
      >
        <el-table-column prop="ingredientId" label="ID" width="80" align="center"/>
        <el-table-column prop="ingredientName" label="原材料名称" width="160"/>
        <el-table-column prop="condimentFlag" label="类型" width="120" align="center">
          <template #default="scope">
            <span :class="['type-badge', scope.row.condimentFlag === 1 ? 'type-condiment' : 'type-ingredient']">
              {{ scope.row.condimentFlag === 1 ? '调味品' : '主配料' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="measureUnit" label="单位" width="100" align="center"/>
        <el-table-column label="热量 (per 100g)" width="140" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.calories || 0 }}</span> <span class="unit">kcal</span>
          </template>
        </el-table-column>
        <el-table-column label="蛋白质 (per 100g)" width="150" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.protein || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="脂肪 (per 100g)" width="140" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.fat || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="碳水 (per 100g)" width="140" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.carbs || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openEditModal(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.ingredientId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增 / 编辑 Dialog -->
    <el-dialog
        v-model="modalVisible"
        :title="form.ingredientId ? '编辑原材料' : '新增原材料'"
        width="540px"
    >
      <div class="ingredient-form">
        <!-- 基本属性 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">原材料名称</label>
            <input
                type="text"
                v-model="form.ingredientName"
                class="input-text"
                placeholder="例如: 鸡胸肉、生抽、橄榄油"
            />
          </div>
          <div class="form-item">
            <label class="caption">计量单位</label>
            <el-select v-model="form.measureUnit" placeholder="请选择单位" style="width: 100%">
              <el-option value="g" label="克 (g)"/>
              <el-option value="ml" label="毫升 (ml)"/>
              <el-option value="个" label="个"/>
              <el-option value="勺" label="勺"/>
            </el-select>
          </div>
        </div>

        <div class="form-item">
          <label class="caption">类型属性</label>
          <el-select v-model="form.condimentFlag" placeholder="主料还是调味料" style="width: 100%">
            <el-option :value="0" label="主配配料 (如鸡胸肉、西兰花，配餐计算主力)"/>
            <el-option :value="1" label="调味辅料 (如生抽、豆瓣酱、食用油，提供风味)"/>
          </el-select>
        </div>

        <!-- 营养素属性 (每100克或100毫升) -->
        <div class="form-divider-title">
          <span class="caption">营养素指标 (每 100 克 / 毫升)</span>
        </div>

        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">热量 (kcal)</label>
            <input type="number" v-model="form.calories" class="input-text" step="0.1" placeholder="每100g热量"/>
          </div>
          <div class="form-item">
            <label class="caption">蛋白质 (g)</label>
            <input type="number" v-model="form.protein" class="input-text" step="0.1" placeholder="每100g蛋白质"/>
          </div>
        </div>

        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">脂肪 (g)</label>
            <input type="number" v-model="form.fat" class="input-text" step="0.1" placeholder="每100g脂肪"/>
          </div>
          <div class="form-item">
            <label class="caption">碳水化合物 (g)</label>
            <input type="number" v-model="form.carbs" class="input-text" step="0.1" placeholder="每100g碳水"/>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" style="margin-right: 10px" @click="modalVisible = false">取消</button>
        <button class="btn-primary" @click="handleSave">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import request from '../utils/request'

const loading = ref(false)
const ingredients = ref<any[]>([])
const searchName = ref('')
const filterCondiment = ref<number | null>(null)
const modalVisible = ref(false)

const defaultForm = {
  ingredientId: null as number | null,
  ingredientName: '',
  calories: 0.0,
  protein: 0.0,
  fat: 0.0,
  carbs: 0.0,
  measureUnit: 'g',
  condimentFlag: 0 // 默认主材料
}
const form = ref({...defaultForm})

// 加载原料
const loadIngredients = async () => {
  loading.value = true
  try {
    let url = '/api/ingredient/list?'
    if (searchName.value.trim()) {
      url += `name=${encodeURIComponent(searchName.value.trim())}&`
    }
    if (filterCondiment.value !== null) {
      url += `condimentFlag=${filterCondiment.value}&`
    }
    ingredients.value = await request.get(url)
  } catch (e) {
    console.error('获取原材料列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchName.value = ''
  filterCondiment.value = null
  loadIngredients()
}

const openCreateModal = () => {
  form.value = {...defaultForm}
  modalVisible.value = true
}

const openEditModal = (row: any) => {
  form.value = {...row}
  modalVisible.value = true
}

const handleSave = async () => {
  if (!form.value.ingredientName.trim()) {
    ElMessage.warning('请输入原料名称！')
    return
  }
  if (form.value.calories === null || form.value.calories < 0) {
    ElMessage.warning('请输入合理的热量值！')
    return
  }
  if (form.value.protein === null || form.value.protein < 0) {
    ElMessage.warning('请输入合理的蛋白质含量！')
    return
  }
  if (form.value.fat === null || form.value.fat < 0) {
    ElMessage.warning('请输入合理的脂肪含量！')
    return
  }
  if (form.value.carbs === null || form.value.carbs < 0) {
    ElMessage.warning('请输入合理的碳水含量！')
    return
  }

  try {
    const res = await request.post('/api/ingredient/save', form.value)
    if (res) {
      ElMessage.success('保存成功！')
      modalVisible.value = false
      loadIngredients()
    }
  } catch (e) {
    console.error('保存原料失败', e)
  }
}

// 软删除原料
const handleDelete = (ingredientId: number) => {
  ElMessageBox.confirm(
      '确认删除该原材料？删除后正在使用此原料的菜谱数据不会被自动删除，但可能导致重算营养比例失效。',
      '提示',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/ingredient/delete/${ingredientId}`)
      if (res) {
        ElMessage.success('删除成功！')
        loadIngredients()
      }
    } catch (e) {
      console.error('删除原料失败', e)
    }
  }).catch(() => {
  })
}

onMounted(() => {
  loadIngredients()
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

.type-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--rounded-xs);
  font-size: 11px;
  font-weight: 500;
}

.type-ingredient {
  background-color: rgba(39, 166, 68, 0.1);
  color: var(--semantic-success);
}

.type-condiment {
  background-color: rgba(255, 56, 92, 0.1);
  color: var(--primary);
}

.unit {
  font-size: 11px;
  color: var(--ink-subtle);
}

.ingredient-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-divider-title {
  border-bottom: 1px dashed var(--hairline);
  padding-bottom: 4px;
  margin-top: 10px;
}
</style>
