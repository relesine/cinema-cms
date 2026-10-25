// 上传相关：上传地址与图片URL拼接辅助
export const UPLOAD_URL = '/api/file/upload'

// 后端返回的图片是 /upload/xxx 相对路径，拼接 /api 前缀经 Vite 代理访问
export function fullImg(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}
