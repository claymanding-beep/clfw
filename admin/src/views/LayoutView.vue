<template>
  <div class="layout-page">
    <!-- 低分辨率提示 -->
    <div v-if="showLowResWarning" class="low-res-warning">
      ⚠ 当前屏幕分辨率较低，建议使用 1366×768 及以上分辨率以获得最佳体验
      <span class="close-btn" @click="dismissLowRes">✕</span>
    </div>

    <!-- 顶栏 -->
    <header class="topbar" :style="{ height: 'var(--topbar-height)' }">
      <div class="tb-left">
        <div class="logo">✚</div>
        <div class="sys"><b>健康守护体系</b><span v-if="!isNarrow">济宁市公共卫生医疗中心 · 慢病专病全周期一体化</span></div>
      </div>
      <div class="tb-right">
        <div class="org-chip">本院（医共体牵头） ▾</div>
        <div class="bell">🔔<i v-if="unreadCount">{{ unreadCount }}</i></div>
        <div class="user-wrap">
          <div class="user" @click="userDropdownVisible = !userDropdownVisible">
            <span class="ava">
              <img v-if="userStore.user?.avatar" :src="userStore.user.avatar" alt="" />
              <template v-else>{{ userInfo.name?.[0] || '用' }}</template>
            </span>
            <span v-if="!isEffectivelyCollapsed">{{ userInfo.name }} · {{ userInfo.role }}</span> ▾
          </div>
          <div v-if="userDropdownVisible" class="user-dropdown">
            <div class="ud-item" @click="goProfile('basic')">个人信息</div>
            <div class="ud-item" @click="goProfile('pwd')">修改密码</div>
            <div class="ud-divider"></div>
            <div class="ud-item danger" @click="handleLogout">注销登录</div>
          </div>
        </div>
      </div>
    </header>

    <div class="layout-body" :style="{ paddingTop: 'var(--topbar-height)' }">
      <!-- 左侧菜单 -->
      <aside class="side" :class="{ collapsed: isEffectivelyCollapsed, narrow: isNarrow && !isEffectivelyCollapsed }">
        <div class="collapse-btn" @click="toggleSidebar">
          {{ isEffectivelyCollapsed ? '▸' : '◂' }}
        </div>

        <!-- 工作台 -->
        <div class="mi-item"
          @mouseenter="isEffectivelyCollapsed && onMiEnter($event, 'dashboard')"
          @mouseleave="onMiLeave">
          <div class="mi" :class="{ active: currentMenu === '/dashboard' }" @click="goDashboard">
            <span class="dot"></span><span v-if="!isEffectivelyCollapsed" class="label">工作台</span>
          </div>
        </div>

        <!-- 菜单分组 -->
        <template v-for="(group, gIdx) in menuGroups" :key="group.title">
          <!-- 展开态 -->
          <template v-if="!isEffectivelyCollapsed">
            <div class="mg" @click="group.open = !group.open">
              <b>{{ group.title }}</b>{{ group.open ? '▾' : '▸' }}
            </div>
            <div v-show="group.open" class="sub">
              <a v-for="item in group.items" :key="item.path || item.name"
                :class="{ active: item.path && currentMenu === item.path }"
                @click="navigateToMenu(item)">{{ item.name }}</a>
            </div>
          </template>

          <!-- 折叠态：首字缩写 + hover 弹出 -->
          <div v-else class="mi-item"
            @mouseenter="onMiEnter($event, gIdx)"
            @mouseleave="onMiLeave">
            <div class="mg mg-collapsed">
              <b>{{ group.title[0] }}</b>
            </div>
          </div>
        </template>

        <!-- 折叠态弹出菜单 -->
        <teleport to="body">
          <div v-if="isEffectivelyCollapsed && hoveredIndex !== null && hoveredIndex !== 'dashboard'"
            class="popup-menu" :style="{ top: popupTop + 'px' }"
            @mouseenter="onPopupEnter" @mouseleave="onPopupLeave">
            <div class="popup-title">{{ menuGroups[hoveredIndex]?.title }}</div>
            <a v-for="item in menuGroups[hoveredIndex]?.items" :key="item.path || item.name"
              class="popup-item" @click="navigateToMenu(item); closePopup()">{{ item.name }}</a>
          </div>
          <div v-if="isEffectivelyCollapsed && hoveredIndex === 'dashboard'"
            class="popup-single" :style="{ top: popupTop + 'px' }">工作台</div>
        </teleport>
      </aside>

      <!-- 主工作区 -->
      <div class="main" :style="{ marginLeft: mainMarginLeft }">
        <div class="tabs">
          <span v-for="tab in openTabs" :key="tab.path"
            class="tab" :class="{ active: tab.path === activeTab }"
            @click="switchTab(tab)"
            @contextmenu.prevent="showTabContext($event, tab)">
            {{ tab.title }}
            <span v-if="tab.path !== '/dashboard'" class="x" @click.stop="closeTab(tab)">✕</span>
          </span>
        </div>
        <div class="crumb">首页 / {{ currentTabTitle }}</div>
        <div class="content">
          <router-view />
        </div>
      </div>

      <!-- Tab 右键菜单 -->
      <teleport to="body">
        <div v-if="tabContextVisible" class="tab-context" :style="{ left: tabContextX + 'px', top: tabContextY + 'px' }">
          <div class="ctx-item" @click="ctxCloseCurrent">关闭当前</div>
          <div class="ctx-item" @click="ctxCloseOthers">关闭其他</div>
          <div class="ctx-item" @click="ctxCloseLeft">关闭左侧</div>
          <div class="ctx-item" @click="ctxCloseRight">关闭右侧</div>
          <div class="ctx-divider"></div>
          <div class="ctx-item danger" @click="ctxCloseAll">全部关闭</div>
        </div>
      </teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentMenu = ref('/dashboard')
