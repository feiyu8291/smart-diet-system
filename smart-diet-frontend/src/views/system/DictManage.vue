<template>
  <div class="dict-manage">
    <el-card>
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <Notebook/>
          </el-icon>
          数据字典管理
        </h3>
        <span class="sub-title">系统基础性数据字典值维护，方便全局统一管理下拉值选项与语义代码映射</span>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchParams">
          <el-form-item label="字典类型">
            <el-select v-model="searchParams.dataType" placeholder="请选择或输入字典类型" clearable filterable
                       allow-create style="width: 220px">
              <el-option
                  v-for="item in dataTypeList"
                  :key="item.dataType"
                  :label="item.dataTypeName + ' (' + item.dataType + ')'"
                  :value="item.dataType"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="字典名称">
            <el-input v-model="searchParams.dataCode" placeholder="请输入字典名称" clearable/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="operation-bar">
        <el-button type="primary" @click="handleAdd">新增字典</el-button>
        <el-button type="warning" @click="handleRefreshCache" :loading="refreshLoading">刷新缓存</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
          v-loading="loading"
          :data="tableData"
          @selection-change="handleSelectionChange"
          border
          style="width: 100%"
      >
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column prop="dataTypeName" label="类型名称"/>
        <el-table-column prop="dataType" label="类型编码"/>
        <el-table-column prop="dataCode" label="数据编码"/>
        <el-table-column prop="dataValue" label="数据值"/>
        <el-table-column prop="parentType" label="上级类型"/>
        <el-table-column prop="parentCode" label="上级编码"/>
        <el-table-column prop="dictSort" label="排序" width="70" align="center"/>
        <el-table-column prop="webReadOnly" label="页面只读" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.webReadOnly === 1 ? 'danger' : 'info'">
              {{ scope.row.webReadOnly === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultState" label="默认选中" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.defaultState === 1 ? 'success' : 'info'">
              {{ scope.row.defaultState === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="initSystemFlag" label="系统预置" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.initSystemFlag === 1 ? 'warning' : 'info'">
              {{ scope.row.initSystemFlag === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataRemark" label="字典备注" show-overflow-tooltip/>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)" :disabled="scope.row.initSystemFlag === 1">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="pageParams.current"
            v-model:page-size="pageParams.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        :title="dialogType === 'add' ? '新增字典' : '编辑字典'"
        v-model="dialogVisible"
        width="500px"
        @close="resetForm"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="110px"
      >
        <el-form-item label="上级标签">
          <el-select
              v-model="selectedParentKey"
              placeholder="请选择上级标签 (可为空)"
              clearable
              filterable
              style="width: 100%"
              @change="handleParentChange"
          >
            <el-option
                v-for="item in firstLevelDicts"
                :key="item.dataType + ',' + item.dataCode"
                :label="item.dataTypeName + ' / ' + item.dataValue"
                :value="item.dataType + ',' + item.dataCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型编码" prop="dataType">
          <el-select
              v-model="formData.dataType"
              filterable
              allow-create
              default-first-option
              :disabled="isTypeDisabled"
              placeholder="请选择或输入类型编码 (如: sys_user_sex)"
              style="width: 100%"
              @change="handleDataTypeChange"
          >
            <el-option
                v-for="item in dataTypeList"
                :key="item.dataType"
                :label="item.dataType + ' (' + item.dataTypeName + ')'"
                :value="item.dataType"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型名称" prop="dataTypeName">
          <el-input v-model="formData.dataTypeName" :disabled="isTypeDisabled"
                    placeholder="请输入类型名称 (如: 用户性别)"/>
        </el-form-item>
        <el-form-item label="数据编码" prop="dataCode">
          <el-input v-model="formData.dataCode" placeholder="请输入数据编码 (如: male)"/>
        </el-form-item>
        <el-form-item label="数据值" prop="dataValue">
          <el-input v-model="formData.dataValue" placeholder="请输入数据值 (如: 男)"/>
        </el-form-item>
        <el-form-item label="页面只读" prop="webReadOnly">
          <el-switch v-model="formData.webReadOnly" :active-value="1" :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="默认选中" prop="defaultState">
          <el-switch v-model="formData.defaultState" :active-value="1" :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="formData.dictSort" :min="0" :max="9999" controls-position="right"
                           style="width: 160px"/>
        </el-form-item>
        <el-form-item label="字典备注" prop="dataRemark">
          <el-input v-model="formData.dataRemark" type="textarea" placeholder="请输入备注"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  deleteDict,
  getDataTypeAll,
  getDictChildren,
  getDictPage,
  getFirstLevelDicts,
  refreshDictCache,
  saveDict,
  updateDict
} from '../../api/system';

// 查询参数
const searchParams = reactive({
  dataType: '',
  dataCode: ''
});

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 10
});

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const selectedIds = ref<number[]>([]);
const dataTypeList = ref<any[]>([]);

// 新型级联选项
const firstLevelDicts = ref<any[]>([]);
const selectedParentKey = ref('');
const isTypeDisabled = ref(false);

// 表单相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const refreshLoading = ref(false);
const formRef = ref();
const formData = reactive({
  dictId: undefined as number | undefined,
  dataTypeName: '',
  dataType: '',
  dataCode: '',
  dataValue: '',
  parentType: '',
  parentCode: '',
  webReadOnly: 0,
  defaultState: 0,
  dictSort: 0,
  dataRemark: ''
});

const formRules = {
  dataType: [{required: true, message: '请输入类型编码', trigger: 'change'}],
  dataCode: [{required: true, message: '请输入数据编码', trigger: 'blur'}],
  dataValue: [{required: true, message: '请输入数据值', trigger: 'blur'}]
};

onMounted(() => {
  fetchDataTypeAll();
  fetchFirstLevelDicts();
  fetchData();
});

// 获取所有一级字典标签
const fetchFirstLevelDicts = async () => {
  try {
    const res = await getFirstLevelDicts();
    if (res.code === 200) {
      firstLevelDicts.value = res.data;
    }
  } catch (error) {
    console.error('获取一级字典标签失败', error);
  }
};

// 获取所有字典类型
const fetchDataTypeAll = async () => {
  try {
    const res = await getDataTypeAll();
    if (res.code === 200) {
      dataTypeList.value = res.data;
    }
  } catch (error) {
    console.error('获取字典类型失败', error);
  }
};

// 获取表格数据
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getDictPage({
      pageNo: pageParams.current,
      pageSize: pageParams.size,
      ...searchParams
    });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('获取数据失败', error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pageParams.current = 1;
  fetchData();
};

// 重置搜索
const resetSearch = () => {
  searchParams.dataType = '';
  searchParams.dataCode = '';
  handleSearch();
};

// 分页
const handleSizeChange = (val: number) => {
  pageParams.size = val;
  fetchData();
};
const handleCurrentChange = (val: number) => {
  pageParams.current = val;
  fetchData();
};

// 选择
const handleSelectionChange = (val: any[]) => {
  selectedIds.value = val.map(item => item.dictId);
};

// 上级标签选择变化
const handleParentChange = async (val: string) => {
  if (!val) {
    formData.parentType = '';
    formData.parentCode = '';
    isTypeDisabled.value = false;
    return;
  }
  const [parentType, parentCode] = val.split(',');
  formData.parentType = parentType;
  formData.parentCode = parentCode;

  try {
    const res = await getDictChildren({parentType, parentCode});
    if (res.code === 200 && res.data && res.data.length > 0) {
      // 存在已有子级，直接锁定类型
      const firstChild = res.data[0];
      formData.dataType = firstChild.dataType;
      formData.dataTypeName = firstChild.dataTypeName;
      isTypeDisabled.value = true;
      ElMessage.info(`已自动匹配并锁定子级字典类型：${firstChild.dataTypeName} (${firstChild.dataType})`);
    } else {
      isTypeDisabled.value = false;
      // 自动生成默认的类型编码和名称建议
      const parentNode = firstLevelDicts.value.find(item => item.dataType === parentType && item.dataCode === parentCode);
      if (parentNode) {
        formData.dataType = `${parentType}_${parentCode}`;
        formData.dataTypeName = `${parentNode.dataValue}子类`;
        ElMessage.success(`未检测到已有子级，已自动生成默认子级类型编码与名称`);
      }
    }
  } catch (error) {
    console.error('获取子级字典失败', error);
  }
};

// 类型编码选择变化
const handleDataTypeChange = (val: string) => {
  const matched = dataTypeList.value.find(item => item.dataType === val);
  if (matched) {
    formData.dataTypeName = matched.dataTypeName;
  }
};

// 新增
const handleAdd = () => {
  dialogType.value = 'add';
  const defaultDataType = searchParams.dataType || '';
  const matched = dataTypeList.value.find(item => item.dataType === defaultDataType);
  const defaultDataTypeName = matched ? matched.dataTypeName : '';

  Object.assign(formData, {
    dictId: undefined,
    dataTypeName: defaultDataTypeName,
    dataType: defaultDataType,
    dataCode: '',
    dataValue: '',
    parentType: '',
    parentCode: '',
    webReadOnly: 0,
    defaultState: 0,
    dictSort: 0,
    dataRemark: ''
  });
  selectedParentKey.value = '';
  isTypeDisabled.value = false;
  fetchFirstLevelDicts();
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogType.value = 'edit';
  Object.assign(formData, {
    dictId: row.dictId,
    dataTypeName: row.dataTypeName,
    dataType: row.dataType,
    dataCode: row.dataCode,
    dataValue: row.dataValue,
    parentType: row.parentType,
    parentCode: row.parentCode,
    webReadOnly: row.webReadOnly,
    defaultState: row.defaultState,
    dictSort: row.dictSort ?? 0,
    dataRemark: row.dataRemark
  });
  if (row.parentType && row.parentCode) {
    selectedParentKey.value = `${row.parentType},${row.parentCode}`;
    isTypeDisabled.value = true;
  } else {
    selectedParentKey.value = '';
    isTypeDisabled.value = false;
  }
  fetchFirstLevelDicts();
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const api = dialogType.value === 'add' ? saveDict : updateDict;
        const res = await api(formData);
        if (res.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功');
          dialogVisible.value = false;
          fetchDataTypeAll();
          fetchFirstLevelDicts();
          fetchData();
        }
      } catch (error) {
        console.error('提交失败', error);
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 关闭弹窗重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除字典 [${row.dataCode}] 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDict({ids: [row.dictId]});
      if (res.code === 200) {
        ElMessage.success('删除成功');
        fetchDataTypeAll();
        fetchFirstLevelDicts();
        fetchData();
      }
    } catch (error) {
      console.error('删除失败', error);
    }
  }).catch(() => {
  });
};

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个字典数据吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDict({ids: selectedIds.value});
      if (res.code === 200) {
        ElMessage.success('批量删除成功');
        fetchDataTypeAll();
        fetchFirstLevelDicts();
        fetchData();
      }
    } catch (error) {
      console.error('批量删除失败', error);
    }
  }).catch(() => {
  });
};

// 刷新缓存
const handleRefreshCache = async () => {
  refreshLoading.value = true;
  try {
    const res = await refreshDictCache();
    if (res.code === 200) {
      ElMessage.success('缓存刷新成功');
    }
  } catch (error) {
    console.error('刷新缓存失败', error);
  } finally {
    refreshLoading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.dict-manage {
  .search-bar {
    margin-bottom: 20px;
  }

  .operation-bar {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
