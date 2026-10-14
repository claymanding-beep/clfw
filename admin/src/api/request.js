import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * Axios 实例 - 统一请求配置
 * baseURL 指向 /api，由 Vite 代理转发到后端 http://localhost:8080
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 自动注入 token（Sa-Token: Authorization: Bearer {token}）
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 统一解析 { code, msg, data }
request.interceptors.response.use(
  response => {
    const res = response.data || {}
    // 二进制流（导出等）直接返回
    if (response.request?.responseType === 'blob') {
      return response.data
    }
    if (res.code === 200) {
      return res.data
    }
    // 未授权 / 登录失效
    if (res.code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error(res.msg || '登录已失效'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      handleUnauthorized()
    } else {
      const msg = error.response?.data?.msg || error.message || '网络异常'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

let redirecting = false
function handleUnauthorized() {
  if (redirecting) return
  redirecting = true
  localStorage.removeItem('token')
  ElMessage.error('登录状态已失效，请重新登录')
  router.push('/login').finally(() => {
    redirecting = false
  })
}

export default request
