<template>
  <div class="clfwpage">
    <div class="clfwtable-card">
      <div class="clfwtoolbar">
        <el-button v-if="hasPerm('system:org:add')" type="primary" :icon="Plus" @click="openDialog()">新增</el-button>
        <el-button :icon="Sort" @click="toggleExpand">展开/折叠</el-button>
      </div>

      <el-table v-if="tableVisible" v-loading="loading" :data="rows" row-key="id" border
        :default-expand-all="expandAll" :tree-props="{ children: 'children' }">
        <el-table-column prop="orgName" label="机构名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="orgCode" label="机构编码" min-width="140" />
        <el-table-column label="机构类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="dictClass(orgTypeDict, row.orgType)">{{ dictLabel(orgTypeDict, row.orgType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leader" label="联系人" width="110" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="orderNum" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:org:add')" link type="primary" @click="openDialog(null, row.id)">新增</el-button>
            <el-button v-if="hasPerm('system:org:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:org:remove')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑机构' : '新增机构'" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级机构">
          <el-tree-select v-model="form.parentId" :data="parentOptions" check-strictly
            :props="{ label: 'orgName', children: 'children', value: 'id' }"
            node-key="id" placeholder="选择上级机构" style="width: 100%" />
        </el-form-item>
        <el-form-item label="机构名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="机构编码" prop="orgCode">
          <el-input v-model="form.orgCode" placeholder="请输入机构编码" />
        </el-form-item>
        <el-form-item label="机构类型">
          <el-select v-model="form.orgType" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in orgTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.leader" placeholder="请输入联系人" />
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
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Sort } from '@element-plus/icons-vue'
import { orgTree, addOrg, updateOrg, delOrg } from '@/api/system/org'
import { useDict, dictLabel, dictClass } from '@/composables/useDict'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)
const { sys_org_type: orgTypeDict } = useDict('sys_org_type')

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const expandAll = ref(true)
const tableVisible = ref(true)

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, parentId: 0, orgName: '', orgCode: '', orgType: null, leader: '', phone: '', orderNum: 0, status: 1 })

const rules = {
  orgName: [{ required: true, message: '请输入机构名称', trigger: 'blur' }],
  orgCode: [{ required: true, message: '请输入机构编码', trigger: 'blur' }]
}

const parentOptions = computed(() => [{ id: 0, orgName: '顶级机构', children: rows.value }])

async function loadData() {
  loading.value = true
  try {
    rows.value = await orgTree() || []
  } finally {
    loading.value = false
  }
}

function toggleExpand() {
  expandAll.value = !expandAll.value
  tableVisible.value = false
  nextTick(() => { tableVisible.value = true })
}

function resetForm() {
  Object.assign(form, { id: null, parentId: 0, orgName: '', orgCode: '', orgType: null, leader: '', phone: '', orderNum: 0, status: 1 })
  formRef.value?.clearValidate()
}

function openDialog(row, parentId) {
  resetForm()
  if (row?.id) Object.assign(form, row)
  else if (parentId !== undefined) form.parentId = parentId
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateOrg(form)
      ElMessage.success('修改成功')
    } else {
      await addOrg(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  if (row.children?.length) {
    ElMessage.warning('存在下级机构，请先删除下级')
    return
  }
  await ElMessageBox.confirm(`确认删除机构「${row.orgName}」吗？`, '提示', { type: 'warning' })
  await delOrg(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
