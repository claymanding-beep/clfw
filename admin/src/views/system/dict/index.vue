<template>
  <div class="dict-page">
    <!-- 左：字典类型 -->
    <div class="hg-table-card panel">
      <div class="panel-title">字典类型</div>
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-input v-model="typeQuery.dictName" placeholder="字典名称" clearable style="width: 150px" @keyup.enter="loadTypes(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadTypes(1)">搜索</el-button>
          <el-button v-if="hasPerm('system:dict:add')" type="success" :icon="Plus" @click="openTypeDialog()">新增</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="typeLoading" :data="typeRows" border highlight-current-row
        @current-change="onTypeSelect" height="calc(100vh - 320px)">
        <el-table-column prop="dictName" label="名称" min-width="110" show-overflow-tooltip />
        <el-table-column prop="dictType" label="类型" min-width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:dict:edit')" link type="primary" @click.stop="openTypeDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:dict:remove')" link type="danger" @click.stop="handleDeleteType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="hg-pagination">
        <el-pagination small v-model:current-page="typeQuery.pageNum" v-model:page-size="typeQuery.pageSize"
          :total="typeTotal" layout="total, prev, pager, next" @current-change="loadTypes()" />
      </div>
    </div>

    <!-- 右：字典数据 -->
    <div class="hg-table-card panel">
      <div class="panel-title">
        字典数据
        <span v-if="currentType" class="cur-type">（{{ currentType.dictName }} · {{ currentType.dictType }}）</span>
        <span v-else class="cur-type">（请先在左侧选择字典类型）</span>
      </div>
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-button v-if="hasPerm('system:dict:add')" type="success" :icon="Plus" :disabled="!currentType" @click="openDataDialog()">新增</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="dataLoading" :data="dataRows" border height="calc(100vh - 320px)">
        <el-table-column prop="dictLabel" label="标签" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.listClass || 'info'" size="small">{{ row.dictLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dictValue" label="键值" min-width="100" />
        <el-table-column prop="dictSort" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:dict:edit')" link type="primary" @click="openDataDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:dict:remove')" link type="danger" @click="handleDeleteData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="hg-pagination">
        <el-pagination small v-model:current-page="dataQuery.pageNum" v-model:page-size="dataQuery.pageSize"
          :total="dataTotal" layout="total, prev, pager, next" @current-change="loadData()" />
      </div>
    </div>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="typeDialogVisible" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="480px" @closed="resetTypeForm">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如 用户性别" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="如 sys_user_sex" :disabled="!!typeForm.id" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="dataForm.id ? '编辑字典数据' : '新增字典数据'" width="480px" @closed="resetDataForm">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典类型">
          <el-input v-model="dataForm.dictType" disabled />
        </el-form-item>
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="如 男" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如 1" />
        </el-form-item>
        <el-form-item label="显示排序">
          <el-input-number v-model="dataForm.dictSort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="回显样式">
          <el-select v-model="dataForm.listClass" placeholder="请选择" clearable style="width: 100%">
            <el-option v-for="c in listClassOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import {
  pageDictType, addDictType, updateDictType, delDictType,
  pageDictData, addDictData, updateDictData, delDictData
} from '@/api/system/dict'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)
const listClassOptions = ['primary', 'success', 'info', 'warning', 'danger']

const submitting = ref(false)

// ---------- 字典类型 ----------
const typeLoading = ref(false)
const typeRows = ref([])
const typeTotal = ref(0)
const typeQuery = reactive({ pageNum: 1, pageSize: 20, dictName: '' })
const currentType = ref(null)

async function loadTypes(resetPage) {
  if (resetPage === 1) typeQuery.pageNum = 1
  typeLoading.value = true
  try {
    const data = await pageDictType(typeQuery)
    typeRows.value = data.rows || []
    typeTotal.value = data.total || 0
  } finally {
    typeLoading.value = false
  }
}

function onTypeSelect(row) {
  currentType.value = row
  if (row) loadData(1)
}

const typeDialogVisible = ref(false)
const typeFormRef = ref()
const typeForm = reactive({ id: null, dictName: '', dictType: '', status: 1, remark: '' })
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}
function resetTypeForm() {
  Object.assign(typeForm, { id: null, dictName: '', dictType: '', status: 1, remark: '' })
  typeFormRef.value?.clearValidate()
}
function openTypeDialog(row) {
  resetTypeForm()
  if (row?.id) Object.assign(typeForm, row)
  typeDialogVisible.value = true
}
async function submitType() {
  await typeFormRef.value.validate()
  submitting.value = true
  try {
    if (typeForm.id) { await updateDictType(typeForm); ElMessage.success('修改成功') }
    else { await addDictType(typeForm); ElMessage.success('新增成功') }
    typeDialogVisible.value = false
    loadTypes()
  } finally { submitting.value = false }
}
async function handleDeleteType(row) {
  await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」吗？`, '提示', { type: 'warning' })
  await delDictType(row.id)
  ElMessage.success('删除成功')
  if (currentType.value?.id === row.id) { currentType.value = null; dataRows.value = [] }
  loadTypes()
}

// ---------- 字典数据 ----------
const dataLoading = ref(false)
const dataRows = ref([])
const dataTotal = ref(0)
const dataQuery = reactive({ pageNum: 1, pageSize: 20, dictType: '' })

async function loadData(resetPage) {
  if (!currentType.value) return
  if (resetPage === 1) dataQuery.pageNum = 1
  dataQuery.dictType = currentType.value.dictType
  dataLoading.value = true
  try {
    const data = await pageDictData(dataQuery)
    dataRows.value = data.rows || []
    dataTotal.value = data.total || 0
  } finally {
    dataLoading.value = false
  }
}

const dataDialogVisible = ref(false)
const dataFormRef = ref()
const dataForm = reactive({ id: null, dictType: '', dictLabel: '', dictValue: '', dictSort: 0, listClass: 'primary', status: 1, remark: '' })
const dataRules = {
  dictLabel: [{ required: true, message: '请输入数据标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入数据键值', trigger: 'blur' }]
}
function resetDataForm() {
  Object.assign(dataForm, { id: null, dictType: currentType.value?.dictType || '', dictLabel: '', dictValue: '', dictSort: 0, listClass: 'primary', status: 1, remark: '' })
  dataFormRef.value?.clearValidate()
}
function openDataDialog(row) {
  resetDataForm()
  if (row?.id) Object.assign(dataForm, row)
  dataDialogVisible.value = true
}
async function submitData() {
  await dataFormRef.value.validate()
  submitting.value = true
  try {
    if (dataForm.id) { await updateDictData(dataForm); ElMessage.success('修改成功') }
    else { await addDictData(dataForm); ElMessage.success('新增成功') }
    dataDialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
async function handleDeleteData(row) {
  await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」吗？`, '提示', { type: 'warning' })
  await delDictData(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadTypes())
</script>

<style scoped>
.dict-page { display: flex; gap: 12px; align-items: flex-start; }
.panel { flex: 1; min-width: 0; }
.panel-title { font-size: 14px; font-weight: 600; color: var(--color-text); margin-bottom: 12px; }
.cur-type { font-size: 12px; font-weight: 400; color: var(--color-text-secondary); }
</style>
