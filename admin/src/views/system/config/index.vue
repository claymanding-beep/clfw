<template>
  <div class="clfwpage">
    <el-form :inline="true" :model="query" class="clfwsearch-bar" @submit.prevent>
      <el-form-item label="参数分组">
        <el-input v-model="query.configGroup" placeholder="请输入分组" clearable style="width: 180px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="clfwtable-card">
      <div class="clfwtoolbar">
        <el-button v-if="hasPerm('system:config:add')" type="primary" :icon="Plus" @click="openDialog()">新增</el-button>
        <el-button v-if="hasPerm('system:config:remove')" type="danger" :icon="Delete" :disabled="!selection.length" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe @selection-change="s => selection = s">
        <el-table-column type="selection" width="46" :selectable="row => row.configType !== 1" />
        <el-table-column prop="configName" label="参数名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="configKey" label="参数键名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="configValue" label="参数键值" min-width="140" show-overflow-tooltip />
        <el-table-column prop="configGroup" label="分组" width="120" />
        <el-table-column label="内置" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.configType === 1 ? 'warning' : 'info'" size="small">{{ row.configType === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:config:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:config:remove')" link type="danger" :disabled="row.configType === 1" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="clfwpagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑参数' : '新增参数'" width="540px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如 sys.user.initPassword" :disabled="form.configType === 1" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="form.configValue" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="参数分组">
          <el-input v-model="form.configGroup" placeholder="如 用户管理" />
        </el-form-item>
        <el-form-item label="系统内置">
          <el-radio-group v-model="form.configType" :disabled="!!form.id">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { pageConfig, addConfig, updateConfig, delConfig } from '@/api/system/config'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const total = ref(0)
const selection = ref([])
const query = reactive({ pageNum: 1, pageSize: 20, configGroup: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, configName: '', configKey: '', configValue: '', configGroup: '', configType: 0, remark: '' })
const rules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageConfig(query)
    rows.value = data.rows || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
function handleSearch() { query.pageNum = 1; loadData() }
function handleReset() { query.configGroup = ''; query.pageNum = 1; loadData() }

function resetForm() {
  Object.assign(form, { id: null, configName: '', configKey: '', configValue: '', configGroup: '', configType: 0, remark: '' })
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
    if (form.id) { await updateConfig(form); ElMessage.success('修改成功') }
    else { await addConfig(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除参数「${row.configName}」吗？`, '提示', { type: 'warning' })
  await delConfig(row.id)
  ElMessage.success('删除成功')
  loadData()
}
async function handleBatchDelete() {
  const ids = selection.value.map(s => s.id)
  await ElMessageBox.confirm(`确认删除选中的 ${ids.length} 个参数吗？`, '提示', { type: 'warning' })
  await delConfig(ids.join(','))
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
