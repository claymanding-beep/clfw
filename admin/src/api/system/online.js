import request from '@/api/request'

/** 在线用户列表 -> SysUserOnline[] */
export function listOnline(params) {
  return request.get('/system/online/list', { params })
}

/** 强制退出指定会话 */
export function forceLogout(tokenValue) {
  return request.delete(`/system/online/${tokenValue}`)
}
