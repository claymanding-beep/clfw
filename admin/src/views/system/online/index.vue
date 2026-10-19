<template>
  <div class="clfwpage">
    <el-form :inline="true" :model="query" class="clfwsearch-bar" @submit.prevent>
      <el-form-item label="登录IP">
        <el-input v-model="query.ip" placeholder="请输入登录IP" clearable style="width: 180px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="clfwtable-card">
      <div class="clfwtoolbar">
        <span class="tip">当前在线 <b>{{ rows.length }}</b> 个会话</span>
        <el-button :icon="Refresh" circle size="small" title="刷新" @click="loadData" />
      </div>

      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="姓名" width="140" />
        <el-table-column prop="ip" label="登录IP" width="150" />
        <el-table-column prop="browser" label="浏览器" min-width="140" show-overflow-tooltip />
        <el-table-column prop="os" label="操作系统" min-width="140" show-overflow-tooltip />
        <el-table-column label="登录时间" width="180">
          <template #default="{ row }">{{ formatTime(row.loginTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:online:forceLogout')" link type="danger"
              :disabled="row.tokenValue === currentToken" @click="handleForceLogout(row)">强退</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { listOnline, forceLogout } from '@/api/system/online'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)
const currentToken = localStorage.getItem('token')

const loading = ref(false)
const rows = ref([])
const query = reactive({ ip: '', username: '' })

function formatTime(ts) {
  if (!ts) return '—'
  const d = new Date(Number(ts))
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

async function loadData() {
  loading.value = true
  try {
    const data = await listOnline(query)
    rows.value = data || []
  } finally {
    loading.value = false
  }
}
function handleSearch() { loadData() }
function handleReset() { Object.assign(query, { ip: '', username: '' }); loadData() }

async function handleForceLogout(row) {
  await ElMessageBox.confirm(`确认强制退出用户「${row.username}」的会话吗？`, '提示', { type: 'warning' })
  await forceLogout(row.tokenValue)
  ElMessage.success('已强制退出')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.clfw-toolbar { display: flex; align-items: center; justify-content: space-between; }
.clfw-toolbar .tip { font-size: 13px; color: var(--color-text-secondary); }
.clfw-toolbar .tip b { color: var(--color-primary); font-size: 15px; }
</style>
