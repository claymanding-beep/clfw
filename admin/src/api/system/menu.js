import request from '@/api/request'

// 菜单管理
export function menuTree() {
  return request.get('/system/menu/tree')
}
export function getMenu(id) {
  return request.get(`/system/menu/${id}`)
}
export function addMenu(data) {
  return request.post('/system/menu', data)
}
export function updateMenu(data) {
  return request.put('/system/menu', data)
}
export function delMenu(id) {
  return request.delete(`/system/menu/${id}`)
}
