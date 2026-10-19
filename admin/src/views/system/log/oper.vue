<template>
  <div class="clfwpage">
    <el-form :inline="true" :model="query" class="clfwsearch-bar" @submit.prevent>
      <el-form-item label="操作模块">
        <el-input v-model="query.module" placeholder="请输入模块" clearable style="width: 160px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="操作人">
        <el-input v-model="query.operBy" placeholder="请输入操作人" clearable style="width: 140px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
          range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
          style="width: 240px" @change="onDateChange" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="clfwtable-card">
      <div class="clfwtoolbar">
        <el-button v-if="hasPerm('system:operlog:remove')" type="danger" :icon="Delete" @click="handleClean">清空日志</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">{{ operTypeText(row.operType) }}</template>
        </el-table-column>
        <el-table-column prop="operBy" label="操作人" width="110" />
        <el-table-column prop="operIp" label="IP" width="130" />
        <el-table-column prop="requestMethod" label="请求方式" width="90" align="center" />
        <el-table-column prop="operUrl" label="请求地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="90" align="center" />
        <el-table-column prop="operTime" label="操作时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="hasPerm('system:operlog:query')" link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="clfwpagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="680px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作模块">{{ detail.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ operTypeText(detail.operType) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operBy }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="操作地点">{{ detail.operLocation || '—' }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detail.status === 1 ? 'success' : 'danger'" size="small">{{ detail.status === 1 ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detail.operTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="log-pre">{{ detail.operParam || '—' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <pre class="log-pre">{{ detail.jsonResult || '—' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.status === 0" label="异常信息" :span="2">
          <pre class="log-pre error">{{ detail.errorMsg || '—' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { pageOperLog, getOperLog, cleanOperLog } from '@/api/system/log'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.hasPermission(p)

const operTypeMap = { INSERT: '新增', UPDATE: '修改', DELETE: '删除', GRANT: '授权', CLEAN: '清空', EXPORT: '导出', IMPORT: '导入', OTHER: '其他' }
const operTypeText = (t) => operTypeMap[t] || t || '—'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({ pageNum: 1, pageSize: 20, module: '', operBy: '', status: null, beginTime: '', endTime: '' })

const detailVisible = ref(false)
const detail = ref({})

async function loadData() {
  loading.value = true
  try {
    const data = await pageOperLog(query)
    rows.value = data.rows || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
function handleSearch() { query.pageNum = 1; loadData() }
function handleReset() {
  dateRange.value = []
  Object.assign(query, { pageNum: 1, module: '', operBy: '', status: null, beginTime: '', endTime: '' })
  loadData()
}
function onDateChange(val) {
  query.beginTime = val && val[0] ? val[0] : ''
  query.endTime = val && val[1] ? val[1] : ''
}

async function openDetail(row) {
  detail.value = await getOperLog(row.id) || row
  detailVisible.value = true
}

async function handleClean() {
  await ElMessageBox.confirm('确认清空所有操作日志吗？此操作不可恢复！', '警告', { type: 'warning' })
  await cleanOperLog()
  ElMessage.success('已清空')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.log-pre {
  margin: 0; white-space: pre-wrap; word-break: break-all;
  font-family: monospace; font-size: 12px; max-height: 160px; overflow-y: auto;
}
.log-pre.error { color: var(--color-danger); }
</style>
