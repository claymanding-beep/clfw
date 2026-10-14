import request from '@/api/request'

// 用户管理
export function pageUsers(params) {
  return request.get('/system/user/page', { params })
}
export function getUser(id) {
  return request.get(`/system/user/${id}`)
}
export function addUser(data) {
  return request.post('/system/user', data)
}
export function updateUser(data) {
  return request.put('/system/user', data)
}
export function delUser(ids) {
  return request.delete(`/system/user/${ids}`)
}
export function resetUserPwd(userId, password) {
  return request.put('/system/user/resetPwd', null, { params: { userId, password } })
}
export function changeUserStatus(userId, status) {
  return request.put('/system/user/changeStatus', null, { params: { userId, status } })
}
