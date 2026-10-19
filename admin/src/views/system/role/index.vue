<template>
  <div class="clfwpage">
    <div class="clfwtable-card">
      <div class="clfwtoolbar">
        <el-button v-if="hasPerm('system:role:add')" type="primary" :icon="Plus" @click="openDialog()">新增</el-button>
        <el-button v-if="hasPerm('system:role:remove')" type="danger" :icon="Delete" :disabled="!selection.length" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe @selection-change="s => selection = s">
        <el-table-column type="selection" width="46" />
        <el-table-column prop="roleName" label="角色名称" min-width="130" />
        <el-table-column prop="roleKey" label="角色标识" min-width="140" />
        <el-table-column prop="roleSort" label="排序" width="70" align="center" />
        <el-table-column label="数据范围" width="130" align="center">
          <template #default="{ row }">{{ dataScopeText(row.dataScope) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:role:query')" link type="primary" @click="openUsersDialog(row)">关联用户</el-button>
            <el-button v-if="hasPerm('system:role:edit')" link type="primary" :disabled="row.id === 1" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:role:remove')" link type="danger" :disabled="row.id === 1" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="clfwpagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="640px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="如 doctor，权限字符串" />
        </el-form-item>
        <el-form-item label="显示排序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope" placeholder="请选择" style="width: 220px">
            <el-option v-for="d in dataScopeOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="tree-box">
            <el-tree ref="treeRef" :data="menuTreeData" :props="{ label: 'menuName', children: 'children' }"
              node-key="id" show-checkbox accordion :default-checked-keys="checkedKeys" class="perm-tree" />
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 关联用户 -->
    <el-dialog v-model="usersDialogVisible" :title="`关联用户・${currentRole.roleName || ''}`" width="720px">
      <el-table v-loading="usersLoading" :data="userRows" border stripe max-height="420">
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="nickname" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <template #empty>该角色暂无关联用户</template>
      </el-table>
      <div class="clfwpagination">
        <el-pagination v-model:current-page="userQuery.pageNum" v-model:page-size="userQuery.pageSize"
          :total="userTotal" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" @size-change="loadRoleUsers" @current-change="loadRoleUsers" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { pageRoles, getRole, addRole, updateRole, delRole, roleUsers } from '@/api/system/role'
import { menuTree } from '@/api/system/menu'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)

const dataScopeOptions = [
  { value: 1, label: '全部数据' },
  { value: 2, label: '本机构及以下' },
  { value: 3, label: '本机构' },
  { value: 4, label: '本科室' },
  { value: 5, label: '仅本人' },
  { value: 6, label: '自定义' }
]
const dataScopeText = (v) => dataScopeOptions.find(d => d.value === v)?.label || '—'

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const total = ref(0)
const selection = ref([])
const menuTreeData = ref([])
const treeRef = ref()
const checkedKeys = ref([])

const query = reactive({ pageNum: 1, pageSize: 20 })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, roleName: '', roleKey: '', roleSort: 0, dataScope: 5, status: 1, remark: '' })

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

// 收集树中所有叶子节点 id
function collectLeafIds(nodes, acc = new Set()) {
  nodes.forEach(n => {
    if (n.children?.length) collectLeafIds(n.children, acc)
    else acc.add(n.id)
  })
  return acc
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageRoles(query)
    rows.value = data.rows || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, { id: null, roleName: '', roleKey: '', roleSort: 0, dataScope: 5, status: 1, remark: '' })
  checkedKeys.value = []
  formRef.value?.clearValidate()
}

async function openDialog(row) {
  resetForm()
  if (row?.id) {
    const data = await getRole(row.id)
    Object.assign(form, data)
    // 仅回显叶子节点，父节点由 el-tree 自动半选/全选
    const leafIds = collectLeafIds(menuTreeData.value)
    checkedKeys.value = (data.menuIds || []).filter(id => leafIds.has(id))
  }
  dialogVisible.value = true
  await nextTick()
  treeRef.value?.setCheckedKeys(checkedKeys.value)
}

async function handleSubmit() {
  await formRef.value.validate()
  const menuIds = [...treeRef.value.getCheckedKeys(), ...treeRef.value.getHalfCheckedKeys()]
  submitting.value = true
  try {
    const payload = { ...form, menuIds }
    if (form.id) {
      await updateRole(payload)
      ElMessage.success('修改成功')
    } else {
      await addRole(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  await delRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  const ids = selection.value.map(s => s.id)
  await ElMessageBox.confirm(`确认删除选中的 ${ids.length} 个角色吗？`, '提示', { type: 'warning' })
  await delRole(ids.join(','))
  ElMessage.success('删除成功')
  loadData()
}

// ---------- 关联用户 ----------
const usersDialogVisible = ref(false)
const usersLoading = ref(false)
const currentRole = ref({})
const userRows = ref([])
const userTotal = ref(0)
const userQuery = reactive({ pageNum: 1, pageSize: 10 })

function openUsersDialog(row) {
  currentRole.value = row
  userQuery.pageNum = 1
  userRows.value = []
  userTotal.value = 0
  usersDialogVisible.value = true
  loadRoleUsers()
}

async function loadRoleUsers() {
  if (!currentRole.value?.id) return
  usersLoading.value = true
  try {
    const data = await roleUsers(currentRole.value.id, userQuery)
    userRows.value = data.rows || []
    userTotal.value = data.total || 0
  } finally {
    usersLoading.value = false
  }
}

onMounted(async () => {
  loadData()
  menuTreeData.value = await menuTree() || []
})
</script>

<style scoped>
.tree-box {
  width: 100%; max-height: 300px; overflow-y: auto;
  border: 1px solid var(--color-border); border-radius: 6px; padding: 8px;
}
.perm-tree :deep(.el-tree-node__content) { height: 30px; }
</style>
