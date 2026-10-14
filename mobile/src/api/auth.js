/**
 * 认证相关接口
 * TODO: 后端完成后填入实际接口路径
 */
import request from './request'

/** 手机号登录（获取微信手机号后调用） */
export function loginByPhone(data) {
  // TODO: 后端完成后填入路径，如 '/auth/login/phone'
  return request({ url: '/auth/login/phone', method: 'POST', data })
}

/** 获取用户信息 */
export function getUserInfo() {
  // TODO: 后端完成后填入路径
  return request({ url: '/auth/user/info', method: 'GET' })
}

/** 退出登录 */
export function logout() {
  // TODO: 后端完成后填入路径
  return request({ url: '/auth/logout', method: 'POST' })
}

/** 实名认证 */
export function realNameAuth(data) {
  // TODO: 后端完成后填入路径
  return request({ url: '/auth/realname', method: 'POST', data })
}
