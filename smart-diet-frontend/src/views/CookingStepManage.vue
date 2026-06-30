<template>
  <div class="content-container section-gap">
    <el-card class="cooking-step-manage-card">
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <Notebook/>
          </el-icon>
          烹饪步骤模板管理
        </h3>
        <span class="sub-title">系统膳食烹饪标准步骤模板维护，规范多渠道烹饪环节命名与默认细化指导说明</span>
      </div>

      <!-- 搜索与操作栏（合并为单行展示） -->
      <div class="search-bar" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap;">
        <el-form :inline="true" @submit.prevent style="margin-bottom: 0;">
          <el-form-item label="步骤名称">
            <el-input
                v-model="searchQuery"
                placeholder="搜索步骤名称..."
                clearable
                @keyup.enter="handleSearch"
                style="width: 240px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="action-buttons">
          <el-button type="primary" @click="openCreateModal">
            <el-icon style="margin-right: 4px;">
              <Plus/>
            </el-icon>
            新增步骤模板
          </el-button>
        </div>
      </div>

      <!-- 操作步骤表格 -->
      <el-table
          v-loading="loading"
          :data="steps"
          border
          max-height="calc(100vh - 240px)"
          style="width: 100%; margin-top: 10px"
      >
        <el-table-column prop="stepPoolId" label="步骤ID" width="100" align="center"/>
        <el-table-column prop="stepName" label="步骤名称" width="200"/>
        <el-table-column prop="stepDetail" label="默认步骤描述" min-width="400" show-overflow-tooltip/>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openEditModal(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.stepPoolId)">删除</el-button>
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

    <!-- 新增 / 编辑步骤模板 Dialog -->
    <el-dialog
        v-model="modalVisible"
        :title="form.stepPoolId ? '编辑步骤模板' : '新增步骤模板'"
        width="500px"
        custom-class="premium-dialog"
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
import {Notebook, Plus} from '@element-plus/icons-vue'
import request from '../utils/request'

const loading = ref(false)
const steps = ref<any[]>([])
const searchQuery = ref('')
const modalVisible = ref(false)

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

const defaultForm = {
  stepPoolId: null as number | null,
  stepName: '',
  stepDetail: ''
}
const form = ref({...defaultForm})

// 加载步骤列表 (分页)
const loadSteps = async () => {
  loading.value = true
  try {
    const res: any = await request.post('/api/step/page', {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      name: searchQuery.value.trim() || null
    })
    if (res && res.code === 200) {
      steps.value = res.data || []
      totalCount.value = res.page ? res.page.total : steps.value.length
    }
  } catch (e) {
    console.error('获取步骤列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadSteps()
}

const resetSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
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
      const res = await request.post('/api/step/delete', {id: stepPoolId})
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

// 分页切换处理器
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadSteps()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadSteps()
}

onMounted(() => {
  loadSteps()
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

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
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
