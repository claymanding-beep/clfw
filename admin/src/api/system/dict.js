import request from '@/api/request'

// 数据字典 - 类型
export function pageDictType(params) {
  return request.get('/system/dict/type/page', { params })
}
export function addDictType(data) {
  return request.post('/system/dict/type', data)
}
export function updateDictType(data) {
  return request.put('/system/dict/type', data)
}
export function delDictType(ids) {
  return request.delete(`/system/dict/type/${ids}`)
}

// 数据字典 - 数据
export function dictDataByType(dictType) {
  return request.get(`/system/dict/data/${dictType}`)
}
export function pageDictData(params) {
  return request.get('/system/dict/data/page', { params })
}
export function addDictData(data) {
  return request.post('/system/dict/data', data)
}
export function updateDictData(data) {
  return request.put('/system/dict/data', data)
}
export function delDictData(ids) {
  return request.delete(`/system/dict/data/${ids}`)
}
