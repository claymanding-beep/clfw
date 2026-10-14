<template>
  <div class="hg-page">
    <el-form :inline="true" :model="query" class="hg-search-bar" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="登录IP">
        <el-input v-model="query.ip" placeholder="请输入IP" clearable style="width: 160px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="登录时间">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
          range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
          style="width: 240px" @change="onDateChange" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="hg-table-card">
      <div class="hg-toolbar">
        <el-button v-if="hasPerm('system:loginlog:remove')" type="danger" :icon="Delete" @click="handleClean">清空日志</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="ip" label="登录IP" width="140" />
        <el-table-column prop="loginLocation" label="登录地点" min-width="140">
          <template #default="{ row }">{{ row.loginLocation || '—' }}</template>
        </el-table-column>
        <el-table-column prop="browser" label="浏览器" width="130" />
        <el-table-column prop="os" label="操作系统" width="130" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="提示消息" min-width="140" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="170" />
      </el-table>

      <div class="hg-pagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { pageLoginLog, cleanLoginLog } from '@/api/system/log'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({ pageNum: 1, pageSize: 20, username: '', ip: '', status: null, beginTime: '', endTime: '' })

async function loadData() {
  loading.value = true
  try {
    const data = await pageLoginLog(query)
    rows.value = data.rows || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
function handleSearch() { query.pageNum = 1; loadData() }
function handleReset() {
  dateRange.value = []
  Object.assign(query, { pageNum: 1, username: '', ip: '', status: null, beginTime: '', endTime: '' })
  loadData()
}
function onDateChange(val) {
  query.beginTime = val && val[0] ? val[0] : ''
  query.endTime = val && val[1] ? val[1] : ''
}
async function handleClean() {
  await ElMessageBox.confirm('确认清空所有登录日志吗？此操作不可恢复！', '警告', { type: 'warning' })
  await cleanLoginLog()
  ElMessage.success('已清空')
  loadData()
}

onMounted(loadData)
</script>
