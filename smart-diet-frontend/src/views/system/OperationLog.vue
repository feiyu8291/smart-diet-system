<!-- src/views/system/OperationLog.vue - 操作日志 -->
<template>
  <div class="log-container">
    <el-card class="glass-card" shadow="never">
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <Memo/>
          </el-icon>
          操作日志
        </h3>
        <span class="sub-title">核心业务操作轨迹审计，涵盖线索流转、成交审核等关键动作</span>
      </div>

      <!-- 查询栏 -->
      <div class="search-wrapper">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="操作人">
            <el-input v-model="queryForm.realName" placeholder="请输入操作人姓名" clearable/>
          </el-form-item>
          <el-form-item label="操作模块">
            <el-input v-model="queryForm.opModule" placeholder="如：成交审核" clearable/>
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select v-model="queryForm.opType" placeholder="全部" clearable style="width:130px">
              <el-option
                  v-for="item in opTypeOptions"
                  :key="item.dataCode"
                  :label="item.dataValue"
                  :value="item.dataCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="操作时间">
            <el-date-picker
                v-model="queryForm.dateRange"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width:240px"
            />
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
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData" class="custom-table" style="width:100%">
        <el-table-column type="index" label="序号" width="70" align="center"/>
        <el-table-column prop="realName" label="操作人" min-width="110"/>
        <el-table-column prop="opModule" label="操作模块" min-width="120" align="center">
          <template #default="scope">
            <el-tag effect="plain">{{ scope.row.opModule || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="getOpTypeTagType(scope.row.opType)" effect="light">
              {{ getOpTypeLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作详情" min-width="260">
          <template #default="scope">
            <span class="detail-text">{{ scope.row.content || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" min-width="170" align="center"/>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryForm.pageNo"
            v-model:page-size="queryForm.pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Memo, Refresh, Search} from '@element-plus/icons-vue';
import {getOperationLogPage, getDictMore} from '../../api/system';

interface QueryForm {
  pageNo: number;
  pageSize: number;
  realName: string;
  opModule: string;
  opType: any;
  dateRange: any;
  startDate?: string;
  endDate?: string;
}

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const opTypeOptions = ref<any[]>([]);

const queryForm = ref<QueryForm>({
  pageNo: 1,
  pageSize: 10,
  realName: '',
  opModule: '',
  opType: null,
  dateRange: null
});

// 获取操作类型字典
const fetchDictOptions = async () => {
  try {
    const res = await getDictMore('sys_operation_type');
    if (res.code === 200) {
      opTypeOptions.value = res.data.sys_operation_type || [];
    }
  } catch (e) {
    console.error('获取操作类型字典失败', e);
  }
};

const fetchList = async () => {
  loading.value = true;
  try {
    const params = {...queryForm.value};
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0];
      params.endDate = params.dateRange[1];
    }
    delete params.dateRange;

    const res = await getOperationLogPage(params);
    if (res.code === 200) {
      tableData.value = res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.msg || '获取操作日志失败');
    }
  } catch (error) {
    console.error('获取操作日志出错', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryForm.value.pageNo = 1;
  fetchList();
};

const resetQuery = () => {
  queryForm.value = {pageNo: 1, pageSize: 10, realName: '', opModule: '', opType: null, dateRange: null};
  fetchList();
};

// 获取操作类型标签
const getOpTypeLabel = (row: any) => {
  if (row.opTypeStr) return row.opTypeStr;
  const found = opTypeOptions.value.find(o => o.dataCode === String(row.opType));
  return found ? found.dataValue : row.opType || '-';
};

// 操作类型标签颜色
const getOpTypeTagType = (code: any) => {
  const typeMap: Record<string, string> = {
    '7': 'warning',   // 提交审核
    '8': 'success',   // 审批通过
    '9': 'danger',   // 审批驳回
    '10': 'info',     // 保存规则配置
    '11': 'primary',  // 保存配置
  };
  return typeMap[String(code)] || '';
};

const handleSizeChange = (val: any) => {
  queryForm.value.pageSize = val;
  fetchList();
};
const handleCurrentChange = (val: any) => {
  queryForm.value.pageNo = val;
  fetchList();
};

onMounted(() => {
  fetchDictOptions();
  fetchList();
});
</script>


