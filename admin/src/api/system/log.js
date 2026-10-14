import request from '@/api/request'

// 操作日志
export function pageOperLog(params) {
  return request.get('/system/log/oper/page', { params })
}
export function getOperLog(id) {
  return request.get(`/system/log/oper/${id}`)
}
export function cleanOperLog() {
  return request.delete('/system/log/oper/clean')
}

// 登录日志
export function pageLoginLog(params) {
  return request.get('/system/log/login/page', { params })
}
export function cleanLoginLog() {
  return request.delete('/system/log/login/clean')
}
