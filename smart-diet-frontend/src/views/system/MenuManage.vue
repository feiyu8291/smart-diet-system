<template>
  <div class="menu-manage">
    <el-card>
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <Menu/>
          </el-icon>
          菜单管理
        </h3>
        <span class="sub-title">系统导航菜单及权限标识配置，支持配置目录、菜单、按钮及API级别的权限</span>
      </div>

      <!-- 操作栏 -->
      <div class="operation-bar">
        <el-button type="primary" @click="handleAdd(null)">新增顶级菜单</el-button>
        <el-button @click="handleExpandAll">{{ isExpandAll ? '折叠全部' : '展开全部' }}</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
          v-if="refreshTable"
          v-loading="loading"
          :data="tableData"
          row-key="menuId"
          border
          :default-expand-all="isExpandAll"
          style="width: 100%"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="150"/>
        <el-table-column prop="menuIcon" label="图标" width="80" align="center">
          <template #default="scope">
            <el-icon v-if="scope.row.menuIcon">
              <component :is="scope.row.menuIcon"/>
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="菜单类型" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.menuType === 0" type="primary">目录</el-tag>
            <el-tag v-else-if="scope.row.menuType === 1" type="success">菜单</el-tag>
            <el-tag v-else-if="scope.row.menuType === 2" type="warning">按钮</el-tag>
            <el-tag v-else-if="scope.row.menuType === 3" type="danger">权限</el-tag>
            <el-tag v-else-if="scope.row.menuType === 4" type="info">外链</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center"/>
        <el-table-column prop="requestUrl" label="请求路径"/>
        <el-table-column prop="menuCode" label="菜单编码"/>
        <el-table-column prop="menuDescription" label="菜单描述" min-width="160"/>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleAdd(scope.row)">新增子菜单</el-button>
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        :title="dialogType === 'add' ? '新增菜单' : '编辑菜单'"
        v-model="dialogVisible"
        width="550px"
        @close="resetForm"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
      >
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
              v-model="formData.parentId"
              :data="menuTreeOptions"
              :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
              placeholder="请选择上级菜单"
              clearable
              style="width: 100%"
              check-strictly
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" placeholder="请输入菜单名称"/>
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="formData.menuType">
            <el-radio :label="0">目录</el-radio>
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
            <el-radio :label="3">权限</el-radio>
            <el-radio :label="4">外链</el-radio>
            <el-radio :label="5">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="请求路径" prop="requestUrl">
          <el-input v-model="formData.requestUrl" placeholder="请输入请求路径 (如: /sys/menu/tree)"/>
        </el-form-item>
        <el-form-item label="菜单编码" prop="menuCode">
          <el-input v-model="formData.menuCode" placeholder="请输入菜单编码 (如: system/menu/index)"/>
        </el-form-item>
        <el-form-item label="图标" prop="menuIcon">
          <el-input v-model="formData.menuIcon" placeholder="请输入图标名称 (如: User)"/>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" controls-position="right"/>
        </el-form-item>
        <el-form-item label="描述" prop="menuDescription">
          <el-input v-model="formData.menuDescription" type="textarea" placeholder="请输入菜单描述"/>
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
import {computed, nextTick, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {deleteMenu, getMenuTree, saveMenu, updateMenu} from '../../api/system';

const loading = ref(false);
const tableData = ref<any[]>([]);
const isExpandAll = ref(false);
const refreshTable = ref(true);

// 表单相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formRef = ref();
const formData = reactive({
  menuId: undefined as number | undefined,
  parentId: 0 as number | null,
  menuName: '',
  menuCode: '',
  requestUrl: '',
  menuType: 1 as number,
  menuIcon: '',
  sortOrder: 0,
  menuDescription: ''
});

const formRules = {
  menuName: [{required: true, message: '请输入菜单名称', trigger: 'blur'}],
  requestUrl: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        if ((formData.menuType === 1 || formData.menuType === 4) && !value) {
          callback(new Error('请输入请求路径'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  menuType: [{required: true, message: '请选择菜单类型', trigger: 'change'}]
};

// 树形选择器的选项，加上一个“顶级菜单”的虚拟节点
const menuTreeOptions = computed(() => {
  return [
    {menuId: 0, menuName: '顶级菜单', children: tableData.value}
  ];
});

onMounted(() => {
  fetchData();
});

// 获取表格数据
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getMenuTree();
    if (res.code === 200) {
      tableData.value = res.data;
    }
  } catch (error) {
    console.error('获取数据失败', error);
  } finally {
    loading.value = false;
  }
};

// 展开/折叠全部
const handleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value;
  refreshTable.value = false;
  nextTick(() => {
    refreshTable.value = true;
  });
};

// 新增
const handleAdd = (row: any | null) => {
  dialogType.value = 'add';
  Object.assign(formData, {
    menuId: undefined,
    parentId: row ? row.menuId : 0,
    menuName: '',
    menuCode: '',
    requestUrl: '',
    menuType: 1,
    menuIcon: '',
    sortOrder: 0,
    menuDescription: ''
  });
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogType.value = 'edit';
  Object.assign(formData, {
    menuId: row.menuId,
    parentId: row.parentId || 0,
    menuName: row.menuName,
    menuCode: row.menuCode || '',
    requestUrl: row.requestUrl || '',
    menuType: row.menuType || 1,
    menuIcon: row.menuIcon || '',
    sortOrder: row.sortOrder || 0,
    menuDescription: row.menuDescription || ''
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
        const payload = {...formData};
        if (payload.parentId === 0) {
          payload.parentId = null; // 修正后端可能不接受0作为parentId的问题
        }

        const api = dialogType.value === 'add' ? saveMenu : updateMenu;
        const res = await api(payload);
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
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该菜单包含子菜单，无法直接删除');
    return;
  }

  ElMessageBox.confirm(`确认删除菜单 ${row.menuName} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMenu({ids: [row.menuId]});
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
</script>

<style lang="scss" scoped>
.menu-manage {
  .operation-bar {
    margin-bottom: 20px;
  }
}
</style>
