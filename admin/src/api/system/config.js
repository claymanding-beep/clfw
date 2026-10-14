import request from '@/api/request'

// 参数设置
export function pageConfig(params) {
  return request.get('/system/config/page', { params })
}
export function getConfigByKey(configKey) {
  return request.get(`/system/config/key/${configKey}`)
}
export function addConfig(data) {
  return request.post('/system/config', data)
}
export function updateConfig(data) {
  return request.put('/system/config', data)
}
export function delConfig(ids) {
  return request.delete(`/system/config/${ids}`)
}