const unreadCount = ref(5)
const sidebarCollapsed = ref(false)
const showLowResWarning = ref(false)
const userDropdownVisible = ref(false)
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1920)
const hoveredIndex = ref(null)
const popupTop = ref(0)
let hideTimer = null

// 菜单名称 → 路由映射
const menuRouteMap = {
  '工作台': '/dashboard',
  '用户健康档案': '/patient/health-record',
  '用户健康画像': '/patient/health-profile',
  '慢病专档管理': '/patient/disease-archive',
  '医患关系管理': '/patient/doctor-patient',
  '用户签约管理': '/patient/user-contract'
}

// 多标签页状态
const openTabs = ref([{ title: '工作台', path: '/dashboard' }])
const activeTab = ref('/dashboard')

// Tab 右键菜单状态
const tabContextVisible = ref(false)
const tabContextX = ref(0)
const tabContextY = ref(0)
const tabContextTarget = ref(null)

const currentTabTitle = computed(() => {
  const tab = openTabs.value.find(t => t.path === activeTab.value)
  return tab ? tab.title : '工作台'
})

// 点击菜单导航（item: { name, path, isFrame }）
function navigateToMenu(item) {
  const path = item?.path
  if (!path) return // 未配置路由的菜单暂不跳转
  // 外链菜单：新窗口打开，不进入内部路由/标签体系
  if (item.isFrame === 1 || /^https?:\/\//.test(path)) {
    window.open(path, '_blank')
    return
  }
  currentMenu.value = path
  // 添加 tab
  if (!openTabs.value.find(t => t.path === path)) {
    openTabs.value.push({ title: item.name, path })
  }
  activeTab.value = path
  router.push(path)
}

function goDashboard() {
  currentMenu.value = '/dashboard'
  activeTab.value = '/dashboard'
  router.push('/dashboard')
}

// 跳转个人中心（tab: basic 基本资料 / pwd 修改密码）
function goProfile(tab) {
  userDropdownVisible.value = false
  const path = '/profile'
  if (!openTabs.value.find(t => t.path === path)) {
    openTabs.value.push({ title: '个人中心', path })
  }
  currentMenu.value = path
  activeTab.value = path
  router.push({ path, query: { tab } })
}

function switchTab(tab) {
  activeTab.value = tab.path
  currentMenu.value = tab.path
  router.push(tab.path)
}

function closeTab(tab) {
  const idx = openTabs.value.findIndex(t => t.path === tab.path)
  if (idx === -1) return
  openTabs.value.splice(idx, 1)
  // 如果关闭的是当前活动 tab，切换到前一个
  if (activeTab.value === tab.path) {
    const newTab = openTabs.value[Math.min(idx, openTabs.value.length - 1)]
    activeTab.value = newTab.path
    currentMenu.value = newTab.path
    router.push(newTab.path)
  }
}

