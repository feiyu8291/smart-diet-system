<template>
  <div class="user-manage">
    <el-card>
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <User/>
          </el-icon>
          用户管理
        </h3>
        <span class="sub-title">系统用户及组织架构管理，负责分配登录账号、关联所属角色</span>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchParams">
          <el-form-item label="用户名">
            <el-input v-model="searchParams.username" placeholder="请输入用户名" clearable/>
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="searchParams.realName" placeholder="请输入姓名" clearable/>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="searchParams.phoneNum" placeholder="请输入手机号" clearable/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchParams.useStatus" placeholder="请选择状态" clearable>
              <el-option label="启用" :value="0"/>
              <el-option label="禁用" :value="1"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="operation-bar">
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
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
        <el-table-column prop="username" label="用户名"/>
        <el-table-column prop="realName" label="姓名"/>
        <el-table-column prop="phoneNum" label="手机号"/>
        <el-table-column prop="idCardNum" label="身份证号"/>
        <el-table-column prop="employeeNum" label="工号"/>
        <el-table-column prop="userSexStr" label="性别" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.userSex === 2 ? 'danger' : ''">
              {{ scope.row.userSexStr || (scope.row.userSex === 2 ? '女' : '男') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮件地址" show-overflow-tooltip/>
        <el-table-column label="角色">
          <template #default="scope">
            <el-tag
                v-for="role in scope.row.roleNames || []"
                :key="role"
                size="small"
                class="role-tag"
            >
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
                v-model="scope.row.useStatus"
                :active-value="0"
                :inactive-value="1"
                @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"/>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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
        :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
        v-model="dialogVisible"
        width="500px"
        @close="resetForm"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="dialogType === 'edit'"/>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名"/>
        </el-form-item>
        <el-form-item label="工号" prop="employeeNum">
          <el-input v-model="formData.employeeNum" placeholder="请输入工号"/>
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNum">
          <el-input v-model="formData.phoneNum" placeholder="请输入手机号"/>
        </el-form-item>
        <el-form-item label="性别" prop="userSex">
          <el-radio-group v-model="formData.userSex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮件地址" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮件地址"/>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCardNum">
          <el-input v-model="formData.idCardNum" placeholder="请输入身份证号"/>
        </el-form-item>
        <el-form-item label="密码" prop="userPassword" v-if="dialogType === 'add'">
          <el-input v-model="formData.userPassword" type="password" placeholder="请输入密码" show-password/>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="formData.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
                v-for="role in roleList"
                :key="role.roleId"
                :label="role.roleName"
                :value="role.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="useStatus">
          <el-radio-group v-model="formData.useStatus">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
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
  deleteUser,
  getAllRoles,
  getUserPage,
  resetUserPassword,
  saveUser,
  updateUser,
  updateUserStatus,
  getPublicKey
} from '../../api/system';
import JSEncrypt from 'jsencrypt';

// 查询参数
const searchParams = reactive({
  username: '',
  realName: '',
  phoneNum: '',
  useStatus: undefined
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
const roleList = ref<any[]>([]);

// 表单相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formRef = ref();
const formData = reactive({
  userId: undefined as number | undefined,
  username: '',
  realName: '',
  phoneNum: '',
  employeeNum: '',
  email: '',
  userSex: 1,
  idCardNum: '',
  userPassword: '',
  roleIds: [] as number[],
  useStatus: 0
});

const formRules = {
  username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  realName: [{required: true, message: '请输入姓名', trigger: 'blur'}],
  employeeNum: [{required: true, message: '请输入工号', trigger: 'blur'}],
  phoneNum: [{required: true, message: '请输入手机号', trigger: 'blur'}],
  idCardNum: [{required: true, message: '请输入身份证号', trigger: 'blur'}],
  roleIds: [{type: 'array', required: true, message: '请选择角色', trigger: 'change'}]
};

onMounted(() => {
  fetchRoleList();
  fetchData();
});

// 获取角色列表
const fetchRoleList = async () => {
  try {
    const res = await getAllRoles();
    if (res.code === 200) {
      roleList.value = res.data;
    }
  } catch (error) {
    console.error('获取角色列表失败', error);
  }
};

// 获取表格数据
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getUserPage({
      ...pageParams,
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
  searchParams.username = '';
  searchParams.realName = '';
  searchParams.phoneNum = '';
  searchParams.useStatus = undefined;
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
  selectedIds.value = val.map(item => item.userId);
};

// 状态变更
const handleStatusChange = async (row: any) => {
  try {
    const res = await updateUserStatus(row.userId, row.useStatus);
    if (res.code === 200) {
      ElMessage.success('状态更新成功');
    } else {
      row.useStatus = row.useStatus === 0 ? 1 : 0;
    }
  } catch (error) {
    row.useStatus = row.useStatus === 0 ? 1 : 0;
  }
};

// 新增
const handleAdd = () => {
  dialogType.value = 'add';
  Object.assign(formData, {
    userId: undefined,
    username: '',
    realName: '',
    phoneNum: '',
    employeeNum: '',
    email: '',
    userSex: 1,
    idCardNum: '',
    userPassword: '',
    roleIds: [],
    useStatus: 0
  });
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogType.value = 'edit';
  Object.assign(formData, {
    userId: row.userId,
    username: row.username || '',
    realName: row.realName,
    phoneNum: row.phoneNum,
    employeeNum: row.employeeNum,
    email: row.email,
    userSex: row.userSex || 1,
    idCardNum: row.idCardNum || '',
    roleIds: row.roleIds || [],
    useStatus: row.useStatus
  });
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const submitData = {...formData};
        if (dialogType.value === 'add') {
          // 1. 获取后端动态公钥
          const res = await getPublicKey();
          if (!res) {
            throw new Error('无法获取安全密钥，请联系系统管理员！');
          }
          const publicKey = res && typeof res === 'object' && 'data' in res ? res.data : res;
          if (!publicKey) {
            throw new Error('获取的安全密钥格式不正确！');
          }
          // 2. 使用 JSEncrypt 进行密码 RSA 加密
          const encryptor = new JSEncrypt();
          encryptor.setPublicKey(publicKey);
          const encryptedPassword = encryptor.encrypt(formData.userPassword);
          if (!encryptedPassword) {
            throw new Error('密码加密失败！');
          }
          submitData.userPassword = encryptedPassword;
        }

        const api = dialogType.value === 'add' ? saveUser : updateUser;
        const res = await api(submitData);
        if (res.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功');
          dialogVisible.value = false;
          fetchData();
        }
      } catch (error: any) {
        console.error('提交失败', error);
        ElMessage.error(error.message || '提交失败，请重试');
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
  ElMessageBox.confirm(`确认删除用户 ${row.username} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteUser({ids: [row.userId]});
      if (res.code === 200) {
        ElMessage.success('删除成功');
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
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个用户吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteUser({ids: selectedIds.value});
      if (res.code === 200) {
        ElMessage.success('批量删除成功');
        fetchData();
      }
    } catch (error) {
      console.error('批量删除失败', error);
    }
  }).catch(() => {
  });
};

// 重置密码
const handleResetPassword = (row: any) => {
  ElMessageBox.confirm(`确认重置用户 ${row.username} 的密码吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await resetUserPassword(row.userId);
      if (res.code === 200) {
        ElMessage.success('密码重置成功');
      }
    } catch (error) {
      console.error('重置密码失败', error);
    }
  }).catch(() => {
  });
};
</script>

<style lang="scss" scoped>
.user-manage {
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

  .role-tag {
    margin-right: 4px;
    margin-bottom: 4px;
  }
}
</style>
