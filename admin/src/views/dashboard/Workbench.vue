<template>
  <div class="workbench">
    <!-- 问候 -->
    <div class="card hello">
      <div class="l">
        <b>早上好，{{ userInfo.name }}</b>
        <span>{{ todayStr }} ｜ 值班机构：济宁市公共卫生医疗中心</span>
      </div>
      <div class="r">
        <button class="btn">数据大屏</button>
        <button class="btn pri">+ 新建随访计划</button>
      </div>
    </div>

    <!-- 统计卡 -->
    <div class="stats">
      <div class="card stat" v-for="s in stats" :key="s.label">
        <div :class="['ic', s.bg]">{{ s.icon }}</div>
        <div>
          <div class="n">{{ s.value }}</div>
          <div class="t">{{ s.label }}</div>
          <div :class="['ext', s.extColor]">{{ s.ext }}</div>
        </div>
      </div>
    </div>

    <!-- 待办 + 预警 -->
    <div class="grid2">
      <div class="card">
        <div class="card-h"><b>待办服务任务</b><a>查看全部 →</a></div>
        <table>
          <tr><th>任务类型</th><th>用户</th><th>病种</th><th>责任医生</th><th>计划日期</th><th>状态</th><th>操作</th></tr>
          <tr v-for="task in tasks" :key="task.id">
            <td>{{ task.type }}</td>
            <td>{{ task.user }}</td>
            <td>{{ task.disease }}</td>
            <td>{{ task.doctor }}</td>
            <td>{{ task.date }}</td>
            <td><span :class="['chip', task.status]">{{ task.statusText }}</span></td>
            <td class="lk">处理</td>
          </tr>
        </table>
      </div>
      <div class="card">
        <div class="card-h"><b>实时指标预警</b><a>预警配置 →</a></div>
        <div class="warn">
          <div class="wi" v-for="w in warnings" :key="w.id">
            <span :class="['lv', `lv-${w.level}`]"></span>
            <div class="nm"><b>{{ w.name }}</b><span>{{ w.info }}</span></div>
            <span :class="['val', `val-${w.level}`]">{{ w.value }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="grid-chart">
      <div class="card">
        <div class="card-h"><b>近7日随访完成率</b><a>用户服务统计 →</a></div>
        <div class="bars">
          <div class="bar" v-for="(d, i) in followUpData" :key="i">
            <em>{{ d.rate }}</em><i :style="{ height: d.height }"></i><span>{{ d.label }}</span>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-h"><b>专病建档分布</b><a>用户数据统计 →</a></div>
        <div class="hbars">
          <div class="hb" v-for="d in diseaseData" :key="d.name">
            <div class="row"><span>{{ d.name }}</span><span>{{ d.count }} 人</span></div>
            <div class="track"><div class="fill" :style="{ width: d.pct }"></div></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="card">
      <div class="card-h"><b>快捷入口</b></div>
      <div class="quick">
        <div class="q" v-for="q in shortcuts" :key="q.name"><span class="qi">{{ q.icon }}</span>{{ q.name }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const userInfo = reactive({ name: '张伟' })
const todayStr = computed(() => {
  const d = new Date()
  const week = ['日','一','二','三','四','五','六']
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 星期${week[d.getDay()]}`
})

// TODO: 后端完成后通过接口获取真实数据
const stats = ref([
  { icon: '🗂', label: '今日待办任务', value: 12, bg: 'c-blue', ext: '其中逾期 2 项', extColor: 'ext-danger' },
  { icon: '⚠', label: '指标预警', value: 5, bg: 'c-red', ext: '危急 1 · 警告 3', extColor: 'ext-danger' },
  { icon: '🏥', label: '今日应复诊', value: 8, bg: 'c-orange', ext: '已到院 3 人', extColor: 'ext-muted' },
  { icon: '📝', label: '待审核签约', value: 3, bg: 'c-green', ext: '最早提交于昨日', extColor: 'ext-muted' }
])

const tasks = ref([
  { id: 1, type: '高血压随访', user: '王秀兰', disease: '高血压', doctor: '崔松', date: '08-27', status: 'today', statusText: '今日待办' },
  { id: 2, type: '糖尿病随访', user: '李建国', disease: '糖尿病', doctor: '于世鹏', date: '08-27', status: 'today', statusText: '今日待办' },
  { id: 3, type: '复诊提醒', user: '张桂芳', disease: '冠心病', doctor: '崔松', date: '08-26', status: 'late', statusText: '逾期' },
  { id: 4, type: '指标异常回访', user: '刘志远', disease: '糖尿病', doctor: '于世鹏', date: '08-28', status: 'near', statusText: '临近' },
  { id: 5, type: '满意度随访问卷', user: '陈玉梅', disease: '高脂血症', doctor: '孙丽丽（健管师）', date: '08-29', status: 'ok', statusText: '未到期' }
])

const warnings = ref([
  { id: 1, name: '王秀兰 · 空腹血糖', info: '高血压 · 责任医生 崔松', value: '12.6 mmol/L 危急', level: 'danger' },
  { id: 2, name: '李建国 · 血压', info: '糖尿病 · 责任医生 于世鹏', value: '168/102 警告', level: 'warning' },
  { id: 3, name: '张桂芳 · 低密度脂蛋白', info: '冠心病 · 责任医生 崔松', value: '4.9 mmol/L', level: 'warning' },
  { id: 4, name: '刘志远 · 血氧', info: '糖尿病 · 责任医生 于世鹏', value: '92% 提示', level: 'info' }
])

const followUpData = ref([
  { rate: '82%', height: '62%', label: '08-21' },
  { rate: '88%', height: '70%', label: '08-22' },
  { rate: '76%', height: '55%', label: '08-23' },
  { rate: '91%', height: '76%', label: '08-24' },
  { rate: '95%', height: '82%', label: '08-25' },
  { rate: '89%', height: '72%', label: '08-26' },
  { rate: '93%', height: '79%', label: '08-27' }
])

const diseaseData = ref([
  { name: '高血压', count: '1,268', pct: '100%' },
  { name: '糖尿病', count: '986', pct: '78%' },
  { name: '高脂血症', count: '642', pct: '51%' },
  { name: '冠心病', count: '315', pct: '25%' },
  { name: '慢阻肺', count: '208', pct: '16%' }
])

const shortcuts = ref([
  { icon: '🏛', name: '医疗机构管理' },
  { icon: '🛡', name: '权限管理' },
  { icon: '👥', name: '服务团队管理' },
  { icon: '📚', name: '资料库管理' },
  { icon: '📋', name: '问卷管理' },
  { icon: '📊', name: '数据大屏' }
])

// TODO: 后端完成后调用接口加载数据
// onMounted(async () => {
//   stats.value = await getDashboardStats()
//   tasks.value = await getTodoTasks()
//   warnings.value = await getWarnings()
//   followUpData.value = await getFollowUpRate()
//   diseaseData.value = await getDiseaseDistribution()
// })
</script>

<style scoped>
.workbench { display: flex; flex-direction: column; gap: 14px; }

.card { background: #fff; border-radius: 8px; border: 1px solid var(--color-border); }
.card-h { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid var(--color-border); }
.card-h b { font-size: 14px; }
.card-h b::before { content: ''; display: inline-block; width: 3px; height: 14px; background: var(--color-primary); border-radius: 2px; margin-right: 8px; vertical-align: -2px; }
.card-h a { color: var(--color-primary); font-size: 12px; cursor: pointer; }

/* 问候条 */
.hello { display: flex; justify-content: space-between; align-items: center; padding: 16px; }
.hello .l b { font-size: 17px; }
.hello .l span { color: var(--color-text-secondary); margin-left: 14px; }
.hello .r { display: flex; gap: 10px; }
.btn { padding: 7px 16px; border-radius: 6px; font-size: 13px; cursor: pointer; border: 1px solid var(--color-border); background: #fff; }
.btn.pri { background: var(--color-primary); border-color: var(--color-primary); color: #fff; }
.btn.pri:hover { filter: brightness(1.1); }

/* 统计卡 */
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.stat { padding: 16px; display: flex; gap: 14px; align-items: center; }
.stat .ic { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex: none; }
.stat .n { font-size: 24px; font-weight: 700; line-height: 1.1; }
.stat .t { color: var(--color-text-secondary); margin-top: 2px; }
.stat .ext { font-size: 11px; margin-top: 4px; }
.ext-danger { color: var(--color-danger); }
.ext-muted { color: var(--color-text-secondary); }
.c-blue { background: var(--color-primary-light); color: var(--color-primary); }
.c-red { background: #FDECEC; color: var(--color-danger); }
.c-orange { background: #FDF5E8; color: var(--color-warning); }
.c-green { background: #E9F6F0; color: var(--color-success); }

/* 中部两栏 */
.grid2 { display: grid; grid-template-columns: 2fr 1fr; gap: 14px; }
table { width: 100%; border-collapse: collapse; }
th { background: #FAFBFC; text-align: left; padding: 9px 16px; color: var(--color-text-secondary); font-weight: 500; border-bottom: 1px solid var(--color-border); }
td { padding: 10px 16px; border-bottom: 1px solid #F0F2F5; }
tr:hover td { background: #FAFCFF; }
.chip { padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.chip.today { background: var(--color-primary-light); color: var(--color-primary); }
.chip.late { background: #FDECEC; color: var(--color-danger); }
.chip.near { background: #FDF5E8; color: var(--color-warning); }
.chip.ok { background: #E9F6F0; color: var(--color-success); }
.lk { color: var(--color-primary); cursor: pointer; }

/* 预警列表 */
.warn { padding: 6px 0; }
.wi { display: flex; gap: 10px; align-items: center; padding: 9px 16px; }
.lv { width: 8px; height: 8px; border-radius: 50%; flex: none; }
.lv-danger { background: var(--color-danger); }
.lv-warning { background: var(--color-warning); }
.lv-info { background: var(--color-primary); }
.nm { flex: 1; }
.nm b { display: block; font-size: 13px; }
.nm span { color: var(--color-text-secondary); font-size: 12px; }
.val { font-weight: 700; }
.val-danger { color: var(--color-danger); }
.val-warning { color: var(--color-warning); }
.val-info { color: var(--color-primary); }

/* 图表 */
.grid-chart { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.bars { display: flex; align-items: flex-end; gap: 18px; height: 140px; padding: 20px 24px 8px; }
.bar { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; height: 100%; justify-content: flex-end; }
.bar i { width: 22px; background: linear-gradient(180deg, #4D8FD1, var(--color-primary)); border-radius: 4px 4px 0 0; display: block; }
.bar span { font-size: 11px; color: var(--color-text-secondary); }
.bar em { font-style: normal; font-size: 11px; color: var(--color-primary); font-weight: 600; }

.hbars { padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; }
.hb .row { display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 5px; }
.hb .track { height: 8px; background: #EEF1F5; border-radius: 4px; overflow: hidden; }
.hb .fill { height: 100%; border-radius: 4px; background: var(--color-primary); }

/* 快捷入口 */
.quick { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; padding: 16px; }
.q { border: 1px solid var(--color-border); border-radius: 8px; padding: 12px 6px; text-align: center; cursor: pointer; transition: .15s; }
.q:hover { border-color: var(--color-primary); color: var(--color-primary); box-shadow: 0 4px 10px rgba(0,80,160,.12); }
.q .qi { font-size: 20px; display: block; margin-bottom: 6px; }
</style>
