<template>
  <div class="clfwpage">
    <el-form :inline="true" class="clfwsearch-bar" @submit.prevent>
      <el-form-item label="所属机构">
        <el-select v-model="filterOrgId" placeholder="全部机构" clearable style="width: 220px" @change="loadData">
          <el-option v-for="o in orgOptions" :key="o.id" :label="o.label" :value="o.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <div class="clfwtable-card">
      <div class="clfwtoolbar">
        <el-button v-if="hasPerm('system:dept:add')" type="primary" :icon="Plus" @click="openDialog()">新增</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="deptName" label="科室名称" min-width="160" />
        <el-table-column prop="deptCode" label="科室编码" min-width="140" />
        <el-table-column label="所属机构" min-width="180">
          <template #default="{ row }">{{ orgName(row.orgId) }}</template>
        </el-table-column>
        <el-table-column prop="leader" label="负责人" width="110" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="orderNum" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:dept:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:dept:remove')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑科室' : '新增科室'" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属机构" prop="orgId">
          <el-select v-model="form.orgId" placeholder="请选择机构" style="width: 100%">
            <el-option v-for="o in orgOptions" :key="o.id" :label="o.label" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="科室编码">
          <el-input v-model="form.deptCode" placeholder="请输入科室编码" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="显示排序">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listDept, addDept, updateDept, delDept } from '@/api/system/dept'
import { orgTree } from '@/api/system/org'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const orgOptions = ref([])
const orgFlat = ref([])
const filterOrgId = ref(null)

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, orgId: null, deptName: '', deptCode: '', leader: '', phone: '', orderNum: 0, status: 1 })

const rules = {
  orgId: [{ required: true, message: '请选择所属机构', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
}

function flattenOrg(nodes, level = 0, acc = []) {
  nodes.forEach(n => {
    acc.push({ id: n.id, label: '　'.repeat(level) + n.orgName, orgName: n.orgName })
    if (n.children?.length) flattenOrg(n.children, level + 1, acc)
  })
  return acc
}
const orgName = (id) => orgFlat.value.find(o => o.id === id)?.orgName || '—'

async function loadData() {
  loading.value = true
  try {
    rows.value = await listDept(filterOrgId.value) || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, { id: null, orgId: filterOrgId.value || null, deptName: '', deptCode: '', leader: '', phone: '', orderNum: 0, status: 1 })
  formRef.value?.clearValidate()
}

function openDialog(row) {
  resetForm()
  if (row?.id) Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateDept(form)
      ElMessage.success('修改成功')
    } else {
      await addDept(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除科室「${row.deptName}」吗？`, '提示', { type: 'warning' })
  await delDept(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  const orgs = await orgTree() || []
  orgOptions.value = flattenOrg(orgs)
  orgFlat.value = orgOptions.value
  loadData()
})
</script>
