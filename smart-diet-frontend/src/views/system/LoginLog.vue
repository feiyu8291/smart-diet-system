<!-- src/views/system/LoginLog.vue - 登录日志 -->
<template>
  <div class="log-container">
    <el-card class="glass-card" shadow="never">
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <Key/>
          </el-icon>
          登录日志
        </h3>
        <span class="sub-title">记录所有用户的系统登录/登出行为，保障安全审计</span>
      </div>

      <!-- 查询栏 -->
      <div class="search-wrapper">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="用户名">
            <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable/>
          </el-form-item>
          <el-form-item label="操作结果">
            <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:120px">
              <el-option label="成功" :value="0"/>
              <el-option label="失败" :value="1"/>
            </el-select>
          </el-form-item>
          <el-form-item label="登录时间">
            <el-date-picker
                v-model="queryForm.dateRange"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD HH:mm:ss"
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
        <el-table-column prop="username" label="登录账号" min-width="130"/>
        <el-table-column prop="realName" label="真实姓名" min-width="120"/>
        <el-table-column prop="loginIp" label="登录IP" min-width="140"/>
        <el-table-column label="登录结果" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="提示消息" min-width="160">
          <template #default="scope">
            <span :class="scope.row.status === 0 ? '' : 'fail-reason'">{{ scope.row.msg || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="loginTime" label="登录时间" min-width="170" align="center"/>
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
import {Key, Refresh, Search} from '@element-plus/icons-vue';
import {getLoginLogPage} from '../../api/system';

interface QueryForm {
  pageNo: number;
  pageSize: number;
  username: string;
  status: any;
  dateRange: any;
  startDate?: string;
  endDate?: string;
}

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);

const queryForm = ref<QueryForm>({
  pageNo: 1,
  pageSize: 10,
  username: '',
  status: null,
  dateRange: null
});

const fetchList = async () => {
  loading.value = true;
  try {
    const params = {...queryForm.value};
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0];
      params.endDate = params.dateRange[1];
    }
    delete params.dateRange;

    const res = await getLoginLogPage(params);
    if (res.code === 200) {
      tableData.value = res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.msg || '获取登录日志失败');
    }
  } catch (error) {
    console.error('获取登录日志出错', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryForm.value.pageNo = 1;
  fetchList();
};

const resetQuery = () => {
  queryForm.value = {pageNo: 1, pageSize: 10, username: '', status: null, dateRange: null};
  fetchList();
};

const handleSizeChange = (val: any) => {
  queryForm.value.pageSize = val;
  fetchList();
};
const handleCurrentChange = (val: any) => {
  queryForm.value.pageNo = val;
  fetchList();
};

onMounted(fetchList);
</script>

<style lang="scss" scoped>
.fail-reason {
  color: #dc2626;
  font-size: 12px;
}
</style>
