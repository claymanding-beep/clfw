import request from '@/api/request'

// 科室管理
export function listDept(orgId) {
  return request.get('/system/dept/list', { params: { orgId } })
}
export function getDept(id) {
  return request.get(`/system/dept/${id}`)
}
export function addDept(data) {
  return request.post('/system/dept', data)
}
export function updateDept(data) {
  return request.put('/system/dept', data)
}
export function delDept(id) {
  return request.delete(`/system/dept/${id}`)
}
