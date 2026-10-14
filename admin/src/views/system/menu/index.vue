<template>
  <div class="hg-page">
    <div class="hg-table-card">
      <div class="hg-toolbar">
        <el-button v-if="hasPerm('system:menu:add')" type="primary" :icon="Plus" @click="openDialog()">新增</el-button>
        <el-button :icon="Sort" @click="toggleExpand">展开/折叠</el-button>
      </div>

      <el-table v-if="tableVisible" v-loading="loading" :data="rows" row-key="id" border
        :default-expand-all="expandAll" :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="图标" width="70" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="iconComp(row.icon)" /></el-icon>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="70" align="center" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeTag(row.menuType)" size="small">{{ menuTypeText(row.menuType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识" min-width="160" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="160" show-overflow-tooltip />
        <el-table-column label="外链" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isFrame === 1" type="warning" size="small">是</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:menu:add') && row.menuType !== 'F'" link type="primary" @click="openDialog(null, row.id)">新增</el-button>
            <el-button v-if="hasPerm('system:menu:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="hasPerm('system:menu:remove')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="640px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select v-model="form.parentId" :data="parentOptions" check-strictly
            :props="{ label: 'menuName', children: 'children', value: 'id' }"
            node-key="id" placeholder="选择上级菜单" style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="菜单图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 Setting" />
        </el-form-item>
        <el-form-item label="显示排序">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="路由地址" prop="path">
          <el-input v-model="form.path" :placeholder="form.isFrame === 1 ? '外链请填完整地址，如 https://example.com' : '如 /system 或 user'" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'C'" label="组件路径">
          <el-input v-model="form.component" placeholder="如 system/user/index" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="是否外链">
          <el-radio-group v-model="form.isFrame">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'M'" label="权限标识">
          <el-input v-model="form.perms" placeholder="如 system:user:list" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="显示状态">
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单状态">
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
import * as Icons from '@element-plus/icons-vue'
import { menuTree, addMenu, updateMenu, delMenu } from '@/api/system/menu'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const expandAll = ref(true)
const tableVisible = ref(true)

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, parentId: 0, menuName: '', menuType: 'C', icon: '', path: '', component: '', perms: '', orderNum: 0, isFrame: 0, visible: 1, status: 1 })

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// 上级菜单下拉：在树顶部插入「主目录」
const parentOptions = computed(() => [{ id: 0, menuName: '主目录', children: rows.value }])

const menuTypeText = (t) => ({ M: '目录', C: '菜单', F: '按钮' }[t] || t)
const menuTypeTag = (t) => ({ M: 'warning', C: 'primary', F: 'info' }[t] || 'info')
// 图标名兼容：数据库存的小写别名映射到 Element Plus 组件
const iconAlias = { setting: 'Setting', user: 'User', peoples: 'UserFilled', 'tree-table': 'Grid', tree: 'Share', post: 'Postcard', dict: 'Collection', edit: 'Edit', form: 'Document', logininfor: 'Clock' }
function iconComp(name) {
  const key = iconAlias[name] || name
  return Icons[key] || Icons.Menu
}

async function loadData() {
  loading.value = true
  try {
    rows.value = await menuTree() || []
  } finally {
    loading.value = false
  }
}

// 切换展开/折叠：通过重渲染表格生效
function toggleExpand() {
  expandAll.value = !expandAll.value
  tableVisible.value = false
  nextTick(() => { tableVisible.value = true })
}

function resetForm() {
  Object.assign(form, { id: null, parentId: 0, menuName: '', menuType: 'C', icon: '', path: '', component: '', perms: '', orderNum: 0, isFrame: 0, visible: 1, status: 1 })
  formRef.value?.clearValidate()
}

function openDialog(row, parentId) {
  resetForm()
  if (row?.id) {
    Object.assign(form, row)
  } else if (parentId !== undefined) {
    form.parentId = parentId
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateMenu(form)
      ElMessage.success('修改成功')
    } else {
      await addMenu(form)
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
    ElMessage.warning('存在子菜单，请先删除子菜单')
    return
  }
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」吗？`, '提示', { type: 'warning' })
  await delMenu(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