// ---------- Tab 右键菜单操作 ----------
function showTabContext(event, tab) {
  tabContextTarget.value = tab
  tabContextX.value = event.clientX
  tabContextY.value = event.clientY
  tabContextVisible.value = true
}

function hideTabContext() {
  tabContextVisible.value = false
}

function ctxCloseCurrent() {
  hideTabContext()
  const tab = tabContextTarget.value
  if (tab && tab.path !== '/dashboard') {
    closeTab(tab)
  }
}

function ctxCloseOthers() {
  hideTabContext()
  const target = tabContextTarget.value
  openTabs.value = openTabs.value.filter(t => t.path === '/dashboard' || t.path === target?.path)
  if (target) {
    activeTab.value = target.path
    currentMenu.value = target.path
    router.push(target.path)
  }
}

function ctxCloseLeft() {
  hideTabContext()
  const target = tabContextTarget.value
  const idx = openTabs.value.findIndex(t => t.path === target?.path)
  if (idx <= 0) return
  openTabs.value = openTabs.value.filter((t, i) => i >= idx || t.path === '/dashboard')
  if (!openTabs.value.find(t => t.path === activeTab.value)) {
    activeTab.value = target.path
    currentMenu.value = target.path
    router.push(target.path)
  }
}

function ctxCloseRight() {
  hideTabContext()
  const target = tabContextTarget.value
  const idx = openTabs.value.findIndex(t => t.path === target?.path)
  if (idx === -1 || idx === openTabs.value.length - 1) return
  openTabs.value = openTabs.value.filter((t, i) => i <= idx || t.path === '/dashboard')
  if (!openTabs.value.find(t => t.path === activeTab.value)) {
    activeTab.value = target.path
    currentMenu.value = target.path
    router.push(target.path)
  }
}

function ctxCloseAll() {
  hideTabContext()
  openTabs.value = [{ title: '工作台', path: '/dashboard' }]
  activeTab.value = '/dashboard'
  currentMenu.value = '/dashboard'
  router.push('/dashboard')
}

// 监听路由变化同步 tab 状态
watch(() => route.path, (newPath) => {
  const tab = openTabs.value.find(t => t.path === newPath)
  if (tab) {
    activeTab.value = newPath
    currentMenu.value = tab.path
  }
})

// 是否为窄版侧边栏（1024~1440px 之间自动缩窄，但仍显示文字）
const isNarrow = computed(() => windowWidth.value <= 1440 && windowWidth.value > 1024)

// 是否实际处于折叠态（手动折叠 OR 视口 <1024px 自动折叠）
const isEffectivelyCollapsed = computed(() => sidebarCollapsed.value || windowWidth.value < 1024)

// 主工作区左边距，跟随侧边栏实际宽度
const mainMarginLeft = computed(() => {
  if (isEffectivelyCollapsed.value) return 'var(--sidebar-collapsed-width)'
  return 'var(--sidebar-width)'
})

// 角色标识 -> 中文
const roleNameMap = {
  super_admin: '超级管理员', org_admin: '机构管理员', dept_director: '科室主任',
  doctor: '医生', nurse: '护士', health_manager: '健康管理师', auditor: '审核人员'
}
const userInfo = computed(() => ({
  name: userStore.nickname,
  role: userStore.isSuperAdmin ? '超级管理员' : (roleNameMap[userStore.roles?.[0]] || userStore.roles?.[0] || '—')
}))

// 业务占位菜单：名称 -> 路由（多数暂未开放）
function toItems(names) {
  return names.map(n => ({ name: n, path: menuRouteMap[n] || null }))
}

const menuGroups = reactive([
  {
    title: '基础支撑与配置管理', open: false,
    items: toItems(['医疗机构管理', '权限管理', '服务团队管理', '资料库管理', '批量消息通知', '指标预警配置', '服务项目管理', '任务模板管理', '履约规则配置', '问卷管理', '自定义标签'])
  },
  { title: '用户档案与画像管理', open: false, items: toItems(['用户健康档案', '用户健康画像', '慢病专档管理', '医患关系管理', '用户签约管理']) },
  { title: '数据接入与处理', open: false, items: toItems(['HIS/LIS数据对接', '检验检查报告管理', '用药信息管理']) },
  { title: '干预与随访管理', open: false, items: toItems(['健康干预（健康处方）', '用药方案管理', '服务任务管理', '随访管理', '复诊提醒', '异常提醒']) },
  { title: '评估、筛查与统计分析', open: false, items: toItems(['用户健康评估报告', '慢病评估报告生成', '并发症筛查', '用户数据统计', '用户服务统计', '数据大屏', '标签自动更新']) }
])

