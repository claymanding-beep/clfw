import request from '@/api/request'

// 机构管理
export function orgTree() {
  return request.get('/system/org/tree')
}
export function getOrg(id) {
  return request.get(`/system/org/${id}`)
}
export function addOrg(data) {
  return request.post('/system/org', data)
}
export function updateOrg(data) {
  return request.put('/system/org', data)
}
export function delOrg(id) {
  return request.delete(`/system/org/${id}`)
}
