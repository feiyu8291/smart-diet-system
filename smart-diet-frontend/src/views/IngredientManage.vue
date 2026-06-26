<template>
  <div class="content-container section-gap">
    <el-card class="ingredient-manage-card">
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <Memo/>
          </el-icon>
          原材料库管理
        </h3>
        <span class="sub-title">系统膳食原材料数据维护，包含荤菜类、素菜类、调辅配料、基础调味分类设定与每百克基础营养占比</span>
      </div>

      <!-- 搜索与操作栏（合并为单行展示） -->
      <div class="search-bar" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap;">
        <el-form :inline="true" @submit.prevent style="margin-bottom: 0;">
          <el-form-item label="原材料名称">
            <el-input
                v-model="searchName"
                placeholder="搜索原材料名称..."
                clearable
                @keyup.enter="loadIngredients"
                style="width: 200px;"
            />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="filterType" placeholder="类型过滤" style="width: 140px" clearable @change="loadIngredients">
              <el-option :value="null" label="全部类型"/>
              <el-option :value="1" label="荤菜类"/>
              <el-option :value="2" label="素菜类"/>
              <el-option :value="3" label="调辅配料"/>
              <el-option :value="4" label="基础调味"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadIngredients">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="action-buttons">
          <el-button type="primary" @click="openCreateModal">
            <el-icon style="margin-right: 4px;">
              <Plus/>
            </el-icon>
            新增原材料
          </el-button>
        </div>
      </div>

      <!-- 原材料数据表格 -->
      <el-table
          v-loading="loading"
          :data="ingredients"
          border
          max-height="calc(100vh - 240px)"
          style="width: 100%; margin-top: 10px"
      >
        <el-table-column prop="ingredientId" label="ID" width="80" align="center"/>
        <el-table-column prop="ingredientName" label="原材料名称" min-width="150"/>
        <el-table-column prop="ingredientType" label="类型" width="105" align="center">
          <template #default="scope">
            <span v-if="scope.row.ingredientType === 1" class="type-badge type-meat">{{ scope.row.ingredientTypeLabel }}</span>
            <span v-else-if="scope.row.ingredientType === 2" class="type-badge type-veg">{{ scope.row.ingredientTypeLabel }}</span>
            <span v-else-if="scope.row.ingredientType === 3" class="type-badge type-side">{{ scope.row.ingredientTypeLabel }}</span>
            <span v-else-if="scope.row.ingredientType === 4" class="type-badge type-condiment">{{ scope.row.ingredientTypeLabel }}</span>
            <span v-else class="type-badge type-unknown">{{ scope.row.ingredientTypeLabel || '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ingredientDesc" label="食材描述(健康选购及烹饪备注)" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="measureUnit" label="单位" width="80" align="center"/>
        <el-table-column label="热量 (100g)" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.calories || 0 }}</span> <span class="unit">kcal</span>
          </template>
        </el-table-column>
        <el-table-column label="蛋白质 (100g)" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.protein || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="脂肪 (100g)" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.fat || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="碳水 (100g)" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.carbs || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openEditModal(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.ingredientId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 真分页器 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="totalCount"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

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
          <el-select v-model="form.ingredientType" placeholder="请选择食材分类" style="width: 100%">
            <el-option :value="1" label="荤菜类 (如畜禽肉、蛋类、乳制品)"/>
            <el-option :value="2" label="素菜类 (如蔬菜、豆制品、主食粗粮)"/>
            <el-option :value="3" label="调辅配料 (如葱姜蒜、香料、干辣椒等)"/>
            <el-option :value="4" label="基础调味 (如食用油、盐、生抽、醋、豆瓣酱等)"/>
          </el-select>
        </div>

        <div class="form-item">
          <label class="caption">食材描述(健康选购及烹饪备注)</label>
          <el-input
              type="textarea"
              v-model="form.ingredientDesc"
              :rows="3"
              placeholder="输入食材的健康选购及烹饪备注信息，如：低脂高蛋白、控量使用等"
              maxlength="500"
              show-word-limit
          />
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
import {Memo, Plus} from '@element-plus/icons-vue'
import request from '../utils/request'

const loading = ref(false)
const ingredients = ref<any[]>([])
const searchName = ref('')
const filterType = ref<number | null>(null)
const modalVisible = ref(false)

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

const defaultForm = {
  ingredientId: null as number | null,
  ingredientName: '',
  calories: 0.0,
  protein: 0.0,
  fat: 0.0,
  carbs: 0.0,
  measureUnit: 'g',
  ingredientType: 1, // 默认荤菜类
  ingredientDesc: ''
}
const form = ref({...defaultForm})

// 加载原料 (分页)
const loadIngredients = async () => {
  loading.value = true
  try {
    let url = `/api/ingredient/page?pageNo=${currentPage.value}&pageSize=${pageSize.value}`
    if (searchName.value.trim()) {
      url += `&name=${encodeURIComponent(searchName.value.trim())}`
    }
    if (filterType.value !== null && filterType.value !== undefined && String(filterType.value) !== 'undefined') {
      url += `&ingredientType=${filterType.value}`
    }
    const res: any = await request.get(url)
    if (res && res.code === 200) {
      ingredients.value = res.data || []
      totalCount.value = res.page ? res.page.total : ingredients.value.length
    }
  } catch (e) {
    console.error('获取原材料列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchName.value = ''
  filterType.value = null
  currentPage.value = 1
  loadIngredients()
}

// 分页切换处理器
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadIngredients()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
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
.panel-header-section {
  margin-bottom: 24px;
  border-bottom: 1px solid var(--hairline);
  padding-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 500;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px 0;
}

.title-icon {
  font-size: 20px;
  color: var(--primary);
}

.sub-title {
  font-size: 13px;
  color: var(--ink-subtle);
  display: block;
}

.search-bar {
  margin-bottom: 20px;
}

.operation-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.type-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--rounded-xs);
  font-size: 11px;
  font-weight: 500;
}

.type-meat {
  background-color: rgba(220, 53, 69, 0.08); /* 红色 */
  color: #dc3545;
}

.type-veg {
  background-color: rgba(40, 167, 69, 0.08); /* 绿色 */
  color: #28a745;
}

.type-side {
  background-color: rgba(0, 123, 255, 0.08); /* 蓝色 */
  color: #007bff;
}

.type-condiment {
  background-color: rgba(253, 126, 20, 0.08); /* 黄橙色 */
  color: #fd7e14;
}

.type-unknown {
  background-color: rgba(108, 117, 125, 0.08);
  color: #6c757d;
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