// 从后端路由树构建系统管理菜单分组
function resolvePath(base, path) {
  if (!path) return base
  if (path.startsWith('/')) return path
  return (base.endsWith('/') ? base.slice(0, -1) : base) + '/' + path
}
function collectMenuItems(node, basePath, acc) {
  const isFrame = node.isFrame === 1
  const fullPath = isFrame ? node.path : resolvePath(basePath, node.path)
  if (node.menuType === 'C') acc.push({ name: node.menuName, path: fullPath, isFrame })
  ;(node.children || []).forEach(c => collectMenuItems(c, isFrame ? basePath : fullPath, acc))
}
function appendSystemMenus(routers) {
  const existing = new Set(menuGroups.map(g => g.title))
  ;(routers || []).forEach(top => {
    if (existing.has(top.menuName)) return
    const topIsFrame = top.isFrame === 1
    const basePath = topIsFrame ? top.path : ((top.path || '').startsWith('/') ? top.path : '/' + (top.path || ''))
    const items = []
    if (top.menuType === 'C') items.push({ name: top.menuName, path: basePath, isFrame: topIsFrame })
    ;(top.children || []).forEach(c => collectMenuItems(c, basePath, items))
    if (items.length) menuGroups.push({ title: top.menuName, open: true, items })
  })
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function onMiEnter(event, index) {
  clearTimeout(hideTimer)
  const rect = event.currentTarget.getBoundingClientRect()
  popupTop.value = rect.top
  hoveredIndex.value = index
}

function onMiLeave() {
  // 延迟隐藏，给鼠标移入弹出层的时间
  hideTimer = setTimeout(() => {
    hoveredIndex.value = null
  }, 150)
}

function onPopupEnter() {
  // 鼠标移入弹出层，取消隐藏
  clearTimeout(hideTimer)
}

function onPopupLeave() {
  // 鼠标离开弹出层，延迟关闭
  hideTimer = setTimeout(() => {
    hoveredIndex.value = null
  }, 150)
}

function closePopup() {
  clearTimeout(hideTimer)
  hoveredIndex.value = null
}

function handleResize() {
  windowWidth.value = window.innerWidth
  // 低分辨率提示（宽度<1280 且 高度>768 时显示）
  if (window.innerWidth < 1280 && window.innerHeight > 768 && !localStorage.getItem('lowResDismissed')) {
    showLowResWarning.value = true
  } else {
    showLowResWarning.value = false
  }
}

async function handleLogout() {
  userDropdownVisible.value = false
  await userStore.logout()
  router.push('/login')
}

function dismissLowRes() {
  showLowResWarning.value = false
  localStorage.setItem('lowResDismissed', '1')
}

onMounted(async () => {
  handleResize()
  window.addEventListener('resize', handleResize)
  // 点击其他区域关闭右键菜单和用户下拉
  document.addEventListener('click', handleGlobalClick)
  // 加载用户信息与动态菜单
  if (userStore.token) {
    try {
      if (!userStore.user?.id) await userStore.getInfo()
      const routers = await userStore.getRouters()
      appendSystemMenus(routers)
    } catch (e) {
      console.error('加载用户信息/菜单失败', e)
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('click', handleGlobalClick)
})

function handleGlobalClick(e) {
  hideTabContext()
  // 点击用户区域外部时关闭下拉
  if (!e.target.closest('.user-wrap')) {
    userDropdownVisible.value = false
  }
}
</script>

<style scoped>
.layout-page { min-height: 100vh; background: var(--color-bg); }

/* ---------- 顶栏 ---------- */
.topbar {
  background: var(--color-primary-dark); color: #fff;
  display: flex; align-items: center; justify-content: space-between; padding: 0 16px;
  position: fixed; top: 0; left: 0; right: 0; z-index: 10;
  transition: height 0.2s ease;
}
.tb-left { display: flex; align-items: center; gap: 10px; }
.logo {
  width: 32px; height: 32px; border-radius: 50%; background: #fff; color: var(--color-primary-dark);
  display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px;
  flex: none;
}
.sys b { font-size: 15px; letter-spacing: 1px; }
.sys span { font-size: 11px; opacity: .7; margin-left: 8px; letter-spacing: 0.5px; }

.tb-right { display: flex; align-items: center; gap: 14px; font-size: 13px; }
.org-chip {
  background: rgba(255,255,255,.12); border: 1px solid rgba(255,255,255,.25);
  border-radius: 6px; padding: 4px 10px; cursor: pointer; font-size: 12px;
}
.bell { position: relative; cursor: pointer; font-size: 16px; }
.bell i {
  position: absolute; top: -6px; right: -8px; background: var(--color-danger); color: #fff;
  font-style: normal; font-size: 10px; line-height: 14px; min-width: 14px; border-radius: 8px;
  text-align: center; padding: 0 3px;
}
.user { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 12px; }
.ava {
  width: 28px; height: 28px; border-radius: 50%; background: var(--color-primary);
  border: 1px solid rgba(255,255,255,.5); display: flex; align-items: center; justify-content: center; font-size: 12px;
  flex: none; overflow: hidden;
}
.ava img { width: 100%; height: 100%; object-fit: cover; }

/* 用户下拉菜单 */
.user-wrap { position: relative; }
.user-dropdown {
  position: absolute; top: 100%; right: 0; margin-top: 8px;
  background: #fff; border: 1px solid var(--color-border); border-radius: 6px;
  box-shadow: 4px 4px 16px rgba(0,0,0,.12); padding: 4px 0;
  min-width: 130px; z-index: 20;
}
.ud-item {
  padding: 8px 16px; font-size: 13px; color: var(--color-text);
  cursor: pointer; white-space: nowrap;
}
.ud-item:hover {
  background: var(--color-primary-light); color: var(--color-primary);
}
.ud-item.danger:hover {
  background: #FDECEC; color: var(--color-danger);
}
.ud-divider {
  height: 1px; background: var(--color-border); margin: 4px 0;
}

/* ---------- 布局 ---------- */
.layout-body { display: flex; transition: padding-top 0.2s ease; }

.side {
  width: var(--sidebar-width); background: #fff; border-right: 1px solid var(--color-border);
  position: fixed; top: var(--topbar-height); bottom: 0; overflow-y: auto; overflow-x: hidden;
  padding: 8px 0; transition: width 0.2s ease;
}
.side::-webkit-scrollbar { width: 4px; }
.side::-webkit-scrollbar-thumb { background: #D8DCE3; border-radius: 2px; }

/* 窄版侧边栏（1024~1440px）：压缩间距和字号，但仍显示文字 */
.side.narrow {
  padding: 4px 0;
}
.side.narrow .mi { padding: 8px 12px; gap: 6px; font-size: 12px; }
.side.narrow .mg { padding: 8px 12px 4px; font-size: 11px; }
.side.narrow .mg b { font-size: 12px; }
.side.narrow .sub a { padding: 6px 12px 6px 24px; font-size: 12px; }
.side.narrow .collapse-btn { height: 26px; margin: 0 8px 4px; }

/* 折叠态 */
.side.collapsed {
  width: var(--sidebar-collapsed-width);
}
.side.collapsed .mi { justify-content: center; padding: 11px 0; }
.side.collapsed .mg-collapsed {
  justify-content: center; padding: 12px 0;
  cursor: pointer; border-radius: 4px; margin: 2px 8px;
}
.side.collapsed .mg-collapsed b {
  font-size: 16px; font-weight: 700; color: var(--color-primary);
}
.side.collapsed .mg-collapsed:hover {
  background: var(--color-primary-light);
}

/* 折叠态菜单项容器 */
.mi-item { position: relative; }

/* 折叠按钮 */
.collapse-btn {
  display: flex; align-items: center; justify-content: center;
  height: 32px; margin: 0 12px 8px; border-radius: 4px;
  background: var(--color-bg); color: var(--color-text-secondary);
  cursor: pointer; font-size: 12px; transition: background 0.15s;
}
.collapse-btn:hover { background: var(--color-primary-light); color: var(--color-primary); }

.mi {
  display: flex; align-items: center; gap: 10px; padding: 11px 16px;
  cursor: pointer; color: var(--color-text); white-space: nowrap;
}
.mi:hover { background: var(--color-primary-light); }
.mi.active {
  background: var(--color-primary-light); color: var(--color-primary);
  font-weight: 600; border-right: 3px solid var(--color-primary);
}
.mi .dot { width: 6px; height: 6px; border-radius: 50%; background: #C3C9D4; flex: none; }
.mi.active .dot { background: var(--color-primary); }
.mi .label { overflow: hidden; }

.mg { padding: 10px 16px 6px; color: #9AA1AC; font-size: 12px; display: flex; justify-content: space-between; cursor: pointer; white-space: nowrap; }
.mg b { color: var(--color-text); font-size: 13px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; }

.sub a { display: block; padding: 8px 16px 8px 32px; color: var(--color-text-secondary); cursor: pointer; white-space: nowrap; }
.sub a:hover { color: var(--color-primary); background: var(--color-primary-light); }
.sub a.active { color: var(--color-primary); background: var(--color-primary-light); font-weight: 600; }

/* ---------- 折叠态弹出菜单 ---------- */
.popup-menu {
  position: fixed; left: var(--sidebar-collapsed-width); z-index: 100;
  background: #fff; border: 1px solid var(--color-border); border-radius: 6px;
  box-shadow: 4px 4px 16px rgba(0,0,0,.12); padding: 6px 0;
  min-width: 180px; max-height: 360px; overflow-y: auto;
}
.popup-title {
  padding: 8px 16px 6px; font-size: 12px; font-weight: 600;
  color: var(--color-text-secondary); border-bottom: 1px solid var(--color-border);
  margin-bottom: 4px; white-space: nowrap;
}
.popup-item {
  display: block; padding: 8px 16px; font-size: 13px;
  color: var(--color-text); cursor: pointer; white-space: nowrap;
}
.popup-item:hover {
  background: var(--color-primary-light); color: var(--color-primary);
}
.popup-single {
  position: fixed; left: var(--sidebar-collapsed-width); z-index: 100;
  background: var(--color-primary); color: #fff; border-radius: 4px;
  padding: 6px 14px; font-size: 13px; white-space: nowrap;
  box-shadow: 2px 2px 8px rgba(0,0,0,.15);
}

/* ---------- 主区 ---------- */
.main { flex: 1; display: flex; flex-direction: column; transition: margin-left 0.2s ease; }
.tabs {
  height: var(--tabs-height, 40px); background: #fff; border-bottom: 1px solid var(--color-border);
  display: flex; align-items: center; padding: 0 16px; gap: 6px;
  overflow-x: auto; overflow-y: hidden; white-space: nowrap;
}
.tabs::-webkit-scrollbar { height: 0; }
.tab {
  padding: 4px 12px; border-radius: 4px; background: var(--color-bg); color: var(--color-text-secondary);
  font-size: 12px; display: flex; gap: 6px; align-items: center; cursor: pointer;
  border: 1px solid var(--color-border); transition: all 0.15s;
}
.tab:hover { color: var(--color-primary); border-color: var(--color-primary); }
.tab.active {
  background: var(--color-primary); color: #fff; border-color: var(--color-primary);
}
.tab .x { opacity: .7; cursor: pointer; font-size: 10px; }
.tab .x:hover { opacity: 1; }
.tab.active .x:hover { color: #fdd; }

/* ---------- Tab 右键菜单 ---------- */
.tab-context {
  position: fixed; z-index: 200;
  background: #fff; border: 1px solid var(--color-border); border-radius: 6px;
  box-shadow: 4px 4px 16px rgba(0,0,0,.12); padding: 4px 0;
  min-width: 130px;
}
.ctx-item {
  padding: 8px 16px; font-size: 13px; color: var(--color-text);
  cursor: pointer; white-space: nowrap;
}
.ctx-item:hover {
  background: var(--color-primary-light); color: var(--color-primary);
}
.ctx-item.danger:hover {
  background: #FDECEC; color: var(--color-danger);
}
.ctx-divider {
  height: 1px; background: var(--color-border); margin: 4px 0;
}

.crumb { padding: var(--crumb-padding, 10px 20px 0); color: #9AA1AC; font-size: 12px; }
.content { padding: var(--content-padding-top, 14px) 20px var(--content-padding-bottom, 24px); display: flex; flex-direction: column; gap: var(--content-gap, 14px); }
</style>
