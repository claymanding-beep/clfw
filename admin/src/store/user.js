import { defineStore } from 'pinia'
import { login as loginApi, getInfo as getInfoApi, getRouters as getRoutersApi, logout as logoutApi } from '@/api/auth'

const TOKEN_KEY = 'token'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: {},
    roles: [],
    permissions: [],
    routers: []
  }),
  getters: {
    nickname: (state) => state.user.nickname || state.user.username || '用户',
    isSuperAdmin: (state) => state.permissions.includes('*:*:*')
  },
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },
    async login(loginForm) {
      const data = await loginApi({
        username: loginForm.username.trim(),
        password: loginForm.password,
        code: loginForm.code,
        uuid: loginForm.uuid
      })
      this.setToken(data.tokenValue)
      return data
    },
    async getInfo() {
      const data = await getInfoApi()
      this.user = data.user || {}
      this.roles = data.roles || []
      this.permissions = data.permissions || []
      return data
    },
    async getRouters() {
      const data = await getRoutersApi()
      this.routers = data || []
      return this.routers
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 忽略登出接口异常，前端强制清理
      }
      this.resetState()
    },
    resetState() {
      this.token = ''
      this.user = {}
      this.roles = []
      this.permissions = []
      this.routers = []
      localStorage.removeItem(TOKEN_KEY)
    },
    /** 判断是否拥有某权限标识 */
    hasPermission(perm) {
      if (!perm) return true
      if (this.isSuperAdmin) return true
      return this.permissions.includes(perm)
    }
  }
})
