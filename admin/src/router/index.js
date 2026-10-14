import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', anonymous: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/LayoutView.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Workbench.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      },
      // ---------------- 用户档案与画像管理（占位页） ----------------
      {
        path: 'patient/health-record',
        name: 'HealthRecord',
        component: () => import('@/views/patient/HealthRecord.vue'),
        meta: { title: '用户健康档案' }
      },
      {
        path: 'patient/health-profile',
        name: 'HealthProfile',
        component: () => import('@/views/patient/HealthProfile.vue'),
        meta: { title: '用户健康画像' }
      },
      {
        path: 'patient/disease-archive',
        name: 'DiseaseArchive',
        component: () => import('@/views/patient/DiseaseArchive.vue'),
        meta: { title: '慢病专档管理' }
      },
      {
        path: 'patient/doctor-patient',
        name: 'DoctorPatient',
        component: () => import('@/views/patient/DoctorPatient.vue'),
        meta: { title: '医患关系管理' }
      },
      {
        path: 'patient/user-contract',
        name: 'UserContract',
        component: () => import('@/views/patient/UserContract.vue'),
        meta: { title: '用户签约管理' }
      },
      // ---------------- 系统管理 ----------------
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理' }
      },
      {
        path: 'system/org',
        name: 'SystemOrg',
        component: () => import('@/views/system/org/index.vue'),
        meta: { title: '机构管理' }
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '科室管理' }
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '数据字典' }
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '参数设置' }
      },
      {
        path: 'system/operlog',
        name: 'SystemOperLog',
        component: () => import('@/views/system/log/oper.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'system/loginlog',
        name: 'SystemLoginLog',
        component: () => import('@/views/system/log/login.vue'),
        meta: { title: '登录日志' }
      },
      {
        path: 'system/online',
        name: 'SystemOnline',
        component: () => import('@/views/system/online/index.vue'),
        meta: { title: '在线用户' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫：无 token 一律跳转登录页
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '健康守护体系'} - 健康守护体系`
  const token = localStorage.getItem('token')
  if (to.meta.anonymous) {
    // 已登录访问登录页，直接进首页
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
    return
  }
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router
