import request from '@/api/request'

/**
 * 上传文件到 MinIO
 * @param {File} file 原始文件对象
 * @returns {Promise<{url:string, fileName:string}>}
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/system/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
