import request from '@/api/request'

// 角色管理
export function pageRoles(params) {
  return request.get('/system/role/page', { params })
}
export function listRoles() {
  return request.get('/system/role/list')
}
export function getRole(id) {
  return request.get(`/system/role/${id}`)
}
export function addRole(data) {
  return request.post('/system/role', data)
}
export function updateRole(data) {
  return request.put('/system/role', data)
}
export function delRole(ids) {
  return request.delete(`/system/role/${ids}`)
}
export function assignRoleMenus(roleId, menuIds) {
  return request.put(`/system/role/${roleId}/menus`, menuIds)
}
export function roleUsers(roleId, params) {
  return request.get(`/system/role/${roleId}/users`, { params })
}
