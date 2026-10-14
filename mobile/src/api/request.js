/**
 * 统一请求封装
 * 基于 uni.request，支持 Promise、拦截器、token 注入
 * TODO: 后端完成后配置 baseURL
 */

const BASE_URL = '' // TODO: 后端完成后填入，如 'https://api.example.com'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        const { code, data, message } = res.data || {}
        // TODO: 后端完成后按实际返回结构调整 code 判断
        if (code === 0 || code === 200 || res.statusCode === 200) {
          resolve(data !== undefined ? data : res.data)
        } else {
          reject(new Error(message || '请求失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export default request
