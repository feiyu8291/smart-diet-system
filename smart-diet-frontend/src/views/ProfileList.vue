<template>
  <div class="content-container section-gap">
    <el-card class="profile-list-card">
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <Notebook/>
          </el-icon>
          就餐成员健康档案
        </h3>
        <span class="sub-title">系统就餐成员身体档案维护，可为不同成员测算 BMI、BMR 代谢率及制定科学减重膳食目标</span>
      </div>

      <!-- 搜索与操作栏（合并为单行展示） -->
      <div class="search-bar" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap;">
        <el-form :inline="true" @submit.prevent style="margin-bottom: 0;">
          <el-form-item label="成员姓名">
            <el-input
                v-model="searchName"
                placeholder="搜索成员姓名..."
                clearable
                @keyup.enter="handleSearch"
                style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="家庭组">
            <el-select v-model="filterGroupId" placeholder="全部家庭组" style="width: 160px" clearable @change="handleSearch">
              <el-option
                  v-for="item in allGroups"
                  :key="item.groupId"
                  :label="item.groupName"
                  :value="item.groupId"
              />
            </el-select>
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
            新增成员档案
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
          v-loading="loading"
          :data="profiles"
          border
          max-height="calc(100vh - 240px)"
          style="width: 100%; margin-top: 10px"
      >
        <el-table-column prop="profileId" label="ID" width="80" align="center"/>
        <el-table-column prop="memberName" label="姓名" min-width="120">
          <template #default="scope">
            <strong class="member-title-text">{{ scope.row.memberName }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="memberRelation" label="家庭关系" width="95" align="center"/>
        <el-table-column label="性别" width="80" align="center">
          <template #default="scope">
            <span>{{ scope.row.memberGender === 1 ? '男' : '女' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberAge" label="年龄" width="80" align="center">
          <template #default="scope">
            <span>{{ scope.row.memberAge || 0 }} 岁</span>
          </template>
        </el-table-column>
        <el-table-column label="身高" width="100" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.memberHeight }}</span> <span class="unit">cm</span>
          </template>
        </el-table-column>
        <el-table-column label="体重" width="100" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.memberWeight }}</span> <span class="unit">kg</span>
          </template>
        </el-table-column>
        <el-table-column label="目标体重" width="100" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.targetWeight }}</span> <span class="unit">kg</span>
          </template>
        </el-table-column>
        <el-table-column label="BMI 指数" width="130" align="center">
          <template #default="scope">
            <span :class="['bmi-tag', getBmiClass(scope.row)]">
              {{ getBmiText(scope.row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="每日推荐摄入" width="145" align="right">
          <template #default="scope">
            <strong class="font-mono" style="color: var(--primary)">{{ scope.row.dailyTargetCalories || 0 }}</strong> <span class="unit">kcal</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewReport(scope.row.profileId)">健康报告</el-button>
            <el-button type="primary" link @click="openEditModal(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.profileId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
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
        :title="form.profileId ? '修改健康参数测评' : '录入成员健康测评'"
        width="580px"
        custom-class="premium-dialog"
    >
      <div class="member-form">
        <!-- 成员称呼 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">姓名/称呼</label>
            <input type="text" v-model="form.memberName" class="input-text" placeholder="例如: 爸爸、张三"/>
          </div>
          <div class="form-item">
            <label class="caption">归属家庭组</label>
            <el-select v-model="form.groupId" placeholder="选择家庭组" style="width: 100%">
              <el-option
                  v-for="g in allGroups"
                  :key="g.groupId"
                  :label="g.groupName"
                  :value="g.groupId"
              />
            </el-select>
          </div>
        </div>

        <!-- 身份证号码 (自动提取生日与性别) -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">与做饭人关系</label>
            <el-select v-model="form.memberRelation" placeholder="选择亲属关系" style="width: 100%">
              <el-option value="本人" label="本人"/>
              <el-option value="配偶" label="配偶"/>
              <el-option value="子女" label="子女"/>
              <el-option value="父母" label="父母"/>
              <el-option value="亲友" label="亲友"/>
            </el-select>
          </div>
          <div class="form-item">
            <label class="caption">身份证号码 (可选)</label>
            <input
                type="text"
                v-model="form.idCardNum"
                class="input-text"
                placeholder="输入以自动提取生日和性别"
                maxlength="18"
                @input="handleIdCardInput"
            />
          </div>
        </div>

        <!-- 绑定状态 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">账号绑定状态</label>
            <el-select v-model="bindStatus" placeholder="选择绑定状态" style="width: 100%">
              <el-option :value="0" label="离线成员（仅做饭人维护指标）"/>
              <el-option :value="1" label="在线用户（关联系统账号）"/>
            </el-select>
          </div>
          <div class="form-item" v-if="bindStatus === 1">
            <label class="caption">关联系统账户</label>
            <el-select v-model="form.userId" filterable placeholder="选择系统账号" style="width: 100%" clearable>
              <el-option
                  v-for="user in allSysUsers"
                  :key="user.userId"
                  :label="`${user.realName} (${user.username})`"
                  :value="user.userId"
              />
            </el-select>
          </div>
        </div>

        <!-- 性别、出生日期 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">成员性别</label>
            <el-select v-model="form.memberGender" placeholder="选择性别" style="width: 100%">
              <el-option :value="1" label="男"/>
              <el-option :value="2" label="女"/>
            </el-select>
          </div>
          <div class="form-item">
            <label class="caption">出生日期</label>
            <el-date-picker
                v-model="form.memberBirthday"
                type="date"
                placeholder="选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
            />
          </div>
        </div>

        <!-- 身高、体重 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">身高 (cm)</label>
            <input type="number" v-model="form.memberHeight" class="input-text" step="0.1" placeholder="身高"/>
          </div>
          <div class="form-item">
            <label class="caption">体重 (kg)</label>
            <input type="number" v-model="form.memberWeight" class="input-text" step="0.1" placeholder="当前体重"/>
          </div>
        </div>

        <!-- 活动强度 & 减重参数 -->
        <div class="form-row-2">
          <div class="form-item">
            <label class="caption">日常活动强度</label>
            <el-select v-model="form.activityLevel" placeholder="选择日常活动量" style="width: 100%">
              <el-option :value="1" label="久坐（办公室工作、少运动）"/>
              <el-option :value="2" label="轻度（日常零星散步或轻度活动）"/>
              <el-option :value="3" label="中度（每周规律运动3-5次）"/>
              <el-option :value="4" label="重度（高强度体力劳动或重度健身）"/>
            </el-select>
          </div>
          <div class="form-item">
            <label class="caption">目标体重 (kg)</label>
            <input type="number" v-model="form.targetWeight" class="input-text" step="0.1" placeholder="目标体重"/>
          </div>
        </div>

        <div class="form-item">
          <label class="caption">周科学减重速度 (kg/周)</label>
          <el-select v-model="form.dietSpeed" placeholder="选择建议减脂赤字" style="width: 100%">
            <el-option :value="0.00" label="0.00 kg/周 (维持当前体重 / 纯保养食谱)"/>
            <el-option :value="0.25" label="0.25 kg/周 (温和减脂 - 每日赤字 275 kcal)"/>
            <el-option :value="0.50" label="0.50 kg/周 (推荐速度 - 每日赤字 550 kcal)"/>
            <el-option :value="0.75" label="0.75 kg/周 (强效减脂 - 每日赤字 825 kcal)"/>
            <el-option :value="1.00" label="1.00 kg/周 (极限挑战 - 每日赤字 1100 kcal)"/>
          </el-select>
        </div>
      </div>

      <template #footer>
        <button class="btn-secondary" style="margin-right: 10px" @click="modalVisible = false">取消</button>
        <button class="btn-primary" @click="handleSave">保存并自动测评</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Notebook, Plus} from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'
import request from '../utils/request'

const router = useRouter()

const loading = ref(false)
const profiles = ref<any[]>([])
const searchName = ref('')
const filterGroupId = ref<number | null>(null)
const modalVisible = ref(false)
const bindStatus = ref(0)

const allGroups = ref<any[]>([])
const allSysUsers = ref<any[]>([])

// 分页变量
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

const defaultForm = {
  profileId: null as number | null,
  userId: null as number | null,
  groupId: null as number | null,
  groupRole: 2,
  memberName: '',
  idCardNum: '',
  memberRelation: '配偶',
  memberGender: 1,
  memberHeight: 170.0,
  memberWeight: 65.0,
  memberBirthday: '1995-01-01',
  activityLevel: 2,
  targetWeight: 60.0,
  dietSpeed: 0.50
}
const form = ref({...defaultForm})

// 计算 BMI 相关指标
const getBmiText = (row: any) => {
  if (!row.memberWeight || !row.memberHeight) return '0.0'
  const heightM = row.memberHeight / 100.0
  const bmi = row.memberWeight / (heightM * heightM)
  const val = Math.round(bmi * 10) / 10
  if (val < 18.5) return `${val} (偏瘦)`
  if (val >= 18.5 && val < 24.0) return `${val} (正常)`
  if (val >= 24.0 && val < 28.0) return `${val} (超重)`
  return `${val} (肥胖)`
}

const getBmiClass = (row: any) => {
  if (!row.memberWeight || !row.memberHeight) return ''
  const heightM = row.memberHeight / 100.0
  const bmi = row.memberWeight / (heightM * heightM)
  const val = Math.round(bmi * 10) / 10
  if (val < 18.5) return 'bmi-under'
  if (val >= 18.5 && val < 24.0) return 'bmi-normal'
  if (val >= 24.0 && val < 28.0) return 'bmi-over'
  return 'bmi-obese'
}

// 身份证号码提取生日与性别
const handleIdCardInput = (e: any) => {
  const val = e.target.value
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
  if (val.length === 18 && reg.test(val)) {
    const year = val.substring(6, 10)
    const month = val.substring(10, 12)
    const day = val.substring(12, 14)
    form.value.memberBirthday = `${year}-${month}-${day}`

    const genderDigit = Number(val.substring(16, 17))
    form.value.memberGender = (genderDigit % 2 === 1) ? 1 : 2
    ElMessage.success('已根据身份证号码自动识别提取生日与性别！')
  }
}

// 加载健康档案分页列表
const loadProfiles = async () => {
  loading.value = true
  try {
    let url = `/api/profile/page?pageNo=${currentPage.value}&pageSize=${pageSize.value}`
    if (searchName.value.trim()) {
      url += `&name=${encodeURIComponent(searchName.value.trim())}`
    }
    if (filterGroupId.value !== null) {
      url += `&groupId=${filterGroupId.value}`
    }
    const res: any = await request.get(url)
    if (res && res.code === 200) {
      profiles.value = res.data || []
      totalCount.value = res.page ? res.page.total : profiles.value.length
    }
  } catch (e) {
    console.error('获取健康档案失败', e)
  } finally {
    loading.value = false
  }
}

// 字典与列表参数加载
const loadSysUsers = async () => {
  try {
    const res: any = await request.get('/sys/user/listAll')
    allSysUsers.value = res.data || res || []
  } catch (err) {
    console.error(err)
  }
}

const loadGroups = async () => {
  try {
    allGroups.value = await request.get('/api/group/list')
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadProfiles()
}

const resetSearch = () => {
  searchName.value = ''
  filterGroupId.value = null
  currentPage.value = 1
  loadProfiles()
}

// 跳转详情大盘
const viewReport = (profileId: number) => {
  router.push({path: '/profile/personal-detail', query: {profileId: profileId}})
}

// 新建弹窗
const openCreateModal = () => {
  form.value = {
    ...defaultForm,
    groupId: allGroups.value.length > 0 ? allGroups.value[0].groupId : null
  }
  bindStatus.value = 0
  modalVisible.value = true
}

// 编辑弹窗
const openEditModal = (row: any) => {
  form.value = {...row}
  bindStatus.value = row.userId ? 1 : 0
  modalVisible.value = true
}

// 保存逻辑
const handleSave = async () => {
  if (!form.value.memberName) {
    ElMessage.warning('请输入姓名或称呼！')
    return
  }
  if (!form.value.groupId) {
    ElMessage.warning('请选择所属家庭组！')
    return
  }
  if (!form.value.memberBirthday) {
    ElMessage.warning('出生日期不能为空！')
    return
  }
  if (bindStatus.value === 0) {
    form.value.userId = null
  }

  try {
    const res = await request.post('/api/profile/save', form.value)
    if (res) {
      ElMessage.success('保存成功，健康测评已自动更新！')
      modalVisible.value = false
      loadProfiles()
    }
  } catch (e) {
    console.error('保存失败', e)
  }
}

// 删除逻辑
const handleDelete = (profileId: number) => {
  ElMessageBox.confirm(
      '确认将该就餐人从系统健康档案中删除？其健康历史记录会被软删除。',
      '警告',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/profile/delete/${profileId}`)
      if (res) {
        ElMessage.success('删除健康档案成功！')
        loadProfiles()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {
  })
}

// 分页控制
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadProfiles()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadProfiles()
}

onMounted(() => {
  loadGroups()
  loadSysUsers()
  loadProfiles()
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

.member-title-text {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
}

.unit {
  font-size: 11px;
  color: var(--ink-subtle);
}

.bmi-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: var(--rounded-xs);
  font-size: 11px;
  font-weight: 600;
}

/* BMI 体质标签颜色 */
.bmi-under {
  background-color: #e0f2fe;
  color: #0369a1;
}

.bmi-normal {
  background-color: #dcfce7;
  color: #15803d;
}

.bmi-over {
  background-color: #fef3c7;
  color: #b45309;
}

.bmi-obese {
  background-color: #fee2e2;
  color: #b91c1c;
}

.member-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
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
</style>
