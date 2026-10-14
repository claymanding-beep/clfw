/**
 * 认证相关接口
 */
import request from './request'

/** 账号密码登录 -> 返回 { tokenName, tokenValue } */
export function login(data) {
  return request.post('/auth/login', data)
}

/** 获取图形验证码 -> { captchaEnabled, uuid, img } */
export function getCaptcha() {
  return request.get('/auth/captcha')
}

/** 退出登录 */
export function logout() {
  return request.post('/auth/logout')
}

/** 获取当前登录用户信息 -> { user, roles[], permissions[] } */
export function getInfo() {
  return request.get('/auth/getInfo')
}

/** 获取当前登录用户的动态路由菜单 -> SysMenu[] 树 */
export function getRouters() {
  return request.get('/auth/getRouters')
}

/** 获取个人信息 -> SysUser */
export function getProfile() {
  return request.get('/auth/profile')
}

/** 修改个人信息 */
export function updateProfile(data) {
  return request.put('/auth/profile', data)
}

/** 修改个人密码（query 参数） */
export function updatePwd(params) {
  return request.put('/auth/profile/updatePwd', null, { params })
}
