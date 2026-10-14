/**
 * 工作台 / 仪表盘接口
 * TODO: 后端完成后填入实际接口路径
 */
import request from './request'

/** 获取工作台统计数据 */
export function getDashboardStats() {
  // TODO: 后端完成后填入路径，如 '/dashboard/stats'
  return request.get('/dashboard/stats')
}

/** 获取待办任务列表 */
export function getTodoTasks(params) {
  // TODO: 后端完成后填入路径
  return request.get('/dashboard/tasks', { params })
}

/** 获取实时预警列表 */
export function getWarnings(params) {
  // TODO: 后端完成后填入路径
  return request.get('/dashboard/warnings', { params })
}

/** 获取随访完成率（近7日） */
export function getFollowUpRate() {
  // TODO: 后端完成后填入路径
  return request.get('/dashboard/followup-rate')
}

/** 获取专病建档分布 */
export function getDiseaseDistribution() {
  // TODO: 后端完成后填入路径
  return request.get('/dashboard/disease-distribution')
}
