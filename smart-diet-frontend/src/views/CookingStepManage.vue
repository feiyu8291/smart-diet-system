<template>
  <div class="content-container section-gap">
    <div class="header-section">
      <span class="eyebrow">DIET PREPARATION STEPS</span>
      <h1 class="display-lg">烹饪步骤库管理</h1>
    </div>

    <div class="cooking-step-manage-card color-block">
      <!-- 顶部搜索栏与操作按钮 -->
      <div class="toolbar-section">
        <div class="search-form">
          <input
              type="text"
              v-model="searchQuery"
              class="input-text"
              placeholder="搜索步骤名称..."
              @keyup.enter="loadSteps"
              style="width: 260px;"
          />
          <button class="btn-primary" @click="loadSteps">搜索</button>
          <button class="btn-secondary" @click="resetSearch">重置</button>
        </div>
        <button class="btn-primary" @click="openCreateModal">
          + 新增步骤模板
        </button>
      </div>

      <!-- 操作步骤表格 -->
      <el-table
          v-loading="loading"
          :data="steps"
          style="width: 100%; margin-top: 20px"
          class="custom-table"
      >
        <el-table-column prop="stepPoolId" label="步骤ID" width="100" align="center"/>
        <el-table-column prop="stepName" label="步骤名称" width="200"/>
        <el-table-column prop="stepDetail" label="默认步骤描述" min-width="400" show-overflow-tooltip/>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openEditModal(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.stepPoolId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增 / 编辑步骤模板 Dialog -->
    <el-dialog
        v-model="modalVisible"
        :title="form.stepPoolId ? '编辑步骤模板' : '新增步骤模板'"
        width="500px"
    >
      <div class="step-form">
        <div class="form-item">
          <label class="caption">步骤名称</label>
          <input
              type="text"
              v-model="form.stepName"
              class="input-text"
              placeholder="例如: 焯水预处理、食材改刀、慢火小炒"
          />
        </div>
        <div class="form-item">
          <label class="caption">详细步骤说明描述</label>
          <textarea
              v-model="form.stepDetail"
              class="input-text"
              rows="6"
              placeholder="请输入本步骤的默认指导说明..."
              style="resize: vertical; font-family: inherit; line-height: 1.5;"
          ></textarea>
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
const steps = ref<any[]>([])
const searchQuery = ref('')
const modalVisible = ref(false)

const defaultForm = {
  stepPoolId: null as number | null,
  stepName: '',
  stepDetail: ''
}
const form = ref({...defaultForm})

// 加载步骤列表
const loadSteps = async () => {
  loading.value = true
  try {
    const url = searchQuery.value.trim()
        ? `/api/step/list?name=${encodeURIComponent(searchQuery.value.trim())}`
        : '/api/step/list'
    steps.value = await request.get(url)
  } catch (e) {
    console.error('获取步骤列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchQuery.value = ''
  loadSteps()
}

const openCreateModal = () => {
  form.value = {...defaultForm}
  modalVisible.value = true
}

const openEditModal = (row: any) => {
  form.value = {...row}
  modalVisible.value = true
}

// 保存步骤模板
const handleSave = async () => {
  if (!form.value.stepName.trim()) {
    ElMessage.warning('请输入步骤名称！')
    return
  }
  if (!form.value.stepDetail.trim()) {
    ElMessage.warning('请输入详细步骤说明！')
    return
  }

  try {
    const res = await request.post('/api/step/save', form.value)
    if (res) {
      ElMessage.success('保存成功！')
      modalVisible.value = false
      loadSteps()
    }
  } catch (e) {
    console.error('保存步骤失败', e)
  }
}

// 软删除步骤模板
const handleDelete = (stepPoolId: number) => {
  ElMessageBox.confirm(
      '确认删除该操作步骤模板？删除后新建菜品将无法直接引入此步骤。',
      '提示',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/step/delete/${stepPoolId}`)
      if (res) {
        ElMessage.success('删除成功！')
        loadSteps()
      }
    } catch (e) {
      console.error('删除步骤模板失败', e)
    }
  }).catch(() => {
  })
}

onMounted(() => {
  loadSteps()
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

.step-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>
