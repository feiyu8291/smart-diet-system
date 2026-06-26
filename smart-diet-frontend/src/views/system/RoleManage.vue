<template>
  <div class="role-manage">
    <el-card>
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <UserFilled/>
          </el-icon>
          角色管理
        </h3>
        <span class="sub-title">管理系统中的角色权限体系，支持配置关联的功能菜单与资源权限</span>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchParams">
          <el-form-item label="角色名称">
            <el-input v-model="searchParams.roleName" placeholder="请输入角色名称" clearable/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="operation-bar">
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
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
        <el-table-column prop="roleName" label="角色名称" width="160"/>
        <el-table-column prop="roleDescription" label="描述" min-width="160"/>
        <el-table-column prop="createTime" label="创建时间" width="180"/>
        <el-table-column prop="updateTime" label="修改时间" width="180"/>
        <el-table-column prop="createBy" label="创建人" width="120"/>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" link @click="handleAssignMenu(scope.row)">分配菜单</el-button>
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
        :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称"/>
        </el-form-item>
        <el-form-item label="描述" prop="roleDescription">
          <el-input v-model="formData.roleDescription" type="textarea" placeholder="请输入角色描述"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog
        title="分配菜单"
        v-model="menuDialogVisible"
        width="500px"
        @close="resetMenuForm"
    >
      <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          show-checkbox
          node-key="menuId"
          :props="{ label: 'menuName', children: 'children' }"
          :default-checked-keys="checkedMenuIds"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="menuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleMenuSubmit" :loading="menuSubmitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {nextTick, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {configRoleMenus, deleteRole, getMenuTree, getRoleMenuIds, getRolePage, saveRole, updateRole} from '../../api/system';

// 查询参数
const searchParams = reactive({
  roleName: ''
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

// 表单相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formRef = ref();
const formData = reactive({
  roleId: undefined as number | undefined,
  roleName: '',
  roleDescription: ''
});

const formRules = {
  roleName: [{required: true, message: '请输入角色名称', trigger: 'blur'}]
};

// 菜单分配相关
const menuDialogVisible = ref(false);
const menuSubmitLoading = ref(false);
const menuTreeData = ref<any[]>([]);
const checkedMenuIds = ref<number[]>([]);
const currentRoleId = ref<number | undefined>();
const menuTreeRef = ref();

onMounted(() => {
  fetchData();
  fetchMenuTree();
});

// 获取表格数据
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getRolePage({
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

// 获取菜单树
const fetchMenuTree = async () => {
  try {
    const res = await getMenuTree();
    if (res.code === 200) {
      menuTreeData.value = res.data;
    }
  } catch (error) {
    console.error('获取菜单树失败', error);
  }
};

// 搜索
const handleSearch = () => {
  pageParams.current = 1;
  fetchData();
};

// 重置搜索
const resetSearch = () => {
  searchParams.roleName = '';
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
  selectedIds.value = val.map(item => item.roleId);
};

// 新增
const handleAdd = () => {
  dialogType.value = 'add';
  Object.assign(formData, {
    roleId: undefined,
    roleName: '',
    roleDescription: ''
  });
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogType.value = 'edit';
  Object.assign(formData, {
    roleId: row.roleId,
    roleName: row.roleName,
    roleDescription: row.roleDescription
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
        const api = dialogType.value === 'add' ? saveRole : updateRole;
        const res = await api(formData);
        if (res.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功');
          dialogVisible.value = false;
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
  ElMessageBox.confirm(`确认删除角色 ${row.roleName} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteRole({ids: [row.roleId]});
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
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个角色吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteRole({ids: selectedIds.value});
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

// 过滤保留叶子节点的 menuId
const getLeafKeys = (tree: any[], checkedKeys: number[]): number[] => {
  const leafKeys: number[] = [];
  const traverse = (nodes: any[]) => {
    for (const node of nodes) {
      if (!node.children || node.children.length === 0) {
        if (checkedKeys.includes(node.menuId)) {
          leafKeys.push(node.menuId);
        }
      } else {
        traverse(node.children);
      }
    }
  };
  traverse(tree);
  return leafKeys;
};

// 分配菜单
const handleAssignMenu = async (row: any) => {
  currentRoleId.value = row.roleId;
  try {
    const res = await getRoleMenuIds(row.roleId);
    if (res.code === 200) {
      // 获取角色已有的菜单ID列表
      const rawCheckedKeys = res.data || [];
      const leafKeys = getLeafKeys(menuTreeData.value, rawCheckedKeys);
      checkedMenuIds.value = leafKeys;
      menuDialogVisible.value = true;

      // 等待DOM更新后设置选中节点
      nextTick(() => {
        if (menuTreeRef.value) {
          menuTreeRef.value.setCheckedKeys(checkedMenuIds.value);
        }
      });
    }
  } catch (error) {
    console.error('获取角色菜单失败', error);
  }
};

const handleMenuSubmit = async () => {
  if (!currentRoleId.value || !menuTreeRef.value) return;

  menuSubmitLoading.value = true;
  try {
    // 获取选中的节点以及半选中的节点
    const checkedKeys = menuTreeRef.value.getCheckedKeys();
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys();
    const allSelectedIds = [...checkedKeys, ...halfCheckedKeys];

    const res = await configRoleMenus({
      roleId: currentRoleId.value,
      menuIds: allSelectedIds
    });

    if (res.code === 200) {
      ElMessage.success('分配菜单成功');
      menuDialogVisible.value = false;
    }
  } catch (error) {
    console.error('分配菜单失败', error);
  } finally {
    menuSubmitLoading.value = false;
  }
};

const resetMenuForm = () => {
  checkedMenuIds.value = [];
  currentRoleId.value = undefined;
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys([]);
  }
};
</script>

<style lang="scss" scoped>
.role-manage {
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
