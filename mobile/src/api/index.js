/**
 * 首页相关接口
 * TODO: 后端完成后填入实际接口路径
 */
import request from './request'

/** 获取首页数据（签约信息、待办、指标等） */
export function getHomeData() {
  // TODO: 后端完成后填入路径
  return request({ url: '/home/data', method: 'GET' })
}

/** 获取签约服务信息 */
export function getContractInfo() {
  // TODO: 后端完成后填入路径
  return request({ url: '/home/contract', method: 'GET' })
}

/** 获取待办任务列表 */
export function getTodoTasks(params) {
  // TODO: 后端完成后填入路径
  return request({ url: '/home/tasks', method: 'GET', data: params })
}

/** 获取健康指标趋势 */
export function getHealthMetrics(params) {
  // TODO: 后端完成后填入路径
  return request({ url: '/home/metrics', method: 'GET', data: params })
}

/** 获取健康宣教列表 */
export function getHealthEducation(params) {
  // TODO: 后端完成后填入路径
  return request({ url: '/home/education', method: 'GET', data: params })
}
