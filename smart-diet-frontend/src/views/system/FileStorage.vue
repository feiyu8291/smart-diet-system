<!-- src/views/system/FileStorage.vue - S3 文件存储管理 -->
<template>
  <div class="storage-container">
    <el-card class="glass-card" shadow="never">
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <FolderOpened/>
          </el-icon>
          文件存储管理
        </h3>
        <span class="sub-title">基于 S3 协议的文件存储中心，支持预览与批量管理</span>
      </div>

      <!-- 查询栏 -->
      <div class="search-wrapper">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="文件名称">
            <el-input v-model="queryForm.fileName" placeholder="请输入文件名模糊检索" clearable/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon>
                <Search/>
              </el-icon>
              查询
            </el-button>
            <el-button @click="resetQuery">
              <el-icon>
                <Refresh/>
              </el-icon>
              重置
            </el-button>
            <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
              <el-icon>
                <Delete/>
              </el-icon>
              批量删除
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
          v-loading="loading"
          :data="tableData"
          class="custom-table"
          style="width:100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column prop="storageId" label="ID" width="90" align="center"/>
        <el-table-column label="文件预览" width="120" align="center">
          <template #default="scope">
            <div class="preview-cell">
              <img
                  v-if="isImage(scope.row.fileType)"
                  :src="`${BASE_URL}/api/s3Storage/preview/${scope.row.storageId}`"
                  class="thumbnail"
                  @click="handlePreview(scope.row)"
                  alt="预览"
              />
              <el-button v-else link type="primary" @click="handlePreview(scope.row)">
                <el-icon>
                  <View/>
                </el-icon>
                预览
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="原文件名" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="fileRealName" label="存储文件名" min-width="260" show-overflow-tooltip/>
        <el-table-column prop="fileSize" label="文件大小" width="120" align="center"/>
        <el-table-column prop="fileType" label="类型" width="80" align="center">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ scope.row.fileType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="上传人" width="110" align="center"/>
        <el-table-column prop="createTime" label="上传时间" min-width="170" align="center"/>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
              <el-icon>
                <Delete/>
              </el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryForm.page"
            v-model:page-size="queryForm.size"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 文件预览对话框 -->
    <el-dialog
        v-model="previewDialogVisible"
        title="文件预览"
        width="680px"
        destroy-on-close
        class="custom-dialog"
    >
      <div class="preview-dialog-content">
        <div v-if="previewLoading" class="loading-placeholder">
          <el-icon class="loading-icon">
            <Loading/>
          </el-icon>
          <span>加载预览中...</span>
        </div>
        <img v-else-if="currentPreviewIsImage && currentPreviewSrc" :src="currentPreviewSrc" class="full-preview-img"
             alt="预览"/>
        <div v-else-if="currentPreviewSrc" class="file-download-hint">
          <el-icon class="file-icon">
            <Document/>
          </el-icon>
          <p>该文件类型暂不支持在线预览</p>
          <a :href="currentPreviewSrc" :download="currentPreviewName" class="download-link">
            <el-button type="primary">
              <el-icon>
                <Download/>
              </el-icon>
              点击下载文件
            </el-button>
          </a>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Document, Download, FolderOpened, Loading, Refresh, Search, View} from '@element-plus/icons-vue';
import {deleteStorageFiles, getFileStoragePage} from '../../api/system';
import {API_BASE_URL} from '../../config';

const BASE_URL = API_BASE_URL;

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const selectedIds = ref<any[]>([]);
const selectedRows = ref<any[]>([]);
const previewDialogVisible = ref(false);
const previewLoading = ref(false);
const currentPreviewSrc = ref('');
const currentPreviewIsImage = ref(false);
const currentPreviewName = ref('');

const queryForm = ref({
  page: 1,
  size: 10,
  fileName: ''
});

const isImage = (type: any) => ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes((type || '').toLowerCase());

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getFileStoragePage(queryForm.value);
    if (res.code === 200) {
      tableData.value = res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.msg || '获取文件列表失败');
    }
  } catch (error) {
    console.error('获取文件列表出错', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryForm.value.page = 1;
  fetchList();
};

const resetQuery = () => {
  queryForm.value = {page: 1, size: 10, fileName: ''};
  fetchList();
};

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(r => r.storageId);
  selectedRows.value = selection;
};

// 单文件预览
const handlePreview = (row: any) => {
  previewDialogVisible.value = true;
  previewLoading.value = false;
  currentPreviewIsImage.value = isImage(row.fileType);
  currentPreviewName.value = row.fileName;
  currentPreviewSrc.value = `${BASE_URL}/api/s3Storage/preview/${row.storageId}`;
};

// 单文件删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除文件【${row.fileName}】吗？删除后无法恢复！`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    });
    const res = await deleteStorageFiles([{storageId: row.storageId, bucketName: row.bucketName}]);
    if (res.code === 200) {
      ElMessage.success('文件删除成功');
      fetchList();
    } else {
      ElMessage.error(res.msg || '删除失败');
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除文件出错', e);
  }
};

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确认批量删除 ${selectedRows.value.length} 个文件吗？删除后无法恢复！`, '批量删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    });
    const deleteList = selectedRows.value.map(row => ({
      storageId: row.storageId,
      bucketName: row.bucketName
    }));
    const res = await deleteStorageFiles(deleteList);
    if (res.code === 200) {
      ElMessage.success('批量删除成功');
      selectedIds.value = [];
      selectedRows.value = [];
      fetchList();
    } else {
      ElMessage.error(res.msg || '批量删除失败');
    }
  } catch (e) {
    if (e !== 'cancel') console.error('批量删除出错', e);
  }
};

const handleSizeChange = (val: any) => {
  queryForm.value.size = val;
  fetchList();
};
const handleCurrentChange = (val: any) => {
  queryForm.value.page = val;
  fetchList();
};

onMounted(fetchList);
</script>

<style lang="scss" scoped>
.preview-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #e5e7eb;
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.1);
  }
}

.preview-dialog-content {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;

  .loading-placeholder {
    color: #6b7280;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;

    .loading-icon {
      font-size: 32px;
      animation: spin 1s linear infinite;
    }
  }

  .full-preview-img {
    max-width: 100%;
    max-height: 500px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .file-download-hint {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    color: #6b7280;

    .file-icon {
      font-size: 64px;
      color: #9ca3af;
    }

    .download-link {
      text-decoration: none;
    }
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
