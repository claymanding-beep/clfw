<template>
  <div class="clfwpage">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" class="clfwsearch-bar" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.nickname" placeholder="请输入姓名" clearable style="width: 140px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="query.phone" placeholder="请输入手机号" clearable style="width: 160px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="正常" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格卡片 -->
    <div class="clfwtable-card">
      <div class="clfwtoolbar">
        <el-button v-if="hasPerm('system:user:add')" type="primary" :icon="Plus" @click="openDialog()">新增</el-button>
        <el-button v-if="hasPerm('system:user:remove')" type="danger" :icon="Delete" :disabled="!selection.length" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe @selection-change="s => selection = s">
        <el-table-column type="selection" width="46" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="性别" width="70" align="center">
          <template #default="{ row }">{{ dictLabel(sexDict, row.sex) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
              :disabled="row.id === 1 || !hasPerm('system:user:edit')"
              @change="val => handleStatusChange(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:user:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:user:resetPwd')" link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button v-if="hasPerm('system:user:remove')" link type="danger" :disabled="row.id === 1" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="clfwpagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="620px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col v-if="!form.id" :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="默认 123456" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.sex" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in sexDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属机构">
              <el-select v-model="form.orgId" placeholder="请选择机构" clearable style="width: 100%" @change="onOrgChange">
                <el-option v-for="o in orgOptions" :key="o.id" :label="o.label" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属科室">
              <el-select v-model="form.deptId" placeholder="请选择科室" clearable style="width: 100%">
                <el-option v-for="d in deptOptionsOfOrg" :key="d.id" :label="d.deptName" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
                <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { pageUsers, getUser, addUser, updateUser, delUser, resetUserPwd, changeUserStatus } from '@/api/system/user'
import { listRoles } from '@/api/system/role'
import { orgTree } from '@/api/system/org'
import { listDept } from '@/api/system/dept'
import { useDict, dictLabel } from '@/composables/useDict'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)

const { sys_user_sex: sexDict } = useDict('sys_user_sex')

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const total = ref(0)
const selection = ref([])
const roleOptions = ref([])
const orgOptions = ref([])
const allDepts = ref([])

const query = reactive({ pageNum: 1, pageSize: 20, username: '', nickname: '', phone: '', status: null, roleId: null })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', nickname: '', password: '', phone: '', email: '', sex: 1, orgId: null, deptId: null, status: 1, roleIds: [], remark: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

// 当前所选机构下的科室
const deptOptionsOfOrg = computed(() =>
  form.orgId ? allDepts.value.filter(d => d.orgId === form.orgId) : allDepts.value
)

function flattenOrg(nodes, level = 0, acc = []) {
  nodes.forEach(n => {
    acc.push({ id: n.id, label: '　'.repeat(level) + n.orgName })
    if (n.children?.length) flattenOrg(n.children, level + 1, acc)
  })
  return acc
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageUsers(query)
    rows.value = data.rows || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  Object.assign(query, { pageNum: 1, username: '', nickname: '', phone: '', status: null, roleId: null })
  loadData()
}

function onOrgChange() {
  form.deptId = null
}

function resetForm() {
  Object.assign(form, { id: null, username: '', nickname: '', password: '', phone: '', email: '', sex: 1, orgId: null, deptId: null, status: 1, roleIds: [], remark: '' })
  formRef.value?.clearValidate()
}

async function openDialog(row) {
  resetForm()
  if (row?.id) {
    const data = await getUser(row.id)
    if (!data) {
      ElMessage.error('未获取到用户详情')
      return
    }
    Object.assign(form, { ...data, password: '', roleIds: data.roleIds || [] })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateUser(form)
      ElMessage.success('修改成功')
    } else {
      await addUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.nickname || row.username}」吗？`, '提示', { type: 'warning' })
  await delUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  const ids = selection.value.map(s => s.id)
  await ElMessageBox.confirm(`确认删除选中的 ${ids.length} 个用户吗？`, '提示', { type: 'warning' })
  await delUser(ids.join(','))
  ElMessage.success('删除成功')
  loadData()
}

async function handleResetPwd(row) {
  const { value } = await ElMessageBox.prompt(`请输入「${row.username}」的新密码`, '重置密码', {
    inputValue: '123456',
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: '密码长度 5-20 位'
  })
  await resetUserPwd(row.id, value)
  ElMessage.success('密码已重置')
}

async function handleStatusChange(row, val) {
  try {
    await changeUserStatus(row.id, val)
    ElMessage.success(val === 1 ? '已启用' : '已停用')
  } catch (e) {
    row.status = val === 1 ? 0 : 1 // 失败回滚
  }
}

onMounted(async () => {
  loadData()
  const [roles, orgs, depts] = await Promise.all([listRoles(), orgTree(), listDept()])
  roleOptions.value = roles || []
  orgOptions.value = flattenOrg(orgs || [])
  allDepts.value = depts || []
})
</script>
